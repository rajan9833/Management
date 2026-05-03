/**
 * api.js — Shared API client for Employee Management System
 * Resolves URLs relative to the current page (same host/port/context) so deployments like Render stay correct,
 * skips Authorization when no JWT (avoid invalid "Bearer null"), and restores data after BFCache.
 */

/** Build an absolute URL to a servlet path beneath the directory of the current HTML file. */
function apiUrl(path) {
    const p = path.startsWith('/') ? path.slice(1) : path;
    return new URL(p, window.location.href).toString();
}

// ── Token Management ─────────────────────────────────────────
const Auth = {
    getToken:     ()       => localStorage.getItem('emp_token'),
    getUser:      ()       => localStorage.getItem('emp_user'),
    setSession:   (t, u)   => {
        localStorage.setItem('emp_token', t);
        localStorage.setItem('emp_user', u);
    },
    clearSession: ()       => {
        localStorage.removeItem('emp_token');
        localStorage.removeItem('emp_user');
    },
    isLoggedIn:   ()       => !!localStorage.getItem('emp_token'),
    requireAuth:  ()       => {
        if (!Auth.isLoggedIn()) window.location.replace('login.html');
    },
};

/** After logout/login, BFCache may restore dashboard without re-fetch; pages register a reload hook. */
if (!window.__emsPageReloadListener) {
    window.__emsPageReloadListener = true;
    window.addEventListener('pageshow', (event) => {
        if (!event.persisted || !Auth.isLoggedIn()) return;
        if (typeof window.__emsReload === 'function') {
            Promise.resolve(window.__emsReload()).catch(() => {});
        }
    });
}

/** Call from authenticated pages once your loadFoo() exists: registerAuthReload(loadFoo); */
function registerAuthReload(fn) {
    window.__emsReload = fn;
}

// ── HTTP Client ──────────────────────────────────────────────
const API = {
    headers(isFormData = false) {
        const h = {};
        const token = Auth.getToken();
        if (token) h['Authorization'] = `Bearer ${token}`;
        if (!isFormData) h['Content-Type'] = 'application/json';
        return h;
    },

    async request(method, path, body = null, isFormData = false) {
        const opts = { method, headers: this.headers(isFormData) };
        if (body) opts.body = isFormData ? body : JSON.stringify(body);

        const res = await fetch(apiUrl(path), opts);

        const ct = res.headers.get('content-type') || '';
        const json = ct.includes('application/json');

        if (res.status === 401 || res.status === 403) {
            Auth.clearSession();
            const onLogin = /login\.html$/i.test(window.location.pathname);
            if (!onLogin) window.location.replace('login.html');
            throw new Error('Session expired — please login again.');
        }

        if (!json) {
            await res.text();
            throw new Error(`Unexpected response (${res.status})`);
        }

        return res.json();
    },

    get:     (path)            => API.request('GET',    path),
    post:    (path, body)      => API.request('POST',   path, body),
    put:     (path, body)      => API.request('PUT',    path, body),
    delete:  (path)            => API.request('DELETE', path),
    postForm:(path, formData)  => API.request('POST',   path, formData, true),
    putForm: (path, formData)  => API.request('PUT',    path, formData, true),

    login: (username, password) =>
        fetch(apiUrl('auth/login'), {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password }),
        }).then((r) => r.json()),

    getEmployees:         ()                         => API.get('employees/all'),
    searchEmployees:      (q)                        => API.get(`employees/search?query=${encodeURIComponent(q)}`),
    getEmployee:          (id)                       => API.get(`employees/${id}`),
    addEmployee:          (fd)                       => API.postForm('employees/add', fd),
    updateEmployee:       (id, fd)                   => API.putForm(`employees/${id}`, fd),
    deleteEmployee:       (id)                       => API.delete(`employees/${id}`),

    getDashboardStats:    ()                         => API.get('attendance/dashboard'),
    getAttendanceByDate:(date)                      => API.get(`attendance/date?date=${date}`),
    markAttendance:       (data)                     => API.post('attendance/mark', data),
    markBulkAttendance:   (list)                     => API.post('attendance/mark/bulk', list),
    getMonthlyReport:     (id, m, y)                 => API.get(`attendance/report?empId=${id}&month=${m}&year=${y}`),
    getSalaryReport:      (id, m, y)                 => API.get(`attendance/salary?empId=${id}&month=${m}&year=${y}`),
    getAllSalaryReports:  (m, y)                     => API.get(`attendance/salary/all?month=${m}&year=${y}`),
};

// ── UI Helpers ───────────────────────────────────────────────
const UI = {
    toast(message, type = 'success') {
        const el = document.getElementById('toast');
        if (!el) return;
        el.textContent = message;
        el.className = `toast toast-${type} show`;
        setTimeout(() => el.classList.remove('show'), 3500);
    },

    currency(amount) {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency', currency: 'INR', maximumFractionDigits: 0,
        }).format(amount);
    },

    formatDate(dateStr) {
        return new Date(dateStr).toLocaleDateString('en-IN', {
            day: 'numeric', month: 'short', year: 'numeric',
        });
    },

    initials(name) {
        return name.split(' ').map((w) => w[0]).join('').toUpperCase().slice(0, 2);
    },

    setLoading(btn, loading) {
        if (!btn) return;
        btn.disabled = loading;
        btn.dataset.original = btn.dataset.original || btn.innerHTML;
        btn.innerHTML = loading
            ? `<svg class="spin inline w-4 h-4 mr-2" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/></svg>Processing...`
            : btn.dataset.original;
    },

    deptColor(dept) {
        const map = {
            Engineering: 'bg-blue-100 text-blue-800',
            HR:          'bg-purple-100 text-purple-800',
            Finance:     'bg-green-100 text-green-800',
            Management:  'bg-orange-100 text-orange-800',
            Marketing:   'bg-pink-100 text-pink-800',
        };
        return map[dept] || 'bg-gray-100 text-gray-700';
    },

    statusBadge(status) {
        return status === 'PRESENT'
            ? '<span class="badge-present">● Present</span>'
            : '<span class="badge-absent">● Absent</span>';
    },
};

const MONTHS = ['January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'];
