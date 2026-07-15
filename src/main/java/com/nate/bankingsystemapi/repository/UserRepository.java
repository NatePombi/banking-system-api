package com.nate.bankingsystemapi.repository;

import com.nate.bankingsystemapi.model.user.entity.User;
import com.nate.bankingsystemapi.model.user.enums.Role;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByIdAndUsername(Long id, String username);

    boolean existsByUsername(@NotBlank(message = "Username cannot be empty") String username);

    boolean existsByUsernameAndRole(String username, Role role);
}
