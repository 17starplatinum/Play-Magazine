package com.example.backend.model.auth;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@XmlRootElement(name = "userList")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserList {
    @XmlElement(name = "user")
    private List<User> users = new ArrayList<>();
}