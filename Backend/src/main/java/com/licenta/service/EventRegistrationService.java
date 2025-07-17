package com.licenta.service;

import com.licenta.model.Event;
import com.licenta.model.EventRegistration;
import com.licenta.repository.EventRegistrationRepository;
import com.licenta.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Service pentru gestionarea înscrierilor utilizatorilor la evenimente.
 */
@Service
@RequiredArgsConstructor
public class EventRegistrationService {

    private final EventRegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final EventWaitlistService waitlistService;

    /**
     * Înscrie un utilizator la un eveniment.
     */
    public void registerUserToEvent(Long eventId, Long userId) {
        boolean exists = registrationRepository.existsByEventIdAndUserId(eventId, userId);
        if (exists) {
            throw new RuntimeException("Utilizatorul este deja inscris la acest eveniment.");
        }

        int registeredCount = registrationRepository.countByEventId(eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evenimentul nu a fost gasit."));

        if (event.getMaxVolunteers() != null &&
                event.getMaxVolunteers() > 0 &&
                registeredCount >= event.getMaxVolunteers()) {
            throw new RuntimeException("Numarul maxim de voluntari a fost atins pentru acest eveniment.");
        }

        EventRegistration registration = new EventRegistration();
        registration.setEventId(eventId);
        registration.setUserId(userId);
        registration.setRegistrationDate(OffsetDateTime.now());

        registrationRepository.save(registration);
        waitlistService.unsubscribe(userId, eventId);
    }

    /**
     * Dezînscrie un utilizator de la un eveniment.
     */
    public void unregisterUserFromEvent(Long eventId, Long userId) {
        boolean exists = registrationRepository.existsByEventIdAndUserId(eventId, userId);
        System.out.println("Exista? " + exists);

        if (!exists) {
            System.out.println("Inregistrarea nu exista!");
            throw new RuntimeException("Utilizatorul nu este inscris la acest eveniment.");
        }

        try {
            registrationRepository.deleteByEventIdAndUserId(eventId, userId);
            System.out.println("Deregistrare realizata cu succes.");
            waitlistService.checkAndNotifyWaitlist(eventId);
        } catch (Exception e){
            throw new RuntimeException("Eroare in timpul deregistrarii: " + e.getMessage());
        }
    }

    /**
     * Returnează ID-urile utilizatorilor înscriși la un eveniment.
     */
    public List<Long> getUserIdsByEventId(Long eventId) {
        return registrationRepository.findUserIdsByEventId(eventId);
    }
}

