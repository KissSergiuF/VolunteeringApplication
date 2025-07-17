package com.licenta.service;

import com.licenta.model.Event;
import com.licenta.model.EventRegistration;
import com.licenta.repository.EventRegistrationRepository;
import com.licenta.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventRegistrationServiceTest {

    @InjectMocks
    private EventRegistrationService eventRegistrationService;

    @Mock
    private EventRegistrationRepository registrationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventWaitlistService waitlistService;

    @Test
    void testRegisterUserToEvent_success() {
        Event event = new Event();
        event.setId(1L);
        event.setMaxVolunteers(10);

        when(registrationRepository.existsByEventIdAndUserId(1L, 2L)).thenReturn(false);
        when(registrationRepository.countByEventId(1L)).thenReturn(5);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        eventRegistrationService.registerUserToEvent(1L, 2L);

        verify(registrationRepository, times(1)).save(any(EventRegistration.class));
        verify(waitlistService, times(1)).unsubscribe(2L, 1L);
    }

    @Test
    void testUnregisterUserFromEvent_success() {
        when(registrationRepository.existsByEventIdAndUserId(1L, 2L)).thenReturn(true);

        eventRegistrationService.unregisterUserFromEvent(1L, 2L);

        verify(registrationRepository, times(1)).deleteByEventIdAndUserId(1L, 2L);
        verify(waitlistService, times(1)).checkAndNotifyWaitlist(1L);
    }

    @Test
    void testGetUserIdsByEventId_returnsList() {
        List<Long> userIds = List.of(10L, 20L, 30L);

        when(registrationRepository.findUserIdsByEventId(5L)).thenReturn(userIds);

        List<Long> result = eventRegistrationService.getUserIdsByEventId(5L);

        assertEquals(3, result.size());
        assertTrue(result.contains(10L));
        assertTrue(result.contains(20L));
        assertTrue(result.contains(30L));
    }
}
