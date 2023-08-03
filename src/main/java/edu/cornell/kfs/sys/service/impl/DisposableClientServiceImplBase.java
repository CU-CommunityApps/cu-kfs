package edu.cornell.kfs.sys.service.impl;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;

import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.springframework.beans.factory.DisposableBean;

import edu.cornell.kfs.sys.util.CURestClientUtils;
import edu.cornell.kfs.sys.web.CuJaxbProvider;

public abstract class DisposableClientServiceImplBase implements DisposableBean {
    private static final Logger LOG = LogManager.getLogger();
    private volatile Client client;
    
    @Override
    public void destroy() throws Exception {
        closeClientQuietly();
    }
    
    protected Client getClient(Class<?> classToRegister) {
        // Use double-checked locking to lazy-load the Client object.
        // See effective java 2nd ed. pg. 71
        Client jerseyClient = client;
        if (jerseyClient == null) {
            synchronized (this) {
                jerseyClient = client;
                if (jerseyClient == null) {
                    ClientConfig clientConfig = new ClientConfig();
                    clientConfig.register(CuJaxbProvider.class);
                    if (classToRegister != null) {
                        clientConfig.register(classToRegister);
                    }
                    jerseyClient = ClientBuilder.newClient(clientConfig);
                    client = jerseyClient;
                }
            }
        }
        return jerseyClient;
    }

    protected Client getClient() {
        return getClient(null);
    }

    protected void closeClientQuietly() {
        // Use double-checked locking to retrieve the Client object.
        // See effective java 2nd ed. pg. 71
        Client jerseyClient = client;
        if (jerseyClient == null) {
            synchronized (this) {
                jerseyClient = client;
            }
        }
        
        CURestClientUtils.closeQuietly(jerseyClient);
    }
    
    protected void disableRequestChunkingIfNecessary(Client cxfClient, Invocation.Builder requestBuilder) {
        if (cxfClient instanceof org.apache.cxf.jaxrs.client.spec.ClientImpl) {
            LOG.info("disableRequestChunkingIfNecessary: Explicitly disabling chunking because KFS is using a JAX-RS client of CXF type "
                    + cxfClient.getClass().getName());
            ClientConfiguration cxfConfig = WebClient.getConfig(requestBuilder);
            HTTPConduit conduit = cxfConfig.getHttpConduit();
            HTTPClientPolicy clientPolicy = conduit.getClient();
            clientPolicy.setAllowChunking(false);
        } else {
            LOG.info("disableRequestChunkingIfNecessary: There is no need to explicitly disable chunking for a JAX-RS client of type "
                    + cxfClient.getClass().getName());
        }
    }

}
