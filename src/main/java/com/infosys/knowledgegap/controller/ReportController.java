package com.infosys.knowledgegap.controller;

import com.infosys.knowledgegap.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Module 11 — Reports & Export.
 * Serves downloadable PDF / Excel reports built from the Gap Analysis engine.
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports & Export", description = "Module 11 — PDF and Excel exports of gap-analysis data")
public class ReportController {

    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ReportService reportService;

    @GetMapping("/gap/me/pdf")
    @Operation(summary = "Download my individual skill-gap report as a PDF")
    public ResponseEntity<byte[]> myGapReportPdf(@AuthenticationPrincipal UserDetails userDetails) {
        byte[] pdf = reportService.myGapReportPdf(userDetails.getUsername());
        return download(pdf, MediaType.APPLICATION_PDF_VALUE, "my-gap-report-" + LocalDate.now() + ".pdf");
    }

    @GetMapping("/gap/user/{userId}/pdf")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','HR_SPECIALIST','DEPARTMENT_HEAD','TEAM_LEAD_MANAGER')")
    @Operation(summary = "Download a specific employee's skill-gap report as a PDF")
    public ResponseEntity<byte[]> userGapReportPdf(@PathVariable Long userId) {
        byte[] pdf = reportService.userGapReportPdf(userId);
        return download(pdf, MediaType.APPLICATION_PDF_VALUE, "gap-report-user-" + userId + ".pdf");
    }

    @GetMapping("/gap/department-summary/excel")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','HR_SPECIALIST')")
    @Operation(summary = "Download the org-wide department gap summary as an Excel file")
    public ResponseEntity<byte[]> departmentSummaryExcel() {
        byte[] xlsx = reportService.departmentSummaryExcel();
        return download(xlsx, XLSX, "department-gap-summary-" + LocalDate.now() + ".xlsx");
    }

    @GetMapping("/gap/workforce/excel")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','HR_SPECIALIST')")
    @Operation(summary = "Download the full workforce skill-gap report as an Excel file")
    public ResponseEntity<byte[]> workforceExcel() {
        byte[] xlsx = reportService.workforceGapExcel();
        return download(xlsx, XLSX, "workforce-gap-report-" + LocalDate.now() + ".xlsx");
    }

    private ResponseEntity<byte[]> download(byte[] body, String contentType, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(body);
    }
}
