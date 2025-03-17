package com.example.jeuxolympiques.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.jeuxolympiques.model.UserApp;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAppRepository extends JpaRepository<UserApp, Long> {

    UserApp findByEmail(String email);


}
