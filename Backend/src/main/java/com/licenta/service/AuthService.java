package com.licenta.service;

import com.licenta.DTO.UserDTO;
import com.licenta.DTO.UserLoginDTO;
import com.licenta.DTO.UserRegisterDTO;
import com.licenta.model.User;
import com.licenta.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Service pentru autentificarea și înregistrarea utilizatorilor.
 * Include metode pentru login, înregistrare, verificare parolă și căutare utilizator după email.
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Caută un utilizator după email.
     */
    public Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }

    /**
     * Înregistrează un utilizator nou cu datele din DTO și returnează un UserDTO.
     */
    public UserDTO registerUser(UserRegisterDTO dto) {
        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        User user = new User(
                dto.getFirstName(),
                dto.getLastName(),
                dto.getEmail(),
                hashedPassword,
                dto.getTelephone(),
                dto.getRole()
        );
        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser);
    }

    /**
     * Verifică dacă parola introdusă corespunde cu cea criptată.
     */
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    /**
     * Autentifică un utilizator pe baza credențialelor și returnează informații esențiale.
     */
    public Map<String, String> login(UserLoginDTO loginDTO){
        Optional<User> optionalUser = userRepository.findByEmail(loginDTO.getEmail());
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Email sau parola invalide");
        }

        User user = optionalUser.get();
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email sau parola invalide");
        }

        String profilePicture = user.getProfilePicture();
        if (profilePicture == null || profilePicture.isBlank()) {
            profilePicture = "http://localhost:4200/Default_pfp.jpg";
        }

        return Map.of(
                "userId", String.valueOf(user.getId()),
                "role", user.getRole(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "profilePicture", profilePicture
        );
    }
}
