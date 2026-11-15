/**
 * SPEDO Omnichannel Central Hub Controller
 * Solves The Fragmented Data Problem: Unified Central Database Telemetry
 */

let shareChartInstance = null;
let efficiencyChartInstance = null;

document.addEventListener('DOMContentLoaded', () => {
    loadOmnichannelData();
});

async function loadOmnichannelData() {
    try {
        const res = await ApiClient.get('/api/omnichannel/overview');
        if (!res.success || !res.data) return;

        const data = res.data;
        renderKPIs(data);
        renderCharts(data);
        renderMatrixTable(data.platformBreakdowns);
        renderCrossPosts(data.crossPlatformTopPosts);
    } catch (err) {
        console.error('Failed to load omnichannel overview:', err);
    }
}

function renderKPIs(data) {
    document.getElementById('totalAudienceVal').textContent = formatNumber(data.totalUnifiedAudience);
    document.getElementById('totalReachVal').textContent = formatNumber(data.consolidatedReach);
    document.getElementById('avgEngagementVal').textContent = (data.omnichannelEngagementRate || 0).toFixed(2) + '%';
    document.getElementById('hubHoursSaved').textContent = data.hoursSavedPerMonth + ' hrs';
}

function renderCharts(data) {
    const breakdowns = data.platformBreakdowns || [];
    if (breakdowns.length === 0) return;

    const platformColors = {
        'INSTAGRAM': '#E1306C',
        'FACEBOOK': '#1877F2',
        'YOUTUBE': '#FF0000',
        'LINKEDIN': '#0A66C2',
        'TWITTER': '#1D9BF0'
    };

    const labels = breakdowns.map(b => b.platformName);
    const audienceData = breakdowns.map(b => b.audience);
    const engagementData = breakdowns.map(b => b.avgEngagementRate);
    const bgColors = breakdowns.map(b => platformColors[b.platform] || '#4361ee');

    // 1. Audience Share Donut Chart
    const ctxShare = document.getElementById('audienceShareChart');
    if (ctxShare) {
        if (shareChartInstance) shareChartInstance.destroy();
        shareChartInstance = new Chart(ctxShare, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: audienceData,
                    backgroundColor: bgColors,
                    borderWidth: 2,
                    borderColor: '#ffffff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { boxWidth: 12, font: { family: 'Inter', size: 12 } }
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const val = context.raw || 0;
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const pct = total > 0 ? ((val / total) * 100).toFixed(1) : 0;
                                return ` ${context.label}: ${formatNumber(val)} (${pct}%)`;
                            }
                        }
                    }
                },
                cutout: '65%'
            }
        });
    }

    // 2. Platform Efficiency Chart
    const ctxEff = document.getElementById('platformEfficiencyChart');
    if (ctxEff) {
        if (efficiencyChartInstance) efficiencyChartInstance.destroy();
        efficiencyChartInstance = new Chart(ctxEff, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Avg Engagement Rate (%)',
                    data: engagementData,
                    backgroundColor: bgColors,
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                indexAxis: 'y',
                scales: {
                    x: {
                        beginAtZero: true,
                        ticks: { callback: v => v + '%' },
                        grid: { color: '#f1f5f9' }
                    },
                    y: {
                        grid: { display: false }
                    }
                },
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: ctx => ` Engagement Rate: ${ctx.raw.toFixed(2)}%`
                        }
                    }
                }
            }
        });
    }
}

function renderMatrixTable(breakdowns) {
    const tbody = document.getElementById('omnichannelTableBody');
    if (!tbody) return;

    if (!breakdowns || breakdowns.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:20px; color:#94a3b8;">No social accounts connected yet.</td></tr>`;
        return;
    }

    tbody.innerHTML = breakdowns.map(b => {
        const pClass = `pill-${b.platform.toLowerCase()}`;
        return `
            <tr>
                <td><span class="platform-pill ${pClass}">${b.platformName}</span></td>
                <td style="font-weight:600; color:var(--text-dark);">${b.accountHandle}</td>
                <td style="font-weight:700;">${formatNumber(b.audience)}</td>
                <td>
                    <div style="display:flex; align-items:center; gap:8px;">
                        <span style="font-weight:700; width:45px;">${b.audienceSharePercent}%</span>
                        <div style="flex:1; background:#f1f5f9; height:6px; border-radius:3px; max-width:80px; overflow:hidden;">
                            <div style="width:${b.audienceSharePercent}%; height:100%; background:var(--primary); border-radius:3px;"></div>
                        </div>
                    </div>
                </td>
                <td style="font-weight:700; color:#16a34a;">${(b.avgEngagementRate || 0).toFixed(2)}%</td>
                <td style="font-size:12px; color:#475569;">${b.primaryContentType || 'Standard Feed'}</td>
                <td style="font-size:12px; color:#64748b;">${b.keyStrength || 'Multi-channel presence'}</td>
            </tr>
        `;
    }).join('');
}

function renderCrossPosts(posts) {
    const grid = document.getElementById('crossPostsGrid');
    if (!grid) return;

    if (!posts || posts.length === 0) {
        grid.innerHTML = `<div style="grid-column:1/-1; text-align:center; padding:30px; color:#94a3b8;">No post content synchronized.</div>`;
        return;
    }

    grid.innerHTML = posts.map(p => {
        const pClass = `pill-${p.platform.toLowerCase()}`;
        const hasVanityAlert = p.vanityAlert;
        return `
            <div class="card" style="display:flex; flex-direction:column; justify-content:space-between; border:${hasVanityAlert ? '1px solid #f87171' : '1px solid var(--border-color)'};">
                <div>
                    <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:12px;">
                        <span class="platform-pill ${pClass}">${p.platform}</span>
                        <span style="font-size:11px; color:#94a3b8;">${formatDate(p.publishedAt)}</span>
                    </div>

                    ${hasVanityAlert ? `
                        <div style="background:#fef2f2; border:1px solid #fecaca; border-radius:6px; padding:8px 10px; margin-bottom:12px; font-size:11px; color:#b91c1c; font-weight:600; display:flex; align-items:flex-start; gap:6px;">
                            <span>⚠️</span>
                            <span>Vanity Alert: High likes, but negative comment complaints detected.</span>
                        </div>
                    ` : ''}

                    <div style="font-size:13px; color:#334155; line-height:1.5; margin-bottom:14px; max-height:60px; overflow:hidden; text-overflow:ellipsis;">
                        ${p.caption || 'No text preview available.'}
                    </div>

                    <div style="display:grid; grid-template-columns: repeat(3, 1fr); gap:8px; background:var(--bg-muted); border-radius:8px; padding:10px; text-align:center; margin-bottom:12px;">
                        <div>
                            <div style="font-size:10px; color:#64748b; text-transform:uppercase;">Likes</div>
                            <div style="font-size:14px; font-weight:800; color:var(--text-dark);">${formatNumber(p.likesCount)}</div>
                        </div>
                        <div>
                            <div style="font-size:10px; color:#64748b; text-transform:uppercase;">Comments</div>
                            <div style="font-size:14px; font-weight:800; color:var(--text-dark);">${formatNumber(p.commentsCount)}</div>
                        </div>
                        <div>
                            <div style="font-size:10px; color:#64748b; text-transform:uppercase;">Rate</div>
                            <div style="font-size:14px; font-weight:800; color:#16a34a;">${(p.engagementRate || 0).toFixed(2)}%</div>
                        </div>
                    </div>
                </div>

                <div style="display:flex; justify-content:space-between; align-items:center; border-top:1px solid var(--border-color); padding-top:10px;">
                    <span style="font-size:11px; color:#64748b;">${p.accountName}</span>
                    <a href="posts.html" class="btn btn-outline btn-sm" style="font-size:11px; padding:3px 8px;">View In Post Analytics</a>
                </div>
            </div>
        `;
    }).join('');
}
