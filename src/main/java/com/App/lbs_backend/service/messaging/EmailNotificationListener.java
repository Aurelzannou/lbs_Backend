package com.App.lbs_backend.service.messaging;

import com.App.lbs_backend.config.RabbitMQConfig;
import com.App.lbs_backend.dto.message.DossierNotificationMessage;
import com.App.lbs_backend.dto.message.ProfesseurActivationMessage;
import com.App.lbs_backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handle(DossierNotificationMessage message) {
        log.info("Traitement notification email dossier {} (statut={})", message.numeroDossier(), message.type());

        if ("ACCEPTE".equals(message.type())) {
            emailService.sendDossierAccepte(
                    message.toEmail(), message.tuteurNom(), message.tuteurPrenom(),
                    message.eleveNom(), message.elevePrenom(),
                    message.classe(), message.anneeScolaire(), message.numeroDossier());
        } else {
            emailService.sendDossierRefuse(
                    message.toEmail(), message.tuteurNom(), message.tuteurPrenom(),
                    message.eleveNom(), message.elevePrenom(),
                    message.classe(), message.anneeScolaire(), message.numeroDossier(), message.motif());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.PROFESSEUR_ACTIVATION_QUEUE)
    public void handleProfesseurActivation(ProfesseurActivationMessage message) {
        log.info("Envoi du lien d'activation au professeur {}", message.toEmail());
        emailService.sendProfesseurActivation(
                message.toEmail(), message.nom(), message.prenom(), message.lienActivation());
    }
}
