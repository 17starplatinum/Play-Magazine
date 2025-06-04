package com.example.backend.services.util;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public LocalDate unmarshal(String v) {
        return LocalDate.parse(v, formatter);
    }

    @Override
    public String marshal(LocalDate v) {
        return v != null ? v.format(formatter) : null;
    }
}
