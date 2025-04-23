package com.andah.userservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.email.simulation:false}")
    private boolean simulationMode;

    public void sendStationCreationEmail(String to, String stationName, String stationLocation) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
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
            emailSender.send(message);

        } catch (MessagingException e) {
            // Log the error but don't fail the station creation process
            throw new RuntimeException("Failed to send email notification", e);
        }
    }

    /**
     * Send a simple email message
     * 
     * @param to      Recipient email address
     * @param subject Email subject
     * @param text    Email content
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        logger.info("Preparing to send email to: {}", to);

        if (simulationMode) {
            logger.info("SIMULATION MODE - Email not actually sent");
            logger.info("Would send email to: {} with subject: {}", to, subject);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            logger.debug("Sending email through JavaMailSender");
            emailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}