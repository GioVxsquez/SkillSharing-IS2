package com.skillsharing.application.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

// servicio de envio de correos - HU29 (activar cuenta)
// principio srp: solo se encarga de enviar correos, nada mas
// se usa @Async para no bloquear el hilo de la peticion mientras se envia el correo
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String mailUsername;

    // envia el correo de verificacion con el enlace para activar la cuenta
    @Async
    public void enviarCorreoVerificacion(String destinatario, String nombre, String token) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(mailUsername);
            helper.setTo(destinatario);
            helper.setSubject("SkillSharing - Verifica tu cuenta");

            String enlace = baseUrl + "/api/auth/verificar?token=" + token;

            String cuerpoHtml = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <style>
                            body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
                            .container { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                            .header { background-color: #1B3A6B; padding: 30px; text-align: center; }
                            .header h1 { color: #FFD700; margin: 0; font-size: 28px; letter-spacing: 2px; }
                            .header p { color: #c8d8f0; margin: 5px 0 0; font-size: 14px; }
                            .body { padding: 40px 30px; }
                            .body h2 { color: #1B3A6B; font-size: 22px; }
                            .body p { color: #555; line-height: 1.7; font-size: 15px; }
                            .btn { display: inline-block; margin: 25px 0; padding: 15px 35px; background-color: #1B3A6B; color: #ffffff !important; text-decoration: none; border-radius: 8px; font-size: 16px; font-weight: bold; }
                            .footer { background-color: #f9f9f9; text-align: center; padding: 20px; font-size: 12px; color: #aaa; border-top: 1px solid #eee; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <h1>SkillSharing</h1>
                                <p>Plataforma de aprendizaje colaborativo</p>
                            </div>
                            <div class="body">
                                <h2>¡Hola, %s!</h2>
                                <p>Gracias por registrarte en <strong>SkillSharing</strong>. Estás a un solo clic de activar tu cuenta y comenzar a aprender.</p>
                                <p>Haz clic en el botón de abajo para verificar tu correo electrónico:</p>
                                <a href="%s" class="btn">Verificar mi cuenta</a>
                                <p>Si no puedes hacer clic en el botón, copia y pega este enlace en tu navegador:</p>
                                <p style="word-break: break-all; color: #1B3A6B;">%s</p>
                                <p style="color: #e74c3c; font-size: 13px;">⚠️ Este enlace expirará en 24 horas.</p>
                            </div>
                            <div class="footer">
                                Si no creaste esta cuenta, puedes ignorar este correo.
                            </div>
                        </div>
                    </body>
                    </html>
                    """.formatted(nombre, enlace, enlace);

            helper.setText(cuerpoHtml, true);
            mailSender.send(mensaje);
            log.info("Correo de verificacion enviado a: {}", destinatario);

        } catch (MessagingException e) {
            log.error("Error al enviar correo de verificacion a {}: {}", destinatario, e.getMessage());
        }
    }
}
