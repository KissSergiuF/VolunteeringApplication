package com.licenta.repository;

import com.licenta.model.EventFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository pentru gestionarea feedback-ului evenimentelor.
 * Permite salvarea, căutarea și analiza feedback-urilor între utilizatori.
 */
public interface EventFeedbackRepository extends JpaRepository<EventFeedback, Long> {

    /**
     * Caută feedback-ul trimis de un utilizator pentru un eveniment (indiferent de destinatar).
     */
    Optional<EventFeedback> findByEvent_IdAndFromUser_Id(Long eventId, Long fromUserId);

    /**
     * Caută feedback-ul trimis de un utilizator către alt utilizator pentru un eveniment.
     */
    Optional<EventFeedback> findByEvent_IdAndFromUser_IdAndToUser_Id(Long eventId, Long fromUserId, Long toUserId);

    /**
     * Returnează toate feedback-urile primite de un utilizator.
     */
    List<EventFeedback> findByToUser_Id(Long toUserId);

    /**
     * Returnează toate feedback-urile trimise de un utilizator.
     */
    List<EventFeedback> findByFromUser_Id(Long fromUserId);

    /**
     * Calculează media ratingurilor primite de un utilizator.
     */
    @Query("SELECT AVG(f.rating) FROM EventFeedback f WHERE f.toUser.id = :userId")
    Double getAverageRatingForUser(@Param("userId") Long userId);

    /**
     * Returnează numărul total de feedback-uri primite de un utilizator.
     */
    long countByToUser_Id(Long userId);
}