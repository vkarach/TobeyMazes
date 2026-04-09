(function () {
    const levelCards = Array.from(document.querySelectorAll('.level-card'));
    if (levelCards.length === 0) return;

    const ac = new AbortController();
    const sig = { signal: ac.signal };
    document.addEventListener('turbo:before-visit', () => ac.abort(), { once: true });

    let mouseMode = false;
    let selected = 0;
    let selectedForm = null;
    let bestResults = [];

    function getPerRow(cards) {
        if (cards.length === 0) return 0;
        const firstTop = cards[0].offsetTop;
        let perRow = 0;
        for (const card of cards) {
            if (card.offsetTop === firstTop) perRow++;
            else break;
        }
        return perRow;
    }

    let perRow = getPerRow(levelCards);

    function updateSelected(cards, index) {
        cards.forEach(c => c.classList.remove('hover'));
        if (cards[index]) cards[index].classList.add('hover');
    }

    function clearSelected(cards) {
        cards.forEach(c => c.classList.remove('hover'));
    }

    function closeModal() {
        const modal = document.getElementById('level-modal');
        if (modal) modal.classList.remove('open');
        document.body.classList.remove('modal-open');
        document.getElementById('modal-overlay').classList.remove('open');
    }

    function formatTimeMs(ms) {
        if (ms == null) return '—';
        const value = Number(ms);
        if (!Number.isFinite(value) || value < 0) return '—';
        if (value < 1000) return Math.round(value) + 'ms';
        const trim = num => num.replace(/\.0+$/, '').replace(/(\.\d*[1-9])0+$/, '$1');
        const totalSeconds = value / 1000;
        if (totalSeconds < 60) {
            return trim(totalSeconds.toFixed(totalSeconds < 10 ? 2 : 1)) + 's';
        }
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = Math.floor(totalSeconds % 60);
        return minutes + 'm ' + String(seconds).padStart(2, '0') + 's';
    }

    updateSelected(levelCards, selected);

    async function loadBestResults() {
        const userIdRaw = document.body?.dataset?.userId;
        if (!userIdRaw) { bestResults = []; return; }
        try {
            const res = await fetch(`/api/users/${userIdRaw}/best-results`);
            if (!res.ok) { bestResults = []; return; }
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

    let submitting = false;
    async function submitSelected() {
        if (!selectedForm || submitting) return;
        submitting = true;
        try {
            const r = await fetch(selectedForm.action, {
                method: 'POST',
                body: new FormData(selectedForm),
                redirect: 'follow',
                credentials: 'same-origin'
            });
            Turbo.visit(r.url, { action: 'replace' });
        } catch (_) {
            selectedForm.submit();
        }
    }

    function openModal(card) {
        if (document.body.classList.contains('modal-open')) return;
        selectedForm = card.closest('form');

        const levelId = card.dataset.levelId;
        const result = findResultByLevelId(levelId);

        document.getElementById('info-label').textContent =
          (card.dataset.levelTitle ?? ('Level ' + levelId));

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

    function isModalOpen() {
        return document.getElementById('level-modal')?.classList.contains('open') === true;
    }

    document.addEventListener('keydown', e => {
        if (e.key === 'Escape') {
            e.preventDefault();
            e.stopPropagation();
            e.stopImmediatePropagation();
            if (isModalOpen()) closeModal();
            else Turbo.visit('/menu');
            return;
        }

        if (e.key === 'Q' || e.key === 'q' || e.key === 'й' || e.key === 'Й') {
            e.preventDefault();
            Turbo.visit('/menu');
            return;
        }

        const modalOpen = isModalOpen();
        if (modalOpen) {
            if (e.key === 'Enter' && selectedForm) {
                e.preventDefault();
                submitSelected();
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
            if (levelCards[selected]) openModal(levelCards[selected]);
        }
    }, { ...sig, capture: true });

    document.addEventListener('mousemove', () => {
        if (!mouseMode) {
            mouseMode = true;
            clearSelected(levelCards);
        }
    }, sig);

    levelCards.forEach(card => {
        card.addEventListener('click', () => {
            if (document.body.classList.contains('modal-open')) return;
            openModal(card);
        }, sig);
    });

    document.addEventListener('click', e => {
        const modal = document.getElementById('level-modal');
        if (!modal || !modal.classList.contains('open')) return;
        if (e.target.closest('.level-card')) return;
        if (!modal.contains(e.target)) closeModal();
    }, sig);

    document.getElementById('modal-back-btn').addEventListener('click', closeModal, sig);
    document.getElementById('modal-start-btn').addEventListener('click', () => {
        if (selectedForm) submitSelected();
    }, sig);

    loadBestResults();
})();
