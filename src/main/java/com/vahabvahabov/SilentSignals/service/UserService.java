package com.vahabvahabov.SilentSignals.service;

import com.vahabvahabov.SilentSignals.model.User;
import java.util.Optional;

public interface UserService {
    Optional<User> findUserByMail(String mail);

    Optional<User> findUserByUsername(String username);

    void saveNewUser(User user);
}