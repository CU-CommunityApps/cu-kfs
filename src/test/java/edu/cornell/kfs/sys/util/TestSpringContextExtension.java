package edu.cornell.kfs.sys.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

/**
 * JUnit 5 extension for micro-tests that need to use a Spring context for loading some KFS runtime beans,
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
 * In addition, this extension will programmatically register the current unit test instance as a bean
 * named "testBeanFactory". This allows using the unit test as a bean factory in the Spring XML file,
 * making it easier to create mock versions of service beans. Also, to help clarify which methods
 * on the unit test instance are intended to be used as bean factory methods, they can be annotated
 * with the "SpringXmlBeanFactoryMethod" annotation.
 * 
 * This implementation currently does not inject Spring beans or Spring contexts into the test class.
 * To retrieve a specific Spring bean, invoke this class's "getBean()" helper method.
 */
public class TestSpringContextExtension implements BeforeEachCallback, AfterEachCallback {

    public static final String TEST_BEAN_FACTORY_NAME = "testBeanFactory";
    public static final String CLASSPATH_PREFIX = "classpath:";

    private final String classPathSpringXmlFile;

    private StaticApplicationContext testBeanFactoryContext;
    private ClassPathXmlApplicationContext springContext;

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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        final Object testInstance = context.getRequiredTestInstance();
        final Class<?> testClass = context.getRequiredTestClass();

        testBeanFactoryContext = new StaticApplicationContext();
        testBeanFactoryContext.registerBean(
                TEST_BEAN_FACTORY_NAME, (Class) testClass, () -> testInstance);
        testBeanFactoryContext.refresh();

        final String[] configLocations = { classPathSpringXmlFile };
        springContext = new ClassPathXmlApplicationContext(configLocations, testBeanFactoryContext);
    }

    public <T> T getBean(final String name, final Class<T> requiredType) {
        if (springContext == null) {
            throw new IllegalStateException("Extension's Spring context has not been initialized");
        }
        return springContext.getBean(name, requiredType);
    }

    @Override
    public void afterEach(final ExtensionContext context) throws Exception {
        IOUtils.closeQuietly(springContext);
        IOUtils.closeQuietly(testBeanFactoryContext);
        springContext = null;
        testBeanFactoryContext = null;
    }

}
