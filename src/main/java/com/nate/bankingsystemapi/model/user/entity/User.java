package com.nate.bankingsystemapi.model.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nate.bankingsystemapi.model.user.enums.Role;
import com.nate.bankingsystemapi.model.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username")
})
public class User implements UserDetails {
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
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private Role role;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserStatus status;
    @CreationTimestamp
    @Column(nullable = false, name = "created_at")
    private Instant createdAt;



    public static User createUser(String fullName, String username, String password) {
        User user = new User();
        user.fullName = fullName;
        user.username = username;
        user.password = password;
        user.role = Role.USER;
        user.status = UserStatus.ACTIVE;

        return user;
    }

    protected User(Long id, String fullName, String username, String password) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.role = Role.USER;
        this.status = UserStatus.ACTIVE;
    }


    public void changeRole(Role role) {
        this.role = role;
    }
    public void changeStatus(UserStatus status){this.status = status;}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toString()));
    }
}
