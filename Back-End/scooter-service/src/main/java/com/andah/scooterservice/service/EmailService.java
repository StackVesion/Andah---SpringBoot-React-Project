package com.andah.scooterservice.service;

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

    public void sendScooterCreationEmail(String to, String scooterName, String description, Double price, String status) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("New Scooter Created: " + scooterName);

            String emailContent = """
                <html>
                <body>
                    <h2>New Scooter Created</h2>
                    <p>Congratulations! A new scooter has been successfully created.</p>
                    <p><b>Scooter Name:</b> %s</p>
                    <p><b>Description:</b> %s</p>
                    <p><b>Price:</b> $%s</p>
                    <p><b>Status:</b> %s</p>
                    <p>You can now manage this scooter through your dashboard.</p>
                    <p>Thank you for using our platform!</p>
                </body>
                </html>
                """.formatted(scooterName, description, price, status);

            helper.setText(emailContent, true); // true indicates this is HTML
            mailSender.send(message);

        } catch (MessagingException e) {
            // Log the error but don't fail the scooter creation process
            throw new RuntimeException("Failed to send email notification", e);
        }
    }
}
