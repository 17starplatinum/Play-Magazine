package com.example.pmcore.repositories.auth.file;

import com.example.pmcore.dto.auth.file.UserDatabaseFileDto;
import com.example.pmcore.dto.auth.file.UserVerificationFileDto;
import com.example.pmcore.model.auth.UserVerification;
import com.example.pmcore.repositories.auth.custom.UserVerificationRepositoryCustom;
import com.example.pmcore.services.util.XMLParser;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository("fileBasedUserVerificationRepository")
public class FileBasedUserVerificationRepository implements UserVerificationRepositoryCustom {

    private final XMLParser xmlParser;
    private final String xmlPath = "users.xml";

    public FileBasedUserVerificationRepository(XMLParser xmlParser) {
        this.xmlParser = xmlParser;
    }

    private UserDatabaseFileDto loadDatabase() {
        try {
            UserDatabaseFileDto db = xmlParser.getEntity(xmlPath, UserDatabaseFileDto.class);
            return db != null ? db : new UserDatabaseFileDto();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load user database", e);
        }
    }

    private void saveDatabase(UserDatabaseFileDto db) {
        try {
            xmlParser.saveEntity(db, xmlPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save user database", e);
        }
    }

    @Override
    public UserVerification save(UserVerification budget) {
        UserDatabaseFileDto db = loadDatabase();
        UserVerificationFileDto dto = new UserVerificationFileDto(
                budget.getId(),
                budget.getEmail(),
                budget.getVerificationCode(),
                budget.getCreationTime(),
                budget.isEnable(),
                budget.getFailedAttempts()
        );

        for (int i = 0; i < db.getBudgets().size(); i++) {
            if (db.getVerifications().get(i).getId().equals(dto.getId())) {
                db.getVerifications().set(i, dto);
                saveDatabase(db);
                return budget;
            }
        }
        db.getVerifications().add(dto);
        saveDatabase(db);
        return budget;
    }

    @Override
    public Optional<UserVerification> findById(UUID id) {
        UserDatabaseFileDto db = loadDatabase();
        UserVerificationFileDto dto = db.findVerificationById(id);
        return dto != null ? Optional.of(new UserVerification(dto.getId(), dto.getEmail(), dto.getVerificationCode(), dto.getCreationTime(), dto.isEnable(), dto.getFailedAttempts())) : Optional.empty();
    }

    @Override
    public Optional<UserVerification> findByEmail(String email) {
        UserDatabaseFileDto db = loadDatabase();
        UserVerificationFileDto dto = db.findVerificationByEmail(email);
        return dto != null ? Optional.of(new UserVerification(dto.getId(), dto.getEmail(), dto.getVerificationCode(), dto.getCreationTime(), dto.isEnable(), dto.getFailedAttempts())) : Optional.empty();
    }

    @Override
    public Optional<UserVerification> findByEmailAndId(String email, UUID uuid) {
        UserDatabaseFileDto db = loadDatabase();
        UserVerificationFileDto dto = db.findVerificationByIdAndEmail(uuid, email);
        return dto != null ? Optional.of(new UserVerification(dto.getId(), dto.getEmail(), dto.getVerificationCode(), dto.getCreationTime(), dto.isEnable(), dto.getFailedAttempts())) : Optional.empty();
    }

    @Override
    public void deleteById(UUID id) {
        UserDatabaseFileDto db = loadDatabase();
        db.getProfiles().removeIf(u -> u.getId().equals(id));
        saveDatabase(db);
    }
}
