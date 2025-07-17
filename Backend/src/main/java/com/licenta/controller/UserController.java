package com.licenta.controller;

import com.licenta.DTO.PublicUserDTO;
import com.licenta.model.User;
import com.licenta.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;


/**
 * Controller pentru operațiuni legate de utilizator:
 * - obținerea datelor profilului
 * - actualizarea profilului
 * - vizualizarea profilului public
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Returnează datele utilizatorului logat (din sesiune).
     */
    @GetMapping("/profil")
    public ResponseEntity<?> getUserInfo(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body("Nu esti autentificat");
        }

        Optional<User> user = userService.findById(userId);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.badRequest().body("Utilizatorul nu a fost gasit");
        }
    }

    /**
     * Actualizează datele profilului pentru utilizatorul logat.
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateUserProfile(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("telephone") String telephone,
            @RequestParam(value = "profilePicture", required = false) MultipartFile file,
            HttpSession session
    ) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body("Nu esti autentificat");
        }

        try {
            return userService.updateUserProfile(userId, firstName, lastName, telephone, file);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Eroare la actualizarea profilului: " + e.getMessage());
        }
    }

    /**
     * Returnează datele publice ale unui utilizator, pentru vizualizare de către alții.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<PublicUserDTO> getPublicUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getPublicUserById(id));
    }

}