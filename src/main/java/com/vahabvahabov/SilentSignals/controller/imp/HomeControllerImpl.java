package com.vahabvahabov.SilentSignals.controller.imp;

import com.vahabvahabov.SilentSignals.controller.HomeController;
import com.vahabvahabov.SilentSignals.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeControllerImpl implements HomeController {

    @Override
    @GetMapping("/home")
    public String getHomeAfterLogin() {
        return "home"; //
    }

    @Override
    @GetMapping("/")
    public String getHomePage() {
        return "home";
    }

    @Override
    @GetMapping("/my-login")
    public String getLoginPage() {
        return "my-login";
    }

    @Override
    @GetMapping("/forgot-password")
    public String getForgotPasswordPage() {
        return "forgot-password";
    }

    @Override
    @GetMapping("/register")
    public String showRegisterationForm(Model model) {
        model.addAttribute("user", new User());
        return "create-account";
    }
}