package com.third.springboot.employee.hierarchy.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "page_actions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"page_id", "action_code"})
})
public class PageAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_id", nullable = false, length = 64)
    private String pageId;

    @Column(name = "action_code", nullable = false, length = 32)
    private String actionCode; // VIEW, CREATE, UPDATE, DELETE, EXPORT, PRINT

    @Column(name = "action_name", nullable = false)
    private String actionName;

    @Column(name = "description")
    private String description;

    public PageAction() {}

    public PageAction(String pageId, String actionCode, String actionName, String description) {
        this.pageId = pageId;
        this.actionCode = actionCode;
        this.actionName = actionName;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPageId() { return pageId; }
    public void setPageId(String pageId) { this.pageId = pageId; }

    public String getActionCode() { return actionCode; }
    public void setActionCode(String actionCode) { this.actionCode = actionCode; }

    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
