package com.third.springboot.employee.hierarchy.controller;

import com.third.springboot.employee.entity.UserMaster;
import com.third.springboot.employee.hierarchy.dto.GroupActionAssignmentDto;
import com.third.springboot.employee.hierarchy.dto.OrgEntityDto;
import com.third.springboot.employee.hierarchy.dto.PageAccessRequestDto;
import com.third.springboot.employee.hierarchy.dto.PermissionGroupDto;
import com.third.springboot.employee.hierarchy.entity.*;
import com.third.springboot.employee.hierarchy.repository.OrgPagePermissionRepository;
import com.third.springboot.employee.hierarchy.repository.PageRegistryRepository;
import com.third.springboot.employee.hierarchy.service.HierarchyService;
import com.third.springboot.employee.hierarchy.service.PageAccessRequestService;
import com.third.springboot.employee.hierarchy.service.PermissionGroupService;
import com.third.springboot.employee.repository.UserMasterRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/org-admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class OrgAdminController {

    @Autowired
    private HierarchyService hierarchyService;

    @Autowired
    private PermissionGroupService permissionGroupService;

    @Autowired
    private PageAccessRequestService pageAccessRequestService;

    @Autowired
    private OrgPagePermissionRepository orgPagePermissionRepository;

    @Autowired
    private PageRegistryRepository pageRegistryRepository;

    @Autowired
    private UserMasterRepository userMasterRepository;

    // --- Approved Pages for Organization ---
    @GetMapping("/approved-pages")
    public ResponseEntity<List<Map<String, Object>>> getApprovedPages(
            @RequestHeader(value = "X-Organization-Id", defaultValue = "ORG001") String organizationId) {

        List<OrgPagePermission> permissions = orgPagePermissionRepository
                .findByOrganizationIdAndStatus(organizationId, "ACTIVE");

        List<Map<String, Object>> result = new ArrayList<>();
        for (OrgPagePermission p : permissions) {
            PageRegistry page = pageRegistryRepository.findByPageId(p.getPageId()).orElse(null);
            result.add(Map.of(
                    "pageId", p.getPageId(),
                    "pageName", page != null ? page.getPageName() : p.getPageId(),
                    "route", page != null ? page.getRoute() : "",
                    "allowedActions", p.getAllowedActions(),
                    "status", p.getStatus()
            ));
        }
        return ResponseEntity.ok(result);
    }

    // --- Organizational Entities (Branches, Departments, Stores) ---
    @GetMapping("/entities")
    public ResponseEntity<List<OrgEntity>> getEntities(
            @RequestHeader(value = "X-Organization-Id", defaultValue = "ORG001") String organizationId) {
        return ResponseEntity.ok(hierarchyService.getEntitiesByOrganization(organizationId));
    }

    @PostMapping("/entities")
    public ResponseEntity<OrgEntity> createEntity(
            @RequestBody OrgEntityDto dto,
            @RequestHeader(value = "X-Organization-Id", defaultValue = "ORG001") String organizationId,
            @RequestHeader(value = "X-User-Id", defaultValue = "US001") String actorId,
            HttpServletRequest request) {

        String ip = request != null ? request.getRemoteAddr() : "127.0.0.1";
        OrgEntity entity = hierarchyService.createOrgEntity(organizationId, dto, actorId, ip);
        return ResponseEntity.ok(entity);
    }

    // --- Permission Groups ---
    @GetMapping("/groups")
    public ResponseEntity<List<PermissionGroup>> getGroups(
            @RequestHeader(value = "X-Organization-Id", defaultValue = "ORG001") String organizationId) {
        return ResponseEntity.ok(permissionGroupService.getGroupsByOrganization(organizationId));
    }

    @PostMapping("/groups")
    public ResponseEntity<PermissionGroup> createGroup(
            @RequestBody PermissionGroupDto dto,
            @RequestHeader(value = "X-Organization-Id", defaultValue = "ORG001") String organizationId,
            @RequestHeader(value = "X-User-Id", defaultValue = "US001") String actorId,
            HttpServletRequest request) {

        String ip = request != null ? request.getRemoteAddr() : "127.0.0.1";
        PermissionGroup group = permissionGroupService.createGroup(organizationId, dto, actorId, ip);
        return ResponseEntity.ok(group);
    }

    @GetMapping("/groups/{groupId}/permissions")
    public ResponseEntity<List<GroupPermission>> getGroupPermissions(@PathVariable String groupId) {
        return ResponseEntity.ok(permissionGroupService.getGroupPermissions(groupId));
    }

    @PostMapping("/groups/{groupId}/permissions")
    public ResponseEntity<?> assignPageActionsToGroup(
            @PathVariable String groupId,
            @RequestBody GroupActionAssignmentDto dto,
            @RequestHeader(value = "X-Organization-Id", defaultValue = "ORG001") String organizationId,
            @RequestHeader(value = "X-User-Id", defaultValue = "US001") String actorId,
            HttpServletRequest request) {

        String ip = request != null ? request.getRemoteAddr() : "127.0.0.1";
        try {
            GroupPermission gp = permissionGroupService.assignPageActionsToGroup(organizationId, groupId, dto, actorId, ip);
            return ResponseEntity.ok(gp);
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/groups/{groupId}/permissions/{pageId}")
    public ResponseEntity<Map<String, String>> removeGroupPermission(
            @PathVariable String groupId,
            @PathVariable String pageId,
            @RequestHeader(value = "X-Organization-Id", defaultValue = "ORG001") String organizationId,
            @RequestHeader(value = "X-User-Id", defaultValue = "US001") String actorId,
            HttpServletRequest request) {

        String ip = request != null ? request.getRemoteAddr() : "127.0.0.1";
        permissionGroupService.removeGroupPermission(organizationId, groupId, pageId, actorId, ip);
        return ResponseEntity.ok(Map.of("message", "Permission removed from group successfully"));
    }

    // --- User Group Assignments ---
    @PostMapping("/groups/{groupId}/users/{userId}")
    public ResponseEntity<UserGroupAssignment> assignUserToGroup(
            @PathVariable String groupId,
            @PathVariable String userId,
            @RequestHeader(value = "X-Organization-Id", defaultValue = "ORG001") String organizationId,
            @RequestHeader(value = "X-User-Id", defaultValue = "US001") String actorId,
            HttpServletRequest request) {

        String ip = request != null ? request.getRemoteAddr() : "127.0.0.1";
        UserGroupAssignment assignment = permissionGroupService.assignUserToGroup(organizationId, userId, groupId, actorId, ip);
        return ResponseEntity.ok(assignment);
    }

    @DeleteMapping("/groups/{groupId}/users/{userId}")
    public ResponseEntity<Map<String, String>> removeUserFromGroup(
            @PathVariable String groupId,
            @PathVariable String userId,
            @RequestHeader(value = "X-Organization-Id", defaultValue = "ORG001") String organizationId,
            @RequestHeader(value = "X-User-Id", defaultValue = "US001") String actorId,
            HttpServletRequest request) {

        String ip = request != null ? request.getRemoteAddr() : "127.0.0.1";
        permissionGroupService.removeUserFromGroup(organizationId, userId, groupId, actorId, ip);
        return ResponseEntity.ok(Map.of("message", "User removed from group successfully"));
    }

    // --- User Entity Assignments ---
    @PostMapping("/entities/{entityId}/users/{userId}")
    public ResponseEntity<UserEntityAssignment> assignUserToEntity(
            @PathVariable String entityId,
            @PathVariable String userId,
            @RequestHeader(value = "X-Organization-Id", defaultValue = "ORG001") String organizationId,
            @RequestHeader(value = "X-User-Id", defaultValue = "US001") String actorId,
            HttpServletRequest request) {

        String ip = request != null ? request.getRemoteAddr() : "127.0.0.1";
        UserEntityAssignment assignment = hierarchyService.assignUserToEntity(organizationId, userId, entityId, actorId, ip);
        return ResponseEntity.ok(assignment);
    }

    @DeleteMapping("/entities/{entityId}/users/{userId}")
    public ResponseEntity<Map<String, String>> removeUserFromEntity(
            @PathVariable String entityId,
            @PathVariable String userId,
            @RequestHeader(value = "X-Organization-Id", defaultValue = "ORG001") String organizationId,
            @RequestHeader(value = "X-User-Id", defaultValue = "US001") String actorId,
            HttpServletRequest request) {

        String ip = request != null ? request.getRemoteAddr() : "127.0.0.1";
        hierarchyService.removeUserFromEntity(organizationId, userId, entityId, actorId, ip);
        return ResponseEntity.ok(Map.of("message", "User removed from entity successfully"));
    }

    // --- Page Access Requests ---
    @PostMapping("/requests")
    public ResponseEntity<PageAccessRequest> submitRequest(
            @RequestBody PageAccessRequestDto dto,
            @RequestHeader(value = "X-Organization-Id", defaultValue = "ORG001") String organizationId,
            @RequestHeader(value = "X-User-Id", defaultValue = "US001") String actorId,
            HttpServletRequest request) {

        String ip = request != null ? request.getRemoteAddr() : "127.0.0.1";
        PageAccessRequest req = pageAccessRequestService.submitRequest(organizationId, dto, actorId, ip);
        return ResponseEntity.ok(req);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<PageAccessRequest>> getRequests(
            @RequestHeader(value = "X-Organization-Id", defaultValue = "ORG001") String organizationId) {
        return ResponseEntity.ok(pageAccessRequestService.getRequestsByOrganization(organizationId));
    }

    // --- User Management & Privilege Escalation Protection ---
    @GetMapping("/users")
    public ResponseEntity<List<UserMaster>> getUsers(
            @RequestHeader(value = "X-Organization-Id", defaultValue = "ORG001") String organizationId) {
        return ResponseEntity.ok(userMasterRepository.findByOrganizationId(organizationId));
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable String userId,
            @RequestParam String role,
            @RequestHeader(value = "X-Organization-Id", defaultValue = "ORG001") String organizationId,
            @RequestHeader(value = "X-User-Id", defaultValue = "US001") String actorId,
            @RequestHeader(value = "X-User-Role", defaultValue = "ORG_ADMIN") String actorRole,
            HttpServletRequest request) {

        String ip = request != null ? request.getRemoteAddr() : "127.0.0.1";
        try {
            UserMaster updated = hierarchyService.updateUserRole(organizationId, userId, role, actorRole, actorId, ip);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }
}
