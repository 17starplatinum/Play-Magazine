package com.example.pmcore.repositories.auth.file;

import com.example.pmcore.dto.auth.file.UserDatabaseFileDto;
import com.example.pmcore.dto.auth.file.UserProfileFileDto;
import com.example.pmcore.model.auth.UserProfile;
import com.example.pmcore.repositories.auth.custom.UserProfileRepositoryCustom;
import com.example.pmcore.services.util.XMLParser;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository("fileBasedUserProfileRepository")
public class FileBasedUserProfileRepository implements UserProfileRepositoryCustom {
    private final XMLParser xmlParser;
    private final String xmlPath = "users.xml";

    public FileBasedUserProfileRepository(XMLParser xmlParser) {
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
    public UserProfile save(UserProfile profile) {
        UserDatabaseFileDto db = loadDatabase();
        UserProfileFileDto dto = new UserProfileFileDto(
                profile.getId(),
                profile.getName(),
                profile.getSurname(),
                profile.getBirthday()
        );

        for (int i = 0; i < db.getProfiles().size(); i++) {
            if (db.getProfiles().get(i).getId().equals(dto.getId())) {
                db.getProfiles().set(i, dto);
                saveDatabase(db);
                return profile;
            }
        }
        db.getProfiles().add(dto);
        saveDatabase(db);
        return profile;
    }

    @Override
    public UserProfile findById(UUID id) {
        UserDatabaseFileDto db = loadDatabase();
        UserProfileFileDto dto = db.findProfileByUserId(id);
        return dto != null ? new UserProfile(dto.getId(), dto.getName(), dto.getSurname(), dto.getBirthday()) : null;
    }
}
