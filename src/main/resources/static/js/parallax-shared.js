(function () {
    const KEY     = 'parallaxMs';
    const baseMs  = parseFloat(sessionStorage.getItem(KEY) || '0');
    const startTs = performance.now();

    // Persist elapsed time when navigating away so next page continues from same position
    window.addEventListener('pagehide', () => {
        sessionStorage.setItem(KEY, baseMs + (performance.now() - startTs));
    });

    document.querySelectorAll('.bg-layer[data-bg]').forEach(layer => {
        const url   = layer.dataset.bg;
        const speed = parseFloat(layer.dataset.speed);
        const img   = new Image();

        img.onload = () => {
            const h = layer.clientHeight || window.innerHeight || 720;
            const w = img.naturalWidth * (h / img.naturalHeight);

            layer.style.backgroundImage = `url('${url}')`;
            layer.style.backgroundSize  = `${w}px 100%`;
            layer.style.opacity         = '1';

            (function tick(ts) {
                const totalMs = baseMs + (ts - startTs);
                const x = -((speed * totalMs / 1000) % w);
                layer.style.backgroundPositionX = x + 'px';
                requestAnimationFrame(tick);
            })(performance.now());
        };

        img.src = url;
    });
})();
