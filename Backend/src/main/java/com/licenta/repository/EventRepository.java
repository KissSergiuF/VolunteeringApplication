package com.licenta.repository;

import com.licenta.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Repository pentru entitatea Event.
 * Oferă metode pentru accesarea evenimentelor pe baza organizatorului sau a datei de start.
 */
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Returnează toate evenimentele organizate de un anumit utilizator.
     *
     * @param organizerId ID-ul organizatorului
     * @return lista de evenimente
     */
    List<Event> findByOrganizer_Id(Long organizerId);

    /**
     * Returnează toate evenimentele care încep înainte de un anumit moment
     * și care nu au avut încă reminder-ul trimis.
     *
     * @param time momentul de referință
     * @return lista de evenimente eligibile pentru reminder
     */
    List<Event> findByStartDateBeforeAndReminderSentFalse(LocalDateTime time);
}