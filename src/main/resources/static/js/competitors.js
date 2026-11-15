/**
 * SPEDO Competitor Intelligence & Cross-Platform Benchmarking Controller
 */

let competitorChartInstance = null;
let currentComparisonData = null;

document.addEventListener('DOMContentLoaded', () => {
    initCompetitorsPage();
});

async function initCompetitorsPage() {
    const user = ApiClient.getUser();
    const emailBadge = document.getElementById('registeredEmailBadge');
    if (user && user.email && emailBadge) {
        emailBadge.textContent = user.email;
    } else if (emailBadge) {
        emailBadge.textContent = 'arunvemula92@gmail.com';
    }

    // Populate default channel inputs & suggestions from connected accounts
    await setupChannelInputs();

    await Promise.all([
        loadPlatformComparison(),
        reloadComparison(),
        loadCompetitorProfiles()
    ]);
}

async function setupChannelInputs() {
    const channelA = document.getElementById('channelAInput');
    const channelB = document.getElementById('channelBInput');

    try {
        const res = await ApiClient.get('/api/social-accounts');
        const accounts = (res && res.data) ? res.data : [];

        // Populate datalists
        const dlA = document.getElementById('channelSuggestionsA');
        const dlB = document.getElementById('channelSuggestionsB');

        let optionsHtml = '';
        accounts.forEach(acc => {
            optionsHtml += `<option value="${acc.handle}">${acc.accountName} (${acc.platform})</option>`;
        });

        // Add prominent public media suggestions
        optionsHtml += `
            <option value="@saregamamusic">Saregama Music</option>
            <option value="@tseries">T-Series</option>
            <option value="@sonymusicindia">Sony Music India</option>
            <option value="@zeemusiccompany">Zee Music Company</option>
            <option value="@way4news999">Way4News</option>
        `;

        if (dlA) dlA.innerHTML = optionsHtml;
        if (dlB) dlB.innerHTML = optionsHtml;

        // Set smart default values if empty
        if (channelA && !channelA.value.trim()) {
            if (accounts.length > 0) {
                channelA.value = accounts[0].handle || accounts[0].accountName;
            } else {
                channelA.value = '@saregamamusic';
            }
        }

        if (channelB && !channelB.value.trim()) {
            if (accounts.length > 1) {
                channelB.value = accounts[1].handle || accounts[1].accountName;
            } else {
                channelB.value = '@tseries';
            }
        }
    } catch (e) {
        if (channelA && !channelA.value.trim()) channelA.value = '@saregamamusic';
        if (channelB && !channelB.value.trim()) channelB.value = '@tseries';
    }
}

function onPlatformSelectChange(side) {
    const platform = document.getElementById(side === 'A' ? 'platformASelect' : 'platformBSelect').value;
    const input = document.getElementById(side === 'A' ? 'channelAInput' : 'channelBInput');

    if (!input) return;

    if (platform === 'YOUTUBE') {
        input.placeholder = side === 'A' ? 'e.g. @saregamamusic or Channel ID' : 'e.g. @tseries or Channel ID';
        if (side === 'B' && (!input.value || input.value.includes('insta') || input.value.includes('twitter'))) {
            input.value = '@tseries';
        }
    } else if (platform === 'INSTAGRAM') {
        input.placeholder = 'e.g. @saregama_official or @rival';
        if (side === 'B' && (!input.value || input.value.includes('tseries'))) {
            input.value = '@tseriesfilms';
        }
    } else if (platform === 'TWITTER') {
        input.placeholder = 'e.g. @tseries or @handle';
    } else if (platform === 'LINKEDIN') {
        input.placeholder = 'e.g. @tseries or @company-name';
    } else if (platform === 'FACEBOOK') {
        input.placeholder = 'e.g. @tseries or @page-name';
    }

    loadPlatformComparison();
}

// =========================================================================
// HEAD-TO-HEAD TWO PLATFORM / TWO CHANNEL COMPARATOR
// =========================================================================

async function loadPlatformComparison() {
    const platformA = document.getElementById('platformASelect').value;
    const channelA = document.getElementById('channelAInput').value.trim();
    const platformB = document.getElementById('platformBSelect').value;
    const channelB = document.getElementById('channelBInput').value.trim();
    const container = document.getElementById('comparisonContent');

    if (!container) return;

    container.innerHTML = `
        <div style="text-align:center; padding:35px; color:#64748b;">
            <div style="font-size:24px; margin-bottom:10px;">⚡</div>
            <div style="font-weight:600;">Fetching live channel stats, subscribers, views, and comment sentiment...</div>
        </div>
    `;

    try {
        const res = await ApiClient.get('/api/competitors/compare-platforms', {
            platformA,
            channelA,
            platformB,
            channelB
        });

        if (!res.success || !res.data) throw new Error('Failed to compute channel comparison');

        currentComparisonData = res.data;
        renderPlatformComparison(res.data);
    } catch (err) {
        container.innerHTML = `
            <div style="text-align:center; padding:30px; color:var(--danger);">
                <div style="font-weight:700; margin-bottom:6px;">Comparison Computation Error</div>
                <div style="font-size:13px;">${err.message}</div>
            </div>
        `;
    }
}

function renderPlatformComparison(data) {
    const container = document.getElementById('comparisonContent');
    if (!container || !data) return;

    const colorA = getPlatformColor(data.platformA);
    const colorB = getPlatformColor(data.platformB);
    const iconA = getPlatformIcon(data.platformA);
    const iconB = getPlatformIcon(data.platformB);

    const titleA = data.channelTitleA || data.platformAName;
    const titleB = data.channelTitleB || data.platformBName;

    const winnerA_Audience = data.audienceLeader && data.audienceLeader.includes(titleA);
    const winnerB_Audience = data.audienceLeader && data.audienceLeader.includes(titleB);

    const winnerA_Engagement = data.engagementLeader && data.engagementLeader.includes(titleA);
    const winnerB_Engagement = data.engagementLeader && data.engagementLeader.includes(titleB);

    const winnerA_Velocity = data.velocityLeader && data.velocityLeader.includes(titleA);
    const winnerB_Velocity = data.velocityLeader && data.velocityLeader.includes(titleB);

    const winnerA_Sentiment = data.sentimentLeader && data.sentimentLeader.includes(titleA);
    const winnerB_Sentiment = data.sentimentLeader && data.sentimentLeader.includes(titleB);

    container.innerHTML = `
        <!-- Side-by-Side Dual Platform / Channel Grid -->
        <div style="display:grid; grid-template-columns: 1fr 1fr; gap:24px; margin-bottom:24px;" class="duel-grid">
            <!-- Platform A Card -->
            <div class="card" style="border: 2px solid ${colorA.border}; background:var(--bg-card); box-shadow:0 4px 14px rgba(0,0,0,0.03); border-radius:12px; padding:20px;">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; border-bottom:1px solid var(--border-color); padding-bottom:14px;">
                    <div style="display:flex; align-items:center; gap:12px;">
                        <div style="width:42px; height:42px; border-radius:4px; background:${colorA.bg}; color:${colorA.text}; display:flex; align-items:center; justify-content:center; font-weight:600; font-size:20px;">
                            ${iconA}
                        </div>
                        <div>
                            <div style="font-size:18px; font-weight:600; color:var(--text-dark);">${titleA}</div>
                            <div style="font-size:12px; color:var(--text-secondary); font-weight:600;">
                                <span class="platform-pill pill-${data.platformA.toLowerCase()}" style="font-size:10px; padding:2px 6px; margin-right:4px;">${data.platformAName}</span>
                                ${data.handleA || '@verified'}
                            </div>
                        </div>
                    </div>
                    ${data.connectedA 
                        ? '<span class="status-badge status-connected" style="font-size:11px;">CONNECTED</span>' 
                        : '<span class="status-badge" style="background:var(--bg-muted); color:var(--text-secondary); border:1px solid var(--border-color); font-size:11px;">PUBLIC CHANNEL</span>'}
                </div>

                <!-- Metrics Grid -->
                <div style="display:grid; grid-template-columns: 1fr 1fr; gap:12px; margin-bottom:18px;">
                    <div style="background:var(--bg-muted); padding:12px; border-radius:8px; border:1px solid var(--border-color);">
                        <div style="font-size:11px; font-weight:700; color:var(--text-secondary); text-transform:uppercase;">Subscribers / Audience</div>
                        <div style="font-size:20px; font-weight:600; color:var(--text-dark); margin-top:2px;">
                            ${formatNumber(data.followersA)}
                            ${winnerA_Audience ? '<span style="font-size:11px; font-weight:700; background:rgba(16,185,129,0.15); color:#10b981; border:1px solid rgba(16,185,129,0.3); padding:2px 6px; border-radius:4px; margin-left:4px;">🏆 LEADER</span>' : ''}
                        </div>
                    </div>

                    <div style="background:var(--bg-muted); padding:12px; border-radius:8px; border:1px solid var(--border-color);">
                        <div style="font-size:11px; font-weight:700; color:var(--text-secondary); text-transform:uppercase;">Engagement Rate</div>
                        <div style="font-size:20px; font-weight:600; color:${colorA.primary}; margin-top:2px;">
                            ${data.engagementRateA ? data.engagementRateA.toFixed(2) : '0.00'}%
                            ${winnerA_Engagement ? '<span style="font-size:11px; font-weight:700; background:rgba(16,185,129,0.15); color:#10b981; border:1px solid rgba(16,185,129,0.3); padding:2px 6px; border-radius:4px; margin-left:4px;">🏆 HIGHER</span>' : ''}
                        </div>
                    </div>

                    <div style="background:var(--bg-muted); padding:12px; border-radius:8px; border:1px solid var(--border-color);">
                        <div style="font-size:11px; font-weight:700; color:var(--text-secondary); text-transform:uppercase;">Published Videos / Posts</div>
                        <div style="font-size:18px; font-weight:600; color:var(--text-dark); margin-top:2px;">
                            ${data.postsCountA.toLocaleString()}
                            ${winnerA_Velocity ? '<span style="font-size:11px; font-weight:700; background:rgba(16,185,129,0.15); color:#10b981; border:1px solid rgba(16,185,129,0.3); padding:2px 6px; border-radius:4px; margin-left:4px;">🏆 VELOCITY</span>' : ''}
                        </div>
                    </div>

                    <div style="background:var(--bg-muted); padding:12px; border-radius:8px; border:1px solid var(--border-color);">
                        <div style="font-size:11px; font-weight:700; color:var(--text-secondary); text-transform:uppercase;">Total Lifetime Views / Reach</div>
                        <div style="font-size:18px; font-weight:600; color:var(--text-dark); margin-top:2px;">
                            ${formatNumber(data.viewsOrReachA)}
                        </div>
                    </div>
                </div>

                <!-- Sentiment Pulse -->
                <div style="background:var(--bg-muted); padding:12px 14px; border-radius:8px; border:1px solid var(--border-color); margin-bottom:16px;">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
                        <div style="font-size:11px; font-weight:700; color:var(--text-secondary); text-transform:uppercase;">Audience & Comment Sentiment</div>
                        <div style="font-size:12px; font-weight:700; color:#10b981;">
                            ${data.sentimentPositiveA.toFixed(1)}% Positive
                            ${winnerA_Sentiment ? '🏆' : ''}
                        </div>
                    </div>
                    <div style="height:8px; border-radius:4px; background:rgba(148, 163, 184, 0.2); display:flex; overflow:hidden;">
                        <div style="width:${data.sentimentPositiveA}%; background:#10b981;" title="Positive: ${data.sentimentPositiveA}%"></div>
                        <div style="width:${data.sentimentNeutralA}%; background:#94a3b8;" title="Neutral: ${data.sentimentNeutralA}%"></div>
                        <div style="width:${data.sentimentNegativeA}%; background:#ef4444;" title="Negative: ${data.sentimentNegativeA}%"></div>
                    </div>
                    <div style="display:flex; justify-content:space-between; font-size:11px; color:var(--text-secondary); margin-top:6px;">
                        <span style="color:#10b981; font-weight:600;">● ${data.sentimentPositiveA.toFixed(1)}% Pos</span>
                        <span style="color:var(--text-secondary); font-weight:600;">● ${data.sentimentNeutralA.toFixed(1)}% Neu</span>
                        <span style="color:#ef4444; font-weight:600;">● ${data.sentimentNegativeA.toFixed(1)}% Neg</span>
                    </div>
                </div>

                <!-- Strengths -->
                <div>
                    <div style="font-size:11px; font-weight:700; color:var(--text-secondary); text-transform:uppercase; margin-bottom:6px;">Platform & Channel Strengths</div>
                    <ul style="margin:0; padding-left:18px; font-size:12.5px; color:var(--text-primary); line-height:1.6;">
                        ${(data.strengthsA || []).map(s => `<li>${s}</li>`).join('')}
                    </ul>
                </div>
            </div>

            <!-- Platform B Card -->
            <div class="card" style="border: 2px solid ${colorB.border}; background:var(--bg-card); box-shadow:0 4px 14px rgba(0,0,0,0.03); border-radius:12px; padding:20px;">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; border-bottom:1px solid var(--border-color); padding-bottom:14px;">
                    <div style="display:flex; align-items:center; gap:12px;">
                        <div style="width:42px; height:42px; border-radius:4px; background:${colorB.bg}; color:${colorB.text}; display:flex; align-items:center; justify-content:center; font-weight:600; font-size:20px;">
                            ${iconB}
                        </div>
                        <div>
                            <div style="font-size:18px; font-weight:600; color:var(--text-dark);">${titleB}</div>
                            <div style="font-size:12px; color:var(--text-secondary); font-weight:600;">
                                <span class="platform-pill pill-${data.platformB.toLowerCase()}" style="font-size:10px; padding:2px 6px; margin-right:4px;">${data.platformBName}</span>
                                ${data.handleB || '@benchmark'}
                            </div>
                        </div>
                    </div>
                    ${data.connectedB 
                        ? '<span class="status-badge status-connected" style="font-size:11px;">CONNECTED</span>' 
                        : '<span class="status-badge" style="background:var(--bg-muted); color:var(--text-secondary); border:1px solid var(--border-color); font-size:11px;">PUBLIC CHANNEL</span>'}
                </div>

                <!-- Metrics Grid -->
                <div style="display:grid; grid-template-columns: 1fr 1fr; gap:12px; margin-bottom:18px;">
                    <div style="background:var(--bg-muted); padding:12px; border-radius:8px; border:1px solid var(--border-color);">
                        <div style="font-size:11px; font-weight:700; color:var(--text-secondary); text-transform:uppercase;">Subscribers / Audience</div>
                        <div style="font-size:20px; font-weight:600; color:var(--text-dark); margin-top:2px;">
                            ${formatNumber(data.followersB)}
                            ${winnerB_Audience ? '<span style="font-size:11px; font-weight:700; background:rgba(16,185,129,0.15); color:#10b981; border:1px solid rgba(16,185,129,0.3); padding:2px 6px; border-radius:4px; margin-left:4px;">🏆 LEADER</span>' : ''}
                        </div>
                    </div>

                    <div style="background:var(--bg-muted); padding:12px; border-radius:8px; border:1px solid var(--border-color);">
                        <div style="font-size:11px; font-weight:700; color:var(--text-secondary); text-transform:uppercase;">Engagement Rate</div>
                        <div style="font-size:20px; font-weight:600; color:${colorB.primary}; margin-top:2px;">
                            ${data.engagementRateB ? data.engagementRateB.toFixed(2) : '0.00'}%
                            ${winnerB_Engagement ? '<span style="font-size:11px; font-weight:700; background:rgba(16,185,129,0.15); color:#10b981; border:1px solid rgba(16,185,129,0.3); padding:2px 6px; border-radius:4px; margin-left:4px;">🏆 HIGHER</span>' : ''}
                        </div>
                    </div>

                    <div style="background:var(--bg-muted); padding:12px; border-radius:8px; border:1px solid var(--border-color);">
                        <div style="font-size:11px; font-weight:700; color:var(--text-secondary); text-transform:uppercase;">Published Videos / Posts</div>
                        <div style="font-size:18px; font-weight:600; color:var(--text-dark); margin-top:2px;">
                            ${data.postsCountB.toLocaleString()}
                            ${winnerB_Velocity ? '<span style="font-size:11px; font-weight:700; background:rgba(16,185,129,0.15); color:#10b981; border:1px solid rgba(16,185,129,0.3); padding:2px 6px; border-radius:4px; margin-left:4px;">🏆 VELOCITY</span>' : ''}
                        </div>
                    </div>

                    <div style="background:var(--bg-muted); padding:12px; border-radius:8px; border:1px solid var(--border-color);">
                        <div style="font-size:11px; font-weight:700; color:var(--text-secondary); text-transform:uppercase;">Total Lifetime Views / Reach</div>
                        <div style="font-size:18px; font-weight:600; color:var(--text-dark); margin-top:2px;">
                            ${formatNumber(data.viewsOrReachB)}
                        </div>
                    </div>
                </div>

                <!-- Sentiment Pulse -->
                <div style="background:var(--bg-muted); padding:12px 14px; border-radius:8px; border:1px solid var(--border-color); margin-bottom:16px;">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
                        <div style="font-size:11px; font-weight:700; color:var(--text-secondary); text-transform:uppercase;">Audience & Comment Sentiment</div>
                        <div style="font-size:12px; font-weight:700; color:#10b981;">
                            ${data.sentimentPositiveB.toFixed(1)}% Positive
                            ${winnerB_Sentiment ? '🏆' : ''}
                        </div>
                    </div>
                    <div style="height:8px; border-radius:4px; background:rgba(148, 163, 184, 0.2); display:flex; overflow:hidden;">
                        <div style="width:${data.sentimentPositiveB}%; background:#10b981;" title="Positive: ${data.sentimentPositiveB}%"></div>
                        <div style="width:${data.sentimentNeutralB}%; background:#94a3b8;" title="Neutral: ${data.sentimentNeutralB}%"></div>
                        <div style="width:${data.sentimentNegativeB}%; background:#ef4444;" title="Negative: ${data.sentimentNegativeB}%"></div>
                    </div>
                    <div style="display:flex; justify-content:space-between; font-size:11px; color:var(--text-secondary); margin-top:6px;">
                        <span style="color:#10b981; font-weight:600;">● ${data.sentimentPositiveB.toFixed(1)}% Pos</span>
                        <span style="color:var(--text-secondary); font-weight:600;">● ${data.sentimentNeutralB.toFixed(1)}% Neu</span>
                        <span style="color:#ef4444; font-weight:600;">● ${data.sentimentNegativeB.toFixed(1)}% Neg</span>
                    </div>
                </div>

                <!-- Strengths -->
                <div>
                    <div style="font-size:11px; font-weight:700; color:var(--text-secondary); text-transform:uppercase; margin-bottom:6px;">Platform & Channel Strengths</div>
                    <ul style="margin:0; padding-left:18px; font-size:12.5px; color:var(--text-primary); line-height:1.6;">
                        ${(data.strengthsB || []).map(s => `<li>${s}</li>`).join('')}
                    </ul>
                </div>
            </div>
        </div>

        <!-- Executive Narrative & Strategic Action Items -->
        <div style="background:var(--bg-muted); border:1px solid var(--border-color); border-radius:12px; padding:20px; margin-bottom:16px;">
            <div style="display:flex; align-items:center; justify-content:space-between; flex-wrap:wrap; gap:12px; margin-bottom:12px; border-bottom:1px solid var(--border-color); padding-bottom:12px;">
                <div style="display:flex; align-items:center; gap:8px;">
                    <span style="font-size:20px;">🏆</span>
                    <div style="font-size:16px; font-weight:600; color:var(--text-dark);">${data.overallWinner || 'Channel Matchup Verdict'}</div>
                </div>
                <div style="font-size:12px; color:var(--text-secondary); font-weight:600;">
                    Audience Leader: <strong style="color:var(--text-dark);">${data.audienceLeader}</strong> • Engagement Leader: <strong style="color:var(--text-dark);">${data.engagementLeader}</strong>
                </div>
            </div>

            <p style="margin:0 0 14px 0; font-size:13.5px; line-height:1.6; color:var(--text-primary);">
                ${data.executiveSummary}
            </p>

            ${data.recommendations && data.recommendations.length > 0 ? `
                <div style="font-size:12px; font-weight:600; color:var(--text-secondary); text-transform:uppercase; margin-bottom:8px;">
                    Recommended Strategic Playbook:
                </div>
                <ul style="margin:0; padding-left:20px; font-size:13px; line-height:1.6; color:var(--text-primary);">
                    ${data.recommendations.map(r => `<li style="margin-bottom:4px;">${r}</li>`).join('')}
                </ul>
            ` : ''}
        </div>

        <!-- Quick 1-Click Email Trigger Bar -->
        <div style="background:linear-gradient(135deg, rgba(0, 242, 254, 0.08) 0%, rgba(192, 132, 252, 0.08) 100%); border:1px dashed var(--primary-blue); border-radius:8px; padding:14px 20px; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px;">
            <div style="display:flex; align-items:center; gap:10px;">
                <span style="font-size:20px;">✉️</span>
                <div>
                    <div style="font-size:13px; font-weight:700; color:var(--text-dark);">Send Comprehensive Head-to-Head Dossier to Registered Address</div>
                    <div style="font-size:12px; color:var(--text-secondary);">Target Recipient: <strong style="color:var(--primary-blue);">${data.recipientEmail || 'arunvemula92@gmail.com'}</strong></div>
                </div>
            </div>
            <button class="btn btn-primary" style="font-weight:700; font-size:13px; padding:8px 18px;" onclick="sendComparisonEmail()">
                Dispatch Now ↗
            </button>
        </div>
    `;
}

// =========================================================================
// EMAIL DISPATCH CONTROLLER & DELIVERY HUB
// =========================================================================

let currentEmailPayload = null;

async function sendComparisonEmail(autoOpenGmail = false) {
    const platformA = document.getElementById('platformASelect').value;
    const channelA = document.getElementById('channelAInput').value.trim();
    const platformB = document.getElementById('platformBSelect').value;
    const channelB = document.getElementById('channelBInput').value.trim();
    const btn = document.getElementById('sendEmailBtn');

    const user = ApiClient.getUser();
    const recipientEmail = (user && user.email) ? user.email : 'arunvemula92@gmail.com';

    if (btn) {
        btn.disabled = true;
        btn.innerHTML = `
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="spin"><circle cx="12" cy="12" r="10"></circle><path d="M12 2a10 10 0 0 1 10 10"></path></svg>
            <span>Preparing Dossier...</span>
        `;
    }

    try {
        const res = await ApiClient.post('/api/competitors/compare-platforms/email', {
            platformA,
            channelA,
            platformB,
            channelB,
            recipientEmail,
            customNotes: `Executive Head-to-Head Audit: ${channelA || platformA} vs ${channelB || platformB}`
        });

        if (res.success && res.data) {
            currentEmailPayload = res.data;

            // Update UI elements in modal
            const emailEl = document.getElementById('receiptEmail');
            if (emailEl) emailEl.textContent = res.data.recipientEmail;

            const timeEl = document.getElementById('receiptTimestamp');
            if (timeEl) timeEl.textContent = res.data.sentAt;

            const routeEl = document.getElementById('receiptRouteText');
            const banner = document.getElementById('receiptStatusBanner');
            const dot = document.getElementById('receiptStatusDot');
            const statusText = document.getElementById('receiptStatusText');
            const statusDetail = document.getElementById('receiptStatusDetail');

            if (res.data.deliveryStatus === 'SENT_VIA_SMTP') {
                if (banner) {
                    banner.style.background = '#f0fdf4';
                    banner.style.borderColor = '#bbf7d0';
                }
                if (dot) dot.style.background = '#16a34a';
                if (statusText) {
                    statusText.textContent = 'Delivered to Inbox via SMTP';
                    statusText.style.color = '#15803d';
                }
                if (statusDetail) {
                    statusDetail.textContent = res.data.deliveryMessage || `Outbound email dispatched directly to ${res.data.recipientEmail}. Check your inbox!`;
                    statusDetail.style.color = '#166534';
                }
                if (routeEl) {
                    routeEl.innerHTML = '<span style="color:#16a34a; display:inline-flex; align-items:center; gap:4px;"><span style="width:8px; height:8px; border-radius:50%; background:#16a34a;"></span> Direct SMTP Delivery</span>';
                }
                showToast(`Report delivered to ${res.data.recipientEmail} via SMTP!`, 'success');
            } else {
                if (banner) {
                    banner.style.background = '#fff7ed';
                    banner.style.borderColor = '#fed7aa';
                }
                if (dot) dot.style.background = '#ea580c';
                if (statusText) {
                    statusText.textContent = 'Direct SMTP Not Yet Configured';
                    statusText.style.color = '#9a3412';
                }
                if (statusDetail) {
                    statusDetail.textContent = `To deliver automatically in the background, Google requires a 16-character App Password. Click "Open & Send in Gmail" below to send now with 1 click, or enter your App Password below.`;
                    statusDetail.style.color = '#c2410c';
                }
                if (routeEl) {
                    routeEl.innerHTML = '<span style="color:#ea580c; display:inline-flex; align-items:center; gap:4px;"><span style="width:8px; height:8px; border-radius:50%; background:#ea580c;"></span> Setup App Password / Webmail</span>';
                }

                // Automatically display SMTP setup card so user knows exactly what to do
                const smtpCard = document.getElementById('smtpSettingsCard');
                if (smtpCard) smtpCard.style.display = 'block';

                showToast(`Report prepared! Click "Open in Gmail" or add App Password below.`, 'info');
            }

            const previewContainer = document.getElementById('emailPreviewFrame');
            if (previewContainer && res.data.previewHtml) {
                previewContainer.innerHTML = res.data.previewHtml;
            }

            openEmailReceiptModal();
        } else {
            throw new Error(res.message || 'Failed to dispatch report');
        }
    } catch (err) {
        showToast(err.message || 'Failed to prepare comparison email.', 'error');
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = `
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
                <span>Send to Registered Email</span>
            `;
        }
    }
}

async function openQuickGmailDraft() {
    const platformA = document.getElementById('platformASelect').value;
    const channelA = document.getElementById('channelAInput').value.trim();
    const platformB = document.getElementById('platformBSelect').value;
    const channelB = document.getElementById('channelBInput').value.trim();
    const user = ApiClient.getUser();
    const recipientEmail = (user && user.email) ? user.email : 'arunvemula92@gmail.com';

    // Synchronously open a window before the async call to bypass popup blockers
    const composeWin = window.open('about:blank', '_blank');
    if (composeWin) {
        composeWin.document.write(`
            <!DOCTYPE html>
            <html>
            <head>
                <title>Loading SPEDO Intelligence Briefing...</title>
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #0f172a; color: #f8fafc; display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; }
                    .card { background: #1e293b; border: 1px solid #334155; border-radius: 16px; padding: 36px 44px; text-align: center; max-width: 440px; box-shadow: 0 20px 40px rgba(0,0,0,0.5); }
                    .spinner { width: 36px; height: 36px; border: 3px solid rgba(67, 97, 238, 0.2); border-top-color: #4361ee; border-radius: 50%; animation: spin 0.8s linear infinite; margin: 20px auto 0; }
                    @keyframes spin { to { transform: rotate(360deg); } }
                </style>
            </head>
            <body>
                <div class="card">
                    <div style="font-size: 32px; margin-bottom: 12px;">📊 ➔ ✉️</div>
                    <h2 style="margin: 0 0 8px; font-size: 18px; font-weight: 800;">Preparing Gmail Draft</h2>
                    <p style="color: #94a3b8; font-size: 13.5px; line-height: 1.5; margin: 0;">Generating executive benchmark comparison addressed to <strong>${recipientEmail}</strong>...</p>
                    <div class="spinner"></div>
                </div>
            </body>
            </html>
        `);
    }

    try {
        const res = await ApiClient.post('/api/competitors/compare-platforms/email', {
            platformA,
            channelA,
            platformB,
            channelB,
            recipientEmail,
            customNotes: `Executive Head-to-Head Audit: ${channelA || platformA} vs ${channelB || platformB}`
        });

        if (res.success && res.data && res.data.gmailComposeUrl) {
            currentEmailPayload = res.data;

            // Automatically copy rich full-color HTML to clipboard so Ctrl+V in Gmail pastes the colorful visual report
            if (res.data.previewHtml && window.ClipboardItem && navigator.clipboard && navigator.clipboard.write) {
                try {
                    const htmlBlob = new Blob([res.data.previewHtml], { type: 'text/html' });
                    const textBlob = new Blob([res.data.plainTextSummary || ''], { type: 'text/plain' });
                    await navigator.clipboard.write([new ClipboardItem({ 'text/html': htmlBlob, 'text/plain': textBlob })]);
                } catch (clipErr) {
                    console.warn('Auto clipboard write error:', clipErr);
                }
            }

            if (composeWin) {
                composeWin.location.href = res.data.gmailComposeUrl;
            } else {
                window.location.href = res.data.gmailComposeUrl;
            }
            showToast('🎨 Full color visual report copied! In Gmail compose, press Ctrl+V to paste the color cards!', 'success', 8000);
        } else {
            if (composeWin) composeWin.close();
            throw new Error(res.message || 'Failed to prepare email draft');
        }
    } catch (err) {
        if (composeWin) composeWin.close();
        showToast(err.message || 'Failed to connect to analytics server.', 'error');
    }
}

async function openGmailDraft() {
    if (!currentEmailPayload || !currentEmailPayload.gmailComposeUrl) {
        showToast('Generate a comparison first before opening Gmail.', 'warning');
        return;
    }

    // Copy rich full-color HTML to clipboard before opening Gmail
    if (currentEmailPayload.previewHtml && window.ClipboardItem && navigator.clipboard && navigator.clipboard.write) {
        try {
            const htmlBlob = new Blob([currentEmailPayload.previewHtml], { type: 'text/html' });
            const textBlob = new Blob([currentEmailPayload.plainTextSummary || ''], { type: 'text/plain' });
            await navigator.clipboard.write([new ClipboardItem({ 'text/html': htmlBlob, 'text/plain': textBlob })]);
        } catch (clipErr) {
            console.warn('Auto clipboard write error:', clipErr);
        }
    }

    window.open(currentEmailPayload.gmailComposeUrl, '_blank');
    showToast('🎨 Full color visual report copied! In Gmail compose, press Ctrl+V to paste the color cards!', 'success', 8000);
}

function openDefaultMailClient() {
    if (!currentEmailPayload || !currentEmailPayload.mailtoUrl) {
        showToast('Generate a comparison first.', 'warning');
        return;
    }
    window.location.href = currentEmailPayload.mailtoUrl;
}

async function copyEmailBriefing() {
    if (!currentEmailPayload || !currentEmailPayload.previewHtml) {
        showToast('No briefing available to copy.', 'warning');
        return;
    }
    try {
        if (window.ClipboardItem && navigator.clipboard && navigator.clipboard.write) {
            const htmlBlob = new Blob([currentEmailPayload.previewHtml], { type: 'text/html' });
            const textBlob = new Blob([currentEmailPayload.plainTextSummary || ''], { type: 'text/plain' });
            await navigator.clipboard.write([new ClipboardItem({ 'text/html': htmlBlob, 'text/plain': textBlob })]);
            const btnText = document.getElementById('copyBriefingBtnText');
            if (btnText) {
                const orig = btnText.innerHTML;
                btnText.innerHTML = 'Copied Full Color Report! &#10003;';
                setTimeout(() => { btnText.innerHTML = orig; }, 3000);
            }
            showToast('🎨 Full color visual report copied! In Gmail or Outlook, press Ctrl+V to paste the color cards.', 'success', 7000);
        } else {
            await navigator.clipboard.writeText(currentEmailPayload.plainTextSummary || '');
            showToast('Copied text briefing to clipboard.', 'info');
        }
    } catch (err) {
        console.warn('Rich clipboard write failed, falling back to text:', err);
        try {
            await navigator.clipboard.writeText(currentEmailPayload.plainTextSummary || '');
            showToast('Copied briefing summary to clipboard.', 'info');
        } catch (e) {
            showToast('Could not access clipboard. Please check browser permissions.', 'error');
        }
    }
}

function downloadEmailReport() {
    if (!currentEmailPayload || !currentEmailPayload.previewHtml) {
        showToast('No report preview available to download.', 'warning');
        return;
    }
    const blob = new Blob([currentEmailPayload.previewHtml], { type: 'text/html;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `SPEDO_Executive_Benchmark_${Date.now()}.html`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    showToast('Executive HTML briefing downloaded successfully!', 'success');
}

function toggleSmtpSettingsCard() {
    const card = document.getElementById('smtpSettingsCard');
    if (!card) return;
    const isHidden = card.style.display === 'none' || card.style.display === '';
    card.style.display = isHidden ? 'block' : 'none';

    if (isHidden) {
        // Fetch current SMTP status from server
        ApiClient.get('/api/competitors/smtp-status')
            .then(res => {
                if (res.success && res.data) {
                    if (res.data.host) document.getElementById('smtpHostInput').value = res.data.host;
                    if (res.data.port) document.getElementById('smtpPortInput').value = res.data.port;
                    if (res.data.username) document.getElementById('smtpUserInput').value = res.data.username;
                }
            })
            .catch(() => {});
    }
}

async function saveSmtpConfigAndSend() {
    const host = document.getElementById('smtpHostInput').value.trim();
    const port = document.getElementById('smtpPortInput').value.trim();
    const username = document.getElementById('smtpUserInput').value.trim();
    const password = document.getElementById('smtpPassInput').value.trim();

    if (!username || !password) {
        showToast('Please enter both your Gmail address and 16-char App Password.', 'warning');
        return;
    }

    try {
        const res = await ApiClient.post('/api/competitors/smtp-config', { host, port, username, password });
        if (res.success) {
            showToast('SMTP settings saved! Now attempting direct SMTP dispatch...', 'info');
            await sendComparisonEmail(false);
        } else {
            throw new Error(res.message || 'Failed to update SMTP settings');
        }
    } catch (err) {
        showToast(err.message || 'Failed to save SMTP settings.', 'error');
    }
}

async function testSmtpConnection() {
    const user = ApiClient.getUser();
    const recipientEmail = (user && user.email) ? user.email : 'arunvemula92@gmail.com';
    showToast('Sending test email via SMTP...', 'info');

    try {
        const res = await ApiClient.post('/api/competitors/test-smtp', { recipientEmail });
        if (res.success && res.data && res.data.sent) {
            showToast(`SMTP Test Successful! Email delivered to ${recipientEmail}`, 'success');
        } else {
            showToast(`SMTP Test Failed: ${(res.data && res.data.message) || res.message}`, 'error');
        }
    } catch (err) {
        showToast(`SMTP Test Error: ${err.message}`, 'error');
    }
}

function openEmailReceiptModal() {
    const modal = document.getElementById('emailReceiptModal');
    if (modal) modal.classList.add('open');
}

function closeEmailReceiptModal() {
    const modal = document.getElementById('emailReceiptModal');
    if (modal) modal.classList.remove('open');
}

// =========================================================================
// COMPETITOR LEADERBOARD & BENCHMARK CHARTS
// =========================================================================

async function reloadComparison() {
    const platform = document.getElementById('compPlatformFilter').value;
    const sortBy = document.getElementById('compSortFilter').value;

    const params = { sortBy };
    if (platform) params.platform = platform;

    try {
        const res = await ApiClient.get('/api/competitors/compare', params);
        if (!res.success || !res.data) throw new Error('Failed to load comparison data');

        const benchmarks = res.data;
        renderLeaderboard(benchmarks);
        renderComparisonChart(benchmarks, sortBy);
    } catch (err) {
        document.getElementById('competitorTableBody').innerHTML = `
            <tr><td colspan="10" style="color:var(--danger); text-align:center; padding:20px;">Error loading benchmarks: ${err.message}</td></tr>
        `;
    }
}

function renderLeaderboard(list) {
    const tbody = document.getElementById('competitorTableBody');
    if (!list || list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" style="text-align:center; padding:30px; color:#94a3b8;">No competitor metrics recorded. Add competitors above.</td></tr>';
        return;
    }

    tbody.innerHTML = list.map(item => {
        const isSelf = item.self;
        const rowStyle = isSelf ? 'background:rgba(67, 97, 238, 0.08); font-weight:600;' : '';
        const rankBadge = item.rank === 1 ? '🥇' : item.rank === 2 ? '🥈' : item.rank === 3 ? '🥉' : `#${item.rank}`;

        return `
            <tr style="${rowStyle}">
                <td style="font-weight:600; font-size:14px;">${rankBadge}</td>
                <td>
                    <div style="font-weight:700; color:${isSelf ? 'var(--primary-blue)' : 'var(--text-dark)'};">
                        ${item.name} ${isSelf ? '<span class="status-badge status-connected" style="margin-left:4px;">YOU</span>' : ''}
                    </div>
                </td>
                <td>
                    <span class="platform-pill pill-${(item.platform || 'youtube').toLowerCase()}">
                        ${item.platform || 'Cross-Platform'}
                    </span>
                </td>
                <td style="font-weight:700;">${formatNumber(item.followers)}</td>
                <td style="color:${item.followerGrowth >= 0 ? 'var(--success)' : 'var(--danger)'};">
                    ${item.followerGrowth >= 0 ? '+' : ''}${formatNumber(item.followerGrowth)}
                </td>
                <td>${item.postingFrequencyPerWeek ? Number(item.postingFrequencyPerWeek).toFixed(1) + '/wk' : '—'}</td>
                <td>${formatNumber(item.avgLikes)}</td>
                <td>${formatNumber(item.avgComments)}</td>
                <td>${formatNumber(item.avgShares)}</td>
                <td style="font-weight:700; color:var(--primary-violet); font-size:14px;">
                    ${(item.engagementRate || 0).toFixed(2)}%
                </td>
            </tr>
        `;
    }).join('');
}

function renderComparisonChart(list, sortBy) {
    const ctx = document.getElementById('competitorChart');
    if (!ctx) return;

    if (competitorChartInstance) {
        competitorChartInstance.destroy();
    }

    const labels = list.map(item => item.name);
    let values = [];
    let metricLabel = '';

    switch (sortBy) {
        case 'followers':
            values = list.map(i => i.followers);
            metricLabel = 'Follower Count';
            break;
        case 'frequency':
            values = list.map(i => Number(i.postingFrequencyPerWeek) || 0);
            metricLabel = 'Posts per Week';
            break;
        case 'likes':
            values = list.map(i => i.avgLikes);
            metricLabel = 'Average Likes per Post';
            break;
        case 'growth':
            values = list.map(i => Number(i.followerGrowthRate) || 0);
            metricLabel = 'Growth Rate (%)';
            break;
        default:
            values = list.map(i => Number(i.engagementRate) || 0);
            metricLabel = 'Engagement Rate (%)';
            break;
    }

    const backgroundColors = list.map(item => item.self ? '#4361ee' : 'rgba(114, 9, 183, 0.7)');

    competitorChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: metricLabel,
                data: values,
                backgroundColor: backgroundColors,
                borderRadius: 6,
                borderWidth: 1,
                borderColor: '#ffffff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: (ctx) => `${metricLabel}: ${ctx.parsed.y}${sortBy === 'engagement' || sortBy === 'growth' ? '%' : ''}`
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: { color: '#f1f5f9' },
                    ticks: {
                        callback: (v) => (sortBy === 'engagement' || sortBy === 'growth') ? v + '%' : formatNumber(v)
                    }
                },
                x: {
                    grid: { display: false }
                }
            }
        }
    });
}

async function loadCompetitorProfiles() {
    const container = document.getElementById('competitorCardsContainer');
    try {
        const res = await ApiClient.get('/api/competitors');
        if (!res.success || !res.data || res.data.length === 0) {
            container.innerHTML = '<div style="grid-column:1/-1; text-align:center; padding:20px; color:#94a3b8;">No competitors tracked yet.</div>';
            return;
        }

        container.innerHTML = res.data.map(comp => `
            <div class="card" style="display:flex; flex-direction:column; justify-content:space-between;">
                <div>
                    <div style="display:flex; align-items:flex-start; justify-content:space-between; margin-bottom:10px;">
                        <div>
                            <div style="font-weight:700; color:var(--text-dark); font-size:16px;">${comp.name}</div>
                            <div style="font-size:12px; color:var(--text-secondary);">${comp.industry || 'Technology'}</div>
                        </div>
                        ${comp.demo ? '<span style="font-size:10px; background:#f3e8ff; color:#7209b7; padding:2px 6px; border-radius:4px; font-weight:600;">DEMO DATA</span>' : ''}
                    </div>

                    ${comp.website ? `
                        <div style="font-size:12px; margin-bottom:12px;">
                            <a href="${comp.website}" target="_blank" rel="noopener noreferrer" style="color:var(--primary-blue);">
                                ${comp.website.replace('https://', '').replace('http://', '')} ↗
                            </a>
                        </div>
                    ` : ''}

                    <div style="display:flex; flex-wrap:wrap; gap:6px; margin-top:8px; font-size:11px;">
                        ${comp.youtubeHandle ? `<span class="platform-pill pill-youtube">${comp.youtubeHandle}</span>` : ''}
                        ${comp.instagramHandle ? `<span class="platform-pill pill-instagram">${comp.instagramHandle}</span>` : ''}
                        ${comp.twitterHandle ? `<span class="platform-pill pill-twitter">${comp.twitterHandle}</span>` : ''}
                        ${comp.linkedinHandle ? `<span class="platform-pill pill-linkedin">${comp.linkedinHandle}</span>` : ''}
                    </div>
                </div>

                <div style="display:flex; justify-content:flex-end; border-top:1px solid var(--border-color); padding-top:12px; margin-top:16px;">
                    <button class="btn btn-sm" style="color:var(--danger); background:none; border:none; cursor:pointer;" onclick="deleteCompetitor(${comp.id}, '${comp.name}')">
                        Remove
                    </button>
                </div>
            </div>
        `).join('');
    } catch (err) {
        container.innerHTML = `<div style="grid-column:1/-1; color:var(--danger); padding:20px;">Failed to load profiles: ${err.message}</div>`;
    }
}

function openAddCompetitorModal() {
    document.getElementById('addCompetitorModal').classList.add('open');
}

function closeAddCompetitorModal() {
    document.getElementById('addCompetitorModal').classList.remove('open');
}

async function handleCompetitorSubmit(e) {
    e.preventDefault();
    const name = document.getElementById('compName').value.trim();
    const website = document.getElementById('compWebsite').value.trim();
    const industry = document.getElementById('compIndustry').value.trim();
    const instagramHandle = document.getElementById('compIg').value.trim();
    const twitterHandle = document.getElementById('compTw').value.trim();
    const linkedinHandle = document.getElementById('compLi').value.trim();
    const youtubeHandle = document.getElementById('compYt').value.trim();

    const btn = document.getElementById('saveCompBtn');
    btn.disabled = true;
    btn.textContent = 'Saving...';

    try {
        const res = await ApiClient.post('/api/competitors', {
            name,
            website,
            industry,
            instagramHandle,
            twitterHandle,
            linkedinHandle,
            youtubeHandle
        });

        if (res.success) {
            showToast(`Tracking competitor: ${name}!`, 'success');
            closeAddCompetitorModal();
            document.getElementById('competitorForm').reset();
            await initCompetitorsPage();
        }
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Start Tracking';
    }
}

async function deleteCompetitor(id, name) {
    if (!confirm(`Are you sure you want to stop tracking "${name}"?`)) return;

    try {
        const res = await ApiClient.delete(`/api/competitors/${id}`);
        if (res.success) {
            showToast(`Removed ${name} from competitor tracking.`, 'success');
            await initCompetitorsPage();
        }
    } catch (err) {
        showToast('Delete error: ' + err.message, 'error');
    }
}

// Helpers
function getPlatformColor(p) {
    const isDark = document.body.classList.contains('dark-mode');
    switch (p) {
        case 'YOUTUBE':
            return isDark 
                ? { primary: '#ef4444', bg: 'rgba(239, 68, 68, 0.15)', text: '#f87171', border: 'rgba(239, 68, 68, 0.35)' }
                : { primary: '#ef4444', bg: '#fef2f2', text: '#dc2626', border: '#fca5a5' };
        case 'INSTAGRAM':
            return isDark
                ? { primary: '#d946ef', bg: 'rgba(217, 70, 239, 0.15)', text: '#f472b6', border: 'rgba(217, 70, 239, 0.35)' }
                : { primary: '#d946ef', bg: '#fdf4ff', text: '#c026d3', border: '#f0abfc' };
        case 'TWITTER':
            return isDark
                ? { primary: '#38bdf8', bg: 'rgba(56, 189, 248, 0.15)', text: '#7dd3fc', border: 'rgba(56, 189, 248, 0.35)' }
                : { primary: '#0284c7', bg: '#f0f9ff', text: '#0369a1', border: '#7dd3fc' };
        case 'LINKEDIN':
            return isDark
                ? { primary: '#3b82f6', bg: 'rgba(59, 130, 246, 0.15)', text: '#60a5fa', border: 'rgba(59, 130, 246, 0.35)' }
                : { primary: '#2563eb', bg: '#eff6ff', text: '#1d4ed8', border: '#93c5fd' };
        case 'FACEBOOK':
            return isDark
                ? { primary: '#3b82f6', bg: 'rgba(59, 130, 246, 0.15)', text: '#60a5fa', border: 'rgba(59, 130, 246, 0.35)' }
                : { primary: '#1d4ed8', bg: '#eff6ff', text: '#1e40af', border: '#93c5fd' };
        default:
            return isDark
                ? { primary: '#00f2fe', bg: 'rgba(0, 242, 254, 0.12)', text: '#00f2fe', border: 'rgba(0, 242, 254, 0.3)' }
                : { primary: '#4361ee', bg: '#f1f5f9', text: '#334155', border: '#cbd5e1' };
    }
}

function getPlatformIcon(p) {
    switch (p) {
        case 'YOUTUBE': return '▶';
        case 'INSTAGRAM': return '📷';
        case 'TWITTER': return '𝕏';
        case 'LINKEDIN': return '💼';
        case 'FACEBOOK': return 'f';
        default: return '🌐';
    }
}

function formatNumber(num) {
    if (num == null) return '0';
    const n = Number(num);
    if (isNaN(n)) return '0';
    if (n >= 1_000_000_000) return (n / 1_000_000_000).toFixed(1) + 'B';
    if (n >= 1_000_000) return (n / 1_000_000).toFixed(1) + 'M';
    if (n >= 1_000) return (n / 1_000).toFixed(1) + 'K';
    return n.toLocaleString();
}
