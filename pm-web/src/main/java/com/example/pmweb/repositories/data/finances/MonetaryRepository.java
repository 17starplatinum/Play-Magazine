package com.example.pmweb.repositories.data.finances;

import com.example.pmweb.model.data.finances.MonetaryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MonetaryRepository extends JpaRepository<MonetaryTransaction, UUID> {

}
