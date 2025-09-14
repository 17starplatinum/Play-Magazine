package com.example.pmcore.repositories.auth.custom;

import com.example.backend.model.auth.UserBudget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserBudgetRepositoryCustom {
    UserBudget save(UserBudget userBudget);
    List<UserBudget> findAll();
    Optional<UserBudget> findById(UUID id);
}
