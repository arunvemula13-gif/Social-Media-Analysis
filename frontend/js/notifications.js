/**
 * SPEDO Notifications & Intelligence Alerts Controller
 */

let unreadOnlyFilter = false;

document.addEventListener('DOMContentLoaded', () => {
    loadNotifications();
});

async function loadNotifications() {
    const container = document.getElementById('notificationsList');
    try {
        const res = await ApiClient.get('/api/notifications', { unreadOnly: unreadOnlyFilter });
        if (!res.success || !res.data || res.data.length === 0) {
            container.innerHTML = `
                <div style="text-align:center; padding:50px; color:#94a3b8;">
                    <div style="font-size:24px; margin-bottom:8px;">🔔</div>
                    <div style="font-size:15px; font-weight:600; color:var(--text-dark);">All Caught Up!</div>
                    <p style="margin-top:4px;">No new alerts or system notifications at this time.</p>
                </div>
            `;
            return;
        }

        container.innerHTML = res.data.map(renderNotificationItem).join('');
    } catch (err) {
        container.innerHTML = `<div style="color:var(--danger); text-align:center; padding:30px;">Failed to load alerts: ${err.message}</div>`;
    }
}

function renderNotificationItem(n) {
    const isUnread = !n.read;
    const bg = isUnread ? 'rgba(67, 97, 238, 0.04)' : '#ffffff';

    const iconColor = n.type === 'METRIC_MILESTONE' ? 'var(--primary-violet)' :
                      n.type === 'REPORT_GENERATED' ? 'var(--accent-pink)' :
                      n.type === 'SYNC_STATUS' ? 'var(--success)' : 'var(--primary-blue)';

    return `
        <div style="display:flex; align-items:flex-start; justify-content:space-between; padding:18px 24px; border-bottom:1px solid var(--border-color); background:${bg}; transition:background 0.2s;"
             id="notif-${n.id}">
            <div style="display:flex; align-items:flex-start; gap:16px;">
                <div style="width:36px; height:36px; border-radius:50%; background:rgba(67,97,238,0.1); display:flex; align-items:center; justify-content:center; color:${iconColor}; flex-shrink:0; margin-top:2px;">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>
                </div>

                <div>
                    <div style="display:flex; align-items:center; gap:8px;">
                        <span style="font-weight:700; color:var(--text-dark); font-size:14px;">${n.title}</span>
                        ${isUnread ? '<span style="width:7px; height:7px; border-radius:50%; background:var(--primary-blue);"></span>' : ''}
                    </div>
                    <p style="font-size:13px; color:var(--text-secondary); margin-top:4px; line-height:1.4;">${n.message}</p>
                    ${n.link ? `<a href="${n.link}" style="display:inline-block; font-size:12px; font-weight:600; margin-top:6px;">View details →</a>` : ''}
                </div>
            </div>

            <div style="display:flex; flex-direction:column; align-items:flex-end; gap:6px; flex-shrink:0; margin-left:16px;">
                <span style="font-size:11px; color:#94a3b8;">${formatDate(n.createdAt)}</span>
                ${isUnread ? `
                    <button class="btn btn-outline btn-sm" style="font-size:11px; padding:3px 8px;" onclick="markSingleAsRead(${n.id})">
                        Mark read
                    </button>
                ` : ''}
            </div>
        </div>
    `;
}

function toggleUnreadFilter() {
    unreadOnlyFilter = !unreadOnlyFilter;
    const btn = document.getElementById('filterUnreadBtn');
    btn.textContent = unreadOnlyFilter ? 'Show All' : 'Show Unread Only';
    loadNotifications();
}

async function markSingleAsRead(id) {
    try {
        const res = await ApiClient.patch(`/api/notifications/${id}/read`);
        if (res.success) {
            await loadNotifications();
            if (typeof refreshNotificationBadge === 'function') {
                refreshNotificationBadge();
            }
        }
    } catch (err) {
        showToast('Error marking notification: ' + err.message, 'error');
    }
}

async function markAllNotificationsAsRead() {
    try {
        const res = await ApiClient.post('/api/notifications/read-all', {});
        if (res.success) {
            showToast('All notifications marked as read', 'success');
            await loadNotifications();
            if (typeof refreshNotificationBadge === 'function') {
                refreshNotificationBadge();
            }
        }
    } catch (err) {
        showToast('Error: ' + err.message, 'error');
    }
}
