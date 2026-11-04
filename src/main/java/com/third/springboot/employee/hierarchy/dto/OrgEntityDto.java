package com.third.springboot.employee.hierarchy.dto;

public class OrgEntityDto {

    private String id;
    private String organizationId;
    private String entityType; // BRANCH, DEPARTMENT, STORE, OTHER
    private String name;
    private String code;
    private String status;

    public OrgEntityDto() {}

    public OrgEntityDto(String id, String organizationId, String entityType, String name, String code, String status) {
        this.id = id;
        this.organizationId = organizationId;
        this.entityType = entityType;
        this.name = name;
        this.code = code;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
