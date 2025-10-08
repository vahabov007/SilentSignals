package com.vahabvahabov.SilentSignals.service;

public interface LoginService {

    public boolean authenticate(String usernameOrEmail, String password);
}
