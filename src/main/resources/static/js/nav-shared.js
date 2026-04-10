/**
 * Shared keyboard/mouse navigation for menu buttons.
 *
 * Usage: initNav(selector, { signal, onExit })
 * Returns { selected, updateNav, setSelected, isMouseMode }
 */
function initNav(selector, opts = {}) {
    const btns = Array.from(document.querySelectorAll(selector));
    if (btns.length === 0) return null;

    const sig = opts.signal ? { signal: opts.signal } : {};

    // Per-page memory of last selected button — survives Turbo navigations.
    // Falls back to opts.initialSelected on first visit, then 0.
    const storageKey = 'nav:' + window.location.pathname + ':' + selector;
    const stored = sessionStorage.getItem(storageKey);
    let selected;
    if (stored !== null && Number.isFinite(+stored)) {
        selected = +stored;
    } else if (typeof opts.initialSelected === 'function') {
        selected = opts.initialSelected(btns);
    } else if (typeof opts.initialSelected === 'number') {
        selected = opts.initialSelected;
    } else {
        selected = 0;
    }
    if (selected < 0 || selected >= btns.length) selected = 0;

    function saveSelected() {
        sessionStorage.setItem(storageKey, String(selected));
    }

    let mouseMode = false;
    // Track real mouse position to distinguish stale cursor (after Turbo nav)
    // from genuine new movement
    let lastMouseX = null, lastMouseY = null;

    function updateNav() {
        btns.forEach(b => b.classList.remove('active'));
        if (!mouseMode && btns[selected]) btns[selected].classList.add('active');
    }
    // Page starts in keyboard mode — sync body class so CSS hover overrides apply
    // even if previous page (Turbo) left it removed
    document.body.classList.add('kb-mode');
    // Suppress nav button transitions until the user actually interacts.
    // This prevents the .active highlight from animating in on every page load
    // (even one stray frame is visible as a micro-twitch).
    document.body.classList.add('nav-no-anim');
    updateNav();
    function enableAnim() { document.body.classList.remove('nav-no-anim'); }
    const animOpts = { capture: true, once: true, signal: opts.signal };
    document.addEventListener('keydown',     enableAnim, animOpts);
    document.addEventListener('mousemove',   enableAnim, animOpts);
    document.addEventListener('pointerdown', enableAnim, animOpts);

    document.addEventListener('keydown', e => {
        if (mouseMode) {
            mouseMode = false;
            document.body.classList.add('kb-mode');
            if (!(opts.skipNav && opts.skipNav())) updateNav();
        }

        // Q — hard exit
        if (e.key === 'q' || e.key === 'Q' || e.key === 'й' || e.key === 'Й') {
            if (opts.skipQ && opts.skipQ()) return;
            e.preventDefault();
            if (opts.onExit) opts.onExit();
            return;
        }

        // Esc — soft exit
        if (e.key === 'Escape') {
            if (opts.onEsc) { e.preventDefault(); opts.onEsc(); return; }
            e.preventDefault();
            if (opts.onExit) opts.onExit();
            return;
        }

        // Skip navigation when custom check says so (e.g. textarea focused)
        if (opts.skipNav && opts.skipNav()) return;

        // Custom left/right handler
        if ((e.key === 'ArrowLeft' || e.key === 'ArrowRight') && opts.onLeftRight) {
            if (opts.onLeftRight(e.key, selected)) {
                e.preventDefault();
                return;
            }
        }

        if (e.key === 'ArrowDown') {
            e.preventDefault();
            selected = (selected + 1) % btns.length;
            updateNav();
            saveSelected();
            if (opts.onSelect) opts.onSelect(selected);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            selected = (selected - 1 + btns.length) % btns.length;
            updateNav();
            saveSelected();
            if (opts.onSelect) opts.onSelect(selected);
        }

        if (e.key === 'Enter') {
            e.preventDefault();
            e.stopImmediatePropagation();
            saveSelected();
            btns[selected].click();
        }
    }, sig);

    // Track hovered button so kb-mode starts from it — only after real mouse motion,
    // not on stale cursor from Turbo navigation
    btns.forEach((btn, i) => {
        btn.addEventListener('mouseenter', () => {
            if (mouseMode) { selected = i; saveSelected(); }
        }, sig);
        btn.addEventListener('click', () => saveSelected(), sig);
    });

    document.addEventListener('mousemove', e => {
        // First mousemove after init may be synthetic (cursor sitting still on new DOM).
        // Require actual coordinate change to switch into mouse mode.
        if (lastMouseX === null) {
            lastMouseX = e.clientX; lastMouseY = e.clientY;
            return;
        }
        if (e.clientX === lastMouseX && e.clientY === lastMouseY) return;
        lastMouseX = e.clientX; lastMouseY = e.clientY;

        if (!mouseMode) {
            mouseMode = true;
            document.body.classList.remove('kb-mode');
            btns.forEach(b => b.classList.remove('active'));
            // Pick up whichever button cursor is currently over
            const hovered = btns.findIndex(b => b.matches(':hover'));
            if (hovered >= 0) { selected = hovered; saveSelected(); }
        }
    }, sig);

    return {
        btns,
        get selected() { return selected; },
        set selected(v) { selected = v; saveSelected(); },
        updateNav,
        setSelected(i) { selected = i; updateNav(); saveSelected(); },
        get isMouseMode() { return mouseMode; }
    };
}
