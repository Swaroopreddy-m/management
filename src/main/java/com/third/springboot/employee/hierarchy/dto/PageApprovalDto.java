package com.third.springboot.employee.hierarchy.dto;

public class PageApprovalDto {

    private String decision; // APPROVE, REJECT, REVOKE
    private String allowedActions; // e.g. "VIEW,CREATE,UPDATE"
    private String reason;

    public PageApprovalDto() {}

    public PageApprovalDto(String decision, String allowedActions, String reason) {
        this.decision = decision;
        this.allowedActions = allowedActions;
        this.reason = reason;
    }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getAllowedActions() { return allowedActions; }
    public void setAllowedActions(String allowedActions) { this.allowedActions = allowedActions; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
