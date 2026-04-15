(function () {
    const { ac, sig } = initAbort();

    const stars = Array.from(document.querySelectorAll('.rv-star'));
    const ratingInput = document.getElementById('rv-rating');
    const commentEl = document.getElementById('rv-comment');
    const submitBtn = document.getElementById('rv-submit');
    const thanksEl = document.getElementById('rv-thanks');
    const overallEl = document.querySelector('.rv-overall-stars');

    let currentRating = ratingInput ? parseInt(ratingInput.value) || 0 : 0;
    let submitted = false;

    function fillStars(n, cls) {
        stars.forEach((s, i) => {
            const want = i < n ? cls : '';
            const has = s.classList.contains('filled') ? 'filled'
                      : s.classList.contains('preview') ? 'preview' : '';
            if (has === want) return;
            if (has) s.classList.remove(has);
            if (want) s.classList.add(want);
        });
    }

    function applyRating(n) {
        currentRating = n;
        if (ratingInput) ratingInput.value = n;
        fillStars(n, 'filled');
    }

    const starsWrap = document.getElementById('rv-stars');
    let hoverValue = -1;
    if (starsWrap) {
        starsWrap.addEventListener('mousemove', e => {
            const target = e.target.closest('.rv-star');
            const val = target ? parseInt(target.dataset.value) : -1;
            if (val !== hoverValue) {
                hoverValue = val;
                if (val > 0) fillStars(val, 'filled');
            }
        }, sig);

        starsWrap.addEventListener('mouseleave', () => {
            hoverValue = -1;
            fillStars(currentRating, 'filled');
        }, sig);

        starsWrap.addEventListener('click', e => {
            const target = e.target.closest('.rv-star');
            if (target) applyRating(parseInt(target.dataset.value));
        }, sig);
    }

    function doSubmit() {
        if (currentRating === 0) return;
        const comment = commentEl ? commentEl.value.trim() : '';

        fetch('/review/submit/ajax', {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ rating: currentRating, review: comment })
        })
        .then(r => r.json())
        .then(data => {
            if (data.error) return;

            if (overallEl && data.overallRating != null) {
                const val = parseFloat(data.overallRating);
                overallEl.textContent = val > 0 ? val.toFixed(2) + ' / 5' : 'no ratings yet';
            }

            submitted = true;
            if (submitBtn) submitBtn.style.display = 'none';
            if (thanksEl) thanksEl.style.display = 'inline';
            nav.setSelected(nav.btns.length - 1);
        });
    }

    if (submitBtn) {
        submitBtn.addEventListener('click', doSubmit, sig);
    }

    // Reviews-modal refs, needed before nav init.
    const rvModal = document.getElementById('rv-modal');
    const rvModalList = document.getElementById('rv-modal-list');
    const rvAllBtn = document.getElementById('rv-all-btn');
    const rvModalClose = document.getElementById('rv-modal-close');

    function isRvModalOpen() {
        return rvModal && rvModal.classList.contains('open');
    }

    const nav = initNav('.pf-nav', {
        signal: ac.signal,
        onExit: () => Turbo.visit('/menu'),
        onEsc: () => {
            if (isRvModalOpen()) { closeReviewsModal(); return; }
            if (commentEl && document.activeElement === commentEl) {
                commentEl.blur();
            }
            else {
                Turbo.visit('/menu');
            }
        },
        skipQ: () => (commentEl && document.activeElement === commentEl) || isRvModalOpen(),
        skipNav: () => (commentEl && document.activeElement === commentEl) || isRvModalOpen(),
        onLeftRight: (key, sel) => {
            let changed = false;
            if (key === 'ArrowLeft' && currentRating > 1) { applyRating(currentRating - 1); changed = true; }
            if (key === 'ArrowRight' && currentRating < 5) { applyRating(currentRating + 1); changed = true; }
            if (changed && sel !== 0) {
                if (submitted) {
                    submitted = false;
                    if (thanksEl) thanksEl.style.display = '';
                    if (submitBtn) { submitBtn.style.display = ''; submitBtn.textContent = 'UPDATE'; }
                }
                nav.setSelected(0);
            }
            return changed;
        },
        onSelect: (sel) => {
            if (submitted && sel === 0) {
                submitted = false;
                if (thanksEl) thanksEl.style.display = '';
                if (submitBtn) {
                    submitBtn.style.display = '';
                    submitBtn.textContent = 'UPDATE';
                }
            }
        }
    });

    function openReviewsModal() {
        const wasKb = !nav.isMouseMode;
        nav.btns.forEach(b => b.classList.remove('active'));
        rvModal.classList.add('open');
        if (rvModalClose && wasKb) {
            rvModalClose.classList.add('active');
        }
        rvModalList.innerHTML = '<div class="rv-modal-loading">Loading...</div>';

        fetch('/review/all/ajax', { credentials: 'include' })
            .then(r => r.json())
            .then(reviews => {
                if (!reviews || reviews.length === 0) {
                    rvModalList.innerHTML = '<div class="rv-modal-empty">No reviews yet</div>';
                    return;
                }
                rvModalList.innerHTML = reviews.map(r => {
                    const stars = Array.from({length: 5}, (_, i) =>
                        '<span class="' + (i < r.rating ? 'rv-star-on' : '') + '">★</span>'
                    ).join('');
                    const comment = r.comment ? '<div class="rv-review-comment">' + escHtml(r.comment) + '</div>' : '';
                    const date = r.date ? timeAgo(r.date) : '';
                    return '<div class="rv-review-item">' +
                        '<div class="rv-review-header">' +
                            '<span class="rv-review-name">' + escHtml(r.userName) + '</span>' +
                            '<span class="rv-review-stars">' + stars + '</span>' +
                        '</div>' +
                        comment +
                        (date ? '<div class="rv-review-date">' + date + '</div>' : '') +
                    '</div>';
                }).join('');
            })
            .catch(() => {
                rvModalList.innerHTML = '<div class="rv-modal-empty">Failed to load</div>';
            });
    }

    function closeReviewsModal() {
        rvModal.classList.remove('open');
        if (rvModalClose) rvModalClose.classList.remove('active');
        if (nav && document.body.classList.contains('kb-mode')) {
            nav.updateNav();
        }
    }

    if (rvAllBtn) rvAllBtn.addEventListener('click', openReviewsModal, sig);
    if (rvModalClose) rvModalClose.addEventListener('click', closeReviewsModal, sig);
    if (rvModal) rvModal.addEventListener('click', e => { if (e.target === rvModal) closeReviewsModal(); }, sig);

    // Modal keyboard scroll: discrete eased jump on tap, continuous on hold.
    let scrollDir = 0;
    let scrollCur = 0;
    let scrollMax = 0;
    let scrollMin = 0;
    let scrollAccel = 0;
    let scrollRaf = null;
    let scrollPrev = 0;
    let holdTimer = null;
    const HOLD_DELAY = 100;

    let jumpFrom = 0, jumpTo = 0, jumpStart = 0, jumpDur = 0, jumpRaf = null;

    function jumpLoop(ts) {
        if (!jumpStart) jumpStart = ts;
        const t = Math.min((ts - jumpStart) / jumpDur, 1);
        const eased = 1 - Math.pow(1 - t, 3);
        rvModalList.scrollTop = jumpFrom + (jumpTo - jumpFrom) * eased;
        if (t < 1 && isRvModalOpen()) jumpRaf = requestAnimationFrame(jumpLoop);
        else jumpRaf = null;
    }

    function startJump(px) {
        if (jumpRaf) cancelAnimationFrame(jumpRaf);
        jumpFrom = rvModalList.scrollTop;
        jumpTo = jumpFrom + px;
        jumpDur = 180;
        jumpStart = 0;
        jumpRaf = requestAnimationFrame(jumpLoop);
    }

    function holdLoop(ts) {
        if (!scrollDir || !isRvModalOpen()) {
            scrollRaf = null; scrollPrev = 0; scrollCur = 0; return;
        }
        const dt = scrollPrev ? Math.min((ts - scrollPrev) / 1000, 0.05) : 0;
        scrollPrev = ts;
        if (dt > 0) {
            scrollCur = Math.min(Math.abs(scrollCur) + scrollAccel * dt, scrollMax) * scrollDir;
            rvModalList.scrollTop += scrollCur * dt;
        }
        scrollRaf = requestAnimationFrame(holdLoop);
    }

    document.addEventListener('keydown', function(e) {
        if (!isRvModalOpen()) return;

        if (rvModalClose && !rvModalClose.classList.contains('active')) {
            document.body.classList.add('kb-mode');
            rvModalClose.classList.add('active');
        }

        if (e.key === 'Enter' || e.key === 'Escape') {
            e.preventDefault(); e.stopImmediatePropagation();
            scrollDir = 0;
            if (holdTimer) { clearTimeout(holdTimer); holdTimer = null; }
            if (jumpRaf) { cancelAnimationFrame(jumpRaf); jumpRaf = null; }
            closeReviewsModal(); return;
        }

        let jump = 0;
        if (e.key === 'ArrowDown')       { scrollDir = 1;  scrollMin = 200; scrollMax = 450; scrollAccel = 400; jump = 85; }
        else if (e.key === 'ArrowUp')    { scrollDir = -1; scrollMin = 200; scrollMax = 450; scrollAccel = 400; jump = -85; }
        else if (e.key === 'PageDown')   { scrollDir = 1;  scrollMin = 600; scrollMax = 900; scrollAccel = 600; jump = 160; }
        else if (e.key === 'PageUp')     { scrollDir = -1; scrollMin = 600; scrollMax = 900; scrollAccel = 600; jump = -160; }
        else return;

        e.preventDefault(); e.stopImmediatePropagation();

        // OS auto-repeat ignored, hold loop drives it instead.
        if (e.repeat) return;

        startJump(jump);
        if (holdTimer) clearTimeout(holdTimer);
        holdTimer = setTimeout(() => {
            holdTimer = null;
            if (jumpRaf) { cancelAnimationFrame(jumpRaf); jumpRaf = null; }
            if (!scrollRaf) {
                scrollCur = scrollMin * scrollDir;
                scrollPrev = 0;
                scrollRaf = requestAnimationFrame(holdLoop);
            }
        }, HOLD_DELAY);
    }, true);

    document.addEventListener('keyup', function(e) {
        if (e.key === 'ArrowDown' || e.key === 'ArrowUp' || e.key === 'PageDown' || e.key === 'PageUp') {
            scrollDir = 0;
            if (holdTimer) { clearTimeout(holdTimer); holdTimer = null; }
        }
    });

    if (rvModal) {
        rvModal.addEventListener('mousemove', () => {
            if (rvModalClose) rvModalClose.classList.remove('active');
        }, sig);
    }
})();
