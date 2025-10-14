package com.vahabvahabov.SilentSignals.service.imp;

import com.vahabvahabov.SilentSignals.dto.TrustedContactDTO;
import com.vahabvahabov.SilentSignals.dto.UserDTO;
import com.vahabvahabov.SilentSignals.model.User;
import com.vahabvahabov.SilentSignals.model.contact.TrustedContact;
import com.vahabvahabov.SilentSignals.repository.TrustedContractRepository;
import com.vahabvahabov.SilentSignals.repository.UserRepository;
import com.vahabvahabov.SilentSignals.service.TrustedContactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TrustedContactServiceImpl implements TrustedContactService {

    private static final Logger logger = LoggerFactory.getLogger(TrustedContactServiceImpl.class);

    @Autowired
    private TrustedContractRepository trustedContractRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public TrustedContactDTO addTrustedContact(String username, TrustedContact trustedContact) {
        logger.info("Adding trusted contact for user: {}", username);

        logger.debug("Incoming trusted contact data - FullName: {}, Email: {}, Phone: {}, ContactType: {}, Priority: {}",
                trustedContact.getFullName(), trustedContact.getEmail(), trustedContact.getPhone(),
                trustedContact.getContactType(), trustedContact.getPriorityOrder());

        Optional<User> optional = userRepository.findByUsername(username);
        if(optional.isEmpty()) {
            logger.warn("User not found: {}", username);
            throw new RuntimeException("User not found.");
        }

        User user = optional.get();
        logger.debug("Found user: {} with ID: {}", user.getUsername(), user.getId());

        String email = trustedContact.getEmail().trim().toLowerCase();
        trustedContact.setEmail(email);

        logger.debug("Checking for existing contact with email: {} for user ID: {}", email, user.getId());
        Optional<TrustedContact> existingContact = trustedContractRepository.findByUserIdAndEmail(user.getId(), email);

        if (existingContact.isPresent()) {
            logger.warn("Contact with email {} already exists for user {}. Existing contact ID: {}",
                    email, username, existingContact.get().getId());
            throw new RuntimeException("A contact with this email already exists for your account.");
        } else {
            logger.debug("No existing contact found with email: {} for user: {}", email, username);
        }

        // Check all existing contacts for this user for debugging
        List<TrustedContact> allUserContacts = trustedContractRepository.findByUserId(user.getId());
        logger.debug("User {} has {} total contacts. Checking if any has email: {}",
                username, allUserContacts.size(), email);

        for (TrustedContact contact : allUserContacts) {
            logger.debug("Existing contact - ID: {}, Email: {}, FullName: {}",
                    contact.getId(), contact.getEmail(), contact.getFullName());
        }

        trustedContact.setUser(user);

        if (trustedContact.getPriorityOrder() == null) {
            trustedContact.setPriorityOrder(1);
            logger.debug("Set default priority: 1");
        }

        if (trustedContact.getPhone() != null && trustedContact.getPhone().trim().isEmpty()) {
            trustedContact.setPhone(null);
            logger.debug("Set phone to null (was empty)");
        }

        try {
            logger.debug("Attempting to save trusted contact...");
            TrustedContact savedContact = trustedContractRepository.save(trustedContact);
            logger.info("Successfully saved trusted contact with ID: {} for user: {}", savedContact.getId(), username);

            TrustedContactDTO trustedContactDTO = new TrustedContactDTO();
            BeanUtils.copyProperties(savedContact, trustedContactDTO);

            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            trustedContactDTO.setUser(userDTO);

            logger.debug("Returning trusted contact DTO with email: {}", trustedContactDTO.getEmail());
            return trustedContactDTO;

        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while saving trusted contact for user {}: {}", username, e.getMessage());
            if (e.getMessage() != null && (e.getMessage().contains("unique") || e.getMessage().contains("constraint"))) {
                logger.error("Unique constraint violation detected for email: {}", email);
                throw new RuntimeException("A contact with this email already exists for your account.");
            }
            throw new RuntimeException("Database error occurred while saving contact.");
        } catch (Exception e) {
            logger.error("Unexpected error while saving trusted contact for user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to add trusted contact: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrustedContactDTO> getAllTrustedContactsByUserId(Long userId) {
        logger.debug("Getting all trusted contacts for user ID: {}", userId);

        List<TrustedContactDTO> trustedContactDTOS = new ArrayList<>();
        List<TrustedContact> userContacts = trustedContractRepository.findByUserId(userId);

        logger.debug("Found {} raw contacts for user ID: {}", userContacts.size(), userId);

        for(TrustedContact trustedContact : userContacts) {
            TrustedContactDTO trustedContactDTO = new TrustedContactDTO();
            BeanUtils.copyProperties(trustedContact, trustedContactDTO);

            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(trustedContact.getUser(), userDTO);
            trustedContactDTO.setUser(userDTO);

            trustedContactDTOS.add(trustedContactDTO);
            logger.debug("Processed contact - ID: {}, Email: {}, FullName: {}",
                    trustedContactDTO.getId(), trustedContactDTO.getEmail(), trustedContactDTO.getFullName());
        }

        logger.debug("Returning {} trusted contacts for user ID: {}", trustedContactDTOS.size(), userId);
        return trustedContactDTOS;
    }

    @Override
    @Transactional
    public void removeTrustedContact(Long userId, Long trustedContactId) {
        logger.info("Removing trusted contact ID: {} for user ID: {}", trustedContactId, userId);

        Optional<User> optional = userRepository.findById(userId);
        Optional<TrustedContact> contactOptional = trustedContractRepository.findById(trustedContactId);

        if(optional.isEmpty()) {
            logger.warn("User not found with ID: {}", userId);
            throw new RuntimeException("User not found.");
        }
        if (contactOptional.isEmpty()) {
            logger.warn("Trusted contact not found with ID: {}", trustedContactId);
            throw new RuntimeException("Trusted contact not found.");
        }

        User user = optional.get();
        TrustedContact trustedContact = contactOptional.get();

        if(!trustedContact.getUser().getId().equals(userId)) {
            logger.warn("User {} attempted to delete contact belonging to user {}", userId, trustedContact.getUser().getId());
            throw new RuntimeException("You don't have permission to delete this contact.");
        }

        trustedContractRepository.delete(trustedContact);
        logger.info("Successfully deleted trusted contact ID: {} for user ID: {}", trustedContactId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrustedContactDTO> getAllTrustedContacts() {
        logger.debug("Getting all trusted contacts (admin method)");

        List<TrustedContactDTO> trustedContactDTOS = new ArrayList<>();
        List<TrustedContact> dbTrustedContact = trustedContractRepository.findAll();

        for(TrustedContact trustedContact : dbTrustedContact) {
            TrustedContactDTO trustedContactDTO = new TrustedContactDTO();
            BeanUtils.copyProperties(trustedContact, trustedContactDTO);

            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(trustedContact.getUser(), userDTO);
            trustedContactDTO.setUser(userDTO);

            trustedContactDTOS.add(trustedContactDTO);
        }

        return trustedContactDTOS;
    }
}