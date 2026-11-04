package com.third.springboot.employee.hierarchy.dto;

public class PageAccessRequestDto {

    private String requestedPageId;
    private String requestedActions;
    private String justification;

    public PageAccessRequestDto() {}

    public PageAccessRequestDto(String requestedPageId, String requestedActions, String justification) {
        this.requestedPageId = requestedPageId;
        this.requestedActions = requestedActions;
        this.justification = justification;
    }

    public String getRequestedPageId() { return requestedPageId; }
    public void setRequestedPageId(String requestedPageId) { this.requestedPageId = requestedPageId; }

    public String getRequestedActions() { return requestedActions; }
    public void setRequestedActions(String requestedActions) { this.requestedActions = requestedActions; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
}
