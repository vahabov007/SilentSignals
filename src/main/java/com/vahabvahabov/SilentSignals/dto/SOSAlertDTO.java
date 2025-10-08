package com.vahabvahabov.SilentSignals.dto;

import com.vahabvahabov.SilentSignals.model.alert.AlertStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SOSAlertDTO {
    private Long id;
    private UserDTO user;
    private LocalDateTime triggeredAt;
    private AlertStatus alertStatus;
    private String description;

}
