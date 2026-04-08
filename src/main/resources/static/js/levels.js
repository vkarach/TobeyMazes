function getPerRow(levelCards) {
    if (levelCards.length === 0) return 0;

    const firstTop = levelCards[0].offsetTop;
    let perRow = 0;

    for (const card of levelCards) {
        if (card.offsetTop === firstTop) {
            perRow++;
        } else {
            break;
        }
    }

    return perRow;
}

function updateSelected(levelCards, index) {
    levelCards.forEach(card => card.classList.remove('hover'));
    if (levelCards[index]) {
        levelCards[index].classList.add('hover');
    }
}

function clearSelected(levelCards) {
    levelCards.forEach(card => card.classList.remove('hover'));
}

function closeModal() {
    const modal = document.getElementById('level-modal');
    if (modal) {
        modal.classList.remove('open');
    }
    document.body.classList.remove('modal-open');
    document.getElementById('modal-overlay').classList.remove('open');
}

function formatTimeMs(ms) {
    if (ms == null) return '—';
    const value = Number(ms);
    if (!Number.isFinite(value) || value < 0) return '—';

    if (value < 1000) {
        return Math.round(value) + 'ms';
    }

    const trimTrailingZeros = (num) =>
        num.replace(/\.0+$/, '').replace(/(\.\d*[1-9])0+$/, '$1');

    const totalSeconds = value / 1000;
    if (totalSeconds < 60) {
        return trimTrailingZeros(totalSeconds.toFixed(totalSeconds < 10 ? 2 : 1)) + 's';
    }

    const minutes = Math.floor(totalSeconds / 60);
    const seconds = Math.floor(totalSeconds % 60);
    return minutes + 'm ' + String(seconds).padStart(2, '0') + 's';
}

const levelCards = Array.from(document.querySelectorAll('.level-card'));
let mouseMode = false;
let selected = 0;
let selectedForm = null;
let perRow = getPerRow(levelCards);
let bestResults = [];

updateSelected(levelCards, selected);

async function loadBestResults() {
    const userIdRaw = document.body?.dataset?.userId;
    if (!userIdRaw) {
        bestResults = [];
        return;
    }

    try {
        const res = await fetch(`/api/users/${userIdRaw}/best-results`);
        if (!res.ok) {
            bestResults = [];
            return;
        }
        const data = await res.json();
        bestResults = Array.isArray(data) ? data : [];
    } catch (_) {
        bestResults = [];
    }
}

function findResultByLevelId(levelId) {
    return bestResults.find(r => Number(r?.id?.levelId) === Number(levelId));
}

function setModalDifficulty(card) {
    const difficultyEl = document.getElementById('modal-difficulty');
    const rawDifficulty = card.dataset.levelDifficulty ?? '—';
    const difficulty = String(rawDifficulty).toUpperCase();

    difficultyEl.classList.remove('diff-EASY', 'diff-NORMAL', 'diff-MEDIUM', 'diff-HARD');
    if (['EASY', 'NORMAL', 'MEDIUM', 'HARD'].includes(difficulty)) {
        difficultyEl.classList.add('diff-' + difficulty);
    }

    difficultyEl.innerHTML =
      '<span class="difficulty-label">Difficulty: </span>' +
      '<span class="difficulty-value">' + difficulty + '</span>';
}

function openModal(card) {
    if (document.body.classList.contains('modal-open')) return;

    selectedForm = card.closest('form');

    const levelId = card.dataset.levelId;
    const result = findResultByLevelId(levelId);

    document.getElementById('info-label').textContent =
      (card.dataset.levelTitle ?? ('Level ' + levelId));

//document.getElementById('modal-title').textContent =
//      (card.dataset.levelTitle ?? ('Level ' + levelId));

    document.getElementById('level-preview').src =
      '/img/levels/level_' + levelId + '.png';

    setModalDifficulty(card);

    document.getElementById('modal-time').innerHTML =
      '<span class="stat-label">Best time: </span>' +
      '<span class="stat-value">' + formatTimeMs(result?.bestTimeMs) + '</span>';

    document.getElementById('modal-score').innerHTML =
      '<span class="stat-label">Best score: </span>' +
      '<span class="stat-value">' + (result?.bestScore ?? '—') + '</span>';

    clearSelected(levelCards);
    document.body.classList.add('modal-open');
    document.getElementById('level-modal').classList.add('open');
    document.getElementById('modal-overlay').classList.add('open');
}

document.addEventListener('keydown', e => {
    const modalOpen = document.getElementById('level-modal')?.classList.contains('open');

    if (e.key === 'Escape') {
        e.preventDefault();
        if (modalOpen) {
            closeModal();
        } else {
            location.href = '/menu';
        }
        return;
    }

    if (e.key === 'Q' || e.key === 'q') {
        e.preventDefault();
        location.href = '/menu';
        return;
    }

    if (modalOpen) {
        if (e.key === 'Enter' && selectedForm) {
            e.preventDefault();
            selectedForm.submit();
        }
        return;
    }

    mouseMode = false;
    updateSelected(levelCards, selected);

    if (e.key === 'ArrowRight') {
        e.preventDefault();
        const row = Math.floor(selected / perRow);
        const rowStart = row * perRow;
        const rowEnd = Math.min(rowStart + perRow, levelCards.length) - 1;
        if (selected < rowEnd) selected++;
        updateSelected(levelCards, selected);
    }

    if (e.key === 'ArrowLeft') {
        e.preventDefault();
        const row = Math.floor(selected / perRow);
        const rowStart = row * perRow;
        if (selected > rowStart) selected--;
        updateSelected(levelCards, selected);
    }

    if (e.key === 'ArrowUp') {
        e.preventDefault();
        const next = selected - perRow;
        if (next >= 0) selected = next;
        updateSelected(levelCards, selected);
    }

    if (e.key === 'ArrowDown') {
        e.preventDefault();
        const next = selected + perRow;
        if (next < levelCards.length) selected = next;
        updateSelected(levelCards, selected);
    }

    if (e.key === 'Enter') {
        e.preventDefault();
        if (levelCards[selected]) {
            openModal(levelCards[selected]);
        }
    }
}, true);

document.addEventListener('mousemove', () => {
    if (!mouseMode) {
        mouseMode = true;
        clearSelected(levelCards);
    }
});

levelCards.forEach(card => {
    card.addEventListener('click', () => {
        if (document.body.classList.contains('modal-open')) return;
        openModal(card);
    });
});

document.addEventListener('click', e => {
    const modal = document.getElementById('level-modal');
    if (!modal || !modal.classList.contains('open')) return;
    if (e.target.closest('.level-card')) return;
    if (!modal.contains(e.target)) {
        closeModal();
    }
});

document.getElementById('modal-back-btn').addEventListener('click', closeModal);
document.getElementById('modal-start-btn').addEventListener('click', () => {
    if (selectedForm) {
        selectedForm.submit();
    }
});

loadBestResults();
