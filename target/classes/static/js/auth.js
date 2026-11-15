/**
 * SPEDO Authentication & Session Controller
 */

class Auth {
    static getRedirectUrl(page) {
        return page;
    }

    static checkAuth() {
        const token = ApiClient.getToken();
        if (!token) {
            window.location.href = Auth.getRedirectUrl('login.html');
            return false;
        }
        return true;
    }

    static async login(email, password) {
        try {
            const res = await ApiClient.post('/api/auth/login', { email, password });
            if (res.success && res.data) {
                const userObj = res.data.user || res.data;
                const token = res.data.token;
                ApiClient.setToken(token);
                ApiClient.setUser(userObj);
                const name = (userObj && userObj.fullName) ? userObj.fullName : (res.data.fullName || 'User');
                showToast('Welcome back, ' + name + '!', 'success');
                setTimeout(() => {
                    window.location.href = Auth.getRedirectUrl('dashboard.html');
                }, 600);
                return res.data;
            }
        } catch (err) {
            showToast(err.message || 'Login failed. Please verify credentials.', 'error');
            throw err;
        }
    }

    static async register(fullName, email, password, organization) {
        try {
            const res = await ApiClient.post('/api/auth/register', {
                fullName,
                email,
                password,
                organization
            });
            if (res.success && res.data) {
                const userObj = res.data.user || res.data;
                const token = res.data.token;
                ApiClient.setToken(token);
                ApiClient.setUser(userObj);
                showToast('Account registered successfully! Redirecting...', 'success');
                setTimeout(() => {
                    window.location.href = Auth.getRedirectUrl('dashboard.html');
                }, 800);
                return res.data;
            }
        } catch (err) {
            showToast(err.message || 'Registration failed.', 'error');
            throw err;
        }
    }

    static async forgotPassword(email) {
        try {
            return await ApiClient.post('/api/auth/forgot-password', { email });
        } catch (err) {
            showToast(err.message || 'Failed to dispatch reset request.', 'error');
            throw err;
        }
    }

    static async resetPassword(email, token, newPassword) {
        try {
            return await ApiClient.post('/api/auth/reset-password', { email, token, newPassword });
        } catch (err) {
            showToast(err.message || 'Password reset failed. Please check code.', 'error');
            throw err;
        }
    }

    static logout() {
        ApiClient.removeToken();
        showToast('You have been logged out.');
        setTimeout(() => {
            window.location.href = Auth.getRedirectUrl('login.html');
        }, 500);
    }
}
