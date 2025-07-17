package com.licenta.service;

import com.licenta.DTO.EventFeedbackDTO;
import com.licenta.model.Event;
import com.licenta.model.EventFeedback;
import com.licenta.model.User;
import com.licenta.repository.EventFeedbackRepository;
import com.licenta.repository.EventRepository;
import com.licenta.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventFeedbackServiceTest {

    @InjectMocks
    private EventFeedbackService eventFeedbackService;

    @Mock
    private EventFeedbackRepository feedbackRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void testSubmitFeedback_success() {
        Event event = new Event();
        event.setId(1L);
        event.setIsActive(false);

        User fromUser = new User();
        fromUser.setId(2L);

        User toUser = new User();
        toUser.setId(3L);

        event.setOrganizer(toUser);

        EventFeedbackDTO dto = new EventFeedbackDTO();
        dto.setEventId(1L);
        dto.setFromUserId(2L);
        dto.setRating(5);
        dto.setComment("Great event!");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(2L)).thenReturn(Optional.of(fromUser));
        when(feedbackRepository.findByEvent_IdAndFromUser_IdAndToUser_Id(1L, 2L, 3L)).thenReturn(Optional.empty());

        eventFeedbackService.submitFeedback(dto);

        verify(feedbackRepository, times(1)).save(any(EventFeedback.class));
    }

    @Test
    void testGetFeedbackForUser_returnsList() {
        EventFeedback feedback = new EventFeedback();
        Event event = new Event();
        event.setId(1L);
        event.setName("Test Event");
        feedback.setEvent(event);

        User fromUser = new User();
        fromUser.setId(2L);
        fromUser.setFirstName("Ana");
        fromUser.setLastName("Popescu");
        fromUser.setProfilePicture("ana.jpg");

        User toUser = new User();
        toUser.setId(3L);

        feedback.setFromUser(fromUser);
        feedback.setToUser(toUser);
        feedback.setRating(4);
        feedback.setComment("Nice!");

        when(feedbackRepository.findByToUser_Id(3L)).thenReturn(List.of(feedback));

        List<EventFeedbackDTO> result = eventFeedbackService.getFeedbackForUser(3L);

        assertEquals(1, result.size());
        EventFeedbackDTO dto = result.get(0);
        assertEquals(1L, dto.getEventId());
        assertEquals(2L, dto.getFromUserId());
        assertEquals(4, dto.getRating());
        assertEquals("Nice!", dto.getComment());
        assertEquals("Ana Popescu", dto.getFromUserFullName());
        assertEquals("ana.jpg", dto.getFromUserProfilePicture());
        assertEquals("Test Event", dto.getEventName());
    }

    @Test
    void testGetAverageFeedbackForUser_returnsValue() {
        when(feedbackRepository.getAverageRatingForUser(4L)).thenReturn(4.5);

        Double avg = eventFeedbackService.getAverageFeedbackForUser(4L);

        assertEquals(4.5, avg);
    }
}
