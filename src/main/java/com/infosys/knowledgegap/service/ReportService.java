package com.infosys.knowledgegap.service;

/**
 * Module 11 — Reports & Export.
 * Produces downloadable PDF and Excel documents from the live Gap Analysis data.
 * Each method returns the raw file bytes; the controller wraps them in an HTTP download response.
 */
public interface ReportService {

    /** Individual skill-gap report (PDF) for the currently logged-in employee. */
    byte[] myGapReportPdf(String email);

    /** Individual skill-gap report (PDF) for a specific employee — HR/Admin/Manager use. */
    byte[] userGapReportPdf(Long userId);

    /** Department gap summary (Excel) — one row per department, org-wide. */
    byte[] departmentSummaryExcel();

    /** Workforce skill-gap report (Excel) — every employee plus a per-gap breakdown sheet. */
    byte[] workforceGapExcel();

    /** Training effectiveness & learning-ROI report (Excel) — completion rates, progress, milestones. */
    byte[] trainingEffectivenessExcel();

    /** Strategic workforce planning report (Excel) — department risk ranking + top org-wide skill gaps. */
    byte[] strategicWorkforcePlanExcel();
}
