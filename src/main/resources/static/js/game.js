(function () {
    const playerEl = document.getElementById('player');
    if (!playerEl) return;

    const CELL_SIZE          = 56;
    const STEP_DELAY_MS      = 120;
    const PENDING_SAFE_BOUND = 320;

    const { playerX: initX, playerY: initY,
            gameWon: initWon, lastTimeMs: initTimeMs, lastScore: initScore,
            timeRecord: initTimeRec, scoreRecord: initScoreRec } = window.GAME_INIT;

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

    const ac = new AbortController();
    const sig = { signal: ac.signal };
    document.addEventListener('turbo:before-visit', () => {
        if (timerInterval) clearInterval(timerInterval);
        ac.abort();
    }, { once: true });

    let saltoId = 0;
    let walkoId = 0;
    playerEl.addEventListener('animationend', e => {
        if (e.animationName === 'konek-salto') {
            if (+playerEl.dataset.saltoId === saltoId) playerEl.classList.remove('running');
        } else if (e.animationName === 'konek-run') {
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

    function refreshTargets() {
        targetsEl.textContent = document.querySelectorAll('.target-gem').length;
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

    if (initWon) showWin(initTimeMs, initScore, initTimeRec, initScoreRec);

    window.sendMove = function sendMove(direction) {
        if (animating) {
            pendingDir     = direction;
            pendingSavedMs = Date.now();
            return;
        }
        animating = true;
        startTimer();

        fetch('/game/move', {
            method:  'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body:    'direction=' + direction
        })
        .then(r => r.json())
        .then(data => {
            animatePath(data.path, data.collectedTargets, () => {
                stepCountEl.textContent = data.stepCount;
                refreshTargets();
                animating = false;

                if (data.gameWon) {
                    pendingDir = null;
                    stopTimer();
                    if (data.lastTimeMs != null) timerEl.textContent = fmtTime(data.lastTimeMs);
                    showWin(data.lastTimeMs, data.lastScore, data.timeRecord, data.scoreRecord);
                    return;
                }

                const next = pendingDir;
                const age  = Date.now() - pendingSavedMs;
                pendingDir = null; pendingSavedMs = 0;
                if (next !== null && age < PENDING_SAFE_BOUND) {
                    sendMove(next);
                } else {
                    stopMoveAnim();
                }
            });
        })
        .catch(() => { animating = false; });
    };

    function animatePath(path, collectedTargets, onDone) {
        if (!path || path.length === 0) { onDone(); return; }

        const collected = new Set((collectedTargets || []).map(p => p[0] + ',' + p[1]));

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
        } else if (vertSteps > 0) {
            lastVertDir = vertDir;
            // scaleX(-1) flips the rotation axis, so when facing left we invert the angle
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

            if (collected.has(x + ',' + y)) {
                const gem = document.getElementById('target-' + y + '-' + x);
                if (gem) {
                    gem.classList.add('collecting');
                    setTimeout(() => gem.remove(), 200);
                }
            }

            setTimeout(nextStep, STEP_DELAY_MS);
        }
        nextStep();
    }

    function showWin(timeMs, score, timeRecord, scoreRecord) {
        stopMoveAnim();
        winOverlay.classList.add('visible');
        if (timeMs != null) {
            document.getElementById('win-time').textContent  = fmtTime(timeMs);
            document.getElementById('win-score').textContent = score;
            const badges = [];
            if (timeRecord)  badges.push('★ New time record!');
            if (scoreRecord) badges.push('★ New score record!');
            document.getElementById('record-msg').textContent = badges.join('   ');
        }
    }

    const restartKeys = ['r', 'R', 'к', 'К'];
    const quitKeys    = ['q', 'Q', 'й', 'Й'];
    const menuKeys    = ['m', 'M', 'ь', 'Ь'];

    document.addEventListener('keydown', e => {
        if (winOverlay.classList.contains('visible')) {
            // Esc = soft exit (close win → levels), Q = hard exit (same here, no nesting)
            if (e.key === 'Enter' || e.key === 'Escape' || quitKeys.includes(e.key)) {
                e.preventDefault();
                Turbo.visit('/game/levels');
                return;
            }
            if (restartKeys.includes(e.key)) {
                e.preventDefault();
                document.getElementById('restart-form').requestSubmit();
                return;
            }
            if (menuKeys.includes(e.key)) {
                e.preventDefault();
                Turbo.visit('/menu');
                return;
            }
            return;
        }

        if (e.key === 'Escape') {
            e.preventDefault();
            Turbo.visit('/game/levels');
            return;
        }

        const dirs = { ArrowUp:'UP', ArrowDown:'DOWN', ArrowLeft:'LEFT', ArrowRight:'RIGHT' };
        const dir = dirs[e.key];
        if (dir) {
            e.preventDefault();
            window.sendMove(dir);
            return;
        }
        if (restartKeys.includes(e.key)) {
            e.preventDefault();
            document.getElementById('restart-form').requestSubmit();
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
})();
