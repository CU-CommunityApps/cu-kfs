package org.kuali.kfs.sys.context;

import java.util.Properties;

import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.impl.config.property.JAXBConfigImpl;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class KFSTestStartup {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(KFSTestStartup.class);

    private static ClassPathXmlApplicationContext context;

    public static void initializeKfsTestContext() {
        long startInit = System.currentTimeMillis();
        LOG.info("Initializing Kuali Rice Application...");

        String bootstrapSpringBeans = "kfs-startup-test.xml";

        Properties baseProps = new Properties();
        baseProps.putAll(System.getProperties());
        JAXBConfigImpl config = new JAXBConfigImpl(baseProps);
        ConfigContext.init(config);

        context = new ClassPathXmlApplicationContext();
        context.setConfigLocation(bootstrapSpringBeans);
        try {
            context.refresh();
        } catch (Exception e) {
            LOG.error("problem during context.refresh()", e);

            throw e;
        }

        context.start();
        long endInit = System.currentTimeMillis();
        LOG.info("...Kuali Rice Application successfully initialized, startup took " + (endInit - startInit) + " ms.");

        SpringContext.finishInitializationAfterRiceStartup();
    }
}

