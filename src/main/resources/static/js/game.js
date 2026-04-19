(function () {
    const playerEl = document.getElementById('player');
    if (!playerEl) return;

    const CELL_SIZE          = 56;
    const STEP_DELAY_MS      = 120;
    const PENDING_SAFE_BOUND = 320;

    const { playerX: initX, playerY: initY,
            gameWon: initWon, lastTimeMs: initTimeMs, lastScore: initScore,
            timeRecord: initTimeRec, scoreRecord: initScoreRec,
            difficulty } = window.GAME_INIT;

    const BALANCE = {
        EASY:   { max: 500,  kTime: 12, kStep: 8  },
        NORMAL: { max: 1000, kTime: 25, kStep: 15 },
        MEDIUM: { max: 2000, kTime: 44, kStep: 20 },
        HARD:   { max: 5000, kTime: 69, kStep: 35 }
    };

    function computeScore(timeMs, steps) {
        const b = BALANCE[difficulty] || BALANCE.EASY;
        return Math.max(0, b.max - steps * b.kStep - Math.floor(timeMs / 1000 * b.kTime));
    }

    let animating      = false;
    let pendingDir     = null;
    let pendingSavedMs = 0;
    let timerStartMs   = null;
    let timerInterval  = null;
    let lastX          = initX;
    let lastY          = initY;
    let facingDir      = 1;
    let lastVertDir    = 0;

    const stepCountEl = document.getElementById('step-count');
    const timerEl     = document.getElementById('timer');
    const targetsEl   = document.getElementById('targets-left');
    const winOverlay  = document.getElementById('win-overlay');

    // Wall + target matrix snapshot for client-side prediction.
    const mazeRows = Array.from(document.querySelectorAll('.maze-row'));
    const H = mazeRows.length;
    const W = H > 0 ? mazeRows[0].children.length : 0;
    const cells = mazeRows.map(row => Array.from(row.children).map(c => ({
        top:    c.classList.contains('wall-top'),
        bottom: c.classList.contains('wall-bottom'),
        left:   c.classList.contains('wall-left'),
        right:  c.classList.contains('wall-right'),
        target: !!c.querySelector('.target-gem')
    })));

    function slide(sx, sy, dir) {
        const path = [];
        const targets = [];
        let x = sx, y = sy;
        while (true) {
            const cell = cells[y]?.[x];
            if (!cell) break;
            let nx = x, ny = y, blocked = false;
            if (dir === 'UP')    { if (cell.top)    blocked = true; else ny--; }
            if (dir === 'DOWN')  { if (cell.bottom) blocked = true; else ny++; }
            if (dir === 'LEFT')  { if (cell.left)   blocked = true; else nx--; }
            if (dir === 'RIGHT') { if (cell.right)  blocked = true; else nx++; }
            if (blocked) break;
            if (ny < 0 || ny >= H || nx < 0 || nx >= W) break;
            x = nx; y = ny;
            path.push([x, y]);
            if (cells[y][x].target) {
                cells[y][x].target = false;
                targets.push([x, y]);
            }
        }
        return { path, targets };
    }

    (function drawMazeCanvas() {
        const canvas = document.getElementById('maze-canvas');
        if (!canvas || H === 0 || W === 0) return;

        const WALL = 4;
        const PAD  = WALL;
        const cssW = W * CELL_SIZE + PAD * 2;
        const cssH = H * CELL_SIZE + PAD * 2;
        const dpr  = window.devicePixelRatio || 1;

        canvas.style.width  = cssW + 'px';
        canvas.style.height = cssH + 'px';
        canvas.width  = Math.round(cssW * dpr);
        canvas.height = Math.round(cssH * dpr);

        const ctx = canvas.getContext('2d');
        ctx.scale(dpr, dpr);
        ctx.fillStyle = '#F5C518';

        for (let r = 0; r < H; r++) {
            for (let c = 0; c < W; c++) {
                const cell = cells[r][c];
                const cx   = PAD + c * CELL_SIZE;
                const cy   = PAD + r * CELL_SIZE;

                if (cell.top)    ctx.fillRect(cx,                    cy - WALL,             CELL_SIZE, WALL * 2);
                if (cell.bottom) ctx.fillRect(cx,                    cy + CELL_SIZE - WALL, CELL_SIZE, WALL * 2);
                if (cell.left)   ctx.fillRect(cx - WALL,             cy,                    WALL * 2,  CELL_SIZE);
                if (cell.right)  ctx.fillRect(cx + CELL_SIZE - WALL, cy,                    WALL * 2,  CELL_SIZE);

                // corner squares where two perpendicular walls meet
                if (cell.top    && cell.left)  ctx.fillRect(cx - WALL,             cy - WALL,             WALL * 2, WALL * 2);
                if (cell.top    && cell.right) ctx.fillRect(cx + CELL_SIZE - WALL, cy - WALL,             WALL * 2, WALL * 2);
                if (cell.bottom && cell.left)  ctx.fillRect(cx - WALL,             cy + CELL_SIZE - WALL, WALL * 2, WALL * 2);
                if (cell.bottom && cell.right) ctx.fillRect(cx + CELL_SIZE - WALL, cy + CELL_SIZE - WALL, WALL * 2, WALL * 2);
            }
        }
    })();

    const { ac, sig } = initAbort(() => { if (timerInterval) clearInterval(timerInterval); });

    // { once: true } not sig: before-cache fires after before-visit aborts sig.
    document.addEventListener('turbo:before-cache', () => {
        if (document.activeElement instanceof HTMLElement) document.activeElement.blur();
        document.querySelectorAll('.win-nav.active, .ctrl-btn:focus').forEach(b => {
            b.classList.remove('active');
            if (b instanceof HTMLElement) b.blur();
        });
        document.querySelectorAll('.swipe-hint-overlay').forEach(el => el.remove());
    }, { once: true });

    let saltoId = 0;
    let walkoId = 0;
    playerEl.addEventListener('animationend', e => {
        if (e.animationName === 'konek-salto') {
            if (+playerEl.dataset.saltoId === saltoId) playerEl.classList.remove('running');
        }
        else if (e.animationName === 'konek-run') {
            if (+playerEl.dataset.walkoId === walkoId) playerEl.classList.remove('walking');
        }
    });

    function finishVertReturn() {
        lastVertDir = 0;
        playerEl.style.rotate = '0deg';
    }

    function stopMoveAnim() {
        ++walkoId; ++saltoId;
        playerEl.classList.remove('walking', 'running');
        if (lastVertDir !== 0) finishVertReturn();
    }

    let targetsLeft = document.querySelectorAll('.target-gem').length;
    function refreshTargets() {
        targetsEl.textContent = targetsLeft;
    }
    refreshTargets();

    function startTimer() {
        if (timerStartMs !== null) return;
        timerStartMs = Date.now();
        timerInterval = setInterval(() => {
            timerEl.textContent = fmtTime(Date.now() - timerStartMs);
        }, 50);
    }

    function stopTimer() { clearInterval(timerInterval); }

    function fmtTime(ms) {
        if (ms < 0) ms = 0;
        const s  = Math.floor(ms / 1000);
        const cs = Math.floor((ms % 1000) / 10);
        return s + ':' + String(cs).padStart(2, '0');
    }

    let winInputLocked = false;

    if (initWon) showWin(initTimeMs, initScore, initTimeRec, initScoreRec, 0, 0);

    let localStepCount = 0;

    window.sendMove = function sendMove(direction) {
        if (animating) {
            pendingDir     = direction;
            pendingSavedMs = Date.now();
            return;
        }

        const { path, targets } = slide(lastX, lastY, direction);
        if (path.length === 0) return;

        animating = true;
        startTimer();
        localStepCount++;
        stepCountEl.textContent = localStepCount;

        targetsLeft -= targets.length;
        const willWin = targetsLeft <= 0;
        const stopAt = willWin && targets.length > 0 ? targets[targets.length - 1] : null;

        // Server roundtrip in parallel.
        const serverPromise = fetch('/game/move', {
            method:  'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body:    'direction=' + direction
        }).then(r => r.json()).catch(() => null);

        animatePath(path, targets, stopAt, () => {
            refreshTargets();
            animating = false;

            if (willWin) {
                pendingDir = null;
                stopMoveAnim();
                stopTimer();
                const localTimeMs = Date.now() - timerStartMs;
                const localScore = computeScore(localTimeMs, localStepCount);
                timerEl.textContent = fmtTime(localTimeMs);
                showWin(localTimeMs, localScore, false, false, 0, 0);

                serverPromise.then(data => {
                    if (!data) return;
                    if (data.lastTimeMs != null) {
                        timerEl.textContent = fmtTime(data.lastTimeMs);
                        document.getElementById('win-time').textContent = fmtTime(data.lastTimeMs);
                    }
                    if (data.lastScore != null) {
                        document.getElementById('win-score').textContent = data.lastScore;
                    }
                    stepCountEl.textContent = data.stepCount;
                    localStepCount = data.stepCount;

                    const timeImpEl = document.getElementById('win-time-improve');
                    const scoreImpEl = document.getElementById('win-score-improve');
                    if (data.timeRecord && data.timeImproveMs > 0) {
                        timeImpEl.textContent = '★ -' + fmtTime(data.timeImproveMs);
                    }
                    else if (data.timeRecord) {
                        timeImpEl.textContent = '★ NEW RECORD';
                    }
                    if (data.scoreRecord && data.scoreImprove > 0) {
                        scoreImpEl.textContent = '★ +' + data.scoreImprove;
                    }
                    else if (data.scoreRecord) {
                        scoreImpEl.textContent = '★ NEW RECORD';
                    }
                });
                return;
            }

            const next = pendingDir;
            const age  = Date.now() - pendingSavedMs;
            pendingDir = null; pendingSavedMs = 0;
            stopMoveAnim();
            if (next !== null && age < PENDING_SAFE_BOUND) {
                sendMove(next);
            }

            // Defensive: if server reports win but we didn't detect it client-side, recover
            serverPromise.then(data => {
                if (!data || !data.gameWon || winOverlay.classList.contains('visible')) return;
                pendingDir = null;
                stopMoveAnim();
                stopTimer();
                if (data.lastTimeMs != null) timerEl.textContent = fmtTime(data.lastTimeMs);
                if (data.stepCount != null) {
                    stepCountEl.textContent = data.stepCount;
                    localStepCount = data.stepCount;
                }
                showWin(data.lastTimeMs, data.lastScore, data.timeRecord, data.scoreRecord,
                        data.timeImproveMs || 0, data.scoreImprove || 0);
            }).catch(() => null);
        });
    };

    function animatePath(path, collectedTargets, stopAt, onDone) {
        if (!path || path.length === 0) { onDone(); return; }

        const collected = new Set((collectedTargets || []).map(p => p[0] + ',' + p[1]));
        const stopKey = stopAt ? (stopAt[0] + ',' + stopAt[1]) : null;

        let moveDir    = 0;
        let vertDir    = 0;
        let horizSteps = 0;
        let vertSteps  = 0;
        let prevX = lastX, prevY = lastY;
        for (const [x, y] of path) {
            if (x !== prevX) { horizSteps++; moveDir = x > prevX ? 1 : -1; }
            if (y !== prevY) { vertSteps++;  vertDir = y > prevY ? 1 : -1; }
            prevX = x; prevY = y;
        }

        if (horizSteps > 0) {
            facingDir = moveDir;
            playerEl.style.transform = moveDir > 0 ? 'scaleX(1)' : 'scaleX(-1)';
            if (lastVertDir !== 0) {
                lastVertDir = 0;
                playerEl.style.rotate = '0deg';
            }
            if (!playerEl.classList.contains('walking')) {
                playerEl.classList.remove('running');
                playerEl.classList.add('walking');
            }
            playerEl.dataset.walkoId = String(++walkoId);
        }
        else if (vertSteps > 0) {
            lastVertDir = vertDir;
            // scaleX(-1) flips the rotation axis, invert angle when facing left.
            const angle = vertDir * facingDir * 90;
            playerEl.style.rotate = angle + 'deg';
            if (!playerEl.classList.contains('walking')) {
                playerEl.classList.remove('running');
                playerEl.classList.add('walking');
            }
            playerEl.dataset.walkoId = String(++walkoId);
        }

        let i = 0;
        function nextStep() {
            if (i >= path.length) { onDone(); return; }

            const [x, y] = path[i++];
            playerEl.style.left = (x * CELL_SIZE) + 'px';
            playerEl.style.top  = (y * CELL_SIZE) + 'px';
            lastX = x; lastY = y;

            const key = x + ',' + y;
            if (collected.has(key)) {
                const gem = document.getElementById('target-' + y + '-' + x);
                if (gem) {
                    gem.classList.add('collecting');
                    setTimeout(() => gem.remove(), 200);
                }
            }

            if (stopKey && key === stopKey) { onDone(); return; }

            setTimeout(nextStep, STEP_DELAY_MS);
        }
        nextStep();
    }

    function showWin(timeMs, score, timeRecord, scoreRecord, timeImproveMs, scoreImprove) {
        stopMoveAnim();
        winOverlay.classList.add('visible');
        if (timeMs != null) {
            document.getElementById('win-time').textContent  = fmtTime(timeMs);
            document.getElementById('win-score').textContent = score;

            const timeImpEl = document.getElementById('win-time-improve');
            const scoreImpEl = document.getElementById('win-score-improve');

            if (timeRecord && timeImproveMs > 0) {
                timeImpEl.textContent = '★ -' + fmtTime(timeImproveMs);
            }
            else if (timeRecord) {
                timeImpEl.textContent = '★ NEW RECORD';
            }

            if (scoreRecord && scoreImprove > 0) {
                scoreImpEl.textContent = '★ +' + scoreImprove;
            }
            else if (scoreRecord) {
                scoreImpEl.textContent = '★ NEW RECORD';
            }
        }

        // Default to PLAY AGAIN.
        const winBtns = Array.from(document.querySelectorAll('.win-nav'));
        const replayIdx = winBtns.findIndex(b => b.id === 'win-replay');
        let winSel = replayIdx >= 0 ? replayIdx : 0;
        let winMouse = false;
        winInputLocked = true;
        setTimeout(() => { winInputLocked = false; }, 80);

        const _isTouch = ('ontouchstart' in window) || (navigator.maxTouchPoints > 0);

        function updateWinNav() {
            winBtns.forEach(b => b.classList.remove('active'));
            if (!winMouse && winBtns[winSel]) winBtns[winSel].classList.add('active');
        }
        if (!_isTouch) document.body.classList.add('kb-mode');
        winMouse = _isTouch;
        updateWinNav();

        winBtns.forEach((btn, i) => {
            btn.addEventListener('mouseenter', () => { winSel = i; }, sig);

            if (_isTouch) {
                btn.addEventListener('touchstart', () => {
                    winBtns.forEach(b => b.classList.remove('active'));
                    btn.classList.add('active');
                    winSel = i;
                }, { passive: true });
                btn.addEventListener('touchend', () => {
                    setTimeout(() => btn.classList.remove('active'), 120);
                }, { passive: true });
            }
        });

        document.addEventListener('mousemove', () => {
            if (!winMouse) {
                winMouse = true;
                document.body.classList.remove('kb-mode');
                winBtns.forEach(b => b.classList.remove('active'));
            }
        }, sig);

        document.addEventListener('keydown', winHandler, sig);
        function winHandler(e) {
            if (winInputLocked) { e.preventDefault(); return; }
            if (winMouse) {
                winMouse = false;
                document.body.classList.add('kb-mode');
                updateWinNav();
            }

            if (e.key === 'ArrowLeft') {
                e.preventDefault();
                winSel = (winSel - 1 + winBtns.length) % winBtns.length;
                updateWinNav();
                return;
            }
            if (e.key === 'ArrowRight') {
                e.preventDefault();
                winSel = (winSel + 1) % winBtns.length;
                updateWinNav();
                return;
            }
            if (e.key === 'Enter') {
                e.preventDefault();
                winBtns[winSel].click();
                return;
            }
        }

    }

    const restartKeys = ['r', 'R', 'к', 'К'];
    const quitKeys    = ['q', 'Q', 'й', 'Й'];
    const menuKeys    = ['m', 'M', 'ь', 'Ь'];

    document.addEventListener('keydown', e => {
        if (winOverlay.classList.contains('visible')) {
            if (winInputLocked) { e.preventDefault(); return; }
            if (e.key === 'Escape' || quitKeys.includes(e.key)) {
                e.preventDefault();
                Turbo.visit('/game/levels');
            }
            else if (restartKeys.includes(e.key)) {
                e.preventDefault();
                Turbo.visit('/game');
            }
            else if (menuKeys.includes(e.key)) {
                e.preventDefault();
                Turbo.visit('/menu');
            }
            else if (e.key === 'ArrowLeft' || e.key === 'ArrowRight' || e.key === 'Enter') {
                // handled by winHandler
            }
            return;
        }

        if (e.key === 'Escape') {
            e.preventDefault();
            Turbo.visit('/game/levels');
            return;
        }

        const dirs = { ArrowUp:'UP', ArrowDown:'DOWN', ArrowLeft:'LEFT', ArrowRight:'RIGHT',
                       w:'UP', W:'UP', ц:'UP', Ц:'UP',
                       a:'LEFT', A:'LEFT', ф:'LEFT', Ф:'LEFT',
                       s:'DOWN', S:'DOWN', ы:'DOWN', Ы:'DOWN', і:'DOWN', І:'DOWN',
                       d:'RIGHT', D:'RIGHT', в:'RIGHT', В:'RIGHT' };
        const dir = dirs[e.key];
        if (dir) {
            e.preventDefault();
            window.sendMove(dir);
            return;
        }
        if (restartKeys.includes(e.key)) {
            e.preventDefault();
            Turbo.visit('/game');
        }
        if (quitKeys.includes(e.key)) {
            e.preventDefault();
            Turbo.visit('/game/levels');
        }
        if (menuKeys.includes(e.key)) {
            e.preventDefault();
            Turbo.visit('/menu');
        }
    }, sig);

    const SWIPE_MIN = 30;
    const INTERACTIVE_SEL = '.ctrl-btn, .win-nav, .action-link, a, button';
    let swipeStartX = 0;
    let swipeStartY = 0;
    let swipeActive = false;
    let swipeFired  = false;

    function tryFireSwipe(endX, endY) {
        if (!swipeActive || swipeFired) return;
        if (winOverlay.classList.contains('visible')) return;
        const dx = endX - swipeStartX;
        const dy = endY - swipeStartY;
        const ax = Math.abs(dx);
        const ay = Math.abs(dy);
        if (Math.max(ax, ay) < SWIPE_MIN) return;
        let dir;
        if (ax > ay) {
            dir = dx > 0 ? 'RIGHT' : 'LEFT';
        }
        else {
            dir = dy > 0 ? 'DOWN' : 'UP';
        }
        swipeFired = true;
        window.sendMove(dir);
    }

    document.addEventListener('touchstart', e => {
        const t = e.changedTouches[0];
        if (!t) return;
        if (e.target.closest && e.target.closest(INTERACTIVE_SEL)) {
            swipeActive = false;
            return;
        }
        swipeStartX = t.clientX;
        swipeStartY = t.clientY;
        swipeActive = true;
        swipeFired  = false;
    }, sig);

    document.addEventListener('touchmove', e => {
        if (!swipeActive || swipeFired) return;
        const t = e.changedTouches[0];
        if (!t) return;
        tryFireSwipe(t.clientX, t.clientY);
    }, sig);

    document.addEventListener('touchend', e => {
        const t = e.changedTouches[0];
        if (t) tryFireSwipe(t.clientX, t.clientY);
        swipeActive = false;
    }, sig);

    document.addEventListener('touchcancel', () => {
        swipeActive = false;
    }, sig);

    if (_isTouch && !localStorage.getItem('swipe-hint-seen')) {
        const hint = document.createElement('div');
        hint.className = 'swipe-hint-overlay';
        hint.innerHTML =
            '<div class="swipe-hint-anim">' +
                '<div class="swipe-hint-horse"></div>' +
                '<div class="swipe-hint-circle"></div>' +
                '<div class="swipe-hint-stroke"></div>' +
            '</div>' +
            '<div class="swipe-hint-text">SWIPE TO MOVE</div>';
        document.body.appendChild(hint);

        let hintDismissed = false;
        function dismissHint() {
            if (hintDismissed) return;
            hintDismissed = true;
            localStorage.setItem('swipe-hint-seen', '1');
            hint.classList.add('hide');
            setTimeout(() => hint.remove(), 300);
        }
        hint.addEventListener('touchstart', e => {
            e.stopPropagation();
            dismissHint();
        }, { passive: true });
        hint.addEventListener('click', dismissHint);
        setTimeout(dismissHint, 3000);
    }
})();
