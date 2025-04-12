package com.example.backend.repositories.data.finances;

import com.example.backend.model.data.finances.MonetaryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MonetaryTransactionRepository extends JpaRepository<MonetaryTransaction, UUID> {
}
