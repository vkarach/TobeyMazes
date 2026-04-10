(function () {
    const ac = new AbortController();
    document.addEventListener('turbo:before-visit', () => ac.abort(), { once: true });

    initNav('.menu-btn', {
        signal: ac.signal,
        onExit: () => Turbo.visit('/menu')
    });
})();
