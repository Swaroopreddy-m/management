package com.third.springboot.employee.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "EMPLOYEE")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    private String email;
    
    private String department;
    
    private Double salary;

    @Column(name = "organization_id", length = 64)
    private String organizationId = "DEFAULT_ORG";

    @Column(name = "entity_id", length = 64)
    private String entityId = "DEFAULT_ENT";

    public Employee() {}

    public Employee(String name, String email, String department, Double salary) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.salary = salary;
        this.organizationId = "DEFAULT_ORG";
        this.entityId = "DEFAULT_ENT";
    }

    public Employee(String name, String email, String department, Double salary, String organizationId, String entityId) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.salary = salary;
        this.organizationId = organizationId;
        this.entityId = entityId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
}
