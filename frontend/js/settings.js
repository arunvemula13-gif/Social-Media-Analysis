/**
 * SPEDO Settings & User Profile Controller
 */

document.addEventListener('DOMContentLoaded', () => {
    loadUserProfile();
});

async function loadUserProfile() {
    try {
        const res = await ApiClient.get('/api/auth/me');
        if (res.success && res.data) {
            const user = res.data;
            document.getElementById('profileFullName').value = user.fullName || '';
            document.getElementById('profileEmail').value = user.email || '';
            document.getElementById('profileOrg').value = user.organization || 'Independent Creator';
            document.getElementById('profileRole').value = user.role || 'ROLE_USER';

            // Also synchronize local storage cached profile
            ApiClient.setUser(user);
        }
    } catch (err) {
        showToast('Error loading profile: ' + err.message, 'error');
    }
}

async function handleChangePassword(e) {
    e.preventDefault();
    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (newPassword !== confirmPassword) {
        showToast('New passwords do not match. Please verify.', 'error');
        return;
    }

    if (newPassword.length < 6) {
        showToast('New password must be at least 6 characters long.', 'error');
        return;
    }

    const btn = document.getElementById('savePasswordBtn');
    btn.disabled = true;
    btn.textContent = 'Updating...';

    try {
        const res = await ApiClient.post('/api/auth/change-password', {
            currentPassword,
            newPassword
        });

        if (res.success) {
            showToast('Password updated successfully!', 'success');
            document.getElementById('changePasswordForm').reset();
        }
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Update Password';
    }
}
