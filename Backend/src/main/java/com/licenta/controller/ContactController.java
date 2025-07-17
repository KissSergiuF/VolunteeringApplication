package com.licenta.controller;

import com.licenta.DTO.ContactDTO;
import com.licenta.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller pentru trimiterea mesajelor din formularul de contact
 */
@RestController
@RequestMapping("/contact")
@CrossOrigin(origins = "*")
public class ContactController {

    @Autowired
    private EmailService emailService;

    /**
     * Trimite un email de contact pe baza datelor din formular
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> sendContactEmail(@RequestBody ContactDTO contactForm) {
        emailService.sendEmail(
                contactForm.getName(),
                contactForm.getEmail(),
                contactForm.getPhone(),
                contactForm.getMessage()
        );

        Map<String, String> response = new HashMap<>();
        response.put("message", "Email trimis cu succes");
        return ResponseEntity.ok(response);
    }
}