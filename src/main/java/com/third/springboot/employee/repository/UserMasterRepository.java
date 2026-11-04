package com.third.springboot.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.third.springboot.employee.entity.UserMaster;

import java.util.List;

@Repository
public interface UserMasterRepository extends JpaRepository<UserMaster, String> {

    // Optional: find by systemUserId
    UserMaster findBySystemUserId(String systemUserId);

    // Optional: find by userName
    UserMaster findByUserName(String userName);

    // Find by organization
    List<UserMaster> findByOrganizationId(String organizationId);
}
