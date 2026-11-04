package com.third.springboot.employee.hierarchy.repository;

import com.third.springboot.employee.hierarchy.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, String> {
    Optional<Organization> findByCode(String code);
    List<Organization> findBySuperAdminId(String superAdminId);
    List<Organization> findBySuperEntityId(String superEntityId);
}
