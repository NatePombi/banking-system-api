package com.nate.bankingsystemapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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
    @Column( nullable = false)
    @Min(0)
    private Long balance;
    @Column(nullable = false)
    private String currency;
    @ManyToOne
    private User user;
    @Version
    private Integer version;
}
