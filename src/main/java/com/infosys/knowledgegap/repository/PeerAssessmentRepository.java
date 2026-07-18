package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.PeerAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PeerAssessmentRepository extends JpaRepository<PeerAssessment, Long> {
    List<PeerAssessment> findByTargetIdOrderByCreatedAtDesc(Long targetUserId);
    List<PeerAssessment> findByRaterIdOrderByCreatedAtDesc(Long raterUserId);
}
