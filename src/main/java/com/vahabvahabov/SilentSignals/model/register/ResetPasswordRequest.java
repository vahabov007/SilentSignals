package com.vahabvahabov.SilentSignals.model.register;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String mail;

    private String newPassword;
}
