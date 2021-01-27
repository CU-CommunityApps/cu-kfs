package edu.cornell.kfs.sys.rest.util;

import java.net.URI;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import edu.cornell.kfs.sys.CUKFSConstants;

/**
 * Custom JerseyTest subclass that will handle web requests via an Apache HTTP server.
 * 
 * For easier access to the Application instance that will be deployed, an "application" variable
 * will be auto-populated by this class. To pass in the appropriate Application, subclasses can either
 * call the constructor that accepts an Application, or can call the default constructor and then override
 * the superclass's configure() method that the default super-constructor will invoke. (Note that in order
 * for this to work correctly in all cases, any subclass overrides of the configureDeployment() method
 * MUST call super.configureDeployment() as well. Also note that the default constructor may place
 * a wrapper Application around the subclass-provided one.)
 * 
 * Unlike JerseyTest, this class takes full control of the TestContainerFactory setup.
 * Thus, the constructor that takes TestContainerFactory as an input is not supported.
 */
public abstract class ApacheHttpJerseyTestBase extends JerseyTest {

    protected Application application;

    public ApacheHttpJerseyTestBase() {
        super();
    }

    public ApacheHttpJerseyTestBase(Application application) {
        super(application);
        this.application = application;
    }

    @Override
    protected DeploymentContext configureDeployment() {
        DeploymentContext context = super.configureDeployment();
        this.application = context.getResourceConfig();
        return context;
    }

    @Override
    protected final TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return this::createTestContainer;
    }

    protected TestContainer createTestContainer(URI baseUri, DeploymentContext context) {
        return new ApacheHttpTestContainer(context.getResourceConfig());
    }

    protected <T> T getSingletonFromApplication(Class<T> singletonClass) {
        Objects.requireNonNull(singletonClass);
        if (application == null) {
            throw new IllegalStateException("The Application reference was not initialized properly");
        }
        return application.getSingletons().stream()
                .filter(singletonClass::isInstance)
                .findFirst()
                .map(singletonClass::cast)
                .orElseThrow();
    }

    protected String buildAbsoluteUriPath(String baseRelativeSegment, String... otherSegments) {
        return buildUriPath(Stream.of(getBaseUri().toString(), baseRelativeSegment), otherSegments);
    }

    protected String buildRelativeUriPath(String baseSegment, String... otherSegments) {
        return buildUriPath(Stream.of(baseSegment), otherSegments);
    }

    protected String buildUriPath(Stream<String> baseSegments, String... otherSegments) {
        return Stream.concat(baseSegments, Stream.of(otherSegments))
                .collect(Collectors.joining(CUKFSConstants.SLASH));
    }

    public static abstract class ForJUnit5 extends ApacheHttpJerseyTestBase {

        public ForJUnit5() {
            super();
        }

        public ForJUnit5(Application application) {
            super(application);
        }

        @BeforeEach
        public void setUp() throws Exception {
            super.setUp();
        }

        @AfterEach
        public void tearDown() throws Exception {
            super.tearDown();
        }
    }

}
