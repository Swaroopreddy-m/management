package com.third.springboot.employee.hierarchy.service;

import com.third.springboot.employee.hierarchy.dto.PageRegistrationDto;
import com.third.springboot.employee.hierarchy.entity.PageAction;
import com.third.springboot.employee.hierarchy.entity.PageRegistry;
import com.third.springboot.employee.hierarchy.repository.PageActionRepository;
import com.third.springboot.employee.hierarchy.repository.PageRegistryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PageRegistryService {

    @Autowired
    private PageRegistryRepository pageRegistryRepository;

    @Autowired
    private PageActionRepository pageActionRepository;

    @Autowired
    private AuditLogService auditLogService;

    public List<PageRegistry> getAllPages() {
        return pageRegistryRepository.findAll();
    }

    public List<PageRegistry> getActivePages() {
        return pageRegistryRepository.findByActiveTrue();
    }

    public Optional<PageRegistry> getPage(String pageId) {
        return pageRegistryRepository.findByPageId(pageId);
    }

    public List<PageAction> getPageActions(String pageId) {
        return pageActionRepository.findByPageId(pageId);
    }

    @Transactional
    public PageRegistry registerPage(PageRegistrationDto dto, String developerId, String ipAddress) {
        String pageId = dto.getPageId().toUpperCase().trim();
        Optional<PageRegistry> existing = pageRegistryRepository.findByPageId(pageId);

        PageRegistry page;
        if (existing.isPresent()) {
            page = existing.get();
            page.setPageName(dto.getPageName());
            page.setDescription(dto.getDescription());
            page.setRoute(dto.getRoute());
            page.setCategory(dto.getCategory() != null ? dto.getCategory() : page.getCategory());
            page.setStatus("ACTIVE");
            page.setActive(true);
            page.setUpdatedAt(LocalDateTime.now());
        } else {
            page = new PageRegistry(pageId, dto.getPageName(), dto.getDescription(), dto.getRoute(),
                    dto.getCategory() != null ? dto.getCategory() : "CORE");
        }

        PageRegistry savedPage = pageRegistryRepository.save(page);

        // Register Actions
        if (dto.getAvailableActions() != null && !dto.getAvailableActions().isEmpty()) {
            for (String actionCode : dto.getAvailableActions()) {
                String code = actionCode.toUpperCase().trim();
                if (pageActionRepository.findByPageIdAndActionCode(pageId, code).isEmpty()) {
                    PageAction action = new PageAction(pageId, code, formatActionName(code), "Action " + code + " for " + page.getPageName());
                    pageActionRepository.save(action);
                }
            }
        } else {
            // Default VIEW action
            if (pageActionRepository.findByPageIdAndActionCode(pageId, "VIEW").isEmpty()) {
                pageActionRepository.save(new PageAction(pageId, "VIEW", "View Page", "Access and view page"));
            }
        }

        auditLogService.log(developerId, "Developer", "DEVELOPER", null, null,
                "PAGE_REGISTERED", pageId, "SUCCESS", "Registered page " + page.getPageName() + " (" + pageId + ")", ipAddress);

        return savedPage;
    }

    @Transactional
    public PageRegistry setPageStatus(String pageId, boolean active, String developerId, String ipAddress) {
        PageRegistry page = pageRegistryRepository.findByPageId(pageId)
                .orElseThrow(() -> new IllegalArgumentException("Page not found: " + pageId));

        page.setActive(active);
        page.setStatus(active ? "ACTIVE" : "INACTIVE");
        page.setUpdatedAt(LocalDateTime.now());
        PageRegistry updated = pageRegistryRepository.save(page);

        auditLogService.log(developerId, "Developer", "DEVELOPER", null, null,
                active ? "PAGE_ACTIVATED" : "PAGE_DEACTIVATED", pageId, "SUCCESS",
                "Page " + pageId + " status updated to " + (active ? "ACTIVE" : "INACTIVE"), ipAddress);

        return updated;
    }

    private String formatActionName(String code) {
        switch (code.toUpperCase()) {
            case "VIEW": return "View";
            case "CREATE": return "Create";
            case "UPDATE": return "Update";
            case "DELETE": return "Delete";
            case "EXPORT": return "Export";
            case "PRINT": return "Print";
            default: return code;
        }
    }
}
