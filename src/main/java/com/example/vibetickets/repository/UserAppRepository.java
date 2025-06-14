package com.example.vibetickets.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.vibetickets.model.UserApp;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAppRepository extends JpaRepository<UserApp, Long> {

    boolean existsByEmail(String email);
    boolean existsByUserKey(String userKey);
    Optional<UserApp> findByUserKey(String userKey);
    UserApp findByEmail(String email);

}
