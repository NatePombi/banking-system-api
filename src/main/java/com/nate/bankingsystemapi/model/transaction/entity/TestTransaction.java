package com.nate.bankingsystemapi.model.transaction.entity;

public class TestTransaction extends Transactions{
    public TestTransaction(Long id, String idempotencyKey) {
        super(id, idempotencyKey);
    }
}
