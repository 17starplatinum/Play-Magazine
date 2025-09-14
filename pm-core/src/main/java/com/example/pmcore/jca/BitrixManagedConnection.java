package com.example.pmcore.jca;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class BitrixManagedConnection implements ManagedConnection {

    private final BitrixManagedConnectionFactory mcf;
    private final String baseUrl;
    private final String userId;
    private final String token;
    private final List<ConnectionEventListener> listeners = new ArrayList<>();
    private BitrixConnectionImpl connection;

    public BitrixManagedConnection(
            BitrixManagedConnectionFactory mcf,
            String baseUrl,
            String userId,
            String token) {
        this.mcf = mcf;
        this.baseUrl = baseUrl;
        this.userId = userId;
        this.token = token;
    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        if (connection == null || connection.isClosed()) {
            connection = new BitrixConnectionImpl(this, baseUrl, userId, token);
        }
        return connection;
    }

    @Override
    public void destroy() throws ResourceException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    @Override
    public void cleanup() throws ResourceException {
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
        if (connection instanceof BitrixConnectionImpl) {
            this.connection = (BitrixConnectionImpl) connection;
        } else {
            throw new ResourceException("Неподдерживаемый тип соединения");
        }
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        throw new NotSupportedException("XA не поддерживается");
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        throw new NotSupportedException("Локальные транзакции не поддерживаются");
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return new ManagedConnectionMetaData() {
            @Override
            public String getEISProductName() {
                return "Bitrix24";
            }

            @Override
            public String getEISProductVersion() {
                return "1.0";
            }

            @Override
            public int getMaxConnections() {
                return 10;
            }

            @Override
            public String getUserName() {
                return userId;
            }
        };
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return null;
    }

    void notifyConnectionClosed() {
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
        for (ConnectionEventListener listener : listeners) {
            listener.connectionClosed(event);
        }
    }
}