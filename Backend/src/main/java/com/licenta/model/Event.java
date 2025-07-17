package com.licenta.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;

    @Column(name = "location_name")
    private String locationName;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private LocalDateTime startDate;
    private LocalDateTime endDate;


    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private Boolean isActive = true;

    private Integer maxVolunteers;

    @Column(name = "reminder_sent")
    private Boolean reminderSent = false;

    @Column(name = "certificates_generated")
    private Boolean certificatesGenerated = false;

    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

}
