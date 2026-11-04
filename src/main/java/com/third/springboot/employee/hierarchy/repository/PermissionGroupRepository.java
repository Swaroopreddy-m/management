package com.third.springboot.employee.hierarchy.repository;

import com.third.springboot.employee.hierarchy.entity.PermissionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionGroupRepository extends JpaRepository<PermissionGroup, String> {
    List<PermissionGroup> findByOrganizationId(String organizationId);
    List<PermissionGroup> findByOrganizationIdAndStatus(String organizationId, String status);
    Optional<PermissionGroup> findByOrganizationIdAndGroupName(String organizationId, String groupName);
}
