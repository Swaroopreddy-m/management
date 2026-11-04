package com.third.springboot.employee.hierarchy.dto;

public class OrganizationDto {

    private String id;
    private String superEntityId;
    private String superAdminId;
    private String name;
    private String code;
    private String subscriptionTier;
    private String status;

    public OrganizationDto() {}

    public OrganizationDto(String id, String superEntityId, String superAdminId, String name, String code, String subscriptionTier, String status) {
        this.id = id;
        this.superEntityId = superEntityId;
        this.superAdminId = superAdminId;
        this.name = name;
        this.code = code;
        this.subscriptionTier = subscriptionTier;
        this.status = status;
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

    public String getSubscriptionTier() { return subscriptionTier; }
    public void setSubscriptionTier(String subscriptionTier) { this.subscriptionTier = subscriptionTier; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
