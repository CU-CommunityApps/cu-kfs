package edu.cornell.kfs.sys.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Base class for micro-tests that need to use a Spring context for loading some KFS runtime beans,
 * yet still avoid loading beans that are out of scope for the test at hand.
 * 
 * Subclasses must specify the "LoadSpringFile" annotation to indicate the base Spring file
 * that should be loaded for the test. The file is assumed to be in the project's classpath.
 * 
 * The test Spring file should import "cu-spring-base-test-beans.xml" so that the appropriate
 * bean post-processor will remove any unnecessary beans. To configure the set of beans
 * to preserve for the test, override the "beanFilterPostProcessor" bean and merge-override
 * the "beanWhitelist" property to specify the IDs of the beans to preserve.
 */
public abstract class SpringEnabledMicroTestBase {

    protected ClassPathXmlApplicationContext springContext;

    @Before
    public void setUp() throws Exception {
        LoadSpringFile springFileAnnotation = getClass().getAnnotation(LoadSpringFile.class);
        if (springFileAnnotation == null) {
            throw new IllegalStateException("Unit test class " + getClass().getName() + " is missing the required "
                    + LoadSpringFile.class.getSimpleName() + " annotation");
        } else if (StringUtils.isBlank(springFileAnnotation.value())) {
            throw new IllegalStateException("Unit test class " + getClass().getName() + " cannot have a blank value on its "
                    + LoadSpringFile.class.getSimpleName() + " annotation");
        }
        springContext = new ClassPathXmlApplicationContext(springFileAnnotation.value());
    }

    @After
    public void tearDown() throws Exception {
        IOUtils.closeQuietly(springContext);
    }

}
