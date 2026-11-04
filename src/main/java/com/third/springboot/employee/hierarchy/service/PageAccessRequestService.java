package com.third.springboot.employee.hierarchy.service;

import com.third.springboot.employee.hierarchy.dto.PageAccessRequestDto;
import com.third.springboot.employee.hierarchy.dto.PageApprovalDto;
import com.third.springboot.employee.hierarchy.entity.OrgPagePermission;
import com.third.springboot.employee.hierarchy.entity.Organization;
import com.third.springboot.employee.hierarchy.entity.PageAccessRequest;
import com.third.springboot.employee.hierarchy.entity.PageRegistry;
import com.third.springboot.employee.hierarchy.repository.OrgPagePermissionRepository;
import com.third.springboot.employee.hierarchy.repository.OrganizationRepository;
import com.third.springboot.employee.hierarchy.repository.PageAccessRequestRepository;
import com.third.springboot.employee.hierarchy.repository.PageRegistryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PageAccessRequestService {

    @Autowired
    private PageAccessRequestRepository requestRepository;

    @Autowired
    private OrgPagePermissionRepository orgPermissionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PageRegistryRepository pageRegistryRepository;

    @Autowired
    private AuditLogService auditLogService;

    public List<PageAccessRequest> getRequestsByOrganization(String organizationId) {
        return requestRepository.findByOrganizationId(organizationId);
    }

    public List<PageAccessRequest> getAllRequests() {
        return requestRepository.findAll();
    }

    public List<PageAccessRequest> getPendingRequests() {
        return requestRepository.findByStatus("PENDING");
    }

    @Transactional
    public PageAccessRequest submitRequest(String organizationId, PageAccessRequestDto dto, String requestingUserId, String ipAddress) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + organizationId));

        String pageId = dto.getRequestedPageId().toUpperCase().trim();
        String actions = (dto.getRequestedActions() != null && !dto.getRequestedActions().isBlank())
                ? dto.getRequestedActions() : "VIEW";

        String reqId = "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        PageAccessRequest request = new PageAccessRequest(
                reqId,
                organizationId,
                pageId,
                actions,
                dto.getJustification(),
                requestingUserId
        );

        PageAccessRequest saved = requestRepository.save(request);

        auditLogService.log(requestingUserId, "Org User", "ORG_ADMIN", organizationId, null,
                "PAGE_ACCESS_REQUESTED", pageId, "SUCCESS",
                "Requested access to page " + pageId + " with actions: " + actions, ipAddress);

        return saved;
    }

    @Transactional
    public PageAccessRequest reviewRequest(String requestId, PageApprovalDto dto, String reviewerId, String ipAddress) {
        PageAccessRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));

        String decision = dto.getDecision().toUpperCase();
        request.setDecisionTimestamp(LocalDateTime.now());
        request.setReviewedBy(reviewerId);
        request.setDecisionReason(dto.getReason());

        if ("APPROVE".equalsIgnoreCase(decision)) {
            request.setStatus("APPROVED");

            // Grant Organization Page Permission
            String allowedActions = (dto.getAllowedActions() != null && !dto.getAllowedActions().isBlank())
                    ? dto.getAllowedActions() : request.getRequestedActions();

            Optional<OrgPagePermission> existingPerm = orgPermissionRepository
                    .findByOrganizationIdAndPageId(request.getOrganizationId(), request.getRequestedPageId());

            OrgPagePermission permission;
            if (existingPerm.isPresent()) {
                permission = existingPerm.get();
                permission.setAllowedActions(allowedActions);
                permission.setStatus("ACTIVE");
                permission.setGrantedBy(reviewerId);
                permission.setGrantedAt(LocalDateTime.now());
                permission.setRevocationReason(null);
            } else {
                permission = new OrgPagePermission(
                        request.getOrganizationId(),
                        request.getRequestedPageId(),
                        allowedActions,
                        reviewerId
                );
            }
            orgPermissionRepository.save(permission);

            auditLogService.log(reviewerId, "Super Admin", "SUPER_ADMIN", request.getOrganizationId(), null,
                    "PAGE_ACCESS_APPROVED", request.getRequestedPageId(), "SUCCESS",
                    "Approved page " + request.getRequestedPageId() + " with actions [" + allowedActions + "]", ipAddress);

        } else if ("REJECT".equalsIgnoreCase(decision)) {
            request.setStatus("REJECTED");

            auditLogService.log(reviewerId, "Super Admin", "SUPER_ADMIN", request.getOrganizationId(), null,
                    "PAGE_ACCESS_REJECTED", request.getRequestedPageId(), "SUCCESS",
                    "Rejected page access request. Reason: " + dto.getReason(), ipAddress);
        } else {
            throw new IllegalArgumentException("Invalid review decision: " + decision);
        }

        request.setUpdatedAt(LocalDateTime.now());
        return requestRepository.save(request);
    }

    @Transactional
    public OrgPagePermission revokeOrganizationPage(String organizationId, String pageId, String reason, String actorId, String ipAddress) {
        OrgPagePermission permission = orgPermissionRepository
                .findByOrganizationIdAndPageId(organizationId, pageId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found for org: " + organizationId + ", page: " + pageId));

        permission.setStatus("REVOKED");
        permission.setRevokedAt(LocalDateTime.now());
        permission.setRevocationReason(reason);
        OrgPagePermission updated = orgPermissionRepository.save(permission);

        auditLogService.log(actorId, "Super Admin", "SUPER_ADMIN", organizationId, null,
                "PERMISSION_REVOKED", pageId, "SUCCESS",
                "Revoked access to page " + pageId + " from org " + organizationId + ". Reason: " + reason, ipAddress);

        return updated;
    }
}
