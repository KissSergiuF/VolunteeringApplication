package com.licenta.controller;

import com.licenta.DTO.ChatMessageDTO;
import com.licenta.model.ChatMessage;
import com.licenta.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.licenta.model.User;
import com.licenta.repository.UserRepository;
import java.util.stream.Collectors;

import java.util.List;

/**
 * Controller pentru gestionarea mesajelor de chat.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ChatRestController {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    /**
     * Returnează lista de mesaje pentru un eveniment specific.
     *
     * @param eventId ID-ul evenimentului
     * @return lista de mesaje sub formă de DTO
     */
    @GetMapping("/{eventId}")
    public List<ChatMessageDTO> getMessagesByEvent(@PathVariable Long eventId) {
        List<ChatMessage> messages = chatMessageRepository.findByEventIdOrderByTimestampAsc(eventId);

        return messages.stream().map(msg -> {
            User user = userRepository.findById(msg.getSenderId()).orElse(null);

            ChatMessageDTO dto = new ChatMessageDTO();
            dto.setSenderId(msg.getSenderId());
            dto.setSenderName(user != null ? user.getFirstName() + " " + user.getLastName() : "null null");
            dto.setSenderProfilePicture(user != null ? user.getProfilePicture() : null);
            dto.setMessage(msg.getMessage());
            dto.setTimestamp(msg.getTimestamp());

            return dto;
        }).collect(Collectors.toList());
    }
}