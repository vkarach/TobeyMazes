(function () {
    const btns = Array.from(document.querySelectorAll('.menu-btn'));
    if (btns.length === 0) return;

    const ac = new AbortController();
    const sig = { signal: ac.signal };
    document.addEventListener('turbo:before-visit', () => ac.abort(), { once: true });

    let selected = 0;
    let mouseMode = false;

    function updateSelected(buttons, index) {
        buttons.forEach(b => b.classList.remove('active'));
        if (buttons[index]) buttons[index].classList.add('active');
    }
    updateSelected(btns, selected);

    document.addEventListener('keydown', e => {
        updateSelected(btns, selected);
        if (mouseMode) {
            mouseMode = false;
            return;
        }

        if (e.key === 'ArrowDown') {
           e.preventDefault();
           selected = (selected + 1) % btns.length;
           updateSelected(btns, selected);
        }
        else if (e.key === 'ArrowUp') {
            e.preventDefault();
            selected = (selected - 1 + btns.length) % btns.length;
            updateSelected(btns, selected);
        }
        else if (e.key === 'Enter' && selected >= 0) {
            btns[selected].click();
        }
    }, sig);

    document.addEventListener('mousemove', () => {
        if (!mouseMode) {
            mouseMode = true;
            btns.forEach(btn => btn.classList.remove('active'));
        }
    }, sig);
})();
