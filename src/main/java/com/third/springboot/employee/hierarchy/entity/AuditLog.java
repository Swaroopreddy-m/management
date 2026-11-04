package com.third.springboot.employee.hierarchy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_id", length = 100)
    private String actorId;

    @Column(name = "actor_name")
    private String actorName;

    @Column(name = "actor_role", length = 50)
    private String actorRole; // DEVELOPER, SUPER_ENTITY_ADMIN, SUPER_ADMIN, ORG_ADMIN, USER

    @Column(name = "organization_id", length = 64)
    private String organizationId;

    @Column(name = "entity_id", length = 64)
    private String entityId;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "target_resource", length = 255)
    private String targetResource;

    @Column(name = "outcome", nullable = false, length = 32)
    private String outcome; // SUCCESS, FAILURE, DENIED

    @Column(name = "details", length = 2000)
    private String details;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public AuditLog() {}

    public AuditLog(String actorId, String actorName, String actorRole, String organizationId, String entityId,
                    String action, String targetResource, String outcome, String details, String ipAddress) {
        this.actorId = actorId;
        this.actorName = actorName;
        this.actorRole = actorRole;
        this.organizationId = organizationId;
        this.entityId = entityId;
        this.action = action;
        this.targetResource = targetResource;
        this.outcome = outcome;
        this.details = details;
        this.ipAddress = ipAddress;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }

    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }

    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getTargetResource() { return targetResource; }
    public void setTargetResource(String targetResource) { this.targetResource = targetResource; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
