package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Rectangle2D;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.logicalmazes.core.*;
import sk.tuke.gamestudio.game.logicalmazes.ui.LevelView;

@Profile("fxgl")
@Component
public class FxglLevelView implements LevelView {

    @Autowired @Qualifier("gameBackground")
    private ParallaxBackground gameBg;

    private static final int  CELL       = 56;
    private static final int  WALL       = 4;
    private static final int  PANEL_PAD  = 18;
    private static final int  PLAYER_SZ  = 48;
    private static final int  TARGET_SZ  = 28;
    private static final int  HUD_W      = 150;
    private static final int  GAME_GAP   = 28;

    private static final int  SALTO_FRAMES  = 13;
    private static final int  SALTO_FIRST   = 2;   // web starts salto anim at frame 2
    private static final int  SALTO_LAST    = 12;
    private static final int  RUN_FRAMES    = 6;

    private static final long STEP_NS        = 130_000_000L;
    private static final long RUN_FRAME_NS   = 33_000_000L;   // 0.2s / 6
    private static final long SALTO_FRAME_NS = 50_000_000L;   // 0.55s / 11

    private static final Color CELL_CLR   = Color.rgb(28, 15, 5);
    private static final Color WALL_CLR   = Color.rgb(245, 197, 24);
    private static final Color PANEL_BG   = Color.rgb(20, 10, 5, 0.82);
    private static final Color HUD_SEP    = Color.rgb(245, 197, 24, 0.25);
    private static final Color TARGET_CLR = Color.rgb(145, 205, 255);
    private static final Color DIM_CLR    = Color.rgb(255, 255, 255, 0.35);

    private Image saltoSheet, runSheet, flowerImg;
    private int   saltoFW, saltoFH, runFW, runFH;

    private Level currentLevel;

    private double mazePanelX, mazePanelY, mazePanelW, mazePanelH;
    private int    originX, originY;

    private Player currentPlayer;
    private double renderX, renderY;
    private double animStartX, animStartY, animTargetX, animTargetY;
    private long   animStartNs    = -1;
    private int    lastLX         = Integer.MIN_VALUE;
    private int    lastLY         = Integer.MIN_VALUE;
    private int    facingDir      = 1;
    private int    vertDir        = 0;
    private long   lastAnyStepNs  = -1;
    private long   lastHorizNs    = -1;

    private enum SpriteMode { IDLE, WALK, SALTO }
    private SpriteMode spriteMode  = SpriteMode.IDLE;
    private int        walkFrame   = 0;
    private long       walkFrameTs = -1;
    private int        saltoFrame  = 0;
    private long       saltoFrameTs = -1;

    private ImageView        playerView;
    private ImageView[][]    targetViews;
    private ScaleTransition[][] targetPulse;
    private boolean[][]      targetCollecting;
    private AnimationTimer   animTimer;

    private Text timerText, pointsText, targetsText;

    // ─── Public API ───────────────────────────────────────────────────────────

    public void setCurrentLevel(Level level) { this.currentLevel = level; }

    @Override
    public void launchLevel(Field field) {
        ensureSprites();
        Platform.runLater(() -> {
            stopAnimTimer();
            FXGL.getGameScene().clearUINodes();

            int cols = field.getColCount(), rows = field.getRowCount();
            int sw = FXGL.getAppWidth(), sh = FXGL.getAppHeight();

            mazePanelW = cols * CELL + 2 * PANEL_PAD;
            mazePanelH = rows * CELL + 2 * PANEL_PAD;

            double totalW = mazePanelW + GAME_GAP + HUD_W;
            double blockX = (sw - totalW) / 2.0;
            mazePanelX = blockX;

            // Vertical: center with level title above panel
            double titleApproxH = FxglUi.createText("X", 16, WALL_CLR).getLayoutBounds().getHeight();
            double totalH = titleApproxH + 8 + mazePanelH;
            mazePanelY = (sh - totalH) / 2.0 + titleApproxH + 8;

            originX = (int)(mazePanelX + PANEL_PAD);
            originY = (int)(mazePanelY + PANEL_PAD);

            double hudH = computeHudHeight();
            double hudX = mazePanelX + mazePanelW + GAME_GAP;
            double hudY = mazePanelY + (mazePanelH - hudH) / 2.0;

            // BG — match web #bg-game parallax
            gameBg.start(FXGL.getGameScene());

            // Maze panel (card)
            Rectangle mazePanel = new Rectangle(mazePanelW, mazePanelH);
            mazePanel.setFill(PANEL_BG);
            mazePanel.setStroke(WALL_CLR);
            mazePanel.setStrokeWidth(2);
            mazePanel.setTranslateX(mazePanelX);
            mazePanel.setTranslateY(mazePanelY);
            addUI(mazePanel);

            // Cell floors
            for (int r = 0; r < rows; r++)
                for (int c = 0; c < cols; c++)
                    addUI(rect(originX + c * CELL, originY + r * CELL, CELL, CELL, CELL_CLR, 0));

            // Walls
            drawWalls(field, rows, cols);

            // Targets
            targetViews      = new ImageView[rows][cols];
            targetPulse      = new ScaleTransition[rows][cols];
            targetCollecting = new boolean[rows][cols];
            Tile[][] tiles = field.getTiles();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (tiles[r][c].getType() == TileType.TARGET) {
                        ImageView iv = new ImageView(flowerImg);
                        iv.setFitWidth(TARGET_SZ);
                        iv.setFitHeight(TARGET_SZ);
                        iv.setPreserveRatio(false);
                        iv.setSmooth(false);
                        iv.setTranslateX(originX + c * CELL + (CELL - TARGET_SZ) / 2.0);
                        iv.setTranslateY(originY + r * CELL + (CELL - TARGET_SZ) / 2.0);
                        targetViews[r][c] = iv;
                        addUI(iv);

                        // Pulse: 1.0 → 1.25 → 1.0, 3s cycle
                        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.5), iv);
                        pulse.setFromX(1.0); pulse.setFromY(1.0);
                        pulse.setToX(1.25);  pulse.setToY(1.25);
                        pulse.setAutoReverse(true);
                        pulse.setCycleCount(Animation.INDEFINITE);
                        pulse.play();
                        targetPulse[r][c] = pulse;
                    }
                }
            }

            // Player
            playerView = new ImageView(saltoSheet);
            playerView.setViewport(new Rectangle2D(0, 0, saltoFW, saltoFH));
            playerView.setFitWidth(PLAYER_SZ);
            playerView.setFitHeight(PLAYER_SZ);
            playerView.setPreserveRatio(false);
            playerView.setSmooth(false);
            // Player sprite changes viewport every frame — we can't cache it as
            // a bitmap or the animation freezes; setSmooth(false) is enough here.
            addUI(playerView);

            // HUD panel
            buildHud(hudX, hudY);

            // Level title above panel
            if (currentLevel != null) {
                Text titleNode = FxglUi.createText(currentLevel.getTitle(), 16, WALL_CLR);
                double tw = titleNode.getLayoutBounds().getWidth();
                titleNode.setTranslateX(mazePanelX + (mazePanelW - tw) / 2.0);
                double ty = mazePanelY - 8 - titleApproxH;
                titleNode.setTranslateY(ty - titleNode.getLayoutBounds().getMinY());
                addUI(titleNode);
            }

            resetSpriteState();
            FXGL.getGameScene().getContentRoot().setCursor(Cursor.NONE);
        });
    }

    @Override
    public void renderField(Field field, Player player) {
        if (playerView == null) return;
        boolean first = (currentPlayer == null);
        currentPlayer = player;
        if (first) {
            Platform.runLater(() -> {
                renderX      = cellCX(player.getX());
                renderY      = cellCY(player.getY());
                lastLX       = player.getX();
                lastLY       = player.getY();
                animTargetX  = renderX;
                animTargetY  = renderY;
                placeSprite(renderX, renderY);
                startAnimTimer(field);
            });
        }
    }

    @Override
    public void updateHud(long elapsedNs, int targetCount, int points) {
        if (timerText == null) return;
        String time = formatTime(elapsedNs);
        Platform.runLater(() -> {
            timerText.setText(time);
            pointsText.setText(String.valueOf(points));
            targetsText.setText(String.valueOf(targetCount));
        });
    }

    @Override
    public void renderTips() {
        Platform.runLater(() -> {
            int sw = FXGL.getAppWidth();
            double barY = mazePanelY + mazePanelH + 14;
            Text tip = FxglUi.createText("[R] restart   [Q] quit", 8, DIM_CLR);
            double tw = tip.getLayoutBounds().getWidth();
            tip.setTranslateX((sw - tw) / 2.0);
            tip.setTranslateY(barY - tip.getLayoutBounds().getMinY());
            addUI(tip);
        });
    }

    @Override
    public void stopLevel() {
        stopAnimTimer();
        if (targetPulse != null)
            for (ScaleTransition[] row : targetPulse)
                for (ScaleTransition t : row)
                    if (t != null) t.stop();
        Platform.runLater(() ->
            FXGL.getGameScene().getContentRoot().setCursor(Cursor.DEFAULT));
        currentPlayer = null;
    }

    // ─── Build ────────────────────────────────────────────────────────────────

    private double computeHudHeight() {
        double lblH    = FxglUi.createText("X", 8,  Color.WHITE).getLayoutBounds().getHeight();
        double valH    = FxglUi.createText("X", 20, Color.WHITE).getLayoutBounds().getHeight();
        double rowPad  = 14, innerGap = 9;
        double rowH    = rowPad + lblH + innerGap + valH + rowPad;
        return rowH * 3 + 2; // 3 rows + 2 separator px
    }

    private void buildHud(double x, double y) {
        double lblH    = FxglUi.createText("X", 8,  Color.WHITE).getLayoutBounds().getHeight();
        double valH    = FxglUi.createText("X", 20, Color.WHITE).getLayoutBounds().getHeight();
        double rowPad  = 14, innerGap = 9, sepH = 1;
        double rowH    = rowPad + lblH + innerGap + valH + rowPad;
        double hudH    = rowH * 3 + sepH * 2;
        double hudPadX = 18;

        Rectangle hudPanel = new Rectangle(HUD_W, hudH);
        hudPanel.setFill(PANEL_BG);
        hudPanel.setStroke(WALL_CLR);
        hudPanel.setStrokeWidth(2);
        hudPanel.setTranslateX(x);
        hudPanel.setTranslateY(y);
        addUI(hudPanel);

        double iy = y + rowPad;

        // TIME row
        Text lTime = FxglUi.createText("TIME", 8, WALL_CLR);
        lTime.setTranslateX(x + hudPadX);
        lTime.setTranslateY(iy - lTime.getLayoutBounds().getMinY());
        addUI(lTime);
        iy += lblH + innerGap;

        timerText = FxglUi.createText("0:00", 20, Color.WHITE);
        timerText.setTranslateX(x + hudPadX);
        timerText.setTranslateY(iy - timerText.getLayoutBounds().getMinY());
        addUI(timerText);
        iy += valH + rowPad + sepH;

        addUI(sepRect(x + hudPadX, iy - sepH, HUD_W - 2 * hudPadX));

        iy += rowPad;

        // PTS row
        Text lPts = FxglUi.createText("PTS", 8, WALL_CLR);
        lPts.setTranslateX(x + hudPadX);
        lPts.setTranslateY(iy - lPts.getLayoutBounds().getMinY());
        addUI(lPts);
        iy += lblH + innerGap;

        pointsText = FxglUi.createText("0", 20, Color.WHITE);
        pointsText.setTranslateX(x + hudPadX);
        pointsText.setTranslateY(iy - pointsText.getLayoutBounds().getMinY());
        addUI(pointsText);
        iy += valH + rowPad + sepH;

        addUI(sepRect(x + hudPadX, iy - sepH, HUD_W - 2 * hudPadX));

        iy += rowPad;

        // LEFT row (flowers remaining)
        Text lLeft = FxglUi.createText("LEFT", 8, WALL_CLR);
        lLeft.setTranslateX(x + hudPadX);
        lLeft.setTranslateY(iy - lLeft.getLayoutBounds().getMinY());
        addUI(lLeft);
        iy += lblH + innerGap;

        targetsText = FxglUi.createText("0", 20, TARGET_CLR);
        targetsText.setTranslateX(x + hudPadX);
        targetsText.setTranslateY(iy - targetsText.getLayoutBounds().getMinY());
        addUI(targetsText);
    }

    // ─── Walls ────────────────────────────────────────────────────────────────

    private void drawWalls(Field field, int rows, int cols) {
        boolean[][] hWalls = field.getHWalls();
        boolean[][] vWalls = field.getVWalls();

        for (int r = 0; r <= rows; r++)
            for (int c = 0; c < cols; c++)
                if (hWalls[r][c])
                    addUI(rect(originX + c * CELL - WALL / 2.0, originY + r * CELL - WALL / 2.0,
                            CELL + WALL, WALL, WALL_CLR, 0));

        for (int r = 0; r < rows; r++)
            for (int c = 0; c <= cols; c++)
                if (vWalls[r][c])
                    addUI(rect(originX + c * CELL - WALL / 2.0, originY + r * CELL - WALL / 2.0,
                            WALL, CELL + WALL, WALL_CLR, 0));

        for (int r = 0; r <= rows; r++) {
            for (int c = 0; c <= cols; c++) {
                boolean right = c < cols && hWalls[r][c];
                boolean left  = c > 0   && hWalls[r][c - 1];
                boolean down  = r < rows && vWalls[r][c];
                boolean up    = r > 0   && vWalls[r - 1][c];
                if (right || left || down || up)
                    addUI(rect(originX + c * CELL - WALL / 2.0, originY + r * CELL - WALL / 2.0,
                            WALL, WALL, WALL_CLR, 0));
            }
        }
    }

    // ─── Animation timer ──────────────────────────────────────────────────────

    private void startAnimTimer(Field field) {
        animTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                Player p = currentPlayer;
                if (p == null || playerView == null) return;

                int lx = p.getX(), ly = p.getY();

                // Detect new logical step
                if (lx != lastLX || ly != lastLY) {
                    animStartX  = renderX;
                    animStartY  = renderY;
                    animTargetX = cellCX(lx);
                    animTargetY = cellCY(ly);
                    animStartNs = now;
                    lastAnyStepNs = now;

                    if (lx != lastLX) {
                        int dir = lx > lastLX ? 1 : -1;
                        if (dir != facingDir) {
                            facingDir = dir;
                            playerView.setScaleX(dir);
                        }
                        if (vertDir != 0) {
                            vertDir = 0;
                            playerView.setRotate(0);
                        }

                        boolean newSlide = lastHorizNs < 0 || (now - lastHorizNs) > STEP_NS;
                        if (newSlide && spriteMode != SpriteMode.SALTO) {
                            int dist = countHorizSteps(field, lastLX, lastLY, dir);
                            if (dist >= 3) {
                                spriteMode   = SpriteMode.SALTO;
                                saltoFrame   = SALTO_FIRST;
                                saltoFrameTs = now;
                            }
                        }
                        lastHorizNs = now;
                        if (spriteMode == SpriteMode.IDLE) {
                            spriteMode   = SpriteMode.WALK;
                            walkFrame    = 0;
                            walkFrameTs  = now;
                        }
                    }
                    else {
                        int vd = ly > lastLY ? 1 : -1;
                        if (vd != vertDir) {
                            vertDir = vd;
                            playerView.setRotate(vd * facingDir * 90.0);
                        }
                        if (spriteMode == SpriteMode.IDLE) {
                            spriteMode  = SpriteMode.WALK;
                            walkFrame   = 0;
                            walkFrameTs = now;
                        }
                    }

                    lastLX = lx;
                    lastLY = ly;
                }

                boolean recentStep = lastAnyStepNs >= 0 && (now - lastAnyStepNs) < (long)(STEP_NS * 1.5);

                // Advance sprite frames
                switch (spriteMode) {
                    case WALK -> {
                        if (walkFrameTs >= 0 && (now - walkFrameTs) >= RUN_FRAME_NS) {
                            walkFrame   = (walkFrame + 1) % RUN_FRAMES;
                            walkFrameTs = now;
                        }
                        if (!recentStep) {
                            spriteMode  = SpriteMode.IDLE;
                            walkFrame   = 0;
                            walkFrameTs = -1;
                            if (vertDir != 0) { vertDir = 0; playerView.setRotate(0); }
                        }
                    }
                    case SALTO -> {
                        if ((now - saltoFrameTs) >= SALTO_FRAME_NS) {
                            saltoFrame++;
                            saltoFrameTs = now;
                            if (saltoFrame > SALTO_LAST) {
                                if (recentStep) {
                                    spriteMode  = SpriteMode.WALK;
                                    walkFrame   = 0;
                                    walkFrameTs = now;
                                }
                                else {
                                    spriteMode = SpriteMode.IDLE;
                                }
                                saltoFrame = 0;
                            }
                        }
                    }
                    default -> {}
                }

                // Set sprite image + viewport
                switch (spriteMode) {
                    case IDLE -> {
                        playerView.setImage(saltoSheet);
                        playerView.setViewport(new Rectangle2D(0, 0, saltoFW, saltoFH));
                    }
                    case WALK -> {
                        playerView.setImage(runSheet);
                        playerView.setViewport(new Rectangle2D(walkFrame * runFW, 0, runFW, runFH));
                    }
                    case SALTO -> {
                        int f = Math.min(saltoFrame, SALTO_LAST);
                        playerView.setImage(saltoSheet);
                        playerView.setViewport(new Rectangle2D(f * saltoFW, 0, saltoFW, saltoFH));
                    }
                }

                // Interpolate position
                if (animStartNs >= 0) {
                    double t = Math.min((double)(now - animStartNs) / STEP_NS, 1.0);
                    renderX = animStartX + (animTargetX - animStartX) * t;
                    renderY = animStartY + (animTargetY - animStartY) * t;
                }
                placeSprite(renderX, renderY);

                // Update targets
                if (targetViews == null) return;
                Tile[][] tiles = field.getTiles();
                for (int r = 0; r < field.getRowCount(); r++) {
                    for (int c = 0; c < field.getColCount(); c++) {
                        ImageView iv = targetViews[r][c];
                        if (iv == null || targetCollecting[r][c]) continue;
                        boolean isTarget = tiles[r][c].getType() == TileType.TARGET;
                        if (!isTarget && iv.isVisible()) {
                            targetCollecting[r][c] = true;
                            ScaleTransition pulse = targetPulse[r][c];
                            if (pulse != null) { pulse.stop(); targetPulse[r][c] = null; }
                            iv.setScaleX(1.0); iv.setScaleY(1.0);
                            iv.setOpacity(1.0);

                            ScaleTransition sc = new ScaleTransition(Duration.millis(200), iv);
                            sc.setFromX(1.3); sc.setFromY(1.3);
                            sc.setToX(0.0);   sc.setToY(0.0);
                            FadeTransition ft = new FadeTransition(Duration.millis(200), iv);
                            ft.setFromValue(1.0); ft.setToValue(0.0);
                            new ParallelTransition(sc, ft).play();
                        }
                    }
                }
            }
        };
        animTimer.start();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private int countHorizSteps(Field field, int x, int y, int dir) {
        boolean[][] vWalls = field.getVWalls();
        int cols = field.getColCount();
        int count = 0, cx = x;
        while (true) {
            int wallCol = dir > 0 ? cx + 1 : cx;
            if (wallCol < 0 || wallCol >= vWalls[y].length) break;
            if (vWalls[y][wallCol]) break;
            cx += dir;
            if (cx < 0 || cx >= cols) break;
            count++;
        }
        return count;
    }

    private void stopAnimTimer() {
        if (animTimer != null) { animTimer.stop(); animTimer = null; }
        currentPlayer  = null;
        animStartNs    = -1;
        lastLX         = Integer.MIN_VALUE;
        lastLY         = Integer.MIN_VALUE;
        lastAnyStepNs  = -1;
        lastHorizNs    = -1;
    }

    private void resetSpriteState() {
        spriteMode    = SpriteMode.IDLE;
        walkFrame     = 0;
        walkFrameTs   = -1;
        saltoFrame    = 0;
        saltoFrameTs  = -1;
        facingDir     = 1;
        vertDir       = 0;
    }

    private void placeSprite(double cx, double cy) {
        playerView.setLayoutX(cx - PLAYER_SZ / 2.0);
        playerView.setLayoutY(cy - PLAYER_SZ / 2.0);
    }

    private double cellCX(int col) { return originX + col * CELL + CELL / 2.0; }
    private double cellCY(int row) { return originY + row * CELL + CELL / 2.0; }

    private Rectangle rect(double x, double y, double w, double h, Color fill, double arc) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setFill(fill);
        if (arc > 0) { r.setArcWidth(arc); r.setArcHeight(arc); }
        return r;
    }

    private Rectangle sepRect(double x, double y, double w) {
        Rectangle r = new Rectangle(w, 1, HUD_SEP);
        r.setTranslateX(x);
        r.setTranslateY(y);
        return r;
    }

    private void addUI(Node node) { FXGL.getGameScene().addUINode(node); }

    private void ensureSprites() {
        if (saltoSheet != null) return;
        saltoSheet = new Image(FileReader.getInputStream("static/img/KonekTobeySalto80x80.png"));
        runSheet   = new Image(FileReader.getInputStream("static/img/konekTobeyRun80x80.png"));
        flowerImg  = new Image(FileReader.getInputStream("static/img/flower.png"));
        saltoFW    = (int)(saltoSheet.getWidth()  / SALTO_FRAMES);
        saltoFH    = (int) saltoSheet.getHeight();
        runFW      = (int)(runSheet.getWidth()    / RUN_FRAMES);
        runFH      = (int) runSheet.getHeight();
    }

    private String formatTime(long elapsedNs) {
        long ms      = Math.max(0, elapsedNs / 1_000_000L);
        long totalSec = ms / 1000L;
        long min     = totalSec / 60L;
        long sec     = totalSec % 60L;
        long cs      = (ms % 1000L) / 10L;
        return min > 0
                ? String.format("%d:%02d", min, sec)
                : String.format("%d:%02d", sec, cs);
    }
}
