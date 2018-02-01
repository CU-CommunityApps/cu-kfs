package edu.cornell.kfs.sys.service.impl;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.springframework.beans.factory.DisposableBean;

import edu.cornell.kfs.sys.util.CURestClientUtils;

public abstract class DisposableClientServiceImplBase implements DisposableBean {
    
    private volatile Client client;
    
    @Override
    public void destroy() throws Exception {
        closeClientQuietly();
    }
    
    protected Client getClient() {
        // Use double-checked locking to lazy-load the Client object, similar to related locking in Rice.
        // See effective java 2nd ed. pg. 71
        Client jerseyClient = client;
        if (jerseyClient == null) {
            synchronized (this) {
                jerseyClient = client;
                if (jerseyClient == null) {
                    ClientConfig clientConfig = new ClientConfig();
                    jerseyClient = ClientBuilder.newClient(clientConfig);
                    client = jerseyClient;
                }
            }
        }
        return jerseyClient;
    }

    protected void closeClientQuietly() {
        // Use double-checked locking to retrieve the Client object, similar to related locking in Rice.
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
