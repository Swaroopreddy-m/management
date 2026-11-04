package com.third.springboot.employee.hierarchy.config;

import com.third.springboot.employee.entity.Employee;
import com.third.springboot.employee.entity.PinMaster;
import com.third.springboot.employee.entity.UserMaster;
import com.third.springboot.employee.hierarchy.entity.*;
import com.third.springboot.employee.hierarchy.repository.*;
import com.third.springboot.employee.repository.EmployeeRepository;
import com.third.springboot.employee.repository.PinMasterRepository;
import com.third.springboot.employee.repository.UserMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private SuperEntityRepository superEntityRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrgEntityRepository orgEntityRepository;

    @Autowired
    private PageRegistryRepository pageRegistryRepository;

    @Autowired
    private PageActionRepository pageActionRepository;

    @Autowired
    private OrgPagePermissionRepository orgPagePermissionRepository;

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private GroupPermissionRepository groupPermissionRepository;

    @Autowired
    private UserGroupAssignmentRepository userGroupAssignmentRepository;

    @Autowired
    private UserEntityAssignmentRepository userEntityAssignmentRepository;

    @Autowired
    private UserMasterRepository userMasterRepository;

    @Autowired
    private PinMasterRepository pinMasterRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public void run(ApplicationArguments args) {
        initializeHierarchyData();
    }

    public void initializeHierarchyData() {
        // 1. Super Entity
        if (superEntityRepository.count() == 0) {
            superEntityRepository.save(new SuperEntity(
                    "SE001",
                    "Global Enterprise Group",
                    "SE_GLOBAL",
                    "Top-level enterprise holding group"
            ));
        }

        // 2. Super Admin
        if (superAdminRepository.count() == 0) {
            superAdminRepository.save(new SuperAdmin(
                    "SA001",
                    "SE001",
                    "Enterprise Super Admin",
                    "superadmin@global.com",
                    "superadmin",
                    "admin123"
            ));
        }

        // 3. Organizations
        if (organizationRepository.count() == 0) {
            organizationRepository.save(new Organization("ORG001", "SE001", "SA001", "Apex Solutions", "ORG_APEX"));
            organizationRepository.save(new Organization("ORG002", "SE001", "SA001", "Beta Industries", "ORG_BETA"));
        }

        // 4. Organizational Entities (Branches, Departments, Stores)
        if (orgEntityRepository.count() == 0) {
            orgEntityRepository.save(new OrgEntity("BR001", "ORG001", "BRANCH", "Apex Headquarters", "BR_HQ"));
            orgEntityRepository.save(new OrgEntity("BR002", "ORG001", "BRANCH", "Apex West Coast", "BR_WEST"));
            orgEntityRepository.save(new OrgEntity("DEPT001", "ORG001", "DEPARTMENT", "Engineering Dept", "DEP_ENG"));
            orgEntityRepository.save(new OrgEntity("STR001", "ORG001", "STORE", "Main Fulfillment Store", "STR_MAIN"));
            orgEntityRepository.save(new OrgEntity("BR003", "ORG002", "BRANCH", "Beta Metro Hub", "BR_METRO"));
        }

        // 5. Dynamic Page Registry
        if (pageRegistryRepository.count() == 0) {
            registerPage("DASHBOARD", "Main Dashboard", "Executive metrics overview", "/dashboard", "Core", List.of("VIEW"));
            registerPage("EMPLOYEE_MANAGEMENT", "Employee Directory", "Manage organization employees", "/dashboard/employees", "Operations", List.of("VIEW", "CREATE", "UPDATE", "DELETE", "EXPORT"));
            registerPage("USER_MANAGEMENT", "User Administration", "User accounts & credentials", "/dashboard/users", "Administration", List.of("VIEW", "CREATE", "UPDATE", "DELETE"));
            registerPage("BRANCH_MANAGEMENT", "Branch Management", "Manage operational facilities", "/dashboard/branches", "Organization", List.of("VIEW", "CREATE", "UPDATE", "DELETE"));
            registerPage("DEPARTMENT_MANAGEMENT", "Department Management", "Manage functional divisions", "/dashboard/departments", "Organization", List.of("VIEW", "CREATE"));
            registerPage("STORE_MANAGEMENT", "Store Management", "Manage retail and outlets", "/dashboard/stores", "Organization", List.of("VIEW", "CREATE"));
            registerPage("REPORTS", "Analytics & Reports", "Exportable business reports", "/dashboard/reports", "Analytics", List.of("VIEW", "EXPORT"));
            registerPage("SETTINGS", "Organization Settings", "System preferences", "/dashboard/settings", "Settings", List.of("VIEW", "UPDATE"));
            registerPage("DYNAMIC_PAGES", "Dynamic Form Builder", "Capture, confirm & acknowledge builder", "/page/capture", "Core", List.of("VIEW"));
        }

        // 6. Organization Approved Permissions (Super Admin boundary for ORG001)
        if (orgPagePermissionRepository.count() == 0) {
            approvePage("ORG001", "DASHBOARD", "VIEW", "SA001");
            approvePage("ORG001", "EMPLOYEE_MANAGEMENT", "VIEW,CREATE,UPDATE,DELETE,EXPORT", "SA001");
            approvePage("ORG001", "USER_MANAGEMENT", "VIEW,CREATE,UPDATE,DELETE", "SA001");
            approvePage("ORG001", "BRANCH_MANAGEMENT", "VIEW,CREATE,UPDATE,DELETE", "SA001");
            approvePage("ORG001", "DEPARTMENT_MANAGEMENT", "VIEW,CREATE", "SA001");
            approvePage("ORG001", "STORE_MANAGEMENT", "VIEW,CREATE", "SA001");
            approvePage("ORG001", "REPORTS", "VIEW,EXPORT", "SA001");
            approvePage("ORG001", "DYNAMIC_PAGES", "VIEW", "SA001");
        }

        // 7. Permission Groups for ORG001
        if (permissionGroupRepository.count() == 0) {
            permissionGroupRepository.save(new PermissionGroup("GRP001", "ORG001", "Org Administrator Group", "Full org access"));
            permissionGroupRepository.save(new PermissionGroup("GRP002", "ORG001", "General Staff", "Standard staff access"));
            permissionGroupRepository.save(new PermissionGroup("GRP003", "ORG001", "Auditors", "Read and export access only"));

            // Group Permissions
            assignGroupActions("GRP001", "ORG001", "DASHBOARD", "VIEW");
            assignGroupActions("GRP001", "ORG001", "EMPLOYEE_MANAGEMENT", "VIEW,CREATE,UPDATE,DELETE,EXPORT");
            assignGroupActions("GRP001", "ORG001", "USER_MANAGEMENT", "VIEW,CREATE,UPDATE,DELETE");
            assignGroupActions("GRP001", "ORG001", "BRANCH_MANAGEMENT", "VIEW,CREATE,UPDATE,DELETE");
            assignGroupActions("GRP001", "ORG001", "DEPARTMENT_MANAGEMENT", "VIEW,CREATE");
            assignGroupActions("GRP001", "ORG001", "STORE_MANAGEMENT", "VIEW,CREATE");
            assignGroupActions("GRP001", "ORG001", "REPORTS", "VIEW,EXPORT");
            assignGroupActions("GRP001", "ORG001", "DYNAMIC_PAGES", "VIEW");

            assignGroupActions("GRP002", "ORG001", "DASHBOARD", "VIEW");
            assignGroupActions("GRP002", "ORG001", "EMPLOYEE_MANAGEMENT", "VIEW");

            assignGroupActions("GRP003", "ORG001", "DASHBOARD", "VIEW");
            assignGroupActions("GRP003", "ORG001", "REPORTS", "VIEW,EXPORT");
        }

        // 8. User Master & Pin Master
        if (userMasterRepository.count() == 0) {
            UserMaster user = new UserMaster();
            user.setCustomerId("US001");
            user.setName("John Doe");
            user.setEmail("john.doe@example.com");
            user.setMobile("9876543210");
            user.setSystemUserId("SYS001");
            user.setUserName("john_doe");
            user.setUserGroups("ADMIN");
            user.setUserEntity("BR001");
            user.setUserStatus("ACTIVE");
            user.setOrganizationId("ORG001");
            user.setUserRole("ORG_ADMIN");
            user.setUserLastLoginTime("2026-07-07 10:00:00");
            user.setCreatedBy("system");
            user.setCreatedTime("2026-07-07 09:00:00");
            userMasterRepository.save(user);

            PinMaster pin = new PinMaster();
            pin.setUserId("US001");
            pin.setPassword("password123");
            pin.setSystemId("SYS001");
            pinMasterRepository.save(pin);
        }

        // 9. User Group & Entity Assignments for US001
        if (userGroupAssignmentRepository.count() == 0) {
            userGroupAssignmentRepository.save(new UserGroupAssignment("US001", "ORG001", "GRP001"));
        }
        if (userEntityAssignmentRepository.count() == 0) {
            userEntityAssignmentRepository.save(new UserEntityAssignment("US001", "ORG001", "BR001"));
            userEntityAssignmentRepository.save(new UserEntityAssignment("US001", "ORG001", "DEPT001"));
        }

        // 10. Sample Employees
        if (employeeRepository.count() == 0) {
            employeeRepository.save(new Employee("Alice Johnson", "alice@example.com", "Engineering", 75000.00, "ORG001", "BR001"));
            employeeRepository.save(new Employee("Bob Smith", "bob@example.com", "Product", 82000.00, "ORG001", "BR001"));
            employeeRepository.save(new Employee("Carol White", "carol@example.com", "HR", 60000.00, "ORG001", "BR002"));
            employeeRepository.save(new Employee("David Wilson", "david@beta.com", "Logistics", 55000.00, "ORG002", "BR003"));
        }
    }

    private void registerPage(String pageId, String name, String desc, String route, String category, List<String> actions) {
        PageRegistry page = new PageRegistry(pageId, name, desc, route, category);
        pageRegistryRepository.save(page);
        for (String act : actions) {
            pageActionRepository.save(new PageAction(pageId, act, act, act + " on " + name));
        }
    }

    private void approvePage(String orgId, String pageId, String actions, String grantedBy) {
        orgPagePermissionRepository.save(new OrgPagePermission(orgId, pageId, actions, grantedBy));
    }

    private void assignGroupActions(String groupId, String orgId, String pageId, String actions) {
        groupPermissionRepository.save(new GroupPermission(groupId, orgId, pageId, actions));
    }
}
