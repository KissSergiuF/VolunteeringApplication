package com.licenta.repository;
import com.licenta.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
/**
 * Repository pentru entitatea User.
 * Permite operații CRUD și căutare după email.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Găsește un utilizator după adresa de email.
     */
    Optional<User> findByEmail(String email);
}