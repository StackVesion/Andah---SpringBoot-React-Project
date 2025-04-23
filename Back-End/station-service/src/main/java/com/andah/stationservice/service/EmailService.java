package com.andah.stationservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendStationCreationEmail(String to, String stationName, String stationLocation) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("New Station Created: " + stationName);

            String emailContent = """
                <html>
                <body>
                    <h2>New Station Created</h2>
                    <p>Congratulations! Your new station has been successfully created.</p>
                    <p><b>Station Name:</b> %s</p>
                    <p><b>Location:</b> %s</p>
                    <p>You can now manage this station through your dashboard.</p>
                    <p>Thank you for using our platform!</p>
                </body>
                </html>
                """.formatted(stationName, stationLocation);

            helper.setText(emailContent, true); // true indicates this is HTML
            mailSender.send(message);

        } catch (MessagingException e) {
            // Log the error but don't fail the station creation process
            throw new RuntimeException("Failed to send email notification", e);
        }
    }
}