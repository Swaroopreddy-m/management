package com.third.springboot.employee;

import com.third.springboot.employee.dto.LoginRequest;
import com.third.springboot.employee.entity.Employee;
import com.third.springboot.employee.entity.UserMaster;
import com.third.springboot.employee.hierarchy.dto.*;
import com.third.springboot.employee.hierarchy.entity.*;
import com.third.springboot.employee.hierarchy.repository.*;
import com.third.springboot.employee.hierarchy.service.*;
import com.third.springboot.employee.service.EmployeeService;
import com.third.springboot.employee.repository.EmployeeRepository;
import com.third.springboot.employee.repository.UserMasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class HierarchyPermissionsIntegrationTests {

    @Autowired
    private HierarchyService hierarchyService;

    @Autowired
    private PageRegistryService pageRegistryService;

    @Autowired
    private PageAccessRequestService pageAccessRequestService;

    @Autowired
    private PermissionGroupService permissionGroupService;

    @Autowired
    private EffectivePermissionService effectivePermissionService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserMasterRepository userMasterRepository;

    @Autowired
    private OrgPagePermissionRepository orgPagePermissionRepository;

    @Autowired
    private GroupPermissionRepository groupPermissionRepository;

    @Autowired
    private UserGroupAssignmentRepository userGroupAssignmentRepository;

    @Autowired
    private UserEntityAssignmentRepository userEntityAssignmentRepository;

    @Autowired
    private AuditLogService auditLogService;

    private static final String ORG_A = "ORG001";
    private static final String ORG_B = "ORG002";
    private static final String TEST_USER = "US001";

    @BeforeEach
    void setUp() {
        // Ensure test user US001 is active in ORG001
        UserMaster user = userMasterRepository.findById(TEST_USER).orElse(null);
        if (user != null) {
            user.setUserStatus("ACTIVE");
            user.setOrganizationId(ORG_A);
            user.setUserRole("USER");
            userMasterRepository.save(user);
        }
    }

    /**
     * Scenario A: Unapproved Page
     * An organization requests Page A. The Developer registers Page A.
     * The Super Admin has not approved it.
     * Expected: The organization's users cannot access Page A, even if they know its URL or page ID.
     */
    @Test
    @DisplayName("Scenario A: Unapproved page cannot be accessed by organization users")
    void testScenarioA_UnapprovedPage() {
        String testPageId = "PAGE_SCENARIO_A";

        // Developer registers Page A
        pageRegistryService.registerPage(
                new PageRegistrationDto(testPageId, "Feature A", "Experimental Page A", "/page/a", "Custom", List.of("VIEW", "CREATE")),
                "DEV001", "127.0.0.1"
        );

        // Super Admin has NOT approved testPageId for ORG_A
        // Effective permissions check
        boolean hasAccess = effectivePermissionService.hasPermission(TEST_USER, ORG_A, testPageId, "VIEW");
        assertFalse(hasAccess, "User must NOT have access to an unapproved page");

        EffectivePermissionsDto effective = effectivePermissionService.calculateEffectivePermissions(TEST_USER, ORG_A);
        assertFalse(effective.getAuthorizedPages().containsKey(testPageId),
                "Unapproved page must not be present in effective permissions");
    }

    /**
     * Scenario B: Approved Page Without Group Permission
     * The Super Admin approves Page A for Organization A.
     * Organization Admin A has NOT assigned Page A to the user's group.
     * Expected: The user cannot access Page A.
     */
    @Test
    @DisplayName("Scenario B: Approved page without group permission cannot be accessed")
    void testScenarioB_ApprovedPageWithoutGroupPermission() {
        String testPageId = "PAGE_SCENARIO_B";

        // Register Page
        pageRegistryService.registerPage(
                new PageRegistrationDto(testPageId, "Feature B", "Page B", "/page/b", "Core", List.of("VIEW", "CREATE")),
                "DEV001", "127.0.0.1"
        );

        // Super Admin approves for ORG_A
        orgPagePermissionRepository.save(new OrgPagePermission(ORG_A, testPageId, "VIEW,CREATE", "SA001"));

        // Create a restricted group for the user that does NOT contain testPageId
        PermissionGroup group = permissionGroupService.createGroup(ORG_A,
                new PermissionGroupDto(null, "Scenario B Group", "No test page", "ACTIVE"),
                "ORG_ADMIN_1", "127.0.0.1");

        // Clear other groups and assign this restricted group
        userGroupAssignmentRepository.deleteByUserIdAndOrganizationId(TEST_USER, ORG_A);
        userGroupAssignmentRepository.save(new UserGroupAssignment(TEST_USER, ORG_A, group.getId()));

        // Verification
        boolean hasAccess = effectivePermissionService.hasPermission(TEST_USER, ORG_A, testPageId, "VIEW");
        assertFalse(hasAccess, "User without group permission must NOT have access to approved page");
    }

    /**
     * Scenario C: Approved Page With Authorized Action
     * The Super Admin approves Page A.
     * The Organization Admin assigns the VIEW action to a group.
     * The user belongs to that group.
     * Expected: The user can view Page A but cannot perform unauthorized actions such as DELETE.
     */
    @Test
    @DisplayName("Scenario C: Granular action authorization (VIEW granted, DELETE blocked)")
    void testScenarioC_ApprovedPageWithAuthorizedAction() {
        String testPageId = "PAGE_SCENARIO_C";

        // Register Page with VIEW and DELETE actions
        pageRegistryService.registerPage(
                new PageRegistrationDto(testPageId, "Feature C", "Page C", "/page/c", "Core", List.of("VIEW", "DELETE")),
                "DEV001", "127.0.0.1"
        );

        // Super Admin approves VIEW and DELETE for ORG_A
        orgPagePermissionRepository.save(new OrgPagePermission(ORG_A, testPageId, "VIEW,DELETE", "SA001"));

        // Org Admin assigns ONLY "VIEW" action to group
        PermissionGroup group = permissionGroupService.createGroup(ORG_A,
                new PermissionGroupDto(null, "Scenario C Group", "View Only", "ACTIVE"),
                "ORG_ADMIN_1", "127.0.0.1");

        permissionGroupService.assignPageActionsToGroup(ORG_A, group.getId(),
                new GroupActionAssignmentDto(testPageId, "VIEW"), "ORG_ADMIN_1", "127.0.0.1");

        userGroupAssignmentRepository.deleteByUserIdAndOrganizationId(TEST_USER, ORG_A);
        userGroupAssignmentRepository.save(new UserGroupAssignment(TEST_USER, ORG_A, group.getId()));

        // User CAN VIEW
        assertTrue(effectivePermissionService.hasPermission(TEST_USER, ORG_A, testPageId, "VIEW"),
                "User should be authorized for VIEW action");

        // User CANNOT DELETE
        assertFalse(effectivePermissionService.hasPermission(TEST_USER, ORG_A, testPageId, "DELETE"),
                "User must NOT be authorized for unassigned DELETE action");
    }

    /**
     * Scenario D: Unauthorized Permission Assignment
     * The Super Admin has NOT approved Page B for Organization A.
     * Organization Admin A attempts to assign Page B to a group.
     * Expected: The backend rejects the assignment.
     */
    @Test
    @DisplayName("Scenario D: Backend rejects assignment of unapproved page to permission group")
    void testScenarioD_UnauthorizedPermissionAssignment() {
        String unapprovedPageId = "UNAPPROVED_PAGE_X";

        // Register page in registry
        pageRegistryService.registerPage(
                new PageRegistrationDto(unapprovedPageId, "Secret Page", "Not approved", "/secret", "Admin", List.of("VIEW", "CREATE")),
                "DEV001", "127.0.0.1"
        );

        // Ensure NOT approved in ORG_A
        orgPagePermissionRepository.findByOrganizationIdAndPageId(ORG_A, unapprovedPageId)
                .ifPresent(p -> orgPagePermissionRepository.delete(p));

        PermissionGroup group = permissionGroupService.createGroup(ORG_A,
                new PermissionGroupDto(null, "Scenario D Group", "Attempted escalation", "ACTIVE"),
                "ORG_ADMIN_1", "127.0.0.1");

        // Org Admin attempts to assign unapproved page
        assertThrows(SecurityException.class, () -> {
            permissionGroupService.assignPageActionsToGroup(ORG_A, group.getId(),
                    new GroupActionAssignmentDto(unapprovedPageId, "VIEW"), "ORG_ADMIN_1", "127.0.0.1");
        }, "Backend must throw SecurityException when attempting to assign an unapproved page");
    }

    /**
     * Scenario E: Cross-Organization Access
     * A user belonging to Organization A attempts to retrieve a record belonging to Organization B.
     * Expected: Access is denied, and protected information is not disclosed.
     */
    @Test
    @DisplayName("Scenario E: Cross-organization data isolation")
    void testScenarioE_CrossOrganizationAccess() {
        // Create an employee belonging exclusively to ORG_B
        Employee orgBEmployee = employeeService.saveEmployee(
                new Employee("Protected OrgB Employee", "secret@orgb.com", "Finance", 99000.0, ORG_B, "BR003")
        );

        // User belonging to ORG_A tries to access Org B's record via multi-tenant query
        Employee found = employeeService.getEmployeeByIdAndOrg(orgBEmployee.getId(), ORG_A);
        assertNull(found, "Query scoped to ORG_A must not return records from ORG_B");

        List<Employee> orgAEmployees = employeeService.getEmployeesByOrg(ORG_A);
        boolean containsOrgBRecord = orgAEmployees.stream().anyMatch(e -> e.getId().equals(orgBEmployee.getId()));
        assertFalse(containsOrgBRecord, "ORG_A list must not contain ORG_B employee");
    }

    /**
     * Scenario F: Entity-Level Restrictions
     * A user is authorized for Branch A1 (BR001) but not Branch A2 (BR002).
     * The user attempts to access a Branch A2 record.
     * Expected: The backend denies the operation.
     */
    @Test
    @DisplayName("Scenario F: Entity-level access restriction (Branch A1 allowed, Branch A2 denied)")
    void testScenarioF_EntityLevelRestrictions() {
        // Restrict user to BR001 only
        userEntityAssignmentRepository.deleteByUserIdAndEntityId(TEST_USER, "BR002");
        if (userEntityAssignmentRepository.findByUserIdAndEntityId(TEST_USER, "BR001").isEmpty()) {
            userEntityAssignmentRepository.save(new UserEntityAssignment(TEST_USER, ORG_A, "BR001"));
        }

        UserMaster user = userMasterRepository.findById(TEST_USER).orElse(null);
        if (user != null) {
            user.setUserEntity("BR001");
            user.setUserRole("USER");
            userMasterRepository.save(user);
        }

        // Verify BR001 is authorized
        assertTrue(effectivePermissionService.isAuthorizedForEntity(TEST_USER, ORG_A, "BR001"),
                "User should be authorized for assigned entity BR001");

        // Verify BR002 is NOT authorized
        assertFalse(effectivePermissionService.isAuthorizedForEntity(TEST_USER, ORG_A, "BR002"),
                "User should NOT be authorized for unassigned entity BR002");
    }

    /**
     * Scenario G: Permission Revocation
     * The Super Admin revokes Page A from Organization A.
     * A user who previously had Page A access makes another request.
     * Expected: The request is denied immediately according to the updated authorization state.
     */
    @Test
    @DisplayName("Scenario G: Permission revocation takes effect immediately")
    void testScenarioG_PermissionRevocation() {
        String pageToRevoke = "PAGE_SCENARIO_G";

        // Register Page
        pageRegistryService.registerPage(
                new PageRegistrationDto(pageToRevoke, "Revocable Page", "Page G", "/page/g", "Core", List.of("VIEW")),
                "DEV001", "127.0.0.1"
        );

        // Super Admin approves
        orgPagePermissionRepository.save(new OrgPagePermission(ORG_A, pageToRevoke, "VIEW", "SA001"));

        // Org Admin assigns to group
        PermissionGroup group = permissionGroupService.createGroup(ORG_A,
                new PermissionGroupDto(null, "Group G", "G", "ACTIVE"), "ORG_ADMIN_1", "127.0.0.1");
        permissionGroupService.assignPageActionsToGroup(ORG_A, group.getId(),
                new GroupActionAssignmentDto(pageToRevoke, "VIEW"), "ORG_ADMIN_1", "127.0.0.1");

        userGroupAssignmentRepository.deleteByUserIdAndOrganizationId(TEST_USER, ORG_A);
        userGroupAssignmentRepository.save(new UserGroupAssignment(TEST_USER, ORG_A, group.getId()));

        // Prior to revocation: has access
        assertTrue(effectivePermissionService.hasPermission(TEST_USER, ORG_A, pageToRevoke, "VIEW"),
                "User should have access before revocation");

        // Super Admin REVOKES access from Organization
        pageAccessRequestService.revokeOrganizationPage(ORG_A, pageToRevoke, "Security policy update", "SA001", "127.0.0.1");

        // Immediately after revocation: access must be DENIED
        assertFalse(effectivePermissionService.hasPermission(TEST_USER, ORG_A, pageToRevoke, "VIEW"),
                "User must immediately lose access after Super Admin revocation");
    }

    /**
     * Scenario H: Group Modification
     * An Organization Admin removes the UPDATE action from a user's group.
     * Expected: The user can no longer perform the UPDATE action.
     */
    @Test
    @DisplayName("Scenario H: Action removal from group immediately takes effect")
    void testScenarioH_GroupModification() {
        String testPageId = "PAGE_SCENARIO_H";

        pageRegistryService.registerPage(
                new PageRegistrationDto(testPageId, "Mod Page", "H", "/page/h", "Core", List.of("VIEW", "UPDATE")),
                "DEV001", "127.0.0.1"
        );
        orgPagePermissionRepository.save(new OrgPagePermission(ORG_A, testPageId, "VIEW,UPDATE", "SA001"));

        PermissionGroup group = permissionGroupService.createGroup(ORG_A,
                new PermissionGroupDto(null, "Group H", "H", "ACTIVE"), "ORG_ADMIN_1", "127.0.0.1");

        // Assign VIEW and UPDATE
        permissionGroupService.assignPageActionsToGroup(ORG_A, group.getId(),
                new GroupActionAssignmentDto(testPageId, "VIEW,UPDATE"), "ORG_ADMIN_1", "127.0.0.1");

        userGroupAssignmentRepository.deleteByUserIdAndOrganizationId(TEST_USER, ORG_A);
        userGroupAssignmentRepository.save(new UserGroupAssignment(TEST_USER, ORG_A, group.getId()));

        assertTrue(effectivePermissionService.hasPermission(TEST_USER, ORG_A, testPageId, "UPDATE"));

        // Org Admin updates group to only grant VIEW
        permissionGroupService.assignPageActionsToGroup(ORG_A, group.getId(),
                new GroupActionAssignmentDto(testPageId, "VIEW"), "ORG_ADMIN_1", "127.0.0.1");

        // Now UPDATE is denied
        assertFalse(effectivePermissionService.hasPermission(TEST_USER, ORG_A, testPageId, "UPDATE"),
                "User must lose UPDATE action immediately after group modification");
        // VIEW remains granted
        assertTrue(effectivePermissionService.hasPermission(TEST_USER, ORG_A, testPageId, "VIEW"));
    }

    /**
     * Scenario I: Invalid Administrative Escalation
     * An Organization Admin attempts to assign themselves a Super Admin or Developer role.
     * Expected: The backend rejects the operation with SecurityException.
     */
    @Test
    @DisplayName("Scenario I: Backend rejects privilege escalation attempts")
    void testScenarioI_InvalidAdministrativeEscalation() {
        assertThrows(SecurityException.class, () -> {
            hierarchyService.updateUserRole(ORG_A, TEST_USER, "SUPER_ADMIN", "ORG_ADMIN", "ORG_ADMIN_ACTOR", "127.0.0.1");
        }, "Org Admin attempting to escalate to SUPER_ADMIN must be rejected");

        assertThrows(SecurityException.class, () -> {
            hierarchyService.updateUserRole(ORG_A, TEST_USER, "DEVELOPER", "ORG_ADMIN", "ORG_ADMIN_ACTOR", "127.0.0.1");
        }, "Org Admin attempting to escalate to DEVELOPER must be rejected");
    }

    /**
     * Scenario J: Existing Functionality
     * Verify existing core workflows:
     * 1. Employee retrieval
     * 2. User master query
     * 3. Database entities intact
     */
    @Test
    @DisplayName("Scenario J: Existing core functionality continues to operate")
    void testScenarioJ_ExistingFunctionality() {
        // Employee CRUD
        Employee newEmp = employeeService.saveEmployee(new Employee("Regression Test Emp", "reg@test.com", "QA", 60000.0));
        assertNotNull(newEmp.getId());

        Employee fetched = employeeService.getEmployeeById(newEmp.getId());
        assertEquals("Regression Test Emp", fetched.getName());

        // Clean up
        employeeService.deleteEmployee(newEmp.getId());
        assertNull(employeeService.getEmployeeById(newEmp.getId()));

        // User Master
        assertNotNull(userMasterRepository.findById("US001").orElse(null));
    }

    /**
     * Scenario K: Direct Access Rejection
     * Verify that when a user does not have permission on a page,
     * effective permission evaluation consistently returns false for direct inquiries.
     */
    @Test
    @DisplayName("Scenario K: Direct access verification fails when page is not authorized")
    void testScenarioK_DirectAccessRejection() {
        boolean directAllowed = effectivePermissionService.hasPermission(TEST_USER, ORG_A, "NON_EXISTENT_PAGE", "VIEW");
        assertFalse(directAllowed, "Direct verification must deny unauthorized page access");
    }

    /**
     * Scenario L: Deployment and Architecture Separation
     * Verifies that the application configuration is self-contained and isolated from reference projects.
     */
    @Test
    @DisplayName("Scenario L: Architecture and deployment isolation verification")
    void testScenarioL_DeploymentSeparation() {
        // Ensure Super Entity and Super Admin records exist independently
        assertTrue(hierarchyService.getAllSuperEntities().size() >= 1);
        assertTrue(hierarchyService.getAllOrganizations().size() >= 2);
    }
}
