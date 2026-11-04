package com.third.springboot.employee.hierarchy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "super_entity_id", nullable = false, length = 64)
    private String superEntityId;

    @Column(name = "super_admin_id", nullable = false, length = 64)
    private String superAdminId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE

    @Column(name = "subscription_tier")
    private String subscriptionTier = "STANDARD";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Organization() {}

    public Organization(String id, String superEntityId, String superAdminId, String name, String code) {
        this.id = id;
        this.superEntityId = superEntityId;
        this.superAdminId = superAdminId;
        this.name = name;
        this.code = code;
        this.status = "ACTIVE";
        this.subscriptionTier = "STANDARD";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSuperEntityId() { return superEntityId; }
    public void setSuperEntityId(String superEntityId) { this.superEntityId = superEntityId; }

    public String getSuperAdminId() { return superAdminId; }
    public void setSuperAdminId(String superAdminId) { this.superAdminId = superAdminId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSubscriptionTier() { return subscriptionTier; }
    public void setSubscriptionTier(String subscriptionTier) { this.subscriptionTier = subscriptionTier; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
