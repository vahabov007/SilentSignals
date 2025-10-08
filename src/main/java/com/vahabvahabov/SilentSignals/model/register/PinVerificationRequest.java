package com.vahabvahabov.SilentSignals.model.register;

import lombok.Data;

@Data
public class PinVerificationRequest {

    private String mail;

    private String pin;
}