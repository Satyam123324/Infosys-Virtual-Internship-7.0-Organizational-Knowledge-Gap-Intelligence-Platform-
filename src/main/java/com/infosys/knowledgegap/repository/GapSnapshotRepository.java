package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.GapSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface GapSnapshotRepository extends JpaRepository<GapSnapshot, Long> {
    List<GapSnapshot> findAllByOrderBySnapshotDateAsc();
    boolean existsBySnapshotDate(LocalDate snapshotDate);
}
