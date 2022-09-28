package edu.cornell.kfs.sys.service.impl;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.springframework.beans.factory.DisposableBean;

import edu.cornell.kfs.sys.util.CURestClientUtils;

public abstract class DisposableClientServiceImplBase implements DisposableBean {
    
    private volatile Client client;
    
    @Override
    public void destroy() throws Exception {
        closeClientQuietly();
    }
    
    protected Client getClient(Class classToRegister) {
        // Use double-checked locking to lazy-load the Client object.
        // See effective java 2nd ed. pg. 71
        Client jerseyClient = client;
        if (jerseyClient == null) {
            synchronized (this) {
                jerseyClient = client;
                if (jerseyClient == null) {
                    ClientConfig clientConfig = new ClientConfig();
                    jerseyClient = ClientBuilder.newClient(clientConfig);
                    if (classToRegister != null) {
                        jerseyClient.register(classToRegister);
                    }
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

}
