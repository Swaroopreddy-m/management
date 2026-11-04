package com.third.springboot.employee.hierarchy.dto;

import java.util.Set;

public class MenuItemDto {

    private String pageId;
    private String pageName;
    private String route;
    private String category;
    private Set<String> actions;

    public MenuItemDto() {}

    public MenuItemDto(String pageId, String pageName, String route, String category, Set<String> actions) {
        this.pageId = pageId;
        this.pageName = pageName;
        this.route = route;
        this.category = category;
        this.actions = actions;
    }

    public String getPageId() { return pageId; }
    public void setPageId(String pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Set<String> getActions() { return actions; }
    public void setActions(Set<String> actions) { this.actions = actions; }
}
