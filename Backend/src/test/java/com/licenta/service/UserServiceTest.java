package com.licenta.service;

import com.licenta.DTO.PublicUserDTO;
import com.licenta.model.User;
import com.licenta.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Test
    void testFindById_returnsUser_whenUserExists() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Ana");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Ana", result.get().getFirstName());
    }

    @Test
    void testGetPublicUserById_returnsDTO_whenUserExists() {
        User user = new User();
        user.setId(2L);
        user.setFirstName("Maria");
        user.setLastName("Ionescu");
        user.setEmail("maria@example.com");
        user.setTelephone("0712345678");
        user.setProfilePicture("/uploads/maria.jpg");

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        PublicUserDTO result = userService.getPublicUserById(2L);

        assertEquals(2L, result.getId());
        assertEquals("Maria", result.getFirstName());
        assertEquals("Ionescu", result.getLastName());
        assertEquals("maria@example.com", result.getEmail());
        assertEquals("0712345678", result.getTelephone());
        assertEquals("/uploads/maria.jpg", result.getProfilePicture());
    }

    @Test
    void testUpdateUserProfile_withoutFile_success() {
        User user = new User();
        user.setId(3L);
        user.setProfilePicture("default.jpg");

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        ResponseEntity<?> response = userService.updateUserProfile(
                3L,
                "Ion",
                "Pop",
                "0765432109",
                null
        );

        assertEquals(200, response.getStatusCodeValue());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Profil actualizat cu succes", body.get("message"));
    }

}
