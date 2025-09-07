package com.example.pmweb.model.auth;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@XmlRootElement(name = "userList")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserList {
    @XmlElement(name = "user")
    private List<User> users = new ArrayList<>();
}