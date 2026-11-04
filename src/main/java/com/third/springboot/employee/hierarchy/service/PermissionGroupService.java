package com.third.springboot.employee.hierarchy.service;

import com.third.springboot.employee.hierarchy.dto.GroupActionAssignmentDto;
import com.third.springboot.employee.hierarchy.dto.PermissionGroupDto;
import com.third.springboot.employee.hierarchy.entity.GroupPermission;
import com.third.springboot.employee.hierarchy.entity.OrgPagePermission;
import com.third.springboot.employee.hierarchy.entity.PermissionGroup;
import com.third.springboot.employee.hierarchy.entity.UserGroupAssignment;
import com.third.springboot.employee.hierarchy.repository.GroupPermissionRepository;
import com.third.springboot.employee.hierarchy.repository.OrgPagePermissionRepository;
import com.third.springboot.employee.hierarchy.repository.PermissionGroupRepository;
import com.third.springboot.employee.hierarchy.repository.UserGroupAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionGroupService {

    @Autowired
    private PermissionGroupRepository groupRepository;

    @Autowired
    private GroupPermissionRepository groupPermissionRepository;

    @Autowired
    private OrgPagePermissionRepository orgPagePermissionRepository;

    @Autowired
    private UserGroupAssignmentRepository userGroupAssignmentRepository;

    @Autowired
    private AuditLogService auditLogService;

    public List<PermissionGroup> getGroupsByOrganization(String organizationId) {
        return groupRepository.findByOrganizationId(organizationId);
    }

    public Optional<PermissionGroup> getGroup(String groupId) {
        return groupRepository.findById(groupId);
    }

    public List<GroupPermission> getGroupPermissions(String groupId) {
        return groupPermissionRepository.findByGroupId(groupId);
    }

    @Transactional
    public PermissionGroup createGroup(String organizationId, PermissionGroupDto dto, String actorId, String ipAddress) {
        String groupId = (dto.getId() != null && !dto.getId().isBlank()) 
                ? dto.getId() 
                : "GRP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        PermissionGroup group = new PermissionGroup(
                groupId,
                organizationId,
                dto.getGroupName(),
                dto.getDescription()
        );

        PermissionGroup saved = groupRepository.save(group);

        auditLogService.log(actorId, "Org Admin", "ORG_ADMIN", organizationId, null,
                "PERMISSION_GROUP_CREATED", groupId, "SUCCESS",
                "Created group " + dto.getGroupName() + " in org " + organizationId, ipAddress);

        return saved;
    }

    /**
     * Assigns approved page actions to a group.
     * MANDATORY SECURITY RULE: An Organization Admin must never be able to grant
     * a permission that has not been authorized for that organization by the Super Admin!
     */
    @Transactional
    public GroupPermission assignPageActionsToGroup(String organizationId, String groupId,
                                                   GroupActionAssignmentDto dto, String actorId, String ipAddress) {
        PermissionGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        if (!group.getOrganizationId().equals(organizationId)) {
            throw new SecurityException("Cross-organization modification rejected!");
        }

        String pageId = dto.getPageId().toUpperCase().trim();

        // 1. Authoritative check: Page MUST be approved for this organization by Super Admin
        OrgPagePermission orgPermission = orgPagePermissionRepository
                .findByOrganizationIdAndPageIdAndStatus(organizationId, pageId, "ACTIVE")
                .orElseThrow(() -> new SecurityException(
                        "Permission rejected: Page " + pageId + " has NOT been authorized for organization " + organizationId + " by the Super Admin!"));

        // 2. Authoritative check: Actions must be subset of organization's allowed actions
        Set<String> orgAllowedActions = parseActions(orgPermission.getAllowedActions());
        Set<String> requestedActions = parseActions(dto.getPermittedActions());

        for (String act : requestedActions) {
            if (!orgAllowedActions.contains(act)) {
                throw new SecurityException(
                        "Permission escalation rejected: Action " + act + " is NOT authorized for organization " + organizationId + ". Allowed: " + orgAllowedActions);
            }
        }

        // 3. Save or update group permission
        Optional<GroupPermission> existing = groupPermissionRepository.findByGroupIdAndPageId(groupId, pageId);
        GroupPermission gp;
        if (existing.isPresent()) {
            gp = existing.get();
            gp.setPermittedActions(String.join(",", requestedActions));
            gp.setUpdatedAt(LocalDateTime.now());
        } else {
            gp = new GroupPermission(groupId, organizationId, pageId, String.join(",", requestedActions));
        }

        GroupPermission saved = groupPermissionRepository.save(gp);

        auditLogService.log(actorId, "Org Admin", "ORG_ADMIN", organizationId, null,
                "GROUP_PERMISSION_ASSIGNED", pageId, "SUCCESS",
                "Assigned actions [" + dto.getPermittedActions() + "] on page " + pageId + " to group " + group.getGroupName(), ipAddress);

        return saved;
    }

    @Transactional
    public void removeGroupPermission(String organizationId, String groupId, String pageId, String actorId, String ipAddress) {
        PermissionGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        if (!group.getOrganizationId().equals(organizationId)) {
            throw new SecurityException("Cross-organization access rejected!");
        }

        groupPermissionRepository.deleteByGroupIdAndPageId(groupId, pageId);

        auditLogService.log(actorId, "Org Admin", "ORG_ADMIN", organizationId, null,
                "GROUP_PERMISSION_REMOVED", pageId, "SUCCESS",
                "Removed page " + pageId + " from group " + groupId, ipAddress);
    }

    @Transactional
    public UserGroupAssignment assignUserToGroup(String organizationId, String userId, String groupId, String actorId, String ipAddress) {
        PermissionGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        if (!group.getOrganizationId().equals(organizationId)) {
            throw new SecurityException("Group does not belong to organization: " + organizationId);
        }

        Optional<UserGroupAssignment> existing = userGroupAssignmentRepository.findByUserIdAndGroupId(userId, groupId);
        if (existing.isPresent()) {
            return existing.get();
        }

        UserGroupAssignment uga = new UserGroupAssignment(userId, organizationId, groupId);
        UserGroupAssignment saved = userGroupAssignmentRepository.save(uga);

        auditLogService.log(actorId, "Org Admin", "ORG_ADMIN", organizationId, null,
                "USER_GROUP_ASSIGNED", groupId, "SUCCESS",
                "Assigned user " + userId + " to group " + group.getGroupName(), ipAddress);

        return saved;
    }

    @Transactional
    public void removeUserFromGroup(String organizationId, String userId, String groupId, String actorId, String ipAddress) {
        userGroupAssignmentRepository.deleteByUserIdAndGroupId(userId, groupId);

        auditLogService.log(actorId, "Org Admin", "ORG_ADMIN", organizationId, null,
                "USER_GROUP_REMOVED", groupId, "SUCCESS",
                "Removed user " + userId + " from group " + groupId, ipAddress);
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
