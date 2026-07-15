package com.nate.bankingsystemapi.seed;

import com.nate.bankingsystemapi.model.user.entity.User;
import com.nate.bankingsystemapi.model.user.enums.Role;
import com.nate.bankingsystemapi.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SeedData implements CommandLineRunner {

    private UserRepository repo;
    private PasswordEncoder encoder;

    @Override
    public void run(String... args) throws Exception {
        if(repo.findByUsername("admin").isEmpty()){
            User user = User.createUser("Admin User","admin",encoder.encode("admin123"));
            user.changeRole(Role.ADMIN);
            repo.save(user);

            System.out.println("Default admin created: username = admin, password = admin123");
        }
    }
}
