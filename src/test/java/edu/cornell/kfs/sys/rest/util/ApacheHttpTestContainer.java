package edu.cornell.kfs.sys.rest.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.localserver.LocalServerTestBase;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.test.spi.TestContainer;

import edu.cornell.kfs.sys.CUKFSConstants;

public class ApacheHttpTestContainer implements TestContainer {

    private static final int THREAD_POOL_SIZE = 4;
    private static final String ALL_PATHS_PATTERN = "/*";

    private LocalHttpTestServer httpServer;
    private ApplicationHandler applicationHandler;
    private ScheduledExecutorService scheduledExecutorService;

    public ApacheHttpTestContainer(Application application) {
        this.httpServer = new LocalHttpTestServer();
        this.applicationHandler = new ApplicationHandler(application);
        this.scheduledExecutorService = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
    }

    @Override
    public URI getBaseUri() {
        if (!httpServer.hasServerStarted()) {
            throw new IllegalStateException("This container only supports URI retrieval after startup");
        }
        return URI.create(httpServer.getServerUri());
    }

    @Override
    public ClientConfig getClientConfig() {
        return null;
    }

    @Override
    public void start() {
        try {
            httpServer.start(this::handleApacheHttpRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            httpServer.shutDown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleApacheHttpRequest(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {
        try (
                ApacheHttpResponseWriter responseWriter = new ApacheHttpResponseWriter(
                        response, scheduledExecutorService);
                InputStream entityStream = getEntityStream(request);
        ) {
            RequestLine requestLine = request.getRequestLine();
            URI baseUri = new URI(httpServer.getServerUri() + CUKFSConstants.SLASH);
            URI requestUri = new URI(httpServer.getServerUri() + requestLine.getUri());
            
            ContainerRequest containerRequest = new ContainerRequest(
                    baseUri, requestUri, requestLine.getMethod(), new NonAuthenticatedSecurityContext(),
                    new MapPropertiesDelegate());
            containerRequest.setEntityStream(entityStream);
            containerRequest.headers(buildHeadersMap(request));
            containerRequest.setWriter(responseWriter);
            
            applicationHandler.handle(containerRequest);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        }
    }

    private InputStream getEntityStream(HttpRequest request) throws IOException {
        if (request instanceof HttpEntityEnclosingRequest) {
            return ((HttpEntityEnclosingRequest) request).getEntity().getContent();
        } else {
            return InputStream.nullInputStream();
        }
    }

    private Map<String, List<String>> buildHeadersMap(HttpRequest request) {
        Map<String, List<String>> headersMap = new HashMap<>();
        for (Header header : request.getAllHeaders()) {
            List<String> values = headersMap.computeIfAbsent(header.getName(), headerName -> new ArrayList<>());
            values.add(header.getValue());
        }
        return headersMap;
    }

    private static class LocalHttpTestServer extends LocalServerTestBase {

        private HttpHost httpHost;

        public boolean hasServerStarted() {
            return httpHost != null;
        }

        public String getServerUri() {
            if (!hasServerStarted()) {
                throw new IllegalStateException("This server must be running before returning the URI");
            }
            return httpHost.toURI();
        }

        public void start(HttpRequestHandler requestHandler) throws Exception {
            setUp();
            this.serverBootstrap.registerHandler(ALL_PATHS_PATTERN, requestHandler);
            this.httpHost = start();
        }

        @Override
        public void shutDown() throws Exception {
            this.httpHost = null;
            if (this.httpclient != null) {
                this.httpclient.close();
            }
            if (this.server != null) {
                this.server.shutdown(0L, TimeUnit.SECONDS);
            }
        }
    }

    private static class NonAuthenticatedSecurityContext implements SecurityContext {
        @Override
        public Principal getUserPrincipal() {
            return null;
        }

        @Override
        public boolean isUserInRole(String role) {
            return false;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public String getAuthenticationScheme() {
            return null;
        }
    }

}
