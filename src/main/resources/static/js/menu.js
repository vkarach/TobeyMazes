const btns = Array.from(document.querySelectorAll('.menu-btn'));
let selected = 0;

let mouseMode = false;

function updateSelected(buttons, index) {
    buttons.forEach(button => button.classList.remove('active'));
    if (buttons[index]) {
        buttons[index].classList.add('active');
    }
}

document.addEventListener('keydown', e => {
    mouseMode = false;
    updateSelected(btns, selected);
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
});

// if mouse moved clear select
document.addEventListener('mousemove', () => {
    if (!mouseMode) {
        mouseMode = true;
        btns.forEach(btn => btn.classList.remove('active'));
    }
});