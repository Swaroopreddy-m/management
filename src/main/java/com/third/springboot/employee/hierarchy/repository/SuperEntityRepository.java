package com.third.springboot.employee.hierarchy.repository;

import com.third.springboot.employee.hierarchy.entity.SuperEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SuperEntityRepository extends JpaRepository<SuperEntity, String> {
    Optional<SuperEntity> findByCode(String code);
}
