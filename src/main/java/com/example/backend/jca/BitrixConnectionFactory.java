package com.example.backend.jca;

import javax.naming.Referenceable;
import javax.resource.ResourceException;
import java.io.Serializable;

public interface BitrixConnectionFactory extends Referenceable, Serializable {
    BitrixConnection getConnection(String token) throws ResourceException;
}