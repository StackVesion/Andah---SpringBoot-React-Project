package tn.esprit.reservation_service.service;

import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class EmailService {


     JavaMailSender mailSender;

    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        try {
            mailSender.send(message);  // Envoie l'email
            System.out.println("E-mail envoyé avec succès");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'envoi de l'e-mail");
        }
    }
    /**
     * Fonction pour envoyer un e-mail HTML pour informer d'une modification du profil.
     * @param to L'adresse e-mail du destinataire
     * @param userName Le nom de l'utilisateur à inclure dans le message
     */
    public void sendProfileModificationEmail(String to, String userName) {
        String htmlContent = "<html><body>"
                + "<h1>Annulation de votre réservation</h1>"
                + "<p>Bonjour " + userName + ",</p>"
                + "<p>Nous vous informons que votre réservation de scooter a été <strong>annulée</strong>. "
                + "Si vous n'êtes pas à l'origine de cette action, veuillez nous contacter immédiatement.</p>"
                + "<p>Pour consulter les détails de votre réservation, cliquez sur le bouton ci-dessous :</p>"
                + "<a href='http://exemple.com/reservations' style='background-color: #553CDF; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Voir mes réservations</a>"
                + "<p>Merci pour votre compréhension,</p>"
                + "<p>L'équipe de Scooter Service</p>"
                + "<div class='footer' style='color: #999999; font-size: 12px; text-align: center; margin-top: 30px;'>"
                + "<p>Pour toute question, contactez-nous à support@exemple.com.</p>"
                + "<p>© 2025 Scooter Service. Tous droits réservés.</p>"
                + "</div>"
                + "</body></html>";


        try {
            // Création du message MIME
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);  // true pour activer le HTML

            // Configuration de l'e-mail
            helper.setTo(to);
            helper.setSubject("Modification de votre profil");
            helper.setText(htmlContent, true);  // Le "true" indique qu'il s'agit d'un contenu HTML

            // Envoi de l'e-mail
            mailSender.send(message);
            System.out.println("E-mail de modification de profil envoyé avec succès");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'envoi de l'e-mail");
        }
    }
}
