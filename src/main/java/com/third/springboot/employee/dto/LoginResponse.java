package com.third.springboot.employee.dto;

import com.third.springboot.employee.hierarchy.dto.EffectivePermissionsDto;

public class LoginResponse {

    private String status;
    private String userId;
    private String role;
    private String organizationId;
    private EffectivePermissionsDto permissions;

    public LoginResponse() {}

    public LoginResponse(String status) {
        this.status = status;
    }

    public LoginResponse(String status, String userId, String role, String organizationId, EffectivePermissionsDto permissions) {
        this.status = status;
        this.userId = userId;
        this.role = role;
        this.organizationId = organizationId;
        this.permissions = permissions;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public EffectivePermissionsDto getPermissions() { return permissions; }
    public void setPermissions(EffectivePermissionsDto permissions) { this.permissions = permissions; }
}
