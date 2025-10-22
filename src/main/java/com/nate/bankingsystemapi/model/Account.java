package com.nate.bankingsystemapi.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "balance_cents", nullable = false)
    private Long balanceCents;
    @Column(nullable = false)
    private String currency;
    @ManyToOne
    private User user;
    @Version
    private Integer version;
}
