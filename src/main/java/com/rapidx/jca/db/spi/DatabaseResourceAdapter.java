package com.rapidx.jca.db.spi;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.xa.XAResource;
import java.util.Objects;

/**
 * JCA ResourceAdapter implementation managing global adapter lifecycle.
 */
public class DatabaseResourceAdapter implements ResourceAdapter {
    private static final Logger log = LoggerFactory.getLogger(DatabaseResourceAdapter.class);

    private String name = "JCA Database Adapter";

    public DatabaseResourceAdapter() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
        log.info("Starting JCA Resource Adapter: {}", name);
    }

    @Override
    public void stop() {
        log.info("Stopping JCA Resource Adapter: {}", name);
    }

    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) throws ResourceException {
        log.info("Endpoint activation called");
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
        log.info("Endpoint deactivation called");
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        return new XAResource[0];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatabaseResourceAdapter that = (DatabaseResourceAdapter) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
