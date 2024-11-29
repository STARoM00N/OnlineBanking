package com.bankapp.repositories;

import com.bankapp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = :emailOrUsername OR u.name = :emailOrUsername")
    Optional<User> findByUsernameOrEmail(String emailOrUsername);

    Optional<User> findByEmail(String email);
}