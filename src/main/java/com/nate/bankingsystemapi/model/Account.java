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
    @Column(nullable = false)
    private Long balanceCent;
    @Column(nullable = false)
    private String currency;
    @ManyToOne
    private User user;
    @Version
    private Integer integer;
}
