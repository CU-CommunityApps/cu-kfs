package edu.cornell.kfs.sys.service.impl;

import java.util.function.Function;

import org.springframework.beans.factory.DisposableBean;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * Helper superclass for services that need to call RESTful endpoints
 * using the Jersey 1.x client API. This class manages the lifecycle
 * of the heavyweight Client object, and provides utility methods
 * for managing the lifecycle of related objects like ClientResponse.
 * 
 * The Client object will be lazily initialized upon first use,
 * and will be cleaned up accordingly when the application terminates.
 * Subclasses can override the buildClientConfig() method to control
 * what settings will be used on the generated Client.
 */
public abstract class AbstractJerseyClientEnabledService implements DisposableBean {

    private volatile Client initializedClient;

    /**
     * Closes/destroys the Jersey Client object when Spring shuts down.
     * Please be sure to invoke this superclass method in any overrides,
     * to ensure proper Client shutdown.
     * 
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    @Override
    public void destroy() throws Exception {
        closeJerseyClientQuietly();
    }

    /**
     * Helper method for automatically adding ClientResponse lifecycle management
     * to cases where the client returns a ClientResponse object and then
     * some operation is performed on it. This method will auto-close the ClientResponse
     * once the operation is complete or an exception has been thrown.
     * Any exceptions thrown by the given Functions must be handled by the caller.
     * 
     * @param remoteCall A Function that uses a Client to perform a remote call and return its resulting ClientResponse object.
     * @param responseHandler A Function that processes a ClientResponse object and returns the desired final result.
     * @return The result of invoking the responseHandler Function with the response returned by the remoteCall Function.
     */
    protected <T> T handleRemoteCall(Function<Client, ClientResponse> remoteCall, Function<ClientResponse, T> responseHandler) {
        ClientResponse response = remoteCall.apply(getClient());
        
        try {
            return responseHandler.apply(response);
        } finally {
            closeQuietly(response);
        }
    }

    protected Client getClient() {
        // Use double-checked locking to lazy-initialize the Client object, similar to related locking in Rice.
        // See effective java 2nd ed. pg. 71
        Client jerseyClient = initializedClient;
        if (jerseyClient == null) {
            synchronized (this) {
                jerseyClient = initializedClient;
                if (jerseyClient == null) {
                    ClientConfig clientConfig = buildClientConfig();
                    jerseyClient = Client.create(clientConfig);
                    initializedClient = jerseyClient;
                }
            }
        }
        return jerseyClient;
    }

    /**
     * Builds the ClientConfig that will be used for the lazy-initialized Client object.
     * The default implementation just returns a DefaultClientConfig.
     */
    protected ClientConfig buildClientConfig() {
        return new DefaultClientConfig();
    }

    protected void closeQuietly(ClientResponse response) {
        if (response != null) {
            try {
                response.close();
            } catch (Exception e) {
                // Ignore.
            }
        }
    }

    protected void closeJerseyClientQuietly() {
        // Use double-checked locking to retrieve an existing Client object, similar to related locking in Rice.
        // See effective java 2nd ed. pg. 71
        Client jerseyClient = initializedClient;
        if (jerseyClient == null) {
            synchronized (this) {
                jerseyClient = initializedClient;
            }
        }
        
        if (jerseyClient != null) {
            try {
                jerseyClient.destroy();
            } catch (Exception e) {
                // Ignore.
            }
        }
    }

}
