/**
 * SPEDO Random Giveaway Winner Picker Controller
 * Fair Winner Picker powered by Java SecureRandom CSPRNG
 */

let currentPlatformFilter = 'AUTO';
let currentSourceMode = 'URL';
let lastFetchedResponse = null;
let currentGiveaway = null;
let rouletteInterval = null;
let confettiAnimationId = null;

document.addEventListener('DOMContentLoaded', () => {
    initGiveawayPage();
});

function initGiveawayPage() {
    loadGiveawayHistory();

    // Setup input listeners
    const urlInput = document.getElementById('giveawayPostUrl');
    if (urlInput) {
        urlInput.addEventListener('input', () => {
            const val = urlInput.value.trim().toLowerCase();
            if (currentPlatformFilter === 'AUTO') {
                if (val.includes('youtube.com') || val.includes('youtu.be')) {
                    setPlatformBadgeStyle('YOUTUBE');
                } else if (val.includes('instagram.com')) {
                    setPlatformBadgeStyle('INSTAGRAM');
                }
            }
        });
    }

    // Modal close on backdrop click or ESC
    const modal = document.getElementById('historyModal');
    if (modal) {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) closeHistoryModal();
        });
    }
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') closeHistoryModal();
    });

    const urlParams = new URLSearchParams(window.location.search);
    const postIdParam = urlParams.get('postId');
    if (postIdParam) {
        setSourceMode('CONNECTED');
    }
}

function scrollToHistory() {
    const section = document.getElementById('giveawayHistorySection');
    if (section) {
        section.scrollIntoView({ behavior: 'smooth' });
    }
}

function setPlatformFilter(platform) {
    currentPlatformFilter = platform;

    // Update button states
    ['Auto', 'Yt', 'Ig'].forEach(p => {
        const btn = document.getElementById('btn' + p + 'Platform');
        if (btn) btn.classList.remove('active');
    });

    const urlInput = document.getElementById('giveawayPostUrl');
    if (platform === 'AUTO') {
        const btn = document.getElementById('btnAutoPlatform');
        if (btn) btn.classList.add('active');
        if (urlInput) urlInput.placeholder = 'e.g. https://www.youtube.com/watch?v=XFH2Q56Tw6I or https://www.instagram.com/p/...';
    } else if (platform === 'YOUTUBE') {
        const btn = document.getElementById('btnYtPlatform');
        if (btn) btn.classList.add('active');
        if (urlInput) urlInput.placeholder = 'e.g. https://www.youtube.com/watch?v=XFH2Q56Tw6I or YouTube Shorts URL';
    } else if (platform === 'INSTAGRAM') {
        const btn = document.getElementById('btnIgPlatform');
        if (btn) btn.classList.add('active');
        if (urlInput) urlInput.placeholder = 'e.g. https://www.instagram.com/p/DB12345/ or Reel URL';
    }
}

function setPlatformBadgeStyle(platform) {
    const badge = document.getElementById('previewPlatformBadge');
    if (!badge) return;
    badge.textContent = platform;
    badge.style.display = 'inline-block';
    if (platform === 'YOUTUBE') {
        badge.className = 'platform-pill pill-youtube';
    } else if (platform === 'INSTAGRAM') {
        badge.className = 'platform-pill pill-instagram';
    } else {
        badge.className = 'platform-pill';
    }
}

function handleSourceModeChange(mode) {
    currentSourceMode = mode;
    const urlContainer = document.getElementById('urlInputContainer');
    const connectedContainer = document.getElementById('connectedSelectContainer');

    if (mode === 'URL') {
        if (urlContainer) urlContainer.style.display = 'block';
        if (connectedContainer) connectedContainer.style.display = 'none';
    } else {
        if (urlContainer) urlContainer.style.display = 'none';
        if (connectedContainer) connectedContainer.style.display = 'block';
        loadConnectedAccounts();
    }
}

async function loadConnectedAccounts() {
    const selectAcc = document.getElementById('selectConnectedAccount');
    if (!selectAcc) return;

    try {
        selectAcc.innerHTML = '<option value="">Loading accounts...</option>';
        const res = await ApiClient.get('/api/social-accounts');
        if (res.success && res.data && res.data.length > 0) {
            const relevant = res.data.filter(a => a.platform === 'YOUTUBE' || a.platform === 'INSTAGRAM');
            if (relevant.length === 0) {
                selectAcc.innerHTML = '<option value="">No YouTube or Instagram accounts connected</option>';
                return;
            }
            selectAcc.innerHTML = relevant.map(a => `
                <option value="${a.id}">${a.accountName || a.username || a.channelTitle} (${a.platform})</option>
            `).join('');

            loadConnectedAccountPosts(relevant[0].id);
        } else {
            selectAcc.innerHTML = '<option value="">No connected social accounts</option>';
        }
    } catch (e) {
        selectAcc.innerHTML = '<option value="">Failed to load accounts</option>';
    }
}

async function loadConnectedAccountPosts(accountId) {
    const selectPost = document.getElementById('selectConnectedPost');
    if (!selectPost) return;

    if (!accountId) {
        selectPost.innerHTML = '<option value="">Select an account first</option>';
        return;
    }

    try {
        selectPost.innerHTML = '<option value="">Loading posts...</option>';
        const res = await ApiClient.get('/api/posts', { page: 0, size: 50 });
        if (res.success && res.data && res.data.content && res.data.content.length > 0) {
            const posts = res.data.content;
            selectPost.innerHTML = posts.map(p => `
                <option value="${p.id}" data-url="${p.postUrl || ''}" data-platform="${p.platform}">
                    ${(p.caption || 'Post #' + p.id).substring(0, 48)}... (${p.platform})
                </option>
            `).join('');
        } else {
            selectPost.innerHTML = '<option value="">No posts found for this account</option>';
        }
    } catch (e) {
        selectPost.innerHTML = '<option value="">Failed to load posts</option>';
    }
}

function handleConnectedPostChange(postId) {
    // Post selected from connected dropdown
}

async function fetchParticipants() {
    const btn = document.getElementById('btnFetchParticipants');
    const originalText = btn ? btn.innerHTML : 'Fetch Comments';

    let postUrl = '';
    let postId = null;

    if (currentSourceMode === 'URL') {
        const urlInput = document.getElementById('giveawayPostUrl');
        postUrl = urlInput ? urlInput.value.trim() : '';
        if (!postUrl) {
            showToast('Please enter a valid YouTube or Instagram post/video URL', 'warning');
            if (urlInput) urlInput.focus();
            return;
        }
    } else {
        const selectPost = document.getElementById('selectConnectedPost');
        if (!selectPost || !selectPost.value) {
            showToast('Please select a connected post first', 'warning');
            return;
        }
        postId = parseInt(selectPost.value);
    }

    const filterDuplicates = document.getElementById('filterDuplicates')?.checked ?? true;
    const requiredKeyword = document.getElementById('requiredKeyword')?.value?.trim() || null;
    const excludedUsernames = document.getElementById('excludedUsernames')?.value?.trim() || null;

    const payload = {
        platform: currentPlatformFilter === 'AUTO' ? null : currentPlatformFilter,
        postUrl: postUrl || null,
        postId: postId,
        filterDuplicates: filterDuplicates,
        requiredKeyword: requiredKeyword,
        excludedUsernames: excludedUsernames
    };

    try {
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner" style="width:14px; height:14px; border-width:2px; display:inline-block; vertical-align:middle; margin-right:6px;"></span> Fetching...';
        }

        const streamStatus = document.getElementById('participantStreamStatus');
        if (streamStatus) streamStatus.textContent = 'Fetching from official API...';

        const res = await ApiClient.post('/api/giveaways/fetch-participants', payload);

        if (!res.success || !res.data) {
            throw new Error(res.message || 'Failed to fetch giveaway participants');
        }

        lastFetchedResponse = res.data;
        renderFetchedTelemetry(lastFetchedResponse);
        renderParticipantStream(lastFetchedResponse.participants);

        // Update draw trigger button
        const btnDraw = document.getElementById('btnRunDraw');
        if (btnDraw) {
            if (lastFetchedResponse.eligibleCount > 0) {
                btnDraw.disabled = false;
                // Auto-cap max winners
                const winnersInput = document.getElementById('giveawayWinnersCount');
                if (winnersInput) {
                    winnersInput.max = Math.max(1, lastFetchedResponse.eligibleCount);
                }
                showToast(`Retrieved ${lastFetchedResponse.totalCommentsFetched} comments with ${lastFetchedResponse.eligibleCount} eligible entries!`, 'success');
            } else {
                btnDraw.disabled = true;
                showToast('No eligible entries found with the current keyword/exclusion filters.', 'warning');
            }
        }

    } catch (err) {
        console.error('Fetch participants error:', err);
        showToast(err.message || 'Error fetching comments from API', 'error');
        const streamStatus = document.getElementById('participantStreamStatus');
        if (streamStatus) streamStatus.textContent = 'Error';
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = originalText;
        }
    }
}

function renderFetchedTelemetry(data) {
    const emptyState = document.getElementById('postPreviewEmpty');
    const detailsState = document.getElementById('postPreviewDetails');
    if (emptyState) emptyState.style.display = 'none';
    if (detailsState) detailsState.style.display = 'block';

    setPlatformBadgeStyle(data.platform);

    const thumb = document.getElementById('previewMediaThumb');
    if (thumb) {
        thumb.src = data.postThumbnail || (data.platform === 'YOUTUBE' 
            ? 'https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=300'
            : 'https://images.unsplash.com/photo-1611262588024-d12430b98920?w=300');
    }

    const caption = document.getElementById('previewCaption');
    if (caption) {
        caption.textContent = data.postCaption || 'Social Media Giveaway Post';
    }

    const link = document.getElementById('previewPostLink');
    if (link) {
        if (data.postUrl) {
            link.href = data.postUrl;
            link.style.display = 'inline-flex';
        } else {
            link.style.display = 'none';
        }
    }

    // Update 4 telemetry counters
    const totalElem = document.getElementById('statTotalFetched');
    if (totalElem) totalElem.textContent = Number(data.totalCommentsFetched || 0).toLocaleString();

    const uniqueElem = document.getElementById('statUniqueUsers');
    if (uniqueElem) uniqueElem.textContent = Number(data.uniqueUsersCount || 0).toLocaleString();

    const eligibleElem = document.getElementById('statEligibleEntries');
    if (eligibleElem) eligibleElem.textContent = Number(data.eligibleCount || 0).toLocaleString();

    const filteredTotal = (data.duplicateCount || 0) + (data.keywordFilteredCount || 0) + (data.excludedUsersCount || 0);
    const filteredElem = document.getElementById('statFilteredEntries');
    if (filteredElem) filteredElem.textContent = Number(filteredTotal).toLocaleString();

    // Update filter chips
    const chipDup = document.getElementById('chipDuplicates');
    if (chipDup) chipDup.textContent = `Duplicates removed: ${data.duplicateCount || 0}`;

    const chipKey = document.getElementById('chipKeyword');
    if (chipKey) chipKey.textContent = `Keyword filtered: ${data.keywordFilteredCount || 0}`;

    const chipExc = document.getElementById('chipExcluded');
    if (chipExc) chipExc.textContent = `Blacklisted: ${data.excludedUsersCount || 0}`;
}

function renderParticipantStream(participants) {
    const countElem = document.getElementById('participantStreamCount');
    if (countElem) countElem.textContent = participants ? participants.length : 0;

    const statusElem = document.getElementById('participantStreamStatus');
    if (statusElem) {
        statusElem.textContent = participants && participants.length > 0 ? '✅ Ready to Draw' : 'No Eligible Entries';
    }

    const list = document.getElementById('participantStreamList');
    if (!list) return;

    if (!participants || participants.length === 0) {
        list.innerHTML = `
            <div style="text-align:center; padding:30px; color:#94a3b8; font-size:12px;">
                No eligible participants match your criteria.
            </div>
        `;
        return;
    }

    list.innerHTML = participants.map((p, idx) => {
        const avatar = p.userAvatar || `https://api.dicebear.com/7.x/identicon/svg?seed=${encodeURIComponent(p.username)}`;
        const safeComment = escapeHtml(p.commentText || '');
        const timeStr = p.commentedAt ? new Date(p.commentedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '';

        return `
            <div style="display:flex; align-items:flex-start; gap:10px; padding:8px 0; border-bottom:1px solid var(--border-hairline);">
                <img src="${avatar}" alt="${escapeHtml(p.username)}" style="width:32px; height:32px; border-radius:50%; object-fit:cover; background:var(--surface-muted); flex-shrink:0;">
                <div style="flex:1; min-width:0;">
                    <div style="display:flex; justify-content:space-between; align-items:center;">
                        <span style="font-weight:700; font-size:12px; color:var(--text-primary);">${escapeHtml(p.username)}</span>
                        <span style="font-size:10px; color:#94a3b8;">${timeStr}</span>
                    </div>
                    <p style="font-size:11px; color:var(--text-secondary); margin:2px 0 0 0; line-height:1.4; word-break:break-word;">
                        ${safeComment}
                    </p>
                </div>
            </div>
        `;
    }).join('');
}

async function executeGiveawayDraw() {
    if (!lastFetchedResponse || !lastFetchedResponse.participants || lastFetchedResponse.participants.length === 0) {
        showToast('Please fetch comments first before picking winners', 'warning');
        return;
    }

    const winnersInput = document.getElementById('giveawayWinnersCount');
    const backupInput = document.getElementById('giveawayBackupCount');
    const titleInput = document.getElementById('giveawayTitle');

    const winnersCount = parseInt(winnersInput ? winnersInput.value : 1) || 1;
    const backupCount = parseInt(backupInput ? backupInput.value : 0) || 0;
    const title = (titleInput && titleInput.value.trim()) ? titleInput.value.trim() : 'Official Social Giveaway';

    if (winnersCount < 1) {
        showToast('Primary winners count must be at least 1', 'warning');
        return;
    }

    const totalNeeded = winnersCount + backupCount;
    if (totalNeeded > lastFetchedResponse.eligibleCount) {
        showToast(`Cannot pick ${totalNeeded} total winners from ${lastFetchedResponse.eligibleCount} eligible entries`, 'warning');
        return;
    }

    const payload = {
        title: title,
        platform: lastFetchedResponse.platform,
        postUrl: lastFetchedResponse.postUrl,
        platformPostId: lastFetchedResponse.platformPostId,
        postCaption: lastFetchedResponse.postCaption,
        postThumbnail: lastFetchedResponse.postThumbnail,
        winnersCount: winnersCount,
        backupWinnersCount: backupCount,
        filterDuplicates: document.getElementById('filterDuplicates')?.checked ?? true,
        requiredKeyword: document.getElementById('requiredKeyword')?.value?.trim() || null,
        excludedUsernames: document.getElementById('excludedUsernames')?.value?.trim() || null
    };

    const drawStage = document.getElementById('drawAnimationStage');
    const celebSection = document.getElementById('winnerCelebrationSection');
    const btnDraw = document.getElementById('btnRunDraw');

    try {
        if (btnDraw) btnDraw.disabled = true;
        if (celebSection) celebSection.style.display = 'none';

        // Show Roulette Wheel Animation
        if (drawStage) {
            drawStage.style.display = 'block';
            drawStage.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }

        const participants = lastFetchedResponse.participants;
        const nameElem = document.getElementById('rouletteName');
        const avatarElem = document.getElementById('rouletteAvatar');

        // Start high-speed roulette shuffling animation
        let shuffleCount = 0;
        rouletteInterval = setInterval(() => {
            const randomPick = participants[Math.floor(Math.random() * participants.length)];
            if (nameElem) nameElem.textContent = randomPick.username;
            if (avatarElem) {
                avatarElem.innerHTML = `<img src="${randomPick.userAvatar || 'https://api.dicebear.com/7.x/identicon/svg?seed=' + encodeURIComponent(randomPick.username)}" style="width:36px; height:36px; border-radius:50%; vertical-align:middle;">`;
            }
            shuffleCount++;
        }, 60);

        // Minimum dramatic spin time of 2.2 seconds combined with API call
        const [res] = await Promise.all([
            ApiClient.post('/api/giveaways/draw', payload),
            new Promise(resolve => setTimeout(resolve, 2200))
        ]);

        clearInterval(rouletteInterval);

        if (!res.success || !res.data) {
            throw new Error(res.message || 'Draw execution failed');
        }

        currentGiveaway = res.data;

        // Show the winning participant in the roulette box for a moment
        const topWinner = currentGiveaway.winners && currentGiveaway.winners.length > 0 ? currentGiveaway.winners[0] : null;
        if (topWinner && nameElem) {
            nameElem.textContent = topWinner.username;
        }

        await new Promise(resolve => setTimeout(resolve, 600));

        // Hide Roulette, Reveal Congratulatory Screen
        if (drawStage) drawStage.style.display = 'none';
        if (celebSection) {
            celebSection.style.display = 'block';
            celebSection.scrollIntoView({ behavior: 'smooth' });
        }

        // Trigger Confetti Celebration Canvas
        launchConfettiExplosion(60);

        // Render Winners Screen
        renderWinnerCelebration(currentGiveaway);

        // Reload giveaway history
        loadGiveawayHistory();

        showToast(`${currentGiveaway.winners.length} winner(s) drawn with SecureRandom`, 'success');

    } catch (err) {
        clearInterval(rouletteInterval);
        if (drawStage) drawStage.style.display = 'none';
        console.error('Giveaway draw error:', err);
        showToast(err.message || 'Failed to draw winners', 'error');
    } finally {
        if (btnDraw) btnDraw.disabled = false;
    }
}

function renderWinnerCelebration(giveaway) {
    const titleElem = document.getElementById('winnerGiveawayTitle');
    if (titleElem) titleElem.textContent = giveaway.title;

    const timeElem = document.getElementById('winnerTimestampDetails');
    if (timeElem) {
        const d = giveaway.drawnAt ? new Date(giveaway.drawnAt) : new Date();
        timeElem.textContent = `Drawn on ${d.toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' })} • Platform: ${giveaway.platform} • Verified by Java SecureRandom`;
    }

    // Render Primary Winners
    const primaryGrid = document.getElementById('primaryWinnersGrid');
    if (primaryGrid) {
        if (!giveaway.winners || giveaway.winners.length === 0) {
            primaryGrid.innerHTML = `<div style="color:#64748b;">No winners recorded</div>`;
        } else {
            primaryGrid.innerHTML = giveaway.winners.map((w, idx) => renderWinnerCard(w, idx + 1, false, giveaway.platform)).join('');
        }
    }

    // Render Backup Winners
    const backupContainer = document.getElementById('backupWinnersContainer');
    const backupGrid = document.getElementById('backupWinnersGrid');
    if (backupContainer && backupGrid) {
        if (giveaway.backupWinners && giveaway.backupWinners.length > 0) {
            backupContainer.style.display = 'block';
            backupGrid.innerHTML = giveaway.backupWinners.map((w, idx) => renderWinnerCard(w, idx + 1, true, giveaway.platform)).join('');
        } else {
            backupContainer.style.display = 'none';
        }
    }
}

function renderWinnerCard(winner, order, isBackup, platform) {
    const medalLabels = ['1st place', '2nd place', '3rd place'];
    const badgeLabel = isBackup
        ? `Alternate #${order}`
        : (order <= 3 ? medalLabels[order - 1] : `Winner #${order}`);

    const cardClass = isBackup ? 'winner-card backup-card' : 'winner-card primary-card';
    const avatar = winner.userAvatar || `https://api.dicebear.com/7.x/identicon/svg?seed=${encodeURIComponent(winner.username)}`;
    const platformPill = platform === 'YOUTUBE' 
        ? '<span class="platform-pill pill-youtube" style="font-size:10px;">YouTube</span>'
        : '<span class="platform-pill pill-instagram" style="font-size:10px;">Instagram</span>';

    const safeComment = escapeHtml(winner.commentText || '(No comment provided)');
    const safeUser = escapeHtml(formatHandle(winner.username));

    return `
        <div class="${cardClass}" id="winner-card-${winner.id}">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
                <span class="winner-card-badge ${order === 1 && !isBackup ? 'gold' : ''}">
                    ${badgeLabel}
                </span>
                ${platformPill}
            </div>

            <div style="display:flex; align-items:center; gap:12px; margin-bottom:12px;">
                <img src="${avatar}" alt="${safeUser}" style="width:40px; height:40px; border-radius:50%; object-fit:cover; border:1px solid var(--border-hairline); background:var(--surface-muted);">
                <div style="min-width:0; flex:1;">
                    <div style="font-size:14px; font-weight:600; color:var(--text-primary); display:flex; align-items:center; gap:6px;">
                        <span>${safeUser}</span>
                    </div>
                    <div class="mono-tag" style="font-size:11px;">
                        [CSPRNG-VERIFIED]
                    </div>
                </div>
            </div>

            <!-- Original Comment Box -->
            <div class="winner-comment-quote">
                &ldquo;${safeComment}&rdquo;
            </div>

            <!-- Action Toolbar (Reroll Button) -->
            <div style="display:flex; justify-content:space-between; align-items:center; border-top:1px solid var(--border-subtle); padding-top:10px;">
                <span class="mono-tag" style="font-size:11px;">#${winner.id}</span>
                <button type="button" class="btn btn-outline btn-sm" onclick="rerollWinner(${winner.id}, ${isBackup})" style="font-size:11px; font-weight:500; height:28px; padding:0 10px;" title="Redraw this single winner slot">
                    <span>Reroll</span>
                </button>
            </div>
        </div>
    `;
}

async function rerollWinner(winnerId, isBackup) {
    if (!currentGiveaway || !currentGiveaway.id) {
        showToast('No active giveaway to reroll', 'warning');
        return;
    }

    if (!confirm('Reroll this winner? A new random winner will be selected using Java SecureRandom.')) {
        return;
    }

    try {
        const res = await ApiClient.post(`/api/giveaways/${currentGiveaway.id}/reroll?winnerId=${winnerId}&isBackup=${isBackup}`);
        if (!res.success || !res.data) {
            throw new Error(res.message || 'Failed to reroll winner');
        }

        currentGiveaway = res.data;
        renderWinnerCelebration(currentGiveaway);
        loadGiveawayHistory();
        launchConfettiExplosion(50);
        showToast('Winner rerolled with Java SecureRandom!', 'success');

    } catch (err) {
        console.error('Reroll error:', err);
        showToast(err.message || 'Error rerolling winner', 'error');
    }
}

function exportCurrentGiveawayResult() {
    if (!currentGiveaway || !currentGiveaway.id) {
        showToast('No giveaway to export', 'warning');
        return;
    }
    downloadGiveawayResult(currentGiveaway.id);
}

function downloadGiveawayResult(giveawayId) {
    const token = ApiClient.getToken();
    const downloadUrl = `${API_BASE_URL}/api/giveaways/${giveawayId}/export-result?token=${encodeURIComponent(token || '')}`;

    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = 'result.html';
    document.body.appendChild(link);
    link.click();
    link.remove();
    showToast('Giveaway HTML & CSS result downloaded as result.html!', 'success');
}

function openGiveawayResultTab(giveawayId) {
    const token = ApiClient.getToken();
    const viewUrl = `${API_BASE_URL}/api/giveaways/${giveawayId}/result?token=${encodeURIComponent(token || '')}`;
    window.open(viewUrl, '_blank');
}

function exportCurrentGiveawayCsv() {
    exportCurrentGiveawayResult();
}

function downloadGiveawayCsv(giveawayId) {
    const token = ApiClient.getToken();
    const downloadUrl = `${API_BASE_URL}/api/giveaways/${giveawayId}/export-csv?token=${encodeURIComponent(token || '')}`;

    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = `Giveaway_Winners_${giveawayId}.csv`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    showToast('Giveaway winners CSV audit downloaded!', 'success');
}

function copyWinnerAnnouncement() {
    if (!currentGiveaway) {
        showToast('No giveaway data to copy', 'warning');
        return;
    }

    const g = currentGiveaway;
    const winnerLines = (g.winners || []).map((w, idx) => `${idx + 1}. ${formatHandle(w.username)} - "${w.commentText || ''}"`).join('\n');
    const backupLines = (g.backupWinners && g.backupWinners.length > 0)
        ? `\n🛡️ BACKUP WINNER(S):\n` + g.backupWinners.map((w, idx) => `${idx + 1}. ${formatHandle(w.username)}`).join('\n')
        : '';

    const text = `🎉 GIVEAWAY WINNER ANNOUNCEMENT! 🎉
Campaign: ${g.title}
Platform: ${g.platform}
Post URL: ${g.postUrl || 'N/A'}

🏆 PRIMARY WINNER(S):
${winnerLines}${backupLines}

Verified Fair Selection by SPEDO Social Media Analytics Engine using Java SecureRandom.
Congratulations to our winners! Please DM us to claim your prize! 🎁✨`;

    navigator.clipboard.writeText(text).then(() => {
        showToast('Winner announcement copied to clipboard!', 'success');
    }).catch(() => {
        showToast('Failed to copy to clipboard', 'warning');
    });
}

function resetForNewDraw() {
    const celebSection = document.getElementById('winnerCelebrationSection');
    if (celebSection) celebSection.style.display = 'none';

    const setupRow = document.getElementById('giveawaySetupRow');
    if (setupRow) setupRow.scrollIntoView({ behavior: 'smooth' });

    showToast('Ready for next draw!', 'info');
}

async function loadGiveawayHistory() {
    const tbody = document.getElementById('giveawayHistoryTbody');
    const countBadge = document.getElementById('giveawayHistoryCount');

    try {
        const res = await ApiClient.get('/api/giveaways', { page: 0, size: 50 });
        if (!res.success || !res.data) {
            if (tbody) tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding:30px; color:#94a3b8;">No past giveaway records</td></tr>`;
            return;
        }

        const items = res.data.content || [];
        if (countBadge) countBadge.textContent = res.data.totalElements || items.length;

        if (items.length === 0) {
            if (tbody) tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding:30px; color:#94a3b8;">No giveaway draws recorded yet. Complete your first draw above!</td></tr>`;
            return;
        }

        if (tbody) {
            tbody.innerHTML = items.map(item => {
                const dateStr = item.drawnAt ? new Date(item.drawnAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' }) : 'Recent';
                const pClass = item.platform === 'YOUTUBE' ? 'pill-youtube' : 'pill-instagram';
                const winnerNames = (item.winners || []).map(w => `<span style="background:#f4f4f5; color:#27272a; border:1px solid #e4e4e7; padding:2px 6px; border-radius:4px; font-weight:500; font-size:11px; margin-right:4px;">${escapeHtml(formatHandle(w.username))}</span>`).join('');

                return `
                    <tr>
                        <td>
                            <div style="font-weight:600; color:var(--text-primary); font-size:13px;">${escapeHtml(item.title || 'Giveaway')}</div>
                            <div style="font-size:11px; color:var(--text-muted); max-width:240px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">
                                ${escapeHtml(item.postCaption || item.postUrl || '')}
                            </div>
                        </td>
                        <td>
                            <span class="platform-pill ${pClass}" style="font-size:10px;">${item.platform}</span>
                        </td>
                        <td>
                            <span style="font-size:12px; color:var(--text-secondary);">${dateStr}</span>
                        </td>
                        <td>
                            <div class="tabular-stat" style="font-weight:600; color:var(--text-primary); font-size:12px;">${item.eligibleParticipantsCount} eligible</div>
                            <div class="tabular-stat" style="font-size:11px; color:var(--text-muted);">${item.totalCommentsFetched} total</div>
                        </td>
                        <td>
                            <div style="display:flex; flex-wrap:wrap; gap:4px; max-width:200px;">
                                ${winnerNames || '<span style="color:var(--text-muted);">None</span>'}
                            </div>
                        </td>
                        <td>
                            <div style="display:flex; gap:6px;">
                                <button type="button" class="btn btn-outline btn-sm" onclick="viewGiveawayDetails(${item.id})" style="font-size:11px; padding:3px 8px; font-weight:500;" title="View details">
                                    <span>View</span>
                                </button>
                                <button type="button" class="btn btn-outline btn-sm" onclick="downloadGiveawayResult(${item.id})" style="font-size:11px; padding:3px 8px; font-weight:600; color:var(--text-primary);" title="Export HTML result">
                                    <span>Result</span>
                                </button>
                                <button type="button" class="btn btn-outline btn-sm" onclick="deleteGiveawayRecord(${item.id})" style="font-size:11px; padding:3px 8px; color:#dc2626; border-color:#fecaca;" title="Delete record">
                                    <span>Delete</span>
                                </button>
                            </div>
                        </td>
                    </tr>
                `;
            }).join('');
        }

    } catch (e) {
        console.error('Error loading history:', e);
        if (tbody) tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding:30px; color:#ef4444;">Failed to load history: ${escapeHtml(e.message)}</td></tr>`;
    }
}

async function viewGiveawayDetails(giveawayId) {
    const modal = document.getElementById('historyModal');
    const body = document.getElementById('historyModalBody');
    const title = document.getElementById('historyModalTitle');
    const sub = document.getElementById('historyModalSub');
    const btnExport = document.getElementById('btnModalExportCsv');

    if (!modal || !body) return;

    try {
        body.innerHTML = '<div style="text-align:center; padding:40px;"><div class="spinner"></div></div>';
        modal.classList.add('active');

        const res = await ApiClient.get(`/api/giveaways/${giveawayId}`);
        if (!res.success || !res.data) throw new Error(res.message || 'Cannot retrieve giveaway');

        const g = res.data;
        if (title) title.textContent = g.title || 'Giveaway Audit';
        if (sub) {
            const d = g.drawnAt ? new Date(g.drawnAt).toLocaleString() : '';
            sub.textContent = `${g.platform} Draw • ${d} • Java SecureRandom Fair Selection`;
        }

        const btnExportResult = document.getElementById('btnModalExportResult');
        if (btnExportResult) {
            btnExportResult.onclick = () => downloadGiveawayResult(g.id);
        }

        if (btnExport) {
            btnExport.onclick = () => downloadGiveawayCsv(g.id);
        }

        const winnersHtml = (g.winners || []).map((w, idx) => `
            <div style="background:#fafafa; border:1px solid var(--border-hairline); border-radius:8px; padding:12px; margin-bottom:10px; display:flex; align-items:flex-start; gap:12px;">
                <img src="${w.userAvatar || 'https://api.dicebear.com/7.x/identicon/svg?seed=' + encodeURIComponent(w.username)}" style="width:36px; height:36px; border-radius:50%; object-fit:cover; border:1px solid var(--border-hairline); background:#fff;">
                <div style="flex:1;">
                    <div style="font-weight:600; font-size:13px; color:var(--text-primary); display:flex; align-items:center; gap:8px;">
                        <span>${idx === 0 ? '1st place' : (idx === 1 ? '2nd place' : (idx === 2 ? '3rd place' : 'Winner #' + (idx + 1)))}: ${escapeHtml(formatHandle(w.username))}</span>
                    </div>
                    <p style="font-size:12px; color:var(--text-secondary); margin:4px 0 0 0; line-height:1.4;">
                        &ldquo;${escapeHtml(w.commentText || '')}&rdquo;
                    </p>
                </div>
            </div>
        `).join('');

        const backupHtml = (g.backupWinners && g.backupWinners.length > 0) ? `
            <h4 style="font-size:12px; font-weight:600; text-transform:uppercase; letter-spacing:0.5px; color:var(--text-muted); margin:16px 0 10px 0;">
                Backup alternates
            </h4>
            ${g.backupWinners.map((w, idx) => `
                <div style="background:#fafafa; border:1px solid var(--border-hairline); border-radius:8px; padding:10px; margin-bottom:8px; display:flex; align-items:flex-start; gap:10px;">
                    <img src="${w.userAvatar || 'https://api.dicebear.com/7.x/identicon/svg?seed=' + encodeURIComponent(w.username)}" style="width:32px; height:32px; border-radius:50%; object-fit:cover; border:1px solid var(--border-hairline); background:#fff;">
                    <div style="flex:1;">
                        <div style="font-weight:500; font-size:12px; color:var(--text-secondary);">Alternate #${idx + 1}: ${escapeHtml(formatHandle(w.username))}</div>
                        <p style="font-size:11px; color:var(--text-muted); margin:2px 0 0 0;">&ldquo;${escapeHtml(w.commentText || '')}&rdquo;</p>
                    </div>
                </div>
            `).join('')}
        ` : '';

        body.innerHTML = `
            <div style="display:grid; grid-template-columns:1fr 1fr; gap:10px; background:#fafafa; border:1px solid var(--border-hairline); border-radius:8px; padding:12px; margin-bottom:16px; font-size:12px;">
                <div><span style="color:var(--text-muted);">Total comments:</span> <strong>${g.totalCommentsFetched}</strong></div>
                <div><span style="color:var(--text-muted);">Eligible entries:</span> <strong>${g.eligibleParticipantsCount}</strong></div>
                <div><span style="color:var(--text-muted);">Duplicate filter:</span> <strong>${g.filterDuplicates ? 'Active' : 'Disabled'}</strong></div>
                <div><span style="color:var(--text-muted);">Required keyword:</span> <strong>${g.requiredKeyword ? escapeHtml(g.requiredKeyword) : 'None'}</strong></div>
            </div>

            <h4 style="font-size:12px; font-weight:600; text-transform:uppercase; letter-spacing:0.5px; color:var(--text-secondary); margin-bottom:10px;">
                Primary winners (${g.winners ? g.winners.length : 0})
            </h4>
            ${winnersHtml || '<div style="color:var(--text-muted); font-size:12px;">No winners recorded</div>'}
            ${backupHtml}
        `;

    } catch (err) {
        body.innerHTML = `<div style="color:#ef4444; padding:20px; text-align:center;">Failed to load details: ${escapeHtml(err.message)}</div>`;
    }
}

function closeHistoryModal() {
    const modal = document.getElementById('historyModal');
    if (modal) modal.classList.remove('active');
}

async function deleteGiveawayRecord(giveawayId) {
    if (!confirm('Are you sure you want to delete this giveaway record from history?')) {
        return;
    }

    try {
        const res = await ApiClient.delete(`/api/giveaways/${giveawayId}`);
        if (res.success) {
            showToast('Giveaway record deleted successfully', 'success');
            loadGiveawayHistory();
        }
    } catch (e) {
        showToast(e.message || 'Failed to delete giveaway', 'error');
    }
}

// Fullscreen Confetti Particle Simulation (Subtle & Tasteful)
function launchConfettiExplosion(count = 70) {
    const canvas = document.getElementById('confettiCanvas');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;

    const colors = ['#2563eb', '#3b82f6', '#d97706', '#71717a', '#18181b', '#0284c7'];
    const particles = [];

    for (let i = 0; i < count; i++) {
        particles.push({
            x: canvas.width * 0.5 + (Math.random() - 0.5) * 260,
            y: canvas.height * 0.35 + (Math.random() - 0.5) * 160,
            vx: (Math.random() - 0.5) * 12,
            vy: (Math.random() - 0.5) * 12 - 4,
            size: Math.random() * 6 + 3,
            color: colors[Math.floor(Math.random() * colors.length)],
            rotation: Math.random() * 360,
            rotSpeed: (Math.random() - 0.5) * 8,
            gravity: 0.22,
            drag: 0.98,
            opacity: 1
        });
    }

    if (confettiAnimationId) {
        cancelAnimationFrame(confettiAnimationId);
    }

    const startTime = Date.now();
    const duration = 2500; // 2.5 seconds

    function renderConfetti() {
        const elapsed = Date.now() - startTime;
        if (elapsed > duration) {
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            return;
        }

        ctx.clearRect(0, 0, canvas.width, canvas.height);

        particles.forEach(p => {
            p.vx *= p.drag;
            p.vy *= p.drag;
            p.vy += p.gravity;
            p.x += p.vx;
            p.y += p.vy;
            p.rotation += p.rotSpeed;

            if (elapsed > duration - 800) {
                p.opacity = Math.max(0, (duration - elapsed) / 800);
            }

            ctx.save();
            ctx.translate(p.x, p.y);
            ctx.rotate((p.rotation * Math.PI) / 180);
            ctx.globalAlpha = p.opacity;
            ctx.fillStyle = p.color;
            ctx.fillRect(-p.size / 2, -p.size / 2, p.size, p.size * 0.6);
            ctx.restore();
        });

        confettiAnimationId = requestAnimationFrame(renderConfetti);
    }

    renderConfetti();
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function formatHandle(username) {
    if (!username) return '@anonymous';
    const trimmed = String(username).trim();
    return trimmed.startsWith('@') ? trimmed : '@' + trimmed;
}
