package com.nate.bankingsystemapi.model.account.entity;

import com.nate.bankingsystemapi.model.account.enums.CurrencyCode;
import com.nate.bankingsystemapi.model.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TestAccount extends Account {

    public TestAccount(Long id, User user, CurrencyCode code) {
        super(id,user,code);
    }
}
