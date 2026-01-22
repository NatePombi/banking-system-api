package com.nate.bankingsystemapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Getter
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"requestID","username"})
        }
)
public class Transactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(nullable = false,unique = true)
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

    public Transactions(Long amount,String username,String requestID){
        this.amount = amount;
        this.username = username;
        this.requestID = requestID;
        this.status = Status.PENDING;
        this.instant = Instant.now();
    }

    public void changeAction(Action action){
        this.action = action;
    }

    public void changeStatus(Status status){
        this.status = status;
    }

    public void changeFromAccount(Account fromAccount){
        this.fromAccount = fromAccount;
    }

    public void changeToAccount(Account toAccount){
        this.toAccount = toAccount;
    }


}
