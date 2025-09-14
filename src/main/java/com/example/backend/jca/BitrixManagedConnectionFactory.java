package com.example.backend.jca;

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

    @ConfigProperty(defaultValue = "https://b24-yc4n1w.bitrix24.ru")
    private String baseUrl;
    @ConfigProperty(defaultValue = "1")
    private String userId;
    @ConfigProperty
    private String token;
    private PrintWriter logWriter;

    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        return new BitrixConnectionFactoryImpl(this, cxManager);
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        return createConnectionFactory(new LocalConnectionManager());
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        String token = this.token; // fallback

        if (cxRequestInfo instanceof BitrixConnectionRequestInfo) {
            token = ((BitrixConnectionRequestInfo) cxRequestInfo).getToken();
        }

        return new BitrixManagedConnection(this, baseUrl, userId, token);
    }

    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        String requestedToken = this.token;
        if (cxRequestInfo instanceof BitrixConnectionRequestInfo) {
            requestedToken = ((BitrixConnectionRequestInfo) cxRequestInfo).getToken();
        }

        for (Object obj : connectionSet) {
            if (obj instanceof BitrixManagedConnection) {
                BitrixManagedConnection mc = (BitrixManagedConnection) obj;
                return mc;
            }
        }
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }

    @Override
    public int hashCode() {
        return (baseUrl + userId + token).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BitrixManagedConnectionFactory) {
            BitrixManagedConnectionFactory other = (BitrixManagedConnectionFactory) obj;
            return this.baseUrl.equals(other.baseUrl) &&
                    this.userId.equals(other.userId) &&
                    this.token.equals(other.token);
        }
        return false;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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