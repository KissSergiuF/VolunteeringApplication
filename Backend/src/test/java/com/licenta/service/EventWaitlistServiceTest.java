package com.licenta.service;

import com.licenta.model.Event;
import com.licenta.model.EventWaitlist;
import com.licenta.model.User;
import com.licenta.repository.EventRepository;
import com.licenta.repository.EventWaitlistRepository;
import com.licenta.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventWaitlistServiceTest {

    @InjectMocks
    private EventWaitlistService eventWaitlistService;

    @Mock
    private EventWaitlistRepository waitlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EmailService emailService;

    @Test
    void testSubscribe_success() {
        User user = new User();
        user.setId(1L);

        Event event = new Event();
        event.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(event));
        when(waitlistRepository.existsByEventAndUser(event, user)).thenReturn(false);

        eventWaitlistService.subscribe(1L, 2L);

        verify(waitlistRepository, times(1)).save(any(EventWaitlist.class));
    }

    @Test
    void testUnsubscribe_success() {
        User user = new User();
        user.setId(1L);

        Event event = new Event();
        event.setId(2L);

        EventWaitlist waitlist = EventWaitlist.builder()
                .user(user)
                .event(event)
                .subscribedAt(ZonedDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(event));
        when(waitlistRepository.findByEventAndUser(event, user)).thenReturn(Optional.of(waitlist));

        eventWaitlistService.unsubscribe(1L, 2L);

        verify(waitlistRepository, times(1)).delete(waitlist);
    }

    @Test
    void testIsSubscribed_true() {
        User user = new User();
        user.setId(1L);

        Event event = new Event();
        event.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(event));
        when(waitlistRepository.existsByEventAndUser(event, user)).thenReturn(true);

        boolean result = eventWaitlistService.isSubscribed(1L, 2L);

        assertTrue(result);
    }

    @Test
    void testGetSubscribedEventIds_returnsList() {
        User user = new User();
        user.setId(1L);

        Event event1 = new Event();
        event1.setId(10L);

        Event event2 = new Event();
        event2.setId(20L);

        EventWaitlist w1 = EventWaitlist.builder().user(user).event(event1).build();
        EventWaitlist w2 = EventWaitlist.builder().user(user).event(event2).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(waitlistRepository.findAllByUser(user)).thenReturn(List.of(w1, w2));

        List<Long> result = eventWaitlistService.getSubscribedEventIds(1L);

        assertEquals(2, result.size());
        assertTrue(result.contains(10L));
        assertTrue(result.contains(20L));
    }
}
