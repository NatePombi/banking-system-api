package com.nate.bankingsystemapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"requestID","username"})
        }
)
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(nullable = false)
    private String requestID;
    @Column(nullable = false)
    private Long amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Action action;
    @ManyToOne
    private Account fromAccount;
    @ManyToOne
    private Account toAccount;
    @NotNull
    private String username;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(nullable = false, updatable = false)
    private Instant instant;

}
