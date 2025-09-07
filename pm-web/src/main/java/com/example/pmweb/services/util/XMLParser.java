package com.example.pmweb.services.util;

import com.example.pmweb.model.auth.User;
import com.example.pmweb.model.auth.UserList;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class XMLParser {

    public Object getEntity(String xmlPath) throws IOException {
        try(BufferedReader reader = Files.newBufferedReader(Path.of(xmlPath), StandardCharsets.UTF_8)) {
            if (reader.read() > 0) {
                JAXBContext context = JAXBContext.newInstance(UserList.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                return unmarshaller.unmarshal(new File(xmlPath));
            }
        } catch (JAXBException e) {
            throw new IOException("Ошибка при чтении XML", e);
        }
        return null;
    }

    public void saveEntity(List<User> saveEntity, String xmlPath) throws IOException {
        try {
            UserList userList = new UserList();
            userList.setUsers(saveEntity);

            JAXBContext context = JAXBContext.newInstance(UserList.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            marshaller.marshal(userList, new File(xmlPath));
        } catch (JAXBException e) {
            throw new IOException("Ошибка при сохранении XML", e);
        }
    }
}
