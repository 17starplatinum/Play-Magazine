package com.example.backend.repositories.auth;

import com.example.backend.model.auth.UserBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserBudgetRepository extends JpaRepository<UserBudget, UUID> {

}
