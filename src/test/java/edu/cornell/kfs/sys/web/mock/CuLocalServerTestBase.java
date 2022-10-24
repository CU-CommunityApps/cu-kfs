package edu.cornell.kfs.sys.web.mock;

import java.io.IOException;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.function.Decorator;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.io.HttpServerRequestHandler;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.testing.classic.ClassicTestServer;
import org.apache.hc.core5.util.Timeout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;

public abstract class CuLocalServerTestBase {
    private static final Logger LOG = LogManager.getLogger();

    protected ClassicTestServer server;
    protected final URIScheme scheme;
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
        SSLContext sslContext = null;
        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(TIMEOUT).build();
        server = new ClassicTestServer(sslContext, socketConfig);
        
        connManager = new PoolingHttpClientConnectionManager();
        connManager.setDefaultSocketConfig(socketConfig);
    }

    @After
    public void shutDown() throws Exception {
        if (server != null) {
            try {
                server.shutdown(CloseMode.IMMEDIATE);
                server = null;
            } catch (final Exception e) {
                LOG.error("shutDown, had an error", e);
            }
        }
    }

    public HttpHost start(final Http1Config http1Config, final HttpProcessor httpProcessor,
            final Decorator<HttpServerRequestHandler> handlerDecorator) throws IOException {
        this.server.start(http1Config, httpProcessor, handlerDecorator);

        return new HttpHost(this.scheme.name(), "localhost", this.server.getPort());
    }

    public HttpHost start(final HttpProcessor httpProcessor, final Decorator<HttpServerRequestHandler> handlerDecorator)
            throws IOException {
        return start(null, httpProcessor, handlerDecorator);
    }

    public HttpHost start() throws Exception {
        return start(null, null, null);
    }

}
