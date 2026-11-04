package com.third.springboot.employee.hierarchy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "org_page_permissions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"organization_id", "page_id"})
})
public class OrgPagePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false, length = 64)
    private String organizationId;

    @Column(name = "page_id", nullable = false, length = 64)
    private String pageId;

    @Column(name = "allowed_actions", nullable = false, length = 255)
    private String allowedActions; // Comma separated: e.g. "VIEW,CREATE,UPDATE,DELETE,EXPORT"

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE"; // ACTIVE, REVOKED

    @Column(name = "granted_by")
    private String grantedBy;

    @Column(name = "granted_at")
    private LocalDateTime grantedAt = LocalDateTime.now();

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revocation_reason", length = 500)
    private String revocationReason;

    public OrgPagePermission() {}

    public OrgPagePermission(String organizationId, String pageId, String allowedActions, String grantedBy) {
        this.organizationId = organizationId;
        this.pageId = pageId;
        this.allowedActions = allowedActions;
        this.grantedBy = grantedBy;
        this.status = "ACTIVE";
        this.grantedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getPageId() { return pageId; }
    public void setPageId(String pageId) { this.pageId = pageId; }

    public String getAllowedActions() { return allowedActions; }
    public void setAllowedActions(String allowedActions) { this.allowedActions = allowedActions; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getGrantedBy() { return grantedBy; }
    public void setGrantedBy(String grantedBy) { this.grantedBy = grantedBy; }

    public LocalDateTime getGrantedAt() { return grantedAt; }
    public void setGrantedAt(LocalDateTime grantedAt) { this.grantedAt = grantedAt; }

    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }

    public String getRevocationReason() { return revocationReason; }
    public void setRevocationReason(String revocationReason) { this.revocationReason = revocationReason; }
}
