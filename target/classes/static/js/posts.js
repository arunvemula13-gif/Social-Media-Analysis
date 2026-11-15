/**
 * SPEDO Post Analytics & Content Intelligence Controller
 */

let currentPage = 0;
const pageSize = 9;
let currentPlatform = '';
let currentQuery = '';
let currentSort = 'engagementRate';
let currentSentiment = '';

document.addEventListener('DOMContentLoaded', () => {
    // Parse URL query params if present (e.g. from global search)
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('query')) {
        currentQuery = urlParams.get('query');
        document.getElementById('postSearchQuery').value = currentQuery;
    }
    if (urlParams.has('id')) {
        viewPostDetails(urlParams.get('id'));
    }

    loadPosts();
    loadSentimentSummary();
});

async function loadSentimentSummary() {
    try {
        const res = await ApiClient.get('/api/posts/sentiment-summary');
        if (res.success && res.data) {
            renderSentimentSummaryUI(res.data);
        }
    } catch (e) {
        console.warn('Could not load sentiment summary from backend:', e);
    }
}

function renderSentimentSummaryUI(data) {
    const totalElem = document.getElementById('statTotalPosts');
    if (totalElem) totalElem.textContent = data.totalPosts || 0;

    const platElem = document.getElementById('statPlatformBreakdown');
    if (platElem) {
        const yt = data.totalYouTubeVideos || 0;
        const other = data.totalOtherPosts || 0;
        platElem.innerHTML = `<strong>${yt}</strong> YouTube Videos · <strong>${other}</strong> Other Channels`;
    }

    const newsPctElem = document.getElementById('statNewsTonePct');
    if (newsPctElem) {
        newsPctElem.textContent = (data.newsPositivePercent || 0) + '% Positive';
    }

    const newsPosCountElem = document.getElementById('statNewsPosCount');
    if (newsPosCountElem) {
        newsPosCountElem.textContent = `${data.newsPositiveCount || 0} Positive Stories`;
    }

    const newsPosDetail = document.getElementById('statNewsPosDetail');
    if (newsPosDetail) newsPosDetail.textContent = `🟢 ${data.newsPositiveCount || 0} Pos`;

    const newsNeuDetail = document.getElementById('statNewsNeuDetail');
    if (newsNeuDetail) newsNeuDetail.textContent = `⚪ ${data.newsNeutralCount || 0} Neu`;

    const newsNegDetail = document.getElementById('statNewsNegDetail');
    if (newsNegDetail) newsNegDetail.textContent = `🔴 ${data.newsNegativeCount || 0} Alerts`;

    const commentsPosPct = document.getElementById('statCommentsPosPct');
    if (commentsPosPct) {
        commentsPosPct.textContent = `${data.commentsPositivePercent || 0}% Favorable`;
    }

    const totalCommentsElem = document.getElementById('statTotalComments');
    if (totalCommentsElem) {
        totalCommentsElem.textContent = formatNumber(data.totalComments || 0);
    }

    const totalImprElem = document.getElementById('statTotalImpressions');
    if (totalImprElem) {
        totalImprElem.textContent = formatNumber(data.totalImpressions || 0);
    }

    const likesSharesElem = document.getElementById('statLikesShares');
    if (likesSharesElem) {
        likesSharesElem.textContent = `${formatNumber(data.totalLikes || 0)} likes · ${data.vanityAlertCount || 0} vanity alerts`;
    }

    const verdictElem = document.getElementById('sentimentVerdictBadge');
    if (verdictElem) {
        const isNeg = (data.newsNegativeCount || 0) > (data.newsPositiveCount || 0);
        verdictElem.style.background = isNeg ? '#fef2f2' : '#f0fdf4';
        verdictElem.style.borderColor = isNeg ? '#fecaca' : '#bbf7d0';
        verdictElem.style.color = isNeg ? '#991b1b' : '#166534';
        verdictElem.innerHTML = `<strong>Verdict:</strong> ${data.overallVerdict || 'Balanced cross-platform coverage'}`;
    }

    const barPos = document.getElementById('barPos');
    const barNeu = document.getElementById('barNeu');
    const barNeg = document.getElementById('barNeg');
    const legPos = document.getElementById('legendPositive');
    const legNeu = document.getElementById('legendNeutral');
    const legNeg = document.getElementById('legendNegative');

    const posP = Number(data.newsPositivePercent) || 0;
    const neuP = Number(data.newsNeutralPercent) || 0;
    const negP = Number(data.newsNegativePercent) || 0;

    if (barPos) barPos.style.width = `${posP}%`;
    if (barNeu) barNeu.style.width = `${neuP}%`;
    if (barNeg) barNeg.style.width = `${negP}%`;

    if (legPos) legPos.textContent = `🟢 ${posP}% Positive News`;
    if (legNeu) legNeu.textContent = `⚪ ${neuP}% Neutral`;
    if (legNeg) legNeg.textContent = `🔴 ${negP}% Alerts/Negative`;
}

async function loadPosts() {
    const container = document.getElementById('postsContainer');
    container.innerHTML = '<div style="grid-column:1/-1; text-align:center; padding:40px; color:#94a3b8;">Loading published posts & analyzing sentiment...</div>';

    const params = {
        page: currentPage,
        size: pageSize,
        sortBy: currentSort,
        sortDir: 'desc'
    };

    if (currentPlatform) params.platform = currentPlatform;
    if (currentQuery) params.query = currentQuery;

    try {
        const res = await ApiClient.get('/api/posts', params);
        if (!res.success || !res.data) {
            throw new Error('Failed to load posts');
        }

        const pageData = res.data;
        let posts = pageData.content || [];

        // Apply client-side Sentiment Filter if chosen
        if (currentSentiment) {
            if (currentSentiment === 'POSITIVE') {
                posts = posts.filter(p => p.newsSentiment === 'POSITIVE' || (p.positivePercent && p.positivePercent >= 40));
            } else if (currentSentiment === 'NEGATIVE') {
                posts = posts.filter(p => p.newsSentiment === 'NEGATIVE' || (p.negativePercent && p.negativePercent >= 25));
            } else if (currentSentiment === 'NEUTRAL') {
                posts = posts.filter(p => p.newsSentiment === 'NEUTRAL' && (!p.positivePercent || p.positivePercent < 40) && (!p.negativePercent || p.negativePercent < 25));
            } else if (currentSentiment === 'VANITY') {
                posts = posts.filter(p => p.vanityAlert === true);
            }
        }

        if (posts.length === 0) {
            container.innerHTML = `
                <div style="grid-column:1/-1; text-align:center; padding:48px; background:#fff; border-radius:12px; border:1px dashed var(--border-color);">
                    <div style="font-size:16px; font-weight:700; color:var(--text-dark);">No Posts Matching Sentiment Filters</div>
                    <p style="color:var(--text-secondary); margin-top:6px;">Try switching sentiment filter to "All Sentiments" or check different keywords.</p>
                </div>
            `;
            updatePaginationUI(pageData);
            return;
        }

        container.innerHTML = posts.map(renderPostCard).join('');
        updatePaginationUI(pageData);
    } catch (err) {
        container.innerHTML = `<div style="grid-column:1/-1; color:var(--danger); text-align:center; padding:20px;">Error: ${err.message}</div>`;
    }
}

function renderPostCard(post) {
    const pClass = `pill-${post.platform.toLowerCase()}`;
    const mediaHtml = post.mediaUrl ? `
        <div style="height:170px; width:100%; border-radius:8px; overflow:hidden; background:#f1f5f9; margin-bottom:12px; position:relative;">
            <img src="${post.mediaUrl}" alt="Post media" style="width:100%; height:100%; object-fit:cover;">
            ${post.platform === 'YOUTUBE' && post.postUrl ? `
                <a href="${post.postUrl}" target="_blank" rel="noopener noreferrer" style="position:absolute; bottom:8px; right:8px; background:rgba(0,0,0,0.8); color:#fff; font-size:11px; font-weight:700; padding:3px 8px; border-radius:4px; text-decoration:none; display:flex; align-items:center; gap:4px;">
                    ▶️ Watch Video
                </a>
            ` : ''}
        </div>
    ` : '';

    const hasVanityAlert = post.vanityAlert;
    const posPercent = post.positivePercent || 0;
    const negPercent = post.negativePercent || 0;
    const neuPercent = Math.max(0, 100 - posPercent - negPercent);

    // News Headline NLP Sentiment
    const newsSent = post.newsSentiment || 'NEUTRAL';
    const newsScore = post.newsSentimentScore != null ? Number(post.newsSentimentScore).toFixed(2) : '0.00';
    let newsBadgeColor = '#475569';
    let newsBadgeBg = '#f1f5f9';
    let newsIcon = '⚪';
    let newsLabel = 'Neutral / Info';

    if (newsSent === 'POSITIVE') {
        newsBadgeColor = '#15803d';
        newsBadgeBg = '#dcfce7';
        newsIcon = '🟢';
        newsLabel = 'Positive News';
    } else if (newsSent === 'NEGATIVE') {
        newsBadgeColor = '#b91c1c';
        newsBadgeBg = '#fee2e2';
        newsIcon = '🔴';
        newsLabel = 'Alert / Critical';
    }

    return `
        <div class="card" style="display:flex; flex-direction:column; justify-content:space-between; position:relative; border:${hasVanityAlert ? '2px solid #ef4444' : '1px solid var(--border-color)'};">
            <div>
                <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:10px;">
                    <div style="display:flex; align-items:center; gap:8px;">
                        <span class="platform-pill ${pClass}">${post.platform}</span>
                        <span style="font-size:11px; font-weight:700; background:${newsBadgeBg}; color:${newsBadgeColor}; padding:2px 8px; border-radius:12px;">
                            ${newsIcon} News: ${newsSent}
                        </span>
                    </div>
                    <span style="font-size:11px; color:#94a3b8;">${formatDate(post.publishedAt)}</span>
                </div>

                ${hasVanityAlert ? `
                    <div style="background:#fef2f2; border:1px solid #fecaca; border-radius:6px; padding:6px 10px; margin-bottom:10px; font-size:11px; color:#b91c1c; font-weight:700; display:flex; align-items:center; gap:6px;">
                        <span>⚠️ Vanity Alert</span>
                        <span style="font-weight:600; color:#991b1b; font-size:11px;">High Likes, but ${negPercent}% Complaints!</span>
                    </div>
                ` : ''}

                ${mediaHtml}

                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:4px;">
                    <span style="font-size:11px; color:#64748b; font-weight:700;">
                        ${post.accountHandle || post.accountName || 'Channel'}
                    </span>
                    ${post.topNewsKeyword ? `
                        <span style="font-size:10px; color:#2563eb; background:#eff6ff; padding:1px 6px; border-radius:4px; font-weight:600;">
                            Topic: ${post.topNewsKeyword}
                        </span>
                    ` : ''}
                </div>

                <p style="font-size:13px; color:var(--text-dark); line-height:1.45; display:-webkit-box; -webkit-line-clamp:3; -webkit-box-orient:vertical; overflow:hidden; margin-bottom:14px; font-weight:500;">
                    ${post.caption || 'No caption available.'}
                </p>
            </div>

            <div>
                <!-- Dual Sentiment Analysis Block: News Tone vs Viewer Comments -->
                <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:10px; margin-bottom:10px;">
                    <div style="display:flex; justify-content:space-between; align-items:center; font-size:11px; margin-bottom:6px;">
                        <span style="font-weight:700; color:#334155;">📰 News Headline Tone:</span>
                        <span style="font-weight:700; color:${newsBadgeColor};">${newsLabel} (${newsScore})</span>
                    </div>
                    
                    <div style="display:flex; justify-content:space-between; align-items:center; font-size:11px; margin-bottom:4px; font-weight:600;">
                        <span style="color:#334155;">💬 Viewer Comments:</span>
                        <div>
                            <span style="color:#16a34a;">🟢 ${posPercent}%</span>
                            <span style="color:#64748b; margin:0 4px;">⚪ ${neuPercent.toFixed(0)}%</span>
                            <span style="color:#dc2626;">🔴 ${negPercent}%</span>
                        </div>
                    </div>
                    <div style="display:flex; height:6px; border-radius:3px; overflow:hidden; background:#e2e8f0;">
                        <div style="width:${posPercent}%; background:#22c55e;"></div>
                        <div style="width:${neuPercent}%; background:#cbd5e1;"></div>
                        <div style="width:${negPercent}%; background:#ef4444;"></div>
                    </div>
                </div>

                <div style="background:var(--bg-muted); border-radius:8px; padding:8px 10px; display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; font-size:12px;">
                    <span>❤️ ${formatNumber(post.likesCount)}</span>
                    <span>💬 ${formatNumber(post.commentsCount)}</span>
                    <span>👁️ ${formatNumber(post.impressionsCount || post.reachCount || 0)}</span>
                    <span style="font-weight:700; color:var(--primary-violet); background:rgba(114,9,183,0.1); padding:2px 6px; border-radius:4px;">
                        ${(post.engagementRate || 0).toFixed(2)}%
                    </span>
                </div>

                <div style="display:flex; justify-content:space-between; align-items:center; border-top:1px solid var(--border-color); padding-top:10px; gap:8px;">
                    <button class="btn btn-outline btn-sm" style="flex:1; font-size:11px; padding:5px 6px; border-color:${hasVanityAlert ? '#ef4444' : '#4361ee'}; color:${hasVanityAlert ? '#ef4444' : '#4361ee'};" onclick="openSentimentModal(${post.id})">
                        🧠 Inspect NLP & Comments
                    </button>
                    ${post.postUrl ? `
                        <a href="${post.postUrl}" target="_blank" rel="noopener noreferrer" class="btn btn-outline btn-sm" style="font-size:11px; padding:5px 8px; text-decoration:none; display:inline-flex; align-items:center; gap:4px;">
                            <span>Source ↗</span>
                        </a>
                    ` : `
                        <button class="btn btn-outline btn-sm" style="font-size:11px; padding:5px 8px;" onclick="viewPostDetails(${post.id})">
                            Details
                        </button>
                    `}
                </div>
            </div>
        </div>
    `;
}

function updatePaginationUI(pageData) {
    const info = document.getElementById('paginationInfo');
    const prevBtn = document.getElementById('prevPageBtn');
    const nextBtn = document.getElementById('nextPageBtn');

    const total = pageData.totalElements || 0;
    const start = total === 0 ? 0 : (currentPage * pageSize) + 1;
    const end = Math.min((currentPage + 1) * pageSize, total);

    info.textContent = `Showing ${start}-${end} of ${total} posts`;
    prevBtn.disabled = currentPage <= 0;
    nextBtn.disabled = pageData.last;
}

function changePage(delta) {
    currentPage += delta;
    if (currentPage < 0) currentPage = 0;
    loadPosts();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function applyFilters() {
    currentQuery = document.getElementById('postSearchQuery').value.trim();
    currentPlatform = document.getElementById('platformFilter').value;
    currentSort = document.getElementById('sortFilter').value;
    const sentSelect = document.getElementById('sentimentFilter');
    currentSentiment = sentSelect ? sentSelect.value : '';
    currentPage = 0;
    loadPosts();
}

async function viewPostDetails(id) {
    const modal = document.getElementById('postModal');
    const content = document.getElementById('postModalContent');
    content.innerHTML = '<div style="text-align:center; padding:30px; color:#94a3b8;">Loading post telemetry...</div>';
    modal.classList.add('open');

    try {
        const res = await ApiClient.get(`/api/posts/${id}`);
        if (!res.success || !res.data) throw new Error('Post not found');

        const post = res.data;
        const pClass = `pill-${post.platform.toLowerCase()}`;

        content.innerHTML = `
            <div style="display:flex; flex-direction:column; gap:18px;">
                <div style="display:flex; align-items:center; justify-content:space-between;">
                    <div style="display:flex; align-items:center; gap:10px;">
                        <span class="platform-pill ${pClass}">${post.platform}</span>
                        <span style="font-weight:600; font-size:13px;">${post.accountName || ''} (${post.accountHandle || ''})</span>
                    </div>
                    <span style="font-size:12px; color:#94a3b8;">Published: ${formatDate(post.publishedAt)}</span>
                </div>

                ${post.mediaUrl ? `
                    <div style="max-height:280px; width:100%; border-radius:10px; overflow:hidden; background:#0b132b;">
                        <img src="${post.mediaUrl}" alt="Post media" style="width:100%; max-height:280px; object-fit:contain;">
                    </div>
                ` : ''}

                <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:14px; font-size:14px; line-height:1.5;">
                    ${post.caption || 'No caption available.'}
                </div>

                <div style="display:grid; grid-template-columns: repeat(4, 1fr); gap:12px;">
                    <div style="background:#fff; border:1px solid #e2e8f0; border-radius:8px; padding:12px; text-align:center;">
                        <div style="font-size:11px; color:#64748b; text-transform:uppercase;">Likes</div>
                        <div style="font-size:18px; font-weight:800; color:var(--text-dark); margin-top:4px;">${formatNumber(post.likesCount)}</div>
                    </div>
                    <div style="background:#fff; border:1px solid #e2e8f0; border-radius:8px; padding:12px; text-align:center;">
                        <div style="font-size:11px; color:#64748b; text-transform:uppercase;">Comments</div>
                        <div style="font-size:18px; font-weight:800; color:var(--text-dark); margin-top:4px;">${formatNumber(post.commentsCount)}</div>
                    </div>
                    <div style="background:#fff; border:1px solid #e2e8f0; border-radius:8px; padding:12px; text-align:center;">
                        <div style="font-size:11px; color:#64748b; text-transform:uppercase;">Shares</div>
                        <div style="font-size:18px; font-weight:800; color:var(--text-dark); margin-top:4px;">${formatNumber(post.sharesCount)}</div>
                    </div>
                    <div style="background:#f5f3ff; border:1px solid #ddd6fe; border-radius:8px; padding:12px; text-align:center;">
                        <div style="font-size:11px; color:#7209b7; text-transform:uppercase; font-weight:700;">Engagement</div>
                        <div style="font-size:18px; font-weight:800; color:#7209b7; margin-top:4px;">${(post.engagementRate || 0).toFixed(2)}%</div>
                    </div>
                </div>

                <div style="display:grid; grid-template-columns: 1fr 1fr; gap:12px; font-size:13px; color:#475569;">
                    <div style="background:#f1f5f9; padding:10px 14px; border-radius:6px;">
                        <strong>Total Impressions:</strong> ${formatNumber(post.impressionsCount)}
                    </div>
                    <div style="background:#f1f5f9; padding:10px 14px; border-radius:6px;">
                        <strong>Unique Reach:</strong> ${formatNumber(post.reachCount)}
                    </div>
                </div>

                ${post.postUrl ? `
                    <div style="text-align:right;">
                        <a href="${post.postUrl}" target="_blank" rel="noopener noreferrer" class="btn btn-outline btn-sm">
                            View on ${post.platform} ↗
                        </a>
                    </div>
                ` : ''}
            </div>
        `;
    } catch (err) {
        content.innerHTML = `<div style="color:var(--danger); text-align:center; padding:20px;">Error loading post: ${err.message}</div>`;
    }
}

function closePostModal() {
    document.getElementById('postModal').classList.remove('open');
}

let activeSentimentPostId = null;
let currentModalComments = [];
let currentCommentFilter = 'ALL';
let currentCommentSearchText = '';

async function openSentimentModal(id) {
    activeSentimentPostId = id;
    const modal = document.getElementById('sentimentModal');
    const content = document.getElementById('sentimentModalContent');
    const subtitle = document.getElementById('sentimentModalSubtitle');
    const footerStats = document.getElementById('sentimentModalFooterStats');
    const syncBtn = document.getElementById('btnSyncModalComments');

    if (subtitle) subtitle.textContent = 'Processing total comments through Java NLP Engine...';
    if (footerStats) footerStats.textContent = 'Retrieving comments...';
    if (syncBtn) {
        syncBtn.disabled = false;
        syncBtn.innerHTML = '<span>🔄 Sync All Real Comments</span>';
    }

    content.innerHTML = '<div style="text-align:center; padding:40px; color:#64748b;"><div style="font-size:26px; margin-bottom:8px;">🧠</div><div>Processing full comments through Java NLP Engine...</div></div>';
    modal.classList.add('open');

    try {
        const res = await ApiClient.get(`/api/posts/${id}/sentiment`);
        if (!res.success || !res.data) throw new Error('Failed to evaluate comment sentiment');

        const s = res.data;
        renderSentimentModalContent(s);
    } catch (err) {
        content.innerHTML = `<div style="color:var(--danger); text-align:center; padding:20px;">Failed to analyze comments: ${err.message}</div>`;
    }
}

async function syncCurrentPostComments() {
    if (!activeSentimentPostId) return;
    const syncBtn = document.getElementById('btnSyncModalComments');
    const originalHtml = syncBtn ? syncBtn.innerHTML : '';
    if (syncBtn) {
        syncBtn.disabled = true;
        syncBtn.innerHTML = '<span>⏳ Ingesting all comments...</span>';
    }

    try {
        const res = await ApiClient.post(`/api/posts/${activeSentimentPostId}/sync-comments`);
        if (res.success && res.data) {
            showToast(`Ingested & analyzed ${res.data.totalComments} comments with live Java NLP!`, 'success');
            renderSentimentModalContent(res.data);
            await loadPosts(); // Refresh post card metrics
        } else {
            throw new Error(res.message || 'Sync failed');
        }
    } catch (err) {
        showToast('Comment sync failed: ' + err.message, 'error');
    } finally {
        if (syncBtn) {
            syncBtn.disabled = false;
            syncBtn.innerHTML = originalHtml || '<span>🔄 Sync All Real Comments</span>';
        }
    }
}

function filterComments(filterType) {
    currentCommentFilter = filterType;
    document.querySelectorAll('.comment-filter-btn').forEach(btn => {
        if (btn.dataset.filter === filterType) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
    renderCommentListUI();
}

function handleCommentSearch(text) {
    currentCommentSearchText = (text || '').trim().toLowerCase();
    renderCommentListUI();
}

function renderCommentListUI() {
    const listContainer = document.getElementById('commentFeedList');
    const countBadge = document.getElementById('filteredCommentsBadge');
    if (!listContainer) return;

    let filtered = currentModalComments.slice();

    if (currentCommentFilter !== 'ALL') {
        filtered = filtered.filter(c => (c.sentiment || '').toUpperCase() === currentCommentFilter);
    }

    if (currentCommentSearchText) {
        filtered = filtered.filter(c => 
            (c.commentText && c.commentText.toLowerCase().includes(currentCommentSearchText)) ||
            (c.author && c.author.toLowerCase().includes(currentCommentSearchText)) ||
            (c.detectedKeyword && c.detectedKeyword.toLowerCase().includes(currentCommentSearchText))
        );
    }

    if (countBadge) {
        countBadge.textContent = `Showing ${filtered.length} of ${currentModalComments.length} comments`;
    }

    if (filtered.length === 0) {
        listContainer.innerHTML = '<div style="text-align:center; padding:30px; color:#94a3b8; font-size:13px;">No comments match this filter or search query.</div>';
        return;
    }

    listContainer.innerHTML = filtered.map(c => {
        const isNeg = c.sentiment === 'NEGATIVE';
        const isPos = c.sentiment === 'POSITIVE';
        const badgeBg = isNeg ? '#fee2e2' : (isPos ? '#dcfce7' : '#f1f5f9');
        const badgeColor = isNeg ? '#b91c1c' : (isPos ? '#15803d' : '#475569');
        const badgeIcon = isNeg ? '🔴' : (isPos ? '🟢' : '⚪');
        const scoreLabel = (c.sentimentScore >= 0 ? '+' : '') + c.sentimentScore;

        return `
            <div style="background:#fff; border:1px solid #e2e8f0; border-radius:8px; padding:12px 14px; margin-bottom:10px; box-shadow:0 1px 2px rgba(0,0,0,0.02);">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px; flex-wrap:wrap; gap:6px;">
                    <div style="display:flex; align-items:center; gap:8px;">
                        <img src="${c.authorAvatar || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100'}" alt="${c.author || 'Viewer'}" style="width:24px; height:24px; border-radius:50%; object-fit:cover;" onerror="this.src='https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100'">
                        <span style="font-weight:700; font-size:13px; color:#1e293b;">${c.author || 'YouTube Viewer'}</span>
                        <span style="font-size:11px; color:#94a3b8;">${formatDate(c.commentedAt)}</span>
                    </div>
                    <span style="background:${badgeBg}; color:${badgeColor}; font-size:11px; font-weight:700; padding:2px 8px; border-radius:12px;">
                        ${badgeIcon} ${c.sentiment} (${scoreLabel})
                    </span>
                </div>
                <div style="font-size:13px; color:#334155; line-height:1.5; word-break:break-word;">
                    ${c.commentText || ''}
                </div>
                ${c.detectedKeyword ? `<div style="font-size:10px; color:#64748b; margin-top:6px; display:inline-block; background:#f8fafc; border:1px solid #e2e8f0; padding:1px 6px; border-radius:4px;">Keyword trigger: <code>${c.detectedKeyword}</code></div>` : ''}
            </div>
        `;
    }).join('');
}

function renderSentimentModalContent(s) {
    const content = document.getElementById('sentimentModalContent');
    const subtitle = document.getElementById('sentimentModalSubtitle');
    const footerStats = document.getElementById('sentimentModalFooterStats');
    const isVanity = s.vanityAlert;

    currentModalComments = s.comments || [];
    currentCommentFilter = 'ALL';
    currentCommentSearchText = '';

    const cleanTitle = (s.postCaption || 'Social Post').replace(/&amp;/g, '&').replace(/&#39;/g, "'");
    if (subtitle) {
        subtitle.innerHTML = `<span style="font-weight:600; color:#334155;">${cleanTitle.length > 70 ? cleanTitle.substring(0, 70) + '...' : cleanTitle}</span> • <span class="platform-pill pill-${(s.platform || 'youtube').toLowerCase()}" style="font-size:10px; padding:1px 6px;">${s.platform || 'YOUTUBE'}</span>`;
    }

    if (footerStats) {
        footerStats.innerHTML = `Total Video Comments: <strong>${s.realPostCommentsCount || s.totalComments}</strong> | Evaluated by Java NLP: <strong>${s.totalComments}</strong> (${s.totalComments > 0 ? '100% of available threads' : '0%'})`;
    }

    const issueTagsHtml = (s.topIssues && s.topIssues.length > 0)
        ? s.topIssues.map(issue => `<span style="background:#fee2e2; color:#991b1b; padding:3px 8px; border-radius:4px; font-size:11px; font-weight:700;">🚨 ${issue}</span>`).join(' ')
        : '<span style="color:#64748b; font-size:12px;">No major critical complaints detected.</span>';

    const praiseTagsHtml = (s.topPraises && s.topPraises.length > 0)
        ? s.topPraises.map(praise => `<span style="background:#dcfce7; color:#166534; padding:3px 8px; border-radius:4px; font-size:11px; font-weight:700;">⭐ ${praise}</span>`).join(' ')
        : '<span style="color:#64748b; font-size:12px;">Standard feedback levels.</span>';

    content.innerHTML = `
        <div style="display:flex; flex-direction:column; gap:20px;">
            <!-- Core Problem & Solution Alert Box -->
            <div style="background:${isVanity ? '#fef2f2' : '#f0fdf4'}; border:2px solid ${isVanity ? '#ef4444' : '#22c55e'}; border-radius:10px; padding:16px 20px;">
                <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:8px; flex-wrap:wrap; gap:8px;">
                    <span style="font-size:12px; font-weight:800; text-transform:uppercase; letter-spacing:0.5px; color:${isVanity ? '#b91c1c' : '#15803d'};">
                        ${isVanity ? '⚠️ The Vanity Metrics Problem Detected' : '✅ Customer Sentiment Verified'}
                    </span>
                    <span style="font-size:11px; font-weight:700; color:#475569; background:#fff; padding:2px 8px; border-radius:6px; border:1px solid #cbd5e1;">
                        Post Likes: ${formatNumber(s.likesCount)} | Total Comments: ${s.totalComments}
                    </span>
                </div>
                <div style="font-size:15px; font-weight:700; color:${isVanity ? '#991b1b' : '#166534'}; line-height:1.5; margin-bottom:8px;">
                    "${s.sentimentVerdict}"
                </div>
                <p style="font-size:12px; color:${isVanity ? '#7f1d1d' : '#14532d'}; margin:0; line-height:1.5;">
                    ${isVanity 
                        ? 'Getting thousands of likes on a post looks great on paper (a vanity metric), but when customer comments are dominated by complaints, product satisfaction is in jeopardy. Java text-processing isolates complaints before they damage retention.'
                        : 'Customer feedback indicates genuine brand affinity and positive organic reception.'}
                </p>
            </div>

            <!-- Sentiment Quality Breakdown Bar -->
            <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:16px;">
                <div style="display:flex; justify-content:space-between; font-size:12px; font-weight:700; margin-bottom:8px; flex-wrap:wrap; gap:8px;">
                    <span style="color:#16a34a;">🟢 Positive: ${s.positivePercent}% (${s.positiveCount})</span>
                    <span style="color:#64748b;">⚪ Neutral: ${s.neutralPercent}% (${s.neutralCount})</span>
                    <span style="color:#dc2626;">🔴 Complaints: ${s.negativePercent}% (${s.negativeCount})</span>
                </div>
                <div style="display:flex; height:10px; border-radius:5px; overflow:hidden; background:#e2e8f0;">
                    <div style="width:${s.positivePercent}%; background:#22c55e;"></div>
                    <div style="width:${s.neutralPercent}%; background:#94a3b8;"></div>
                    <div style="width:${s.negativePercent}%; background:#ef4444;"></div>
                </div>
            </div>

            <!-- Extracted Pain Points & Praises -->
            <div style="display:grid; grid-template-columns: 1fr 1fr; gap:16px;">
                <div style="background:#fff; border:1px solid #fecaca; border-radius:8px; padding:12px;">
                    <div style="font-size:12px; font-weight:700; color:#b91c1c; margin-bottom:8px;">🚨 Extracted Issues / Complaints</div>
                    <div style="display:flex; flex-wrap:wrap; gap:6px;">${issueTagsHtml}</div>
                </div>
                <div style="background:#fff; border:1px solid #bbf7d0; border-radius:8px; padding:12px;">
                    <div style="font-size:12px; font-weight:700; color:#15803d; margin-bottom:8px;">⭐ Extracted Praises</div>
                    <div style="display:flex; flex-wrap:wrap; gap:6px;">${praiseTagsHtml}</div>
                </div>
            </div>

            <!-- Comment Feed Header & Filter Bar -->
            <div>
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; flex-wrap:wrap; gap:10px;">
                    <div style="display:flex; align-items:center; gap:8px;">
                        <h4 style="font-size:14px; font-weight:700; margin:0; color:#1e293b;">
                            Indexed Audience Comments (${s.totalComments})
                        </h4>
                        <span id="filteredCommentsBadge" style="font-size:11px; color:#64748b; background:#f1f5f9; padding:2px 8px; border-radius:12px;">
                            Showing all ${s.totalComments}
                        </span>
                    </div>

                    <div style="flex:1; min-width:200px; max-width:260px;">
                        <input type="text" id="commentSearchInput" class="form-control" placeholder="Search comments or users..." style="margin-bottom:0; font-size:12px; padding:6px 10px;" oninput="handleCommentSearch(this.value)">
                    </div>
                </div>

                <!-- Filter Tabs -->
                <div style="display:flex; gap:8px; margin-bottom:12px; flex-wrap:wrap;">
                    <button type="button" class="comment-filter-btn active" data-filter="ALL" onclick="filterComments('ALL')">All (${s.totalComments})</button>
                    <button type="button" class="comment-filter-btn" data-filter="POSITIVE" onclick="filterComments('POSITIVE')">🟢 Positive (${s.positiveCount})</button>
                    <button type="button" class="comment-filter-btn" data-filter="NEUTRAL" onclick="filterComments('NEUTRAL')">⚪ Neutral (${s.neutralCount})</button>
                    <button type="button" class="comment-filter-btn" data-filter="NEGATIVE" onclick="filterComments('NEGATIVE')">🔴 Complaints (${s.negativeCount})</button>
                </div>

                <!-- Scrollable comments list with expanded 480px height -->
                <div id="commentFeedList" style="max-height:480px; overflow-y:auto; padding:12px; background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px;">
                    <!-- Dynamically populated by renderCommentListUI() -->
                </div>
            </div>

            <!-- Interactive Live Comment NLP Tester -->
            <div style="background:#f1f5f9; border-radius:8px; padding:14px; border:1px dashed #cbd5e1;">
                <div style="font-size:12px; font-weight:700; color:#1e293b; margin-bottom:6px;">
                    🧪 Test Live Java NLP Text-Processing:
                </div>
                <p style="font-size:11px; color:#64748b; margin-bottom:8px;">
                    Type any sentence or emojis (e.g. <em>"🔥 Absolutely loved the analysis, brilliant explanation!"</em> or <em>"App crashed again, worst update ever!"</em>) to observe live Java tokenization, negation handling, and sentiment scoring.
                </p>
                <form onsubmit="handleLiveCommentSubmit(event)" style="display:flex; gap:8px;">
                    <input type="text" id="liveCommentInput" class="form-control" placeholder="Write a comment to test Java text analyzer..." style="margin-bottom:0; font-size:13px;" required>
                    <button type="submit" id="liveCommentBtn" class="btn btn-primary btn-sm" style="white-space:nowrap;">
                        Analyze Comment
                    </button>
                </form>
            </div>
        </div>
    `;

    renderCommentListUI();
}

async function handleLiveCommentSubmit(e) {
    e.preventDefault();
    const input = document.getElementById('liveCommentInput');
    const btn = document.getElementById('liveCommentBtn');
    const text = input.value.trim();
    if (!text || !activeSentimentPostId) return;

    btn.disabled = true;
    btn.textContent = 'Analyzing...';

    try {
        const res = await ApiClient.post(`/api/posts/${activeSentimentPostId}/comments`, {
            commentText: text,
            author: 'You (Live Tester)'
        });

        if (res.success) {
            showToast(`Analyzed: Classified as ${res.data.sentiment} (${res.data.sentimentScore})`, 'success');
            // Re-fetch post sentiment summary
            const refreshRes = await ApiClient.get(`/api/posts/${activeSentimentPostId}/sentiment`);
            if (refreshRes.success) {
                renderSentimentModalContent(refreshRes.data);
            }
            await loadPosts(); // Refresh post card metrics
        }
    } catch (err) {
        showToast('Analysis error: ' + err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Analyze Comment';
    }
}

function closeSentimentModal() {
    activeSentimentPostId = null;
    document.getElementById('sentimentModal').classList.remove('open');
}

async function openCreatePostModal() {
    try {
        const res = await ApiClient.get('/api/social-accounts');
        const select = document.getElementById('postAccountId');
        select.innerHTML = '';
        if (res.success && res.data && res.data.length > 0) {
            res.data.forEach((acc, idx) => {
                const opt = document.createElement('option');
                opt.value = acc.id;
                opt.textContent = `${acc.platform} — ${acc.accountName} (${acc.handle})`;
                opt.dataset.platform = acc.platform;
                if (idx === 0) opt.selected = true;
                select.appendChild(opt);
            });
            const connectOpt = document.createElement('option');
            connectOpt.value = '__connect_new__';
            connectOpt.textContent = '➕ Connect another account (Instagram, Facebook, X, etc.)...';
            select.appendChild(connectOpt);

            // Synchronize post type with first selected account
            handleAccountSelectChange(select);
        } else {
            select.innerHTML = '<option value="">No connected accounts found. Please connect an account first!</option>';
        }
        document.getElementById('createPostModal').classList.add('open');
    } catch (err) {
        showToast('Error loading accounts: ' + err.message, 'error');
    }
}

function handleAccountSelectChange(selectElem) {
    if (!selectElem) return;
    if (selectElem.value === '__connect_new__') {
        window.location.href = 'social-accounts.html';
        return;
    }
    const selectedOpt = selectElem.options[selectElem.selectedIndex];
    const platform = selectedOpt ? selectedOpt.dataset.platform : '';
    const typeSelect = document.getElementById('postTypeSelect');
    const urlInput = document.getElementById('postUrlInput');

    if (typeSelect && platform === 'YOUTUBE') {
        typeSelect.value = 'VIDEO';
        if (urlInput) urlInput.placeholder = 'https://youtube.com/watch?v=... or shorts/...';
    } else if (typeSelect && platform === 'TWITTER') {
        typeSelect.value = 'TEXT';
        if (urlInput) urlInput.placeholder = 'https://x.com/username/status/...';
    } else if (typeSelect && (platform === 'INSTAGRAM' || platform === 'FACEBOOK')) {
        typeSelect.value = 'IMAGE';
        if (urlInput) urlInput.placeholder = 'https://instagram.com/p/...';
    } else if (typeSelect && platform === 'LINKEDIN') {
        typeSelect.value = 'TEXT';
        if (urlInput) urlInput.placeholder = 'https://linkedin.com/feed/update/...';
    }
}

function closeCreatePostModal() {
    document.getElementById('createPostModal').classList.remove('open');
}

async function handleCreatePostSubmit(e) {
    e.preventDefault();
    const accountId = document.getElementById('postAccountId').value;
    if (!accountId || accountId === '__connect_new__') {
        showToast('Please select a valid connected account or link a new one!', 'error');
        return;
    }

    const caption = document.getElementById('postCaption').value.trim();
    const postType = document.getElementById('postTypeSelect').value;
    const postUrl = document.getElementById('postUrlInput').value.trim();
    const likes = parseInt(document.getElementById('postLikes').value) || 0;
    const comments = parseInt(document.getElementById('postCommentsCount').value) || 0;
    const shares = parseInt(document.getElementById('postShares').value) || 0;
    const impressions = parseInt(document.getElementById('postImpressions').value) || 0;
    const reach = parseInt(document.getElementById('postReach').value) || 0;

    const rawComments = document.getElementById('postCustomerComments').value.trim();
    const commentList = rawComments ? rawComments.split('\n').map(c => c.trim()).filter(c => c.length > 0) : [];

    const btn = document.getElementById('savePostBtn');
    btn.disabled = true;
    btn.textContent = 'Saving & Running NLP...';

    try {
        const res = await ApiClient.post('/api/posts', {
            socialAccountId: parseInt(accountId),
            caption: caption,
            postType: postType,
            postUrl: postUrl,
            likesCount: likes,
            commentsCount: Math.max(comments, commentList.length),
            sharesCount: shares,
            impressionsCount: impressions,
            reachCount: reach,
            comments: commentList
        });

        if (res.success) {
            showToast('Real post saved and customer comments analyzed!', 'success');
            closeCreatePostModal();
            document.getElementById('createPostForm').reset();
            await loadPosts();
        }
    } catch (err) {
        showToast('Failed to save post: ' + err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Save Real Post & Run NLP';
    }
}
