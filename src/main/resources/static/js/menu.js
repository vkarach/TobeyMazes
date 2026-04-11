(function () {
    const ac = new AbortController();
    const abort = () => ac.abort();
    // Abort on either the next visit OR the next render — the second handles
    // Turbo's cached-preview flow where scripts re-run twice per visit.
    document.addEventListener('turbo:before-visit',  abort, { once: true });
    document.addEventListener('turbo:before-render', abort, { once: true });

    initNav('.menu-btn', { signal: ac.signal });
})();
