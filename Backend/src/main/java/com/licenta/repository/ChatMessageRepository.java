package com.licenta.repository;

import com.licenta.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository pentru gestionarea mesajelor din chatul evenimentelor.
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Returnează toate mesajele unui eveniment, ordonate crescător după momentul trimiterii.
     *
     * @param eventId ID-ul evenimentului
     * @return lista de mesaje
     */
    List<ChatMessage> findByEventIdOrderByTimestampAsc(Long eventId);

    /**
     * Șterge toate mesajele trimise de un utilizator într-un anumit eveniment.
     *
     * @param eventId  ID-ul evenimentului
     * @param senderId ID-ul utilizatorului (expeditorului)
     */
    void deleteByEventIdAndSenderId(Long eventId, Long senderId);
}