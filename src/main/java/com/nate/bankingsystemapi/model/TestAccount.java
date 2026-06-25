package com.nate.bankingsystemapi.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TestAccount extends Account {

    public TestAccount(Long id,User user,CurrencyCode code) {
        super(id,user,code);
    }
}
