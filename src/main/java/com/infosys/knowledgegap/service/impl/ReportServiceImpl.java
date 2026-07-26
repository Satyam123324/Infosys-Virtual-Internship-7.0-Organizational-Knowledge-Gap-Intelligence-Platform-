package com.infosys.knowledgegap.service.impl;

import com.infosys.knowledgegap.dto.DepartmentGapSummary;
import com.infosys.knowledgegap.dto.EmployeeGapReport;
import com.infosys.knowledgegap.dto.ResourceLink;
import com.infosys.knowledgegap.dto.SkillGapDetail;
import com.infosys.knowledgegap.dto.TeamMemberLearningProgressResponse;
import com.infosys.knowledgegap.service.GapAnalysisService;
import com.infosys.knowledgegap.service.ReportService;
import com.infosys.knowledgegap.service.TrainingEnrollmentService;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds PDF (OpenPDF) and Excel (Apache POI) exports from the Gap Analysis engine.
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final GapAnalysisService gapAnalysisService;
    private final TrainingEnrollmentService trainingEnrollmentService;

    // ---- brand palette (matches the app) ----
    private static final Color INK   = new Color(14, 23, 38);
    private static final Color TEAL  = new Color(10, 142, 130);
    private static final Color AMBER = new Color(245, 158, 11);
    private static final Color RED   = new Color(225, 29, 72);
    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color SLATE = new Color(71, 85, 105);
    private static final Color LIGHT = new Color(241, 245, 249);

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    // =========================================================================
    // PDF — individual gap report
    // =========================================================================

    @Override
    public byte[] myGapReportPdf(String email) {
        return buildGapPdf(gapAnalysisService.getMyGapReport(email));
    }

    @Override
    public byte[] userGapReportPdf(Long userId) {
        return buildGapPdf(gapAnalysisService.getGapReportForUser(userId));
    }

    private byte[] buildGapPdf(EmployeeGapReport report) {
        Document doc = new Document(PageSize.A4, 40, 40, 48, 40);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font h1 = new Font(Font.HELVETICA, 20, Font.BOLD, INK);
            Font sub = new Font(Font.HELVETICA, 10, Font.NORMAL, SLATE);
            Font h2 = new Font(Font.HELVETICA, 13, Font.BOLD, TEAL);
            Font body = new Font(Font.HELVETICA, 10, Font.NORMAL, INK);
            Font small = new Font(Font.HELVETICA, 8, Font.NORMAL, SLATE);

            Paragraph title = new Paragraph("Individual Skill Gap Report", h1);
            doc.add(title);
            doc.add(new Paragraph("Organizational Knowledge Gap Intelligence Platform", sub));
            doc.add(new Paragraph("Generated " + LocalDate.now().format(DATE), small));
            doc.add(spacer(8));

            // employee summary box
            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);
            info.setWidths(new float[]{1.2f, 2.8f});
            infoRow(info, "Employee", nz(report.getFullName()), body);
            infoRow(info, "Target Role", nz(report.getRoleTitle()), body);
            infoRow(info, "Department", nz(report.getDepartmentName()), body);
            doc.add(info);
            doc.add(spacer(10));

            if (!report.isFrameworkFound()) {
                doc.add(new Paragraph(
                        "No competency framework is defined for this role yet, so a full gap report "
                        + "cannot be generated. Ask HR / L&D Admin to define one, or pick a seeded role.",
                        body));
                doc.close();
                return baos.toByteArray();
            }

            // readiness headline
            double readiness = report.getOverallReadinessPercent();
            Color rc = readiness >= 80 ? GREEN : readiness >= 50 ? AMBER : RED;
            PdfPTable stats = new PdfPTable(4);
            stats.setWidthPercentage(100);
            stats.setWidths(new float[]{1, 1, 1, 1});
            statCell(stats, "Readiness", String.format("%.1f%%", readiness), rc);
            statCell(stats, "Required Skills", String.valueOf(report.getTotalRequiredSkills()), INK);
            statCell(stats, "Meeting Bar", String.valueOf(report.getSkillsMeetingRequirement()), GREEN);
            statCell(stats, "With Gap", String.valueOf(report.getSkillsWithGap()), RED);
            doc.add(stats);
            doc.add(spacer(12));

            doc.add(new Paragraph("Skill-by-Skill Breakdown", h2));
            doc.add(spacer(4));

            PdfPTable t = new PdfPTable(5);
            t.setWidthPercentage(100);
            t.setWidths(new float[]{2.2f, 1.3f, 1.3f, 1.1f, 1.4f});
            headerCell(t, "Skill");
            headerCell(t, "Required");
            headerCell(t, "Current");
            headerCell(t, "Gap");
            headerCell(t, "Severity");
            if (report.getGaps() != null) {
                for (SkillGapDetail g : report.getGaps()) {
                    cell(t, nz(g.getSkillName()), body, false);
                    cell(t, String.valueOf(g.getRequiredLevel()), body, true);
                    cell(t, g.getCurrentLevel() == null ? "None" : String.valueOf(g.getCurrentLevel()), body, true);
                    cell(t, String.valueOf(g.getGapSize()), body, true);
                    severityCell(t, g.getSeverity());
                }
            }
            doc.add(t);
            doc.add(spacer(12));

            // recommendations
            doc.add(new Paragraph("Recommendations", h2));
            doc.add(spacer(4));
            boolean any = false;
            if (report.getGaps() != null) {
                for (SkillGapDetail g : report.getGaps()) {
                    if (g.getGapSize() <= 0 || g.getRecommendationText() == null) continue;
                    any = true;
                    Font skillFont = new Font(Font.HELVETICA, 10, Font.BOLD, INK);
                    doc.add(new Paragraph(g.getSkillName() + "  (" + g.getSeverity() + ")", skillFont));
                    doc.add(new Paragraph(g.getRecommendationText(), body));
                    if (g.getSuggestedResources() != null) {
                        for (ResourceLink r : g.getSuggestedResources()) {
                            doc.add(new Paragraph("  • " + r.getProvider() + ": " + r.getTitle()
                                    + " — " + r.getUrl(), small));
                        }
                    }
                    doc.add(spacer(6));
                }
            }
            if (!any) {
                doc.add(new Paragraph("No gaps detected — this employee meets all role requirements.", body));
            }

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build PDF gap report: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // Excel — department summary
    // =========================================================================

    @Override
    public byte[] departmentSummaryExcel() {
        List<DepartmentGapSummary> summaries = gapAnalysisService.getDepartmentGapSummaries();
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Department Gap Summary");
            CellStyle header = headerStyle(wb);
            CellStyle pct = pctStyle(wb);

            String[] cols = {"Department", "Employees", "Avg Readiness %", "Total Gaps", "Critical Gaps"};
            Row hr = sheet.createRow(0);
            for (int i = 0; i < cols.length; i++) {
                Cell c = hr.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(header);
            }
            int r = 1;
            for (DepartmentGapSummary s : summaries) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(nz(s.getDepartmentName()));
                row.createCell(1).setCellValue(s.getEmployeeCount());
                Cell pc = row.createCell(2);
                pc.setCellValue(s.getAvgReadinessPercent() / 100.0);
                pc.setCellStyle(pct);
                row.createCell(3).setCellValue(s.getTotalGaps());
                row.createCell(4).setCellValue(s.getCriticalGaps());
            }
            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);
            wb.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build department summary Excel: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // Excel — workforce report (2 sheets)
    // =========================================================================

    @Override
    public byte[] workforceGapExcel() {
        List<EmployeeGapReport> reports = gapAnalysisService.getAllGapReports();
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            CellStyle header = headerStyle(wb);
            CellStyle pct = pctStyle(wb);

            // Sheet 1 — one row per employee
            Sheet overview = wb.createSheet("Workforce Overview");
            String[] cols = {"Employee", "Role", "Department", "Required Skills",
                    "Meeting Bar", "With Gap", "Readiness %", "Critical Gaps"};
            Row hr = overview.createRow(0);
            for (int i = 0; i < cols.length; i++) {
                Cell c = hr.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(header);
            }
            int r = 1;
            for (EmployeeGapReport rep : reports) {
                int critical = 0;
                if (rep.getGaps() != null) {
                    for (SkillGapDetail g : rep.getGaps()) {
                        if ("CRITICAL".equals(g.getSeverity())) critical++;
                    }
                }
                Row row = overview.createRow(r++);
                row.createCell(0).setCellValue(nz(rep.getFullName()));
                row.createCell(1).setCellValue(nz(rep.getRoleTitle()));
                row.createCell(2).setCellValue(nz(rep.getDepartmentName()));
                row.createCell(3).setCellValue(rep.getTotalRequiredSkills());
                row.createCell(4).setCellValue(rep.getSkillsMeetingRequirement());
                row.createCell(5).setCellValue(rep.getSkillsWithGap());
                Cell pc = row.createCell(6);
                pc.setCellValue(rep.getOverallReadinessPercent() / 100.0);
                pc.setCellStyle(pct);
                row.createCell(7).setCellValue(critical);
            }
            for (int i = 0; i < cols.length; i++) overview.autoSizeColumn(i);

            // Sheet 2 — one row per detected gap
            Sheet gapsSheet = wb.createSheet("Skill Gaps");
            String[] gcols = {"Employee", "Role", "Skill", "Required", "Current", "Gap Size", "Severity"};
            Row ghr = gapsSheet.createRow(0);
            for (int i = 0; i < gcols.length; i++) {
                Cell c = ghr.createCell(i);
                c.setCellValue(gcols[i]);
                c.setCellStyle(header);
            }
            int gr = 1;
            for (EmployeeGapReport rep : reports) {
                if (rep.getGaps() == null) continue;
                for (SkillGapDetail g : rep.getGaps()) {
                    if (g.getGapSize() <= 0) continue;
                    Row row = gapsSheet.createRow(gr++);
                    row.createCell(0).setCellValue(nz(rep.getFullName()));
                    row.createCell(1).setCellValue(nz(rep.getRoleTitle()));
                    row.createCell(2).setCellValue(nz(g.getSkillName()));
                    row.createCell(3).setCellValue(String.valueOf(g.getRequiredLevel()));
                    row.createCell(4).setCellValue(g.getCurrentLevel() == null ? "None" : String.valueOf(g.getCurrentLevel()));
                    row.createCell(5).setCellValue(g.getGapSize());
                    row.createCell(6).setCellValue(nz(g.getSeverity()));
                }
            }
            for (int i = 0; i < gcols.length; i++) gapsSheet.autoSizeColumn(i);

            wb.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build workforce Excel: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // Excel — training effectiveness & learning ROI (Module 11)
    // =========================================================================

    @Override
    public byte[] trainingEffectivenessExcel() {
        List<TeamMemberLearningProgressResponse> rows = trainingEnrollmentService.getTeamProgress();
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Training Effectiveness");
            CellStyle header = headerStyle(wb);
            CellStyle pct = pctStyle(wb);

            String[] cols = {"Employee", "Department", "Role", "Enrollments", "Completed",
                    "Overdue", "Completion Rate", "Avg Progress", "Milestones Earned"};
            Row hr = sheet.createRow(0);
            for (int i = 0; i < cols.length; i++) {
                Cell c = hr.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(header);
            }

            int r = 1;
            int totalEnrollments = 0;
            int totalCompleted = 0;
            int totalMilestones = 0;
            for (TeamMemberLearningProgressResponse m : rows) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(nz(m.getFullName()));
                row.createCell(1).setCellValue(nz(m.getDepartment()));
                row.createCell(2).setCellValue(nz(m.getDesignation()));
                row.createCell(3).setCellValue(m.getTotalEnrollments());
                row.createCell(4).setCellValue(m.getCompletedCount());
                row.createCell(5).setCellValue(m.getOverdueCount());

                double completionRate = m.getTotalEnrollments() > 0
                        ? (double) m.getCompletedCount() / m.getTotalEnrollments() : 0;
                Cell rate = row.createCell(6);
                rate.setCellValue(completionRate);
                rate.setCellStyle(pct);

                Cell prog = row.createCell(7);
                prog.setCellValue(m.getAvgProgressPercent() / 100.0);
                prog.setCellStyle(pct);

                row.createCell(8).setCellValue(m.getMilestonesEarned());

                totalEnrollments += m.getTotalEnrollments();
                totalCompleted += m.getCompletedCount();
                totalMilestones += m.getMilestonesEarned();
            }

            // Org-level summary (learning ROI snapshot)
            r++;
            Row summaryHeader = sheet.createRow(r++);
            Cell sh = summaryHeader.createCell(0);
            sh.setCellValue("Organization Summary");
            sh.setCellStyle(header);

            summaryRow(sheet, r++, "Total enrollments", String.valueOf(totalEnrollments));
            summaryRow(sheet, r++, "Total completed", String.valueOf(totalCompleted));
            summaryRow(sheet, r++, "Total milestones earned", String.valueOf(totalMilestones));

            Row rateRow = sheet.createRow(r++);
            rateRow.createCell(0).setCellValue("Org completion rate");
            Cell orgRate = rateRow.createCell(1);
            orgRate.setCellValue(totalEnrollments > 0 ? (double) totalCompleted / totalEnrollments : 0);
            orgRate.setCellStyle(pct);

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);
            wb.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build training effectiveness Excel: " + e.getMessage(), e);
        }
    }

    private void summaryRow(Sheet sheet, int rowIdx, String label, String value) {
        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    // =========================================================================
    // Excel — strategic workforce planning (Module 11)
    // =========================================================================

    @Override
    public byte[] strategicWorkforcePlanExcel() {
        List<DepartmentGapSummary> summaries = gapAnalysisService.getDepartmentGapSummaries();
        List<EmployeeGapReport> reports = gapAnalysisService.getAllGapReports();
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            CellStyle header = headerStyle(wb);
            CellStyle pct = pctStyle(wb);

            // Sheet 1 — departments ranked by investment priority
            Sheet dept = wb.createSheet("Department Priorities");
            String[] cols = {"Priority", "Department", "Employees", "Avg Readiness", "Total Gaps", "Critical Gaps"};
            Row hr = dept.createRow(0);
            for (int i = 0; i < cols.length; i++) {
                Cell c = hr.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(header);
            }

            List<DepartmentGapSummary> ranked = new ArrayList<>(summaries);
            ranked.sort(Comparator.comparingInt(DepartmentGapSummary::getCriticalGaps).reversed()
                    .thenComparingDouble(DepartmentGapSummary::getAvgReadinessPercent));

            int r = 1;
            for (DepartmentGapSummary s : ranked) {
                String priority = (s.getAvgReadinessPercent() < 50 || s.getCriticalGaps() >= 5) ? "HIGH"
                        : s.getAvgReadinessPercent() < 75 ? "MEDIUM" : "LOW";
                Row row = dept.createRow(r++);
                row.createCell(0).setCellValue(priority);
                row.createCell(1).setCellValue(nz(s.getDepartmentName()));
                row.createCell(2).setCellValue(s.getEmployeeCount());
                Cell rd = row.createCell(3);
                rd.setCellValue(s.getAvgReadinessPercent() / 100.0);
                rd.setCellStyle(pct);
                row.createCell(4).setCellValue(s.getTotalGaps());
                row.createCell(5).setCellValue(s.getCriticalGaps());
            }
            for (int i = 0; i < cols.length; i++) dept.autoSizeColumn(i);

            // Sheet 2 — top org-wide skill gaps (where to invest training)
            Sheet skillSheet = wb.createSheet("Top Skill Gaps");
            String[] scols = {"Skill", "People With Gap", "Total Gap Size", "Critical Count"};
            Row shr = skillSheet.createRow(0);
            for (int i = 0; i < scols.length; i++) {
                Cell c = shr.createCell(i);
                c.setCellValue(scols[i]);
                c.setCellStyle(header);
            }

            Map<String, int[]> agg = new HashMap<>(); // skill -> [peopleWithGap, totalGapSize, criticalCount]
            for (EmployeeGapReport rep : reports) {
                if (rep.getGaps() == null) continue;
                for (SkillGapDetail g : rep.getGaps()) {
                    if (g.getGapSize() <= 0) continue;
                    int[] a = agg.computeIfAbsent(g.getSkillName(), k -> new int[3]);
                    a[0] += 1;
                    a[1] += g.getGapSize();
                    if ("CRITICAL".equals(g.getSeverity())) a[2] += 1;
                }
            }
            List<Map.Entry<String, int[]>> entries = new ArrayList<>(agg.entrySet());
            entries.sort((a, b) -> Integer.compare(b.getValue()[1], a.getValue()[1]));

            int sr = 1;
            for (Map.Entry<String, int[]> e : entries) {
                Row row = skillSheet.createRow(sr++);
                row.createCell(0).setCellValue(e.getKey());
                row.createCell(1).setCellValue(e.getValue()[0]);
                row.createCell(2).setCellValue(e.getValue()[1]);
                row.createCell(3).setCellValue(e.getValue()[2]);
            }
            for (int i = 0; i < scols.length; i++) skillSheet.autoSizeColumn(i);

            wb.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build strategic workforce plan Excel: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // helpers
    // =========================================================================

    private static String nz(String s) { return s == null ? "" : s; }

    private static Paragraph spacer(float h) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(h);
        return p;
    }

    private void infoRow(PdfPTable t, String label, String value, Font body) {
        Font lf = new Font(Font.HELVETICA, 10, Font.BOLD, SLATE);
        PdfPCell l = new PdfPCell(new Phrase(label, lf));
        PdfPCell v = new PdfPCell(new Phrase(value, body));
        l.setBackgroundColor(LIGHT);
        l.setPadding(6); v.setPadding(6);
        l.setBorderColor(Color.WHITE); v.setBorderColor(Color.WHITE);
        t.addCell(l); t.addCell(v);
    }

    private void statCell(PdfPTable t, String label, String value, Color color) {
        Font vf = new Font(Font.HELVETICA, 16, Font.BOLD, color);
        Font lf = new Font(Font.HELVETICA, 8, Font.NORMAL, SLATE);
        PdfPCell c = new PdfPCell();
        c.setBackgroundColor(LIGHT);
        c.setBorderColor(Color.WHITE);
        c.setPadding(8);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        Paragraph v = new Paragraph(value, vf); v.setAlignment(Element.ALIGN_CENTER);
        Paragraph l = new Paragraph(label, lf); l.setAlignment(Element.ALIGN_CENTER);
        c.addElement(v); c.addElement(l);
        t.addCell(c);
    }

    private void headerCell(PdfPTable t, String text) {
        Font f = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(INK);
        c.setPadding(6);
        c.setBorderColor(Color.WHITE);
        t.addCell(c);
    }

    private void cell(PdfPTable t, String text, Font f, boolean center) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setPadding(5);
        c.setBorderColor(LIGHT);
        if (center) c.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.addCell(c);
    }

    private void severityCell(PdfPTable t, String severity) {
        Color color;
        switch (severity == null ? "" : severity) {
            case "CRITICAL": color = RED; break;
            case "MODERATE": color = AMBER; break;
            case "MINOR": color = TEAL; break;
            default: color = GREEN; break;
        }
        Font f = new Font(Font.HELVETICA, 9, Font.BOLD, color);
        PdfPCell c = new PdfPCell(new Phrase(severity == null ? "NONE" : severity, f));
        c.setPadding(5);
        c.setBorderColor(LIGHT);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.addCell(c);
    }

    private CellStyle headerStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.LEFT);
        return s;
    }

    private CellStyle pctStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setDataFormat(wb.createDataFormat().getFormat("0.0%"));
        return s;
    }
}
