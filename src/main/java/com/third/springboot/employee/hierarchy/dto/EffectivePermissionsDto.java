package com.third.springboot.employee.hierarchy.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class EffectivePermissionsDto {

    private String userId;
    private String userName;
    private String role; // DEVELOPER, SUPER_ENTITY, SUPER_ADMIN, ORG_ADMIN, USER
    private String organizationId;
    private String organizationName;
    private List<String> assignedEntityIds;
    private Map<String, Set<String>> authorizedPages; // pageId -> Set of actionCodes (e.g. "VIEW", "CREATE")
    private List<MenuItemDto> navigationMenus;

    public EffectivePermissionsDto() {}

    public EffectivePermissionsDto(String userId, String userName, String role, String organizationId,
                                   String organizationName, List<String> assignedEntityIds,
                                   Map<String, Set<String>> authorizedPages, List<MenuItemDto> navigationMenus) {
        this.userId = userId;
        this.userName = userName;
        this.role = role;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.assignedEntityIds = assignedEntityIds;
        this.authorizedPages = authorizedPages;
        this.navigationMenus = navigationMenus;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public List<String> getAssignedEntityIds() { return assignedEntityIds; }
    public void setAssignedEntityIds(List<String> assignedEntityIds) { this.assignedEntityIds = assignedEntityIds; }

    public Map<String, Set<String>> getAuthorizedPages() { return authorizedPages; }
    public void setAuthorizedPages(Map<String, Set<String>> authorizedPages) { this.authorizedPages = authorizedPages; }

    public List<MenuItemDto> getNavigationMenus() { return navigationMenus; }
    public void setNavigationMenus(List<MenuItemDto> navigationMenus) { this.navigationMenus = navigationMenus; }
}
