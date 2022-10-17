package edu.cornell.kfs.sys.web.mock;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpExpectationVerifier;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;
import org.apache.http.util.Asserts;

public class MockLocalTestServer {
    public final static String ORIGIN = "LocalTestServer/1.1";

    public final static InetSocketAddress TEST_SERVER_ADDR = new InetSocketAddress("127.0.0.1", 0);

    private final UriHttpRequestHandlerMapper handlerRegistry;

    private final HttpService httpservice;

    private final SSLContext sslcontext;

    private final boolean forceSSLAuth;

    private volatile ServerSocket servicedSocket;

    private volatile ListenerThread listenerThread;

    private final Set<Worker> workers;

    private final AtomicInteger acceptedConnections = new AtomicInteger(0);

    private volatile int timeout;

    public MockLocalTestServer(final HttpProcessor proc, final ConnectionReuseStrategy reuseStrat,
            final HttpResponseFactory responseFactory, final HttpExpectationVerifier expectationVerifier,
            final SSLContext sslcontext, final boolean forceSSLAuth) {
        super();
        this.handlerRegistry = new UriHttpRequestHandlerMapper();
        this.workers = Collections.synchronizedSet(new HashSet<Worker>());

        this.httpservice = new HttpService(proc != null ? proc : newProcessor(),
                reuseStrat != null ? reuseStrat : newConnectionReuseStrategy(),
                responseFactory != null ? responseFactory : newHttpResponseFactory(), handlerRegistry,
                expectationVerifier);

        this.sslcontext = sslcontext;
        this.forceSSLAuth = forceSSLAuth;
    }

    public MockLocalTestServer(final HttpProcessor proc, final ConnectionReuseStrategy reuseStrat) {
        this(proc, reuseStrat, null, null, null, false);
    }

    public MockLocalTestServer(final SSLContext sslcontext, final boolean forceSSLAuth) {
        this(null, null, null, null, sslcontext, forceSSLAuth);
    }

    public MockLocalTestServer(final SSLContext sslcontext) {
        this(null, null, null, null, sslcontext, false);
    }

    protected HttpProcessor newProcessor() {
        return new ImmutableHttpProcessor(new HttpResponseInterceptor[] { new ResponseDate(),
                new ResponseServer(ORIGIN), new ResponseContent(), new ResponseConnControl() });
    }

    protected ConnectionReuseStrategy newConnectionReuseStrategy() {
        return DefaultConnectionReuseStrategy.INSTANCE;
    }

    protected HttpResponseFactory newHttpResponseFactory() {
        return DefaultHttpResponseFactory.INSTANCE;
    }

    public int getAcceptedConnectionCount() {
        return acceptedConnections.get();
    }

    public void registerDefaultHandlers() {
        // handlerRegistry.register("/echo/*", new EchoHandler());
        // handlerRegistry.register("/random/*", new RandomHandler());
    }

    public void register(final String pattern, final HttpRequestHandler handler) {
        handlerRegistry.register(pattern, handler);
    }

    public void unregister(final String pattern) {
        handlerRegistry.unregister(pattern);
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    public void start() throws Exception {
        Asserts.check(servicedSocket == null, "Already running");
        final ServerSocket ssock;
        if (sslcontext != null) {
            final SSLServerSocketFactory sf = sslcontext.getServerSocketFactory();
            final SSLServerSocket sslsock = (SSLServerSocket) sf.createServerSocket();
            if (forceSSLAuth) {
                sslsock.setNeedClientAuth(true);
            } else {
                sslsock.setWantClientAuth(true);
            }
            ssock = sslsock;
        } else {
            ssock = new ServerSocket();
        }

        ssock.setReuseAddress(true); // probably pointless for port '0'
        ssock.bind(TEST_SERVER_ADDR);
        servicedSocket = ssock;

        listenerThread = new ListenerThread();
        listenerThread.setDaemon(false);
        listenerThread.start();
    }

    public void stop() throws Exception {
        if (servicedSocket == null) {
            return; // not running
        }
        final ListenerThread t = listenerThread;
        if (t != null) {
            t.shutdown();
        }
        synchronized (workers) {
            for (final Worker worker : workers) {
                worker.shutdown();
            }
        }
    }

    public void awaitTermination(final long timeMs) throws InterruptedException {
        if (listenerThread != null) {
            listenerThread.join(timeMs);
        }
    }

    @Override
    public String toString() {
        final ServerSocket ssock = servicedSocket; // avoid synchronization
        final StringBuilder sb = new StringBuilder(80);
        sb.append("LocalTestServer/");
        if (ssock == null) {
            sb.append("stopped");
        } else {
            sb.append(ssock.getLocalSocketAddress());
        }
        return sb.toString();
    }

    public InetSocketAddress getServiceAddress() {
        final ServerSocket ssock = servicedSocket; // avoid synchronization
        Asserts.check(ssock != null, "Not running");
        return (InetSocketAddress) ssock.getLocalSocketAddress();
    }

    protected DefaultBHttpServerConnection createHttpServerConnection() {
        return new DefaultBHttpServerConnection(8 * 1024);
    }

    class ListenerThread extends Thread {

        private volatile Exception exception;

        ListenerThread() {
            super();
        }

        @Override
        public void run() {
            try {
                while (!interrupted()) {
                    final Socket socket = servicedSocket.accept();
                    acceptedConnections.incrementAndGet();
                    final DefaultBHttpServerConnection conn = createHttpServerConnection();
                    conn.bind(socket);
                    conn.setSocketTimeout(timeout);
                    // Start worker thread
                    final Worker worker = new Worker(conn);
                    workers.add(worker);
                    worker.setDaemon(true);
                    worker.start();
                }
            } catch (final Exception ex) {
                this.exception = ex;
            } finally {
                try {
                    servicedSocket.close();
                } catch (final IOException ignore) {
                }
            }
        }

        public void shutdown() {
            interrupt();
            try {
                servicedSocket.close();
            } catch (final IOException ignore) {
            }
        }

        public Exception getException() {
            return this.exception;
        }

    }

    class Worker extends Thread {

        private final HttpServerConnection conn;

        private volatile Exception exception;

        public Worker(final HttpServerConnection conn) {
            this.conn = conn;
        }

        @Override
        public void run() {
            /*
             * \ public void handleRequest( final HttpServerConnection conn, final
             * HttpContext context) throws IOException, HttpException {
             */

            final HttpContext context = new BasicHttpContext();
            try {
                while (this.conn.isOpen() && !Thread.interrupted()) {
                    httpservice.handleRequest(this.conn, context);
                }
            } catch (final Exception ex) {
                this.exception = ex;
            } finally {
                workers.remove(this);
                try {
                    this.conn.shutdown();
                } catch (final IOException ignore) {
                }
            }
        }

        public void shutdown() {
            interrupt();
            try {
                this.conn.shutdown();
            } catch (final IOException ignore) {
            }
        }

        public Exception getException() {
            return this.exception;
        }

    }
}
