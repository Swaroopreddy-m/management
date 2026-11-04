package com.third.springboot.employee.hierarchy.repository;

import com.third.springboot.employee.hierarchy.entity.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuperAdminRepository extends JpaRepository<SuperAdmin, String> {
    Optional<SuperAdmin> findByUsername(String username);
    Optional<SuperAdmin> findByEmail(String email);
    List<SuperAdmin> findBySuperEntityId(String superEntityId);
}
