package com.third.springboot.employee.hierarchy.repository;

import com.third.springboot.employee.hierarchy.entity.UserGroupAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGroupAssignmentRepository extends JpaRepository<UserGroupAssignment, Long> {
    List<UserGroupAssignment> findByUserId(String userId);
    List<UserGroupAssignment> findByUserIdAndOrganizationId(String userId, String organizationId);
    List<UserGroupAssignment> findByGroupId(String groupId);
    Optional<UserGroupAssignment> findByUserIdAndGroupId(String userId, String groupId);

    @Transactional
    void deleteByUserIdAndGroupId(String userId, String groupId);

    @Transactional
    void deleteByUserIdAndOrganizationId(String userId, String organizationId);
}
