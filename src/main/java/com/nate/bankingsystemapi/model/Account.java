package com.nate.bankingsystemapi.model;

import com.nate.bankingsystemapi.util.JwtUtil;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@NoArgsConstructor
@Getter
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false,unique = true,name = "account_number")
    private Long accountNum;
    @Column( nullable = false)
    @Min(0)
    private Long balance;
    @Column(nullable = false)
    private String currency;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    public Account(User user) {
        this.accountNum = JwtUtil.generateAccNum();
        this.balance = 0L;
        this.currency = "ZAR";
        this.user = user;
    }

     Account(Long id, User user) {
        this.id = id;
        this.accountNum = JwtUtil.generateAccNum();
        this.balance = 0L;
        this.currency = "ZAR";
        this.user = user;
    }

    public void changeBalance(Long balance) {
        this.balance = balance;
    }

    public void changeCurrency(String currency) {
        this.currency = currency;
    }



}
