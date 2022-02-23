package edu.cornell.kfs.fp.batch;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.sys.KFSConstants;

class CreateAccountingDocumentReportItemTest {
    
    private static final String FILE_NAME = "foo.xml";
    private static final String DEFAULT_WARNING_MESSAGE = "Any warning message will do";
    private static final String DOCUMENT_TYPE_DV = "DV";
    private static final String DOCUMENT_TYPE_DI = "DI";
    private static final String DOCUMENT_TYPE_SB = "SB";
    private static final String TWO_SPACES = KFSConstants.BLANK_SPACE + KFSConstants.BLANK_SPACE;
    
    CreateAccountingDocumentReportItem reportItem;

    @BeforeEach
    public void setUp() throws Exception {
        reportItem = new CreateAccountingDocumentReportItem(FILE_NAME);
        reportItem.getDocumentsInError().add(createCreateAccountingDocumentReportItemDetail(DOCUMENT_TYPE_DV, StringUtils.EMPTY));
        reportItem.getDocumentsInError().add(createCreateAccountingDocumentReportItemDetail(DOCUMENT_TYPE_DI, TWO_SPACES));
        reportItem.getDocumentsSuccessfullyRouted().add(createCreateAccountingDocumentReportItemDetail(DOCUMENT_TYPE_DV, TWO_SPACES));
        reportItem.getDocumentsSuccessfullyRouted().add(createCreateAccountingDocumentReportItemDetail(DOCUMENT_TYPE_DI, StringUtils.EMPTY));
    }

    @AfterEach
    public void tearDown() throws Exception {
        reportItem = null;
    }
    
    private CreateAccountingDocumentReportItemDetail createCreateAccountingDocumentReportItemDetail(String documentType, String warningMessage) {
        CreateAccountingDocumentReportItemDetail detail = new CreateAccountingDocumentReportItemDetail();
        detail.setDocumentType(documentType);
        detail.setWarningMessage(warningMessage);
        return detail;
    }
    
    private void addWarningDetails() {
        reportItem.getDocumentsInError().add(createCreateAccountingDocumentReportItemDetail(DOCUMENT_TYPE_DV, DEFAULT_WARNING_MESSAGE));
        reportItem.getDocumentsInError().add(createCreateAccountingDocumentReportItemDetail(DOCUMENT_TYPE_DI, DEFAULT_WARNING_MESSAGE));
        reportItem.getDocumentsSuccessfullyRouted().add(createCreateAccountingDocumentReportItemDetail(DOCUMENT_TYPE_DV, DEFAULT_WARNING_MESSAGE));
    }

    @Test
    public void testGetAllDocumentDetails() {
        assertEquals(4, reportItem.getAllDocumentDetails().size());
    }
    
    @Test
    public void testGetDocumentDetailsWithWarningsNoWarnings() {
        assertEquals(0, reportItem.getDocumentDetailsWithWarnings().size());
    }
    
    @Test
    public void testGetDocumentDetailsWithWarningsWithWarnings() {
        addWarningDetails();
        assertEquals(3, reportItem.getDocumentDetailsWithWarnings().size());
    }
    
    @Test
    public void testGetDocumentTypeWarningMessageCountMap_NoWarnings() {
        Map<String, Integer> docTypeCountMap = reportItem.getDocumentTypeWarningMessageCountMap();
        assertEquals(0, docTypeCountMap.size());
    }
    
    @Test
    public void testGetDocumentTypeWarningMessageCountMap_WithWarnings() {
        addWarningDetails();
        Map<String, Integer> docTypeCountMap = reportItem.getDocumentTypeWarningMessageCountMap();
        assertEquals(2, docTypeCountMap.size());
        assertEquals(1, docTypeCountMap.get(DOCUMENT_TYPE_DI));
        assertEquals(2, docTypeCountMap.get(DOCUMENT_TYPE_DV));
    }
    
    @Test
    public void testGetDocumentTypeWarningMessageCountMap_WithWarningsDoubled() {
        addWarningDetails();
        addWarningDetails();
        reportItem.getDocumentsInError().add(createCreateAccountingDocumentReportItemDetail(DOCUMENT_TYPE_SB, TWO_SPACES));
        Map<String, Integer> docTypeCountMap = reportItem.getDocumentTypeWarningMessageCountMap();
        assertEquals(2, docTypeCountMap.size());
        assertEquals(2, docTypeCountMap.get(DOCUMENT_TYPE_DI));
        assertEquals(4, docTypeCountMap.get(DOCUMENT_TYPE_DV));
    }

}
