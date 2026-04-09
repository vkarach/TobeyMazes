function scaleGameRoot() {
    const root = document.querySelector('.game-root');
    if (!root) return;
    const scale = Math.max(window.innerWidth / 1280, window.innerHeight / 720);
    root.style.transform = `scale(${scale})`;
}

scaleGameRoot();
window.addEventListener('resize', scaleGameRoot);
document.addEventListener('turbo:load', scaleGameRoot);
