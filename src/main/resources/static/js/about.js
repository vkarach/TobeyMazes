(function () {
    const { sig } = initAbort();
    initNav('.menu-btn', {
        signal: sig.signal,
        onExit: () => Turbo.visit('/menu')
    });
})();
