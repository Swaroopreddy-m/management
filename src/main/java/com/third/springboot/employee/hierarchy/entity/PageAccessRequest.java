package com.third.springboot.employee.hierarchy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "page_access_requests")
public class PageAccessRequest {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "organization_id", nullable = false, length = 64)
    private String organizationId;

    @Column(name = "requested_page_id", nullable = false, length = 64)
    private String requestedPageId;

    @Column(name = "requested_actions", length = 255)
    private String requestedActions = "VIEW";

    @Column(name = "justification", length = 1000)
    private String justification;

    @Column(name = "requesting_user_id", nullable = false, length = 100)
    private String requestingUserId;

    @Column(name = "status", nullable = false, length = 32)
    private String status = "PENDING"; // PENDING, UNDER_REVIEW, APPROVED, REJECTED, CANCELLED

    @Column(name = "decision_reason", length = 1000)
    private String decisionReason;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "decision_timestamp")
    private LocalDateTime decisionTimestamp;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public PageAccessRequest() {}

    public PageAccessRequest(String id, String organizationId, String requestedPageId, String requestedActions, String justification, String requestingUserId) {
        this.id = id;
        this.organizationId = organizationId;
        this.requestedPageId = requestedPageId;
        this.requestedActions = requestedActions != null ? requestedActions : "VIEW";
        this.justification = justification;
        this.requestingUserId = requestingUserId;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getRequestedPageId() { return requestedPageId; }
    public void setRequestedPageId(String requestedPageId) { this.requestedPageId = requestedPageId; }

    public String getRequestedActions() { return requestedActions; }
    public void setRequestedActions(String requestedActions) { this.requestedActions = requestedActions; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public String getRequestingUserId() { return requestingUserId; }
    public void setRequestingUserId(String requestingUserId) { this.requestingUserId = requestingUserId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getDecisionTimestamp() { return decisionTimestamp; }
    public void setDecisionTimestamp(LocalDateTime decisionTimestamp) { this.decisionTimestamp = decisionTimestamp; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
