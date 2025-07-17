package com.licenta.controller;

import com.licenta.DTO.UserDTO;
import com.licenta.model.Event;
import com.licenta.model.User;
import com.licenta.repository.EventRegistrationRepository;
import com.licenta.repository.UserRepository;
import com.licenta.repository.EventRepository;
import com.licenta.service.EventRegistrationService;
import com.licenta.service.EventWaitlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller pentru gestionarea înregistrărilor și listelor de așteptare ale evenimentelor.
 */
@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class EventRegistrationController {

    private final EventRegistrationService registrationService;
    private final EventRegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final EventWaitlistService waitlistService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Înregistrează un utilizator la un eveniment.
     */
    @PostMapping
    public ResponseEntity<?> register(@RequestParam Long eventId, @RequestParam Long userId) {
        try {
            registrationService.registerUserToEvent(eventId, userId);
            return ResponseEntity.ok(Map.of("message", "Utilizatorul s-a inregistrat cu succes."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Returnează ID-urile evenimentelor la care un utilizator este înregistrat.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Long>> getRegisteredEventIds(@PathVariable Long userId) {
        List<Long> eventIds = registrationRepository.findEventIdsByUserId(userId);
        return ResponseEntity.ok(eventIds);
    }

    /**
     * Dezînregistrează un utilizator de la un eveniment.
     */
    @DeleteMapping
    public ResponseEntity<?> unregister(@RequestParam Long eventId, @RequestParam Long userId) {
        try {
            registrationService.unregisterUserFromEvent(eventId, userId);
            return ResponseEntity.ok(Map.of("message", "Utilizatorul s-a dezabonat cu succes."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Returnează lista participanților (doar cei înscriși) la un eveniment.
     */
    @GetMapping("/{eventId}/participants")
    public ResponseEntity<?> getParticipantsForEvent(@PathVariable Long eventId) {
        List<Long> userIds = registrationService.getUserIdsByEventId(eventId);
        List<User> users = userRepository.findAllById(userIds);
        List<UserDTO> userDTOs = users.stream().map(UserDTO::new).toList();
        return ResponseEntity.ok(userDTOs);
    }

    /**
     * Returnează toți membrii evenimentului: participanți + organizator.
     */
    @GetMapping("/{eventId}/all-members")
    public ResponseEntity<?> getAllMembersForEvent(@PathVariable Long eventId) {
        try {
            List<User> allMembers = new ArrayList<>();
            List<Long> participantIds = registrationService.getUserIdsByEventId(eventId);
            List<User> participants = userRepository.findAllById(participantIds);
            allMembers.addAll(participants);

            Optional<Event> eventOpt = eventRepository.findById(eventId);
            if (eventOpt.isPresent()) {
                Long organizerId = eventOpt.get().getOrganizer().getId();
                Optional<User> organizerOpt = userRepository.findById(organizerId);
                organizerOpt.ifPresent(organizer -> {
                    boolean organizerAlreadyInList = allMembers.stream()
                            .anyMatch(user -> user.getId().equals(organizerId));
                    if (!organizerAlreadyInList) {
                        allMembers.add(organizer);
                    }
                });
            }

            List<UserDTO> memberDTOs = allMembers.stream().map(UserDTO::new).toList();
            return ResponseEntity.ok(memberDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Eroare la obtinerea membrilor evenimentului: " + e.getMessage()));
        }
    }

    /**
     * Abonează un utilizator la lista de așteptare a unui eveniment.
     */
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribeToEventWaitlist(@RequestParam Long eventId, @RequestParam Long userId) {
        try {
            boolean alreadySubscribed = waitlistService.isSubscribed(userId, eventId);
            if (alreadySubscribed) {
                return ResponseEntity.badRequest().body(Map.of("message", "Esti deja abonat la acest eveniment."));
            }

            waitlistService.subscribe(userId, eventId);
            return ResponseEntity.ok(Map.of("message", "Te-ai abonat cu succes. Vei fi notificat cand se elibereaza un loc."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Eroare la abonare: " + e.getMessage()));
        }
    }

    /**
     * Dezabonează un utilizator de la lista de așteptare a unui eveniment.
     */
    @DeleteMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribeFromWaitlist(@RequestParam Long eventId, @RequestParam Long userId) {
        try {
            waitlistService.unsubscribe(userId, eventId);
            return ResponseEntity.ok(Map.of("message", "Dezabonare realizata cu succes."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Eroare la dezabonare: " + e.getMessage()));
        }
    }

    /**
     * Returnează ID-urile evenimentelor la care utilizatorul este abonat (waitlist).
     */
    @GetMapping("/subscribed/{userId}")
    public ResponseEntity<List<Long>> getSubscribedEventIds(@PathVariable Long userId) {
        try {
            List<Long> eventIds = waitlistService.getSubscribedEventIds(userId);
            return ResponseEntity.ok(eventIds);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
