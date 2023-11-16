package edu.cornell.kfs.module.purap.batch.service.impl;

import edu.cornell.kfs.module.purap.batch.JaggaerXMLInputFileType;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;

@ConfigureContext
public class JaggaerXMLInputFileTypeTest extends KualiIntegTestBase {
    private static final String EXPECTED_FILE_EXTENSION_XML = "xml";
    private static final String JAGGAER_STAGING_RELATIVE_PATH = "purap/jaggaer/xml";

    private JaggaerXMLInputFileType jaggaerXMLInputFileType;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        jaggaerXMLInputFileType = SpringContext.getBean(JaggaerXMLInputFileType.class);
    }

    @Test
    public void testJaggaerXMLInputFileTypeExists() {
        assertTrue(jaggaerXMLInputFileType != null);
    }

    @Test
    public void testJaggaerXMLInputFileTypeExtension() {
        assertTrue(jaggaerXMLInputFileType.getFileExtension().equals(EXPECTED_FILE_EXTENSION_XML));
    }

    @Test
    public void testJaggaerXMLInputFileTypeDirectoryPath() {
        assertTrue(jaggaerXMLInputFileType.getDirectoryPath().endsWith(JAGGAER_STAGING_RELATIVE_PATH));
    }
}
