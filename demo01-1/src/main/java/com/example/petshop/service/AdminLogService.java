package com.example.petshop.service;

import com.example.petshop.entity.AdminLog;
import com.example.petshop.entity.User;
import com.example.petshop.repository.AdminLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminLogService {
    private final AdminLogRepository adminLogRepository;

    public AdminLogService(AdminLogRepository adminLogRepository) {
        this.adminLogRepository = adminLogRepository;
    }

    public enum Action {
        ADD, UPDATE, DELETE, APPROVE, REJECT, CHANGE_STATUS
    }

    public void log(User admin, Action action, String entityType, Long entityId, String description) {
        AdminLog log = new AdminLog();
        log.setAdmin(admin);
        log.setAction(AdminLog.Action.valueOf(action.name()));
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDescription(description);
        adminLogRepository.save(log);
    }
}
