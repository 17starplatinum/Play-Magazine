package com.example.backend.services.util;

import com.example.backend.model.auth.User;
import com.thoughtworks.xstream.XStream;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service
public class XMLParser {
    private final XStream xstream;

    public XMLParser() {
        this.xstream = new XStream();
    }

    public <T> Object getEntity(Class<T> convertClass, String aliasName, String xmlPath) throws IOException {
        xstream.alias(aliasName, convertClass);
        try (BufferedReader reader = new BufferedReader(new FileReader(xmlPath))){
            File file = new File(xmlPath);
            if (reader.read() > 0) {
                JAXBContext jaxbContext = JAXBContext.newInstance(convertClass);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                return jaxbUnmarshaller.unmarshal(file);
            }
        } catch (JAXBException | IOException e) {
            throw new IOException("Произошла ошибка при работе с файлом");
        }
        return null;
    }

    public void saveEntity(List<?> saveEntity, String xmlPath) throws IOException {
        xstream.alias("user", User.class);
        xstream.alias("users", List.class);
        try {
            xstream.toXML(saveEntity, new FileWriter(xmlPath, false));
        } catch (IOException e) {
            throw new IOException("Произошла ошибка при работе с файлом");
        }
    }
}
