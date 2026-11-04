package com.third.springboot.employee.hierarchy.service;

import com.third.springboot.employee.entity.UserMaster;
import com.third.springboot.employee.hierarchy.dto.EffectivePermissionsDto;
import com.third.springboot.employee.hierarchy.dto.MenuItemDto;
import com.third.springboot.employee.hierarchy.entity.*;
import com.third.springboot.employee.hierarchy.repository.*;
import com.third.springboot.employee.repository.UserMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EffectivePermissionService {

    @Autowired
    private UserMasterRepository userMasterRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

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
    private PageRegistryRepository pageRegistryRepository;

    @Autowired
    private OrgEntityRepository orgEntityRepository;

    /**
     * Evaluates effective permissions for a user within an organization context.
     * Enforces the hierarchical authorization boundary:
     * Effective Actions = (Union of Group Actions) INTERSECT (Org Approved Actions)
     */
    public EffectivePermissionsDto calculateEffectivePermissions(String userId, String organizationId) {
        UserMaster user = userMasterRepository.findById(userId).orElse(null);
        if (user == null) {
            // Check by systemUserId or userName
            user = userMasterRepository.findBySystemUserId(userId);
            if (user == null) {
                user = userMasterRepository.findByUserName(userId);
            }
        }

        if (user == null || !"ACTIVE".equalsIgnoreCase(user.getUserStatus())) {
            return new EffectivePermissionsDto(userId, "Unknown", "NONE", organizationId, "Unknown",
                    Collections.emptyList(), Collections.emptyMap(), Collections.emptyList());
        }

        String userRole = user.getUserRole() != null ? user.getUserRole() : "USER";
        String effectiveOrgId = organizationId != null && !organizationId.isBlank() 
                ? organizationId 
                : (user.getOrganizationId() != null ? user.getOrganizationId() : "ORG001");

        Organization org = organizationRepository.findById(effectiveOrgId).orElse(null);
        String orgName = org != null ? org.getName() : "Default Organization";

        // 1. Fetch Organization Approved Pages and their max allowed actions
        List<OrgPagePermission> orgPermissions = orgPagePermissionRepository
                .findByOrganizationIdAndStatus(effectiveOrgId, "ACTIVE");

        Map<String, Set<String>> orgApprovedActions = new HashMap<>();
        for (OrgPagePermission opp : orgPermissions) {
            Set<String> actions = parseActions(opp.getAllowedActions());
            orgApprovedActions.put(opp.getPageId(), actions);
        }

        // 2. Fetch User Entity Assignments
        List<UserEntityAssignment> entityAssignments = userEntityAssignmentRepository
                .findByUserIdAndOrganizationId(user.getCustomerId(), effectiveOrgId);
        List<String> assignedEntityIds = entityAssignments.stream()
                .map(UserEntityAssignment::getEntityId)
                .collect(Collectors.toList());

        // Also add legacy userEntity if present
        if (user.getUserEntity() != null && !user.getUserEntity().isBlank() && !assignedEntityIds.contains(user.getUserEntity())) {
            assignedEntityIds.add(user.getUserEntity());
        }

        // 3. Calculate Effective Page Actions
        Map<String, Set<String>> authorizedPages = new HashMap<>();

        // If Developer, grant Developer administrative capabilities
        if ("DEVELOPER".equalsIgnoreCase(userRole)) {
            List<PageRegistry> allPages = pageRegistryRepository.findByActiveTrue();
            for (PageRegistry p : allPages) {
                authorizedPages.put(p.getPageId(), new HashSet<>(Arrays.asList("VIEW", "CREATE", "UPDATE", "DELETE", "EXPORT")));
            }
        } else if ("SUPER_ADMIN".equalsIgnoreCase(userRole)) {
            // Super Admin has administrative capabilities over organization management
            authorizedPages.put("ORGANIZATION_MANAGEMENT", new HashSet<>(Arrays.asList("VIEW", "CREATE", "UPDATE", "DELETE")));
            authorizedPages.put("PAGE_APPROVALS", new HashSet<>(Arrays.asList("VIEW", "UPDATE")));
            authorizedPages.put("AUDIT_LOGS", new HashSet<>(Collections.singletonList("VIEW")));
            authorizedPages.put("DASHBOARD", new HashSet<>(Collections.singletonList("VIEW")));
        } else {
            // Standard organizational user or Org Admin
            // Fetch User Groups
            List<UserGroupAssignment> groupAssignments = userGroupAssignmentRepository
                    .findByUserIdAndOrganizationId(user.getCustomerId(), effectiveOrgId);

            // Org Admin default administrative rights (if in Org Admin role)
            if ("ORG_ADMIN".equalsIgnoreCase(userRole)) {
                // Org Admin receives all approved actions for their organization
                for (Map.Entry<String, Set<String>> entry : orgApprovedActions.entrySet()) {
                    authorizedPages.put(entry.getKey(), new HashSet<>(entry.getValue()));
                }
            } else {
                // Regular users must have permissions granted via Permission Groups
                for (UserGroupAssignment uga : groupAssignments) {
                    List<GroupPermission> groupPerms = groupPermissionRepository.findByGroupId(uga.getGroupId());
                    for (GroupPermission gp : groupPerms) {
                        String pageId = gp.getPageId();

                        // Mandatory Rule: Page MUST be approved for the Organization by Super Admin!
                        if (!orgApprovedActions.containsKey(pageId)) {
                            continue; // Unapproved page - cannot be accessed
                        }

                        Set<String> orgActions = orgApprovedActions.get(pageId);
                        Set<String> groupActions = parseActions(gp.getPermittedActions());

                        // Intersect group permissions with organization approved boundary
                        Set<String> validActions = new HashSet<>(groupActions);
                        validActions.retainAll(orgActions);

                        if (!validActions.isEmpty()) {
                            authorizedPages.computeIfAbsent(pageId, k -> new HashSet<>()).addAll(validActions);
                        }
                    }
                }
            }
        }

        // 4. Build Dynamic Navigation Menus
        List<MenuItemDto> navigationMenus = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : authorizedPages.entrySet()) {
            String pageId = entry.getKey();
            Set<String> actions = entry.getValue();

            // Menu item is visible if user has at least "VIEW" action
            if (actions.contains("VIEW")) {
                PageRegistry page = pageRegistryRepository.findByPageId(pageId).orElse(null);
                if (page != null && page.isActive()) {
                    navigationMenus.add(new MenuItemDto(page.getPageId(), page.getPageName(),
                            page.getRoute(), page.getCategory(), actions));
                }
            }
        }

        navigationMenus.sort(Comparator.comparing(MenuItemDto::getPageName));

        return new EffectivePermissionsDto(
                user.getCustomerId(),
                user.getName() != null ? user.getName() : user.getUserName(),
                userRole,
                effectiveOrgId,
                orgName,
                assignedEntityIds,
                authorizedPages,
                navigationMenus
        );
    }

    /**
     * Verifies if a user has permission to perform an action on a page.
     */
    public boolean hasPermission(String userId, String organizationId, String pageId, String actionCode) {
        if (userId == null || pageId == null || actionCode == null) {
            return false;
        }

        EffectivePermissionsDto permissions = calculateEffectivePermissions(userId, organizationId);
        if ("DEVELOPER".equalsIgnoreCase(permissions.getRole())) {
            return true;
        }

        Set<String> actions = permissions.getAuthorizedPages().get(pageId.toUpperCase());
        return actions != null && actions.contains(actionCode.toUpperCase());
    }

    /**
     * Verifies if a user has access to a specific organizational entity.
     */
    public boolean isAuthorizedForEntity(String userId, String organizationId, String entityId) {
        if (userId == null || organizationId == null || entityId == null) {
            return false;
        }

        EffectivePermissionsDto permissions = calculateEffectivePermissions(userId, organizationId);
        if ("DEVELOPER".equalsIgnoreCase(permissions.getRole()) || "SUPER_ADMIN".equalsIgnoreCase(permissions.getRole())
                || "ORG_ADMIN".equalsIgnoreCase(permissions.getRole())) {
            // Organization Admin can access any entity within their own organization
            // Verify entity belongs to this organization
            Optional<OrgEntity> entityOpt = orgEntityRepository.findById(entityId);
            return entityOpt.isPresent() && entityOpt.get().getOrganizationId().equals(organizationId);
        }

        // Regular user must be explicitly assigned to this entity
        return permissions.getAssignedEntityIds().contains(entityId);
    }

    private Set<String> parseActions(String actionStr) {
        if (actionStr == null || actionStr.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(actionStr.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
