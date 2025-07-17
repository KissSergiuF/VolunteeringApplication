package com.licenta.controller;

import com.licenta.DTO.UserDTO;
import com.licenta.DTO.UserLoginDTO;
import com.licenta.DTO.UserRegisterDTO;
import com.licenta.model.User;
import com.licenta.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;


/**
 * Controller pentru autentificare și înregistrare utilizatori.
 */
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Autentifică un utilizator și setează atributele în sesiune.
     *
     * @param loginDTO datele de autentificare
     * @param session sesiunea HTTP
     * @return informațiile utilizatorului sau mesaj de eroare
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserLoginDTO loginDTO, HttpSession session) {
        try {
            Map<String, String> userInfo = authService.login(loginDTO);
            session.setAttribute("userId", Long.parseLong(userInfo.get("userId")));
            session.setAttribute("role", userInfo.get("role"));
            session.setAttribute("firstName", userInfo.get("firstName"));
            session.setAttribute("lastName", userInfo.get("lastName"));
            return ResponseEntity.ok(userInfo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Deloghează utilizatorul și invalidează sesiunea curentă.
     *
     * @param request requestul HTTP
     * @param response răspunsul HTTP
     * @return mesaj de succes
     */
    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok("Utilizator delogat.");
    }

    /**
     * Înregistrează un nou utilizator.
     *
     * @param registerDTO datele noului utilizator
     * @return mesaj de succes sau eroare dacă email-ul este deja folosit
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody UserRegisterDTO registerDTO) {
        if (authService.findByEmail(registerDTO.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email-ul este deja folosit"));
        }

        authService.registerUser(registerDTO);
        return ResponseEntity.ok(Map.of("message", "Utilizator inregistrat cu succes"));
    }
}
