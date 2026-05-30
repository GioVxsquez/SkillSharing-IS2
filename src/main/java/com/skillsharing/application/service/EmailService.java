package com.skillsharing.application.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;

// servicio de envio de correos - hu29
// principio srp: solo arma y envia correos de verificacion
// smtp2go se usa porque render bloquea smtp directo de gmail
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${app.email.from:${spring.mail.username}}")
    private String fromAddress;

    @Value("${app.verification.token.expiry-ms:1800000}")
    private long tokenExpiryMs;

    // hu29: envia el enlace para activar la cuenta
    @Async
    public void enviarCorreoVerificacion(String destinatario, String nombre, String token) {
        try {
            if (mailUsername == null || mailUsername.isBlank()) {
                log.warn("correo de verificacion omitido porque no hay smtp configurado");
                return;
            }

            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            String enlace = baseUrl + "/api/auth/verificar?token=" + token;
            long minutos = Math.max(1, Duration.ofMillis(tokenExpiryMs).toMinutes());

            helper.setFrom(fromAddress);
            helper.setTo(destinatario);
            helper.setSubject("SkillSharing - Verifica tu cuenta");
            helper.setText(cuerpoHtml(nombre, enlace, minutos), true);

            mailSender.send(mensaje);
            log.info("correo de verificacion enviado a {}", destinatario);
        } catch (MessagingException | MailException e) {
            log.error("no se pudo enviar el correo de verificacion a {}: {}", destinatario, e.getMessage());
        }
    }

    private String cuerpoHtml(String nombre, String enlace, long minutos) {
        return """
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
                            <h2>Hola, %s</h2>
                            <p>Gracias por registrarte en <strong>SkillSharing</strong>. Estas a un solo clic de activar tu cuenta y comenzar a aprender.</p>
                            <p>Haz clic en el boton de abajo para verificar tu correo electronico:</p>
                            <a href="%s" class="btn">Verificar mi cuenta</a>
                            <p>Si no puedes hacer clic en el boton, copia y pega este enlace en tu navegador:</p>
                            <p style="word-break: break-all; color: #1B3A6B;">%s</p>
                            <p style="color: #e74c3c; font-size: 13px;">Este enlace expirara en %s minutos.</p>
                        </div>
                        <div class="footer">
                            Si no creaste esta cuenta, puedes ignorar este correo.
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(nombre, enlace, enlace, minutos);
    }
}
