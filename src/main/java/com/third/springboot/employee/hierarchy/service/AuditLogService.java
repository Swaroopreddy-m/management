package com.third.springboot.employee.hierarchy.service;

import com.third.springboot.employee.hierarchy.entity.AuditLog;
import com.third.springboot.employee.hierarchy.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public AuditLog log(String actorId, String actorName, String actorRole, String organizationId, String entityId,
                        String action, String targetResource, String outcome, String details, String ipAddress) {
        AuditLog entry = new AuditLog(actorId, actorName, actorRole, organizationId, entityId,
                action, targetResource, outcome, details, ipAddress);
        return auditLogRepository.save(entry);
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop100ByOrderByTimestampDesc();
    }

    public List<AuditLog> getLogsByOrganization(String organizationId) {
        return auditLogRepository.findByOrganizationIdOrderByTimestampDesc(organizationId);
    }

    public List<AuditLog> getLogsByRole(String actorRole) {
        return auditLogRepository.findByActorRoleOrderByTimestampDesc(actorRole);
    }
}
