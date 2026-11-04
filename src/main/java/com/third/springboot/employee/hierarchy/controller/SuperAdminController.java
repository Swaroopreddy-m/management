package com.third.springboot.employee.hierarchy.controller;

import com.third.springboot.employee.hierarchy.dto.OrganizationDto;
import com.third.springboot.employee.hierarchy.dto.PageApprovalDto;
import com.third.springboot.employee.hierarchy.entity.OrgPagePermission;
import com.third.springboot.employee.hierarchy.entity.Organization;
import com.third.springboot.employee.hierarchy.entity.PageAccessRequest;
import com.third.springboot.employee.hierarchy.repository.OrgPagePermissionRepository;
import com.third.springboot.employee.hierarchy.service.HierarchyService;
import com.third.springboot.employee.hierarchy.service.PageAccessRequestService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/super-admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class SuperAdminController {

    @Autowired
    private HierarchyService hierarchyService;

    @Autowired
    private PageAccessRequestService pageAccessRequestService;

    @Autowired
    private OrgPagePermissionRepository orgPagePermissionRepository;

    @GetMapping("/organizations")
    public ResponseEntity<List<Organization>> getOrganizations(
            @RequestParam(required = false) String superAdminId) {
        if (superAdminId != null && !superAdminId.isBlank()) {
            return ResponseEntity.ok(hierarchyService.getOrganizationsBySuperAdmin(superAdminId));
        }
        return ResponseEntity.ok(hierarchyService.getAllOrganizations());
    }

    @PostMapping("/organizations")
    public ResponseEntity<Organization> createOrganization(
            @RequestBody OrganizationDto dto,
            @RequestHeader(value = "X-Super-Admin-Id", required = false) String saId,
            HttpServletRequest request) {

        String effectiveSa = saId != null ? saId : "SA001";
        if (dto.getSuperAdminId() == null || dto.getSuperAdminId().isBlank()) {
            dto.setSuperAdminId(effectiveSa);
        }
        if (dto.getSuperEntityId() == null || dto.getSuperEntityId().isBlank()) {
            dto.setSuperEntityId("SE001");
        }

        String ip = request != null ? request.getRemoteAddr() : "127.0.0.1";
        Organization org = hierarchyService.createOrganization(dto, effectiveSa, ip);
        return ResponseEntity.ok(org);
    }

    @PutMapping("/organizations/{orgId}/status")
    public ResponseEntity<Organization> updateOrganizationStatus(
            @PathVariable String orgId,
            @RequestParam String status,
            @RequestHeader(value = "X-Super-Admin-Id", required = false) String saId,
            HttpServletRequest request) {

        String effectiveSa = saId != null ? saId : "SA001";
        String ip = request != null ? request.getRemoteAddr() : "127.0.0.1";
        Organization org = hierarchyService.updateOrganizationStatus(orgId, status, effectiveSa, ip);
        return ResponseEntity.ok(org);
    }

    @GetMapping("/organizations/{orgId}/permissions")
    public ResponseEntity<List<OrgPagePermission>> getOrganizationPermissions(@PathVariable String orgId) {
        return ResponseEntity.ok(orgPagePermissionRepository.findByOrganizationId(orgId));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<PageAccessRequest>> getRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String organizationId) {

        if (organizationId != null && !organizationId.isBlank()) {
            return ResponseEntity.ok(pageAccessRequestService.getRequestsByOrganization(organizationId));
        }
        if ("PENDING".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(pageAccessRequestService.getPendingRequests());
        }
        return ResponseEntity.ok(pageAccessRequestService.getAllRequests());
    }

    @PostMapping("/requests/{requestId}/review")
    public ResponseEntity<PageAccessRequest> reviewRequest(
            @PathVariable String requestId,
            @RequestBody PageApprovalDto dto,
            @RequestHeader(value = "X-Super-Admin-Id", required = false) String saId,
            HttpServletRequest request) {

        String effectiveSa = saId != null ? saId : "SA001";
        String ip = request != null ? request.getRemoteAddr() : "127.0.0.1";
        PageAccessRequest result = pageAccessRequestService.reviewRequest(requestId, dto, effectiveSa, ip);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/organizations/{orgId}/pages/{pageId}/revoke")
    public ResponseEntity<OrgPagePermission> revokePage(
            @PathVariable String orgId,
            @PathVariable String pageId,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-Super-Admin-Id", required = false) String saId,
            HttpServletRequest request) {

        String reason = (body != null && body.containsKey("reason")) ? body.get("reason") : "Revoked by Super Admin";
        String effectiveSa = saId != null ? saId : "SA001";
        String ip = request != null ? request.getRemoteAddr() : "127.0.0.1";
        OrgPagePermission result = pageAccessRequestService.revokeOrganizationPage(orgId, pageId, reason, effectiveSa, ip);
        return ResponseEntity.ok(result);
    }
}
