package edu.cornell.kfs.sys.web.mock;

import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.After;
import org.junit.Before;

public class MockLocalServerTestBase {
    public enum ProtocolScheme { http, https };

    public static final String ORIGIN = "TEST/1.1";

    protected final ProtocolScheme scheme;

    protected ServerBootstrap serverBootstrap;
    protected HttpServer server;
    protected PoolingHttpClientConnectionManager connManager;
    protected HttpClientBuilder clientBuilder;
    protected CloseableHttpClient httpclient;

    public MockLocalServerTestBase(final ProtocolScheme scheme) {
        this.scheme = scheme;
    }

    public MockLocalServerTestBase() {
        this(ProtocolScheme.http);
    }

    public String getSchemeName() {
        return this.scheme.name();
    }

    @Before
    public void setUp() throws Exception {
        final SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(15000)
                .build();
        
        
        this.serverBootstrap = ServerBootstrap.bootstrap()
                .setSocketConfig(socketConfig)
                .setServerInfo(ORIGIN)
                .registerHandler("/echo/*", new EchoHandler())
                .registerHandler("/random/*", new RandomHandler());
        if (this.scheme.equals(ProtocolScheme.https)) {
            this.serverBootstrap.setSslContext(SSLTestContexts.createServerSSLContext());
        }

        this.connManager = new PoolingHttpClientConnectionManager();
        this.clientBuilder = HttpClientBuilder.create()
                .setDefaultSocketConfig(socketConfig)
                .setConnectionManager(this.connManager);
    }

    @After
    public void shutDown() throws Exception {
        if (this.httpclient != null) {
            this.httpclient.close();
        }
        if (this.server != null) {
            this.server.shutdown(10, TimeUnit.SECONDS);
        }
    }

    public HttpHost start() throws Exception {
        this.server = this.serverBootstrap.create();
        this.server.start();

        if (this.httpclient == null) {
            this.httpclient = this.clientBuilder.build();
        }

        return new HttpHost("localhost", this.server.getLocalPort(), this.scheme.name());
    }
}
