package com.licenta.repository;

import com.licenta.model.EventBan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository pentru gestionarea interdicțiilor utilizatorilor la evenimente.
 * Permite verificarea, căutarea și eliminarea banărilor pe evenimente.
 */
public interface EventBanRepository extends JpaRepository<EventBan, Long> {

    /**
     * Verifică dacă un utilizator este banat de la un eveniment.
     */
    boolean existsByEventIdAndUserId(Integer eventId, Integer userId);

    /**
     * Returnează înregistrarea de tip ban pentru un utilizator la un eveniment, dacă există.
     */
    Optional<EventBan> findByEventIdAndUserId(Integer eventId, Integer userId);

    /**
     * Elimină banarea unui utilizator de la un eveniment.
     */
    void deleteByEventIdAndUserId(Integer eventId, Integer userId);
}