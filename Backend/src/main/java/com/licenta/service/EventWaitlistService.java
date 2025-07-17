package com.licenta.service;

import com.licenta.model.Event;
import com.licenta.model.EventWaitlist;
import com.licenta.model.User;
import com.licenta.repository.EventRepository;
import com.licenta.repository.EventWaitlistRepository;
import com.licenta.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service care gestionează logica pentru lista de așteptare a evenimentelor.
 * Permite abonarea/dezabonarea utilizatorilor, verificarea statutului și notificarea prin email
 * atunci când se eliberează locuri la un eveniment.
 */
@Service
@RequiredArgsConstructor
public class EventWaitlistService {

    private final EventWaitlistRepository waitlistRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EmailService emailService;

    /**
     * Verifică dacă un utilizator este deja abonat la lista de așteptare a unui eveniment.
     *
     * @param userId  ID-ul utilizatorului
     * @param eventId ID-ul evenimentului
     * @return true dacă utilizatorul este pe lista de așteptare, altfel false
     */
    public boolean isSubscribed(Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow();
        Event event = eventRepository.findById(eventId).orElseThrow();
        return waitlistRepository.existsByEventAndUser(event, user);
    }

    /**
     * Abonează un utilizator la lista de așteptare a unui eveniment, dacă nu este deja abonat.
     *
     * @param userId  ID-ul utilizatorului
     * @param eventId ID-ul evenimentului
     */
    public void subscribe(Long userId, Long eventId) {
        if (isSubscribed(userId, eventId)) {
            return;
        }

        User user = userRepository.findById(userId).orElseThrow();
        Event event = eventRepository.findById(eventId).orElseThrow();

        EventWaitlist waitlist = EventWaitlist.builder()
                .event(event)
                .user(user)
                .subscribedAt(ZonedDateTime.now())
                .build();

        waitlistRepository.save(waitlist);
    }

    /**
     * Caută primul utilizator de pe lista de așteptare pentru un eveniment și, dacă nu a fost deja notificat,
     * îi trimite un email că s-a eliberat un loc. Marcheză utilizatorul ca notificat.
     *
     * @param eventId ID-ul evenimentului
     */
    public void checkAndNotifyWaitlist(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow();

        Optional<EventWaitlist> optional = waitlistRepository
                .findFirstByEventOrderBySubscribedAtAsc(event);

        optional.ifPresent(waitlist -> {
            if (!waitlist.getNotified()) {
                String email = waitlist.getUser().getEmail();
                emailService.sendSlotAvailableEmail(email, event.getName());

                waitlist.setNotified(true);
                waitlistRepository.save(waitlist);
            }
        });
    }

    /**
     * Dezabonează un utilizator de la lista de așteptare a unui eveniment, dacă este prezent.
     *
     * @param userId  ID-ul utilizatorului
     * @param eventId ID-ul evenimentului
     */
    public void unsubscribe(Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow();
        Event event = eventRepository.findById(eventId).orElseThrow();
        Optional<EventWaitlist> waitlist = waitlistRepository.findByEventAndUser(event, user);
        waitlist.ifPresent(waitlistRepository::delete);
    }

    /**
     * Returnează o listă de ID-uri ale evenimentelor pentru care un utilizator este abonat pe lista de așteptare.
     *
     * @param userId ID-ul utilizatorului
     * @return listă de ID-uri de evenimente
     */
    public List<Long> getSubscribedEventIds(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<EventWaitlist> waitlistEntries = waitlistRepository.findAllByUser(user);
        return waitlistEntries.stream()
                .map(entry -> entry.getEvent().getId())
                .toList();
    }

    /**
     * Șterge toate intrările din lista de așteptare pentru un eveniment specific.
     * Util în cazul arhivării sau anulării evenimentului.
     *
     * @param eventId ID-ul evenimentului
     */
    public void clearWaitlistForEvent(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        waitlistRepository.deleteAllByEvent(event);
    }

}
