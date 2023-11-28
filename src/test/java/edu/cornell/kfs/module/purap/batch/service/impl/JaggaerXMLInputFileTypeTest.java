package edu.cornell.kfs.module.purap.batch.service.impl;

import edu.cornell.kfs.module.purap.batch.JaggaerXMLInputFileType;
import edu.cornell.kfs.sys.util.LoadSpringFile;
import edu.cornell.kfs.sys.util.SpringEnabledMicroTestBase;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@LoadSpringFile("edu/cornell/kfs/module/purap/jaggaer/xml/cu-spring-jaggaer-test.xml")
public class JaggaerXMLInputFileTypeTest extends SpringEnabledMicroTestBase {
    private static final String EXPECTED_FILE_EXTENSION_XML = "xml";
    private static final String EXPECTED_AUTHOR_NAME_AJD299 = "ajd299";
    private static final String EXAMPLE_FILENAME = "jaggaerSupplierUploadFile_ajd299_20231025_092052250.xml";
    private static final String EXPECTED_FILENAME_PREFIX = "jaggaerSupplierUploadFile_ajd299_";
    private static final String JAGGAER_STAGING_RELATIVE_PATH = "purap/jaggaer/xml";
    private static final String PRINCIPAL_NAME_AJD299 = "ajd299";

    private JaggaerXMLInputFileType jaggaerXMLInputFileType;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        jaggaerXMLInputFileType = springContext.getBean(JaggaerXMLInputFileType.class);
    }

    @Test
    public void testJaggaerXMLInputFileTypeExists() {
        assertTrue(jaggaerXMLInputFileType != null);
    }

    @Test
    public void testJaggaerXMLInputFileTypeExtension() {
        assertEquals(jaggaerXMLInputFileType.getFileExtension(), EXPECTED_FILE_EXTENSION_XML);
    }

    @Test
    public void testJaggaerXMLInputFileTypeDirectoryPath() {
        assertTrue(jaggaerXMLInputFileType.getDirectoryPath().endsWith(JAGGAER_STAGING_RELATIVE_PATH));
    }

    @Test
    public void testJaggaerXMLInputFileTypeGetAuthor() {
        String relativeFilepath = jaggaerXMLInputFileType.getDirectoryPath() + "/" + EXAMPLE_FILENAME;
        File exampleFile = new File(relativeFilepath);

        String authorName = jaggaerXMLInputFileType.getAuthorPrincipalName(exampleFile);
        assertEquals(authorName, EXPECTED_AUTHOR_NAME_AJD299);
    }

    @Test
    public void testJaggaerXMLInputFileTypeGetFilename() {
        String filename = jaggaerXMLInputFileType.getFileName(PRINCIPAL_NAME_AJD299, null, "");
        assertEquals(filename.indexOf(EXPECTED_FILENAME_PREFIX), 0);
    }

}
