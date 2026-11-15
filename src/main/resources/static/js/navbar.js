/**
 * SPEDO Shared Navigation & App Layout Initializer
 */

document.addEventListener('DOMContentLoaded', () => {
    initThemeMode();
    initAppLayout();
});

function initThemeMode() {
    const savedTheme = localStorage.getItem('spedo_theme') || 'dark';
    if (savedTheme === 'dark') {
        document.body.classList.add('dark-mode');
    } else {
        document.body.classList.remove('dark-mode');
    }
}

function toggleThemeMode(isDark) {
    if (isDark) {
        document.body.classList.add('dark-mode');
        localStorage.setItem('spedo_theme', 'dark');
    } else {
        document.body.classList.remove('dark-mode');
        localStorage.setItem('spedo_theme', 'light');
    }
    const slider = document.getElementById('themeToggleSlider');
    const knob = document.getElementById('themeToggleKnob');
    if (slider) slider.style.background = isDark ? '#00f2fe' : '#334155';
    if (knob) knob.style.left = isDark ? '20px' : '2px';
    if (typeof window.refreshDashboardTheme === 'function') {
        window.refreshDashboardTheme();
    }
}

function initAppLayout() {
    const sidebarEl = document.getElementById('sidebar');
    const headerEl = document.getElementById('header');

    if (!sidebarEl || !headerEl) return;

    // Check auth on protected pages
    if (!Auth.checkAuth()) return;

    const currentPath = window.location.pathname;
    const user = ApiClient.getUser() || { fullName: 'Arun Vemula', email: 'admin@spedo.io', role: 'ROLE_USER' };
    const initials = user.fullName ? user.fullName.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase() : 'AV';
    const link = (p) => Auth.getRedirectUrl ? Auth.getRedirectUrl(p) : p;

    // 1. Render Modern Dark Navy Sidebar
    sidebarEl.innerHTML = `
        <div class="sidebar-header">
            <a href="${link('dashboard.html')}" class="brand-logo">
                <div class="brand-icon">S</div>
                <span>SPEDO</span>
                <span class="brand-tag">PRO</span>
            </a>
        </div>

        <nav class="sidebar-nav">
            <div class="nav-section-title">Core analytics</div>
            <a href="${link('dashboard.html')}" class="nav-item ${currentPath.includes('dashboard') ? 'active' : ''}">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>
                <span>Dashboard</span>
            </a>
            <a href="${link('omnichannel.html')}" class="nav-item ${currentPath.includes('omnichannel') ? 'active' : ''}">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="2" y1="12" x2="22" y2="12"></line><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path></svg>
                <span>Omnichannel Hub</span>
                <span style="font-size:10px; background:rgba(255,255,255,0.06); border:1px solid rgba(255,255,255,0.12); color:#94a3b8; padding:1px 5px; border-radius:4px; margin-left:auto; font-weight:500; letter-spacing:0.5px;">HUB</span>
            </a>
            <a href="${link('social-accounts.html')}" class="nav-item ${currentPath.includes('social-accounts') ? 'active' : ''}">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                <span>Social Accounts</span>
            </a>
            <a href="${link('posts.html')}" class="nav-item ${currentPath.includes('posts') ? 'active' : ''}">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="2" width="20" height="20" rx="5" ry="5"></rect><path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"></path><line x1="17.5" y1="6.5" x2="17.51" y2="6.5"></line></svg>
                <span>Posts & Content</span>
            </a>

            <div class="nav-section-title">Intelligence & growth</div>
            <a href="${link('giveaway.html')}" class="nav-item ${currentPath.includes('giveaway') ? 'active' : ''}">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 12 20 22 4 22 4 12"></polyline><rect x="2" y="7" width="20" height="5"></rect><line x1="12" y1="22" x2="12" y2="7"></line><path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z"></path><path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z"></path></svg>
                <span>Giveaway Picker</span>
                <span style="font-size:10px; background:rgba(37,99,235,0.1); color:#2563eb; border:1px solid rgba(37,99,235,0.2); padding:1px 6px; border-radius:4px; margin-left:auto; font-weight:600; letter-spacing:0.5px;">NEW</span>
            </a>
            <a href="${link('competitors.html')}" class="nav-item ${currentPath.includes('competitors') ? 'active' : ''}">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"></polyline><polyline points="17 6 23 6 23 12"></polyline></svg>
                <span>Competitor Intel</span>
            </a>
            <a href="${link('reports.html')}" class="nav-item ${currentPath.includes('reports') ? 'active' : ''}">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
                <span>Reports & Exports</span>
            </a>
            <a href="${link('notifications.html')}" class="nav-item ${currentPath.includes('notifications') ? 'active' : ''}">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>
                <span>Notifications</span>
            </a>

            <div class="nav-section-title">Settings</div>
            <a href="${link('settings.html')}" class="nav-item ${currentPath.includes('settings') ? 'active' : ''}">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>
                <span>Settings</span>
            </a>
        </nav>

        <div class="sidebar-footer" style="padding:16px 20px; border-top:1px solid var(--border-dark); background:transparent;">
            <div style="display:flex; flex-direction:column; gap:10px; margin-bottom:12px;">
                <a href="${link('posts.html')}?action=new" style="display:flex; align-items:center; gap:8px; font-size:12px; font-weight:600; color:#00f2fe; text-decoration:none;">
                    <span style="font-size:16px; line-height:1; font-weight:700;">+</span>
                    <span>Add Media</span>
                </a>
                <div style="display:flex; align-items:center; justify-content:space-between; font-size:12px; color:#94a3b8;">
                    <span style="display:flex; align-items:center; gap:6px;">
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>
                        Dark Mode
                    </span>
                    <label style="position:relative; display:inline-block; width:38px; height:20px; margin:0; cursor:pointer;">
                        <input type="checkbox" id="themeToggleCheckbox" ${document.body.classList.contains('dark-mode') ? 'checked' : ''} onchange="toggleThemeMode(this.checked)" style="opacity:0; width:0; height:0; position:absolute;">
                        <span style="position:absolute; top:0; left:0; right:0; bottom:0; background:${document.body.classList.contains('dark-mode') ? '#00f2fe' : '#334155'}; border-radius:20px; transition:all .2s;" id="themeToggleSlider">
                            <span style="position:absolute; top:2px; left:${document.body.classList.contains('dark-mode') ? '20px' : '2px'}; width:16px; height:16px; border-radius:50%; background:#ffffff; box-shadow:0 1px 3px rgba(0,0,0,0.3); transition:all .2s;" id="themeToggleKnob"></span>
                        </span>
                    </label>
                </div>
            </div>

            <div style="display:flex; align-items:center; justify-content:space-between; padding-top:10px; border-top:1px solid rgba(255,255,255,0.08);">
                <div class="user-snippet">
                    <div class="user-avatar" style="background:#0f172a; border:1px solid #00f2fe; color:#00f2fe;">${initials}</div>
                    <div class="user-info">
                        <div class="user-name">${user.fullName || 'User'}</div>
                        <div class="user-role">${user.organization || 'Enterprise'}</div>
                    </div>
                </div>
                <button class="btn btn-outline btn-sm" style="padding: 4px 8px; border-color:var(--border-dark); color:#94a3b8;" title="Sign out" onclick="Auth.logout()">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
                </button>
            </div>
        </div>
    `;

    // 2. Render Top Header only if empty (preserves custom headers such as on dashboard.html)
    if (headerEl && headerEl.children.length === 0) {
        headerEl.innerHTML = `
        <div class="header-search">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
            <input type="text" placeholder="Search analytics, posts, competitors..." id="globalSearchInput" onkeydown="handleGlobalSearch(event)">
        </div>

        <div class="header-actions">
            <a href="${link('notifications.html')}" class="icon-btn" title="Notifications">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>
                <span class="badge-count" id="notificationBadge" style="display:none;">0</span>
            </a>
            <a href="${link('reports.html')}" class="btn btn-primary btn-sm">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
                <span>New Report</span>
            </a>
        </div>
        `;
    }

    // Sync header avatar initials if element exists (e.g. on custom dashboard header)
    const headerAvatar = document.getElementById('headerAvatar');
    if (headerAvatar) {
        headerAvatar.textContent = initials;
    }

    // Fetch unread notification count
    refreshNotificationBadge();
}

async function refreshNotificationBadge() {
    try {
        const res = await ApiClient.get('/api/notifications/unread-count');
        const badge = document.getElementById('notificationBadge');
        if (badge && res.success && res.data > 0) {
            badge.textContent = res.data > 9 ? '9+' : res.data;
            badge.style.display = 'flex';
        }
    } catch (e) {
        // Silently catch on badge poll
    }
}

function handleGlobalSearch(e) {
    if (e.key === 'Enter') {
        const query = e.target.value.trim();
        if (query) {
            window.location.href = `posts.html?query=${encodeURIComponent(query)}`;
        }
    }
}
