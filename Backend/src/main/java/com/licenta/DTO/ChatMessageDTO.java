package com.licenta.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {

    private Long senderId;
    private String senderName;
    private String senderProfilePicture;
    private String message;
    private LocalDateTime timestamp;
}
