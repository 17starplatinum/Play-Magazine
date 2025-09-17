package com.example.pmcore.services.util;

import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

@Service
public class XMLParser {
    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAutoMode;

    @Value("${app.user-db.create-if-missing:true}")
    private boolean createIfMissing;

    @Value("${app.user-db.fill-default-on-create:true}")
    private boolean fillDefaultOnCreate;

    private static final String USER_DB_TEMPLATE =
            """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <userDatabase>
                    \t<users/>
                    \t<budgets/>
                    \t<profiles/>
                    \t<verifications/>
                    </userDatabase>
                    """;

    @PostConstruct
    public void init() {
        try {
            String xmlPath = "users.xml";
            setUpFile(xmlPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setUpFile(String xmlPath) throws IOException {
        if (ddlAutoMode == null) {
            return;
        }
        String mode = ddlAutoMode.trim().toLowerCase();
        Path path = Path.of(xmlPath);

        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        boolean exists = Files.exists(path);
        long size = exists ? Files.size(path) : 0;
        if (!exists) {
            if ("create-drop".equals(mode) || "create".equals(mode)) {
                writeAtomic(path);
                return;
            }

            if ("update".equals(mode)) {
                if (createIfMissing) {
                    Files.createFile(path);
                    if (fillDefaultOnCreate) {
                        writeAtomic(path);
                    }
                } else {
                    throw new IOException("XML file for file-based storage is missing and 'app.user-db.create-if-missing' is false: " + path);
                }
                return;
            }

            Files.createFile(path);
            if ("validate".equals(mode)) {
                throw new IOException("XML file created but ddl-auto=validate requires existing non-empty file: " + path);
            }
        }

        switch (mode) {
            case "create-drop":
                writeAtomic(path);
                break;
            case "create":
                if (size == 0) writeAtomic(path);
                break;
            case "validate":
                if (size == 0) throw new IOException("XML file for file-based storage is missing or empty (validate failed): " + path);
                break;
            case "update":
            default:
                break;
        }
    }

    private void writeAtomic(Path target) throws IOException {
        Path parent = target.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        Path tmp;
        if (parent != null) {
            tmp = Files.createTempFile(parent, "tmp-", ".xml");
        } else {
            tmp = Files.createTempFile("tmp-", ".xml");
        }

        try {
            Files.writeString(tmp, USER_DB_TEMPLATE, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            try {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                try { Files.deleteIfExists(tmp); } catch (Exception ignore) {}
                throw e;
            }
        }
    }

    public <T> T getEntity(String xmlPath, Class<T> tClass) throws IOException {
        try {
            Path path = Path.of(xmlPath);
            if (!Files.exists(path) || Files.size(path) == 0) {
                return null;
            }
            JAXBContext context = JAXBContext.newInstance(tClass);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (T) unmarshaller.unmarshal(new File(xmlPath));
        } catch (JAXBException e) {
            throw new IOException("Ошибка при чтении в XML", e);
        }
    }

    public <T> void saveEntity(T entity, String xmlPath) throws IOException {
        try {
            JAXBContext context = JAXBContext.newInstance(entity.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(entity, new File(xmlPath));
        } catch (JAXBException e) {
            throw new IOException("Ошибка при сохранении в XML", e);
        }
    }
}
