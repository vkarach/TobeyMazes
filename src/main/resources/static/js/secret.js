(function () {
    const { sig } = initAbort();
    initNav('.menu-btn', {
        signal: sig.signal,
        onExit: () => Turbo.visit('/about')
    });

    const horse = document.getElementById('horse-sprite');
    if (!horse) return;

    const W = 120, H = 120;
    const GRAVITY = 2000, BOUNCE = 0.38, FRICTION = 0.8, STOP_V = 60;
    const THROW_K = 0.45, FLOOR = 600;
    const FLEE_RX = 200, FLEE_RY = 90, SAFE_RX = 340, SAFE_RY = 150;
    const FLEE_SPD = 380, LERP_A = 5, LERP_D = 3;
    const SALTO_N = 13, RUN_N = 6, RUN_MS = 80;

    let sheet = 'salto';
    let saltoT0 = 0, saltoDur = 0, saltoDone = false;
    let runFrame = 0, runTimer = 0, facingL = false;

    let px = 200, py = FLOOR, vx = 0, vy = 0;
    let thrown = false, dragging = false;
    let dragOx = 0, dragOy = 0, lastT = null;
    let ptrHist = [];
    let cx = -9999, cy = -9999;
    let fleeing = false, fleeDir = 0;

    function setSheet(name) {
        if (sheet === name) return;
        sheet = name;
        if (name === 'run') {
            horse.style.backgroundImage = "url('/img/konekTobeyRun80x80.png')";
            horse.style.backgroundSize = (W * RUN_N) + 'px ' + H + 'px';
            runFrame = runTimer = 0;
        }
        else {
            horse.style.backgroundImage = "url('/img/KonekTobeySalto80x80.png')";
            horse.style.backgroundSize = (W * SALTO_N) + 'px ' + H + 'px';
        }
    }

    function frame(f) { horse.style.backgroundPositionX = -(f * W) + 'px'; }
    function idle() { setSheet('salto'); frame(0); }

    function tickRun(dt) {
        setSheet('run');
        runTimer += dt;
        if (runTimer >= RUN_MS) { runTimer -= RUN_MS; runFrame = (runFrame + 1) % RUN_N; }
        frame(runFrame);
    }

    function tickSalto(ts) {
        if (saltoDone) return;
        setSheet('salto');
        var t = (ts - saltoT0) / saltoDur;
        if (t >= 1) { t = 1; saltoDone = true; }
        frame(Math.min(SALTO_N - 1, Math.floor(t * SALTO_N)));
    }

    function gSize() {
        var r = document.querySelector('.game-root');
        return { w: parseFloat(r.style.width) || 1280, h: parseFloat(r.style.height) || 720 };
    }

    function toGame(sx, sy) {
        var r = document.querySelector('.game-root'), b = r.getBoundingClientRect(), s = gSize();
        return { x: (sx - b.left) * (s.w / b.width), y: (sy - b.top) * (s.h / b.height) };
    }

    function velToGame(dvx, dvy) {
        var r = document.querySelector('.game-root'), b = r.getBoundingClientRect();
        var k = b.width / (parseFloat(r.style.width) || 1280);
        return { x: dvx / k, y: dvy / k };
    }

    function eDist(rx, ry) {
        var dx = cx - px, dy = cy - (py - H / 2);
        return Math.sqrt((dx / rx) ** 2 + (dy / ry) ** 2);
    }

    function apply() {
        horse.style.left = (px - W / 2) + 'px';
        horse.style.top  = (py - H) + 'px';
    }

    function clampX() {
        var w = gSize().w;
        px = Math.max(W / 2, Math.min(w - W / 2, px));
    }

    function flip(left) {
        if (facingL === left) return;
        facingL = left;
        horse.style.transform = left ? 'scaleX(-1)' : '';
    }

    function lerp(cur, tgt, spd, dt) {
        var d = tgt - cur;
        return Math.abs(d) < 1 ? tgt : cur + d * Math.min(1, spd * dt);
    }

    apply(); idle();

    window.addEventListener('pointermove', function (e) {
        var g = toGame(e.clientX, e.clientY);
        cx = g.x; cy = g.y;
        if (!dragging) return;
        px = g.x - dragOx;
        py = (g.y - dragOy) + H / 2;
        clampX(); apply();
        var now = performance.now();
        ptrHist.push({ x: e.clientX, y: e.clientY, t: now });
        ptrHist = ptrHist.filter(function (p) { return now - p.t <= 80; });
    });

    horse.addEventListener('pointerdown', function (e) {
        e.preventDefault(); e.stopPropagation();
        horse.setPointerCapture(e.pointerId);
        dragging = true; fleeing = false; thrown = false;
        fleeDir = 0; vx = vy = 0;
        var g = toGame(e.clientX, e.clientY);
        dragOx = g.x - px;
        dragOy = g.y - (py - H / 2);
        ptrHist = [{ x: e.clientX, y: e.clientY, t: performance.now() }];
        horse.classList.add('dragging');
        idle();
    });

    window.addEventListener('pointerup', function () {
        if (!dragging) return;
        dragging = false;
        horse.classList.remove('dragging');
        thrown = false;
        if (ptrHist.length >= 2) {
            var a = ptrHist[0], b = ptrHist[ptrHist.length - 1];
            var dt = (b.t - a.t) / 1000;
            if (dt > 0.01) {
                var gv = velToGame((b.x - a.x) / dt, (b.y - a.y) / dt);
                vx = gv.x * THROW_K;
                vy = gv.y * THROW_K;
                if (vy < -100 || Math.abs(vx) > 150) {
                    thrown = true; saltoDone = false;
                    saltoT0 = performance.now();
                    saltoDur = Math.max(250, Math.min(1500, 2 * Math.abs(vy) / GRAVITY * 1000));
                    sheet = '';
                    if (vx) flip(vx < 0);
                }
            }
        }
    });

    function tick(ts) {
        if (lastT === null) lastT = ts;
        var dtS = Math.min((ts - lastT) / 1000, 0.05);
        var dtMs = ts - lastT;
        lastT = ts;

        if (!dragging) {
            var onGround = py >= FLOOR - 1;
            var near = eDist(FLEE_RX, FLEE_RY) < 1;
            var far  = eDist(SAFE_RX, SAFE_RY) > 1;
            var dx = cx - px;

            if (onGround && !thrown) {
                if (!fleeing && near) { fleeing = true; fleeDir = dx > 0 ? -1 : 1; }

                if (fleeing) {
                    var w = gSize().w;
                    var atL = px <= W / 2 + 2, atR = px >= w - W / 2 - 2;
                    if ((atL && fleeDir < 0) || (atR && fleeDir > 0)) fleeDir = -fleeDir;
                    vx = lerp(vx, fleeDir * FLEE_SPD, LERP_A, dtS);
                    flip(fleeDir < 0);
                    tickRun(dtMs);
                    if (far) fleeing = false;
                }
                else if (Math.abs(vx) > 15) {
                    vx = lerp(vx, 0, LERP_D, dtS);
                    tickRun(dtMs);
                    flip(vx < 0);
                }
                else {
                    vx = 0; idle();
                }
            }
            else if (!onGround) {
                fleeing = false;
            }

            if (thrown) tickSalto(ts);

            vy += GRAVITY * dtS;
            px += vx * dtS;
            py += vy * dtS;
            clampX();

            if (py >= FLOOR) {
                py = FLOOR;
                if (!fleeing) vx *= FRICTION;
                if (Math.abs(vy) < STOP_V) {
                    vy = 0;
                    if (!fleeing) vx *= 0.7;
                    if (thrown) { thrown = false; idle(); }
                }
                else {
                    vy = -Math.abs(vy) * BOUNCE;
                }
            }

            if (!fleeing && !thrown && !onGround && Math.abs(vx) > 30) flip(vx < 0);
            apply();
        }

        requestAnimationFrame(tick);
    }

    requestAnimationFrame(tick);
})();
