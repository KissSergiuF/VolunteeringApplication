package com.licenta.service;

import com.licenta.DTO.UserDTO;
import com.licenta.DTO.UserRegisterDTO;
import com.licenta.model.User;
import com.licenta.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    void testFindByEmail_returnsUser_whenUserExists() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = authService.findByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void testVerifyPassword_returnsTrue_whenPasswordMatches() {
        when(passwordEncoder.matches("plain", "hashed")).thenReturn(true);

        boolean result = authService.verifyPassword("plain", "hashed");

        assertTrue(result);
    }

    @Test
    void testRegisterUser_success() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setFirstName("Ana");
        dto.setLastName("Popescu");
        dto.setEmail("ana@example.com");
        dto.setPassword("password123");
        dto.setTelephone("0123456789");
        dto.setRole("USER");

        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashedPassword");

        User savedUser = new User(dto.getFirstName(), dto.getLastName(), dto.getEmail(),
                "hashedPassword", dto.getTelephone(), dto.getRole());
        savedUser.setId(1L);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDTO result = authService.registerUser(dto);

        assertEquals(savedUser.getFirstName(), result.getFirstName());
        assertEquals(savedUser.getLastName(), result.getLastName());
        assertEquals(savedUser.getEmail(), result.getEmail());
    }
}
