package com.spedo.analytics.dto;

import com.spedo.analytics.entity.ReportFormat;
import com.spedo.analytics.entity.ReportStatus;
import com.spedo.analytics.entity.ReportType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReportDto {
    private Long id;
    private String title;
    private ReportType reportType;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private ReportFormat fileFormat;
    private ReportStatus status;
    private String reportDataJson;
    private String downloadUrl;
    private LocalDateTime createdAt;

    public ReportDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public ReportFormat getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(ReportFormat fileFormat) {
        this.fileFormat = fileFormat;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getReportDataJson() {
        return reportDataJson;
    }

    public void setReportDataJson(String reportDataJson) {
        this.reportDataJson = reportDataJson;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
