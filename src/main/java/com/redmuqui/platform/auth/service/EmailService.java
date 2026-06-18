package com.redmuqui.platform.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    public void enviarRecuperacionContrasenha(String destinatario, String token) {
        String resetUrl = construirResetUrl(token);
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                message,
                true,
                StandardCharsets.UTF_8.name()
            );
            helper.setFrom(new InternetAddress(from, fromName, StandardCharsets.UTF_8.name()));
            helper.setTo(destinatario);
            helper.setSubject("Restablece tu contrasena - RedMuqui");
            helper.setText(construirTexto(resetUrl), construirHtml(resetUrl));

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException ex) {
            throw new IllegalStateException("No se pudo enviar el correo de recuperacion", ex);
        }
    }

    private String construirResetUrl(String token) {
        String baseUrl = frontendBaseUrl.endsWith("/")
            ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
            : frontendBaseUrl;
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        return baseUrl + "/reset-password?token=" + encodedToken;
    }

    private String construirTexto(String resetUrl) {
        return """
            Hola,

            Recibimos una solicitud para restablecer tu contrasena en RedMuqui.

            Usa este enlace para definir una nueva contrasena:
            %s

            Este enlace vence en 15 minutos. Si no solicitaste este cambio, ignora este mensaje.

            RedMuqui
            """.formatted(resetUrl);
    }

    private String construirHtml(String resetUrl) {
        return """
            <div style="font-family: Arial, sans-serif; color: #1A1A1A; line-height: 1.5;">
              <h2 style="margin: 0 0 12px;">Restablece tu contrasena</h2>
              <p>Recibimos una solicitud para restablecer tu contrasena en RedMuqui.</p>
              <p>
                <a href="%s" style="display: inline-block; padding: 10px 16px; background: #FFD600; color: #1A1A1A; text-decoration: none; border-radius: 8px; font-weight: 700;">
                  Crear nueva contrasena
                </a>
              </p>
              <p>Este enlace vence en 15 minutos. Si no solicitaste este cambio, ignora este mensaje.</p>
              <p style="font-size: 12px; color: #5C5C5C;">RedMuqui</p>
            </div>
            """.formatted(resetUrl);
    }
}
