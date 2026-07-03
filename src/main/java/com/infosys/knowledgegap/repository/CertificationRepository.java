package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByEmployeeProfileId(Long employeeProfileId);
}
