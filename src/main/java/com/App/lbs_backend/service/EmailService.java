package com.App.lbs_backend.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.from:LBS Education <noreply@lbs.edu>}")
    private String fromAddress;

    @Value("${app.portal.url:http://localhost:4200/portail/dashboard}")
    private String portalUrl;

    @Async
    public void sendDossierAccepte(String toEmail, String tuteurNom, String tuteurPrenom,
                                   String eleveNom, String elevePrenom,
                                   String classe, String anneeScolaire, String numeroDossier) {
        Context ctx = new Context();
        ctx.setVariables(Map.of(
            "tuteurNom",      tuteurNom,
            "tuteurPrenom",   tuteurPrenom,
            "eleveNom",       eleveNom,
            "elevePrenom",    elevePrenom,
            "classe",         classe,
            "anneeScolaire",  anneeScolaire,
            "numeroDossier",  numeroDossier,
            "portalUrl",      portalUrl
        ));
        send(toEmail,
             "✅ Dossier d'inscription accepté — LBS Education",
             "email/dossier-accepte",
             ctx);
    }

    @Async
    public void sendDossierRefuse(String toEmail, String tuteurNom, String tuteurPrenom,
                                  String eleveNom, String elevePrenom,
                                  String classe, String anneeScolaire,
                                  String numeroDossier, String motif) {
        Context ctx = new Context();
        ctx.setVariables(Map.of(
            "tuteurNom",      tuteurNom,
            "tuteurPrenom",   tuteurPrenom,
            "eleveNom",       eleveNom,
            "elevePrenom",    elevePrenom,
            "classe",         classe,
            "anneeScolaire",  anneeScolaire,
            "numeroDossier",  numeroDossier,
            "motif",          motif != null ? motif : "",
            "portalUrl",      portalUrl
        ));
        send(toEmail,
             "❌ Dossier d'inscription refusé — LBS Education",
             "email/dossier-refuse",
             ctx);
    }

    private void send(String to, String subject, String template, Context ctx) {
        try {
            String html = templateEngine.process(template, ctx);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email envoyé à {} — {}", to, subject);
        } catch (Exception e) {
            log.error("Erreur envoi email à {} : {}", to, e.getMessage());
        }
    }
}
