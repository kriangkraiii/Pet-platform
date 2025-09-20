package com.example.petshop.repository;

import com.example.petshop.entity.AdminLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {
    Page<AdminLog> findByAdminIdOrderByCreatedAtDesc(Long adminId, Pageable pageable);
}
