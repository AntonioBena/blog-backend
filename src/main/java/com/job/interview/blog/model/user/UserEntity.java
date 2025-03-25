package com.job.interview.blog.model.user;

import com.job.interview.blog.model.AuditingModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_entity")
public class UserEntity extends AuditingModel
        implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_entity_id")
    private Long id;
    @NotEmpty(message = "First name is mandatory")
    @NotBlank(message = "First name is mandatory")
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    @NotEmpty(message = "Last name is mandatory")
    @NotBlank(message = "Last name is mandatory")
    private String lastName;
    @NotEmpty(message = "Password is mandatory")
    @NotBlank(message = "Password is mandatory")
    @Column(name = "user_password")
    private String password;
    @NotEmpty(message = "Email is mandatory")
    @NotBlank(message = "Email is mandatory")
    @Column(name = "user_email", unique = true)
    private String email;
    private boolean enabled;
    private boolean accountLocked;

    @Enumerated(EnumType.STRING)
    private UserRole role;
}