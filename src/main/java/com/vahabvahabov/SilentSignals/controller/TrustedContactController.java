package com.vahabvahabov.SilentSignals.controller;

import com.vahabvahabov.SilentSignals.dto.TrustedContactDTO;
import com.vahabvahabov.SilentSignals.model.contact.TrustedContact;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;

import java.util.List;

public interface TrustedContactController {

    public ResponseEntity<?> addTrustedContact(TrustedContact trustedContact, BindingResult result);
    public ResponseEntity<?> getAllTrustedContacts();
    public ResponseEntity<?> deleteTrustedContactById(Long contactId);
}
