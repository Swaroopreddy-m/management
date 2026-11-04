package com.third.springboot.employee.hierarchy.dto;

public class GroupActionAssignmentDto {

    private String pageId;
    private String permittedActions; // e.g. "VIEW,CREATE"

    public GroupActionAssignmentDto() {}

    public GroupActionAssignmentDto(String pageId, String permittedActions) {
        this.pageId = pageId;
        this.permittedActions = permittedActions;
    }

    public String getPageId() { return pageId; }
    public void setPageId(String pageId) { this.pageId = pageId; }

    public String getPermittedActions() { return permittedActions; }
    public void setPermittedActions(String permittedActions) { this.permittedActions = permittedActions; }
}
