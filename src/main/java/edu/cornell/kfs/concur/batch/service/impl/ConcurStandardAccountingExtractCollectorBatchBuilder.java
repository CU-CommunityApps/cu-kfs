package edu.cornell.kfs.concur.batch.service.impl;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.gl.batch.CollectorBatch;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.gl.CuGeneralLedgerConstants;

/**
 * Helper class for converting parsed SAE file data
 * into GL CollectorBatch objects, which in turn will contain
 * OriginEntryFull objects derived from the SAE detail lines.
 * The constructed CollectorBatch objects will have the "HD"
 * (Header) record type.
 * 
 * This builder is NOT thread-safe; external synchronization
 * is required if one instance is used by multiple threads.
 */
public class ConcurStandardAccountingExtractCollectorBatchBuilder {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurStandardAccountingExtractCollectorBatchBuilder.class);

    protected static final String FAKE_OBJECT_CODE_PREFIX = "?NONE?";
    protected static final int INITIAL_BUILDER_SIZE = 50;
    protected static final int REPORT_ID_LENGTH_FOR_DOC_NUMBER = 10;
    protected static final int NAME_LENGTH_FOR_DESCRIPTION = 14;
    protected static final int MIN_BATCH_SEQUENCE_NUMBER = 0;
    protected static final int MAX_BATCH_SEQUENCE_NUMBER = 9;

    protected final String docTypeCode;
    protected final String systemOriginationCode;
    protected final String chartCode;
    protected final String highestLevelOrgCode;
    protected final String undefinedObjectCode;
    protected final String defaultObjectCode;
    protected final String departmentName;
    protected final String campusCode;
    protected final String campusAddress;
    protected final String notificationEmail;
    protected final String notificationPerson;
    protected final String notificationPhone;

    protected CollectorBatch collectorBatch;
    protected Map<String,OriginEntryFull> originEntries;
    protected Function<Date,Integer> fiscalYearFinder;
    protected BiFunction<Date,String,String> dateFormatter;
    protected Predicate<ConcurStandardAccountingExtractDetailLine> saeLineValidator;
    protected int batchSequenceNumber;
    protected int nextFakeObjectCode;

    public ConcurStandardAccountingExtractCollectorBatchBuilder(
            Function<Date,Integer> fiscalYearFinder, BiFunction<Date,String,String> dateFormatter,
            Predicate<ConcurStandardAccountingExtractDetailLine> saeLineValidator, Function<String,String> parameterFinder) {
        if (fiscalYearFinder == null) {
            throw new IllegalArgumentException("fiscalYearFinder cannot be null");
        } else if (dateFormatter == null) {
            throw new IllegalArgumentException("dateFormatter cannot be null");
        } else if (parameterFinder == null) {
            throw new IllegalArgumentException("propertyFinder cannot be null");
        }
        
        this.fiscalYearFinder = fiscalYearFinder;
        this.dateFormatter = dateFormatter;
        this.saeLineValidator = saeLineValidator;
        
        this.docTypeCode = parameterFinder.apply(ConcurKeyConstants.SAE_COLLECTOR_DOCTYPE_CODE);
        this.systemOriginationCode = parameterFinder.apply(ConcurKeyConstants.SAE_COLLECTOR_SYSTEM_ORIGINATION_CODE);
        this.chartCode = parameterFinder.apply(ConcurKeyConstants.SAE_COLLECTOR_CHART_CODE);
        this.highestLevelOrgCode = parameterFinder.apply(ConcurKeyConstants.SAE_COLLECTOR_HIGHEST_LEVEL_ORG_CODE);
        this.undefinedObjectCode = parameterFinder.apply(ConcurKeyConstants.SAE_COLLECTOR_UNDEFINED_OBJECT_CODE);
        this.defaultObjectCode = parameterFinder.apply(ConcurKeyConstants.SAE_COLLECTOR_DEFAULT_OBJECT_CODE);
        this.departmentName = parameterFinder.apply(ConcurKeyConstants.SAE_COLLECTOR_DEPARTMENT_NAME);
        this.campusCode = parameterFinder.apply(ConcurKeyConstants.SAE_COLLECTOR_CAMPUS_CODE);
        this.campusAddress = parameterFinder.apply(ConcurKeyConstants.SAE_COLLECTOR_CAMPUS_ADDRESS);
        this.notificationEmail = parameterFinder.apply(ConcurKeyConstants.SAE_COLLECTOR_NOTIFICATION_CONTACT_EMAIL);
        this.notificationPerson = parameterFinder.apply(ConcurKeyConstants.SAE_COLLECTOR_NOTIFICATION_CONTACT_PERSON);
        this.notificationPhone = parameterFinder.apply(ConcurKeyConstants.SAE_COLLECTOR_NOTIFICATION_CONTACT_PHONE);
    }

    public void reset() {
        this.collectorBatch = new CollectorBatch();
        this.originEntries = new LinkedHashMap<>();
        this.nextFakeObjectCode = 1;
    }

    public CollectorBatch buildCollectorBatchFromStandardAccountingExtract(int nextBatchSequenceNumber,
            ConcurStandardAccountingExtractFile saeFileContents) {
        this.batchSequenceNumber = nextBatchSequenceNumber;
        reset();
        
        try {
            // TODO: Once Jay's changes are merged, use the filename instead of the batch date!
            String formattedBatchDate = dateFormatter.apply(saeFileContents.getBatchDate(), ConcurConstants.SAE_COLLECTOR_DATE_FORMAT);
            LOG.info("Generating Collector data from SAE file with batch date: " + formattedBatchDate);
            
            updateCollectorBatchHeaderFields(saeFileContents);
            buildOriginEntries(saeFileContents.getConcurStandardAccountingExtractDetailLines());
            updateCollectorBatchWithOriginEntries();
            
            LOG.info("Finished generating collector data from SAE file with batch date: " + formattedBatchDate);
            LOG.info("Total GL Entry Count: " + collectorBatch.getTotalRecords());
            LOG.info("Total Amount: " + collectorBatch.getTotalAmount());
        } catch (Exception e) {
            LOG.error("Error encountered while generating Collector data from SAE file", e);
            return null;
        }
        
        return collectorBatch;
    }

    protected void updateCollectorBatchHeaderFields(ConcurStandardAccountingExtractFile saeFileContents) {
        if (batchSequenceNumber < MIN_BATCH_SEQUENCE_NUMBER || batchSequenceNumber > MAX_BATCH_SEQUENCE_NUMBER) {
            throw new RuntimeException("Batch Sequence Number should have been an integer between 0 and 9");
        }
        
        Integer fiscalYear = fiscalYearFinder.apply(saeFileContents.getBatchDate());
        if (fiscalYear == null) {
            throw new RuntimeException("No fiscal year found for batch date: " + saeFileContents.getBatchDate().toString());
        }
        
        collectorBatch.setRecordType(CuGeneralLedgerConstants.COLLECTOR_HEADER_RECORD_TYPE);
        collectorBatch.setUniversityFiscalYear(fiscalYear.toString());
        collectorBatch.setChartOfAccountsCode(chartCode);
        collectorBatch.setOrganizationCode(highestLevelOrgCode);
        collectorBatch.setTransmissionDate(saeFileContents.getBatchDate());
        collectorBatch.setBatchSequenceNumber(Integer.valueOf(batchSequenceNumber));
        collectorBatch.setCampusCode(campusCode);
        collectorBatch.setMailingAddress(campusAddress);
        collectorBatch.setEmailAddress(notificationEmail);
        collectorBatch.setPersonUserID(notificationPerson);
        collectorBatch.setPhoneNumber(notificationPhone);
    }

    protected void buildOriginEntries(List<ConcurStandardAccountingExtractDetailLine> saeLines) {
        for (ConcurStandardAccountingExtractDetailLine saeLine : saeLines) {
            String itemKey = buildItemKey(saeLine);
            if (shouldProcessLine(itemKey, saeLine) && saeLineValidator.test(saeLine)) {
                OriginEntryFull originEntry = originEntries.computeIfAbsent(
                        itemKey, (newKey) -> buildOriginEntryForExtractedLine(newKey, saeLine));
                compareLineNonKeyFieldsAgainstEntry(itemKey, saeLine, originEntry);
                addLineAmountToOriginEntry(saeLine, originEntry);
            }
        }
    }

    protected void updateCollectorBatchWithOriginEntries() {
        for (OriginEntryFull originEntry : originEntries.values()) {
            collectorBatch.addOriginEntry(originEntry);
        }
        
        Integer totalRecords = Integer.valueOf(collectorBatch.getOriginEntries().size());
        KualiDecimal totalAmount = collectorBatch.getOriginEntries()
                .stream()
                .map(OriginEntryFull::getTransactionLedgerEntryAmount)
                .reduce(KualiDecimal.ZERO, KualiDecimal::add);
        
        collectorBatch.setTotalRecords(totalRecords);
        collectorBatch.setTotalAmount(totalAmount);
    }

    protected boolean shouldProcessLine(String itemKey, ConcurStandardAccountingExtractDetailLine saeLine) {
        switch (saeLine.getPaymentCode()) {
            case ConcurConstants.SAE_OUT_OF_POCKET_PAYMENT_CODE :
            case ConcurConstants.SAE_UNIVERSITY_BILLED_OR_PAID_PAYMENT_CODE :
                return true;
            case ConcurConstants.SAE_PRE_PAID_OR_OTHER_PAYMENT_CODE :
                LOG.warn("Found a transaction with a Pre-Paid/Other payment code; it will not be processed. Item key: " + itemKey);
                return false;
            case ConcurConstants.SAE_PSEUDO_PAYMENT_CODE :
                LOG.warn("Found a transaction with a Pseudo payment code; it will not be processed. Item key: " + itemKey);
                return false;
            default :
                LOG.warn("Found a transaction with an unknown payment code; it will not be processed. Item key: " + itemKey);
                return false;
        }
    }

    protected String buildItemKey(ConcurStandardAccountingExtractDetailLine saeLine) {
        String objectCodeForKey = saeLine.getJournalAccountCode();
        if (StringUtils.equals(undefinedObjectCode, objectCodeForKey)) {
            objectCodeForKey = buildFakeObjectCodeToLeaveDetailLineUnmerged();
        }
        
        return new StringBuilder(INITIAL_BUILDER_SIZE)
                .append(makeEmptyIfBlank(saeLine.getChartOfAccountsCode()))
                .append('|').append(makeEmptyIfBlank(saeLine.getAccountNumber()))
                .append('|').append(StringUtils.defaultIfBlank(saeLine.getSubAccountNumber(), getDashSubAccountNumber()))
                .append('|').append(makeEmptyIfBlank(objectCodeForKey))
                .append('|').append(StringUtils.defaultIfBlank(saeLine.getSubObjectCode(), getDashSubObjectCode()))
                .append('|').append(StringUtils.defaultIfBlank(saeLine.getProjectCode(), getDashProjectCode()))
                .append('|').append(makeEmptyIfBlank(saeLine.getOrgRefId()))
                .toString();
    }

    protected String makeEmptyIfBlank(String value) {
        return StringUtils.defaultIfBlank(value, StringUtils.EMPTY);
    }

    protected String buildFakeObjectCodeToLeaveDetailLineUnmerged() {
        return FAKE_OBJECT_CODE_PREFIX + (nextFakeObjectCode++);
    }

    protected OriginEntryFull buildOriginEntryForExtractedLine(String itemKey, ConcurStandardAccountingExtractDetailLine saeLine) {
        OriginEntryFull originEntry = new OriginEntryFull();
        
        String objectCode = saeLine.getJournalAccountCode();
        if (StringUtils.equals(undefinedObjectCode, objectCode)) {
            LOG.warn("Found a detail line with an undefined object code; will use " + defaultObjectCode
                    + " as the object code and will not merge the line into any others. Item key: " + itemKey);
            objectCode = defaultObjectCode;
        }
        
        originEntry.setChartOfAccountsCode(saeLine.getChartOfAccountsCode());
        originEntry.setAccountNumber(saeLine.getAccountNumber());
        originEntry.setSubAccountNumber(
                StringUtils.defaultIfBlank(saeLine.getSubAccountNumber(), getDashSubAccountNumber()));
        originEntry.setFinancialObjectCode(objectCode);
        originEntry.setFinancialSubObjectCode(
                StringUtils.defaultIfBlank(saeLine.getSubObjectCode(), getDashSubObjectCode()));
        originEntry.setProjectCode(
                StringUtils.defaultIfBlank(saeLine.getProjectCode(), getDashProjectCode()));
        originEntry.setOrganizationReferenceId(
                StringUtils.defaultIfBlank(saeLine.getOrgRefId(), StringUtils.EMPTY));
        
        originEntry.setFinancialDocumentTypeCode(docTypeCode);
        originEntry.setFinancialSystemOriginationCode(systemOriginationCode);
        originEntry.setDocumentNumber(buildDocumentNumber(saeLine));
        originEntry.setTransactionLedgerEntrySequenceNumber(collectorBatch.getBatchSequenceNumber());
        originEntry.setTransactionLedgerEntryDescription(buildTransactionDescription(saeLine));
        originEntry.setTransactionDate(collectorBatch.getTransmissionDate());
        originEntry.setTransactionLedgerEntryAmount(KualiDecimal.ZERO);
        
        return originEntry;
    }

    protected String getDashSubAccountNumber() {
        return KFSConstants.getDashSubAccountNumber();
    }

    protected String getDashSubObjectCode() {
        return KFSConstants.getDashFinancialSubObjectCode();
    }

    protected String getDashProjectCode() {
        return KFSConstants.getDashProjectCode();
    }

    protected String buildDocumentNumber(ConcurStandardAccountingExtractDetailLine saeLine) {
        return docTypeCode + StringUtils.left(saeLine.getReportId(), REPORT_ID_LENGTH_FOR_DOC_NUMBER);
    }

    protected String buildTransactionDescription(ConcurStandardAccountingExtractDetailLine saeLine) {
        String formattedEndDate = dateFormatter.apply(saeLine.getReportEndDate(), ConcurConstants.SAE_COLLECTOR_DATE_FORMAT);
        
        return new StringBuilder(INITIAL_BUILDER_SIZE)
                .append(StringUtils.left(saeLine.getEmployeeLastName(), NAME_LENGTH_FOR_DESCRIPTION))
                .append(',').append(StringUtils.left(saeLine.getEmployeeFirstName(), NAME_LENGTH_FOR_DESCRIPTION))
                .append(',').append(formattedEndDate)
                .toString();
    }

    protected void compareLineNonKeyFieldsAgainstEntry(String itemKey,
            ConcurStandardAccountingExtractDetailLine saeLine, OriginEntryFull originEntry) {
        if (!StringUtils.equals(originEntry.getDocumentNumber(), buildDocumentNumber(saeLine))) {
            LOG.warn("Found a documentNumber/reportId mismatch on at least one line for item key: " + itemKey);
        } else if (!StringUtils.equals(originEntry.getTransactionLedgerEntryDescription(), buildTransactionDescription(saeLine))) {
            LOG.warn("Found a lastName/firstName/endDate mismatch on at least one line for item key: " + itemKey);
        }
    }

    protected void addLineAmountToOriginEntry(ConcurStandardAccountingExtractDetailLine saeLine, OriginEntryFull originEntry) {
        String debitCreditCode = getGLDebitCreditCode(saeLine.getJounalDebitCredit());
        KualiDecimal amount = saeLine.getJournalAmount();
        
        if (StringUtils.isBlank(originEntry.getTransactionDebitCreditCode())) {
            originEntry.setTransactionDebitCreditCode(debitCreditCode);
        } else if (!StringUtils.equals(debitCreditCode, originEntry.getTransactionDebitCreditCode())) {
            throw new RuntimeException("Cannot have debits and credits on the same consolidated transaction");
        }
        
        if (amount.isNegative()) {
            if (!StringUtils.equals(KFSConstants.GL_CREDIT_CODE, debitCreditCode)) {
                throw new RuntimeException("Cannot have a negative amount on a debit transaction");
            }
            amount = amount.negated();
        }
        
        originEntry.setTransactionLedgerEntryAmount(
                originEntry.getTransactionLedgerEntryAmount().add(amount));
    }

    protected String getGLDebitCreditCode(String saeDebitCreditCode) {
        switch (saeDebitCreditCode) {
            case ConcurConstants.SAE_CREDIT_CODE :
                return KFSConstants.GL_CREDIT_CODE;
            case ConcurConstants.SAE_DEBIT_CODE :
                return KFSConstants.GL_DEBIT_CODE;
            default :
                throw new IllegalArgumentException("Unrecognized debit/credit code: " + saeDebitCreditCode);
        }
    }

}
