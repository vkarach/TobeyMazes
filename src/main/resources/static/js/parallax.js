document.querySelectorAll('.bg-layer[data-bg]').forEach(layer => {
    const url = layer.dataset.bg;
    const speed = parseFloat(layer.dataset.speed);
    const img = new Image();

    img.onload = () => {
        const h = 720; // fixed game height, same as FXGL
        const w = img.naturalWidth * (h / img.naturalHeight);

        layer.style.backgroundImage = `url('${url}')`;
        layer.style.backgroundSize = `${w}px 100%`;
        layer.style.opacity = '1';

        let x = 0;
        let last = null;

        (function tick(ts) {
            if (last !== null) {
                x -= speed * Math.min((ts - last) / 1000, 1 / 30);
                if (x <= -w) x += w;
                layer.style.backgroundPositionX = x + 'px';
            }
            last = ts;
            requestAnimationFrame(tick);
        })(performance.now());
    };

    img.src = url;
});
