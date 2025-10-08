package com.vahabvahabov.SilentSignals.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class UserDTO {

    private Long id;

    private String username;

    private Date date_of_birth;

    private LocalDateTime createdAt;

}






