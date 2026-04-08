const CELL_SIZE          = 56;
const STEP_DELAY_MS      = 75;
const PENDING_SAFE_BOUND = 200;

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

const playerEl    = document.getElementById('player');
const stepCountEl = document.getElementById('step-count');
const timerEl     = document.getElementById('timer');
const targetsEl   = document.getElementById('targets-left');
const winOverlay  = document.getElementById('win-overlay');

// Auto-remove .running when one-shot salto animation finishes
playerEl.addEventListener('animationend', () => playerEl.classList.remove('running'));

function refreshTargets() {
    targetsEl.textContent = document.querySelectorAll('.target-gem').length;
}
refreshTargets();

// ---- Timer ----
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

// ---- Restore win state on page load (e.g. after refresh mid-game) ----
if (initWon) showWin(initTimeMs, initScore, initTimeRec, initScoreRec);

// ---- Move (exposed globally for onclick buttons) ----
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
            if (next !== null && age < PENDING_SAFE_BOUND) sendMove(next);
        });
    })
    .catch(() => { animating = false; });
};

// ---- Animate path ----
function animatePath(path, collectedTargets, onDone) {
    if (!path || path.length === 0) { onDone(); return; }

    const collected = new Set((collectedTargets || []).map(p => p[0] + ',' + p[1]));

    let moveDir    = 0;
    let horizSteps = 0;
    let prevX = lastX;
    for (const [x] of path) {
        if (x !== prevX) { horizSteps++; moveDir = x > prevX ? 1 : -1; }
        prevX = x;
    }

    if (moveDir !== 0) {
        playerEl.style.transform = moveDir > 0 ? 'scaleX(1)' : 'scaleX(-1)';
    }

    if (horizSteps >= 3) playerEl.classList.add('running');

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

// ---- Win screen ----
function showWin(timeMs, score, timeRecord, scoreRecord) {
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

// ---- Keyboard ----
document.addEventListener('keydown', e => {
    const restartKeys = ['r', 'R', 'к', 'К'];
    const quitKeys    = ['q', 'Q', 'й', 'Й'];
    const menuKeys    = ['m', 'M', 'ь', 'Ь'];

    const dirs = { ArrowUp:'UP', ArrowDown:'DOWN', ArrowLeft:'LEFT', ArrowRight:'RIGHT' };
    const dir = dirs[e.key];
    if (dir) {
        e.preventDefault();
        window.sendMove(dir);
        return;
    }
    if (restartKeys.includes(e.key)) {
        e.preventDefault();
        document.getElementById('restart-form').submit();
    }
    if (quitKeys.includes(e.key)) {
        e.preventDefault();
        location.href = '/game/levels';
    }
    if (menuKeys.includes(e.key)) {
        e.preventDefault();
        location.href = '/menu';
    }
});
