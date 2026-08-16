package com.nate.bankingsystemapi.model.account.entity;

import com.nate.bankingsystemapi.exception.InsufficientAmountException;
import com.nate.bankingsystemapi.exception.InvalidAmountException;
import com.nate.bankingsystemapi.exception.NoAmountException;
import com.nate.bankingsystemapi.model.account.enums.CurrencyCode;
import com.nate.bankingsystemapi.model.user.entity.User;
import com.nate.bankingsystemapi.util.AccountNumGenerator;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
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
    private BigDecimal balance;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
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
        account.balance = BigDecimal.ZERO;
        account.currency = code;
        account.user = user;
        return account;
    }

     Account(Long id, User user,CurrencyCode code) {
        this.id = id;
        this.accountNum = AccountNumGenerator.generateAccNum();
        this.balance = BigDecimal.ZERO;
        this.currency = code;
        this.user = user;
    }

    public void debit(BigDecimal amount) {
        if(amount == null){
            throw new NoAmountException("Amount is required");
        }

        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidAmountException("Amount must be greater than zero");
        }


        if (amount.compareTo(balance) > 0 ) {
            throw new InsufficientAmountException("Insufficient balance");
        }


        this.balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        if(amount == null){
            throw new NoAmountException("Amount is required");
        }

        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidAmountException("Amount must be greater than zero");
        }

        this.balance = balance.add(amount);
    }


}
