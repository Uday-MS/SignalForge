// ============================================
// UptimeRobot Landing Page — JavaScript
// ============================================

document.addEventListener('DOMContentLoaded', () => {
    initNavbar();
    initMobileMenu();
    initScrollAnimations();
    fetchStats();

    // Refresh stats every 60 seconds (matches ping interval)
    setInterval(fetchStats, 60000);
});

// ============================================
// NAVBAR — scroll effect
// ============================================
function initNavbar() {
    const navbar = document.getElementById('navbar');
    let lastScroll = 0;

    window.addEventListener('scroll', () => {
        const currentScroll = window.scrollY;

        if (currentScroll > 50) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }

        lastScroll = currentScroll;
    });
}

// ============================================
// MOBILE MENU
// ============================================
function initMobileMenu() {
    const toggle = document.getElementById('mobile-toggle');
    const navLinks = document.getElementById('nav-links');

    toggle.addEventListener('click', () => {
        navLinks.classList.toggle('active');
        toggle.classList.toggle('active');
    });

    // Close menu when a link is clicked
    navLinks.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', () => {
            navLinks.classList.remove('active');
            toggle.classList.remove('active');
        });
    });
}

// ============================================
// SCROLL ANIMATIONS — Intersection Observer
// ============================================
function initScrollAnimations() {
    const observer = new IntersectionObserver(
        (entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('visible');
                    observer.unobserve(entry.target);
                }
            });
        },
        {
            threshold: 0.15,
            rootMargin: '0px 0px -50px 0px',
        }
    );

    document.querySelectorAll('.fade-in').forEach((el) => {
        observer.observe(el);
    });
}

// ============================================
// FETCH LIVE STATS
// ============================================
async function fetchStats() {
    try {
        const response = await fetch('/uptimerobot/stats');

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();

        animateCounter('stat-total', data.totalUrls ?? 0);
        animateCounter('stat-up', data.urlsUp ?? 0);
        animateCounter('stat-down', data.urlsDown ?? 0);
    } catch (error) {
        console.warn('Stats fetch failed:', error.message);
        // Show fallback — keep the current values or dashes
    }
}

// ============================================
// ANIMATED COUNTER
// ============================================
function animateCounter(elementId, targetValue) {
    const element = document.getElementById(elementId);
    if (!element) return;

    const startValue = parseInt(element.textContent) || 0;
    const duration = 1200; // ms
    const startTime = performance.now();

    // If value hasn't changed, skip animation
    if (startValue === targetValue) return;

    function update(currentTime) {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);

        // Ease-out cubic for smooth deceleration
        const eased = 1 - Math.pow(1 - progress, 3);

        const currentValue = Math.round(startValue + (targetValue - startValue) * eased);
        element.textContent = currentValue.toLocaleString();

        if (progress < 1) {
            requestAnimationFrame(update);
        }
    }

    requestAnimationFrame(update);
}
