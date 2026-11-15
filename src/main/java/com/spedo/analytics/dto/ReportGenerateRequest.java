package com.spedo.analytics.dto;

import com.spedo.analytics.entity.ReportFormat;
import com.spedo.analytics.entity.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ReportGenerateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Report type is required")
    private ReportType reportType;

    @NotNull(message = "Start date is required")
    private LocalDate dateFrom;

    @NotNull(message = "End date is required")
    private LocalDate dateTo;

    private ReportFormat fileFormat = ReportFormat.CSV;

    public ReportGenerateRequest() {
    }

    public ReportGenerateRequest(String title, ReportType reportType, LocalDate dateFrom, LocalDate dateTo, ReportFormat fileFormat) {
        this.title = title;
        this.reportType = reportType;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.fileFormat = fileFormat != null ? fileFormat : ReportFormat.CSV;
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
}
