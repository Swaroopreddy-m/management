package com.third.springboot.employee.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_master")
public class UserMaster {

    @Id
    @Column(name = "CUSTOMER_ID", nullable = false, length = 255)
    private String customerId;

    @Column(name = "NAME", length = 255)
    private String name;

    @Column(name = "EMAIL", length = 200)
    private String email;

    @Column(name = "MOBILE", length = 200)
    private String mobile;

    @Column(name = "SYSTEM_USER_ID", length = 100)
    private String systemUserId;

    @Column(name = "USER_NAME", length = 200)
    private String userName;

    @Column(name = "USER_GROUPS", length = 100)
    private String userGroups;

    @Column(name = "USER_ENTITY", length = 100)
    private String userEntity;

    @Column(name = "USER_STATUS", length = 100)
    private String userStatus;

    @Column(name = "USER_LAST_LOGIN_TIME", length = 100)
    private String userLastLoginTime;

    @Column(name = "CREATED_BY", length = 100)
    private String createdBy;

    @Column(name = "CREATED_TIME", length = 100)
    private String createdTime;

    @Column(name = "APPROVED_BY", length = 100)
    private String approvedBy;

    @Column(name = "APPROVED_TIME", length = 100)
    private String approvedTime;

    @Column(name = "IP", length = 100)
    private String ip;

    @Column(name = "OTHERS", length = 100)
    private String others;

    @Column(name = "ORGANIZATION_ID", length = 64)
    private String organizationId;

    @Column(name = "USER_ROLE", length = 50)
    private String userRole = "USER"; // DEVELOPER, SUPER_ENTITY, SUPER_ADMIN, ORG_ADMIN, USER

    // ✅ Getters and Setters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getSystemUserId() { return systemUserId; }
    public void setSystemUserId(String systemUserId) { this.systemUserId = systemUserId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserGroups() { return userGroups; }
    public void setUserGroups(String userGroups) { this.userGroups = userGroups; }

    public String getUserEntity() { return userEntity; }
    public void setUserEntity(String userEntity) { this.userEntity = userEntity; }

    public String getUserStatus() { return userStatus; }
    public void setUserStatus(String userStatus) { this.userStatus = userStatus; }

    public String getUserLastLoginTime() { return userLastLoginTime; }
    public void setUserLastLoginTime(String userLastLoginTime) { this.userLastLoginTime = userLastLoginTime; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getCreatedTime() { return createdTime; }
    public void setCreatedTime(String createdTime) { this.createdTime = createdTime; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public String getApprovedTime() { return approvedTime; }
    public void setApprovedTime(String approvedTime) { this.approvedTime = approvedTime; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getOthers() { return others; }
    public void setOthers(String others) { this.others = others; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
}
