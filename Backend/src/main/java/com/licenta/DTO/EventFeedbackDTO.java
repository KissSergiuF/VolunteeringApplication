package com.licenta.DTO;

import lombok.Data;

@Data
public class EventFeedbackDTO {
    private Long eventId;
    private Long fromUserId;
    private Long toUserId;
    private int rating;
    private String comment;
    private String fromUserFullName;
    private String fromUserProfilePicture;
    private String eventName;
}
