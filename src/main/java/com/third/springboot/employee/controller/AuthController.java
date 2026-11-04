package com.third.springboot.employee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.third.springboot.employee.service.AuthService;
import com.third.springboot.employee.dto.LoginRequest;
import com.third.springboot.employee.dto.LoginResponse;
import com.third.springboot.employee.dto.Signup;
import com.third.springboot.employee.entity.UserMaster;
import com.third.springboot.employee.hierarchy.dto.EffectivePermissionsDto;
import com.third.springboot.employee.hierarchy.service.AuditLogService;
import com.third.springboot.employee.hierarchy.service.EffectivePermissionService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = {"*"})
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private EffectivePermissionService effectivePermissionService;

    @Autowired
    private AuditLogService auditLogService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest != null ? httpRequest.getRemoteAddr() : "127.0.0.1";
        boolean isValid = authService.authenticate(
                request.getUserId(),
                request.getPassword()
        );

        if (!isValid) {
            auditLogService.log(request.getUserId(), "Unknown", "UNKNOWN", null, null,
                    "LOGIN_FAILED", "AUTH", "FAILURE", "Invalid credentials entered", ip);
            return ResponseEntity
                    .status(401)
                    .body(new LoginResponse("INVALID_CREDENTIALS"));
        }

        String userId = request.getUserId().trim();
        String role = "USER";
        String orgId = "ORG001";

        if ("developer".equalsIgnoreCase(userId) || "devroot".equalsIgnoreCase(userId)) {
            role = "DEVELOPER";
            orgId = "SYSTEM";
        } else if ("superadmin".equalsIgnoreCase(userId)) {
            role = "SUPER_ADMIN";
            orgId = "SYSTEM";
        } else {
            UserMaster user = authService.getUserMaster(userId);
            if (user != null) {
                role = user.getUserRole() != null ? user.getUserRole() : "USER";
                orgId = user.getOrganizationId() != null ? user.getOrganizationId() : "ORG001";
            }
        }

        EffectivePermissionsDto permissions = effectivePermissionService.calculateEffectivePermissions(userId, orgId);

        auditLogService.log(userId, permissions.getUserName(), role, orgId, null,
                "LOGIN_SUCCESS", "AUTH", "SUCCESS", "User logged in successfully", ip);

        LoginResponse response = new LoginResponse("LOGIN_SUCCESS", userId, role, orgId, permissions);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Signup signupDto, HttpServletRequest httpRequest) {
        String ip = httpRequest != null ? httpRequest.getRemoteAddr() : "127.0.0.1";
        try {
            UserMaster created = authService.registerUser(signupDto, ip);
            return ResponseEntity.ok(Map.of("message", "Signup successful", "customerId", created.getCustomerId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error registering user: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<EffectivePermissionsDto> getCurrentUser(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String organizationId) {

        String effectiveUser = (userId != null && !userId.isBlank()) ? userId : "US001";
        String effectiveOrg = (organizationId != null && !organizationId.isBlank()) ? organizationId : "ORG001";

        EffectivePermissionsDto permissions = effectivePermissionService.calculateEffectivePermissions(effectiveUser, effectiveOrg);
        return ResponseEntity.ok(permissions);
    }
}
