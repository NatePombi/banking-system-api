package com.nate.bankingsystemapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LedgerEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Account account;
    @Column(nullable = false)
    private Long amount;
    @Enumerated(EnumType.STRING)
    private Type type;
    @Enumerated(EnumType.STRING)
    private Action action;
    @ManyToOne(optional = false)
    private Transaction transaction;
    @Column(nullable = false)
    private Instant createAt;

}
