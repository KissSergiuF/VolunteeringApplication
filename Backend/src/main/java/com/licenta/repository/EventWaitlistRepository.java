package com.licenta.repository;

import com.licenta.model.EventWaitlist;
import com.licenta.model.Event;
import com.licenta.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository pentru gestionarea listei de așteptare la evenimente.
 * Permite verificări, căutări și operații de ștergere în funcție de utilizator și eveniment.
 */
@Repository
public interface EventWaitlistRepository extends JpaRepository<EventWaitlist, Long> {

    /**
     * Verifică dacă un utilizator este deja pe lista de așteptare pentru un eveniment.
     */
    boolean existsByEventAndUser(Event event, User user);

    /**
     * Găsește primul utilizator abonat la un eveniment, ordonat după data abonării.
     */
    Optional<EventWaitlist> findFirstByEventOrderBySubscribedAtAsc(Event event);

    /**
     * Găsește o intrare din listă pentru un utilizator și un eveniment.
     */
    Optional<EventWaitlist> findByEventAndUser(Event event, User user);

    /**
     * Găsește toate intrările din lista de așteptare pentru un utilizator.
     */
    List<EventWaitlist> findAllByUser(User user);

    /**
     * Șterge toate intrările din lista de așteptare pentru un eveniment.
     */
    void deleteAllByEvent(Event event);
}
