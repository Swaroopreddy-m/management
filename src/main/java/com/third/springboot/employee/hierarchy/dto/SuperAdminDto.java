package com.third.springboot.employee.hierarchy.dto;

public class SuperAdminDto {

    private String id;
    private String superEntityId;
    private String name;
    private String email;
    private String username;
    private String password;
    private String status;

    public SuperAdminDto() {}

    public SuperAdminDto(String id, String superEntityId, String name, String email, String username, String password, String status) {
        this.id = id;
        this.superEntityId = superEntityId;
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSuperEntityId() { return superEntityId; }
    public void setSuperEntityId(String superEntityId) { this.superEntityId = superEntityId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
