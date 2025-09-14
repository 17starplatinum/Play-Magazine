package com.example.pmcore.jca;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;

public class BitrixConnectionFactoryImpl implements BitrixConnectionFactory {

    private final BitrixManagedConnectionFactory mcf;
    private final ConnectionManager cxManager;

    public BitrixConnectionFactoryImpl(BitrixManagedConnectionFactory mcf, ConnectionManager cxManager) {
        this.mcf = mcf;
        this.cxManager = cxManager;
    }

    public BitrixConnection getConnection(String token) throws ResourceException {
        BitrixConnectionRequestInfo cxRequestInfo = new BitrixConnectionRequestInfo(token);
        return (BitrixConnection) cxManager.allocateConnection(mcf, cxRequestInfo);
    }

    @Override
    public javax.naming.Reference getReference() {
        return null;
    }
}