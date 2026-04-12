// Scale .game-root to fill the viewport.
// Portrait phones get a narrower virtual canvas (600x1066) so UI stays readable.
function scaleGameRoot(rootEl, bodyEl) {
    const root = rootEl || document.querySelector('.game-root');
    if (!root) return;
    const body = bodyEl || document.body;
    const vw = window.innerWidth;
    const vh = window.innerHeight;
    const mobile = vh > vw * 1.2;

    body.classList.toggle('mobile', mobile);
    if (mobile) body.classList.remove('kb-mode');

    if (mobile) {
        const W = 600;
        const H = Math.round(W * vh / vw);
        root.style.width = W + 'px';
        root.style.height = H + 'px';
        root.style.position = 'fixed';
        root.style.left = '0';
        root.style.top = '0';
        root.style.transformOrigin = 'top left';
        root.style.transform = `scale(${vw / W})`;
    } else {
        root.style.width = '1280px';
        root.style.height = '720px';
        root.style.position = '';
        root.style.left = '';
        root.style.top = '';
        root.style.transformOrigin = 'center center';
        root.style.transform = `scale(${Math.max(vw / 1280, vh / 720)})`;
    }
}

scaleGameRoot();
window.addEventListener('resize', () => scaleGameRoot());
document.addEventListener('turbo:load', () => scaleGameRoot());

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
    if (!(window.innerHeight > window.innerWidth * 1.2)) {
        bodyEl.classList.add('kb-mode');
    }

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
