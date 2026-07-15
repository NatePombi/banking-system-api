package com.nate.bankingsystemapi.model.user.entity;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TestUser extends User {
    public TestUser(Long id, String fullName, String username, String password){
        super(id, fullName, username, password);
    }
}
