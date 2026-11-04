package com.third.springboot.employee.hierarchy.service;

import com.third.springboot.employee.entity.UserMaster;
import com.third.springboot.employee.hierarchy.dto.OrgEntityDto;
import com.third.springboot.employee.hierarchy.dto.OrganizationDto;
import com.third.springboot.employee.hierarchy.dto.SuperAdminDto;
import com.third.springboot.employee.hierarchy.entity.*;
import com.third.springboot.employee.hierarchy.repository.*;
import com.third.springboot.employee.repository.UserMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class HierarchyService {

    @Autowired
    private SuperEntityRepository superEntityRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrgEntityRepository orgEntityRepository;

    @Autowired
    private UserEntityAssignmentRepository userEntityAssignmentRepository;

    @Autowired
    private UserMasterRepository userMasterRepository;

    @Autowired
    private AuditLogService auditLogService;

    // --- Super Entities ---
    public List<SuperEntity> getAllSuperEntities() {
        return superEntityRepository.findAll();
    }

    public Optional<SuperEntity> getSuperEntity(String id) {
        return superEntityRepository.findById(id);
    }

    @Transactional
    public SuperEntity createSuperEntity(String name, String code, String description, String actorId, String ipAddress) {
        String id = "SE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        SuperEntity entity = new SuperEntity(id, name, code, description);
        SuperEntity saved = superEntityRepository.save(entity);

        auditLogService.log(actorId, "Developer", "DEVELOPER", null, null,
                "SUPER_ENTITY_CREATED", id, "SUCCESS", "Created super entity: " + name, ipAddress);
        return saved;
    }

    // --- Super Admins ---
    public List<SuperAdmin> getAllSuperAdmins() {
        return superAdminRepository.findAll();
    }

    public List<SuperAdmin> getSuperAdminsBySuperEntity(String superEntityId) {
        return superAdminRepository.findBySuperEntityId(superEntityId);
    }

    @Transactional
    public SuperAdmin createSuperAdmin(SuperAdminDto dto, String actorId, String ipAddress) {
        String id = (dto.getId() != null && !dto.getId().isBlank()) 
                ? dto.getId() 
                : "SA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        SuperAdmin admin = new SuperAdmin(
                id,
                dto.getSuperEntityId(),
                dto.getName(),
                dto.getEmail().toLowerCase(),
                dto.getUsername().toLowerCase(),
                dto.getPassword() != null ? dto.getPassword() : "admin123"
        );
        SuperAdmin saved = superAdminRepository.save(admin);

        auditLogService.log(actorId, "Super Entity Admin", "SUPER_ENTITY", null, null,
                "SUPER_ADMIN_CREATED", id, "SUCCESS", "Created super admin: " + dto.getName() + " (" + dto.getUsername() + ")", ipAddress);
        return saved;
    }

    // --- Organizations ---
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    public List<Organization> getOrganizationsBySuperAdmin(String superAdminId) {
        return organizationRepository.findBySuperAdminId(superAdminId);
    }

    public Optional<Organization> getOrganization(String id) {
        return organizationRepository.findById(id);
    }

    @Transactional
    public Organization createOrganization(OrganizationDto dto, String actorId, String ipAddress) {
        String id = (dto.getId() != null && !dto.getId().isBlank()) 
                ? dto.getId() 
                : "ORG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Organization org = new Organization(
                id,
                dto.getSuperEntityId(),
                dto.getSuperAdminId(),
                dto.getName(),
                dto.getCode()
        );
        if (dto.getSubscriptionTier() != null) {
            org.setSubscriptionTier(dto.getSubscriptionTier());
        }

        Organization saved = organizationRepository.save(org);

        auditLogService.log(actorId, "Super Admin", "SUPER_ADMIN", id, null,
                "ORGANIZATION_CREATED", id, "SUCCESS", "Created organization: " + dto.getName() + " (" + dto.getCode() + ")", ipAddress);
        return saved;
    }

    @Transactional
    public Organization updateOrganizationStatus(String organizationId, String status, String actorId, String ipAddress) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + organizationId));

        org.setStatus(status);
        org.setUpdatedAt(LocalDateTime.now());
        Organization updated = organizationRepository.save(org);

        auditLogService.log(actorId, "Super Admin", "SUPER_ADMIN", organizationId, null,
                "ORGANIZATION_STATUS_UPDATED", organizationId, "SUCCESS", "Updated status to " + status, ipAddress);
        return updated;
    }

    // --- Organizational Entities (Branches, Departments, Stores) ---
    public List<OrgEntity> getEntitiesByOrganization(String organizationId) {
        return orgEntityRepository.findByOrganizationId(organizationId);
    }

    @Transactional
    public OrgEntity createOrgEntity(String organizationId, OrgEntityDto dto, String actorId, String ipAddress) {
        String id = (dto.getId() != null && !dto.getId().isBlank()) 
                ? dto.getId() 
                : "ENT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        OrgEntity entity = new OrgEntity(
                id,
                organizationId,
                dto.getEntityType().toUpperCase(),
                dto.getName(),
                dto.getCode()
        );
        OrgEntity saved = orgEntityRepository.save(entity);

        auditLogService.log(actorId, "Org Admin", "ORG_ADMIN", organizationId, id,
                "ORG_ENTITY_CREATED", id, "SUCCESS",
                "Created " + dto.getEntityType() + ": " + dto.getName() + " in org " + organizationId, ipAddress);
        return saved;
    }

    // --- User Entity Assignments ---
    @Transactional
    public UserEntityAssignment assignUserToEntity(String organizationId, String userId, String entityId, String actorId, String ipAddress) {
        OrgEntity entity = orgEntityRepository.findById(entityId)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + entityId));

        if (!entity.getOrganizationId().equals(organizationId)) {
            throw new SecurityException("Entity " + entityId + " does not belong to organization " + organizationId);
        }

        Optional<UserEntityAssignment> existing = userEntityAssignmentRepository.findByUserIdAndEntityId(userId, entityId);
        if (existing.isPresent()) {
            return existing.get();
        }

        UserEntityAssignment uea = new UserEntityAssignment(userId, organizationId, entityId);
        UserEntityAssignment saved = userEntityAssignmentRepository.save(uea);

        auditLogService.log(actorId, "Org Admin", "ORG_ADMIN", organizationId, entityId,
                "USER_ENTITY_ASSIGNED", entityId, "SUCCESS",
                "Assigned user " + userId + " to entity " + entity.getName(), ipAddress);
        return saved;
    }

    @Transactional
    public void removeUserFromEntity(String organizationId, String userId, String entityId, String actorId, String ipAddress) {
        userEntityAssignmentRepository.deleteByUserIdAndEntityId(userId, entityId);

        auditLogService.log(actorId, "Org Admin", "ORG_ADMIN", organizationId, entityId,
                "USER_ENTITY_REMOVED", entityId, "SUCCESS",
                "Removed user " + userId + " from entity " + entityId, ipAddress);
    }

    // --- Role Management & Privilege Escalation Protection ---
    @Transactional
    public UserMaster updateUserRole(String organizationId, String userId, String newRole, String actorRole, String actorId, String ipAddress) {
        // MANDATORY SECURITY RULE: An Organization Admin must NOT be able to create a Super Admin or Developer!
        if (("SUPER_ADMIN".equalsIgnoreCase(newRole) || "DEVELOPER".equalsIgnoreCase(newRole) || "SUPER_ENTITY".equalsIgnoreCase(newRole))
                && !"DEVELOPER".equalsIgnoreCase(actorRole) && !"SUPER_ADMIN".equalsIgnoreCase(actorRole)) {
            auditLogService.log(actorId, "Unknown", actorRole, organizationId, null,
                    "SECURITY_ALERT_PRIVILEGE_ESCALATION", userId, "DENIED",
                    "Rejected attempt to escalate role to " + newRole + " by " + actorRole, ipAddress);
            throw new SecurityException("Privilege escalation denied: You cannot grant administrative role: " + newRole);
        }

        UserMaster user = userMasterRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Enforce organization boundary
        if (user.getOrganizationId() != null && !user.getOrganizationId().equals(organizationId)
                && !"DEVELOPER".equalsIgnoreCase(actorRole) && !"SUPER_ADMIN".equalsIgnoreCase(actorRole)) {
            throw new SecurityException("Cross-organization user modification rejected!");
        }

        user.setUserRole(newRole);
        UserMaster updated = userMasterRepository.save(user);

        auditLogService.log(actorId, "Administrator", actorRole, organizationId, null,
                "USER_ROLE_UPDATED", userId, "SUCCESS",
                "Updated role of " + userId + " to " + newRole, ipAddress);
        return updated;
    }
}
