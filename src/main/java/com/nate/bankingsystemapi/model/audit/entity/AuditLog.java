package com.nate.bankingsystemapi.model.audit.entity;

import com.nate.bankingsystemapi.model.audit.enums.Action;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "audit_log")
@Getter

public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Action action;
    @Column(name = "performed_by", nullable = false)
    private String performedBy;
    @Column(nullable = false, length = 1000)
    private String details;
    @CreationTimestamp
    private LocalDateTime createdAt;

    public AuditLog(Action action, String performedBy, String details) {
        this.action = action;
        this.details = details;
        this.performedBy = performedBy;
    }
}
