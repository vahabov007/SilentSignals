package com.vahabvahabov.SilentSignals.model.contact;

import com.vahabvahabov.SilentSignals.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "trusted_contact",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "email"})
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrustedContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    @NotBlank(message = "Full name must not be empty.")
    private String fullName;

    @Column(name = "email", nullable = false)
    @NotBlank(message = "Email is required.")
    @Email(message = "The email address is invalid.")
    private String email;

    @Column(name = "phone")
    @Pattern(regexp = "^$|^\\+?[0-9\\-\\s()]{10,}$", message = "The phone number format is invalid.")
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_type", nullable = false)
    private ContactType contactType;

    @Column(name = "priority_order")
    private Integer priorityOrder = 1;

    private boolean active = true;

    @CreationTimestamp
    private LocalDateTime addedAt;
}