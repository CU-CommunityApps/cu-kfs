package edu.cornell.kfs.sys.web.mock;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.hc.core5.http.HttpHost;

public class MockRemoteServerExtension implements Closeable {
    private LocalServer localServer;
    private Optional<String> serverUrl;

    public MockRemoteServerExtension() {
        this.localServer = new LocalServer();
        this.serverUrl = Optional.empty();
    }

    public void initialize(MockServiceCore5EndpointBase... endpoints) throws Exception {
        initialize(Arrays.asList(endpoints));
    }

    public void initialize(List<? extends MockServiceCore5EndpointBase> endpoints) throws Exception {
        String baseUrl = localServer.configureAndLaunchServer(endpoints);
        serverUrl = Optional.of(baseUrl);
        for (MockServiceCore5EndpointBase endpoint : endpoints) {
            endpoint.onServerInitialized(baseUrl);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            localServer.shutDown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            localServer = null;
            serverUrl = null;
        }
    }

    public Optional<String> getServerUrl() {
        return serverUrl;
    }

    private static class LocalServer extends CuLocalServerTestBase {
        
        public String configureAndLaunchServer(List<? extends MockServiceCore5EndpointBase> endpoints) throws Exception {
            setUp();
            for (MockServiceCore5EndpointBase endpoint : endpoints) {
                server.registerHandler(endpoint.getRelativeUrlPatternForHandlerRegistration(), endpoint);
            }
            HttpHost httpHost = start();
            return httpHost.toURI();
        }
        
        @Override
        public void shutDown() throws Exception {
            super.shutDown();
        }
    }

}
