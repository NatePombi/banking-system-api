package com.nate.bankingsystemapi.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long amountCents;
    @ManyToOne
    private Account fromAccount;
    @ManyToOne
    private Account toAccount;
    @Enumerated(EnumType.STRING)
    private Status status;
}
