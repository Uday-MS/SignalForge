// ============================================
// SignalForge — SPA Application Logic
// ============================================

const API_BASE = '';
let accessToken = null;
let currentEmail = null;
let allMonitors = [];

document.addEventListener('DOMContentLoaded', () => {
    // Check URL params for OAuth callback
    const params = new URLSearchParams(window.location.search);
    if (params.get('token')) {
        accessToken = params.get('token');
        currentEmail = params.get('email');
        window.history.replaceState({}, '', '/index.html');
        navigateTo('dashboard');
        return;
    }

    // Check hash routing
    const hash = window.location.hash.replace('#/', '').replace('#', '');
    if (hash && ['login', 'register', 'dashboard'].includes(hash)) {
        navigateTo(hash);
    } else {
        navigateTo('landing');
    }

    initNavbar();
    initMobileMenu();
    initScrollAnimations();
    initOtpInputs();
    fetchPublicStats();
    setInterval(fetchPublicStats, 60000);
});

// ============================================
// ROUTER
// ============================================
function navigateTo(page) {
    // Auth check for dashboard
    if (page === 'dashboard' && !accessToken) {
        page = 'login';
    }

    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    const target = document.getElementById('page-' + page);
    if (target) {
        target.classList.add('active');
    }

    window.scrollTo(0, 0);

    if (page === 'landing') {
        window.location.hash = '';
        setTimeout(initScrollAnimations, 100);
    } else {
        window.location.hash = '/' + page;
    }

    if (page === 'dashboard') {
        loadDashboard();
    }
}

// ============================================
// NAVBAR
// ============================================
function initNavbar() {
    const navbar = document.getElementById('navbar');
    if (!navbar) return;
    window.addEventListener('scroll', () => {
        if (window.scrollY > 50) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
    });
}

function initMobileMenu() {
    const toggle = document.getElementById('mobile-toggle');
    const navLinks = document.getElementById('nav-links');
    if (!toggle || !navLinks) return;

    toggle.addEventListener('click', () => {
        navLinks.classList.toggle('active');
        toggle.classList.toggle('active');
    });

    navLinks.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', () => {
            navLinks.classList.remove('active');
            toggle.classList.remove('active');
        });
    });
}

// ============================================
// SCROLL ANIMATIONS
// ============================================
function initScrollAnimations() {
    const observer = new IntersectionObserver(
        (entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('visible');
                    observer.unobserve(entry.target);
                }
            });
        },
        { threshold: 0.15, rootMargin: '0px 0px -50px 0px' }
    );

    document.querySelectorAll('.fade-in:not(.visible)').forEach(el => observer.observe(el));
}

// ============================================
// OTP INPUT HANDLING
// ============================================
function initOtpInputs() {
    const inputs = document.querySelectorAll('.otp-input');
    inputs.forEach((input, idx) => {
        input.addEventListener('input', (e) => {
            const val = e.target.value;
            if (val && idx < inputs.length - 1) {
                inputs[idx + 1].focus();
            }
        });
        input.addEventListener('keydown', (e) => {
            if (e.key === 'Backspace' && !e.target.value && idx > 0) {
                inputs[idx - 1].focus();
            }
        });
        input.addEventListener('paste', (e) => {
            e.preventDefault();
            const data = e.clipboardData.getData('text').trim().slice(0, 6);
            data.split('').forEach((char, i) => {
                if (inputs[i]) inputs[i].value = char;
            });
            if (inputs[data.length - 1]) inputs[data.length - 1].focus();
        });
    });
}

function getOtpValue() {
    return Array.from(document.querySelectorAll('.otp-input')).map(i => i.value).join('');
}

function clearOtpInputs() {
    document.querySelectorAll('.otp-input').forEach(i => { i.value = ''; });
}

// ============================================
// FAQ TOGGLE
// ============================================
function toggleFaq(btn) {
    const item = btn.closest('.faq-item');
    item.classList.toggle('open');
}

// ============================================
// PUBLIC STATS
// ============================================
async function fetchPublicStats() {
    try {
        const res = await fetch(API_BASE + '/api/stats');
        if (!res.ok) return;
        const data = await res.json();
        animateCounter('stat-total', data.totalUrls ?? 0);
        animateCounter('stat-up', data.urlsUp ?? 0);
        animateCounter('stat-down', data.urlsDown ?? 0);
    } catch (e) {
        // Silent fail for landing page stats
    }
}

function animateCounter(elementId, targetValue) {
    const el = document.getElementById(elementId);
    if (!el) return;
    const start = parseInt(el.textContent) || 0;
    if (start === targetValue) return;
    const duration = 1000;
    const startTime = performance.now();

    function update(now) {
        const progress = Math.min((now - startTime) / duration, 1);
        const eased = 1 - Math.pow(1 - progress, 3);
        el.textContent = Math.round(start + (targetValue - start) * eased).toLocaleString();
        if (progress < 1) requestAnimationFrame(update);
    }
    requestAnimationFrame(update);
}

// ============================================
// AUTH — API HELPER
// ============================================
async function apiCall(url, method = 'GET', body = null) {
    const headers = { 'Content-Type': 'application/json' };
    if (accessToken) {
        headers['Authorization'] = 'Bearer ' + accessToken;
    }
    const opts = { method, headers, credentials: 'include' };
    if (body) opts.body = JSON.stringify(body);

    const res = await fetch(API_BASE + url, opts);

    // Auto refresh on 401
    if (res.status === 401 && url !== '/api/auth/refresh' && url !== '/api/auth/login') {
        const refreshed = await refreshToken();
        if (refreshed) {
            headers['Authorization'] = 'Bearer ' + accessToken;
            const retry = await fetch(API_BASE + url, { method, headers, credentials: 'include', body: body ? JSON.stringify(body) : null });
            return retry;
        } else {
            accessToken = null;
            navigateTo('login');
            return res;
        }
    }
    return res;
}

async function refreshToken() {
    try {
        const headers = { 'Content-Type': 'application/json' };
        if (accessToken) headers['Authorization'] = 'Bearer ' + accessToken;
        const res = await fetch(API_BASE + '/api/auth/refresh', {
            method: 'POST', headers, credentials: 'include'
        });
        if (res.ok) {
            const data = await res.json();
            accessToken = data.token;
            return true;
        }
    } catch (e) {}
    return false;
}

// ============================================
// AUTH — LOGIN
// ============================================
async function handleLogin(e) {
    e.preventDefault();
    const btn = document.getElementById('login-btn');
    const errorEl = document.getElementById('login-error');
    errorEl.style.display = 'none';

    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;

    btn.innerHTML = '<span class="loading-spinner"></span>';
    btn.disabled = true;

    try {
        const res = await fetch(API_BASE + '/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ email, password })
        });

        const data = await res.json();

        if (res.ok) {
            accessToken = data.token;
            currentEmail = email;
            showToast('Welcome back!', 'success');
            navigateTo('dashboard');
        } else {
            errorEl.textContent = data.error || data.message || 'Invalid credentials';
            errorEl.style.display = 'block';
        }
    } catch (err) {
        errorEl.textContent = 'Network error. Please try again.';
        errorEl.style.display = 'block';
    }

    btn.innerHTML = '<span>Sign In</span>';
    btn.disabled = false;
}

// ============================================
// AUTH — REGISTER
// ============================================
let pendingRegisterEmail = '';

async function handleRegister(e) {
    e.preventDefault();
    const btn = document.getElementById('register-btn');
    const errorEl = document.getElementById('register-error');
    errorEl.style.display = 'none';

    const email = document.getElementById('register-email').value;
    const password = document.getElementById('register-password').value;

    btn.innerHTML = '<span class="loading-spinner"></span>';
    btn.disabled = true;

    try {
        const res = await fetch(API_BASE + '/api/auth/send-otp', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        const data = await res.json();

        if (res.ok) {
            pendingRegisterEmail = email;
            document.getElementById('otp-email-display').textContent = email;
            clearOtpInputs();
            showToast('Verification code sent!', 'success');
            navigateTo('otp');
            setTimeout(() => document.querySelector('.otp-input')?.focus(), 200);
        } else {
            errorEl.textContent = data.error || data.message || 'Registration failed';
            errorEl.style.display = 'block';
        }
    } catch (err) {
        errorEl.textContent = 'Network error. Please try again.';
        errorEl.style.display = 'block';
    }

    btn.innerHTML = '<span>Send Verification Code</span>';
    btn.disabled = false;
}

// ============================================
// AUTH — OTP VERIFY
// ============================================
async function handleOtpVerify(e) {
    e.preventDefault();
    const btn = document.getElementById('otp-btn');
    const errorEl = document.getElementById('otp-error');
    errorEl.style.display = 'none';

    const otp = getOtpValue();
    if (otp.length !== 6) {
        errorEl.textContent = 'Please enter all 6 digits';
        errorEl.style.display = 'block';
        return;
    }

    btn.innerHTML = '<span class="loading-spinner"></span>';
    btn.disabled = true;

    try {
        const res = await fetch(API_BASE + '/api/auth/verify-otp', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ email: pendingRegisterEmail, otp })
        });

        const data = await res.json();

        if (res.ok) {
            accessToken = data.token;
            currentEmail = pendingRegisterEmail;
            showToast('Account created successfully!', 'success');
            navigateTo('dashboard');
        } else {
            errorEl.textContent = data.error || 'Invalid verification code';
            errorEl.style.display = 'block';
        }
    } catch (err) {
        errorEl.textContent = 'Network error. Please try again.';
        errorEl.style.display = 'block';
    }

    btn.innerHTML = '<span>Verify & Create Account</span>';
    btn.disabled = false;
}

// ============================================
// AUTH — LOGOUT
// ============================================
async function handleLogout() {
    try {
        await apiCall('/api/auth/logout', 'POST');
    } catch (e) {}
    accessToken = null;
    currentEmail = null;
    showToast('Signed out', 'info');
    navigateTo('landing');
}

// ============================================
// DASHBOARD
// ============================================
async function loadDashboard() {
    if (!accessToken) return;

    // Set email display
    const emailEl = document.getElementById('dashboard-email');
    if (emailEl && currentEmail) emailEl.textContent = currentEmail;

    // Show loading
    const content = document.getElementById('monitors-content');
    content.innerHTML = '<div style="padding: 60px; text-align: center;"><span class="loading-spinner" style="width: 28px; height: 28px; color: var(--brand-primary);"></span></div>';

    try {
        const res = await apiCall('/api/monitors');
        if (!res.ok) {
            if (res.status === 401) return;
            throw new Error('Failed to load');
        }
        allMonitors = await res.json();
        updateDashboardStats();
        renderMonitors(allMonitors);
    } catch (err) {
        content.innerHTML = '<div class="empty-state"><div class="empty-icon">⚠️</div><div class="empty-title">Failed to load monitors</div><div class="empty-desc">Please try refreshing the page.</div></div>';
    }
}

function updateDashboardStats() {
    const total = allMonitors.length;
    const up = allMonitors.filter(m => m.lastStatus === 'UP').length;
    const down = total - up;
    const uptime = total > 0 ? Math.round((up / total) * 100) : 0;

    document.getElementById('dash-total').textContent = total;
    document.getElementById('dash-up').textContent = up;
    document.getElementById('dash-down').textContent = down;
    document.getElementById('dash-uptime').textContent = total > 0 ? uptime + '%' : '—';
}

function renderMonitors(monitors) {
    const content = document.getElementById('monitors-content');

    if (monitors.length === 0) {
        content.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">📡</div>
                <div class="empty-title">No monitors yet</div>
                <div class="empty-desc">Add your first monitor to start tracking uptime.</div>
                <button class="btn btn-primary" onclick="openAddModal()">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 5v14M5 12h14"/></svg>
                    Add Your First Monitor
                </button>
            </div>`;
        return;
    }

    let html = `
        <table class="monitor-table">
            <thead>
                <tr>
                    <th>Monitor</th>
                    <th>Status</th>
                    <th>Response Time</th>
                    <th>Last Checked</th>
                    <th></th>
                </tr>
            </thead>
            <tbody>`;

    monitors.forEach(m => {
        const statusClass = getStatusClass(m.lastStatus);
        const statusText = getStatusText(m.lastStatus);
        const responseTime = m.responseTime != null && m.responseTime >= 0 ? m.responseTime + 'ms' : '—';
        const lastChecked = m.lastChecked ? formatTime(m.lastChecked) : 'Pending...';
        const displayName = m.name || extractDomain(m.url);

        html += `
            <tr>
                <td>
                    <div class="monitor-name">${escapeHtml(displayName)}</div>
                    <div class="monitor-url">${escapeHtml(m.url)}</div>
                </td>
                <td>
                    <span class="status-badge ${statusClass}">
                        <span class="status-dot"></span>
                        ${statusText}
                    </span>
                </td>
                <td><span class="response-time">${responseTime}</span></td>
                <td><span class="last-checked">${lastChecked}</span></td>
                <td class="monitor-actions">
                    <button onclick="deleteMonitor(${m.id})" title="Delete monitor">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 6h18M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/></svg>
                    </button>
                </td>
            </tr>`;
    });

    html += '</tbody></table>';
    content.innerHTML = html;
}

function filterMonitors() {
    const search = document.getElementById('search-monitors').value.toLowerCase();
    const statusFilter = document.getElementById('filter-status').value;

    let filtered = allMonitors;

    if (search) {
        filtered = filtered.filter(m =>
            (m.name && m.name.toLowerCase().includes(search)) ||
            m.url.toLowerCase().includes(search)
        );
    }

    if (statusFilter !== 'all') {
        if (statusFilter === 'DOWN') {
            filtered = filtered.filter(m => m.lastStatus !== 'UP');
        } else {
            filtered = filtered.filter(m => m.lastStatus === statusFilter);
        }
    }

    renderMonitors(filtered);
}

// ============================================
// ADD MONITOR
// ============================================
function openAddModal() {
    document.getElementById('add-modal').classList.add('active');
    document.getElementById('monitor-url').focus();
}

function closeAddModal() {
    document.getElementById('add-modal').classList.remove('active');
    document.getElementById('add-monitor-form').reset();
    document.getElementById('add-error').style.display = 'none';
}

async function handleAddMonitor(e) {
    e.preventDefault();
    const btn = document.getElementById('add-btn');
    const errorEl = document.getElementById('add-error');
    errorEl.style.display = 'none';

    const url = document.getElementById('monitor-url').value;
    const name = document.getElementById('monitor-name').value;

    btn.innerHTML = '<span class="loading-spinner"></span>';
    btn.disabled = true;

    try {
        const res = await apiCall('/api/monitors', 'POST', { url, name });
        const data = await res.json();

        if (res.ok) {
            showToast('Monitor added successfully', 'success');
            closeAddModal();
            loadDashboard();
        } else {
            errorEl.textContent = data.error || data.message || 'Failed to add monitor';
            errorEl.style.display = 'block';
        }
    } catch (err) {
        errorEl.textContent = 'Network error. Please try again.';
        errorEl.style.display = 'block';
    }

    btn.innerHTML = '<span>Add Monitor</span>';
    btn.disabled = false;
}

// ============================================
// DELETE MONITOR
// ============================================
async function deleteMonitor(id) {
    if (!confirm('Are you sure you want to delete this monitor?')) return;

    try {
        const res = await apiCall('/api/monitors/' + id, 'DELETE');
        if (res.ok) {
            showToast('Monitor deleted', 'success');
            loadDashboard();
        } else {
            showToast('Failed to delete monitor', 'error');
        }
    } catch (err) {
        showToast('Network error', 'error');
    }
}

// ============================================
// TOAST NOTIFICATIONS
// ============================================
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = 'toast ' + type;

    const icons = { success: '✓', error: '✕', info: 'ℹ' };
    toast.innerHTML = `<span>${icons[type] || 'ℹ'}</span><span>${escapeHtml(message)}</span>`;

    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// ============================================
// HELPERS
// ============================================
function getStatusClass(status) {
    if (status === 'UP') return 'up';
    if (status === 'DOWN' || status === 'SERVER_ERROR' || status === 'CLIENT_ERROR') return 'down';
    if (status === 'REDIRECTED') return 'warning';
    return 'unknown';
}

function getStatusText(status) {
    if (!status) return 'Pending';
    const map = {
        'UP': 'Operational',
        'DOWN': 'Down',
        'SERVER_ERROR': 'Server Error',
        'CLIENT_ERROR': 'Client Error',
        'REDIRECTED': 'Redirected'
    };
    return map[status] || status;
}

function extractDomain(url) {
    try {
        return new URL(url).hostname;
    } catch {
        return url;
    }
}

function formatTime(dateStr) {
    if (!dateStr) return '—';
    // Handle "dd-MM-yyyy HH:mm:ss" format
    const parts = dateStr.split(' ');
    if (parts.length === 2) {
        const [d, m, y] = parts[0].split('-');
        const date = new Date(`${y}-${m}-${d}T${parts[1]}`);
        if (!isNaN(date)) {
            const now = new Date();
            const diffMs = now - date;
            const diffSec = Math.floor(diffMs / 1000);
            if (diffSec < 60) return diffSec + 's ago';
            if (diffSec < 3600) return Math.floor(diffSec / 60) + 'm ago';
            if (diffSec < 86400) return Math.floor(diffSec / 3600) + 'h ago';
            return date.toLocaleDateString();
        }
    }
    return dateStr;
}

function escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

// Close modal on overlay click
document.getElementById('add-modal')?.addEventListener('click', (e) => {
    if (e.target === e.currentTarget) closeAddModal();
});

// Close modal on Escape
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') closeAddModal();
});

// Auto-refresh dashboard every 60s
setInterval(() => {
    if (document.getElementById('page-dashboard')?.classList.contains('active') && accessToken) {
        loadDashboard();
    }
}, 60000);
