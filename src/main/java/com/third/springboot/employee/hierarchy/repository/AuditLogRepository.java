package com.third.springboot.employee.hierarchy.repository;

import com.third.springboot.employee.hierarchy.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop100ByOrderByTimestampDesc();
    List<AuditLog> findByOrganizationIdOrderByTimestampDesc(String organizationId);
    List<AuditLog> findByActorRoleOrderByTimestampDesc(String actorRole);
    List<AuditLog> findByActionOrderByTimestampDesc(String action);
}
