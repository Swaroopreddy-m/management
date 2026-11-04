package com.third.springboot.employee.hierarchy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_permissions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"group_id", "page_id"})
})
public class GroupPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false, length = 64)
    private String groupId;

    @Column(name = "organization_id", nullable = false, length = 64)
    private String organizationId;

    @Column(name = "page_id", nullable = false, length = 64)
    private String pageId;

    @Column(name = "permitted_actions", nullable = false, length = 255)
    private String permittedActions; // Comma separated: e.g. "VIEW,CREATE"

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public GroupPermission() {}

    public GroupPermission(String groupId, String organizationId, String pageId, String permittedActions) {
        this.groupId = groupId;
        this.organizationId = organizationId;
        this.pageId = pageId;
        this.permittedActions = permittedActions;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getPageId() { return pageId; }
    public void setPageId(String pageId) { this.pageId = pageId; }

    public String getPermittedActions() { return permittedActions; }
    public void setPermittedActions(String permittedActions) { this.permittedActions = permittedActions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
