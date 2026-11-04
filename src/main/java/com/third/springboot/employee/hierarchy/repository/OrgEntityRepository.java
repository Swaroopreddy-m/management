package com.third.springboot.employee.hierarchy.repository;

import com.third.springboot.employee.hierarchy.entity.OrgEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrgEntityRepository extends JpaRepository<OrgEntity, String> {
    List<OrgEntity> findByOrganizationId(String organizationId);
    List<OrgEntity> findByOrganizationIdAndEntityType(String organizationId, String entityType);
    Optional<OrgEntity> findByOrganizationIdAndCode(String organizationId, String code);
}
