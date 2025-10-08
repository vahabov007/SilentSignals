package com.vahabvahabov.SilentSignals.controller;

import org.springframework.ui.Model;

public interface HomeController {

    public String getHomeAfterLogin();
    public String getHomePage();

    public String getLoginPage();

    public String getForgotPasswordPage();

    public String showRegisterationForm(Model model);
}
