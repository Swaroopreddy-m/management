package com.third.springboot.employee.hierarchy.repository;

import com.third.springboot.employee.hierarchy.entity.PageRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRegistryRepository extends JpaRepository<PageRegistry, String> {
    Optional<PageRegistry> findByPageId(String pageId);
    Optional<PageRegistry> findByRoute(String route);
    List<PageRegistry> findByActiveTrue();
    List<PageRegistry> findByCategory(String category);
}
