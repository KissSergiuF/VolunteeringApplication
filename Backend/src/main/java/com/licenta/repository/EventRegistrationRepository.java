package com.licenta.repository;

import com.licenta.model.Event;
import com.licenta.model.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository pentru gestionarea înregistrărilor utilizatorilor la evenimente.
 * Include metode personalizate pentru verificare, ștergere și listare înregistrări.
 */
@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    /**
     * Șterge înregistrarea unui utilizator de la un eveniment.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM EventRegistration e WHERE e.eventId = :eventId AND e.userId = :userId")
    void deleteByEventIdAndUserId(@Param("eventId") Long eventId, @Param("userId") Long userId);

    /**
     * Returnează ID-urile evenimentelor la care este înscris un utilizator.
     */
    @Query("SELECT e.eventId FROM EventRegistration e WHERE e.userId = :userId")
    List<Long> findEventIdsByUserId(@Param("userId") Long userId);

    /**
     * Returnează lista de obiecte Event la care este înscris un utilizator.
     */
    @Query("SELECT e FROM Event e WHERE e.id IN (SELECT er.eventId FROM EventRegistration er WHERE er.userId = :userId)")
    List<Event> findEventsByUserId(@Param("userId") Long userId);

    /**
     * Verifică dacă un utilizator este deja înscris la un eveniment.
     */
    @Query("SELECT COUNT(er) > 0 FROM EventRegistration er WHERE er.eventId = :eventId AND er.userId = :userId")
    boolean existsByEventIdAndUserId(@Param("eventId") Long eventId, @Param("userId") Long userId);

    /**
     * Șterge toate înregistrările asociate unui eveniment.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM EventRegistration e WHERE e.eventId = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);

    /**
     * Returnează ID-urile utilizatorilor înscriși la un eveniment.
     */
    @Query("SELECT er.userId FROM EventRegistration er WHERE er.eventId = :eventId")
    List<Long> findUserIdsByEventId(@Param("eventId") Long eventId);

    /**
     * Returnează numărul de utilizatori înscriși la un eveniment.
     */
    int countByEventId(Long eventId);
}
