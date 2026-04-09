function showGameRoot() {
    const root = document.querySelector('.game-root');
    if (root) requestAnimationFrame(() => requestAnimationFrame(() => root.style.opacity = '1'));
}
document.addEventListener('DOMContentLoaded', showGameRoot);
document.addEventListener('turbo:load', showGameRoot);
