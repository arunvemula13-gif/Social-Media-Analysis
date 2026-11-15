/**
 * SPEDO Reports & Executive Exports Controller
 */

let currentViewingReport = null;

document.addEventListener('DOMContentLoaded', () => {
    initReportsPage();
});

function initReportsPage() {
    // Pre-populate default date range (past 30 days)
    const today = new Date();
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(today.getDate() - 30);

    const fromInput = document.getElementById('reportDateFrom');
    const toInput = document.getElementById('reportDateTo');

    if (fromInput && toInput) {
        fromInput.value = thirtyDaysAgo.toISOString().split('T')[0];
        toInput.value = today.toISOString().split('T')[0];
    }

    loadReports();
}

async function loadReports() {
    const tbody = document.getElementById('reportsTableBody');
    try {
        const res = await ApiClient.get('/api/reports');
        if (!res.success || !res.data || res.data.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align:center; padding:36px; color:#94a3b8;">
                        <p>No reports generated yet. Click "Generate New Report" above to compile an executive audit.</p>
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = res.data.map(report => renderReportRow(report)).join('');
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="7" style="color:var(--danger); text-align:center; padding:20px;">Failed to load reports: ${err.message}</td></tr>`;
    }
}

function renderReportRow(r) {
    const formatBadge = r.fileFormat === 'CSV'
        ? '<span style="background:rgba(16,185,129,0.15); color:#10b981; border:1px solid rgba(16,185,129,0.3); font-weight:700; font-size:11px; padding:2px 8px; border-radius:4px;">CSV</span>'
        : '<span style="background:rgba(59,130,246,0.15); color:#60a5fa; border:1px solid rgba(59,130,246,0.3); font-weight:700; font-size:11px; padding:2px 8px; border-radius:4px;">PDF</span>';

    return `
        <tr>
            <td style="font-weight:700; color:var(--text-dark);">${r.title}</td>
            <td style="font-size:12px; color:var(--text-secondary);">${formatReportType(r.reportType)}</td>
            <td style="font-size:12px; color:var(--text-primary);">${formatDate(r.dateFrom)} → ${formatDate(r.dateTo)}</td>
            <td>${formatBadge}</td>
            <td><span class="status-badge status-connected">${r.status}</span></td>
            <td style="font-size:12px; color:var(--text-secondary);">${formatDate(r.createdAt)}</td>
            <td style="text-align:right;">
                <div style="display:inline-flex; gap:6px;">
                    <button class="btn btn-outline btn-sm" onclick="downloadReportCsv(${r.id})" style="font-weight:600; display:inline-flex; align-items:center; gap:5px;">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
                        <span>Download CSV</span>
                    </button>
                    <button class="btn btn-primary btn-sm" onclick="viewPrintableReport(${r.id})" style="font-weight:600; display:inline-flex; align-items:center; gap:5px;">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                        <span>View / Print</span>
                    </button>
                </div>
            </td>
        </tr>
    `;
}

function formatReportType(type) {
    switch (type) {
        case 'WEEKLY': return 'Weekly Performance';
        case 'MONTHLY': return 'Monthly Analytics';
        case 'COMPETITOR_COMPARISON': return 'Competitor Audit';
        case 'PLATFORM_PERFORMANCE': return 'Platform Breakdown';
        default: return type;
    }
}

function openReportModal() {
    document.getElementById('generateReportModal').classList.add('open');
}

function closeReportModal() {
    document.getElementById('generateReportModal').classList.remove('open');
}

async function handleReportGenerate(e) {
    e.preventDefault();
    const title = document.getElementById('reportTitle').value.trim();
    const reportType = document.getElementById('reportType').value;
    const dateFrom = document.getElementById('reportDateFrom').value;
    const dateTo = document.getElementById('reportDateTo').value;
    const fileFormat = document.getElementById('reportFormat').value;

    const btn = document.getElementById('compileReportBtn');
    btn.disabled = true;
    btn.textContent = 'Compiling analytics...';

    try {
        const res = await ApiClient.post('/api/reports', {
            title,
            reportType,
            dateFrom,
            dateTo,
            fileFormat
        });

        if (res.success) {
            showToast('Report successfully generated!', 'success');
            closeReportModal();
            document.getElementById('reportForm').reset();
            initReportsPage();
        }
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Compile Report';
    }
}

async function downloadReportCsv(id) {
    showToast('Preparing CSV download...', 'info');
    try {
        const token = ApiClient.getToken();
        const res = await fetch(`/api/reports/${id}/csv`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!res.ok) throw new Error('Download failed');

        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `spedo_report_${id}.csv`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        a.remove();
        showToast('CSV report downloaded!', 'success');
    } catch (err) {
        showToast('Download error: ' + err.message, 'error');
    }
}

async function viewPrintableReport(id) {
    const modal = document.getElementById('printableReportModal');
    const content = document.getElementById('printableReportContent');
    const titleHeader = document.getElementById('previewReportTitle');

    content.innerHTML = '<div style="text-align:center; padding:30px; color:#94a3b8;">Rendering report document...</div>';
    modal.classList.add('open');

    try {
        const res = await ApiClient.get(`/api/reports/${id}`);
        if (!res.success || !res.data) throw new Error('Report data unavailable');

        const report = res.data;
        currentViewingReport = report;
        titleHeader.textContent = report.title;

        let payload = {};
        try {
            payload = JSON.parse(report.reportDataJson || '{}');
        } catch (e) {}

        const summary = payload.summary || {};
        const platforms = payload.platforms || [];
        const competitors = payload.competitors || [];

        content.innerHTML = `
            <div style="font-family:inherit; color:#0f172a;">
                <!-- Printable Header -->
                <div style="display:flex; justify-content:space-between; align-items:flex-start; border-bottom:2px solid #0f172a; padding-bottom:16px; margin-bottom:24px;">
                    <div>
                        <div style="font-size:24px; font-weight:900; letter-spacing:-0.5px;">SPEDO ANALYTICS</div>
                        <div style="font-size:16px; font-weight:700; color:var(--primary-blue); margin-top:2px;">${report.title}</div>
                        <div style="font-size:12px; color:#64748b; margin-top:4px;">Period: ${formatDate(report.dateFrom)} to ${formatDate(report.dateTo)}</div>
                    </div>
                    <div style="text-align:right;">
                        <span style="font-size:11px; text-transform:uppercase; font-weight:700; color:#64748b;">Template:</span>
                        <div style="font-size:13px; font-weight:600;">${formatReportType(report.reportType)}</div>
                        <div style="font-size:11px; color:#94a3b8; margin-top:4px;">Generated: ${formatDate(report.createdAt)}</div>
                    </div>
                </div>

                <!-- Executive Summary -->
                <div style="margin-bottom:28px;">
                    <h4 style="font-size:14px; text-transform:uppercase; letter-spacing:0.8px; color:#64748b; margin-bottom:12px;">1. Executive Summary Telemetry</h4>
                    <div style="display:grid; grid-template-columns: repeat(4, 1fr); gap:12px;">
                        <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:12px;">
                            <div style="font-size:11px; color:#64748b;">Followers</div>
                            <div style="font-size:20px; font-weight:800; margin-top:2px;">${formatNumber(summary.totalFollowers)}</div>
                        </div>
                        <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:12px;">
                            <div style="font-size:11px; color:#64748b;">Avg Engagement</div>
                            <div style="font-size:20px; font-weight:800; color:var(--primary-violet); margin-top:2px;">${(summary.engagementRate || 0).toFixed(2)}%</div>
                        </div>
                        <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:12px;">
                            <div style="font-size:11px; color:#64748b;">Total Impressions</div>
                            <div style="font-size:20px; font-weight:800; margin-top:2px;">${formatNumber(summary.totalImpressions)}</div>
                        </div>
                        <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:12px;">
                            <div style="font-size:11px; color:#64748b;">Follower Growth Rate</div>
                            <div style="font-size:20px; font-weight:800; color:var(--success); margin-top:2px;">+${summary.followerGrowthPercentage || '0.0'}%</div>
                        </div>
                    </div>
                </div>

                <!-- Platform Breakdown Table -->
                <div style="margin-bottom:28px;">
                    <h4 style="font-size:14px; text-transform:uppercase; letter-spacing:0.8px; color:#64748b; margin-bottom:12px;">2. Multi-Channel Performance Matrix</h4>
                    <table style="width:100%; border-collapse:collapse; font-size:12px; border:1px solid #e2e8f0;">
                        <thead>
                            <tr style="background:#f1f5f9; text-align:left;">
                                <th style="padding:8px 12px; border:1px solid #e2e8f0;">Platform</th>
                                <th style="padding:8px 12px; border:1px solid #e2e8f0;">Followers</th>
                                <th style="padding:8px 12px; border:1px solid #e2e8f0;">Engagements</th>
                                <th style="padding:8px 12px; border:1px solid #e2e8f0;">Engagement Rate</th>
                                <th style="padding:8px 12px; border:1px solid #e2e8f0;">Share of Audience</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${platforms.map(p => `
                                <tr>
                                    <td style="padding:8px 12px; border:1px solid #e2e8f0; font-weight:600;">${p.platformName}</td>
                                    <td style="padding:8px 12px; border:1px solid #e2e8f0;">${formatNumber(p.followers)}</td>
                                    <td style="padding:8px 12px; border:1px solid #e2e8f0;">${formatNumber(p.totalEngagements)}</td>
                                    <td style="padding:8px 12px; border:1px solid #e2e8f0; font-weight:700; color:var(--primary-violet);">${(p.engagementRate || 0).toFixed(2)}%</td>
                                    <td style="padding:8px 12px; border:1px solid #e2e8f0;">${p.shareOfAudiencePercentage}%</td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>

                <!-- Competitor Ranking Table -->
                <div style="margin-bottom:20px;">
                    <h4 style="font-size:14px; text-transform:uppercase; letter-spacing:0.8px; color:#64748b; margin-bottom:12px;">3. Competitor Benchmarks & Share of Voice</h4>
                    <table style="width:100%; border-collapse:collapse; font-size:12px; border:1px solid #e2e8f0;">
                        <thead>
                            <tr style="background:#f1f5f9; text-align:left;">
                                <th style="padding:8px 12px; border:1px solid #e2e8f0;">Rank</th>
                                <th style="padding:8px 12px; border:1px solid #e2e8f0;">Brand Name</th>
                                <th style="padding:8px 12px; border:1px solid #e2e8f0;">Followers</th>
                                <th style="padding:8px 12px; border:1px solid #e2e8f0;">Posting Freq</th>
                                <th style="padding:8px 12px; border:1px solid #e2e8f0;">Avg Likes</th>
                                <th style="padding:8px 12px; border:1px solid #e2e8f0;">Engagement Rate</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${competitors.map(c => `
                                <tr style="${c.self ? 'background:rgba(67,97,238,0.06); font-weight:600;' : ''}">
                                    <td style="padding:8px 12px; border:1px solid #e2e8f0;">#${c.rank}</td>
                                    <td style="padding:8px 12px; border:1px solid #e2e8f0;">${c.name}</td>
                                    <td style="padding:8px 12px; border:1px solid #e2e8f0;">${formatNumber(c.followers)}</td>
                                    <td style="padding:8px 12px; border:1px solid #e2e8f0;">${c.postingFrequencyPerWeek || '—'}/wk</td>
                                    <td style="padding:8px 12px; border:1px solid #e2e8f0;">${formatNumber(c.avgLikes)}</td>
                                    <td style="padding:8px 12px; border:1px solid #e2e8f0; font-weight:700; color:var(--primary-violet);">${(c.engagementRate || 0).toFixed(2)}%</td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>

                <div style="font-size:11px; color:#94a3b8; text-align:center; margin-top:28px; border-top:1px solid #e2e8f0; padding-top:12px;">
                    Confidential & Proprietary — Generated by SPEDO Competitive Intelligence Platform.
                </div>
            </div>
        `;
    } catch (err) {
        content.innerHTML = `<div style="color:var(--danger); text-align:center; padding:20px;">Error rendering report: ${err.message}</div>`;
    }
}

function closePrintableModal() {
    document.getElementById('printableReportModal').classList.remove('open');
}

function printReport() {
    window.print();
}
