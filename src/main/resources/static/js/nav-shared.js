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
    let selected = 0;
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
    updateNav();

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
            if (opts.onSelect) opts.onSelect(selected);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            selected = (selected - 1 + btns.length) % btns.length;
            updateNav();
            if (opts.onSelect) opts.onSelect(selected);
        }

        if (e.key === 'Enter') {
            e.preventDefault();
            e.stopImmediatePropagation();
            btns[selected].click();
        }
    }, sig);

    // Track hovered button so kb-mode starts from it — only after real mouse motion,
    // not on stale cursor from Turbo navigation
    btns.forEach((btn, i) => {
        btn.addEventListener('mouseenter', () => {
            if (mouseMode) selected = i;
        }, sig);
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
            if (hovered >= 0) selected = hovered;
        }
    }, sig);

    return {
        btns,
        get selected() { return selected; },
        set selected(v) { selected = v; },
        updateNav,
        setSelected(i) { selected = i; updateNav(); },
        get isMouseMode() { return mouseMode; }
    };
}
