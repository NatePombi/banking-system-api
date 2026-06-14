package com.nate.bankingsystemapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Entity
@Getter
@NoArgsConstructor
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String fullName;
    @Column(unique = true,nullable = false)
    private String username;
    @JsonIgnore
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;



    public static User createUser(String fullName, String username, String password) {
        User user = new User();
        user.fullName = fullName;
        user.username = username;
        user.password = password;
        user.role = Role.USER;

        return user;
    }

    User(Long id, String fullName, String username, String password) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.role = Role.USER;
    }


    public void changeRole(Role role) {
        this.role = role;
    }

}
