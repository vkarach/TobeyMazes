(function () {
    const cta = document.querySelector('.hero-cta');
    const navPlay = document.querySelector('.nav-play');
    if (cta && navPlay) {
        new IntersectionObserver(([e]) => {
            navPlay.style.opacity = e.isIntersecting ? '0' : '1';
            navPlay.style.pointerEvents = e.isIntersecting ? 'none' : 'auto';
        }).observe(cta);
    }

    const heroScroll = document.querySelector('.hero-scroll');
    if (heroScroll) {
        const updateScrollFade = () => {
            const fade = Math.max(0, 1 - window.scrollY / 220);
            heroScroll.style.opacity = fade;
            heroScroll.style.pointerEvents = fade < 0.05 ? 'none' : 'auto';
        };
        window.addEventListener('scroll', updateScrollFade, { passive: true });
        updateScrollFade();
    }
})();
