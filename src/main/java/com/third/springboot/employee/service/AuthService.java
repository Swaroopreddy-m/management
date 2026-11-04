package com.third.springboot.employee.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.third.springboot.employee.dto.Signup;
import com.third.springboot.employee.entity.PinMaster;
import com.third.springboot.employee.entity.UserMaster;
import com.third.springboot.employee.hierarchy.entity.SuperAdmin;
import com.third.springboot.employee.hierarchy.repository.SuperAdminRepository;
import com.third.springboot.employee.hierarchy.service.AuditLogService;
import com.third.springboot.employee.repository.PinMasterRepository;
import com.third.springboot.employee.repository.UserMasterRepository;

@Service
public class AuthService {

    @Autowired
    private UserMasterRepository userMasterRepository;

    @Autowired
    private PinMasterRepository pinMasterRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private AuditLogService auditLogService;

    public boolean authenticate(String userId, String password) {
        if (userId == null || password == null) {
            return false;
        }

        String trimmedUser = userId.trim();
        String trimmedPass = password.trim();

        // 1. Check Technical Developer Account
        if ("developer".equalsIgnoreCase(trimmedUser) && "dev123".equals(trimmedPass)) {
            return true;
        }

        // 2. Check Super Admin Account
        Optional<SuperAdmin> superAdminOpt = superAdminRepository.findByUsername(trimmedUser.toLowerCase());
        if (superAdminOpt.isPresent()) {
            SuperAdmin sa = superAdminOpt.get();
            if ("ACTIVE".equalsIgnoreCase(sa.getStatus()) && sa.getPasswordHash().equals(trimmedPass)) {
                return true;
            }
        }

        // 3. Check user_master + pin_master
        Optional<UserMaster> userOpt = userMasterRepository.findById(trimmedUser);
        if (userOpt.isEmpty()) {
            UserMaster bySys = userMasterRepository.findBySystemUserId(trimmedUser);
            if (bySys != null) {
                userOpt = Optional.of(bySys);
            } else {
                UserMaster byName = userMasterRepository.findByUserName(trimmedUser);
                if (byName != null) {
                    userOpt = Optional.of(byName);
                }
            }
        }

        if (userOpt.isEmpty()) {
            return false;
        }

        UserMaster user = userOpt.get();
        if (user.getUserStatus() != null && "INACTIVE".equalsIgnoreCase(user.getUserStatus())) {
            return false;
        }

        Optional<PinMaster> pinOpt = pinMasterRepository.findByUserId(user.getCustomerId());
        if (pinOpt.isEmpty()) {
            return false;
        }

        String dbPassword = pinOpt.get().getPassword();
        return dbPassword != null && dbPassword.trim().equals(trimmedPass);
    }

    public UserMaster getUserMaster(String userId) {
        if (userId == null) return null;
        String trimmed = userId.trim();

        Optional<UserMaster> userOpt = userMasterRepository.findById(trimmed);
        if (userOpt.isPresent()) return userOpt.get();

        UserMaster bySys = userMasterRepository.findBySystemUserId(trimmed);
        if (bySys != null) return bySys;

        return userMasterRepository.findByUserName(trimmed);
    }

    @Transactional
    public UserMaster registerUser(Signup signupDto, String ipAddress) {
        if (signupDto == null || signupDto.getUserId() == null || signupDto.getPassword() == null) {
            throw new IllegalArgumentException("UserId and Password are required");
        }

        String customerId = signupDto.getUserId().trim();
        if (userMasterRepository.existsById(customerId)) {
            throw new IllegalArgumentException("User with ID " + customerId + " already exists");
        }

        UserMaster user = new UserMaster();
        user.setCustomerId(customerId);
        user.setName(signupDto.getName() != null ? signupDto.getName() : customerId);
        user.setUserName(customerId);
        user.setSystemUserId("SYS-" + customerId);
        user.setUserGroups("STAFF");
        user.setUserEntity("BR001");
        user.setUserStatus("ACTIVE");
        user.setOrganizationId("ORG001");
        user.setUserRole("USER");
        user.setCreatedBy("system");
        user.setCreatedTime(java.time.LocalDateTime.now().toString());

        UserMaster savedUser = userMasterRepository.save(user);

        PinMaster pin = new PinMaster();
        pin.setUserId(customerId);
        pin.setPassword(signupDto.getPassword());
        pin.setSystemId(user.getSystemUserId());
        pinMasterRepository.save(pin);

        auditLogService.log(customerId, user.getName(), "USER", "ORG001", "BR001",
                "USER_REGISTERED", customerId, "SUCCESS", "User account registered", ipAddress);

        return savedUser;
    }
}
