(function () {
    const levelCards = Array.from(document.querySelectorAll('.level-card'));
    if (levelCards.length === 0) return;

    if (typeof window.__levelsAbort === 'function') {
        try { window.__levelsAbort(); } catch (_) {}
    }
    const { ac, sig, abort } = initAbort();
    window.__levelsAbort = abort;

    // { once: true } not sig — before-cache fires after before-visit aborts sig
    document.addEventListener('turbo:before-cache', () => {
        document.body.classList.remove('modal-open');
        document.getElementById('level-modal')?.classList.remove('open');
        document.getElementById('modal-overlay')?.classList.remove('open');
        document.querySelectorAll('.level-card.hover').forEach(c => c.classList.remove('hover'));
        document.querySelectorAll('.lm-nav.active').forEach(b => b.classList.remove('active'));
        if (document.activeElement instanceof HTMLElement) document.activeElement.blur();
    }, { once: true });

    document.body.classList.remove('modal-open');
    document.getElementById('level-modal')?.classList.remove('open');
    document.getElementById('modal-overlay')?.classList.remove('open');

    function enableAnim() { document.body.classList.remove('nav-no-anim'); }
    const animOpts = { capture: true, once: true, signal: ac.signal };
    document.addEventListener('keydown',     enableAnim, animOpts);
    document.addEventListener('mousemove',   enableAnim, animOpts);
    document.addEventListener('pointerdown', enableAnim, animOpts);

    let mouseMode = false;
    let selected = 0;
    let selectedForm = null;
    let bestResults = [];
    let lastMouseX = null, lastMouseY = null;
    let modalClosedAt = 0;
    const scriptStartedAt = performance.now();
    const NAV_ARRIVAL_ENTER_LOCK_MS = 120;
    document.body.classList.add('kb-mode');

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
        document.getElementById('modal-overlay')?.classList.remove('open');
        modalClosedAt = performance.now();
        if (!mouseMode) {
            document.body.classList.add('kb-mode');
            updateSelected(levelCards, selected);
        }
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
        difficultyEl.textContent = difficulty;
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
                credentials: 'same-origin',
                signal: ac.signal
            });
            if (ac.signal.aborted) return;
            Turbo.visit(r.url, { action: 'replace' });
        } catch (e) {
            if (e && e.name === 'AbortError') return;
            selectedForm.submit();
        } finally {
            submitting = false;
        }
    }

    function openModal(card) {
        if (isModalOpen()) return;
        if (!card) return;
        selectedForm = card.closest('form');

        const levelId = card.dataset.levelId;
        const result = findResultByLevelId(levelId);

        document.getElementById('info-label').textContent =
          (card.dataset.levelTitle ?? ('Level ' + levelId));

        document.getElementById('level-preview').src =
          '/img/levels/level_' + levelId + '.png';

        setModalDifficulty(card);

        document.getElementById('modal-time').textContent = formatTimeMs(result?.bestTimeMs);
        document.getElementById('modal-score').textContent = result?.bestScore ?? '—';

        clearSelected(levelCards);
        document.body.classList.add('modal-open');
        document.getElementById('level-modal').classList.add('open');
        document.getElementById('modal-overlay').classList.add('open');

        modalSel = 1;
        modalMouse = false;
        updateModalNav();
    }

    const modalBtns = Array.from(document.querySelectorAll('.lm-nav'));
    let modalSel = 1;
    let modalMouse = false;

    function updateModalNav() {
        modalBtns.forEach(b => b.classList.remove('active'));
        if (!modalMouse && modalBtns[modalSel]) modalBtns[modalSel].classList.add('active');
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
            if (isModalOpen()) closeModal();
            Turbo.visit('/menu');
            return;
        }

        const modalOpen = isModalOpen();
        if (modalOpen) {
            if (modalMouse) { modalMouse = false; document.body.classList.add('kb-mode'); updateModalNav(); }

            if (e.key === 'ArrowLeft') {
                e.preventDefault();
                modalSel = (modalSel - 1 + modalBtns.length) % modalBtns.length;
                updateModalNav();
            }
            if (e.key === 'ArrowRight') {
                e.preventDefault();
                modalSel = (modalSel + 1) % modalBtns.length;
                updateModalNav();
            }
            if (e.key === 'Enter') {
                e.preventDefault();
                if (e.repeat) return;
                if (performance.now() - scriptStartedAt < NAV_ARRIVAL_ENTER_LOCK_MS) return;
                modalBtns[modalSel].click();
            }
            return;
        }

        mouseMode = false;
        document.body.classList.add('kb-mode');
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
            if (e.repeat) return;
            if (performance.now() - modalClosedAt < 150) return;
            if (performance.now() - scriptStartedAt < NAV_ARRIVAL_ENTER_LOCK_MS) return;
            if (levelCards[selected]) openModal(levelCards[selected]);
        }
    }, { ...sig, capture: true });

    document.addEventListener('mousemove', e => {
        if (lastMouseX === null) {
            lastMouseX = e.clientX; lastMouseY = e.clientY;
            return;
        }
        if (e.clientX === lastMouseX && e.clientY === lastMouseY) return;
        lastMouseX = e.clientX; lastMouseY = e.clientY;

        if (!mouseMode) {
            mouseMode = true;
            document.body.classList.remove('kb-mode');
            clearSelected(levelCards);
        }
        if (!modalMouse) {
            modalMouse = true;
            document.body.classList.remove('kb-mode');
            modalBtns.forEach(b => b.classList.remove('active'));
        }
    }, sig);

    levelCards.forEach((card, i) => {
        card.addEventListener('mouseenter', () => {
            if (mouseMode) selected = i;
        }, sig);
        card.addEventListener('click', () => {
            if (isModalOpen()) return;
            openModal(card);
        }, sig);
    });

    modalBtns.forEach((btn, i) => {
        btn.addEventListener('mouseenter', () => {
            if (modalMouse) modalSel = i;
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
