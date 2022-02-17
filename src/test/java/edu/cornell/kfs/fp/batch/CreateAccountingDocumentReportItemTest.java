package edu.cornell.kfs.fp.batch;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateAccountingDocumentReportItemTest {
    
    private static final String FILE_NAME = "foo.xml";
    private static final String DEFAULT_WARNING_MESSAGE = "Any warning message will do";
    private static final String DOCUMENT_TYPE_DV = "DV";
    private static final String DOCUMENT_TYPE_DI = "DI";
    
    private int documentNumberIndex;
    
    CreateAccountingDocumentReportItem reportItem;

    @BeforeEach
    public void setUp() throws Exception {
        reportItem = new CreateAccountingDocumentReportItem(FILE_NAME);
        reportItem.getDocumentsInError().add(createCreateAccountingDocumentReportItemDetail(DOCUMENT_TYPE_DV, StringUtils.EMPTY));
        reportItem.getDocumentsInError().add(createCreateAccountingDocumentReportItemDetail(DOCUMENT_TYPE_DI, StringUtils.EMPTY));
        reportItem.getDocumentsSuccessfullyRouted().add(createCreateAccountingDocumentReportItemDetail(DOCUMENT_TYPE_DV, StringUtils.EMPTY));
        reportItem.getDocumentsSuccessfullyRouted().add(createCreateAccountingDocumentReportItemDetail(DOCUMENT_TYPE_DI, StringUtils.EMPTY));
        documentNumberIndex = 0;
    }

    @AfterEach
    public void tearDown() throws Exception {
        reportItem = null;
    }
    
    private CreateAccountingDocumentReportItemDetail createCreateAccountingDocumentReportItemDetail(String documentType, String warningMessage) {
        CreateAccountingDocumentReportItemDetail detail = new CreateAccountingDocumentReportItemDetail();
        detail.setIndexNumber(getNextIndexNumber());
        detail.setDocumentType(documentType);
        detail.setWarningMessage(warningMessage);
        return detail;
    }
    
    private int getNextIndexNumber() {
        documentNumberIndex++;
        return documentNumberIndex;
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
    public void testGetDocumentTypeWarningMessmageCountMapNoWarnings() {
        Map<String, Integer> docTypeCountMap = reportItem.getDocumentTypeWarningMessmageCountMap();
        assertEquals(0, docTypeCountMap.size());
    }
    
    @Test
    public void testGetDocumentTypeWarningMessmageCountMapWithWarnings() {
        addWarningDetails();
        Map<String, Integer> docTypeCountMap = reportItem.getDocumentTypeWarningMessmageCountMap();
        assertEquals(2, docTypeCountMap.size());
        assertEquals(1, docTypeCountMap.get(DOCUMENT_TYPE_DI));
        assertEquals(2, docTypeCountMap.get(DOCUMENT_TYPE_DV));
    }

}
