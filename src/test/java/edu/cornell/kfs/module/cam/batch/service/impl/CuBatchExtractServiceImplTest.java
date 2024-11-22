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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.kfs.gl.businessobject.Entry;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.document.DocumentBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import edu.cornell.kfs.module.cam.CuCamsTestConstants;
import edu.cornell.kfs.module.cam.fixture.EntryFixture;
import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;
import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;
import edu.cornell.kfs.module.purap.fixture.CuVendorCreditMemoDocumentFixture;

@SuppressWarnings("deprecation")
@RunWith(PowerMockRunner.class)
@PrepareForTest({CuVendorCreditMemoDocument.class, CuPaymentRequestDocument.class})
@PowerMockIgnore({"javax.management.*", "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*"}) 
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
        VendorCreditMemoDocument vendorCreditMemoDocument = cuBatchExtractServiceImpl.findCreditMemoDocument(EntryFixture.VCM_5319793.createEntry());
        assertNotNull("vendor credit memo should have been non-null", vendorCreditMemoDocument);
        assertEquals("Wrong credit memo document was retrieved", CuCamsTestConstants.DOC_5319793, vendorCreditMemoDocument.getDocumentNumber());
        
        vendorCreditMemoDocument = cuBatchExtractServiceImpl.findCreditMemoDocument(EntryFixture.VCM_NONEXISTENT_DOC.createEntry());
        assertNull("vendor credit memo should have been null", vendorCreditMemoDocument);
    }

    @Test
    public void testFindPaymentRequestDocument() {
        PaymentRequestDocument paymentRequestDocument = cuBatchExtractServiceImpl.findPaymentRequestDocument(EntryFixture.PREQ_5773686.createEntry());
        assertNotNull("Payment request document should have been non-null", paymentRequestDocument);
        assertEquals("Wrong payment request document was retrieved", CuCamsTestConstants.DOC_5773686, paymentRequestDocument.getDocumentNumber());
        
        paymentRequestDocument = cuBatchExtractServiceImpl.findPaymentRequestDocument(EntryFixture.PREQ_NONEXISTENT_DOC.createEntry());
        assertNull("Payment request document should have been null", paymentRequestDocument);
    }

    @Test
    public void testSeparatePOLines() {
        EntryFixture[] expectedFpEntries = {};
        EntryFixture[] expectedPurapEntries = { EntryFixture.VCM_5319793, EntryFixture.VCM_5686500, EntryFixture.PREQ_5773686, EntryFixture.PREQ_5773687 };
        assertPOLinesAreSeparatedCorrectly(expectedFpEntries, expectedPurapEntries,
                EntryFixture.VCM_5319793, EntryFixture.VCM_5686500, EntryFixture.PREQ_5773686, EntryFixture.PREQ_5773687);
    }

    @Test
    public void testSeparatePOLinesContainingCreditMemoWithoutPurchaseOrder() {
        EntryFixture[] expectedFpEntries = { EntryFixture.VCM_5686501 };
        EntryFixture[] expectedPurapEntries = { EntryFixture.VCM_5319793, EntryFixture.PREQ_5773686, EntryFixture.PREQ_5773687 };
        assertPOLinesAreSeparatedCorrectly(expectedFpEntries, expectedPurapEntries,
                EntryFixture.VCM_5319793, EntryFixture.VCM_5686501, EntryFixture.PREQ_5773686, EntryFixture.PREQ_5773687);
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

    private BusinessObjectService buildMockBusinessObjectService() {
        BusinessObjectService businessObjectService = Mockito.mock(BusinessObjectService.class);
        Mockito.when(businessObjectService.findMatching(Mockito.any(), Mockito.any())).then(this::findMatching);
        return businessObjectService;
    }
    
    private <T extends BusinessObject> Collection<T> findMatching(InvocationOnMock invocation) {
        Class<T> boClass = invocation.getArgument(0);
        Map<String, ?> criteria = invocation.getArgument(1);
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
                return CuVendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO_5319793.createVendorCreditMemoDocumentForMicroTest();
            case CuCamsTestConstants.DOC_5686500 :
                return CuVendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO_5686500.createVendorCreditMemoDocumentForMicroTest();
            case CuCamsTestConstants.DOC_5686501 :
                return CuVendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO_5686501.createVendorCreditMemoDocumentForMicroTest();
            case CuCamsTestConstants.DOC_5773686 :
                return buildMinimalPaymentRequestDocument(documentNumber);
            case CuCamsTestConstants.DOC_5773687 :
                return buildMinimalPaymentRequestDocument(documentNumber);
            default :
                return null;
        }
    }

    private CuPaymentRequestDocument buildMinimalPaymentRequestDocument(String documentNumber) {
        PowerMockito.suppress(PowerMockito.constructor(DocumentBase.class));
        CuPaymentRequestDocument paymentRequestDocument = PowerMockito.spy(new CuPaymentRequestDocument());
        paymentRequestDocument.setDocumentNumber(documentNumber);
        return paymentRequestDocument;
    }

    private DataDictionaryService buildMockDataDictionaryService() {
        DataDictionaryService dataDictionaryService = Mockito.mock(DataDictionaryService.class);
        Mockito.when(dataDictionaryService.getDocumentClassByTypeName(Mockito.anyString())).then(this::findClassByType);
        return dataDictionaryService;
    }
    
    private Class findClassByType(InvocationOnMock invocation) {
        String documentTypeName = invocation.getArgument(0);
        if (StringUtils.equalsIgnoreCase(documentTypeName, PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT)) {
            return CuVendorCreditMemoDocument.class;
        } else if (StringUtils.equalsIgnoreCase(documentTypeName, PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT)) {
            return CuPaymentRequestDocument.class;
        } else {
            throw new IllegalArgumentException("Unexpected document type: " + documentTypeName);
        }
    }

}
