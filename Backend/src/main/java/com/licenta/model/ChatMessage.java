package com.licenta.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Long senderId;


    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    private LocalDateTime timestamp;

    @Transient
    private String senderProfilePicture;
}
