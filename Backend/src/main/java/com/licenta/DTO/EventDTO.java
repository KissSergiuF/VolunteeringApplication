package com.licenta.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class EventDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String location;
    private BigDecimal lat;
    private BigDecimal lng;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long organizerId;
    private Boolean isActive;
    private Boolean isFull;
    private Integer maxVolunteers;
    private Integer currentVolunteers;
    private Boolean certificatesGenerated;


}
