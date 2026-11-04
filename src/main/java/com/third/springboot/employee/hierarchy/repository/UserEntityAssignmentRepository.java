package com.third.springboot.employee.hierarchy.repository;

import com.third.springboot.employee.hierarchy.entity.UserEntityAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserEntityAssignmentRepository extends JpaRepository<UserEntityAssignment, Long> {
    List<UserEntityAssignment> findByUserId(String userId);
    List<UserEntityAssignment> findByUserIdAndOrganizationId(String userId, String organizationId);
    List<UserEntityAssignment> findByEntityId(String entityId);
    Optional<UserEntityAssignment> findByUserIdAndEntityId(String userId, String entityId);

    @Transactional
    void deleteByUserIdAndEntityId(String userId, String entityId);
}
