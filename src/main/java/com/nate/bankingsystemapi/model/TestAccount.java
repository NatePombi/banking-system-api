package com.nate.bankingsystemapi.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TestAccount extends Account {

    public TestAccount(Long id,Long userId) {
        super(id,userId);
    }
}
