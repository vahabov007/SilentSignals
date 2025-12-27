package com.vahabvahabov.SilentSignals.service.imp;

import com.vahabvahabov.SilentSignals.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String myEmail;

    @Override
    @Async
    public void sendPinToEmail(String toEmail, String pin) throws MessagingException {

        MimeMessage message = javaMailSender.createMimeMessage();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true, "UTF-8");

        mimeMessageHelper.setFrom(myEmail);
        mimeMessageHelper.setTo(toEmail);
        mimeMessageHelper.setSubject("Your Account Verification Code");

        String htmlContent = buildEmailContent(pin);

        mimeMessageHelper.setText(htmlContent, true);

        javaMailSender.send(message);

    }
    @Override
    public void sendSosAlertReminder(String toEmail, String username, String description, String locationAddress) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true, "UTF-8");
        mimeMessageHelper.setFrom(myEmail);
        mimeMessageHelper.setTo(toEmail);
        mimeMessageHelper.setSubject("REMINDER: SOS Alert from " + username);
        String htmlContent = buildSosReminderEmailContent(username, description, locationAddress);
        mimeMessageHelper.setText(htmlContent, true);
        javaMailSender.send(message);
    }



    @Override
    public void sendSosAlert(String toEmail, String username, String description, String locationAddress) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true, "UTF-8");
        mimeMessageHelper.setFrom(myEmail);
        mimeMessageHelper.setTo(toEmail);
        mimeMessageHelper.setSubject("SOS Alert from " + username);
        String htmlContent = buildSosEmailContent(username, description, locationAddress);
        mimeMessageHelper.setText(htmlContent, true);
        javaMailSender.send(message);
    }

    private String buildEmailContent(String pin) {
        String html = "<div style=\"font-family: Arial, sans-serif; padding: 20px; background-color: #f4f4f4; border-radius: 8px; max-width: 600px; margin: auto;\">"
                + "  <div style=\"background-color: #ffffff; padding: 30px; border-radius: 8px; border-top: 5px solid #007bff;\">"
                + "    <h2 style=\"color: #333333; text-align: center; border-bottom: 1px solid #eeeeee; padding-bottom: 10px;\">Account Verification</h2>"
                + "    <p style=\"color: #555555; font-size: 16px;\">Dear user,</p>"
                + "    <p style=\"color: #555555; font-size: 16px;\">To complete your account verification, please use the PIN code below:</p>"
                + "    "
                + "    <div style=\"text-align: center; margin: 30px 0;\">"
                + "      <span style=\"background-color: #007bff; color: #ffffff; font-size: 28px; padding: 15px 30px; border-radius: 5px; font-weight: bold; letter-spacing: 5px; display: inline-block;\">"
                +          pin
                + "      </span>"
                + "    </div>"
                + "    "
                + "    <p style=\"color: #555555; font-size: 16px;\">This code is valid for a limited time and should be used immediately.</p>"
                + "    <p style=\"color: #999999; font-size: 14px; border-top: 1px solid #eeeeee; padding-top: 15px; margin-top: 20px;\">For your security, please do not share this code with anyone.</p>"
                + "  </div>"
                + "  <p style=\"text-align: center; color: #aaaaaa; font-size: 12px; margin-top: 20px;\">This is an automated message, please do not reply.</p>"
                + "</div>";

        return html;
    }

    private String buildSosEmailContent(String username, String description, String locationAddress) {
        return "<div style=\"font-family: Arial, sans-serif; padding: 20px; background-color: #f4f4f4; border-radius: 8px; max-width: 600px; margin: auto;\">"
                + "  <div style=\"background-color: #ffffff; padding: 30px; border-radius: 8px; border-top: 5px solid #ff0000;\">"
                + "    <h2 style=\"color: #333333; text-align: center; border-bottom: 1px solid #eeeeee; padding-bottom: 10px;\">Emergency SOS Alert</h2>"
                + "    <p style=\"color: #555555; font-size: 16px;\">Dear recipient,</p>"
                + "    <p style=\"color: #555555; font-size: 16px;\">You have received an SOS alert from <strong>" + username + "</strong>.</p>"
                + "    <p style=\"color: #555555; font-size: 16px;\"><strong>Description:</strong> " + description + "</p>"
                + "    <p style=\"color: #555555; font-size: 16px;\"><strong>Location:</strong> " + locationAddress + "</p>"
                + "    <p style=\"color: #ff0000; font-size: 16px; font-weight: bold;\">Please take immediate action to ensure their safety.</p>"
                + "    <p style=\"color: #999999; font-size: 14px; border-top: 1px solid #eeeeee; padding-top: 15px; margin-top: 20px;\">This is an automated message, please do not reply.</p>"
                + "  </div>"
                + "</div>";
    }
    private String buildSosReminderEmailContent(String username, String description, String locationAddress) {
        return "<div style=\"font-family: Arial, sans-serif; padding: 20px; background-color: #f4f4f4; border-radius: 8px; max-width: 600px; margin: auto;\">"
                + "  <div style=\"background-color: #ffffff; padding: 30px; border-radius: 8px; border-top: 5px solid #ff9900;\">"
                + "    <h2 style=\"color: #333333; text-align: center; border-bottom: 1px solid #eeeeee; padding-bottom: 10px;\">REMINDER: Emergency SOS Alert</h2>"
                + "    <p style=\"color: #555555; font-size: 16px;\">Dear recipient,</p>"
                + "    <p style=\"color: #555555; font-size: 16px;\">This is a <strong>REMINDER</strong> for the SOS alert from <strong>" + username + "</strong>.</p>"
                + "    <p style=\"color: #555555; font-size: 16px;\"><strong>Description:</strong> " + description + "</p>"
                + "    <p style=\"color: #555555; font-size: 16px;\"><strong>Location:</strong> " + locationAddress + "</p>"
                + "    <p style=\"color: #ff9900; font-size: 16px; font-weight: bold;\">This alert is still active. Please ensure their safety.</p>"
                + "    <p style=\"color: #999999; font-size: 14px; border-top: 1px solid #eeeeee; padding-top: 15px; margin-top: 20px;\">This is an automated reminder message, please do not reply.</p>"
                + "  </div>"
                + "</div>";
    }


}