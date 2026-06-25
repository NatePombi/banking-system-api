package com.nate.bankingsystemapi.model;

import com.nate.bankingsystemapi.util.AccountNumGenerator;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;


@Entity
@NoArgsConstructor
@Getter
@Table(name = "accounts", uniqueConstraints = {
        @UniqueConstraint(columnNames = "account_number"),
        @UniqueConstraint(columnNames = {"user_id","currency"})
})
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false,name = "account_number")
    private Long accountNum;
    @Column( nullable = false)
    @Min(0)
    private Long balance;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrencyCode currency;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "user_id")
    private User user;
    @CreationTimestamp
    @Column(name = "created_at",nullable = false,updatable = false)
    private Timestamp createdAt;

    public static Account create(User user, CurrencyCode code) {
        Account account = new Account();
        account.accountNum = AccountNumGenerator.generateAccNum();
        account.balance = 0L;
        account.currency = code;
        account.user = user;
        return account;
    }

     Account(Long id, User user,CurrencyCode code) {
        this.id = id;
        this.accountNum = AccountNumGenerator.generateAccNum();
        this.balance = 0L;
        this.currency = code;
        this.user = user;
    }

    public void changeBalance(Long balance) {
        this.balance = balance;
    }


}
