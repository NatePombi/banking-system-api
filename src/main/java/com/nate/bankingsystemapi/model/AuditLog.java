package com.nate.bankingsystemapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Action action;
    @Column(name = "performed_by", nullable = false)
    private String performedBy;
    @Column(nullable = false)
    private String details;
    private LocalDateTime createdAt = LocalDateTime.now();
}
