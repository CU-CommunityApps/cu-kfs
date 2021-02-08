package edu.cornell.kfs.sys.rest.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import edu.cornell.kfs.sys.CUKFSConstants;

/**
 * Helper class that wraps a custom JerseyTest instance that integrates with an Apache HTTP server,
 * and which allows for starting/stopping the test server on a per-test-class basis.
 * 
 * TODO: For a future enhancement, this class should be converted into a true JUnit 5 extension.
 */
public class ApacheHttpJerseyTestExtension implements Closeable {

    private Application application;
    private InternalJerseyTest jerseyHandler;

    public ApacheHttpJerseyTestExtension(Application application) {
        this.application = Objects.requireNonNull(application);
    }

    public void startUp() throws Exception {
        jerseyHandler = new InternalJerseyTest(application);
        jerseyHandler.setUp();
    }

    public void shutDown() throws Exception {
        try {
            if (jerseyHandler != null) {
                jerseyHandler.tearDown();
            }
        } finally {
            jerseyHandler = null;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            shutDown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            application = null;
        }
    }

    public Client client() {
        return jerseyHandler.client();
    }

    public WebTarget target() {
        return jerseyHandler.target();
    }

    public WebTarget target(String path) {
        return jerseyHandler.target(path);
    }

    public URI getBaseUri() {
        return jerseyHandler.getBaseUri();
    }

    public <T> T getSingletonFromApplication(Class<T> singletonClass) {
        Objects.requireNonNull(singletonClass);
        return application.getSingletons().stream()
                .filter(singletonClass::isInstance)
                .findFirst()
                .map(singletonClass::cast)
                .orElseThrow();
    }

    public String buildAbsoluteUriPath(String baseRelativeSegment, String... otherSegments) {
        return buildUriPath(Stream.of(getBaseUri().toString(), baseRelativeSegment), otherSegments);
    }

    public String buildRelativeUriPath(String baseSegment, String... otherSegments) {
        return buildUriPath(Stream.of(baseSegment), otherSegments);
    }

    private String buildUriPath(Stream<String> baseSegments, String... otherSegments) {
        return Stream.concat(baseSegments, Stream.of(otherSegments))
                .collect(Collectors.joining(CUKFSConstants.SLASH));
    }

    private static class InternalJerseyTest extends JerseyTest {
        public InternalJerseyTest(Application application) {
            super(application);
        }
        
        // Overridden to increase the method's visibility.
        @Override
        public URI getBaseUri() {
            return super.getBaseUri();
        }
        
        @Override
        protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
            return this::buildTestContainer;
        }
        
        private TestContainer buildTestContainer(URI baseUri, DeploymentContext context) {
            return new ApacheHttpTestContainer(context.getResourceConfig());
        }
    }

}
