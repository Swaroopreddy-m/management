package com.third.springboot.employee.hierarchy.dto;

import java.util.List;

public class UserAssignmentDto {

    private String userId;
    private List<String> groupIds;
    private List<String> entityIds;
    private String userRole;

    public UserAssignmentDto() {}

    public UserAssignmentDto(String userId, List<String> groupIds, List<String> entityIds, String userRole) {
        this.userId = userId;
        this.groupIds = groupIds;
        this.entityIds = entityIds;
        this.userRole = userRole;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<String> getGroupIds() { return groupIds; }
    public void setGroupIds(List<String> groupIds) { this.groupIds = groupIds; }

    public List<String> getEntityIds() { return entityIds; }
    public void setEntityIds(List<String> entityIds) { this.entityIds = entityIds; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
}
