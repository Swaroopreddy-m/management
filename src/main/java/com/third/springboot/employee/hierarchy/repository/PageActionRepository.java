package com.third.springboot.employee.hierarchy.repository;

import com.third.springboot.employee.hierarchy.entity.PageAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageActionRepository extends JpaRepository<PageAction, Long> {
    List<PageAction> findByPageId(String pageId);
    Optional<PageAction> findByPageIdAndActionCode(String pageId, String actionCode);

    @Transactional
    void deleteByPageId(String pageId);
}
