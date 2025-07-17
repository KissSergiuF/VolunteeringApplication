package com.licenta.service;

import com.licenta.model.Event;
import com.licenta.model.User;
import com.licenta.repository.EventRepository;
import com.licenta.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service care trimite emailuri de remindere pentru evenimentele ce încep în următoarele 24 de ore.
 */
@Service
@RequiredArgsConstructor
public class EventReminderService {

    private final EventRepository eventRepository;
    private final EventRegistrationService registrationService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd 'la ora' HH:mm");

    /**
     * Execută la fiecare 5 minute. Trimite remindere utilizatorilor înregistrați la evenimente
     * ce încep în următoarele 24 de ore, dacă nu au primit deja unul.
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void sendEventReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in24h = now.plusHours(24);

        List<Event> events = eventRepository
                .findByStartDateBeforeAndReminderSentFalse(in24h)
                .stream()
                .filter(e -> e.getStartDate().isAfter(now))
                .toList();

        for (Event event : events) {
            List<Long> userIds = registrationService.getUserIdsByEventId(event.getId());

            for (Long userId : userIds) {
                userRepository.findById(userId).ifPresent(user -> {
                    String subject = "Reminder: \"" + event.getName() + "\" începe în curând!";
                    String body = String.format(
                            "Bună, %s %s!\n\nEvenimentul \"%s\" va începe în mai puțin de 24 de ore.\nLocație: %s\nData: %s\n\nEchipa",
                            user.getFirstName(),
                            user.getLastName(),
                            event.getName(),
                            event.getLocationName(),
                            event.getStartDate().format(formatter)
                    );
                    emailService.sendSimpleEmail(user.getEmail(), subject, body);
                });
            }

            event.setReminderSent(true);
            eventRepository.save(event);
        }
    }
}

