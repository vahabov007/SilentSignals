package com.vahabvahabov.SilentSignals.service;

import com.vahabvahabov.SilentSignals.dto.TrustedContactDTO;
import com.vahabvahabov.SilentSignals.model.User;
import com.vahabvahabov.SilentSignals.model.contact.TrustedContact;

import java.util.List;

public interface TrustedContactService {

    public TrustedContactDTO addTrustedContact(String username, TrustedContact trustedContact);

    public List<TrustedContactDTO> getAllTrustedContactsByUserId(Long userId);

    public void removeTrustedContact(Long userId, Long trustedId);

    public List<TrustedContactDTO> getAllTrustedContacts();
}
