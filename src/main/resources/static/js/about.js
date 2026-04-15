(function () {
    const { sig } = initAbort();
    initNav('.menu-btn', {
        signal: sig.signal,
        onExit: () => Turbo.visit('/menu')
    });

    const CLICKS_NEEDED = 5;
    const RESET_DELAY   = 1000;
    let clickCount = 0;
    let resetTimer = null;

    document.getElementById('secret-btn').addEventListener('click', () => {
        clickCount++;
        clearTimeout(resetTimer);
        resetTimer = setTimeout(() => { clickCount = 0; }, RESET_DELAY);

        if (clickCount >= CLICKS_NEEDED) {
            clickCount = 0;
            clearTimeout(resetTimer);
            Turbo.visit('/secret');
        }
    });
})();
