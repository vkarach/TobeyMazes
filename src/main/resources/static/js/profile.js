async function post(url, data) {
    const r = await fetch(url, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams(data)
    });
    return r.json();
}

function openModal(id) {
    document.getElementById(id).classList.add('open');
    const first = document.querySelector('#' + id + ' input');
    if (first) setTimeout(() => first.focus(), 50);
}
function closeModal(id) { document.getElementById(id).classList.remove('open'); }

const _errTimers = {};
function showError(id, msg) {
    const el = document.getElementById(id);
    clearTimeout(_errTimers[id]);
    el.textContent = '! ' + msg;
    el.style.transition = 'opacity 0.15s';
    el.style.opacity = '1';
    _errTimers[id] = setTimeout(() => {
        el.style.transition = 'opacity 0.6s';
        el.style.opacity = '0';
    }, 2500);
}

// --- Logout ---
async function submitLogout() {
    await post('/profile/logout', {});
    window.location.href = '/profile';
}

// --- Login ---
async function submitLogin() {
    const username = document.getElementById('login-username').value.trim();
    const password = document.getElementById('login-password').value;
    if (!username || !password) { showError('login-error', 'Please fill in all fields'); return; }
    const res = await post('/profile/login/ajax', { username, password });
    if (res.error) { showError('login-error', res.error); return; }
    closeModal('login-modal');

    setTimeout(() => {
        window.location.href = '/profile';
    }, 250);
}

// --- Register (2 steps) ---
let regData = {};
async function submitRegister() {
    const username = document.getElementById('reg-username').value.trim();
    const password = document.getElementById('reg-password').value;
    const email    = document.getElementById('reg-email').value.trim();
    if (!username || !password || !email) { showError('reg-error', 'Please fill in all fields'); return; }
    const res = await post('/profile/register/ajax', { username, password, email });
    if (res.error) { showError('reg-error', res.error); return; }
    regData = { username, password, email };
    document.getElementById('reg-step1').style.display = 'none';
    document.getElementById('reg-step2').style.display = 'flex';
    document.getElementById('reg-title').textContent = 'CONFIRM EMAIL';
    setTimeout(() => document.getElementById('reg-code').focus(), 50);
}
async function submitConfirm() {
    const userCode = document.getElementById('reg-code').value.trim();
    if (!userCode) { showError('reg-error', 'Please fill in the field'); return; }
    const res = await post('/profile/confirm/ajax', { ...regData, userCode });
    if (res.error) { showError('reg-error', res.error); return; }

    setTimeout(() => {
        window.location.href = '/profile';
    }, 250);
}

// --- Change password (2 steps) ---
async function openChangePassword() {
    document.getElementById('cp-loading').style.display = 'flex';
    document.getElementById('cp-step2').style.display = 'none';
    document.getElementById('cp-success').style.display = 'none';
    document.getElementById('cp-error').style.display = '';
    document.getElementById('cp-title').textContent = 'CHANGE PASSWORD';
    openModal('cp-modal');

    const res = await post('/profile/change-password/request', {});
    document.getElementById('cp-loading').style.display = 'none';
    if (res.error) { showError('cp-error', res.error); return; }
    document.getElementById('cp-step2').style.display = 'flex';
    document.getElementById('cp-title').textContent = 'ENTER CODE';
    setTimeout(() => document.getElementById('cp-code').focus(), 50);
}
async function submitChangePassword() {
    const userCode    = document.getElementById('cp-code').value.trim();
    const newPassword = document.getElementById('cp-newpass').value;
    if (!userCode || !newPassword) { showError('cp-error', 'Please fill in all fields'); return; }
    const res = await post('/profile/change-password/ajax', { userCode, newPassword });
    if (res.error) { showError('cp-error', res.error); return; }
    document.getElementById('cp-step2').style.display = 'none';
    document.getElementById('cp-error').style.display = 'none';
    document.getElementById('cp-success').style.display = 'flex';
    document.getElementById('cp-title').textContent = 'DONE';
}

// --- Close modal ---
document.querySelectorAll('.modal-overlay').forEach(o =>
    o.addEventListener('click', e => { if (e.target === o) o.classList.remove('open'); })
);
document.addEventListener('keydown', e => {
    if (e.key === 'Escape')
        document.querySelectorAll('.modal-overlay.open').forEach(m => m.classList.remove('open'));
});

// -- Confirm modal ---
document.addEventListener('keydown', e => {
    if (e.key !== 'Enter') return;
    if (document.getElementById('login-modal')?.classList.contains('open')) {
        submitLogin();
        return;
    }
    if (document.getElementById('register-modal')?.classList.contains('open')) {
        submitRegister();
        return;
    }
    if (document.getElementById('cp-modal')?.classList.contains('open')) {
        submitChangePassword();
    }
});

let mouseMode = false;

// --- Keyboard nav ---
const btns = Array.from(document.querySelectorAll('.menu-wrap .menu-btn'));
let idx = -1;
document.addEventListener('keydown', e => {
    mouseMode = false;
    if (document.querySelector('.modal-overlay.open')) return;
    if (e.key === 'ArrowDown') {
        if (idx >= 0) btns[idx].classList.remove('active');
        idx = (idx + 1) % btns.length; btns[idx].classList.add('active'); e.preventDefault();
    }
    else if (e.key === 'ArrowUp') {
        if (idx >= 0) btns[idx].classList.remove('active');
        idx = (idx - 1 + btns.length) % btns.length; btns[idx].classList.add('active'); e.preventDefault();
    }
    else if (e.key === 'Enter' && idx >= 0) { btns[idx].click(); }
});

// if mouse moved clear select
document.addEventListener('mousemove', () => {
    if (!mouseMode) {
        mouseMode = true;
        btns.forEach(btn => btn.classList.remove('active'));
    }
});