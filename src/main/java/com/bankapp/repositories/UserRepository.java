package com.bankapp.repositories;

import com.bankapp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    Optional<User> findByUsername(String username);

    // แก้ไข method นี้โดยใช้ @Query
    @Query("SELECT u FROM User u WHERE u.email = :emailOrUsername OR u.username = :emailOrUsername")
    Optional<User> findByEmailOrUsername(String emailOrUsername);
}

