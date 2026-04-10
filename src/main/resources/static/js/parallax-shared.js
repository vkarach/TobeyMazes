(function () {
    const KEY     = 'parallaxMs';
    const baseMs  = parseFloat(sessionStorage.getItem(KEY) || '0');
    const startTs = performance.now();

    window.addEventListener('pagehide', () => {
        sessionStorage.setItem(KEY, baseMs + (performance.now() - startTs));
    });

    window._parallaxInited = window._parallaxInited || new WeakSet();
    const inited = window._parallaxInited;
    // Cache decoded image dimensions across navigations so layers re-init instantly
    window._parallaxImgDims = window._parallaxImgDims || {};
    const imgDims = window._parallaxImgDims;

    document.querySelectorAll('.bg-layer[data-bg]').forEach(layer => {
        // skip if same DOM element already animated (data-turbo-permanent .bg).
        // WeakSet keyed by element ref — survives across script re-executions but
        // a fresh element from a Turbo snapshot restore will not be in it.
        if (inited.has(layer)) return;
        inited.add(layer);

        const url   = layer.dataset.bg;
        const speed = parseFloat(layer.dataset.speed);

        // Set background-image and opacity immediately so the layer is visible
        // as soon as the image is in the browser cache (no waiting for onload)
        layer.style.backgroundImage = `url('${url}')`;
        layer.style.backgroundRepeat = 'repeat-x';
        layer.style.opacity = '1';

        let layerWidth = 0;
        function applyDims(naturalW, naturalH) {
            const h = layer.clientHeight || window.innerHeight || 720;
            layerWidth = naturalW * (h / naturalH);
            layer.style.backgroundSize = `${layerWidth}px 100%`;
        }

        if (imgDims[url]) {
            applyDims(imgDims[url].w, imgDims[url].h);
        } else {
            const img = new Image();
            img.onload = () => {
                imgDims[url] = { w: img.naturalWidth, h: img.naturalHeight };
                applyDims(img.naturalWidth, img.naturalHeight);
            };
            img.src = url;
        }

        (function tick(ts) {
            if (layerWidth > 0) {
                const totalMs = baseMs + (ts - startTs);
                const x = -((speed * totalMs / 1000) % layerWidth);
                layer.style.backgroundPositionX = x + 'px';
            }
            requestAnimationFrame(tick);
        })(performance.now());
    });
})();
