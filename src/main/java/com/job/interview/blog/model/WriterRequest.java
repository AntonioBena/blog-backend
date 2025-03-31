package com.job.interview.blog.model;

import com.job.interview.blog.model.user.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "writer_request")
public class WriterRequest extends AuditingModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Valid
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_entity_id")
    private UserEntity user;
}