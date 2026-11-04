package com.third.springboot.employee.hierarchy.repository;

import com.third.springboot.employee.hierarchy.entity.PageAccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageAccessRequestRepository extends JpaRepository<PageAccessRequest, String> {
    List<PageAccessRequest> findByOrganizationId(String organizationId);
    List<PageAccessRequest> findByOrganizationIdAndStatus(String organizationId, String status);
    List<PageAccessRequest> findByStatus(String status);
    Optional<PageAccessRequest> findByOrganizationIdAndRequestedPageIdAndStatus(String organizationId, String requestedPageId, String status);
}
