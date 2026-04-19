var _isTouch = ('ontouchstart' in window) || (navigator.maxTouchPoints > 0);

// Set by scaleGameRoot(), read by overlay reposition functions.
var _gameInset  = { top: 0, right: 0, bottom: 0 };
var _gameMobile = false;

function repositionFsOverlay() {
    var btn  = document.querySelector('.fs-toggle');
    var hint = document.querySelector('.fs-hint');
    if (!btn) return;
    var t = Math.max(8, _gameInset.top    + 8);
    var r = Math.max(8, _gameInset.right  + 8);
    btn.style.top   = t + 'px';
    btn.style.right = r + 'px';
    if (hint) {
        hint.style.top   = t + 'px';
        hint.style.right = (r + 42) + 'px';
    }
}

function repositionVersionTag() {
    var tag = document.querySelector('.version-tag');
    if (!tag) return;
    var bOff = _gameMobile ? 4 : 6;
    var rOff = _gameMobile ? 6 : 10;
    tag.style.bottom = Math.max(bOff, _gameInset.bottom + bOff) + 'px';
    tag.style.right  = Math.max(rOff, _gameInset.right  + rOff) + 'px';
}

// Portrait phones get a narrower virtual canvas (600x1066) so UI stays readable.
function scaleGameRoot(rootEl, bodyEl) {
    const root = rootEl || document.querySelector('.game-root');
    if (!root) return;
    const body = bodyEl || document.body;
    const vw = window.innerWidth;
    const vh = window.innerHeight;
    const mobile = vh > vw * 1.2;

    const mobileLand = _isTouch && !mobile;

    const wasMobile = body.classList.contains('mobile');
    body.classList.toggle('mobile', mobile);
    body.classList.toggle('mobile-land', mobileLand);
    body.classList.toggle('touch', _isTouch);
    if (_isTouch) body.classList.remove('kb-mode');

    root.style.position = 'fixed';
    root.style.left = '0';
    root.style.top = '0';
    root.style.transformOrigin = 'top left';

    if (mobile) {
        const W = 600;
        const H = Math.round(W * vh / vw);
        root.style.width = W + 'px';
        root.style.height = H + 'px';
        root.style.transform = `scale(${vw / W})`;
        _gameMobile = true;
        _gameInset  = { top: 0, right: 0, bottom: 0 };
    }
    else {
        root.style.width = '1280px';
        root.style.height = '720px';
        const scale = Math.max(vw / 1280, vh / 720);
        const ox = (vw - 1280 * scale) / 2;
        const oy = (vh - 720 * scale) / 2;
        root.style.transform = `translate(${ox}px,${oy}px) scale(${scale})`;
        _gameMobile = false;
        _gameInset  = { top: oy, right: ox, bottom: oy };
        if (wasMobile && !_isTouch) body.classList.add('kb-mode');
    }

    repositionFsOverlay();
    repositionVersionTag();
}

if (document.querySelector('.game-root')) {
    scaleGameRoot();
}
else {
    const _initObs = new MutationObserver(() => {
        if (document.querySelector('.game-root')) {
            scaleGameRoot();
            _initObs.disconnect();
        }
    });
    _initObs.observe(document.documentElement, { childList: true, subtree: true });
}
window.addEventListener('resize', () => scaleGameRoot());
window.addEventListener('orientationchange', () => {
    setTimeout(() => scaleGameRoot(), 100);
});
document.addEventListener('turbo:load', () => scaleGameRoot());

var _fsRootEl    = null;
var _fsDismissHint = function() {};

function maybeShowFsHint() {
    if (!_isTouch) return;
    if (!_fsRootEl) return;
    if (localStorage.getItem('fs-hint-seen')) return;
    if (document.querySelector('.fs-hint')) return;
    // While landscape-hint overlay is actually visible, don't show fs-hint yet.
    var landscapeHintActive =
        !localStorage.getItem('landscape-hint-dismissed') &&
        window.innerHeight > window.innerWidth * 1.2;
    if (landscapeHintActive) return;

    var hint = document.createElement('div');
    hint.className = 'fs-hint';
    hint.innerHTML = '<span class="fs-hint-bubble">fullscreen</span><span class="fs-hint-arrow"><span>&#8250;</span><span>&#8250;</span></span>';
    _fsRootEl.appendChild(hint);
    repositionFsOverlay();

    _fsDismissHint = function() {
        if (hint.parentNode) {
            localStorage.setItem('fs-hint-seen', '1');
            hint.classList.add('fs-hint-hide');
            setTimeout(function() { hint.remove(); }, 300);
        }
    };
    setTimeout(_fsDismissHint, 4000);
}

// Appended to <html> so Turbo body-swaps don't remove it.
(function initFullscreenToggle() {
    if (!_isTouch) return;
    var el = document.documentElement;
    var fsMethod = el.requestFullscreen || el.webkitRequestFullscreen;
    var isStandalone = (window.matchMedia && window.matchMedia('(display-mode: standalone)').matches) ||
                       navigator.standalone === true;
    // Already running as a standalone PWA: the full-screen button has nothing to do.
    if (isStandalone) return;
    // Neither fullscreen API nor iOS Safari: nothing we can offer.
    if (!fsMethod && navigator.standalone === undefined) return;

    _fsRootEl = el;

    var btn = document.createElement('button');
    btn.className = 'fs-toggle';
    btn.setAttribute('aria-label', 'Toggle fullscreen');
    el.appendChild(btn);
    repositionFsOverlay();

    function isFS() {
        return !!(document.fullscreenElement || document.webkitFullscreenElement);
    }

    var svgExpand = '<svg viewBox="0 0 20 20" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">' +
        '<polyline points="7,2 2,2 2,7"/><polyline points="13,2 18,2 18,7"/>' +
        '<polyline points="7,18 2,18 2,13"/><polyline points="13,18 18,18 18,13"/></svg>';
    var svgCollapse = '<svg viewBox="0 0 20 20" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">' +
        '<polyline points="2,7 7,7 7,2"/><polyline points="18,7 13,7 13,2"/>' +
        '<polyline points="2,13 7,13 7,18"/><polyline points="18,13 13,13 13,18"/></svg>';

    function updateIcon() {
        btn.innerHTML = isFS() ? svgCollapse : svgExpand;
        btn.classList.toggle('fs-on', isFS());
    }
    updateIcon();

    btn.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        _fsDismissHint();
        if (!fsMethod) {
            showIosInstallGuide();
            return;
        }
        if (isFS()) {
            (document.exitFullscreen || document.webkitExitFullscreen).call(document)
                .catch(function() {});
        }
        else {
            fsMethod.call(el).then(function() {
                setTimeout(scaleGameRoot, 150);
            }).catch(function() {});
        }
    });

    if (fsMethod) {
        document.addEventListener('fullscreenchange', function() {
            updateIcon(); setTimeout(scaleGameRoot, 100);
        });
        document.addEventListener('webkitfullscreenchange', function() {
            updateIcon(); setTimeout(scaleGameRoot, 100);
        });
    }

    maybeShowFsHint();
})();

function showIosInstallGuide() {
    if (document.querySelector('.ios-install-overlay')) return;
    if (!document.body) return;
    var shareSvg = '<svg class="ios-share-icon" viewBox="0 0 20 24" width="14" height="17" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
        '<path d="M10 2 L10 15"/>' +
        '<path d="M6 6 L10 2 L14 6"/>' +
        '<path d="M4 10 L4 22 L16 22 L16 10"/>' +
        '</svg>';
    var overlay = document.createElement('div');
    overlay.className = 'ios-install-overlay';
    overlay.innerHTML =
        '<div class="ios-install-title">FULLSCREEN MODE</div>' +
        '<div class="ios-install-subtitle">iOS Safari can\'t go fullscreen.<br>Add this site to your home screen instead.</div>' +
        '<div class="ios-install-steps">' +
            '<div class="ios-install-step">' +
                '<span class="ios-install-num">1</span>' +
                '<span>Tap ' + shareSvg + ' in Safari</span>' +
            '</div>' +
            '<div class="ios-install-step">' +
                '<span class="ios-install-num">2</span>' +
                '<span>Pick &quot;Add to Home Screen&quot;</span>' +
            '</div>' +
            '<div class="ios-install-step">' +
                '<span class="ios-install-num">3</span>' +
                '<span>Launch from the icon</span>' +
            '</div>' +
        '</div>' +
        '<button class="ios-install-btn" type="button">GOT IT</button>';
    document.body.appendChild(overlay);
    overlay.querySelector('.ios-install-btn').addEventListener('click', function(e) {
        e.stopPropagation();
        overlay.classList.add('hide');
        setTimeout(function() { overlay.remove(); }, 300);
    });
    overlay.addEventListener('touchstart', function(e) { e.stopPropagation(); }, { passive: true });
}

// Landscape recommendation: shown on any touch page on first visit. Only the
// GOT IT button permanently dismisses it; rotating to landscape just hides it
// via CSS (body.mobile only), so it returns if the user rotates back.
function createLandscapeHintIfNeeded() {
    if (!_isTouch) return;
    if (localStorage.getItem('landscape-hint-dismissed')) return;
    if (document.querySelector('.landscape-hint-overlay')) return;
    if (!document.body) {
        document.addEventListener('DOMContentLoaded', createLandscapeHintIfNeeded, { once: true });
        return;
    }
    var overlay = document.createElement('div');
    overlay.className = 'landscape-hint-overlay';
    overlay.innerHTML =
        '<div class="landscape-hint-anim">' +
            '<div class="landscape-hint-phone"></div>' +
        '</div>' +
        '<div class="landscape-hint-text">ROTATE TO LANDSCAPE<br>FOR BEST EXPERIENCE</div>' +
        '<button class="landscape-hint-btn" type="button">GOT IT</button>';
    document.body.appendChild(overlay);

    var dismissed = false;
    overlay.querySelector('.landscape-hint-btn').addEventListener('click', function(e) {
        e.stopPropagation();
        if (dismissed) return;
        dismissed = true;
        localStorage.setItem('landscape-hint-dismissed', '1');
        overlay.classList.add('hide');
        setTimeout(function() {
            overlay.remove();
            maybeShowFsHint();
        }, 300);
    });
    overlay.addEventListener('touchstart', function(e) { e.stopPropagation(); }, { passive: true });
}

createLandscapeHintIfNeeded();
document.addEventListener('turbo:load', createLandscapeHintIfNeeded);
document.addEventListener('turbo:before-cache', function() {
    document.querySelectorAll('.landscape-hint-overlay, .ios-install-overlay').forEach(function(el) { el.remove(); });
});

// When rotating to landscape while landscape-hint is still pending, show fs-hint too.
window.addEventListener('resize', maybeShowFsHint);
window.addEventListener('orientationchange', function() { setTimeout(maybeShowFsHint, 150); });

// Must match the selector each page's own initNav() uses so we read the
// same sessionStorage key nav-shared.js writes.
function getNavSelectorFor(bodyEl) {
    if (bodyEl.querySelector('.pf-nav')) return '.pf-nav';
    if (bodyEl.querySelector('.profile-content')) return '.menu-wrap .menu-btn';
    if (bodyEl.querySelector('.menu-btn')) return '.menu-btn';
    return null;
}

function preselectNavInBody(bodyEl, pathname) {
    bodyEl.classList.add('nav-no-anim');
    if (!_isTouch) {
        bodyEl.classList.add('kb-mode');
    }

    // On touch there's no kb nav, so .active is never shown.
    // Skipping pre-selection avoids a one-frame gold flash.
    if (_isTouch) return;

    const sel = getNavSelectorFor(bodyEl);
    if (!sel) return;

    const btns = bodyEl.querySelectorAll(sel);
    if (btns.length === 0) return;

    const stored = sessionStorage.getItem('nav:' + pathname + ':' + sel);
    let idx;
    if (stored !== null && Number.isFinite(+stored)) {
        idx = +stored;
    }
    else if (pathname === '/profile') {
        // profile.js defaults to BACK on first visit; mirror it so first paint
        // matches initNav's post-run state.
        idx = btns.length - 1;
    }
    else {
        idx = 0;
    }
    if (idx < 0 || idx >= btns.length) idx = 0;
    btns[idx].classList.add('active');
}

// Apply .active before body scripts run post-paint.
if (document.body) {
    preselectNavInBody(document.body, location.pathname);
}
else {
    document.addEventListener('readystatechange', function once() {
        if (document.body) {
            document.removeEventListener('readystatechange', once);
            preselectNavInBody(document.body, location.pathname);
        }
    });
}

let _pendingVisitPath = null;
document.addEventListener('turbo:before-visit', e => {
    try { _pendingVisitPath = new URL(e.detail.url, location.href).pathname; }
    catch (_) { _pendingVisitPath = null; }
});

// Hold Turbo's body swap until new stylesheets are loaded and nav state
// is pre-applied, otherwise slow networks show an unstyled flash.
document.addEventListener('turbo:before-render', event => {
    const newBody = event.detail.newBody;
    if (newBody) {
        preselectNavInBody(newBody, _pendingVisitPath || location.pathname);
        const root = newBody.querySelector('.game-root');
        if (root) scaleGameRoot(root, newBody);
    }

    const links = Array.from(document.querySelectorAll('link[rel="stylesheet"]'));
    const pending = links.filter(l => !l.sheet);
    if (pending.length === 0) return;

    event.preventDefault();
    Promise.all(pending.map(l => new Promise(res => {
        const done = () => res();
        l.addEventListener('load', done, { once: true });
        l.addEventListener('error', done, { once: true });
        setTimeout(done, 1500);
    }))).then(() => event.detail.resume());
});

document.addEventListener('turbo:render', () => { _pendingVisitPath = null; });
