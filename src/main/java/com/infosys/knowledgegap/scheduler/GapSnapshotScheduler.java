package com.infosys.knowledgegap.scheduler;

import com.infosys.knowledgegap.dto.DepartmentGapSummary;
import com.infosys.knowledgegap.entity.GapSnapshot;
import com.infosys.knowledgegap.repository.GapSnapshotRepository;
import com.infosys.knowledgegap.service.GapAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Records one org-wide gap snapshot per day, so the Gap Analysis module can
 * show trend-over-time (Module 4). Idempotent per day and failure-isolated.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GapSnapshotScheduler {

    private final GapAnalysisService gapAnalysisService;
    private final GapSnapshotRepository gapSnapshotRepository;

    @Scheduled(cron = "0 30 6 * * *") // daily at 06:30 server time
    public void recordDailySnapshot() {
        try {
            LocalDate today = LocalDate.now();
            if (gapSnapshotRepository.existsBySnapshotDate(today)) return;

            List<DepartmentGapSummary> summaries = gapAnalysisService.getDepartmentGapSummaries();
            if (summaries.isEmpty()) return;

            int employeeCount = summaries.stream().mapToInt(DepartmentGapSummary::getEmployeeCount).sum();
            int totalGaps = summaries.stream().mapToInt(DepartmentGapSummary::getTotalGaps).sum();
            int criticalGaps = summaries.stream().mapToInt(DepartmentGapSummary::getCriticalGaps).sum();
            double avgReadiness = summaries.stream()
                    .filter(s -> s.getEmployeeCount() > 0)
                    .mapToDouble(DepartmentGapSummary::getAvgReadinessPercent)
                    .average().orElse(0);

            gapSnapshotRepository.save(GapSnapshot.builder()
                    .snapshotDate(today)
                    .avgReadinessPercent(Math.round(avgReadiness * 10) / 10.0)
                    .totalGaps(totalGaps)
                    .criticalGaps(criticalGaps)
                    .employeeCount(employeeCount)
                    .build());

            log.info("Recorded daily gap snapshot for {}", today);
        } catch (Exception ex) {
            log.error("Failed to record daily gap snapshot: {}", ex.getMessage());
        }
    }
}
