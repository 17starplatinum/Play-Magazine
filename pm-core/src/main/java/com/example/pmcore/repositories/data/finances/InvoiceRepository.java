package com.example.pmcore.repositories.data.finances;

import com.example.pmcore.model.data.finances.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
}
