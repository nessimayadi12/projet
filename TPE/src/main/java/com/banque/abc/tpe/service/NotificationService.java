package com.banque.abc.tpe.service;

import com.banque.abc.tpe.entity.Affectation;
import com.banque.abc.tpe.entity.Demande;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void notifierNouvelleDemande(Demande demande) {
        // Email désactivé temporairement - Outlook nécessite OAuth2 ou mot de passe d'application
        log.info("Notification email (mock) - Nouvelle demande: {} pour commerçant: {}", 
            demande.getReference(), 
            demande.getCommercant() != null ? demande.getCommercant().getRaisonSociale() : demande.getRaisonSociale());
        return;
        
        /* Configuration email requise:
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("monetique@banque.com"); // À configurer
            message.setSubject("Nouvelle demande TPE - " + demande.getReference());
            
            // Le commerçant est maintenant toujours créé
            String commercantNom = demande.getCommercant() != null 
                ? demande.getCommercant().getRaisonSociale() 
                : demande.getRaisonSociale();
            
            message.setText(String.format(
                    "Une nouvelle demande TPE a été créée.\n\n" +
                    "Référence: %s\n" +
                    "Type: %s\n" +
                    "Commerçant: %s\n" +
                    "Demandeur: %s\n" +
                    "Urgence: %s\n\n" +
                    "Veuillez traiter cette demande.",
                    demande.getReference(),
                    demande.getTypeDemande(),
                    commercantNom,
                    demande.getDemandeur().getUsername(),
                    demande.getUrgence()
            ));
            
            mailSender.send(message);
            log.info("Email de notification envoyé pour la demande {}", demande.getReference());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de notification", e);
        }
        */
    }

    public void notifierDemandeValidee(Demande demande) {
        // Email désactivé temporairement
        log.info("Notification email (mock) - Demande validée: {}", demande.getReference());
        return;
        
        /* Configuration email requise:
        try {
            String emailDestinataire = demande.getDemandeur().getEmail();
            if (emailDestinataire == null || emailDestinataire.isEmpty()) {
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailDestinataire);
            message.setSubject("Demande TPE validée - " + demande.getReference());
            message.setText(String.format(
                    "Votre demande TPE a été validée.\n\n" +
                    "Référence: %s\n" +
                    "Commerçant: %s\n" +
                    "Validé par: %s\n" +
                    "Date: %s\n\n" +
                    "La demande sera traitée prochainement.",
                    demande.getReference(),
                    demande.getCommercant().getRaisonSociale(),
                    demande.getValideur().getUsername(),
                    demande.getDateValidation()
            ));
            
            mailSender.send(message);
            log.info("Email de validation envoyé pour la demande {}", demande.getReference());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de validation", e);
        }
        */
    }

    public void notifierDemandeRejetee(Demande demande) {
        // Email désactivé temporairement
        log.info("Notification email (mock) - Demande rejetée: {}", demande.getReference());
        return;
        
        /* Configuration email requise:
        try {
            String emailDestinataire = demande.getDemandeur().getEmail();
            if (emailDestinataire == null || emailDestinataire.isEmpty()) {
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailDestinataire);
            message.setSubject("Demande TPE rejetée - " + demande.getReference());
            message.setText(String.format(
                    "Votre demande TPE a été rejetée.\n\n" +
                    "Référence: %s\n" +
                    "Commerçant: %s\n" +
                    "Rejeté par: %s\n" +
                    "Motif: %s\n\n" +
                    "Veuillez contacter le service Monétique pour plus d'informations.",
                    demande.getReference(),
                    demande.getCommercant().getRaisonSociale(),
                    demande.getValideur().getUsername(),
                    demande.getCommentaireValidation()
            ));
            
            mailSender.send(message);
            log.info("Email de rejet envoyé pour la demande {}", demande.getReference());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de rejet", e);
        }
        */
    }

    public void notifierAffectationTPE(Affectation affectation) {
        // Email désactivé temporairement
        log.info("Notification email (mock) - TPE affecté: {} au commerçant {}", 
                affectation.getTpe().getNumeroTerminal(), 
                affectation.getCommercant().getRaisonSociale());
        return;
        
        /* Configuration email requise:
        try {
            String emailDestinataire = affectation.getCommercant().getEmail();
            if (emailDestinataire == null || emailDestinataire.isEmpty()) {
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailDestinataire);
            message.setSubject("TPE affecté - " + affectation.getDemande().getReference());
            message.setText(String.format(
                    "Un TPE a été affecté à votre établissement.\n\n" +
                    "Numéro Terminal: %s\n" +
                    "Numéro de Série: %s\n" +
                    "Date d'affectation: %s\n" +
                    "Demande: %s\n\n" +
                    "Vous serez contacté prochainement pour la mise en service.",
                    affectation.getTpe().getNumeroTerminal(),
                    affectation.getTpe().getNumeroSerie(),
                    affectation.getDateAffectation(),
                    affectation.getDemande().getReference()
            ));
            
            mailSender.send(message);
            log.info("Email d'affectation envoyé pour le TPE {}", affectation.getTpe().getNumeroTerminal());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email d'affectation", e);
        }
        */
    }
}
