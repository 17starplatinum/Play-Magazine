package com.example.pmweb.repositories.data.finances;

import com.example.pmweb.model.data.finances.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
}
