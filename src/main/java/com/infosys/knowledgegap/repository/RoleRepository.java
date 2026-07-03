package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.Role;
import com.infosys.knowledgegap.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);
}
