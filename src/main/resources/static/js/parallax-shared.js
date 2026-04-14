(function () {
    const KEY     = 'parallaxMs';
    const baseMs  = parseFloat(sessionStorage.getItem(KEY) || '0');
    const startTs = performance.now();

    window.addEventListener('pagehide', () => {
        sessionStorage.setItem(KEY, baseMs + (performance.now() - startTs));
    });

    window._parallaxInited = window._parallaxInited || new WeakSet();
    const inited = window._parallaxInited;
    window._parallaxImgDims = window._parallaxImgDims || {};
    const imgDims = window._parallaxImgDims;

    // Resolve variant groups: within each group exactly one layer gets its alt image.
    // Skip groups where _variantUrl is already set — Turbo-permanent elements survive
    // navigation, so re-picking would cause the background to flicker on each visit.
    const groups = {};
    document.querySelectorAll('.bg-layer[data-variant-group]').forEach(layer => {
        if (layer._variantUrl) return;
        const g = layer.dataset.variantGroup;
        (groups[g] = groups[g] || []).push(layer);
    });
    for (const members of Object.values(groups)) {
        const pick = Math.floor(Math.random() * members.length);
        members.forEach((layer, i) => {
            layer._variantUrl = i === pick ? layer.dataset.bgAlt : layer.dataset.bg;
        });
    }

    function applyImage(layer, url) {
        layer.style.backgroundImage = `url('${url}')`;
        layer.style.backgroundRepeat = 'repeat-x';
        layer.style.opacity = '1';

        if (imgDims[url]) {
            const h = layer.clientHeight || window.innerHeight || 720;
            layer._layerWidth = imgDims[url].w * (h / imgDims[url].h);
            layer.style.backgroundSize = `${layer._layerWidth}px 100%`;
        } else {
            const img = new Image();
            img.onload = () => {
                imgDims[url] = { w: img.naturalWidth, h: img.naturalHeight };
                const h = layer.clientHeight || window.innerHeight || 720;
                layer._layerWidth = img.naturalWidth * (h / img.naturalHeight);
                layer.style.backgroundSize = `${layer._layerWidth}px 100%`;
            };
            img.src = url;
        }
    }

    document.querySelectorAll('.bg-layer[data-bg]').forEach(layer => {
        const url = layer._variantUrl || layer.dataset.bg;

        const firstTime = !inited.has(layer);
        if (firstTime) {
            inited.add(layer);
            layer._layerWidth = 0;
        }

        // Skip applyImage if this layer already shows the correct URL (Turbo-permanent
        // elements are reused across navigations — no need to re-apply the same image).
        if (layer._appliedUrl !== url) {
            layer._appliedUrl = url;
            applyImage(layer, url);
        }

        if (!firstTime) return;

        const speed = parseFloat(layer.dataset.speed);
        (function tick(ts) {
            if (layer._layerWidth > 0) {
                const totalMs = baseMs + (ts - startTs);
                const x = -((speed * totalMs / 1000) % layer._layerWidth);
                layer.style.backgroundPositionX = x + 'px';
            }
            requestAnimationFrame(tick);
        })(performance.now());
    });
})();
