/**
 * sidebar.js — Renders the shared sidebar and topbar on every page.
 * Inject <div id="app-shell"></div> and call renderShell('active-page-id')
 */

function renderShell(activePage) {
    const user = Auth.getUser() || 'Admin';

    const navItems = [
        { id: 'dashboard',  href: 'dashboard.html',  icon: homeIcon(),   label: 'Dashboard'   },
        { id: 'employees',  href: 'employees.html',  icon: usersIcon(),  label: 'Employees'   },
        { id: 'addEmployee',href: 'add-employee.html',icon: addUserIcon(),label: 'Add Employee'},
        { id: 'attendance', href: 'attendance.html', icon: calIcon(),    label: 'Attendance'  },
        { id: 'reports',    href: 'reports.html',    icon: chartIcon(),  label: 'Reports'     },
    ];

    const navHTML = navItems.map(item => `
        <a href="${item.href}" class="nav-item ${activePage === item.id ? 'active' : ''}">
            ${item.icon}
            <span>${item.label}</span>
        </a>
    `).join('');

    document.getElementById('app-shell').innerHTML = `
        <!-- Sidebar -->
        <aside class="sidebar" id="sidebar">
            <div class="sidebar-logo">
                <div class="logo-icon">👥</div>
                <div>
                    <span>EMS Portal<small>Employee Management</small></span>
                </div>
            </div>
            <nav>
                <div class="nav-section-label">Main Menu</div>
                ${navHTML}
                <div class="nav-section-label" style="margin-top:1rem;">Account</div>
                <div class="nav-item" onclick="logout()">
                    ${logoutIcon()}
                    <span>Logout</span>
                </div>
            </nav>
            <div class="sidebar-footer">
                <div style="display:flex;align-items:center;gap:0.75rem;">
                    <div class="avatar" style="background:#312E81;color:#A5B4FC;font-size:0.7rem;">${UI.initials(user)}</div>
                    <div>
                        <div style="font-size:0.8rem;font-weight:600;color:rgba(255,255,255,0.85);">${user}</div>
                        <div style="font-size:0.65rem;color:rgba(255,255,255,0.4);">Administrator</div>
                    </div>
                </div>
            </div>
        </aside>

        <!-- Toast -->
        <div id="toast" class="toast"></div>

        <!-- Mobile overlay -->
        <div id="sidebar-overlay" style="display:none;position:fixed;inset:0;background:rgba(0,0,0,0.4);z-index:49;"
             onclick="closeSidebar()"></div>
    `;
}

function logout() {
    if (confirm('Are you sure you want to logout?')) {
        Auth.clearSession();
        window.__emsReload = undefined;
        window.location.replace('login.html');
    }
}

function openSidebar() {
    document.getElementById('sidebar').classList.add('open');
    document.getElementById('sidebar-overlay').style.display = 'block';
}

function closeSidebar() {
    document.getElementById('sidebar').classList.remove('open');
    document.getElementById('sidebar-overlay').style.display = 'none';
}

// ── SVG Icon helpers ──────────────────────────────────────────
function homeIcon()    { return `<svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9,22 9,12 15,12 15,22"/></svg>`; }
function usersIcon()   { return `<svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`; }
function addUserIcon() { return `<svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/><line x1="12" y1="13" x2="12" y2="22"/><line x1="8" y1="17" x2="16" y2="17"/></svg>`; }
function calIcon()     { return `<svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>`; }
function chartIcon()   { return `<svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>`; }
function logoutIcon()  { return `<svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16,17 21,12 16,7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>`; }