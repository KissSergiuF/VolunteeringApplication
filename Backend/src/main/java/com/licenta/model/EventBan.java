package com.licenta.model;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "event_bans", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventBan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Integer eventId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "banned_at")
    private ZonedDateTime bannedAt = ZonedDateTime.now();
}
