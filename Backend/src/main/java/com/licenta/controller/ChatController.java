package com.licenta.controller;

import com.licenta.DTO.ChatMessageDTO;
import com.licenta.model.ChatMessage;
import com.licenta.model.Event;
import com.licenta.repository.ChatMessageRepository;
import com.licenta.repository.EventRepository;
import com.licenta.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;


/**
 * Controller pentru procesarea mesajelor WebSocket în cadrul chatului de eveniment.
 */
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    /**
     * Primește și procesează un mesaj trimis prin WebSocket pentru un eveniment.
     *
     * @param eventId ID-ul evenimentului
     * @param message mesajul primit
     */
    @MessageMapping("/chat/{eventId}")
    public void processMessage(
            @DestinationVariable Long eventId,
            @Payload ChatMessage message) {

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null || Boolean.FALSE.equals(event.getIsActive())) {
            return;
        }

        message.setTimestamp(LocalDateTime.now());
        message.setEventId(eventId);
        chatMessageRepository.save(message);

        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setSenderId(message.getSenderId());

        userRepository.findById(message.getSenderId()).ifPresent(user -> {
            dto.setSenderName(user.getFirstName() + " " + user.getLastName());
            dto.setSenderProfilePicture(user.getProfilePicture());
        });

        dto.setMessage(message.getMessage());
        dto.setTimestamp(message.getTimestamp());

        messagingTemplate.convertAndSend("/topic/chat/" + eventId, dto);
    }
}