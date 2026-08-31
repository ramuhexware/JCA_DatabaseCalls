package com.rapidx.jca.db.spi;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnectionMetaData;

/**
 * JCA ManagedConnectionMetaData providing metadata about physical EIS connection.
 */
public class DatabaseManagedConnectionMetaData implements ManagedConnectionMetaData {
    private final String userName;

    public DatabaseManagedConnectionMetaData(String userName) {
        this.userName = userName;
    }

    @Override
    public String getEISProductName() throws ResourceException {
        return "Embedded H2 Relational Database";
    }

    @Override
    public String getEISProductVersion() throws ResourceException {
        return "2.3.232";
    }

    @Override
    public int getMaxConnections() throws ResourceException {
        return 100;
    }

    @Override
    public String getUserName() throws ResourceException {
        return userName != null ? userName : "sa";
    }
}
