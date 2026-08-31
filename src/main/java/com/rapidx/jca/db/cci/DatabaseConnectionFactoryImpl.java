package com.rapidx.jca.db.cci;

import com.rapidx.jca.db.record.ListRecord;
import com.rapidx.jca.db.record.MapRecord;
import com.rapidx.jca.db.spi.DatabaseManagedConnectionFactory;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.*;
import jakarta.resource.spi.ConnectionManager;

import javax.naming.NamingException;
import javax.naming.Reference;

/**
 * JCA CCI ConnectionFactory implementation allowing application clients to acquire connections.
 */
public class DatabaseConnectionFactoryImpl implements ConnectionFactory {
    private static final long serialVersionUID = 1L;

    private final DatabaseManagedConnectionFactory mcf;
    private final ConnectionManager cm;
    private Reference reference;

    public DatabaseConnectionFactoryImpl(DatabaseManagedConnectionFactory mcf, ConnectionManager cm) {
        this.mcf = mcf;
        this.cm = cm;
    }

    @Override
    public Connection getConnection() throws ResourceException {
        if (cm != null) {
            return (Connection) cm.allocateConnection(mcf, null);
        } else {
            return new DatabaseConnectionImpl((com.rapidx.jca.db.spi.DatabaseManagedConnection) mcf.createManagedConnection(null, null));
        }
    }

    @Override
    public Connection getConnection(ConnectionSpec properties) throws ResourceException {
        return getConnection();
    }

    @Override
    public RecordFactory getRecordFactory() throws ResourceException {
        return new RecordFactory() {
            @Override
            public MappedRecord createMappedRecord(String recordName) throws ResourceException {
                return new MapRecord(recordName, "JCA Mapped Record");
            }

            @Override
            public IndexedRecord createIndexedRecord(String recordName) throws ResourceException {
                return new ListRecord(recordName, "JCA Indexed Record");
            }
        };
    }

    @Override
    public ResourceAdapterMetaData getMetaData() throws ResourceException {
        return new ResourceAdapterMetaData() {
            @Override
            public String getAdapterVersion() {
                return "1.0.0";
            }

            @Override
            public String getAdapterVendorName() {
                return "RapidX JCA";
            }

            @Override
            public String getAdapterName() {
                return "JCA Database Adapter";
            }

            @Override
            public String getAdapterShortDescription() {
                return "Resource Adapter for Database Calls, Persistence and Retrieval";
            }

            @Override
            public String getSpecVersion() {
                return "2.1";
            }

            @Override
            public String[] getInteractionSpecsSupported() {
                return new String[]{DatabaseInteractionSpec.class.getName()};
            }

            @Override
            public boolean supportsExecuteWithInputAndOutputRecord() {
                return true;
            }

            @Override
            public boolean supportsExecuteWithInputRecordOnly() {
                return true;
            }

            @Override
            public boolean supportsLocalTransactionDemarcation() {
                return true;
            }
        };
    }

    @Override
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    @Override
    public Reference getReference() throws NamingException {
        return reference;
    }
}
