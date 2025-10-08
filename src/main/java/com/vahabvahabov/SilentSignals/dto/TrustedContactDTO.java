package com.vahabvahabov.SilentSignals.dto;

import com.vahabvahabov.SilentSignals.model.contact.ContactType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrustedContactDTO {
    private Long id;
    private UserDTO user;
    private String email;
    private String fullName;
    private ContactType contactType;
    private Integer priorityOrder;
}
