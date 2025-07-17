package com.licenta.controller;

import com.licenta.DTO.EventFeedbackDTO;
import com.licenta.service.EventFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.Map;

/**
 * Controller pentru gestionarea feedback-ului:

 */
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class EventFeedbackController {

    private final EventFeedbackService feedbackService;

    /**
     * Trimite feedback general despre un utilizator
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> submitFeedback(@RequestBody EventFeedbackDTO dto) {
        try {
            feedbackService.submitFeedback(dto);
            return ResponseEntity.ok(Map.of("message", "Feedback trimis"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Organizatorul trimite feedback către un participant
     */
    @PostMapping("/participant/{participantId}")
    public ResponseEntity<?> submitFeedbackToParticipant(
            @PathVariable Long participantId,
            @RequestBody EventFeedbackDTO dto) {
        try {
            feedbackService.submitFeedbackToParticipant(dto, participantId);
            return ResponseEntity.ok(Map.of("message", "Feedback trimis"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Returnează lista de feedbackuri primite de un utilizator
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EventFeedbackDTO>> getFeedbackForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(feedbackService.getFeedbackForUser(userId));
    }

    /**
     * Returnează media ratingurilor primite de utilizator
     */
    @GetMapping("/user/{userId}/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long userId) {
        return ResponseEntity.ok(feedbackService.getAverageFeedbackForUser(userId));
    }

    /**
     * Returnează numărul total de feedbackuri primite de utilizator
     */
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> getFeedbackCount(@PathVariable Long userId) {
        return ResponseEntity.ok(feedbackService.getFeedbackCountForUser(userId));
    }

    /**
     * Returnează feedbackurile lăsate de organizator altor utilizatori
     */
    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<List<EventFeedbackDTO>> getFeedbackFromOrganizer(@PathVariable Long organizerId) {
        return ResponseEntity.ok(feedbackService.getFeedbackFromOrganizer(organizerId));
    }

}