package edu.cornell.kfs.concur.batch.service.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountExtractPdpEntryService;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedAccountingEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedDetailEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedFileBaseEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedGroupEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedHeaderEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedPayeeIdEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedTrailerEntry;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class ConcurStandardAccountExtractPdpEntryServiceImpl implements ConcurStandardAccountExtractPdpEntryService {
	private static final Logger LOG = LogManager.getLogger(ConcurStandardAccountExtractPdpEntryServiceImpl.class);
    protected DataDictionaryService dataDictionaryService;
    protected DateTimeService dateTimeService;
    protected ParameterService parameterService;
    protected ConcurBatchUtilityService concurBatchUtilityService;
    
    private Integer payeeNameFieldSize;
    
    protected enum DebitCreditTotal {
        DEBIT,
        CREDIT;
    }

    @Override
    public PdpFeedHeaderEntry buildPdpFeedHeaderEntry(Date batchDate) {
        PdpFeedHeaderEntry headerEntry = new PdpFeedHeaderEntry();
        headerEntry.setCampus(getConcurParameterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_LOCATION));
        headerEntry.setCreationDate(formatDate(batchDate));
        headerEntry.setSubUnit(getConcurParameterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_SUB_UNIT));
        headerEntry.setUnit(getConcurParameterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_UNIT));
        return headerEntry;
    }

    @Override
    public PdpFeedGroupEntry buildPdpFeedGroupEntry(ConcurStandardAccountingExtractDetailLine line) {
        PdpFeedGroupEntry currentGroup = new PdpFeedGroupEntry();
        currentGroup.setPayeeName(buildPayeeName(line.getEmployeeLastName(), line.getEmployeeFirstName(), 
                line.getEmployeeMiddleInitial()));
        currentGroup.setPayeeId(buildPayeeIdEntry(line));
        currentGroup.setPaymentDate(formatDate(line.getBatchDate()));
        currentGroup.setCombineGroupInd(ConcurConstants.COMBINED_GROUP_INDICATOR);
        currentGroup.setBankCode(ConcurConstants.BANK_CODE);
        currentGroup.setCustomerInstitutionIdentifier(StringUtils.EMPTY);
        return currentGroup;
    }
    
    protected String buildPayeeName(String lastName, String firstName, String middleInitial) {
        String separator = KFSConstants.COMMA + KFSConstants.BLANK_SPACE;
        String fullName = lastName + separator + firstName;
        if (StringUtils.isNotBlank(middleInitial)) {
            fullName = fullName + KFSConstants.BLANK_SPACE + middleInitial + KFSConstants.DELIMITER;
        }
        if(fullName.length() > getPayeeNameFieldSize()) {
            fullName = StringUtils.substring(fullName, 0, getPayeeNameFieldSize().intValue());
            fullName = removeLastCharacterWhenComma(fullName);
        }
        return fullName;
    }
    
    private String removeLastCharacterWhenComma(String fullName) {
        if (fullName.lastIndexOf(KFSConstants.COMMA) >= getPayeeNameFieldSize()-2) {
            fullName = fullName.substring(0, fullName.lastIndexOf(KFSConstants.COMMA));
        }
        return fullName;
    }

    @Override
    public PdpFeedPayeeIdEntry buildPayeeIdEntry(ConcurStandardAccountingExtractDetailLine line) {
        PdpFeedPayeeIdEntry payeeIdEntry = new PdpFeedPayeeIdEntry();
        payeeIdEntry.setContent(line.getEmployeeId());
        if (getConcurBatchUtilityService().isValidTravelerStatusForProcessingAsPDPEmployeeType(line.getEmployeeStatus())) {
            payeeIdEntry.setIdType(ConcurConstants.EMPLOYEE_PAYEE_STATUS_TYPE_CODE);
        } else {
            LOG.error("buildPayeeIdEntry, Unable to to set the payee ID type based do the line's employee status " + line.getEmployeeStatus());
        }
        return payeeIdEntry;
    }

    @Override
    public PdpFeedDetailEntry buildPdpFeedDetailEntry(ConcurStandardAccountingExtractDetailLine line) {
        PdpFeedDetailEntry currentDetailEntry  = new PdpFeedDetailEntry();
        currentDetailEntry.setSourceDocNbr(buildSourceDocumentNumber(line.getReportId()));
        currentDetailEntry.setFsOriginCd(getConcurParameterValue(ConcurParameterConstants.CONCUR_AP_PDP_ORIGINATION_CODE));
        currentDetailEntry.setFdocTypCd(getConcurParameterValue(ConcurParameterConstants.CONCUR_SAE_PDP_DOCUMENT_TYPE));
        currentDetailEntry.setInvoiceNbr(getConcurParameterValue(ConcurParameterConstants.CONCUR_PDP_DEFAULT_INVOICE_NUMBER));
        currentDetailEntry.setPoNbr(StringUtils.EMPTY);
        currentDetailEntry.setInvoiceDate(formatDate(line.getBatchDate()));
        return currentDetailEntry;
    }
    
    @Override
    public String buildSourceDocumentNumber(String reportId) {
        String sourceDocNumber = getConcurParameterValue(ConcurParameterConstants.CONCUR_SAE_PDP_DOCUMENT_TYPE) + reportId;
        return StringUtils.substring(sourceDocNumber, 0, ConcurConstants.SOURCE_DOCUMENT_NUMBER_FIELD_SIZE);
    }

    @Override
    public PdpFeedAccountingEntry buildPdpFeedAccountingEntry( ConcurAccountInfo concurAccountInfo) {
        PdpFeedAccountingEntry currentAccountingEntry =  new PdpFeedAccountingEntry();
        currentAccountingEntry.setCoaCd(concurAccountInfo.getChart());
        currentAccountingEntry.setAccountNbr(concurAccountInfo.getAccountNumber());
        currentAccountingEntry.setSubAccountNbr(concurAccountInfo.getSubAccountNumber());
        currentAccountingEntry.setObjectCd(getConcurParameterValue(ConcurParameterConstants.CONCUR_SAE_PDP_DEFAULT_OBJECT_CODE));
        currentAccountingEntry.setSubObjectCd(StringUtils.EMPTY);
        currentAccountingEntry.setOrgRefId(concurAccountInfo.getOrgRefId());
        currentAccountingEntry.setProjectCd(concurAccountInfo.getProjectCode());
        currentAccountingEntry.setAmount(KualiDecimal.ZERO);
        return currentAccountingEntry;
    }
    
    @Override
    public PdpFeedTrailerEntry buildPdpFeedTrailerEntry(PdpFeedFileBaseEntry pdpFeedFileBaseEntry, ConcurStandardAccountingExtractBatchReportData reportData) {
        PdpFeedTrailerEntry trailerEntry = new PdpFeedTrailerEntry();
        KualiDecimal pdpTotal = KualiDecimal.ZERO;
        int numberOfDetails = 0;
        for (PdpFeedGroupEntry group : pdpFeedFileBaseEntry.getGroup()) {
            numberOfDetails += group.getDetail().size();
            for(PdpFeedDetailEntry detailEntry : group.getDetail()) {
                for (PdpFeedAccountingEntry accountingEntry : detailEntry.getAccounting()) {
                    pdpTotal = pdpTotal.add(accountingEntry.getAmount());
                }
            }
        }
        trailerEntry.setDetailCount(numberOfDetails);
        trailerEntry.setDetailTotAmt(pdpTotal);
        reportData.getPdpRecordsProcessed().setRecordCount(numberOfDetails);
        reportData.getPdpRecordsProcessed().setDollarAmount(pdpTotal);
        return trailerEntry;
    }
    
    protected String formatDate(Date date) {
        return getDateTimeService().toString(date, ConcurConstants.DATE_FORMAT);
    }
    
    public Integer getPayeeNameFieldSize() {
        if (payeeNameFieldSize == null) {
            payeeNameFieldSize = getDataDictionaryService().getAttributeMaxLength(PaymentGroup.class, PdpConstants.PaymentDetail.PAYEE_NAME);
        }
        return payeeNameFieldSize;
    }
    
    public String getConcurParameterValue(String parameterName) {
        String parameterValue = getParameterService().getParameterValueAsString(CUKFSConstants.ParameterNamespaces.CONCUR, 
                CUKFSParameterKeyConstants.ALL_COMPONENTS, parameterName);
        return parameterValue;
    }

    public void setPayeeNameFieldSize(Integer payeeNameFieldSize) {
        this.payeeNameFieldSize = payeeNameFieldSize;
    }
    
    @Override
    public PdpFeedFileBaseEntry createPdpFileBaseEntryThatDoesNotContainNonReimbursableSections(PdpFeedFileBaseEntry pdpFeedFileBaseEntry, 
            ConcurStandardAccountingExtractBatchReportData reportData) {
        LOG.debug("Entering createPdpFileBaseEntryThatDoesNotContainNonReimbursableSections");
        PdpFeedFileBaseEntry newBaseEntry = new PdpFeedFileBaseEntry();
        newBaseEntry.setHeader(copyHeaderEntry(pdpFeedFileBaseEntry.getHeader()));
        newBaseEntry.setVersion(pdpFeedFileBaseEntry.getVersion());
        addGroupEntriesToNewBaseEntry(newBaseEntry, pdpFeedFileBaseEntry);
        newBaseEntry.setTrailer(buildPdpFeedTrailerEntry(newBaseEntry, reportData));
        return newBaseEntry;
    }

    private void addGroupEntriesToNewBaseEntry(PdpFeedFileBaseEntry newBaseEntry, PdpFeedFileBaseEntry originalBaseEntry) {
        for (PdpFeedGroupEntry originalGroupEntry : originalBaseEntry.getGroup()) {
            PdpFeedGroupEntry newGroupEntry = copyGroupEntry(originalGroupEntry);
            addDetailEntriesToNewGroupEntry(newGroupEntry, originalGroupEntry);
            if (CollectionUtils.isNotEmpty(newGroupEntry.getDetail())) {
                newBaseEntry.getGroup().add(newGroupEntry);
            } else {
                LOG.info("addGroupEntriesToNewBaseEntry, not adding group for " + newGroupEntry.getPayeeName());
            }
        }
    }
    
    private void addDetailEntriesToNewGroupEntry(PdpFeedGroupEntry newGroupEntry, PdpFeedGroupEntry originalGroupEntry) {
        for (PdpFeedDetailEntry originalDetailEntry : originalGroupEntry.getDetail()) {
            PdpFeedDetailEntry newDetailEntry = copyDetailEntry(originalDetailEntry);
            KualiDecimal originalDetailTransactionTotal = KualiDecimal.ZERO;
            for (PdpFeedAccountingEntry originalAccountingEntry : originalDetailEntry.getAccounting()) {
                originalDetailTransactionTotal = originalDetailTransactionTotal.add(originalAccountingEntry.getAmount());
                newDetailEntry.getAccounting().add(copyAccountingEntry(originalAccountingEntry));
            }
            cleanAccountingEntriesInDetailEntry(newDetailEntry);
            addNewPaymentDetailToGroupIfTotalIsPositive(newGroupEntry, newDetailEntry, originalDetailTransactionTotal);
        }
    }
    
    protected void cleanAccountingEntriesInDetailEntry(PdpFeedDetailEntry newDetailEntry) {
        Map<DebitCreditTotal, KualiDecimal> totals = calculateTotals(newDetailEntry);
        KualiDecimal originalDebitTotal = totals.get(DebitCreditTotal.DEBIT);
        KualiDecimal originalCreditTotal = totals.get(DebitCreditTotal.CREDIT);
        
        if (areThereMoreCreditsThanDebits(originalDebitTotal, originalCreditTotal)) {
            LOG.info("cleanAccountingEntriesInDetailEntry, credits are greater than or equal to debits, so removing PDP transactions");
            newDetailEntry.getAccounting().clear();
        } else if (shouldDeductCreditsFromDebits(originalDebitTotal, originalCreditTotal)) {
            LOG.info("cleanAccountingEntriesInDetailEntry, deduct credits from debits required");
            deductCreditAmountsFromDebitTransactions(newDetailEntry, originalCreditTotal);
        } else {
            LOG.info("cleanAccountingEntriesInDetailEntry, no changes required to the accounting entries");
        }
        removeNonPositiveAccountingEntriesFromDetailEntry(newDetailEntry);
    }
    
    protected EnumMap<DebitCreditTotal, KualiDecimal> calculateTotals(PdpFeedDetailEntry detailEntry) {
        KualiDecimal debitTotal = KualiDecimal.ZERO;
        KualiDecimal creditTotal = KualiDecimal.ZERO;
        for (PdpFeedAccountingEntry accountingEntry : detailEntry.getAccounting()) {
            if (accountingEntry.getAmount().isPositive()) {
                debitTotal = debitTotal.add(accountingEntry.getAmount());
            } else {
                creditTotal = creditTotal.add(accountingEntry.getAmount());
            }
        }
        LOG.info("calculateTotals, debitTotal: " + debitTotal + "  creditTotal: " + creditTotal) ;
        EnumMap<DebitCreditTotal, KualiDecimal> totals = new EnumMap<DebitCreditTotal, KualiDecimal>(DebitCreditTotal.class);
        totals.put(DebitCreditTotal.CREDIT, creditTotal);
        totals.put(DebitCreditTotal.DEBIT, debitTotal);
        return totals;
    }
    
    private boolean shouldDeductCreditsFromDebits(KualiDecimal originalDebitTotal, KualiDecimal originalCreditTotal) {
        return originalDebitTotal.isPositive() && originalCreditTotal.isNegative();
    }

    private boolean areThereMoreCreditsThanDebits(KualiDecimal originalDebitTotal, KualiDecimal originalCreditTotal) {
        return originalCreditTotal.abs().isGreaterEqual(originalDebitTotal);
    }

    private void deductCreditAmountsFromDebitTransactions(PdpFeedDetailEntry newDetailEntry, KualiDecimal originalCreditTotal) {
        KualiDecimal totalDeductionsLeft = originalCreditTotal;
        for (PdpFeedAccountingEntry accountingEntry : newDetailEntry.getAccounting()) {
            if (accountingEntry.getAmount().isPositive() && totalDeductionsLeft.isNegative()) {
                KualiDecimal newTransactionAmount = accountingEntry.getAmount().add(totalDeductionsLeft);
                if (newTransactionAmount.isNegative()) {
                    totalDeductionsLeft = newTransactionAmount;
                    newTransactionAmount = KualiDecimal.ZERO;
                } else {
                    totalDeductionsLeft = KualiDecimal.ZERO;
                }
                LOG.info("deductCreditAmountsFromDebitTransactions, Editing transction " + accountingEntry.toString() + ". changing the amount to " + newTransactionAmount);
                accountingEntry.setAmount(newTransactionAmount);
            }
        }
    }

    private void removeNonPositiveAccountingEntriesFromDetailEntry(PdpFeedDetailEntry newDetailEntry) {
        List<PdpFeedAccountingEntry> newAccountingEntries = new ArrayList<PdpFeedAccountingEntry>();
        for (PdpFeedAccountingEntry accountingEntry : newDetailEntry.getAccounting()) {
            if (accountingEntry.getAmount().isPositive()) {
                newAccountingEntries.add(accountingEntry);
            } else {
                LOG.info("removeNonPositiveAccountingEntriesFromDetailEntry, removing " + accountingEntry.toString());
            }
        }
        newDetailEntry.setAccounting(newAccountingEntries);
    }

    private void addNewPaymentDetailToGroupIfTotalIsPositive(PdpFeedGroupEntry newGroupEntry, PdpFeedDetailEntry newDetailEntry, KualiDecimal originalDetailTransactionTotal) {
        if (originalDetailTransactionTotal.isPositive()) {
            if (LOG.isInfoEnabled()) {
                LOG.info("addNewPaymentDetailToGroupIfTotalIsPositive, total transaction for " + newDetailEntry.getSourceDocNbr() + " detail: " + originalDetailTransactionTotal);
            }
            newGroupEntry.getDetail().add(newDetailEntry);
        } else {
            if (LOG.isInfoEnabled()) {
                StringBuilder sb = new StringBuilder("addNewPaymentDetailToGroupIfTotalIsPositive, Not adding detail to group for source document  ");
                sb.append(newDetailEntry.getSourceDocNbr()).append(" with a tansaction total of ").append(originalDetailTransactionTotal);
                if (originalDetailTransactionTotal.isNegative()) {
                    sb.append(".  A negative amount, it will be handled by the collector");
                } else {
                    sb.append(".  A zero amount, no payment needs to be issued");
                }
                LOG.info(sb.toString());
            }
            
        }
    }
    

    private PdpFeedAccountingEntry copyAccountingEntry(PdpFeedAccountingEntry accountingEntry) {
        PdpFeedAccountingEntry newAccountingEntry = new PdpFeedAccountingEntry();
        newAccountingEntry.setCoaCd(accountingEntry.getCoaCd());
        newAccountingEntry.setAccountNbr(accountingEntry.getAccountNbr());
        newAccountingEntry.setSubAccountNbr(accountingEntry.getSubAccountNbr());
        newAccountingEntry.setObjectCd(accountingEntry.getObjectCd());
        newAccountingEntry.setSubObjectCd(accountingEntry.getSubObjectCd());
        newAccountingEntry.setProjectCd(accountingEntry.getProjectCd());
        newAccountingEntry.setOrgRefId(accountingEntry.getOrgRefId());
        newAccountingEntry.setAmount(accountingEntry.getAmount());
        return newAccountingEntry;
    }

    private PdpFeedDetailEntry copyDetailEntry(PdpFeedDetailEntry detailEntry) {
        PdpFeedDetailEntry newDetailEntry = new PdpFeedDetailEntry();
        newDetailEntry.setSourceDocNbr(detailEntry.getSourceDocNbr());
        newDetailEntry.setInvoiceNbr(detailEntry.getInvoiceNbr());
        newDetailEntry.setPoNbr(detailEntry.getPoNbr());
        newDetailEntry.setInvoiceDate(detailEntry.getInvoiceDate());
        newDetailEntry.setFsOriginCd(detailEntry.getFsOriginCd());
        newDetailEntry.setFdocTypCd(detailEntry.getFdocTypCd());
        return newDetailEntry;
    }

    private PdpFeedGroupEntry copyGroupEntry(PdpFeedGroupEntry groupEntry) {
        PdpFeedGroupEntry newGroup = new PdpFeedGroupEntry();
        newGroup.setPayeeName(groupEntry.getPayeeName());
        
        PdpFeedPayeeIdEntry newPayeeIdEntry = new PdpFeedPayeeIdEntry();
        newPayeeIdEntry.setContent(groupEntry.getPayeeId().getContent());
        newPayeeIdEntry.setIdType(groupEntry.getPayeeId().getIdType());
        newGroup.setPayeeId(newPayeeIdEntry);
        
        newGroup.setCustomerInstitutionIdentifier(groupEntry.getCustomerInstitutionIdentifier());
        newGroup.setPaymentDate(groupEntry.getPaymentDate());
        newGroup.setBankCode(groupEntry.getBankCode());
        return newGroup;
    }

    private PdpFeedHeaderEntry copyHeaderEntry(PdpFeedHeaderEntry headerEntry) {
        PdpFeedHeaderEntry newHeaderEntry = new PdpFeedHeaderEntry();
        newHeaderEntry.setCampus(headerEntry.getCampus());
        newHeaderEntry.setCreationDate(headerEntry.getCreationDate());
        newHeaderEntry.setSubUnit(headerEntry.getSubUnit());
        newHeaderEntry.setUnit(headerEntry.getUnit());
        return newHeaderEntry;
    }
    
    public DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public ConcurBatchUtilityService getConcurBatchUtilityService() {
        return concurBatchUtilityService;
    }

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }

}
