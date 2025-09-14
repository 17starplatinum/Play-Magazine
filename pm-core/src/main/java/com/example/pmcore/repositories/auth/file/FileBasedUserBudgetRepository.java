package com.example.pmcore.repositories.auth.file;

import com.example.pmcore.dto.auth.file.UserBudgetFileDto;
import com.example.pmcore.dto.auth.file.UserDatabaseFileDto;
import com.example.pmcore.model.auth.UserBudget;
import com.example.pmcore.repositories.auth.custom.UserBudgetRepositoryCustom;
import com.example.pmcore.services.util.XMLParser;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository("fileBasedUserBudgetRepository")
public class FileBasedUserBudgetRepository implements UserBudgetRepositoryCustom {

    private final XMLParser xmlParser;
    private final String xmlPath = "users.xml";

    public FileBasedUserBudgetRepository(XMLParser xmlParser) {
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
    public UserBudget save(UserBudget budget) {
        UserDatabaseFileDto db = loadDatabase();
        UserBudgetFileDto dto = new UserBudgetFileDto(
                budget.getId(),
                budget.getSpendingLimit(),
                budget.getCurrentSpending(),
                budget.getLastLimitReset()
        );

        for (int i = 0; i < db.getBudgets().size(); i++) {
            if (db.getBudgets().get(i).getId().equals(dto.getId())) {
                db.getBudgets().set(i, dto);
                saveDatabase(db);
                return budget;
            }
        }
        db.getBudgets().add(dto);
        saveDatabase(db);
        return budget;
    }

    private UserBudgetFileDto toDto(UserBudget userBudget) {
        return UserBudgetFileDto.builder()
                .id(userBudget.getId())
                .spendingLimit(userBudget.getSpendingLimit())
                .currentSpending(userBudget.getCurrentSpending())
                .lastLimitReset(userBudget.getLastLimitReset())
                .build();
    }

    private UserBudget fromDto(UserBudgetFileDto dto) {
        return UserBudget.builder()
                .id(dto.getId())
                .spendingLimit(dto.getSpendingLimit())
                .currentSpending(dto.getCurrentSpending())
                .lastLimitReset(dto.getLastLimitReset())
                .build();
    }

    @Override
    public List<UserBudget> findAll() {
        UserDatabaseFileDto db = loadDatabase();
        return db.getBudgets().stream().map(this::fromDto).collect(Collectors.toList());
    }

    @Override
    public Optional<UserBudget> findById(UUID id) {
        UserDatabaseFileDto db = loadDatabase();
        UserBudgetFileDto dto = db.findBudgetById(id);
        return dto != null ? Optional.of(new UserBudget(dto.getId(), dto.getSpendingLimit(), dto.getCurrentSpending(), dto.getLastLimitReset())) : Optional.empty();
    }
}
