document.addEventListener('DOMContentLoaded', () => {
    const root = document.querySelector('.game-root');
    if (root) requestAnimationFrame(() => requestAnimationFrame(() => root.style.opacity = '1'));
});
