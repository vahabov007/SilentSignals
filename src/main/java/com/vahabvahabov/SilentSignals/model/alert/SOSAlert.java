package com.vahabvahabov.SilentSignals.model.alert;

import com.vahabvahabov.SilentSignals.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "sos_alert")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SOSAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    @Column(name = "location_coordinates")
    private String locationCoordinates;

    @Column(name = "location_address")
    private String locationAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_alert")
    private AlertStatus alertStatus = AlertStatus.ACTIVE;

    @Column(name = "description", length = 500)
    private String description;


}
