package com.licenta.service;

import com.licenta.DTO.PublicUserDTO;
import com.licenta.model.User;
import com.licenta.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Service responsabil pentru operațiunile legate de utilizatori:
 * căutare, actualizare profil și profil public.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Găsește un utilizator după ID.
     *
     * @param id ID-ul utilizatorului
     * @return Optional cu utilizatorul (sau gol dacă nu există)
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Actualizează datele de profil ale utilizatorului și imaginea de profil, dacă este trimisă.
     *
     * @param userId    ID-ul utilizatorului
     * @param firstName Prenume
     * @param lastName  Nume
     * @param telephone Număr de telefon
     * @param file      Imaginea de profil (opțional)
     * @return răspuns HTTP cu mesaj și linkul imaginii, sau eroare
     */
    public ResponseEntity<?> updateUserProfile(Long userId, String firstName, String lastName,
                                               String telephone, MultipartFile file) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Utilizatorul nu a fost gasit");
        }

        User user = optionalUser.get();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setTelephone(telephone);

        if (file != null && !file.isEmpty()) {
            try {
                String fileName = "user_" + userId + "_" + file.getOriginalFilename();
                String folderPath = System.getProperty("user.dir") + "/uploads/";
                File uploadDir = new File(folderPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                String fullPath = folderPath + fileName;
                file.transferTo(new File(fullPath));
                user.setProfilePicture("/uploads/" + fileName);

            } catch (IOException e) {
                return ResponseEntity.status(500).body("Incarcarea imaginii a esuat: " + e.getMessage());
            }
        }

        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Profil actualizat cu succes",
                        "profilePicture", user.getProfilePicture()
                )
        );
    }

    /**
     * Returnează un obiect public (DTO) cu datele utilizatorului.
     *
     * @param userId ID-ul utilizatorului
     * @return DTO cu datele publice ale utilizatorului
     */
    public PublicUserDTO getPublicUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost gasit"));

        return new PublicUserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getTelephone(),
                user.getProfilePicture()
        );
    }
}

