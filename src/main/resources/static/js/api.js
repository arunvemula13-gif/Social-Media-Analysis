/**
 * SPEDO API Client & Utilities
 */

// Auto-detect API server: If running inside VS Code (Live Server port 5500, Vite port 3000/5173, or file://), auto-route to Spring Boot on :8080
const API_BASE_URL = (window.location.protocol === 'file:' || (window.location.port && window.location.port !== '8080'))
    ? 'http://localhost:8080'
    : window.location.origin;

let memoryStorage = {};
function safeGetStorage(key) {
    try {
        return localStorage.getItem(key);
    } catch (e) {
        return memoryStorage[key] || null;
    }
}
function safeSetStorage(key, val) {
    try {
        localStorage.setItem(key, val);
    } catch (e) {
        memoryStorage[key] = val;
    }
}
function safeRemoveStorage(key) {
    try {
        localStorage.removeItem(key);
    } catch (e) {
        delete memoryStorage[key];
    }
}

class ApiClient {
    static getToken() {
        return safeGetStorage('spedo_token');
    }

    static setToken(token) {
        safeSetStorage('spedo_token', token);
    }

    static removeToken() {
        safeRemoveStorage('spedo_token');
        safeRemoveStorage('spedo_user');
    }

    static getUser() {
        const u = safeGetStorage('spedo_user');
        try {
            return u ? JSON.parse(u) : null;
        } catch (e) {
            return null;
        }
    }

    static setUser(user) {
        safeSetStorage('spedo_user', JSON.stringify(user));
    }

    static async request(endpoint, options = {}) {
        const token = this.getToken();
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...(options.headers || {})
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const config = {
            ...options,
            headers
        };

        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, config);

            if (response.status === 401) {
                // If it's an auth endpoint, parse server error message
                if (endpoint.includes('/api/auth/')) {
                    const errData = await response.json().catch(() => ({}));
                    throw new Error(errData.message || 'Invalid email or password');
                }
                // Clear any invalid/expired token immediately
                this.removeToken();
                // If on protected pages, redirect to login
                const path = window.location.pathname;
                if (!path.endsWith('login.html') && !path.endsWith('register.html') && path !== '/' && !path.endsWith('index.html')) {
                    window.location.href = 'login.html';
                }
                throw new Error('Session expired or unauthorized. Please log in.');
            }

            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('text/csv')) {
                return await response.text();
            }

            const data = await response.json().catch(() => ({}));

            if (!response.ok) {
                let errorMsg = data.message || data.error;
                if (data.fieldErrors && Object.keys(data.fieldErrors).length > 0) {
                    errorMsg = Object.values(data.fieldErrors).join(', ');
                }
                throw new Error(errorMsg || 'A server error occurred (HTTP ' + response.status + ')');
            }

            return data;
        } catch (error) {
            console.error(`API Error on [${options.method || 'GET'} ${endpoint}]:`, error);
            if (error.message && (error.message.includes('Failed to fetch') || error.message.includes('NetworkError') || error.name === 'TypeError')) {
                throw new Error('Cannot reach SPEDO Backend at ' + API_BASE_URL + '. Please ensure your Spring Boot server is running on port 8080.');
            }
            throw error;
        }
    }

    static get(endpoint, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        const url = queryString ? `${endpoint}?${queryString}` : endpoint;
        return this.request(url, { method: 'GET' });
    }

    static post(endpoint, body) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(body)
        });
    }

    static put(endpoint, body) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(body)
        });
    }

    static patch(endpoint, body = {}) {
        return this.request(endpoint, {
            method: 'PATCH',
            body: JSON.stringify(body)
        });
    }

    static delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
}

// Global Toast Notification Helper
function showToast(message, type = 'success') {
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <div style="display:flex; align-items:center; gap:8px;">
            <strong>${type === 'success' ? '✓' : type === 'error' ? '✕' : 'ℹ'}</strong>
            <span>${message}</span>
        </div>
        <button style="background:none; border:none; cursor:pointer; color:#94a3b8; font-size:16px;" onclick="this.parentElement.remove()">×</button>
    `;

    container.appendChild(toast);
    setTimeout(() => {
        toast.remove();
    }, 4500);
}

// Format numbers: 142500 -> 142.5K
function formatNumber(num) {
    if (num === null || num === undefined) return '0';
    if (num >= 1000000) return (num / 1000000).toFixed(1).replace(/\.0$/, '') + 'M';
    if (num >= 1000) return (num / 1000).toFixed(1).replace(/\.0$/, '') + 'K';
    return num.toLocaleString();
}

// Format date: 2026-10-26T14:30:00 -> Oct 26, 2026
function formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}
