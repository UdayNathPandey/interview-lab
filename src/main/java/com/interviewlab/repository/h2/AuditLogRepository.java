package com.interviewlab.repository.h2;

import com.interviewlab.entity.h2.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog,Long> {
}
