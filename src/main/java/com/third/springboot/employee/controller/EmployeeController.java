package com.third.springboot.employee.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.third.springboot.employee.entity.Employee;
import com.third.springboot.employee.hierarchy.service.AuditLogService;
import com.third.springboot.employee.hierarchy.service.EffectivePermissionService;
import com.third.springboot.employee.service.EmployeeService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class EmployeeController {

    private final EmployeeService service;

    @Autowired
    private EffectivePermissionService permissionService;

    @Autowired
    private AuditLogService auditLogService;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody Employee emp,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Organization-Id", required = false) String orgId,
            HttpServletRequest request) {

        String effectiveUser = userId != null ? userId : "US001";
        String effectiveOrg = (orgId != null && !orgId.isBlank()) 
                ? orgId 
                : (emp.getOrganizationId() != null ? emp.getOrganizationId() : "ORG001");

        // Verify CREATE permission on EMPLOYEE_MANAGEMENT
        if (!"admin".equalsIgnoreCase(effectiveUser) && !permissionService.hasPermission(effectiveUser, effectiveOrg, "EMPLOYEE_MANAGEMENT", "CREATE")) {
            auditLogService.log(effectiveUser, "User", "USER", effectiveOrg, emp.getEntityId(),
                    "SECURITY_ALERT_UNAUTHORIZED_ACTION", "EMPLOYEE_MANAGEMENT", "DENIED",
                    "Attempted to CREATE employee without authorization", request != null ? request.getRemoteAddr() : null);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized: CREATE action on EMPLOYEE_MANAGEMENT is not granted.");
        }

        // Verify entity scope
        if (emp.getEntityId() != null && !permissionService.isAuthorizedForEntity(effectiveUser, effectiveOrg, emp.getEntityId())) {
            auditLogService.log(effectiveUser, "User", "USER", effectiveOrg, emp.getEntityId(),
                    "SECURITY_ALERT_UNAUTHORIZED_ENTITY", emp.getEntityId(), "DENIED",
                    "Attempted to create employee in unauthorized entity: " + emp.getEntityId(), request != null ? request.getRemoteAddr() : null);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized: Entity " + emp.getEntityId() + " is outside your permitted scope.");
        }

        emp.setOrganizationId(effectiveOrg);
        Employee saved = service.saveEmployee(emp);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getAll(
            @RequestParam(required = false) String organizationId,
            @RequestParam(required = false) String entityId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Organization-Id", required = false) String headerOrgId,
            HttpServletRequest request) {

        String effectiveUser = userId != null ? userId : "US001";
        String effectiveOrg = organizationId != null ? organizationId : (headerOrgId != null ? headerOrgId : "ORG001");

        // Verify VIEW permission on EMPLOYEE_MANAGEMENT
        if (!"admin".equalsIgnoreCase(effectiveUser) && !permissionService.hasPermission(effectiveUser, effectiveOrg, "EMPLOYEE_MANAGEMENT", "VIEW")) {
            auditLogService.log(effectiveUser, "User", "USER", effectiveOrg, entityId,
                    "SECURITY_ALERT_UNAUTHORIZED_PAGE", "EMPLOYEE_MANAGEMENT", "DENIED",
                    "Attempted to view employees without VIEW permission", request != null ? request.getRemoteAddr() : null);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized: VIEW action on EMPLOYEE_MANAGEMENT is not granted.");
        }

        // Check Entity scope restriction if specified
        if (entityId != null && !entityId.isBlank()) {
            if (!permissionService.isAuthorizedForEntity(effectiveUser, effectiveOrg, entityId)) {
                auditLogService.log(effectiveUser, "User", "USER", effectiveOrg, entityId,
                        "SECURITY_ALERT_UNAUTHORIZED_ENTITY", entityId, "DENIED",
                        "Attempted to query employees in unauthorized entity: " + entityId, request != null ? request.getRemoteAddr() : null);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized: Access to entity " + entityId + " is denied.");
            }
            return ResponseEntity.ok(service.getEmployeesByOrgAndEntity(effectiveOrg, entityId));
        }

        return ResponseEntity.ok(service.getEmployeesByOrg(effectiveOrg));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getById(
            @PathVariable Long id,
            @RequestParam(required = false) String organizationId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Organization-Id", required = false) String headerOrgId,
            HttpServletRequest request) {

        String effectiveUser = userId != null ? userId : "US001";
        String effectiveOrg = organizationId != null ? organizationId : (headerOrgId != null ? headerOrgId : "ORG001");

        Employee emp = service.getEmployeeById(id);
        if (emp == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }

        // Multi-tenant check: Prevent cross-organization data retrieval
        if (emp.getOrganizationId() != null && !emp.getOrganizationId().equals(effectiveOrg)) {
            auditLogService.log(effectiveUser, "User", "USER", effectiveOrg, null,
                    "SECURITY_ALERT_CROSS_ORGANIZATION_ACCESS", "Employee ID " + id, "DENIED",
                    "Attempted cross-organization access to record in org " + emp.getOrganizationId(), request != null ? request.getRemoteAddr() : null);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Cross-organization access violation.");
        }

        // Entity-level check: Verify user is authorized for employee's entity
        if (emp.getEntityId() != null && !permissionService.isAuthorizedForEntity(effectiveUser, effectiveOrg, emp.getEntityId())) {
            auditLogService.log(effectiveUser, "User", "USER", effectiveOrg, emp.getEntityId(),
                    "SECURITY_ALERT_UNAUTHORIZED_ENTITY", emp.getEntityId(), "DENIED",
                    "Attempted to access employee record in unauthorized entity: " + emp.getEntityId(), request != null ? request.getRemoteAddr() : null);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Entity scope restriction.");
        }

        return ResponseEntity.ok(emp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @PathVariable Long id,
            @RequestParam(required = false) String organizationId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Organization-Id", required = false) String headerOrgId,
            HttpServletRequest request) {

        String effectiveUser = userId != null ? userId : "US001";
        String effectiveOrg = organizationId != null ? organizationId : (headerOrgId != null ? headerOrgId : "ORG001");

        // Verify DELETE action on EMPLOYEE_MANAGEMENT
        if (!"admin".equalsIgnoreCase(effectiveUser) && !permissionService.hasPermission(effectiveUser, effectiveOrg, "EMPLOYEE_MANAGEMENT", "DELETE")) {
            auditLogService.log(effectiveUser, "User", "USER", effectiveOrg, null,
                    "SECURITY_ALERT_UNAUTHORIZED_ACTION", "EMPLOYEE_MANAGEMENT", "DENIED",
                    "Attempted DELETE action on EMPLOYEE_MANAGEMENT without permission", request != null ? request.getRemoteAddr() : null);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized: DELETE action on EMPLOYEE_MANAGEMENT is not granted.");
        }

        Employee emp = service.getEmployeeById(id);
        if (emp != null && emp.getOrganizationId() != null && !emp.getOrganizationId().equals(effectiveOrg)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Cross-organization modification violation.");
        }

        service.deleteEmployee(id);
        return ResponseEntity.ok("Deleted successfully");
    }
}
