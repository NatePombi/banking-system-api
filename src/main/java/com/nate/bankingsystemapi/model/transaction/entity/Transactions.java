package com.nate.bankingsystemapi.model.transaction.entity;

import com.nate.bankingsystemapi.model.transaction.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "transactions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "id")
        }
)
public class Transactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(name = "idempotency_key", nullable = false,unique = true)
    private String idempotencyKey;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Status status;
    @Column(name = "failure_reason")
    private String failureReason;
    @CreationTimestamp
    @Column(nullable = false, name = "created_at")
    private Instant createdAt;

    public static Transactions create(String idempotencyKey){
        Transactions transactions = new Transactions();
        transactions.status = Status.CREATED;
        transactions.createdAt = Instant.now();
        transactions.idempotencyKey = idempotencyKey;
        return transactions;
    }

    Transactions(Long id ,String idempotencyKey){
        this.id = id;
        this.status = Status.CREATED;
        this.createdAt = Instant.now();
        this.idempotencyKey = idempotencyKey;
    }

    public void markFailed(String failureReason){
        this.failureReason = failureReason;
        this.status = Status.FAILED;
    }

    public void markProcessing(){
        this.status = Status.PROCESSING;
    }

    public void markSuccess(){
        this.status = Status.SUCCESS;
    }



}
