package edu.cornell.kfs.tax.service.impl;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.mockito.Mockito;

import edu.cornell.kfs.tax.CUTaxConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class DocumentType1099BoxServiceImplTest {

    private DocumentType1099BoxServiceImpl documentType1099BoxService;

    @Before
    public void setUp() throws Exception {
        documentType1099BoxService = new DocumentType1099BoxServiceImpl();
    }

    @Test
    public void testSearchForMappings() throws Exception {
        setupMockParameterServiceForMappings("APCP=MISC(2)", "APLB=NEC(3)");
        assertMappingExists("APCP", "MISC(2)");
        assertMappingExists("APLB", "NEC(3)");
        assertMappingDoesNotExist("PVEN");
        assertMappingDoesNotExist(null);
        assertMappingDoesNotExist("");
    }

    @Test
    public void testInvalidMappings() throws Exception {
        setupMockParameterServiceForMappings("APLB:MISC(4)");
        try {
            documentType1099BoxService.isDocumentTypeMappedTo1099Box("APLB");
            fail("DocumentType1099BoxService should have thrown an exception due to invalid mapping configuration");
        } catch (RuntimeException e) {
        }
        
        try {
            documentType1099BoxService.getDocumentType1099Box("APLB");
            fail("DocumentType1099BoxService should have thrown an exception due to invalid mapping configuration");
        } catch (RuntimeException e) {
        }
    }

    protected void assertMappingExists(String documentTypeName, String expected1099Box) throws Exception {
        assertTrue("Document type should have been flagged as being mapped", documentType1099BoxService.isDocumentTypeMappedTo1099Box(documentTypeName));
        assertEquals("Document type does not map to the correct 1099 box",
                expected1099Box, documentType1099BoxService.getDocumentType1099Box(documentTypeName));
    }

    protected void assertMappingDoesNotExist(String documentTypeName) throws Exception {
        assertFalse("Document type should have been flagged as not being mapped", documentType1099BoxService.isDocumentTypeMappedTo1099Box(documentTypeName));
        assertNull("Document type should not have been mapped to a 1099 box", documentType1099BoxService.getDocumentType1099Box(documentTypeName));
    }

    protected void setupMockParameterServiceForMappings(String... mappings) {
        Collection<String> mappingsCollection = Arrays.asList(mappings);
        ParameterService parameterService = Mockito.mock(ParameterService.class);
        Mockito.when(parameterService.getParameterValuesAsString(CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1099_PARM_DETAIL,
                CUTaxConstants.Tax1099ParameterNames.DOCUMENT_TYPE_TO_TAX_BOX)).thenReturn(mappingsCollection);
        documentType1099BoxService.setParameterService(parameterService);
    }

}
