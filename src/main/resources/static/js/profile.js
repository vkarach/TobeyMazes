(function () {
    const root = document.querySelector('.profile-actions, .profile-content');
    if (!root) return;

    const ac = new AbortController();
    const sig = { signal: ac.signal };
    document.addEventListener('turbo:before-visit', () => ac.abort(), { once: true });

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

    async function submitLogout() {
        await post('/profile/logout', {});
        Turbo.visit('/profile');
    }

    async function submitLogin() {
        const username = document.getElementById('login-username').value.trim();
        const password = document.getElementById('login-password').value;
        if (!username || !password) { showError('login-error', 'Please fill in all fields'); return; }
        const res = await post('/profile/login/ajax', { username, password });
        if (res.error) { showError('login-error', res.error); return; }
        closeModal('login-modal');
        setTimeout(() => Turbo.visit('/profile'), 250);
    }

    let regData = {};
    async function submitRegister() {
        const username = document.getElementById('reg-username').value.trim();
        const password = document.getElementById('reg-password').value;
        const email    = document.getElementById('reg-email').value.trim();
        if (!username || !password || !email) { showError('reg-error', 'Please fill in all fields'); return; }
        document.getElementById('reg-step1').style.display = 'none';
        document.getElementById('reg-loading').style.display = 'flex';
        const res = await post('/profile/register/ajax', { username, password, email });
        document.getElementById('reg-loading').style.display = 'none';
        if (res.error) {
            document.getElementById('reg-step1').style.display = 'flex';
            showError('reg-error', res.error);
            return;
        }
        regData = { username, password, email };
        document.getElementById('reg-step2').style.display = 'flex';
        document.getElementById('reg-title').textContent = 'CONFIRM EMAIL';
        setTimeout(() => document.getElementById('reg-code').focus(), 50);
    }
    async function submitConfirm() {
        const userCode = document.getElementById('reg-code').value.trim();
        if (!userCode) { showError('reg-error', 'Please fill in the field'); return; }
        const res = await post('/profile/confirm/ajax', { ...regData, userCode });
        if (res.error) { showError('reg-error', res.error); return; }
        setTimeout(() => Turbo.visit('/profile'), 250);
    }

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

    // expose for inline onclick handlers in template
    window.openModal = openModal;
    window.closeModal = closeModal;
    window.submitLogout = submitLogout;
    window.submitLogin = submitLogin;
    window.submitRegister = submitRegister;
    window.submitConfirm = submitConfirm;
    window.openChangePassword = openChangePassword;
    window.submitChangePassword = submitChangePassword;

    document.querySelectorAll('.modal-overlay').forEach(o =>
        o.addEventListener('click', e => { if (e.target === o) o.classList.remove('open'); }, sig)
    );
    document.addEventListener('keydown', e => {
        if (e.key !== 'Escape') return;
        const open = document.querySelectorAll('.modal-overlay.open');
        if (open.length > 0) {
            e.preventDefault();
            e.stopImmediatePropagation();
            open.forEach(m => m.classList.remove('open'));
            return;
        }
        e.preventDefault();
        Turbo.visit('/menu');
    }, sig);

    document.addEventListener('keydown', e => {
        if (e.key !== 'Enter' || e.repeat) return;
        if (document.getElementById('login-modal')?.classList.contains('open')) {
            submitLogin();
            return;
        }
        if (document.getElementById('register-modal')?.classList.contains('open')) {
            if (document.getElementById('reg-step2').style.display !== 'none') submitConfirm();
            else submitRegister();
            return;
        }
        if (document.getElementById('cp-modal')?.classList.contains('open')) {
            submitChangePassword();
        }
    }, sig);

    const btns = Array.from(document.querySelectorAll('.menu-wrap .menu-btn'));
    let selected = 0;
    let mouseMode = false;

    function updateSelected(buttons, index) {
        buttons.forEach(b => b.classList.remove('active'));
        if (buttons[index]) buttons[index].classList.add('active');
    }
    updateSelected(btns, selected);

    document.addEventListener('keydown', e => {
        if (document.querySelector('.modal-overlay.open')) return;
        updateSelected(btns, selected);
        if (mouseMode) {
            mouseMode = false;
            return;
        }

        if (e.key === 'q' || e.key === 'Q' || e.key === 'й' || e.key === 'Й') {
            e.preventDefault();
            Turbo.visit('/menu');
            return;
        }
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            selected = (selected + 1) % btns.length;
            updateSelected(btns, selected);
        }
        else if (e.key === 'ArrowUp') {
            e.preventDefault();
            selected = (selected - 1 + btns.length) % btns.length;
            updateSelected(btns, selected);
        }
        else if (e.key === 'Enter' && selected >= 0) {
            e.preventDefault();
            e.stopImmediatePropagation();
            btns[selected].click();
        }
    }, sig);

    document.addEventListener('mousemove', () => {
        if (!mouseMode) {
            mouseMode = true;
            btns.forEach(b => b.classList.remove('active'));
        }
    }, sig);
})();
