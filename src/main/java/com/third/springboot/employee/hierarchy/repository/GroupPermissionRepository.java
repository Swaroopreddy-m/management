package com.third.springboot.employee.hierarchy.repository;

import com.third.springboot.employee.hierarchy.entity.GroupPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupPermissionRepository extends JpaRepository<GroupPermission, Long> {
    List<GroupPermission> findByGroupId(String groupId);
    List<GroupPermission> findByOrganizationId(String organizationId);
    Optional<GroupPermission> findByGroupIdAndPageId(String groupId, String pageId);

    @Transactional
    void deleteByGroupId(String groupId);

    @Transactional
    void deleteByGroupIdAndPageId(String groupId, String pageId);
}
