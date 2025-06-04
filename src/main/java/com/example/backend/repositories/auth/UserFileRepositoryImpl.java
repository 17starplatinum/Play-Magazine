package com.example.backend.repositories.auth;

import com.example.backend.model.auth.User;
import com.example.backend.model.auth.UserList;
import com.example.backend.services.util.XMLParser;
import org.springframework.stereotype.Repository;


import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserFileRepositoryImpl implements UserFileRepository {
    private final XMLParser xmlParser;
    private final String xmlPath = "D:\\ITMO\\Software Engineering\\Business Logic of Software Systems\\Play Magazine\\src\\main\\resources\\users.xml";

    public UserFileRepositoryImpl(XMLParser xmlParser) {
        this.xmlParser = xmlParser;
    }

    public Optional<User> findByIdFromFile(UUID id) {
        UserList users;
        try {
            users = (UserList) xmlParser.getEntity(xmlPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (users == null) return Optional.empty();
        List<User> userEntities = users.getUsers();
        for (User cur: userEntities) {
            if (cur.getId().equals(id)) return Optional.of(cur);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsernameFromFile(String username) {
        UserList users;
        try {
            users = (UserList) xmlParser.getEntity(xmlPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (users == null) return Optional.empty();
        List<User> userEntities = users.getUsers();
        for (User cur: userEntities) {
            if (cur.getUsername().equals(username)) return Optional.of(cur);
        }
        return Optional.empty();
    }

    @Override
    public void saveIntoFile(User user) {
        UserList users;
        try {
            users = (UserList) xmlParser.getEntity(xmlPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (users == null) users = new UserList();
        users.getUsers().add(user);
        try {
            xmlParser.saveEntity(users.getUsers(), xmlPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
