/**
 * SPEDO Social Media Account Management Controller
 */

document.addEventListener('DOMContentLoaded', () => {
    loadAccounts();
});

async function loadAccounts() {
    const container = document.getElementById('accountsGrid');
    try {
        const res = await ApiClient.get('/api/social-accounts');
        if (!res.success || !res.data || res.data.length === 0) {
            container.innerHTML = `
                <div style="grid-column:1/-1; text-align:center; padding:48px; background:#fff; border-radius:12px; border:1px dashed var(--border-color);">
                    <div style="font-size:18px; font-weight:700; color:var(--text-dark); margin-bottom:8px;">No Social Accounts Connected</div>
                    <p style="color:var(--text-secondary); max-width:400px; margin:0 auto 20px auto;">Connect your Instagram, X, LinkedIn, Facebook, or YouTube accounts to begin automated intelligence tracking.</p>
                    <button class="btn btn-primary" onclick="openConnectModal()">Connect First Account</button>
                </div>
            `;
            return;
        }

        container.innerHTML = res.data.map(account => renderAccountCard(account)).join('');
    } catch (err) {
        container.innerHTML = `<div style="grid-column:1/-1; color:var(--danger); padding:20px; text-align:center;">Failed to load accounts: ${err.message}</div>`;
    }
}

function renderAccountCard(acc) {
    const pClass = `pill-${acc.platform.toLowerCase()}`;
    const statusClass = `status-${acc.status.toLowerCase()}`;
    const avatar = acc.profileImageUrl || 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150';
    const stats = acc.platform === 'YOUTUBE'
        ? `
            <div style="display:grid; grid-template-columns:repeat(3, 1fr); gap:12px; background:var(--bg-muted); border-radius:8px; padding:12px; margin-bottom:16px;">
                <div>
                    <div style="font-size:11px; color:#64748b; text-transform:uppercase;">Subscribers</div>
                    <div style="font-size:18px; font-weight:800; color:var(--text-dark);">${formatNumber(acc.followerCount)}</div>
                </div>
                <div>
                    <div style="font-size:11px; color:#64748b; text-transform:uppercase;">Channel views</div>
                    <div style="font-size:18px; font-weight:800; color:var(--text-dark);">${formatNumber(acc.channelViews)}</div>
                </div>
                <div>
                    <div style="font-size:11px; color:#64748b; text-transform:uppercase;">Videos</div>
                    <div style="font-size:18px; font-weight:800; color:var(--text-dark);">${formatNumber(acc.videoCount)}</div>
                </div>
            </div>`
        : `
            <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px; background:var(--bg-muted); border-radius:8px; padding:12px; margin-bottom:16px;">
                <div>
                    <div style="font-size:11px; color:#64748b; text-transform:uppercase;">Followers</div>
                    <div style="font-size:18px; font-weight:800; color:var(--text-dark);">${formatNumber(acc.followerCount)}</div>
                </div>
                <div>
                    <div style="font-size:11px; color:#64748b; text-transform:uppercase;">Following</div>
                    <div style="font-size:18px; font-weight:800; color:var(--text-dark);">${formatNumber(acc.followingCount)}</div>
                </div>
            </div>`;

    return `
        <div class="card" style="display:flex; flex-direction:column; justify-content:space-between; position:relative;">
            <div>
                <div style="display:flex; align-items:flex-start; justify-content:space-between; margin-bottom:16px;">
                    <div style="display:flex; align-items:center; gap:12px;">
                        <img src="${avatar}" alt="${acc.accountName}" style="width:48px; height:48px; border-radius:50%; object-fit:cover; border:2px solid #e2e8f0;">
                        <div>
                            <div style="font-weight:700; color:var(--text-dark); font-size:15px;">${acc.accountName}</div>
                            <div style="font-size:12px; color:var(--text-secondary);">${acc.handle}</div>
                        </div>
                    </div>
                    <span class="platform-pill ${pClass}">${acc.platform}</span>
                </div>

                ${stats}

                <div style="display:flex; align-items:center; justify-content:space-between; font-size:12px; margin-bottom:16px;">
                    <div>
                        <span class="status-badge ${statusClass}">${acc.status}</span>
                        ${acc.isDemo ? '<span style="font-size:11px; color:#b45309; margin-left:6px;">Sample data</span>' : ''}
                    </div>
                    <span style="color:#94a3b8; font-size:11px;">
                        Synced: ${acc.lastSyncedAt ? formatDate(acc.lastSyncedAt) : 'Never'}
                    </span>
                </div>
            </div>

            <div style="display:flex; align-items:center; justify-content:space-between; border-top:1px solid var(--border-color); padding-top:14px; margin-top:8px;">
                <button class="btn btn-outline btn-sm" onclick="syncSingleAccount(${acc.id})" id="syncBtn-${acc.id}">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"></polyline><polyline points="1 20 1 14 7 14"></polyline><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path></svg>
                    <span>Sync</span>
                </button>
                <button class="btn btn-sm" style="color:var(--danger); background:none; border:none; cursor:pointer;" onclick="disconnectAccount(${acc.id}, '${acc.accountName}')">
                    Disconnect
                </button>
            </div>
        </div>
    `;
}

function openConnectModal() {
    const platformSelect = document.getElementById('connectPlatform');
    if (platformSelect) {
        handlePlatformChange(platformSelect.value);
    }
    document.getElementById('connectModal').classList.add('open');
}

function handlePlatformChange(platform) {
    const isYouTube = platform === 'YOUTUBE';
    const isMeta = platform === 'INSTAGRAM' || platform === 'FACEBOOK';

    const ytBanner = document.getElementById('youtubeInfoBanner');
    const metaGroup = document.getElementById('metaTokenGroup');
    const followersGroup = document.getElementById('manualFollowersGroup');
    const handleInput = document.getElementById('connectHandle');
    const nameInput = document.getElementById('connectAccountName');
    const helpText = document.getElementById('handleHelpText');

    if (ytBanner) ytBanner.style.display = isYouTube ? 'block' : 'none';
    if (metaGroup) metaGroup.style.display = isMeta ? 'block' : 'none';
    if (followersGroup) followersGroup.style.display = isYouTube ? 'none' : 'grid';

    if (isYouTube) {
        if (handleInput) handleInput.placeholder = 'e.g. @way4news999 or UCbtRM4Y4n7Id6trpT38zJ3g';
        if (nameInput) nameInput.placeholder = 'e.g. Way4News (auto-fills from YouTube)';
        if (helpText) helpText.textContent = 'Enter an @handle, channel ID (UC...), or youtube.com URL. Subscribers, views, videos & comments sync automatically!';
    } else {
        if (handleInput) handleInput.placeholder = 'e.g. @username or profile URL';
        if (nameInput) nameInput.placeholder = 'e.g. Brand Name';
        if (helpText) helpText.textContent = 'Enter username or full profile URL.';
    }
}

function closeConnectModal() {
    document.getElementById('connectModal').classList.remove('open');
}

function handleHandleInput(val) {
    const nameInput = document.getElementById('connectAccountName');
    if (!nameInput.value || nameInput.dataset.autoFilled === 'true') {
        const clean = cleanHandleString(val);
        if (clean) {
            nameInput.value = clean.charAt(0).toUpperCase() + clean.slice(1);
            nameInput.dataset.autoFilled = 'true';
        }
    }
}

function cleanHandleString(raw) {
    if (!raw) return '';
    let s = raw.trim();
    if (s.startsWith('http://') || s.startsWith('https://')) {
        try {
            const url = new URL(s);
            const parts = url.pathname.split('/').filter(p => p.length > 0);
            if (parts.length > 0) s = parts[0].toLowerCase() === 'channel' && parts.length > 1 ? parts[1] : parts[0];
        } catch (e) {}
    }
    return s.replace(/^[@/]+/, '').replace(/[/?#].*$/, '').trim();
}

function youtubeChannelIdFromInput(raw) {
    const value = raw.trim();
    if (/^UC[\w-]{20,}$/.test(value)) return value;

    try {
        const url = new URL(value);
        if (!url.hostname.endsWith('youtube.com')) return null;
        const parts = url.pathname.split('/').filter(Boolean);
        if (parts.length >= 2 && parts[0].toLowerCase() === 'channel' && /^UC[\w-]{20,}$/.test(parts[1])) {
            return parts[1];
        }
    } catch (e) {}

    return null;
}

async function handleConnectSubmit(e) {
    e.preventDefault();
    const platform = document.getElementById('connectPlatform').value;
    const rawHandle = document.getElementById('connectHandle').value.trim();
    const clean = cleanHandleString(rawHandle);
    const accountName = document.getElementById('connectAccountName').value.trim() || clean;
    const profileImageUrl = document.getElementById('connectAvatar').value.trim();
    const followersVal = document.getElementById('connectFollowers').value.trim();
    const followingVal = document.getElementById('connectFollowing').value.trim();
    const accessToken = document.getElementById('connectAccessToken') ? document.getElementById('connectAccessToken').value.trim() : '';
    const isDemo = false;

    const initialFollowers = followersVal ? parseInt(followersVal, 10) : 0;
    const initialFollowing = followingVal ? parseInt(followingVal, 10) : 0;
    const accountId = platform === 'YOUTUBE' ? youtubeChannelIdFromInput(rawHandle) : null;

    const btn = document.getElementById('saveConnectBtn');
    btn.disabled = true;
    btn.textContent = 'Connecting...';

    try {
        const res = await ApiClient.post('/api/social-accounts', {
            platform,
            accountName,
            accountId,
            handle: clean ? '@' + clean : rawHandle,
            profileImageUrl,
            initialFollowers,
            initialFollowing,
            accessToken: accessToken || null,
            isDemo
        });

        if (res.success) {
            showToast(`Connected ${accountName} (${platform})!`, 'success');
            closeConnectModal();
            document.getElementById('connectForm').reset();
            const nameInput = document.getElementById('connectAccountName');
            if (nameInput) delete nameInput.dataset.autoFilled;
            await loadAccounts();
        }
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Connect Account';
    }
}

async function syncSingleAccount(id) {
    const btn = document.getElementById(`syncBtn-${id}`);
    if (btn) {
        btn.disabled = true;
        btn.innerHTML = '<span>Syncing...</span>';
    }

    try {
        const res = await ApiClient.post(`/api/social-accounts/${id}/sync`, {});
        if (res.success) {
            showToast(res.message || 'Account synchronized successfully!', 'success');
            await loadAccounts();
        }
    } catch (err) {
        showToast('Sync failed: ' + err.message, 'error');
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = '<span>Sync</span>';
        }
    }
}

async function disconnectAccount(id, name) {
    if (!confirm(`Are you sure you want to disconnect "${name}"? Historical metrics for this channel will be removed.`)) {
        return;
    }

    try {
        const res = await ApiClient.delete(`/api/social-accounts/${id}`);
        if (res.success) {
            showToast(`Disconnected ${name}.`, 'success');
            await loadAccounts();
        }
    } catch (err) {
        showToast('Failed to disconnect: ' + err.message, 'error');
    }
}
