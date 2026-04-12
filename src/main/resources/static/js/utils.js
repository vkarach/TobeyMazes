function initAbort(extraCleanup) {
    const ac = new AbortController();
    const sig = { signal: ac.signal };
    const abort = () => { if (extraCleanup) extraCleanup(); ac.abort(); };
    document.addEventListener('turbo:before-visit',  abort, { once: true });
    document.addEventListener('turbo:before-render', abort, { once: true });
    return { ac, sig, abort };
}

function escHtml(s) {
    const d = document.createElement('div');
    d.textContent = s;
    return d.innerHTML;
}

function timeAgo(isoStr) {
    if (!isoStr) return '';
    const date = new Date(isoStr);
    const now = new Date();
    const sec = Math.floor((now - date) / 1000);
    if (sec < 60) return 'just now';
    const min = Math.floor(sec / 60);
    if (min < 60) return min + (min === 1 ? ' minute ago' : ' minutes ago');
    const hrs = Math.floor(min / 60);
    if (hrs < 24) return hrs + (hrs === 1 ? ' hour ago' : ' hours ago');
    const days = Math.floor(hrs / 24);
    if (days < 30) return days + (days === 1 ? ' day ago' : ' days ago');
    const months = Math.floor(days / 30);
    if (months < 12) return months + (months === 1 ? ' month ago' : ' months ago');
    const years = Math.floor(months / 12);
    return years + (years === 1 ? ' year ago' : ' years ago');
}
