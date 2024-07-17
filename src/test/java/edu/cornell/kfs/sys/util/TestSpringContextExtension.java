package edu.cornell.kfs.sys.util;

import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * JUnit extension for micro-tests that need to use a Spring context for loading some KFS runtime beans,
 * yet still avoid loading beans that are out of scope for the test at hand.
 * 
 * This is intended to be used as a *programmatically-configured* extension. To specify which Spring XML file
 * to load (relative to the classpath), pass it to either the constructor or the static forClassPathSpringXmlFile()
 * helper method.
 * 
 * The test Spring file should import "cu-spring-base-test-beans.xml" so that the appropriate
 * bean post-processor will remove any unnecessary beans. To configure the set of beans
 * to preserve for the test, override the "beanFilterPostProcessor" bean and merge-override
 * the "beanWhitelist" property to specify the IDs of the beans to preserve.
 * 
 * This extension's lifecycle behavior depends on what level it is defined at in the unit test class:
 * 
 * -- If it's defined at the INSTANCE level, the Spring context will be created at the Before-Each
 *    phase and destroyed at the After-Each phase.
 * 
 * -- If it's defined at the CLASS level, the Spring context will be created at the Before-All phase
 *    and destroyed at the After-All phase.
 * 
 * In both cases, the current unit test classname will be programmatically registered as a property
 * named "unit.test.classname" (by means of a separate placeholder configurer bean that this class will
 * automatically set up). This makes it easier for unit test classes to prepare static methods that function
 * as bean factory methods. To help clarify which methods on the unit test class are intended to be used
 * as bean factory methods, they can be annotated with the CU-specific "SpringXmlBeanFactoryMethod" annotation.
 * 
 * This implementation currently does not inject Spring beans or Spring contexts into the test class.
 * To retrieve a specific Spring bean during a test run, invoke this class's "getBean()" helper method.
 */
public class TestSpringContextExtension implements BeforeEachCallback, AfterEachCallback,
        BeforeAllCallback, AfterAllCallback {

    public static final String UNIT_TEST_CLASSNAME_PROPERTY = "unit.test.classname";
    public static final String CLASSPATH_PREFIX = "classpath:";

    private final String classPathSpringXmlFile;

    private ClassPathXmlApplicationContext springXmlContext;
    private boolean staticExtension;

    public TestSpringContextExtension(final String classPathSpringXmlFile) {
        if (StringUtils.isBlank(classPathSpringXmlFile)) {
            throw new IllegalArgumentException("classPathSpringFile cannot be blank");
        } else if (StringUtils.equalsIgnoreCase(classPathSpringXmlFile, CLASSPATH_PREFIX)) {
            throw new IllegalArgumentException("classPathSpringFile has no file path after the 'classpath:' prefix");
        }
        this.classPathSpringXmlFile = StringUtils.startsWithIgnoreCase(classPathSpringXmlFile, CLASSPATH_PREFIX)
                ? StringUtils.substring(classPathSpringXmlFile, CLASSPATH_PREFIX.length())
                : classPathSpringXmlFile;
    }

    public static TestSpringContextExtension forClassPathSpringXmlFile(final String xmlSpringFile) {
        return new TestSpringContextExtension(xmlSpringFile);
    }

    @Override
    public void beforeAll(final ExtensionContext junitContext) throws Exception {
        staticExtension = true;
        initializeSpringContext(junitContext);
    }

    @Override
    public void beforeEach(final ExtensionContext junitContext) throws Exception {
        if (!staticExtension) {
            initializeSpringContext(junitContext);
        }
    }

    private void initializeSpringContext(final ExtensionContext junitContext) {
        final Class<?> testClass = junitContext.getRequiredTestClass();
        final PropertySourcesPlaceholderConfigurer propertyConfigurer =
                createBeanPostProcessorForResolvingUnitTestClass(testClass);

        springXmlContext = new ClassPathXmlApplicationContext();
        springXmlContext.addBeanFactoryPostProcessor(propertyConfigurer);
        springXmlContext.setConfigLocation(classPathSpringXmlFile);
        springXmlContext.refresh();
    }

    private PropertySourcesPlaceholderConfigurer createBeanPostProcessorForResolvingUnitTestClass(
            final Class<?> testClass) {
        final Properties properties = new Properties();
        properties.setProperty(UNIT_TEST_CLASSNAME_PROPERTY, testClass.getName());

        final PropertySourcesPlaceholderConfigurer propertyConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertyConfigurer.setProperties(properties);
        propertyConfigurer.setIgnoreUnresolvablePlaceholders(true);
        return propertyConfigurer;
    }

    public <T> T getBean(final String name, final Class<T> requiredType) {
        if (springXmlContext == null) {
            throw new IllegalStateException("Extension's Spring context has not been initialized");
        }
        return springXmlContext.getBean(name, requiredType);
    }

    @Override
    public void afterEach(final ExtensionContext junitContext) throws Exception {
        if (!staticExtension) {
            destroySpringContext();
        }
    }

    @Override
    public void afterAll(final ExtensionContext junitContext) throws Exception {
        if (staticExtension) {
            destroySpringContext();
        }
    }

    private void destroySpringContext() {
        IOUtils.closeQuietly(springXmlContext);
        springXmlContext = null;
    }

}
