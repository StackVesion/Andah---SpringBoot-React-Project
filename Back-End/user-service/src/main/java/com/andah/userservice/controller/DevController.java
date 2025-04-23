package com.andah.userservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import com.andah.userservice.repository.UserRepository;
import com.andah.userservice.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la visualisation des codes OTP en mode développement
 */
@Controller
@RequestMapping("/dev")
public class DevController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Page HTML simple pour voir tous les codes OTP actuels
     */
    @GetMapping("/otp-codes")
    @ResponseBody
    public String viewOtpCodes() {
        List<User> usersWithOtp = userRepository.findAll().stream()
                .filter(user -> user.getTempOtp() != null && !user.getTempOtp().isEmpty())
                .collect(Collectors.toList());

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
            .append("<html lang=\"fr\">\n")
            .append("<head>\n")
            .append("    <meta charset=\"UTF-8\">\n")
            .append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
            .append("    <title>Codes OTP - Mode Développement</title>\n")
            .append("    <style>\n")
            .append("        body { font-family: Arial, sans-serif; margin: 20px; }\n")
            .append("        h1 { color: #2c3e50; }\n")
            .append("        table { border-collapse: collapse; width: 100%; }\n")
            .append("        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n")
            .append("        th { background-color: #f2f2f2; }\n")
            .append("        tr:nth-child(even) { background-color: #f9f9f9; }\n")
            .append("        .info { background-color: #d4edda; border: 1px solid #c3e6cb; padding: 15px; border-radius: 5px; margin-bottom: 20px; }\n")
            .append("        .default-code { font-weight: bold; color: #e74c3c; }\n")
            .append("    </style>\n")
            .append("</head>\n")
            .append("<body>\n")
            .append("    <h1>Codes OTP - Mode Développement</h1>\n")
            .append("    <div class=\"info\">\n")
            .append("        <p>Cette page affiche tous les codes OTP temporaires actuellement valides dans le système.</p>\n")
            .append("        <p>En mode développement, le code par défaut est : <span class=\"default-code\">123456</span></p>\n")
            .append("        <p>Utilisez ce code pour toutes vos validations OTP pendant le développement.</p>\n")
            .append("    </div>\n")
            .append("    <h2>Codes OTP actifs</h2>\n");

        if (usersWithOtp.isEmpty()) {
            html.append("    <p>Aucun code OTP actif trouvé. Utilisez le code par défaut : <span class=\"default-code\">123456</span></p>\n");
        } else {
            html.append("    <table>\n")
                .append("        <tr>\n")
                .append("            <th>Email</th>\n")
                .append("            <th>Code OTP</th>\n")
                .append("            <th>Expire à</th>\n")
                .append("        </tr>\n");

            for (User user : usersWithOtp) {
                html.append("        <tr>\n")
                    .append("            <td>").append(user.getEmail()).append("</td>\n")
                    .append("            <td><span class=\"default-code\">").append(user.getTempOtp()).append("</span></td>\n")
                    .append("            <td>").append(user.getTempOtpExpiryTime()).append("</td>\n")
                    .append("        </tr>\n");
            }

            html.append("    </table>\n");
        }

        html.append("    <h2>Tester un code OTP</h2>\n")
            .append("    <form id=\"validateForm\" action=\"javascript:void(0);\">\n")
            .append("        <div style=\"margin-bottom: 10px;\">\n")
            .append("            <label for=\"email\">Email:</label>\n")
            .append("            <input type=\"email\" id=\"email\" name=\"email\" required style=\"width: 300px; padding: 5px;\">\n")
            .append("        </div>\n")
            .append("        <div style=\"margin-bottom: 10px;\">\n")
            .append("            <label for=\"otpCode\">Code OTP:</label>\n")
            .append("            <input type=\"text\" id=\"otpCode\" name=\"otpCode\" value=\"123456\" required style=\"width: 100px; padding: 5px;\">\n")
            .append("        </div>\n")
            .append("        <button type=\"submit\" style=\"padding: 8px 15px; background-color: #3498db; color: white; border: none; cursor: pointer;\">Valider le code</button>\n")
            .append("    </form>\n")
            .append("    <div id=\"result\" style=\"margin-top: 20px; padding: 10px; display: none;\"></div>\n")
            .append("    <script>\n")
            .append("        document.getElementById('validateForm').addEventListener('submit', function(e) {\n")
            .append("            e.preventDefault();\n")
            .append("            const email = document.getElementById('email').value;\n")
            .append("            const otpCode = document.getElementById('otpCode').value;\n")
            .append("            const resultDiv = document.getElementById('result');\n")
            .append("            \n")
            .append("            // Appel à l'API pour valider le code OTP\n")
            .append("            fetch('/api/otp/validate-temp', {\n")
            .append("                method: 'POST',\n")
            .append("                headers: {\n")
            .append("                    'Content-Type': 'application/json',\n")
            .append("                },\n")
            .append("                body: JSON.stringify({ email: email, otpCode: otpCode }),\n")
            .append("            })\n")
            .append("            .then(response => response.json())\n")
            .append("            .then(data => {\n")
            .append("                resultDiv.style.display = 'block';\n")
            .append("                if (data.valid) {\n")
            .append("                    resultDiv.style.backgroundColor = '#d4edda';\n")
            .append("                    resultDiv.innerHTML = '<p>✅ ' + data.message + '</p>';\n")
            .append("                } else {\n")
            .append("                    resultDiv.style.backgroundColor = '#f8d7da';\n")
            .append("                    resultDiv.innerHTML = '<p>❌ ' + data.message + '</p>';\n")
            .append("                }\n")
            .append("            })\n")
            .append("            .catch(error => {\n")
            .append("                resultDiv.style.display = 'block';\n")
            .append("                resultDiv.style.backgroundColor = '#f8d7da';\n")
            .append("                resultDiv.innerHTML = '<p>❌ Erreur: ' + error.message + '</p>';\n")
            .append("            });\n")
            .append("        });\n")
            .append("    </script>\n")
            .append("</body>\n")
            .append("</html>");

        return html.toString();
    }

    /**
     * Endpoint API pour générer un code OTP pour un utilisateur spécifique
     */
    @GetMapping("/generate-otp")
    @ResponseBody
    public ResponseEntity<?> generateOtpForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null);
                
        if (user == null) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Utilisateur non trouvé avec l'email: " + email);
            return ResponseEntity.badRequest().body(response);
        }
        
        // Toujours utiliser le code 123456 en mode développement
        user.setTempOtp("123456");
        user.setTempOtpExpiryTime(java.time.LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Code OTP généré avec succès pour " + email);
        response.put("code", "123456");
        return ResponseEntity.ok(response);
    }
}