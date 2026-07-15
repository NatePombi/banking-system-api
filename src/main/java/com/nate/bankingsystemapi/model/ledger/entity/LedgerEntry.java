package com.nate.bankingsystemapi.model.ledger.entity;

import com.nate.bankingsystemapi.model.ledger.enums.Type;
import com.nate.bankingsystemapi.model.account.entity.Account;
import com.nate.bankingsystemapi.model.transaction.entity.Transactions;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "ledger_entry")

public class LedgerEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    @Column(nullable = false)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "entry_type",nullable = false)
    private Type type;
    @ManyToOne(optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transactions transactions;
    @Column(nullable = false,name = "created_at")
    private Instant createAt;


    private LedgerEntry(Transactions transactions,Account account,Type type,BigDecimal amount) {
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.createAt = Instant.now();
        this.transactions = transactions;
    }

    public static LedgerEntry debit(Transactions transactions, Account account, BigDecimal amount){

        return new LedgerEntry(transactions,account,Type.DEBIT,amount);
    }

    public static LedgerEntry credit(Transactions transactions,Account account, BigDecimal amount){
        return new LedgerEntry(transactions,account,Type.CREDIT,amount);
    }
}
