package com.spedo.analytics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spedo.analytics.dto.PlatformHeadToHeadDto;
import com.spedo.analytics.entity.*;
import com.spedo.analytics.repository.ReportRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ReportRepository reportRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String defaultHost;

    @Value("${spring.mail.port:587}")
    private int defaultPort;

    @Value("${spring.mail.username:}")
    private String defaultUsername;

    @Value("${spring.mail.password:}")
    private String defaultPassword;

    // In-memory overrides for live UI configuration
    private volatile String overrideHost;
    private volatile Integer overridePort;
    private volatile String overrideUsername;
    private volatile String overridePassword;

    public EmailService(ReportRepository reportRepository,
                        NotificationService notificationService,
                        ObjectMapper objectMapper) {
        this.reportRepository = reportRepository;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    public String getEffectiveHost() {
        return overrideHost != null ? overrideHost : defaultHost;
    }

    public int getEffectivePort() {
        return overridePort != null ? overridePort : defaultPort;
    }

    public String getEffectiveUsername() {
        return overrideUsername != null ? overrideUsername : defaultUsername;
    }

    public String getEffectivePassword() {
        return overridePassword != null ? overridePassword : defaultPassword;
    }

    public boolean isSmtpConfigured() {
        String u = getEffectiveUsername();
        String p = getEffectivePassword();
        return u != null && !u.isBlank() && p != null && !p.isBlank();
    }

    public Map<String, Object> getSmtpStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("host", getEffectiveHost());
        status.put("port", getEffectivePort());
        status.put("username", getEffectiveUsername());
        status.put("configured", isSmtpConfigured());
        return status;
    }

    public Map<String, Object> updateSmtpConfig(String host, String portStr, String username, String password) {
        if (host != null && !host.isBlank()) this.overrideHost = host.trim();
        if (portStr != null && !portStr.isBlank()) {
            try {
                this.overridePort = Integer.parseInt(portStr.trim());
            } catch (NumberFormatException ignored) {}
        }
        if (username != null) this.overrideUsername = username.trim();
        if (password != null && !password.isBlank()) this.overridePassword = password.trim();

        log.info("[SMTP CONFIG UPDATED] Host: {}, Port: {}, User: {}, Configured: {}",
                getEffectiveHost(), getEffectivePort(), getEffectiveUsername(), isSmtpConfigured());

        return getSmtpStatus();
    }

    public Map<String, Object> sendSmtpEmail(String recipientEmail, String subject, String htmlContent, String plainText) {
        Map<String, Object> result = new HashMap<>();
        if (!isSmtpConfigured()) {
            result.put("sent", false);
            result.put("status", "READY_FOR_DIRECT_DISPATCH");
            result.put("message", "SMTP credentials not configured yet. Dispatch via 1-Click Gmail Draft or configure SMTP credentials below.");
            return result;
        }

        try {
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setHost(getEffectiveHost());
            sender.setPort(getEffectivePort());
            sender.setUsername(getEffectiveUsername());
            sender.setPassword(getEffectivePassword());

            Properties props = sender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "false");
            props.put("mail.smtp.connectiontimeout", "8000");
            props.put("mail.smtp.timeout", "8000");
            props.put("mail.smtp.writetimeout", "8000");

            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(getEffectiveUsername(), "SPEDO Analytics");
            helper.setTo(recipientEmail);
            helper.setSubject(subject);

            if (plainText != null && !plainText.isBlank()) {
                helper.setText(plainText, htmlContent);
            } else {
                helper.setText(htmlContent, true);
            }

            sender.send(message);
            log.info("[SMTP SUCCESS] Dispatched report directly to {} via {}", recipientEmail, getEffectiveHost());
            result.put("sent", true);
            result.put("status", "SENT_VIA_SMTP");
            result.put("message", "Report successfully delivered to " + recipientEmail + " via SMTP (" + getEffectiveHost() + ")");
            return result;
        } catch (Exception ex) {
            log.error("[SMTP ERROR] Failed to send email to {}: {}", recipientEmail, ex.getMessage());
            result.put("sent", false);
            result.put("status", "SMTP_FAILED");
            result.put("message", "SMTP transmission failed: " + ex.getMessage());
            result.put("error", ex.getMessage());
            return result;
        }
    }

    public Map<String, Object> sendTestEmail(String testEmail) {
        String subject = "[SPEDO Analytics] Test SMTP Delivery Verification";
        String html = "<div style='font-family:sans-serif; padding:20px;'><h2 style='color:#4361ee;'>SPEDO Analytics SMTP Connection Test</h2><p>Your SMTP mail gateway is properly configured and successfully transmitting outbound messages.</p><p>Timestamp: " + LocalDateTime.now() + "</p></div>";
        String plain = "SPEDO Analytics SMTP Connection Test\n\nYour SMTP mail gateway is properly configured and successfully transmitting outbound messages.\nTimestamp: " + LocalDateTime.now();
        return sendSmtpEmail(testEmail, subject, html, plain);
    }

    public Map<String, Object> sendPasswordResetEmail(String recipientEmail, String recipientName, String resetCode, String resetUrl) {
        String subject = "[SPEDO Analytics] Password Reset Code: " + resetCode;

        String plainText = "Hello " + (recipientName != null ? recipientName : "User") + ",\n\n"
                + "We received a request to reset your password for your SPEDO Analytics account (" + recipientEmail + ").\n\n"
                + "YOUR 6-DIGIT VERIFICATION CODE:\n"
                + resetCode + "\n\n"
                + "Direct password reset link:\n"
                + resetUrl + "\n\n"
                + "This code will expire in 30 minutes. If you did not request this change, you can safely ignore this email.\n\n"
                + "Best regards,\n"
                + "The SPEDO Analytics Security Team";

        String htmlContent = "<!DOCTYPE html><html><head><meta charset='utf-8'></head>"
                + "<body style='font-family:-apple-system,BlinkMacSystemFont,\"Segoe UI\",Roboto,Helvetica,Arial,sans-serif; margin:0; padding:24px; background-color:#0f172a; color:#1e293b;'>"
                + "<div style='max-width:540px; margin:0 auto; background:#ffffff; border-radius:14px; overflow:hidden; box-shadow:0 10px 30px rgba(0,0,0,0.25); border:1px solid #e2e8f0;'>"
                + "<div style='background:linear-gradient(135deg, #090c13 0%, #0f172a 50%, #1e1b4b 100%); padding:28px; text-align:center; color:#ffffff; border-bottom:3px solid #00f2fe;'>"
                + "<div style='display:inline-block; width:34px; height:34px; background:#00f2fe; color:#090c13; font-weight:900; font-size:18px; line-height:34px; text-align:center; border-radius:8px; margin-right:8px;'>S</div>"
                + "<span style='font-size:22px; font-weight:900; letter-spacing:1px; color:#ffffff; vertical-align:middle;'>SPEDO ANALYTICS</span>"
                + "<div style='margin-top:8px;'><span style='background:rgba(0,242,254,0.15); border:1px solid rgba(0,242,254,0.35); color:#00f2fe; font-size:10.5px; font-weight:700; padding:3px 12px; border-radius:20px; text-transform:uppercase;'>Security &bull; Password Reset</span></div>"
                + "</div>"
                + "<div style='padding:28px 24px;'>"
                + "<h3 style='margin:0 0 10px 0; font-size:18px; color:#0f172a;'>Password Reset Verification</h3>"
                + "<p style='font-size:13.5px; color:#475569; line-height:1.6; margin:0 0 20px 0;'>Hello <strong>" + escapeHtml(recipientName != null ? recipientName : "User") + "</strong>, we received a request to reset your password for <strong>" + escapeHtml(recipientEmail) + "</strong>. Use the verification code below to set a new password:</p>"
                + "<div style='background:#f1f5f9; border:2px dashed #00f2fe; border-radius:10px; padding:20px; text-align:center; margin-bottom:24px;'>"
                + "<div style='font-size:11px; font-weight:700; color:#64748b; text-transform:uppercase; letter-spacing:1px; margin-bottom:6px;'>Your 6-Digit Verification Code</div>"
                + "<div style='font-size:32px; font-weight:900; letter-spacing:6px; color:#2563eb; font-family:monospace;'>" + resetCode + "</div>"
                + "<div style='font-size:11px; color:#94a3b8; margin-top:6px;'>Expires in 30 minutes</div>"
                + "</div>"
                + "<div style='text-align:center; margin-bottom:24px;'>"
                + "<a href='" + resetUrl + "' style='display:inline-block; background:linear-gradient(135deg, #00f2fe 0%, #2563eb 100%); color:#ffffff; text-decoration:none; padding:12px 28px; border-radius:8px; font-weight:700; font-size:13.5px;'>Reset Password Now &#8599;</a>"
                + "</div>"
                + "<p style='font-size:12px; color:#94a3b8; line-height:1.5; margin:0; border-top:1px solid #f1f5f9; padding-top:16px;'>If you did not request a password reset, you can safely ignore this email. Your current password remains secure.</p>"
                + "</div>"
                + "<div style='background:#f8fafc; padding:16px; text-align:center; font-size:11px; color:#94a3b8; border-top:1px solid #e2e8f0;'>"
                + "&copy; 2026 SPEDO Analytics Inc. All rights reserved."
                + "</div>"
                + "</div>"
                + "</body></html>";

        return sendSmtpEmail(recipientEmail, subject, htmlContent, plainText);
    }

    @Transactional
    public Map<String, Object> dispatchPlatformComparisonEmail(User user,
                                                               String recipientEmail,
                                                               PlatformHeadToHeadDto comparison,
                                                               String customNotes) {
        String targetEmail = (recipientEmail != null && !recipientEmail.isBlank())
                ? recipientEmail.trim()
                : user.getEmail();

        String titleA = (comparison.getChannelTitleA() != null && !comparison.getChannelTitleA().isBlank())
                ? comparison.getChannelTitleA()
                : comparison.getPlatformAName();
        String titleB = (comparison.getChannelTitleB() != null && !comparison.getChannelTitleB().isBlank())
                ? comparison.getChannelTitleB()
                : comparison.getPlatformBName();

        String subject = String.format("[SPEDO Analytics] Head-to-Head Comparative Benchmark: %s vs %s",
                titleA, titleB);

        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss"));
        comparison.setRecipientEmail(targetEmail);
        comparison.setGeneratedAt(formattedDate);

        // Generate responsive HTML email briefing
        String emailHtml = generateEmailHtml(user, targetEmail, comparison, customNotes, formattedDate, titleA, titleB);

        // Generate clean plain text summary for Gmail web compose and mailto
        String plainTextSummary = generatePlainTextSummary(user, targetEmail, comparison, customNotes, formattedDate, titleA, titleB);

        // Persist report entity
        Report report = new Report(
                user,
                String.format("Comparative Benchmark: %s vs %s", titleA, titleB),
                ReportType.COMPETITOR_COMPARISON,
                LocalDate.now().minusDays(30),
                LocalDate.now(),
                ReportFormat.PDF
        );

        try {
            report.setReportDataJson(objectMapper.writeValueAsString(comparison));
        } catch (Exception e) {
            report.setReportDataJson("{}");
        }
        report.setDownloadUrl("/api/reports");
        Report savedReport = reportRepository.save(report);

        // Record in-app notification
        try {
            notificationService.createNotification(
                    user,
                    "Comparative Report Generated",
                    String.format("Head-to-head comparison of %s vs %s prepared for %s",
                            titleA, titleB, targetEmail),
                    NotificationType.REPORT_GENERATED,
                    "/reports.html"
            );
        } catch (Exception e) {
            log.warn("Failed to create in-app notification: {}", e.getMessage());
        }

        // Attempt live SMTP transmission
        Map<String, Object> smtpResult = sendSmtpEmail(targetEmail, subject, emailHtml, plainTextSummary);
        String deliveryStatus = (String) smtpResult.get("status");
        String deliveryMessage = (String) smtpResult.get("message");

        // Construct 1-click Webmail & Mailto URLs
        String encodedTo = urlEncode(targetEmail);
        String encodedSubject = urlEncode(subject);
        String encodedBody = urlEncode(plainTextSummary);

        String gmailComposeUrl = "https://mail.google.com/mail/?view=cm&fs=1&to=" + encodedTo
                + "&su=" + encodedSubject
                + "&body=" + encodedBody;

        String mailtoUrl = "mailto:" + encodedTo
                + "?subject=" + encodedSubject
                + "&body=" + encodedBody;

        // Audit Logging
        log.info("================================================================================");
        log.info("[EMAIL DISPATCH GATEWAY] Benchmark Dossier Processed");
        log.info("Recipient       : {}", targetEmail);
        log.info("Subject         : {}", subject);
        log.info("User            : {} ({})", user.getId(), user.getFullName());
        log.info("Matchup         : {} vs {}", titleA, titleB);
        log.info("Timestamp       : {}", formattedDate);
        log.info("Report ID       : {}", savedReport.getId());
        log.info("Delivery Status : {}", deliveryStatus);
        log.info("Delivery Note   : {}", deliveryMessage);
        log.info("================================================================================");

        Map<String, Object> result = new HashMap<>();
        result.put("recipientEmail", targetEmail);
        result.put("subject", subject);
        result.put("deliveryStatus", deliveryStatus);
        result.put("deliveryMessage", deliveryMessage);
        result.put("smtpConfigured", isSmtpConfigured());
        result.put("sentAt", formattedDate);
        result.put("platformA", titleA);
        result.put("platformB", titleB);
        result.put("reportId", savedReport.getId());
        result.put("previewHtml", emailHtml);
        result.put("plainTextSummary", plainTextSummary);
        result.put("gmailComposeUrl", gmailComposeUrl);
        result.put("mailtoUrl", mailtoUrl);
        result.put("comparison", comparison);

        return result;
    }

    private String urlEncode(String text) {
        if (text == null) return "";
        try {
            return URLEncoder.encode(text, StandardCharsets.UTF_8).replace("+", "%20");
        } catch (Exception e) {
            return text;
        }
    }

    private String generatePlainTextSummary(User user,
                                            String recipientEmail,
                                            PlatformHeadToHeadDto data,
                                            String customNotes,
                                            String timestamp,
                                            String titleA,
                                            String titleB) {
        StringBuilder sb = new StringBuilder();
        sb.append("🎨 [TIP: The full-color visual cards report is on your clipboard! Press Ctrl+V (or Right Click -> Paste) here in Gmail to insert the rich color cards.]\n\n");
        sb.append("SPEDO ANALYTICS - EXECUTIVE COMPARATIVE BENCHMARK\n");
        sb.append("============================================================\n");
        sb.append("Generated For : ").append(user.getFullName()).append(" (").append(recipientEmail).append(")\n");
        sb.append("Dispatched At : ").append(timestamp).append("\n");
        sb.append("Matchup       : ").append(titleA).append(" (").append(data.getPlatformAName()).append(") vs ")
                .append(titleB).append(" (").append(data.getPlatformBName()).append(")\n\n");

        if (customNotes != null && !customNotes.trim().isEmpty()) {
            sb.append("AUDIT NOTE:\n").append(customNotes).append("\n\n");
        }

        sb.append("EXECUTIVE SUMMARY & KEY TAKEAWAYS:\n");
        sb.append(data.getExecutiveSummary() != null ? data.getExecutiveSummary() : "Comprehensive head-to-head performance audit.").append("\n\n");

        sb.append("HEAD-TO-HEAD METRIC BREAKDOWN:\n");
        sb.append("------------------------------------------------------------\n");
        sb.append(String.format("1. Audience / Followers:\n   * %s: %s\n   * %s: %s\n   >> Leader: %s\n\n",
                titleA, formatNumber(data.getFollowersA()),
                titleB, formatNumber(data.getFollowersB()),
                data.getAudienceLeader()));

        sb.append(String.format("2. Engagement Rate:\n   * %s: %s%%\n   * %s: %s%%\n   >> Leader: %s\n\n",
                titleA, data.getEngagementRateA() != null ? data.getEngagementRateA().toPlainString() : "0.00",
                titleB, data.getEngagementRateB() != null ? data.getEngagementRateB().toPlainString() : "0.00",
                data.getEngagementLeader()));

        sb.append(String.format("3. Content Posts / Velocity:\n   * %s: %d posts\n   * %s: %d posts\n   >> Leader: %s\n\n",
                titleA, data.getPostsCountA(),
                titleB, data.getPostsCountB(),
                data.getVelocityLeader()));

        sb.append(String.format("4. Audience Reach / Views:\n   * %s: %s\n   * %s: %s\n   >> Leader: %s\n\n",
                titleA, formatNumber(data.getViewsOrReachA()),
                titleB, formatNumber(data.getViewsOrReachB()),
                data.getViewsOrReachA() >= data.getViewsOrReachB() ? titleA : titleB));

        sb.append(String.format("5. Audience Sentiment (Positive):\n   * %s: %.1f%%\n   * %s: %.1f%%\n   >> Leader: %s\n\n",
                titleA, data.getSentimentPositiveA(),
                titleB, data.getSentimentPositiveB(),
                data.getSentimentLeader()));

        if (data.getRecommendations() != null && !data.getRecommendations().isEmpty()) {
            sb.append("STRATEGIC RECOMMENDATIONS:\n");
            int idx = 1;
            for (String rec : data.getRecommendations()) {
                sb.append(idx++).append(". ").append(rec).append("\n");
            }
            sb.append("\n");
        }

        sb.append("------------------------------------------------------------\n");
        sb.append("Access Live Interactive Dashboards & Competitor Intel:\n");
        sb.append("http://localhost:8080/competitors.html\n");
        sb.append("============================================================\n");
        sb.append("(c) 2026 SPEDO Analytics Inc. All rights reserved.\n");

        return sb.toString();
    }

    private String generateEmailHtml(User user,
                                     String recipientEmail,
                                     PlatformHeadToHeadDto data,
                                     String customNotes,
                                     String timestamp,
                                     String titleA,
                                     String titleB) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='utf-8'>");
        sb.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        sb.append("<title>SPEDO Executive Channel Intelligence Dossier</title>");
        sb.append("</head><body style='font-family:-apple-system,BlinkMacSystemFont,\"Segoe UI\",Roboto,Helvetica,Arial,sans-serif; margin:0; padding:20px; background-color:#0f172a; color:#1e293b;'>");

        // Outer Container
        sb.append("<div style='max-width:720px; margin:0 auto; background:#ffffff; border-radius:16px; overflow:hidden; box-shadow:0 12px 36px rgba(0,0,0,0.25); border:1px solid #e2e8f0;'>");

        // Header Banner with Neon Gradient
        sb.append("<div style='background:linear-gradient(135deg, #090c13 0%, #0f172a 45%, #1e1b4b 100%); padding:32px 28px; text-align:center; color:#ffffff; border-bottom:3px solid #00f2fe;'>");
        sb.append("<div style='display:inline-block; vertical-align:middle; width:34px; height:34px; background:#00f2fe; color:#090c13; font-weight:900; font-size:18px; line-height:34px; text-align:center; border-radius:8px; margin-right:10px; box-shadow:0 0 14px rgba(0,242,254,0.6);'>S</div>");
        sb.append("<span style='font-size:24px; font-weight:900; letter-spacing:1px; color:#ffffff; vertical-align:middle;'>SPEDO ANALYTICS</span>");
        sb.append("<div style='margin-top:10px;'><span style='background:rgba(0,242,254,0.15); border:1px solid rgba(0,242,254,0.35); color:#00f2fe; font-size:11px; font-weight:700; padding:4px 14px; border-radius:20px; text-transform:uppercase; letter-spacing:0.8px;'>Executive Channel Intelligence Dossier</span></div>");
        sb.append("</div>");

        // Body Container
        sb.append("<div style='padding:28px 24px; background:#ffffff;'>");

        // Greeting & Subject Subtitle
        sb.append("<div style='font-size:18px; font-weight:800; color:#0f172a; margin-bottom:6px;'>Hello ").append(escapeHtml(user.getFullName())).append(",</div>");
        sb.append("<p style='font-size:13.5px; color:#64748b; line-height:1.6; margin:0 0 20px 0;'>Here is your full-color head-to-head performance audit comparing <strong>")
                .append(escapeHtml(titleA)).append(" (").append(escapeHtml(data.getPlatformAName())).append(")</strong> and <strong>")
                .append(escapeHtml(titleB)).append(" (").append(escapeHtml(data.getPlatformBName())).append(")</strong>. Prepared for: <strong style='color:#2563eb;'>")
                .append(escapeHtml(recipientEmail)).append("</strong>.</p>");

        if (customNotes != null && !customNotes.trim().isEmpty()) {
            sb.append("<div style='background:#fef3c7; border-left:4px solid #f59e0b; padding:12px 16px; border-radius:6px; font-size:13px; color:#92400e; margin-bottom:24px;'><strong>Audit Note:</strong> ").append(escapeHtml(customNotes)).append("</div>");
        }

        // Platform Setup Details
        String borderA = getPlatformBorder(data.getPlatformA());
        String bgA = getPlatformBg(data.getPlatformA());
        String colorA = getPlatformColor(data.getPlatformA());
        String iconA = getPlatformIcon(data.getPlatformA());

        String borderB = getPlatformBorder(data.getPlatformB());
        String bgB = getPlatformBg(data.getPlatformB());
        String colorB = getPlatformColor(data.getPlatformB());
        String iconB = getPlatformIcon(data.getPlatformB());

        boolean isAudienceWinnerA = data.getAudienceLeader() != null && data.getAudienceLeader().contains(titleA);
        boolean isAudienceWinnerB = data.getAudienceLeader() != null && data.getAudienceLeader().contains(titleB);

        boolean isEngageWinnerA = data.getEngagementLeader() != null && data.getEngagementLeader().contains(titleA);
        boolean isEngageWinnerB = data.getEngagementLeader() != null && data.getEngagementLeader().contains(titleB);

        boolean isVelocityWinnerA = data.getVelocityLeader() != null && data.getVelocityLeader().contains(titleA);
        boolean isVelocityWinnerB = data.getVelocityLeader() != null && data.getVelocityLeader().contains(titleB);

        boolean isSentimentWinnerA = data.getSentimentLeader() != null && data.getSentimentLeader().contains(titleA);
        boolean isSentimentWinnerB = data.getSentimentLeader() != null && data.getSentimentLeader().contains(titleB);

        // =========================================================================
        // SIDE-BY-SIDE DUAL PLATFORM CARDS (Matching UI screenshot!)
        // =========================================================================
        sb.append("<table style='width:100%; border-collapse:collapse; margin-bottom:28px;' cellpadding='0' cellspacing='0'>");
        sb.append("<tr>");

        // CARD A
        sb.append("<td style='width:50%; vertical-align:top; padding-right:8px;'>");
        sb.append("<div style='background:#ffffff; border:2px solid ").append(borderA).append("; border-radius:12px; padding:18px; box-shadow:0 4px 14px rgba(0,0,0,0.04);'>");
        
        // Card A Header
        sb.append("<table style='width:100%; border-collapse:collapse; margin-bottom:14px; padding-bottom:12px; border-bottom:1px solid #f1f5f9;'><tr>");
        sb.append("<td style='width:36px; vertical-align:middle;'>");
        sb.append("<div style='width:34px; height:34px; border-radius:8px; background:").append(bgA).append("; color:").append(colorA).append("; text-align:center; line-height:34px; font-size:16px; font-weight:bold;'>").append(iconA).append("</div>");
        sb.append("</td><td style='vertical-align:middle; padding-left:10px;'>");
        sb.append("<div style='font-size:15px; font-weight:800; color:#0f172a; line-height:1.2;'>").append(escapeHtml(titleA)).append("</div>");
        sb.append("<div style='font-size:11px; color:#64748b; margin-top:2px;'>");
        sb.append("<span style='background:").append(bgA).append("; color:").append(colorA).append("; font-weight:700; padding:1px 6px; border-radius:4px;'>").append(escapeHtml(data.getPlatformAName())).append("</span> ");
        sb.append(escapeHtml(data.getHandleA() != null ? data.getHandleA() : "@verified"));
        sb.append("</div></td><td style='text-align:right; vertical-align:top;'>");
        if (data.isConnectedA()) {
            sb.append("<span style='background:#dcfce7; color:#15803d; font-size:10px; font-weight:800; padding:2px 7px; border-radius:4px;'>CONNECTED</span>");
        } else {
            sb.append("<span style='background:#f1f5f9; color:#64748b; font-size:10px; font-weight:700; padding:2px 7px; border-radius:4px;'>PUBLIC</span>");
        }
        sb.append("</td></tr></table>");

        // Card A 4-Tile Grid
        sb.append("<table style='width:100%; border-collapse:separate; border-spacing:6px; margin-bottom:14px;' cellpadding='0' cellspacing='0'>");
        sb.append("<tr>");
        sb.append("<td style='background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:10px; width:50%;'>");
        sb.append("<div style='font-size:9.5px; font-weight:700; color:#64748b; text-transform:uppercase;'>Subscribers / Audience</div>");
        sb.append("<div style='font-size:19px; font-weight:900; color:#0f172a; margin-top:3px;'>").append(formatNumber(data.getFollowersA()));
        if (isAudienceWinnerA) sb.append(" <span style='background:#dcfce7; color:#15803d; font-size:9px; font-weight:800; padding:1px 5px; border-radius:4px;'>&#128081; LEADER</span>");
        sb.append("</div></td>");
        sb.append("<td style='background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:10px; width:50%;'>");
        sb.append("<div style='font-size:9.5px; font-weight:700; color:#64748b; text-transform:uppercase;'>Engagement Rate</div>");
        sb.append("<div style='font-size:19px; font-weight:900; color:#ea580c; margin-top:3px;'>").append(data.getEngagementRateA() != null ? data.getEngagementRateA().toPlainString() : "0.00").append("%");
        if (isEngageWinnerA) sb.append(" <span style='background:#ffedd5; color:#c2410c; font-size:9px; font-weight:800; padding:1px 5px; border-radius:4px;'>&#128081; HIGHER</span>");
        sb.append("</div></td></tr><tr>");
        sb.append("<td style='background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:10px; width:50%;'>");
        sb.append("<div style='font-size:9.5px; font-weight:700; color:#64748b; text-transform:uppercase;'>Published Videos / Posts</div>");
        sb.append("<div style='font-size:19px; font-weight:900; color:#0f172a; margin-top:3px;'>").append(data.getPostsCountA());
        if (isVelocityWinnerA) sb.append(" <span style='background:#e0f2fe; color:#0369a1; font-size:9px; font-weight:800; padding:1px 5px; border-radius:4px;'>&#128081; VELOCITY</span>");
        sb.append("</div></td>");
        sb.append("<td style='background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:10px; width:50%;'>");
        sb.append("<div style='font-size:9.5px; font-weight:700; color:#64748b; text-transform:uppercase;'>Total Lifetime Views</div>");
        sb.append("<div style='font-size:19px; font-weight:900; color:#0f172a; margin-top:3px;'>").append(formatNumber(data.getViewsOrReachA())).append("</div>");
        sb.append("</td></tr></table>");

        // Card A Sentiment Bar
        sb.append("<div style='background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:10px; margin-bottom:12px;'>");
        sb.append("<div style='display:flex; justify-content:space-between; font-size:11px; margin-bottom:6px;'>");
        sb.append("<span style='font-weight:700; color:#475569;'>Audience & Comment Sentiment</span>");
        sb.append("<span style='font-weight:700; color:#10b981;'>").append(String.format("%.1f%% Positive", data.getSentimentPositiveA()));
        if (isSentimentWinnerA) sb.append(" &#127942;");
        sb.append("</span></div>");
        sb.append("<table style='width:100%; border-collapse:collapse; height:8px; border-radius:4px; overflow:hidden;' cellpadding='0' cellspacing='0'><tr>");
        sb.append("<td style='width:").append(Math.max(2, (int)data.getSentimentPositiveA())).append("%; background:#10b981;'></td>");
        sb.append("<td style='width:").append(Math.max(2, (int)data.getSentimentNeutralA())).append("%; background:#94a3b8;'></td>");
        sb.append("<td style='width:").append(Math.max(2, (int)data.getSentimentNegativeA())).append("%; background:#ef4444;'></td>");
        sb.append("</tr></table>");
        sb.append("<div style='font-size:10px; color:#64748b; margin-top:6px;'>");
        sb.append("<span style='color:#10b981; font-weight:700;'>&#9679; ").append(String.format("%.1f%%", data.getSentimentPositiveA())).append(" Pos</span> &nbsp; ");
        sb.append("<span style='color:#64748b; font-weight:700;'>&#9679; ").append(String.format("%.1f%%", data.getSentimentNeutralA())).append(" Neu</span> &nbsp; ");
        sb.append("<span style='color:#ef4444; font-weight:700;'>&#9679; ").append(String.format("%.1f%%", data.getSentimentNegativeA())).append(" Neg</span>");
        sb.append("</div></div>");

        // Card A Strengths
        if (data.getStrengthsA() != null && !data.getStrengthsA().isEmpty()) {
            sb.append("<div style='font-size:11px; font-weight:700; color:#475569; text-transform:uppercase; margin-bottom:4px;'>Platform Strengths</div>");
            sb.append("<ul style='margin:0; padding-left:16px; font-size:11.5px; color:#334155; line-height:1.5;'>");
            for (String str : data.getStrengthsA()) {
                sb.append("<li>").append(escapeHtml(str)).append("</li>");
            }
            sb.append("</ul>");
        }

        sb.append("</div></td>");

        // CARD B
        sb.append("<td style='width:50%; vertical-align:top; padding-left:8px;'>");
        sb.append("<div style='background:#ffffff; border:2px solid ").append(borderB).append("; border-radius:12px; padding:18px; box-shadow:0 4px 14px rgba(0,0,0,0.04);'>");
        
        // Card B Header
        sb.append("<table style='width:100%; border-collapse:collapse; margin-bottom:14px; padding-bottom:12px; border-bottom:1px solid #f1f5f9;'><tr>");
        sb.append("<td style='width:36px; vertical-align:middle;'>");
        sb.append("<div style='width:34px; height:34px; border-radius:8px; background:").append(bgB).append("; color:").append(colorB).append("; text-align:center; line-height:34px; font-size:16px; font-weight:bold;'>").append(iconB).append("</div>");
        sb.append("</td><td style='vertical-align:middle; padding-left:10px;'>");
        sb.append("<div style='font-size:15px; font-weight:800; color:#0f172a; line-height:1.2;'>").append(escapeHtml(titleB)).append("</div>");
        sb.append("<div style='font-size:11px; color:#64748b; margin-top:2px;'>");
        sb.append("<span style='background:").append(bgB).append("; color:").append(colorB).append("; font-weight:700; padding:1px 6px; border-radius:4px;'>").append(escapeHtml(data.getPlatformBName())).append("</span> ");
        sb.append(escapeHtml(data.getHandleB() != null ? data.getHandleB() : "@benchmark"));
        sb.append("</div></td><td style='text-align:right; vertical-align:top;'>");
        if (data.isConnectedB()) {
            sb.append("<span style='background:#dcfce7; color:#15803d; font-size:10px; font-weight:800; padding:2px 7px; border-radius:4px;'>CONNECTED</span>");
        } else {
            sb.append("<span style='background:#f1f5f9; color:#64748b; font-size:10px; font-weight:700; padding:2px 7px; border-radius:4px;'>PUBLIC</span>");
        }
        sb.append("</td></tr></table>");

        // Card B 4-Tile Grid
        sb.append("<table style='width:100%; border-collapse:separate; border-spacing:6px; margin-bottom:14px;' cellpadding='0' cellspacing='0'>");
        sb.append("<tr>");
        sb.append("<td style='background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:10px; width:50%;'>");
        sb.append("<div style='font-size:9.5px; font-weight:700; color:#64748b; text-transform:uppercase;'>Subscribers / Audience</div>");
        sb.append("<div style='font-size:19px; font-weight:900; color:#0f172a; margin-top:3px;'>").append(formatNumber(data.getFollowersB()));
        if (isAudienceWinnerB) sb.append(" <span style='background:#dcfce7; color:#15803d; font-size:9px; font-weight:800; padding:1px 5px; border-radius:4px;'>&#128081; LEADER</span>");
        sb.append("</div></td>");
        sb.append("<td style='background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:10px; width:50%;'>");
        sb.append("<div style='font-size:9.5px; font-weight:700; color:#64748b; text-transform:uppercase;'>Engagement Rate</div>");
        sb.append("<div style='font-size:19px; font-weight:900; color:#ea580c; margin-top:3px;'>").append(data.getEngagementRateB() != null ? data.getEngagementRateB().toPlainString() : "0.00").append("%");
        if (isEngageWinnerB) sb.append(" <span style='background:#ffedd5; color:#c2410c; font-size:9px; font-weight:800; padding:1px 5px; border-radius:4px;'>&#128081; HIGHER</span>");
        sb.append("</div></td></tr><tr>");
        sb.append("<td style='background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:10px; width:50%;'>");
        sb.append("<div style='font-size:9.5px; font-weight:700; color:#64748b; text-transform:uppercase;'>Published Videos / Posts</div>");
        sb.append("<div style='font-size:19px; font-weight:900; color:#0f172a; margin-top:3px;'>").append(data.getPostsCountB());
        if (isVelocityWinnerB) sb.append(" <span style='background:#e0f2fe; color:#0369a1; font-size:9px; font-weight:800; padding:1px 5px; border-radius:4px;'>&#128081; VELOCITY</span>");
        sb.append("</div></td>");
        sb.append("<td style='background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:10px; width:50%;'>");
        sb.append("<div style='font-size:9.5px; font-weight:700; color:#64748b; text-transform:uppercase;'>Total Lifetime Views</div>");
        sb.append("<div style='font-size:19px; font-weight:900; color:#0f172a; margin-top:3px;'>").append(formatNumber(data.getViewsOrReachB())).append("</div>");
        sb.append("</td></tr></table>");

        // Card B Sentiment Bar
        sb.append("<div style='background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:10px; margin-bottom:12px;'>");
        sb.append("<div style='display:flex; justify-content:space-between; font-size:11px; margin-bottom:6px;'>");
        sb.append("<span style='font-weight:700; color:#475569;'>Audience & Comment Sentiment</span>");
        sb.append("<span style='font-weight:700; color:#10b981;'>").append(String.format("%.1f%% Positive", data.getSentimentPositiveB()));
        if (isSentimentWinnerB) sb.append(" &#127942;");
        sb.append("</span></div>");
        sb.append("<table style='width:100%; border-collapse:collapse; height:8px; border-radius:4px; overflow:hidden;' cellpadding='0' cellspacing='0'><tr>");
        sb.append("<td style='width:").append(Math.max(2, (int)data.getSentimentPositiveB())).append("%; background:#10b981;'></td>");
        sb.append("<td style='width:").append(Math.max(2, (int)data.getSentimentNeutralB())).append("%; background:#94a3b8;'></td>");
        sb.append("<td style='width:").append(Math.max(2, (int)data.getSentimentNegativeB())).append("%; background:#ef4444;'></td>");
        sb.append("</tr></table>");
        sb.append("<div style='font-size:10px; color:#64748b; margin-top:6px;'>");
        sb.append("<span style='color:#10b981; font-weight:700;'>&#9679; ").append(String.format("%.1f%%", data.getSentimentPositiveB())).append(" Pos</span> &nbsp; ");
        sb.append("<span style='color:#64748b; font-weight:700;'>&#9679; ").append(String.format("%.1f%%", data.getSentimentNeutralB())).append(" Neu</span> &nbsp; ");
        sb.append("<span style='color:#ef4444; font-weight:700;'>&#9679; ").append(String.format("%.1f%%", data.getSentimentNegativeB())).append(" Neg</span>");
        sb.append("</div></div>");

        // Card B Strengths
        if (data.getStrengthsB() != null && !data.getStrengthsB().isEmpty()) {
            sb.append("<div style='font-size:11px; font-weight:700; color:#475569; text-transform:uppercase; margin-bottom:4px;'>Platform Strengths</div>");
            sb.append("<ul style='margin:0; padding-left:16px; font-size:11.5px; color:#334155; line-height:1.5;'>");
            for (String str : data.getStrengthsB()) {
                sb.append("<li>").append(escapeHtml(str)).append("</li>");
            }
            sb.append("</ul>");
        }

        sb.append("</div></td>");

        sb.append("</tr></table>");

        // =========================================================================
        // HEAD-TO-HEAD COMPARATIVE MATRIX TABLE
        // =========================================================================
        sb.append("<div style='font-size:14px; font-weight:800; color:#0f172a; margin-bottom:10px; text-transform:uppercase; letter-spacing:0.5px;'>&#128202; Standardized Metric Benchmark</div>");
        sb.append("<table style='width:100%; border-collapse:collapse; margin-bottom:28px; border:1px solid #e2e8f0; border-radius:10px; overflow:hidden;' cellpadding='0' cellspacing='0'>");
        sb.append("<thead><tr style='background:#f1f5f9; color:#475569; text-transform:uppercase; font-size:11px; font-weight:800;'>");
        sb.append("<th style='padding:12px 14px; text-align:left; border-bottom:1px solid #e2e8f0;'>Metric Dimension</th>");
        sb.append("<th style='padding:12px 14px; text-align:left; border-bottom:1px solid #e2e8f0;'>").append(escapeHtml(titleA)).append("</th>");
        sb.append("<th style='padding:12px 14px; text-align:left; border-bottom:1px solid #e2e8f0;'>").append(escapeHtml(titleB)).append("</th>");
        sb.append("<th style='padding:12px 14px; text-align:left; border-bottom:1px solid #e2e8f0;'>Market Leader</th>");
        sb.append("</tr></thead><tbody>");

        // Row 1
        sb.append("<tr style='border-bottom:1px solid #f1f5f9;'>");
        sb.append("<td style='padding:11px 14px; font-size:12.5px; color:#334155; font-weight:600;'>Audience / Followers</td>");
        sb.append("<td style='padding:11px 14px; font-size:13px; font-weight:700; color:#0f172a;'>").append(formatNumber(data.getFollowersA())).append("</td>");
        sb.append("<td style='padding:11px 14px; font-size:13px; font-weight:700; color:#0f172a;'>").append(formatNumber(data.getFollowersB())).append("</td>");
        sb.append("<td style='padding:11px 14px; font-size:12.5px; font-weight:700; color:#15803d; background:#f0fdf4;'>").append(escapeHtml(data.getAudienceLeader())).append("</td>");
        sb.append("</tr>");

        // Row 2
        sb.append("<tr style='border-bottom:1px solid #f1f5f9; background:#fafafa;'>");
        sb.append("<td style='padding:11px 14px; font-size:12.5px; color:#334155; font-weight:600;'>Engagement Rate</td>");
        sb.append("<td style='padding:11px 14px; font-size:13px; font-weight:800; color:#ea580c;'>").append(data.getEngagementRateA() != null ? data.getEngagementRateA().toPlainString() : "0.00").append("%</td>");
        sb.append("<td style='padding:11px 14px; font-size:13px; font-weight:800; color:#ea580c;'>").append(data.getEngagementRateB() != null ? data.getEngagementRateB().toPlainString() : "0.00").append("%</td>");
        sb.append("<td style='padding:11px 14px; font-size:12.5px; font-weight:700; color:#15803d; background:#f0fdf4;'>").append(escapeHtml(data.getEngagementLeader())).append("</td>");
        sb.append("</tr>");

        // Row 3
        sb.append("<tr style='border-bottom:1px solid #f1f5f9;'>");
        sb.append("<td style='padding:11px 14px; font-size:12.5px; color:#334155; font-weight:600;'>Content Volume (Posts/Videos)</td>");
        sb.append("<td style='padding:11px 14px; font-size:13px; font-weight:700; color:#0f172a;'>").append(data.getPostsCountA()).append("</td>");
        sb.append("<td style='padding:11px 14px; font-size:13px; font-weight:700; color:#0f172a;'>").append(data.getPostsCountB()).append("</td>");
        sb.append("<td style='padding:11px 14px; font-size:12.5px; font-weight:700; color:#15803d; background:#f0fdf4;'>").append(escapeHtml(data.getVelocityLeader())).append("</td>");
        sb.append("</tr>");

        // Row 4
        sb.append("<tr style='border-bottom:1px solid #f1f5f9; background:#fafafa;'>");
        sb.append("<td style='padding:11px 14px; font-size:12.5px; color:#334155; font-weight:600;'>Lifetime Reach / Views</td>");
        sb.append("<td style='padding:11px 14px; font-size:13px; font-weight:700; color:#0f172a;'>").append(formatNumber(data.getViewsOrReachA())).append("</td>");
        sb.append("<td style='padding:11px 14px; font-size:13px; font-weight:700; color:#0f172a;'>").append(formatNumber(data.getViewsOrReachB())).append("</td>");
        sb.append("<td style='padding:11px 14px; font-size:12.5px; font-weight:700; color:#15803d; background:#f0fdf4;'>")
                .append(data.getViewsOrReachA() >= data.getViewsOrReachB() ? escapeHtml(titleA) + " &#127942;" : escapeHtml(titleB) + " &#127942;").append("</td>");
        sb.append("</tr>");

        // Row 5
        sb.append("<tr>");
        sb.append("<td style='padding:11px 14px; font-size:12.5px; color:#334155; font-weight:600;'>Positive Sentiment</td>");
        sb.append("<td style='padding:11px 14px; font-size:13px; font-weight:700; color:#10b981;'>").append(String.format("%.1f%%", data.getSentimentPositiveA())).append("</td>");
        sb.append("<td style='padding:11px 14px; font-size:13px; font-weight:700; color:#10b981;'>").append(String.format("%.1f%%", data.getSentimentPositiveB())).append("</td>");
        sb.append("<td style='padding:11px 14px; font-size:12.5px; font-weight:700; color:#15803d; background:#f0fdf4;'>").append(escapeHtml(data.getSentimentLeader())).append("</td>");
        sb.append("</tr>");

        sb.append("</tbody></table>");

        // Executive Takeaway Box
        sb.append("<div style='background:#f0f7ff; border:1px solid #bfdbfe; border-left:4px solid #2563eb; border-radius:10px; padding:18px; margin-bottom:24px;'>");
        sb.append("<div style='font-size:13px; font-weight:800; color:#1e40af; margin-bottom:6px; text-transform:uppercase;'>&#9889; Executive Intelligence Takeaway</div>");
        sb.append("<div style='font-size:13px; line-height:1.6; color:#1e3a8a;'>").append(escapeHtml(data.getExecutiveSummary())).append("</div>");
        sb.append("</div>");

        // Strategic Recommendations
        if (data.getRecommendations() != null && !data.getRecommendations().isEmpty()) {
            sb.append("<div style='margin-bottom:26px;'>");
            sb.append("<div style='font-size:13px; font-weight:800; color:#0f172a; margin-bottom:10px; text-transform:uppercase;'>&#127919; Recommended Action Items</div>");
            sb.append("<ol style='margin:0; padding-left:20px; line-height:1.7; font-size:13px; color:#334155;'>");
            for (String rec : data.getRecommendations()) {
                sb.append("<li style='margin-bottom:6px;'>").append(escapeHtml(rec)).append("</li>");
            }
            sb.append("</ol></div>");
        }

        // Call to action button
        sb.append("<div style='text-align:center; margin-top:32px; margin-bottom:16px;'>");
        sb.append("<a href='http://localhost:8080/competitors.html' style='display:inline-block; background:linear-gradient(135deg, #00f2fe 0%, #2563eb 100%); color:#ffffff; text-decoration:none; padding:13px 32px; border-radius:8px; font-weight:800; font-size:13.5px; box-shadow:0 4px 14px rgba(37,99,235,0.4);'>Open SPEDO Intelligence Matrix &#8599;</a>");
        sb.append("</div>");

        sb.append("</div>"); // email-body

        // Footer
        sb.append("<div style='background:#f8fafc; padding:22px 28px; border-top:1px solid #e2e8f0; text-align:center; font-size:11.5px; color:#94a3b8; line-height:1.5;'>");
        sb.append("<div>Report compiled automatically by SPEDO Social Media Analytics &amp; Intelligence Engine</div>");
        sb.append("<div style='margin-top:4px;'>Dispatched to ").append(escapeHtml(recipientEmail)).append(" &bull; ").append(timestamp).append("</div>");
        sb.append("<div style='margin-top:6px; font-size:11px;'>&copy; 2026 SPEDO Analytics Inc. All rights reserved.</div>");
        sb.append("</div>");

        sb.append("</div></body></html>");
        return sb.toString();
    }

    private String getPlatformIcon(SocialPlatform p) {
        if (p == null) return "&#9654;";
        return switch (p) {
            case YOUTUBE -> "&#9654;";
            case INSTAGRAM -> "&#128247;";
            case TWITTER -> "X";
            case LINKEDIN -> "&#128188;";
            case FACEBOOK -> "&#128101;";
        };
    }

    private String getPlatformColor(SocialPlatform p) {
        if (p == null) return "#2563eb";
        return switch (p) {
            case YOUTUBE -> "#dc2626";
            case INSTAGRAM -> "#c026d3";
            case TWITTER -> "#0284c7";
            case LINKEDIN, FACEBOOK -> "#1d4ed8";
        };
    }

    private String getPlatformBg(SocialPlatform p) {
        if (p == null) return "#eff6ff";
        return switch (p) {
            case YOUTUBE -> "#fef2f2";
            case INSTAGRAM -> "#fdf4ff";
            case TWITTER -> "#f0f9ff";
            case LINKEDIN, FACEBOOK -> "#eff6ff";
        };
    }

    private String getPlatformBorder(SocialPlatform p) {
        if (p == null) return "#bfdbfe";
        return switch (p) {
            case YOUTUBE -> "#fca5a5";
            case INSTAGRAM -> "#f0abfc";
            case TWITTER -> "#7dd3fc";
            case LINKEDIN, FACEBOOK -> "#93c5fd";
        };
    }

    private String formatNumber(long number) {
        if (number >= 1_000_000_000) {
            return String.format("%.1fB", number / 1_000_000_000.0);
        } else if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        }
        return String.valueOf(number);
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
