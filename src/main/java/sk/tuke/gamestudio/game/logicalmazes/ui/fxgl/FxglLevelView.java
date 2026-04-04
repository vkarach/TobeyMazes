package sk.tuke.gamestudio.game.logicalmazes.ui.fxgl;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Rectangle2D;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.logicalmazes.core.Field;
import sk.tuke.gamestudio.game.logicalmazes.core.FileReader;
import sk.tuke.gamestudio.game.logicalmazes.core.Player;
import sk.tuke.gamestudio.game.logicalmazes.core.Tile;
import sk.tuke.gamestudio.game.logicalmazes.core.TileType;
import sk.tuke.gamestudio.game.logicalmazes.ui.LevelView;


@Profile("fxgl")
@Component
public class FxglLevelView implements LevelView {

    private static final int  CELL    = 52;
    private static final int  WALL    = 5;
    // how long (ns) to animate one tile step — slightly above GameController's 75ms interval
    private static final long STEP_NS = 82_000_000L;

    // salto spritesheet: 13 frames × 70×70 px, horizontal strip
    private static final int  ANIM_FRAMES   = 13;
    private static final int  ANIM_FRAME_W  = 70;
    private static final int  ANIM_FRAME_H  = 70;
    private static final long ANIM_FRAME_NS = 30_000_000L;

    private boolean spriteActive     = false;
    private int     spriteFrame      = 0;
    private long    spriteFrameNs    = -1;
    private long    lastHorizStepNs  = -1; // timestamp of last horizontal step

    // colors matching the game's design language
    private static final Color BG         = FxglUi.DEFAULT_BACKGROUND;        // rgb(68,35,97)
    private static final Color FLOOR      = Color.rgb(45, 20, 68);
    private static final Color WALL_CLR   = FxglUi.DEFAULT_TITLE_COLOR;       // gold rgb(245,197,24)
    private static final Color TARGET_CLR = FxglUi.DEFAULT_BUTTON_COLOR;      // light-blue rgb(145,205,255)
    private static final Color HUD_CLR    = FxglUi.DEFAULT_TEXT_COLOR;        // white
    private static final Color HUD_ACC    = FxglUi.DEFAULT_TITLE_COLOR;       // gold

    // ---- state ----
    private Player currentPlayer;   // volatile fields — safe to read from AnimationTimer
    private int    originX, originY;

    // step-based render position (pixels, FX thread only)
    private double renderX, renderY;
    private double animStartX, animStartY;
    private double animTargetX, animTargetY;
    private long   animStartNs  = -1;
    private int    lastLogicalX = Integer.MIN_VALUE;
    private int    lastLogicalY = Integer.MIN_VALUE;

    private ImageView  playerView;
    private Circle[][] targetNodes;
    private AnimationTimer playerAnimTimer;

    private Text timerText;
    private Text pointsText;
    private Text targetsText;


    // -------------------------------------------------------------------------

    @Override
    public void launchLevel(Field field) {
        Platform.runLater(() -> {
            stopAnimTimer();
            FXGL.getGameScene().clearUINodes();

            int cols = field.getColCount();
            int rows = field.getRowCount();
            int sw   = FXGL.getAppWidth();
            int sh   = FXGL.getAppHeight();

            originX = (sw - cols * CELL) / 2 - 60;
            originY = (sh - rows * CELL) / 2;

            // background
            addUI(rect(0, 0, sw, sh, BG, 0));

            // cell floors
            for (int r = 0; r < rows; r++)
                for (int c = 0; c < cols; c++)
                    addUI(rect(originX + c * CELL + WALL, originY + r * CELL + WALL,
                               CELL - WALL, CELL - WALL, FLOOR, 3));

            drawWalls(field, rows, cols);

            // targets
            targetNodes = new Circle[rows][cols];
            Tile[][] tiles = field.getTiles();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (tiles[r][c].getType() == TileType.TARGET) {
                        Circle t = new Circle(cellCX(c), cellCY(r), CELL * 0.18, TARGET_CLR);
                        DropShadow targetGlow = new DropShadow();
                        targetGlow.setColor(TARGET_CLR); targetGlow.setRadius(12); targetGlow.setSpread(0.4);
                        t.setEffect(targetGlow);
                        targetNodes[r][c] = t;
                        addUI(t);
                    }
                }
            }

            // player sprite — salto spritesheet, frame 0 is idle
            Image saltoSheet = new Image(FileReader.getInputStream("ui/fxgl/animations/KonekTobeySalto70x70.png"));
            playerView = new ImageView(saltoSheet);
            playerView.setViewport(new Rectangle2D(0, 0, ANIM_FRAME_W, ANIM_FRAME_H));
            playerView.setFitWidth(CELL);
            playerView.setFitHeight(CELL);
            playerView.setPreserveRatio(true);
            addUI(playerView);

            buildHud(cols, rows);

            FXGL.getGameScene().getContentRoot().setCursor(Cursor.NONE);
        });
    }

    @Override
    public void renderField(Field field, Player player) {
        if (playerView == null) return;

        boolean firstCall = (currentPlayer == null);
        currentPlayer = player;

        if (firstCall) {
            Platform.runLater(() -> {
                // snap to start tile with no animation on first frame
                renderX = cellCX(player.getX());
                renderY = cellCY(player.getY());
                lastLogicalX = player.getX();
                lastLogicalY = player.getY();
                animTargetX = renderX;
                animTargetY = renderY;
                placeSprite(renderX, renderY);
                startAnimTimer(field);
            });
        }
    }

    @Override
    public void updateHud(long startTime, int targetCount, int points) {
        if (timerText == null) return;
        String time = formatTime(startTime);
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
            int sh = FXGL.getAppHeight();
            Text tips = new Text("← ↓ ↑ → move · Q quit · R restart");
            tips.setFont(Font.font("Consolas", FontWeight.NORMAL, 13));
            tips.setFill(Color.rgb(160, 140, 190));
            tips.setTranslateY(sh - 20);
            tips.setTranslateX((sw - tips.getLayoutBounds().getWidth()) / 2.0);
            addUI(tips);
        });
    }

    @Override
    public void stopLevel() {
        stopAnimTimer();
        Platform.runLater(() ->
            FXGL.getGameScene().getContentRoot().setCursor(Cursor.DEFAULT));
    }

    // -------------------------------------------------------------------------

    /**
     * Started once per level. Each frame (matches display refresh rate):
     *  - detects logical position change
     *  - starts a new step animation from current render pos to new tile
     *  - advances linear interpolation over STEP_NS
     */
    private void startAnimTimer(Field field) {
        playerAnimTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                Player p = currentPlayer;
                if (p == null || playerView == null) return;

                int lx = p.getX();
                int ly = p.getY();

                // new logical step detected — start movement animation
                if (lx != lastLogicalX || ly != lastLogicalY) {
                    animStartX  = renderX;
                    animStartY  = renderY;
                    animTargetX = cellCX(lx);
                    animTargetY = cellCY(ly);
                    animStartNs = now;

                    if (lx != lastLogicalX) {
                        // horizontal step — flip sprite direction
                        int dir = lx > lastLogicalX ? 1 : -1;
                        if (dir < 0) playerView.setScaleX(-1);
                        else         playerView.setScaleX(1);

                        // first step of a new slide sequence — decide whether to animate
                        if (lastHorizStepNs < 0 || (now - lastHorizStepNs) > STEP_NS) {
                            int totalDist = countHorizSteps(field, lastLogicalX, lastLogicalY, dir);
                            if (totalDist >= 3) {
                                spriteActive  = true;
                                spriteFrame   = 0;
                                spriteFrameNs = now;
                                playerView.setViewport(new Rectangle2D(0, 0, ANIM_FRAME_W, ANIM_FRAME_H));
                            }
                        }
                        lastHorizStepNs = now;
                    }
                    // vertical steps: keep current animation state

                    lastLogicalX = lx;
                    lastLogicalY = ly;
                }

                // advance sprite frame while active
                if (spriteActive && (now - spriteFrameNs) >= ANIM_FRAME_NS) {
                    spriteFrame = (spriteFrame + 1) % ANIM_FRAMES;
                    spriteFrameNs = now;
                    playerView.setViewport(new Rectangle2D(
                        spriteFrame * ANIM_FRAME_W, 0, ANIM_FRAME_W, ANIM_FRAME_H));
                    // stop at frame 0 only once player has been still for longer than one step
                    if (spriteFrame == 0 && (now - lastHorizStepNs) > STEP_NS) {
                        spriteActive  = false;
                        spriteFrameNs = -1;
                    }
                }

                // linear progress — constant speed, no deceleration, no pauses
                if (animStartNs >= 0) {
                    double t = Math.min((double)(now - animStartNs) / STEP_NS, 1.0);
                    renderX = animStartX + (animTargetX - animStartX) * t;
                    renderY = animStartY + (animTargetY - animStartY) * t;
                }

                placeSprite(renderX, renderY);

                // update target visibility
                Tile[][] tiles = field.getTiles();
                for (int r = 0; r < field.getRowCount(); r++)
                    for (int c = 0; c < field.getColCount(); c++) {
                        Circle t = targetNodes[r][c];
                        if (t != null) t.setVisible(tiles[r][c].getType() == TileType.TARGET);
                    }
            }
        };
        playerAnimTimer.start();
    }

    /** Count how many tiles the player can slide from (x,y) in direction dir (+1=right,-1=left). */
    private int countHorizSteps(Field field, int x, int y, int dir) {
        boolean[][] vWalls = field.getVWalls();
        int cols = field.getColCount();
        int count = 0;
        int cx = x;
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
        if (playerAnimTimer != null) {
            playerAnimTimer.stop();
            playerAnimTimer = null;
        }
        currentPlayer = null;
        animStartNs   = -1;
        lastLogicalX  = Integer.MIN_VALUE;
        lastLogicalY  = Integer.MIN_VALUE;
        spriteActive    = false;
        spriteFrame     = 0;
        spriteFrameNs   = -1;
        lastHorizStepNs = -1;
    }

    private void placeSprite(double cx, double cy) {
        playerView.setLayoutX(cx - playerView.getFitWidth()  / 2.0);
        playerView.setLayoutY(cy - playerView.getFitHeight() / 2.0);
    }

    // -------------------------------------------------------------------------

    private void drawWalls(Field field, int rows, int cols) {
        boolean[][] hWalls = field.getHWalls();
        boolean[][] vWalls = field.getVWalls();

        // horizontal segments — no arc, centered on the grid line
        for (int r = 0; r <= rows; r++)
            for (int c = 0; c < cols; c++)
                if (hWalls[r][c])
                    addUI(rect(originX + c * CELL - WALL / 2.0, originY + r * CELL - WALL / 2.0,
                               CELL + WALL, WALL, WALL_CLR, 0));

        // vertical segments
        for (int r = 0; r < rows; r++)
            for (int c = 0; c <= cols; c++)
                if (vWalls[r][c])
                    addUI(rect(originX + c * CELL - WALL / 2.0, originY + r * CELL - WALL / 2.0,
                               WALL, CELL + WALL, WALL_CLR, 0));

        // corner fill squares — everywhere at least one adjacent wall touches the junction
        for (int r = 0; r <= rows; r++) {
            for (int c = 0; c <= cols; c++) {
                boolean right = c < cols  && hWalls[r][c];
                boolean left  = c > 0     && hWalls[r][c - 1];
                boolean down  = r < rows  && vWalls[r][c];
                boolean up    = r > 0     && vWalls[r - 1][c];
                if (right || left || down || up)
                    addUI(rect(originX + c * CELL - WALL / 2.0, originY + r * CELL - WALL / 2.0,
                               WALL, WALL, WALL_CLR, 0));
            }
        }
    }

    private void buildHud(int cols, int rows) {
        int hx = originX + cols * CELL + 30;
        int hy = originY + rows * CELL / 2 - 80;

        // labels — system font (readable at small sizes)
        // values — PressStart2P at 16 (crisp at exact pixel size)
        addUI(hudLabel("TIME", hx, hy));
        timerText = FxglUi.createText("0:00", 16, HUD_CLR);
        timerText.setTranslateX(hx); timerText.setTranslateY(hy + 34);
        addUI(timerText);

        addUI(hudLabel("PTS", hx, hy + 70));
        pointsText = FxglUi.createText("0", 16, HUD_CLR);
        pointsText.setTranslateX(hx); pointsText.setTranslateY(hy + 104);
        addUI(pointsText);

        addUI(hudLabel("LEFT", hx, hy + 140));
        targetsText = FxglUi.createText("0", 16, TARGET_CLR);
        targetsText.setTranslateX(hx); targetsText.setTranslateY(hy + 174);
        addUI(targetsText);
    }

    private Text hudLabel(String text, double x, double y) {
        Text t = new Text(text);
        t.setFont(Font.font("Consolas", FontWeight.BOLD, 13));
        t.setFill(HUD_ACC);
        t.setTranslateX(x);
        t.setTranslateY(y);
        return t;
    }

    // -------------------------------------------------------------------------

    private double cellCX(int col) { return originX + col * CELL + CELL / 2.0; }
    private double cellCY(int row) { return originY + row * CELL + CELL / 2.0; }

    private Rectangle rect(double x, double y, double w, double h, Color fill, double arc) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setFill(fill);
        if (arc > 0) { r.setArcWidth(arc); r.setArcHeight(arc); }
        return r;
    }

    private void addUI(Node node) {
        FXGL.getGameScene().addUINode(node);
    }

    private String formatTime(long startNs) {
        long ms = (System.nanoTime() - startNs) / 1_000_000L;
        if (ms < 0) ms = 0;
        long totalSec = ms / 1000L;
        long min = totalSec / 60L;
        long sec = totalSec % 60L;
        long cs  = (ms % 1000L) / 10L;
        return min > 0
            ? String.format("%d:%02d", min, sec)
            : String.format("%d:%02d", totalSec, cs);
    }
}
