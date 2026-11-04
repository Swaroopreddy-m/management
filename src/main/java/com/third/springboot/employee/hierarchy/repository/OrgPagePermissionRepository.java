package com.third.springboot.employee.hierarchy.repository;

import com.third.springboot.employee.hierarchy.entity.OrgPagePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrgPagePermissionRepository extends JpaRepository<OrgPagePermission, Long> {
    List<OrgPagePermission> findByOrganizationId(String organizationId);
    List<OrgPagePermission> findByOrganizationIdAndStatus(String organizationId, String status);
    Optional<OrgPagePermission> findByOrganizationIdAndPageId(String organizationId, String pageId);
    Optional<OrgPagePermission> findByOrganizationIdAndPageIdAndStatus(String organizationId, String pageId, String status);
}
