package edu.cornell.kfs.sys.web.mock;

import java.io.IOException;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.function.Decorator;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.io.HttpServerRequestHandler;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.Closer;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.testing.classic.ClassicTestServer;
import org.apache.hc.core5.util.Timeout;
import org.junit.After;
import org.junit.Before;

public class CuLocalServerTestBase {

    protected ClassicTestServer server;
    protected final URIScheme scheme;
    protected HttpClientBuilder clientBuilder;
    protected CloseableHttpClient httpclient;
    protected PoolingHttpClientConnectionManager connManager;
    
    public static final Timeout TIMEOUT = Timeout.ofMinutes(1);

    public CuLocalServerTestBase(URIScheme scheme) {
        this.scheme = scheme;
    }

    public CuLocalServerTestBase() {
        this(URIScheme.HTTP);
    }

    @Before
    public void setUp() throws Exception {
        server = new ClassicTestServer(scheme == URIScheme.HTTPS ? SSLTestContexts.createServerSSLContext() : null,
                SocketConfig.custom().setSoTimeout(TIMEOUT).build());
        
        connManager = new PoolingHttpClientConnectionManager();
        connManager.setDefaultSocketConfig(SocketConfig.custom()
                .setSoTimeout(TIMEOUT)
                .build());
        /*
        connManager.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setConnectTimeout(TIMEOUT)
                .build()); */
        
        clientBuilder = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(TIMEOUT)
                        .build())
                .setConnectionManager(connManager);
    }

    @After
    public void shutDown() throws Exception {
        if (server != null) {
            try {
                server.shutdown(CloseMode.IMMEDIATE);
                server = null;
            } catch (final Exception ignore) {
            }
        }
        
        Closer.closeQuietly(httpclient);
        httpclient = null;
    }

    public HttpHost start(final Http1Config http1Config, final HttpProcessor httpProcessor,
            final Decorator<HttpServerRequestHandler> handlerDecorator) throws IOException {
        this.server.start(http1Config, httpProcessor, handlerDecorator);

        if (this.httpclient == null) {
            this.httpclient = this.clientBuilder.build();
        }

        return new HttpHost(this.scheme.name(), "localhost", this.server.getPort());
    }

    public HttpHost start(final HttpProcessor httpProcessor, final Decorator<HttpServerRequestHandler> handlerDecorator)
            throws IOException {
        return start(null, httpProcessor, handlerDecorator);
    }

    public HttpHost start() throws Exception {
        return start(null, null, null);
    }

    protected static class SSLTestContexts {

        public static SSLContext createServerSSLContext() throws Exception {
            return SSLContexts.custom()
                    .loadTrustMaterial(SSLTestContexts.class.getResource("/test.keystore"), "nopassword".toCharArray())
                    .loadKeyMaterial(SSLTestContexts.class.getResource("/test.keystore"), "nopassword".toCharArray(),
                            "nopassword".toCharArray())
                    .build();
        }

        public static SSLContext createClientSSLContext() throws Exception {
            return SSLContexts.custom()
                    .loadTrustMaterial(SSLTestContexts.class.getResource("/test.keystore"), "nopassword".toCharArray())
                    .build();
        }

    }

}
