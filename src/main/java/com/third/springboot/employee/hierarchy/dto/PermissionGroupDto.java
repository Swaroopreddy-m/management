package com.third.springboot.employee.hierarchy.dto;

public class PermissionGroupDto {

    private String id;
    private String groupName;
    private String description;
    private String status;

    public PermissionGroupDto() {}

    public PermissionGroupDto(String id, String groupName, String description, String status) {
        this.id = id;
        this.groupName = groupName;
        this.description = description;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
