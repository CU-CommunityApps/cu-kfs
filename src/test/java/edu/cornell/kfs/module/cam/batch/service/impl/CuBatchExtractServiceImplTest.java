package edu.cornell.kfs.module.cam.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.gl.businessobject.Entry;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.rice.krad.bo.BusinessObject;

import edu.cornell.kfs.module.cam.CuCamsTestConstants;
import edu.cornell.kfs.module.cam.fixture.EntryFixture;
import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;
import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;
import edu.cornell.kfs.module.purap.fixture.VendorCreditMemoDocumentFixture;

@SuppressWarnings("deprecation")
public class CuBatchExtractServiceImplTest {

    private CuBatchExtractServiceImpl cuBatchExtractServiceImpl;

    @Before
    public void setUp() throws Exception {
        BusinessObjectService businessObjectService = buildMockBusinessObjectService();
        DataDictionaryService dataDictionaryService = buildMockDataDictionaryService();
        cuBatchExtractServiceImpl = new CuBatchExtractServiceImpl();
        cuBatchExtractServiceImpl.setBusinessObjectService(businessObjectService);
        cuBatchExtractServiceImpl.setDataDictionaryService(dataDictionaryService);
    }

    @Test
    public void testFindCreditMemoDocument() {
        VendorCreditMemoDocument vendorCreditMemoDocument = cuBatchExtractServiceImpl.findCreditMemoDocument(EntryFixture.VCM_TWO.createEntry());
        assertNotNull("vendor credit memo should have been non-null", vendorCreditMemoDocument);
        assertEquals("Wrong credit memo document was retrieved", CuCamsTestConstants.DOC_5686500, vendorCreditMemoDocument.getDocumentNumber());
        
        vendorCreditMemoDocument = cuBatchExtractServiceImpl.findCreditMemoDocument(EntryFixture.VCM_ONE.createEntry());
        assertNotNull("vendor credit memo should have been non-null", vendorCreditMemoDocument);
        assertEquals("Wrong credit memo document was retrieved", CuCamsTestConstants.DOC_5319793, vendorCreditMemoDocument.getDocumentNumber());
        
        vendorCreditMemoDocument = cuBatchExtractServiceImpl.findCreditMemoDocument(EntryFixture.VCM_THREE.createEntry());
        assertNull("vendor credit memo should have been null", vendorCreditMemoDocument);
    }

    @Test
    public void testFindPaymentRequestDocument() {
        PaymentRequestDocument paymentRequestDocument = cuBatchExtractServiceImpl.findPaymentRequestDocument(EntryFixture.PREQ_ONE.createEntry());
        assertNotNull("Payment request document should have been non-null", paymentRequestDocument);
        assertEquals("Wrong payment request document was retrieved", CuCamsTestConstants.DOC_5773686, paymentRequestDocument.getDocumentNumber());
        
        paymentRequestDocument = cuBatchExtractServiceImpl.findPaymentRequestDocument(EntryFixture.PREQ_THREE.createEntry());
        assertNull("Payment request document should have been null", paymentRequestDocument);
    }

    @Test
    public void testSeparatePOLines() {
        EntryFixture[] expectedFpEntries = {};
        EntryFixture[] expectedPurapEntries = { EntryFixture.VCM_ONE, EntryFixture.VCM_TWO, EntryFixture.PREQ_ONE, EntryFixture.PREQ_TWO };
        assertPOLinesAreSeparatedCorrectly(expectedFpEntries, expectedPurapEntries,
                EntryFixture.VCM_ONE, EntryFixture.VCM_TWO, EntryFixture.PREQ_ONE, EntryFixture.PREQ_TWO);
    }

    @Test
    public void testSeparatePOLinesContainingCreditMemoWithoutPurchaseOrder() {
        EntryFixture[] expectedFpEntries = { EntryFixture.VCM_FOUR };
        EntryFixture[] expectedPurapEntries = { EntryFixture.VCM_ONE, EntryFixture.PREQ_ONE, EntryFixture.PREQ_TWO };
        assertPOLinesAreSeparatedCorrectly(expectedFpEntries, expectedPurapEntries,
                EntryFixture.VCM_ONE, EntryFixture.VCM_FOUR, EntryFixture.PREQ_ONE, EntryFixture.PREQ_TWO);
    }

    private void assertPOLinesAreSeparatedCorrectly(
            EntryFixture[] expectedFpEntries, EntryFixture[] expectedPurapEntries, EntryFixture... fixtures) {
        Collection<Entry> glEntries = Stream.of(fixtures)
                .map(EntryFixture::createEntry)
                .collect(Collectors.toCollection(ArrayList::new));
        List<Entry> fpEntries = new ArrayList<>();
        List<Entry> purapEntries = new ArrayList<>();
        
        cuBatchExtractServiceImpl.separatePOLines(fpEntries, purapEntries, glEntries);
        
        assertListContainsCorrectEntries("fpEntries", expectedFpEntries, fpEntries);
        assertListContainsCorrectEntries("purapEntries", expectedPurapEntries, purapEntries);
    }

    private void assertListContainsCorrectEntries(String listName, EntryFixture[] expectedEntries, List<Entry> actualEntries) {
        assertEquals("Wrong " + listName + " list size", expectedEntries.length, actualEntries.size());
        
        for (int i = 0; i < expectedEntries.length; i++) {
            EntryFixture expectedEntry = expectedEntries[i];
            Entry actualEntry = actualEntries.get(i);
            assertEquals("Wrong document number for " + listName + " element at index " + i,
                    expectedEntry.documentNumber, actualEntry.getDocumentNumber());
            assertEquals("Wrong document type code for " + listName + " element at index " + i,
                    expectedEntry.financialDocumentTypeCode, actualEntry.getFinancialDocumentTypeCode());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private BusinessObjectService buildMockBusinessObjectService() {
        BusinessObjectService businessObjectService = EasyMock.createMock(BusinessObjectService.class);
        // Had to leave the boClassArg's inner type as a raw type, due to compiling problems from a bounded-type service method argument.
        Capture<Class> boClassArg = EasyMock.newCapture();
        Capture<Map<String, ?>> criteriaArg = EasyMock.newCapture();
        
        EasyMock.expect(
                businessObjectService.findMatching(EasyMock.<Class>capture(boClassArg), EasyMock.capture(criteriaArg)))
                .andStubAnswer(() -> findMatching(boClassArg.getValue(), criteriaArg.getValue()));
        
        EasyMock.replay(businessObjectService);
        return businessObjectService;
    }

    private <T extends BusinessObject> Collection<T> findMatching(Class<T> boClass, Map<String, ?> criteria) {
        if (criteria.size() == 1) {
            String documentNumber = (String) criteria.get(CamsPropertyConstants.DOCUMENT_NUMBER);
            if (StringUtils.isNotBlank(documentNumber)) {
                BusinessObject matchingObject = findMatchingDocument(documentNumber);
                if (matchingObject != null && boClass.isAssignableFrom(matchingObject.getClass())) {
                    return Collections.singletonList(boClass.cast(matchingObject));
                }
            }
        }
        
        return Collections.emptyList();
    }

    private BusinessObject findMatchingDocument(String documentNumber) {
        switch (documentNumber) {
            case CuCamsTestConstants.DOC_5319793 :
                return VendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO_5319793.createVendorCreditMemoDocumentForMicroTest();
            case CuCamsTestConstants.DOC_5686500 :
                return VendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO_5686500.createVendorCreditMemoDocumentForMicroTest();
            case CuCamsTestConstants.DOC_5686501 :
                return VendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO_5686501.createVendorCreditMemoDocumentForMicroTest();
            case CuCamsTestConstants.DOC_5773686 :
                return buildMinimalPaymentRequestDocument(documentNumber);
            case CuCamsTestConstants.DOC_5773687 :
                return buildMinimalPaymentRequestDocument(documentNumber);
            default :
                return null;
        }
    }

    private CuPaymentRequestDocument buildMinimalPaymentRequestDocument(String documentNumber) {
        CuPaymentRequestDocument paymentRequestDocument = EasyMock.partialMockBuilder(CuPaymentRequestDocument.class)
                .createNiceMock();
        EasyMock.replay(paymentRequestDocument);
        paymentRequestDocument.setDocumentNumber(documentNumber);
        return paymentRequestDocument;
    }

    private DataDictionaryService buildMockDataDictionaryService() {
        DataDictionaryService dataDictionaryService = EasyMock.createMock(DataDictionaryService.class);
        
        expectDocumentClassMapping(
                dataDictionaryService, PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT, CuVendorCreditMemoDocument.class);
        expectDocumentClassMapping(
                dataDictionaryService, PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT, CuPaymentRequestDocument.class);
        
        EasyMock.replay(dataDictionaryService);
        return dataDictionaryService;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void expectDocumentClassMapping(
            DataDictionaryService dataDictionaryService, String typeName, Class<? extends Document> docClass) {
        // Had to cast docClass to a raw type, due to compiling problems from the service method's bounded-wildcard return type.
        EasyMock.expect(dataDictionaryService.getDocumentClassByTypeName(typeName))
                .andStubReturn((Class) docClass);
    }

}
