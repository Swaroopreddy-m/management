package com.third.springboot.employee.hierarchy.controller;

import com.third.springboot.employee.hierarchy.dto.EffectivePermissionsDto;
import com.third.springboot.employee.hierarchy.service.EffectivePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/navigation")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class NavigationController {

    @Autowired
    private EffectivePermissionService effectivePermissionService;

    @GetMapping
    public ResponseEntity<EffectivePermissionsDto> getNavigation(
            @RequestHeader(value = "X-User-Id", defaultValue = "US001") String userId,
            @RequestHeader(value = "X-Organization-Id", defaultValue = "ORG001") String organizationId) {

        EffectivePermissionsDto permissions = effectivePermissionService.calculateEffectivePermissions(userId, organizationId);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyAccess(
            @RequestParam String pageId,
            @RequestParam(defaultValue = "VIEW") String action,
            @RequestHeader(value = "X-User-Id", defaultValue = "US001") String userId,
            @RequestHeader(value = "X-Organization-Id", defaultValue = "ORG001") String organizationId) {

        boolean permitted = effectivePermissionService.hasPermission(userId, organizationId, pageId, action);
        return ResponseEntity.ok(Map.of(
                "pageId", pageId,
                "action", action,
                "permitted", permitted
        ));
    }
}
