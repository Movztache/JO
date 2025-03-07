package com.example.jeuxolympiques.repository;

import com.example.jeuxolympiques.model.Rule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleRepository extends JpaRepository<Rule, Long> {
    Rule findByName(String name);
}

