package com.licenta.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

/**
 * Service responsabil pentru trimiterea emailurilor din aplicație.
 * Include emailuri simple, notificări de slot liber și adeverințe PDF.
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${spring.mail.recipient}")
    private String toEmail;

    /**
     * Trimite un email de contact cu nume, email, telefon și mesaj.
     */
    public void sendEmail(String name, String email, String phone, String message) {
        SimpleMailMessage emailContent = new SimpleMailMessage();
        emailContent.setTo(toEmail);
        emailContent.setSubject("Contact din partea " + name);
        emailContent.setText("Nume: " + name + "\n" +
                "Email: " + email + "\n" +
                "Telefon: " + phone + "\n" +
                "Mesaj: " + message);
        emailContent.setFrom(from);
        mailSender.send(emailContent);
    }

    /**
     * Trimite un email simplu cu destinatar, subiect și text.
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom(from);
        mailSender.send(message);
    }

    /**
     * Trimite un email de notificare când se eliberează un loc la un eveniment.
     */
    public void sendSlotAvailableEmail(String to, String eventName) {
        String subject = "Loc disponibil la evenimentul \"" + eventName + "\"";
        String text = "Salut!\n\n" +
                "S-a eliberat un loc la evenimentul \"" + eventName + "\" la care te-ai abonat.\n" +
                "Daca doresti sa participi, acceseaza platforma cat mai curand pentru a te inregistra.\n\n" +
                "Succes!\nEchipa VoluntariApp";
        sendSimpleEmail(to, subject, text);
    }

    /**
     * Trimite un email cu o adeverință PDF atașată.
     */
    public void sendCertificateEmail(String toEmail, byte[] pdfContent, String fileName) {
        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setTo(toEmail);
            messageHelper.setSubject("Adeverinta de voluntariat");
            messageHelper.setText("Buna,\n\nGasesti atasata adeverinta de voluntariat.\n\nToate cele bune,\nVoluntariApp");
            messageHelper.setFrom(from);

            messageHelper.addAttachment(fileName, new ByteArrayResource(pdfContent));
        };

        mailSender.send(preparator);
    }
}

