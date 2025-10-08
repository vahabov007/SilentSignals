package com.vahabvahabov.SilentSignals.service.imp;

import com.vahabvahabov.SilentSignals.model.User;
import com.vahabvahabov.SilentSignals.service.LoginService;
import com.vahabvahabov.SilentSignals.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public boolean authenticate(String usernameOrEmail, String password) {
        Optional<User> optional;

        if(usernameOrEmail.contains("@")) {
            optional = userService.findUserByMail(usernameOrEmail);
        }else {
            optional = userService.findUserByUsername(usernameOrEmail);
        }
        if(optional.isPresent()) {
            User user = optional.get();
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }
}
