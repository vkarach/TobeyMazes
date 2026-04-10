function scaleGameRoot() {
    const root = document.querySelector('.game-root');
    if (!root) return;
    const scale = Math.max(window.innerWidth / 1280, window.innerHeight / 720);
    root.style.transform = `scale(${scale})`;
}

scaleGameRoot();
window.addEventListener('resize', scaleGameRoot);
document.addEventListener('turbo:load', scaleGameRoot);

// Hold Turbo's body swap until all stylesheets in the new <head> are actually
// loaded — otherwise on slow networks the new page renders unstyled for a flash.
document.addEventListener('turbo:before-render', event => {
    const links = Array.from(document.querySelectorAll('link[rel="stylesheet"]'));
    const pending = links.filter(l => !l.sheet);
    if (pending.length === 0) return;

    event.preventDefault();
    Promise.all(pending.map(l => new Promise(res => {
        const done = () => res();
        l.addEventListener('load', done, { once: true });
        l.addEventListener('error', done, { once: true });
        // safety timeout
        setTimeout(done, 1500);
    }))).then(() => event.detail.resume());
});
