package com.third.springboot.employee.hierarchy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "page_registry")
public class PageRegistry {

    @Id
    @Column(name = "page_id", length = 64)
    private String pageId; // Stable unique identifier, e.g. DASHBOARD, USER_MANAGEMENT

    @Column(name = "page_name", nullable = false)
    private String pageName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "route", nullable = false)
    private String route;

    @Column(name = "category")
    private String category = "CORE";

    @Column(name = "status", nullable = false)
    private String status = "REGISTERED"; // REGISTERED, ACTIVE, INACTIVE

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public PageRegistry() {}

    public PageRegistry(String pageId, String pageName, String description, String route, String category) {
        this.pageId = pageId;
        this.pageName = pageName;
        this.description = description;
        this.route = route;
        this.category = category;
        this.status = "ACTIVE";
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getPageId() { return pageId; }
    public void setPageId(String pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
