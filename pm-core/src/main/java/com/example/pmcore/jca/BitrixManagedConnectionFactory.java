package com.example.pmcore.jca;

import lombok.Getter;
import lombok.Setter;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.util.Set;

@ConnectionDefinition(
        connectionFactory = BitrixConnectionFactory.class,
        connectionFactoryImpl = BitrixConnectionFactoryImpl.class,
        connection = BitrixConnection.class,
        connectionImpl = BitrixConnectionImpl.class
)
public class BitrixManagedConnectionFactory implements ManagedConnectionFactory {

    @Setter
    @Getter
    @ConfigProperty(defaultValue = "https://b24-yc4n1w.bitrix24.ru")
    private String baseUrl;
    @Getter
    @Setter
    @ConfigProperty(defaultValue = "1")
    private String userId;
    @Setter
    @Getter
    @ConfigProperty
    private String token;
    private PrintWriter logWriter;

    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) {
        return new BitrixConnectionFactoryImpl(this, cxManager);
    }

    @Override
    public Object createConnectionFactory() {
        return createConnectionFactory(new LocalConnectionManager());
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) {
        String token = this.token;

        if (cxRequestInfo instanceof BitrixConnectionRequestInfo) {
            token = ((BitrixConnectionRequestInfo) cxRequestInfo).getToken();
        }

        return new BitrixManagedConnection(baseUrl, userId, token);
    }

    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) {
        String requestedToken = this.token;
        if (cxRequestInfo instanceof BitrixConnectionRequestInfo) {
            requestedToken = ((BitrixConnectionRequestInfo) cxRequestInfo).getToken();
        }

        for (Object obj : connectionSet) {
            if (obj instanceof BitrixManagedConnection) {
                return (BitrixManagedConnection) obj;
            }
        }
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter() {
        return logWriter;
    }

    @Override
    public int hashCode() {
        return (baseUrl + userId + token).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BitrixManagedConnectionFactory other) {
            return this.baseUrl.equals(other.baseUrl) &&
                    this.userId.equals(other.userId) &&
                    this.token.equals(other.token);
        }
        return false;
    }

    private static class LocalConnectionManager implements ConnectionManager {
        @Override
        public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
            try {
                ManagedConnection mc = mcf.createManagedConnection(null, cxRequestInfo);
                return mc.getConnection(null, cxRequestInfo);
            } catch (Exception e) {
                throw new ResourceException("Не удалось создать соединение", e);
            }
        }
    }
}