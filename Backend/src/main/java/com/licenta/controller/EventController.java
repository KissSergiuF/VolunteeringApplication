package com.licenta.controller;

import com.licenta.DTO.EventDTO;
import com.licenta.DTO.CertificateRequestDTO;
import com.licenta.service.EventService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

/**
 * Controller pentru gestionarea evenimentelor.
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class EventController {

    private final EventService eventService;

    /**
     * Creează un eveniment (doar pentru utilizatori cu rol ASSOCIATION).
     */
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody EventDTO eventDto, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId == null || role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilizator nelogat");
        }
        if (!role.equals("ASSOCIATION")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Doar asociatiile pot crea evenimente");
        }
        EventDTO createdEvent = eventService.createEvent(eventDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    /**
     * Returnează toate evenimentele.
     */
    @GetMapping
    public ResponseEntity<List<EventDTO>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    /**
     * Returnează evenimentele la care este înregistrat utilizatorul.
     */
    @GetMapping("/registered/{userId}")
    public ResponseEntity<List<EventDTO>> getRegisteredEvents(@PathVariable Long userId) {
        return ResponseEntity.ok(eventService.getEventsForRegisteredUser(userId));
    }

    /**
     * Returnează evenimentele create de asociație.
     */
    @GetMapping("/created-by/{userId}")
    public ResponseEntity<List<EventDTO>> getEventsCreatedByAssociation(@PathVariable Long userId) {
        return ResponseEntity.ok(eventService.getEventsCreatedByAssociation(userId));
    }

    /**
     * Returnează detalii despre un eveniment.
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEventById(eventId));
    }

    /**
     * Șterge un eveniment (doar pentru ASSOCIATION).
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long eventId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId == null || role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilizator nelogat");
        }
        if (!role.equals("ASSOCIATION")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Doar asociatiile pot sterge evenimente");
        }
        try {
            eventService.deleteEvent(eventId, userId);
            return ResponseEntity.ok(Map.of("message", "Evenimentul a fost sters cu succes."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Returnează evenimentele arhivate la care utilizatorul a participat.
     */
    @GetMapping("/archived/registered/{userId}")
    public ResponseEntity<List<EventDTO>> getArchivedRegisteredEvents(@PathVariable Long userId) {
        return ResponseEntity.ok(eventService.getArchivedRegisteredEvents(userId));
    }

    /**
     * Returnează evenimentele arhivate create de asociație.
     */
    @GetMapping("/archived/created-by/{userId}")
    public ResponseEntity<List<EventDTO>> getArchivedCreatedEvents(@PathVariable Long userId) {
        return ResponseEntity.ok(eventService.getArchivedCreatedEvents(userId));
    }

    /**
     * Elimină un utilizator dintr-un eveniment (doar pentru ASSOCIATION).
     */
    @DeleteMapping("/kick/{eventId}/{userId}")
    public ResponseEntity<?> kickUser(@PathVariable Long eventId, @PathVariable Long userId, HttpSession session) {
        Long organizerId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (organizerId == null || role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilizator nelogat");
        }
        if (!role.equals("ASSOCIATION")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Doar asociatiile pot elimina utilizatori");
        }
        try {
            eventService.kickUser(eventId, userId, organizerId);
            return ResponseEntity.ok(Map.of("message", "Utilizatorul a fost eliminat cu succes."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Verifică dacă utilizatorul este banat din eveniment.
     */
    @GetMapping("/{eventId}/ban-status")
    public ResponseEntity<?> checkBanStatus(@PathVariable Long eventId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilizator nelogat");
        }
        boolean isBanned = eventService.isUserBannedFromEvent(eventId, userId);
        return ResponseEntity.ok(Map.of("banned", isBanned));
    }

    /**
     * Generează adeverințe pentru participanți (doar pentru ASSOCIATION).
     */
    @PostMapping("/{eventId}/generate-certificates")
    public ResponseEntity<?> generateCertificates(
            @PathVariable Long eventId,
            @RequestBody CertificateRequestDTO request,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId == null || role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilizator nelogat");
        }
        if (!role.equals("ASSOCIATION")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Doar asociatiile pot genera adeverinte");
        }
        try {
            eventService.generateCertificates(eventId, request, userId);
            return ResponseEntity.ok(Map.of("message", "Adeverintele au fost generate si trimise cu succes."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Încarcă o ștampilă pentru adeverințe (doar pentru ASSOCIATION).
     */
    @PostMapping("/{eventId}/upload-stamp")
    public ResponseEntity<?> uploadStamp(
            @PathVariable Long eventId,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId == null || role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilizator nelogat");
        }
        if (!role.equals("ASSOCIATION")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Doar asociatiile pot incarca stampile");
        }
        try {
            eventService.saveStampTemp(eventId, file);
            return ResponseEntity.ok(Map.of("message", "Stampila a fost incarcata cu succes."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}