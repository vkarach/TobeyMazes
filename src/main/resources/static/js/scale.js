// Detect touch device once (survives orientation changes)
var _isTouch = ('ontouchstart' in window) || (navigator.maxTouchPoints > 0);

// Inset of the game window from viewport edges — set by scaleGameRoot().
// Used by overlay repositioning functions so they never need getBoundingClientRect().
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

// Scale .game-root to fill the viewport.
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
    } else {
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

scaleGameRoot();
window.addEventListener('resize', () => scaleGameRoot());
window.addEventListener('orientationchange', () => {
    setTimeout(() => scaleGameRoot(), 100);
});
document.addEventListener('turbo:load', () => scaleGameRoot());

// Fullscreen toggle button for touch devices.
// Appended to <html> so Turbo body-swaps don't remove it.
(function initFullscreenToggle() {
    if (!_isTouch) return;
    var el = document.documentElement;
    var fsMethod = el.requestFullscreen || el.webkitRequestFullscreen;
    if (!fsMethod) return;

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
        dismissHint();
        if (isFS()) {
            (document.exitFullscreen || document.webkitExitFullscreen).call(document)
                .catch(function() {});
        } else {
            fsMethod.call(el).then(function() {
                setTimeout(scaleGameRoot, 150);
            }).catch(function() {});
        }
    });

    document.addEventListener('fullscreenchange', function() {
        updateIcon(); setTimeout(scaleGameRoot, 100);
    });
    document.addEventListener('webkitfullscreenchange', function() {
        updateIcon(); setTimeout(scaleGameRoot, 100);
    });

    // First-visit hint
    var dismissHint = function() {};
    if (!localStorage.getItem('fs-hint-seen')) {
        var hint = document.createElement('div');
        hint.className = 'fs-hint';
        hint.innerHTML = '<span class="fs-hint-bubble">fullscreen</span><span class="fs-hint-arrow"><span>&#8250;</span><span>&#8250;</span></span>';
        el.appendChild(hint);

        dismissHint = function() {
            if (hint.parentNode) {
                localStorage.setItem('fs-hint-seen', '1');
                hint.classList.add('fs-hint-hide');
                setTimeout(function() { hint.remove(); }, 300);
            }
        };
        setTimeout(dismissHint, 4000);
    }
})();

// ---------------------------------------------------------------------------
// Smooth Turbo navigation — kill flicker / button jitter / style flash.
//
// Two separate things must happen before Turbo swaps the new body in:
//   1. Any newly-added stylesheets must be fully loaded so the first paint of
//      the new body is already styled (no FOUC on slow networks).
//   2. Nav buttons must already have their stored .active class and the body
//      must carry .nav-no-anim / .kb-mode so the first paint is identical to
//      the post-JS steady state (no "jump" while JS runs initNav after paint).
// ---------------------------------------------------------------------------
// Pick the exact selector the target page's own initNav() uses, so the
// sessionStorage key we read matches what nav-shared.js wrote. Must stay in
// sync with the initNav() calls in menu.js / profile.js / review.js / etc.
//
//   profile.js logged-in  → '.pf-nav'                 (marker: .pf-nav exists)
//   profile.js guest      → '.menu-wrap .menu-btn'    (marker: .profile-content)
//   review.js             → '.pf-nav'                 (marker: .pf-nav exists)
//   menu/leaderboard/about → '.menu-btn'              (fallback)
//
// levels.html and game.html don't use initNav at all and are skipped.
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

    // On touch devices there is no keyboard navigation, so .active is never
    // shown — skip pre-selection entirely to avoid a one-frame gold flash.
    if (_isTouch) return;

    const sel = getNavSelectorFor(bodyEl);
    if (!sel) return;

    const btns = bodyEl.querySelectorAll(sel);
    if (btns.length === 0) return;

    const stored = sessionStorage.getItem('nav:' + pathname + ':' + sel);
    let idx;
    if (stored !== null && Number.isFinite(+stored)) {
        idx = +stored;
    } else if (pathname === '/profile') {
        // profile.js defaults to BACK (last button) on first visit —
        // mirror that so the first paint matches initNav's post-run state.
        idx = btns.length - 1;
    } else {
        idx = 0;
    }
    if (idx < 0 || idx >= btns.length) idx = 0;
    btns[idx].classList.add('active');
}

// Cold-load: body already has nav-no-anim/kb-mode from the template, but we
// still need to apply .active before the scripts at the bottom of body would
// otherwise do it post-paint. Runs synchronously as part of scale.js defer
// execution, which happens before DOMContentLoaded / first paint.
if (document.body) {
    preselectNavInBody(document.body, location.pathname);
} else {
    document.addEventListener('readystatechange', function once() {
        if (document.body) {
            document.removeEventListener('readystatechange', once);
            preselectNavInBody(document.body, location.pathname);
        }
    });
}

// Remember the URL of the visit we're navigating to so turbo:before-render can
// look up the right sessionStorage keys for the new page's nav buttons.
let _pendingVisitPath = null;
document.addEventListener('turbo:before-visit', e => {
    try { _pendingVisitPath = new URL(e.detail.url, location.href).pathname; }
    catch (_) { _pendingVisitPath = null; }
});

// Hold Turbo's body swap until:
//   a) all stylesheets in the new <head> are actually loaded, and
//   b) the incoming body has its nav state pre-applied.
// Otherwise on slow networks the new page renders unstyled for a flash and
// the active button visibly jumps into place a frame later.
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
        // safety timeout
        setTimeout(done, 1500);
    }))).then(() => event.detail.resume());
});

// After render, clear the pending path so back/forward/fallback loads
// fall through to location.pathname.
document.addEventListener('turbo:render', () => { _pendingVisitPath = null; });
