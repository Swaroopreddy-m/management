package com.third.springboot.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.third.springboot.employee.entity.Employee;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByOrganizationId(String organizationId);
    List<Employee> findByOrganizationIdAndEntityId(String organizationId, String entityId);
    Optional<Employee> findByIdAndOrganizationId(Long id, String organizationId);
    Optional<Employee> findByIdAndOrganizationIdAndEntityId(Long id, String organizationId, String entityId);
}
