package com.licenta.service;

import com.licenta.DTO.EventFeedbackDTO;
import com.licenta.model.Event;
import com.licenta.model.EventFeedback;
import com.licenta.model.User;
import com.licenta.repository.EventFeedbackRepository;
import com.licenta.repository.EventRepository;
import com.licenta.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service pentru gestionarea feedback-ului evenimentelor între utilizatori.
 */
@Service
@RequiredArgsConstructor
public class EventFeedbackService {

    private final EventFeedbackRepository feedbackRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    /**
     * Trimite feedback de la un utilizator către organizatorul evenimentului.
     */
    public void submitFeedback(EventFeedbackDTO dto) {
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new RuntimeException("Evenimentul nu a fost gasit"));

        if (Boolean.TRUE.equals(event.getIsActive())) {
            throw new RuntimeException("Evenimentul este inca activ");
        }

        User fromUser = userRepository.findById(dto.getFromUserId())
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost gasit"));

        User toUser = event.getOrganizer();

        boolean alreadyGiven = feedbackRepository
                .findByEvent_IdAndFromUser_IdAndToUser_Id(dto.getEventId(), fromUser.getId(), toUser.getId())
                .isPresent();

        if (alreadyGiven) {
            throw new RuntimeException("Feedback-ul a fost deja trimis");
        }

        EventFeedback feedback = new EventFeedback();
        feedback.setEvent(event);
        feedback.setFromUser(fromUser);
        feedback.setToUser(toUser);
        feedback.setRating(dto.getRating());
        feedback.setComment(dto.getComment());
        feedback.setCreatedAt(LocalDateTime.now());

        feedbackRepository.save(feedback);
    }

    /**
     * Trimite feedback de la organizator către un participant.
     */
    public void submitFeedbackToParticipant(EventFeedbackDTO dto, Long participantId) {
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new RuntimeException("Evenimentul nu a fost gasit"));

        if (Boolean.TRUE.equals(event.getIsActive())) {
            throw new RuntimeException("Evenimentul este inca activ");
        }

        User fromUser = userRepository.findById(dto.getFromUserId())
                .orElseThrow(() -> new RuntimeException("Organizatorul nu a fost gasit"));

        User toUser = userRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participantul nu a fost gasit"));

        if (feedbackRepository.findByEvent_IdAndFromUser_IdAndToUser_Id(dto.getEventId(), fromUser.getId(), toUser.getId()).isPresent()) {
            throw new RuntimeException("Feedback-ul a fost deja trimis acestui participant");
        }

        EventFeedback feedback = new EventFeedback();
        feedback.setEvent(event);
        feedback.setFromUser(fromUser);
        feedback.setToUser(toUser);
        feedback.setRating(dto.getRating());
        feedback.setComment(dto.getComment());
        feedback.setCreatedAt(LocalDateTime.now());

        feedbackRepository.save(feedback);
    }

    /**
     * Returnează lista de feedback-uri primite de un utilizator.
     */
    public List<EventFeedbackDTO> getFeedbackForUser(Long userId) {
        return feedbackRepository.findByToUser_Id(userId)
                .stream()
                .map(f -> {
                    EventFeedbackDTO dto = new EventFeedbackDTO();
                    dto.setEventId(f.getEvent().getId());
                    dto.setFromUserId(f.getFromUser().getId());
                    dto.setRating(f.getRating());
                    dto.setComment(f.getComment());
                    dto.setFromUserFullName(f.getFromUser().getFirstName() + " " + f.getFromUser().getLastName());
                    dto.setFromUserProfilePicture(f.getFromUser().getProfilePicture());
                    dto.setEventName(f.getEvent().getName());
                    return dto;
                })
                .toList();
    }

    /**
     * Returnează media ratingurilor primite de un utilizator.
     */
    public Double getAverageFeedbackForUser(Long userId) {
        return feedbackRepository.getAverageRatingForUser(userId);
    }

    /**
     * Returnează numărul total de feedback-uri primite de un utilizator.
     */
    public long getFeedbackCountForUser(Long userId) {
        return feedbackRepository.countByToUser_Id(userId);
    }

    /**
     * Returnează toate feedback-urile trimise de un organizator.
     */
    public List<EventFeedbackDTO> getFeedbackFromOrganizer(Long organizerId) {
        return feedbackRepository.findByFromUser_Id(organizerId)
                .stream()
                .map(f -> {
                    EventFeedbackDTO dto = new EventFeedbackDTO();
                    dto.setEventId(f.getEvent().getId());
                    dto.setFromUserId(f.getFromUser().getId());
                    dto.setToUserId(f.getToUser().getId());
                    dto.setRating(f.getRating());
                    dto.setComment(f.getComment());
                    return dto;
                })
                .toList();
    }
}

