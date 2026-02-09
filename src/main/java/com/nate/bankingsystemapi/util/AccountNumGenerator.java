package com.nate.bankingsystemapi.util;

import java.security.SecureRandom;

public class AccountNumGenerator {

    public static Long generateAccNum(){
        SecureRandom random = new SecureRandom();
        return 1000000000L + random.nextLong(9000000000L);
    }
}
