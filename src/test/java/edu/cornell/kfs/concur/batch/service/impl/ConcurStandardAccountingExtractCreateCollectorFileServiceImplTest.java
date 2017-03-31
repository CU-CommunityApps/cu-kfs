package edu.cornell.kfs.concur.batch.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.batch.CollectorBatch;
import org.kuali.kfs.gl.batch.CollectorBatchHeaderFieldUtil;
import org.kuali.kfs.gl.batch.CollectorBatchTrailerRecordFieldUtil;
import org.kuali.kfs.gl.businessobject.OriginEntryFieldUtil;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.fixture.ConcurCollectorBatchFixture;

public class ConcurStandardAccountingExtractCreateCollectorFileServiceImplTest {

    protected static final String GET_FIELD_LENGTH_MAP_METHOD = "getFieldLengthMap";

    protected ConcurStandardAccountingExtractCollectorBatchBuilder createMockBatchBuilder() {
        return createMockObject(ConcurStandardAccountingExtractCollectorBatchBuilder.class, (builder) -> {
            Capture<Integer> sequenceNumberArg = EasyMock.newCapture();
            Capture<ConcurStandardAccountingExtractFile> saeFileContentsArg = EasyMock.newCapture();
            EasyMock.expect(
                    builder.buildCollectorBatchFromStandardAccountingExtract(
                            EasyMock.captureInt(sequenceNumberArg), EasyMock.capture(saeFileContentsArg)))
                    .andStubAnswer(() -> buildFixtureBasedCollectorBatch(sequenceNumberArg.getValue(), saeFileContentsArg.getValue()));
        });
    }

    protected CollectorBatch buildFixtureBasedCollectorBatch(Integer sequenceNumber, ConcurStandardAccountingExtractFile saeFileContents) {
        String fixtureConstantName = StringUtils.removeEndIgnoreCase(
                saeFileContents.getOriginalFileName(), GeneralLedgerConstants.BatchFileSystem.TEXT_EXTENSION);
        ConcurCollectorBatchFixture fixture;
        
        try {
            fixture = ConcurCollectorBatchFixture.valueOf(fixtureConstantName);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
        
        CollectorBatch collectorBatch = fixture.toCollectorBatch();
        collectorBatch.setBatchSequenceNumber(sequenceNumber);
        return collectorBatch;
    }

    protected <T> T createMockObject(Class<T> objectClass, Consumer<T> objectConfigurer) {
        T mockObject = EasyMock.createMock(objectClass);
        objectConfigurer.accept(mockObject);
        EasyMock.replay(mockObject);
        return mockObject;
    }

    protected CollectorBatchHeaderFieldUtil createMockBatchHeaderFieldUtil() {
        Map<String,Integer> fieldLengthMap = new HashMap<>();
        fieldLengthMap.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, 4);
        fieldLengthMap.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, 2);
        fieldLengthMap.put(KFSPropertyConstants.ORGANIZATION_CODE, 4);
        fieldLengthMap.put(KFSPropertyConstants.TRANSMISSION_DATE, 15);
        fieldLengthMap.put(KFSPropertyConstants.COLLECTOR_BATCH_RECORD_TYPE, 2);
        fieldLengthMap.put(KFSPropertyConstants.BATCH_SEQUENCE_NUMBER, 1);
        fieldLengthMap.put(KFSPropertyConstants.KUALI_USER_PERSON_EMAIL_ADDRESS, 40);
        fieldLengthMap.put(KFSPropertyConstants.COLLECTOR_BATCH_PERSON_USER_ID, 30);
        fieldLengthMap.put(KFSPropertyConstants.DEPARTMENT_NAME, 30);
        fieldLengthMap.put(KFSPropertyConstants.MAILING_ADDRESS, 30);
        fieldLengthMap.put(KFSPropertyConstants.CAMPUS_CODE, 2);
        fieldLengthMap.put(KFSPropertyConstants.PHONE_NUMBER, 10);
        
        return createMockParserFieldUtils(CollectorBatchHeaderFieldUtil.class, fieldLengthMap);
    }

    protected OriginEntryFieldUtil createMockOriginEntryFieldUtil() {
        Map<String,Integer> fieldLengthMap = new HashMap<>();
        fieldLengthMap.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, 4);
        fieldLengthMap.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, 2);
        fieldLengthMap.put(KFSPropertyConstants.ACCOUNT_NUMBER, 7);
        fieldLengthMap.put(KFSPropertyConstants.SUB_ACCOUNT_NUMBER, 5);
        fieldLengthMap.put(KFSPropertyConstants.FINANCIAL_OBJECT_CODE, 4);
        fieldLengthMap.put(KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE, 3);
        fieldLengthMap.put(KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE, 2);
        fieldLengthMap.put(KFSPropertyConstants.FINANCIAL_OBJECT_TYPE_CODE, 2);
        fieldLengthMap.put(KFSPropertyConstants.UNIVERSITY_FISCAL_PERIOD_CODE, 2);
        fieldLengthMap.put(KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE, 4);
        fieldLengthMap.put(KFSPropertyConstants.FINANCIAL_SYSTEM_ORIGINATION_CODE, 2);
        fieldLengthMap.put(KFSPropertyConstants.DOCUMENT_NUMBER, 14);
        fieldLengthMap.put(KFSPropertyConstants.TRANSACTION_ENTRY_SEQUENCE_NUMBER, 5);
        fieldLengthMap.put(KFSPropertyConstants.TRANSACTION_LEDGER_ENTRY_DESC, 40);
        fieldLengthMap.put(KFSPropertyConstants.TRANSACTION_LEDGER_ENTRY_AMOUNT, 21);
        fieldLengthMap.put(KFSPropertyConstants.TRANSACTION_DEBIT_CREDIT_CODE, 1);
        fieldLengthMap.put(KFSPropertyConstants.TRANSACTION_DATE, 10);
        fieldLengthMap.put(KFSPropertyConstants.ORGANIZATION_DOCUMENT_NUMBER, 10);
        fieldLengthMap.put(KFSPropertyConstants.PROJECT_CODE, 10);
        fieldLengthMap.put(KFSPropertyConstants.ORGANIZATION_REFERENCE_ID, 8);
        fieldLengthMap.put(KFSPropertyConstants.REFERENCE_FIN_DOCUMENT_TYPE_CODE, 4);
        fieldLengthMap.put(KFSPropertyConstants.FIN_SYSTEM_REF_ORIGINATION_CODE, 2);
        fieldLengthMap.put(KFSPropertyConstants.FINANCIAL_DOCUMENT_REFERENCE_NBR, 14);
        fieldLengthMap.put(KFSPropertyConstants.FINANCIAL_DOCUMENT_REVERSAL_DATE, 10);
        fieldLengthMap.put(KFSPropertyConstants.TRANSACTION_ENCUMBRANCE_UPDT_CD, 1);
        
        return createMockParserFieldUtils(OriginEntryFieldUtil.class, fieldLengthMap);
    }

    protected CollectorBatchTrailerRecordFieldUtil createMockBatchTrailerRecordFieldUtil() {
        Map<String,Integer> fieldLengthMap = new HashMap<>();
        fieldLengthMap.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, 4);
        fieldLengthMap.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, 2);
        fieldLengthMap.put(KFSPropertyConstants.ORGANIZATION_CODE, 4);
        fieldLengthMap.put(KFSPropertyConstants.TRANSMISSION_DATE, 15);
        fieldLengthMap.put(KFSPropertyConstants.COLLECTOR_BATCH_RECORD_TYPE, 2);
        fieldLengthMap.put(KFSPropertyConstants.TRAILER_RECORD_FIRST_EMPTY_FIELD, 19);
        fieldLengthMap.put(KFSPropertyConstants.TOTAL_RECORDS, 5);
        fieldLengthMap.put(KFSPropertyConstants.TRAILER_RECORD_SECOND_EMPTY_FIELD, 42);
        fieldLengthMap.put(KFSPropertyConstants.TOTAL_AMOUNT, 19);
        
        return createMockParserFieldUtils(CollectorBatchTrailerRecordFieldUtil.class, fieldLengthMap);
    }

    /**
     * The BusinessObjectStringParserFieldUtils.getFieldLengthMap() method relies on a private method
     * to initialize the field length map, which in turn makes calls to SpringContext.getBean()
     * to retrieve the DataDictionaryService. To allow for micro-testing of such classes
     * without introducing several new subclasses, this method will create a partial mock
     * of the class so that the "getFieldLengthMap" method will return a pre-defined Map instead.
     */
    protected <T extends BusinessObjectStringParserFieldUtils> T createMockParserFieldUtils(
            Class<T> fieldUtilsClass, Map<String,Integer> fieldLengthMap) {
        T mockParserFieldUtils = EasyMock.partialMockBuilder(fieldUtilsClass)
                .withConstructor()
                .addMockedMethod(GET_FIELD_LENGTH_MAP_METHOD)
                .createNiceMock();
        EasyMock.expect(mockParserFieldUtils.getFieldLengthMap())
                .andStubReturn(fieldLengthMap);
        EasyMock.replay(mockParserFieldUtils);
        return mockParserFieldUtils;
    }

}
