package com.nate.bankingsystemapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Entity
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true,nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    private Role roles;
}
