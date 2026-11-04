package com.third.springboot.employee.hierarchy.controller;

import com.third.springboot.employee.hierarchy.dto.PageRegistrationDto;
import com.third.springboot.employee.hierarchy.entity.AuditLog;
import com.third.springboot.employee.hierarchy.entity.PageRegistry;
import com.third.springboot.employee.hierarchy.entity.SuperEntity;
import com.third.springboot.employee.hierarchy.service.AuditLogService;
import com.third.springboot.employee.hierarchy.service.HierarchyService;
import com.third.springboot.employee.hierarchy.service.PageRegistryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/developer")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class DeveloperController {

    @Autowired
    private PageRegistryService pageRegistryService;

    @Autowired
    private HierarchyService hierarchyService;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRegisteredPages", pageRegistryService.getAllPages().size());
        stats.put("activePages", pageRegistryService.getActivePages().size());
        stats.put("superEntities", hierarchyService.getAllSuperEntities().size());
        stats.put("superAdmins", hierarchyService.getAllSuperAdmins().size());
        stats.put("totalOrganizations", hierarchyService.getAllOrganizations().size());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/pages")
    public ResponseEntity<List<PageRegistry>> getAllPages() {
        return ResponseEntity.ok(pageRegistryService.getAllPages());
    }

    @PostMapping("/pages")
    public ResponseEntity<PageRegistry> registerPage(
            @RequestBody PageRegistrationDto dto,
            @RequestHeader(value = "X-Developer-Id", required = false) String devId,
            HttpServletRequest request) {

        String effectiveDev = devId != null ? devId : "DEV001";
        String ip = request != null ? request.getRemoteAddr() : "127.0.0.1";
        PageRegistry page = pageRegistryService.registerPage(dto, effectiveDev, ip);
        return ResponseEntity.ok(page);
    }

    @PutMapping("/pages/{pageId}/status")
    public ResponseEntity<PageRegistry> setPageStatus(
            @PathVariable String pageId,
            @RequestParam boolean active,
            @RequestHeader(value = "X-Developer-Id", required = false) String devId,
            HttpServletRequest request) {

        String effectiveDev = devId != null ? devId : "DEV001";
        String ip = request != null ? request.getRemoteAddr() : "127.0.0.1";
        PageRegistry page = pageRegistryService.setPageStatus(pageId, active, effectiveDev, ip);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/super-entities")
    public ResponseEntity<List<SuperEntity>> getSuperEntities() {
        return ResponseEntity.ok(hierarchyService.getAllSuperEntities());
    }

    @PostMapping("/super-entities")
    public ResponseEntity<SuperEntity> createSuperEntity(
            @RequestParam String name,
            @RequestParam String code,
            @RequestParam(required = false) String description,
            @RequestHeader(value = "X-Developer-Id", required = false) String devId,
            HttpServletRequest request) {

        String effectiveDev = devId != null ? devId : "DEV001";
        String ip = request != null ? request.getRemoteAddr() : "127.0.0.1";
        SuperEntity entity = hierarchyService.createSuperEntity(name, code, description, effectiveDev, ip);
        return ResponseEntity.ok(entity);
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditLogService.getRecentLogs());
    }
}
