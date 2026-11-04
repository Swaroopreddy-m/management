package com.third.springboot.employee.hierarchy.dto;

import java.util.List;

public class PageRegistrationDto {

    private String pageId;
    private String pageName;
    private String description;
    private String route;
    private String category;
    private List<String> availableActions; // e.g. ["VIEW", "CREATE", "UPDATE", "DELETE", "EXPORT"]

    public PageRegistrationDto() {}

    public PageRegistrationDto(String pageId, String pageName, String description, String route, String category, List<String> availableActions) {
        this.pageId = pageId;
        this.pageName = pageName;
        this.description = description;
        this.route = route;
        this.category = category;
        this.availableActions = availableActions;
    }

    public String getPageId() { return pageId; }
    public void setPageId(String pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getAvailableActions() { return availableActions; }
    public void setAvailableActions(List<String> availableActions) { this.availableActions = availableActions; }
}
