package edu.cornell.kfs.concur.batch.service.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kns.service.DataDictionaryService;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;
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
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurStandardAccountExtractPdpEntryServiceImpl.class);
    protected DataDictionaryService dataDictionaryService;
    protected DateTimeService dateTimeService;
    protected ParameterService parameterService;
    
    private Integer payeeNameFieldSize;

    @Override
    public PdpFeedHeaderEntry buildPdpFeedHeaderEntry(Date batchDate) {
        PdpFeedHeaderEntry headerEntry = new PdpFeedHeaderEntry();
        headerEntry.setChart(getConcurParameterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_LOCATION));
        headerEntry.setCreationDate(formatDate(batchDate));
        headerEntry.setSubUnit(getConcurParameterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_SUB_UNIT));
        headerEntry.setUnit(getConcurParameterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_UNIT));
        return headerEntry;
    }

    @Override
    public PdpFeedGroupEntry buildPdpFeedGroupEntry(ConcurStandardAccountingExtractDetailLine line) {
        PdpFeedGroupEntry currentGroup = new PdpFeedGroupEntry();
        currentGroup.setPayeeName(buildPayeeName(line.getEmployeeLastName(), line.getEmployeeFirstName(), 
                line.getEmployeeMiddleInitital()));
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
        if (StringUtils.equalsIgnoreCase(line.getEmployeeStatus(), ConcurConstants.EMPLOYEE_STATUS_CODE)) {
            payeeIdEntry.setIdType(ConcurConstants.EMPLOYEE_PAYEE_STATUS_TYPE_CODE);
        } else if (StringUtils.equalsIgnoreCase(line.getEmployeeStatus(), ConcurConstants.NON_EMPLOYEE_STATUS_CODE)) {
            payeeIdEntry.setIdType(ConcurConstants.NON_EMPLOYEE_PAYEE_STATUS_TYPE_CODE);
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
    public PdpFeedFileBaseEntry removeNonReimbursableSectionsFromPdpFeedFileBaseEntry(PdpFeedFileBaseEntry pdpFeedFileBaseEntry, 
            ConcurStandardAccountingExtractBatchReportData reportData) {
        LOG.debug("Entering removeNonReimbursableSectionsFromPdpFeedFileBaseEntry");
        PdpFeedFileBaseEntry baseEntry = new PdpFeedFileBaseEntry();
        baseEntry.setHeader(copyHeaderEntry(pdpFeedFileBaseEntry.getHeader()));
        baseEntry.setVersion(pdpFeedFileBaseEntry.getVersion());
        buildNewGroupEntries(pdpFeedFileBaseEntry, baseEntry);
        baseEntry.setTrailer(buildPdpFeedTrailerEntry(baseEntry, reportData));
        return baseEntry;
    }

    private void buildNewGroupEntries(PdpFeedFileBaseEntry pdpFeedFileBaseEntry, PdpFeedFileBaseEntry baseEntry) {
        for (PdpFeedGroupEntry groupEntry : pdpFeedFileBaseEntry.getGroup()) {
            PdpFeedGroupEntry newGroupEntry = copyGroupEntry(groupEntry);
            boolean validNewGroup = false;
            
            for (PdpFeedDetailEntry detailEntry : groupEntry.getDetail()) {
                PdpFeedDetailEntry newDetailEntry = copyDetailEntry(detailEntry);
                KualiDecimal transactionTotal = KualiDecimal.ZERO;
                for (PdpFeedAccountingEntry accountingEntry : detailEntry.getAccounting()) {
                    transactionTotal = transactionTotal.add(accountingEntry.getAmount());
                    if (accountingEntry.getAmount().isGreaterThan(KualiDecimal.ZERO)) {
                        newDetailEntry.getAccounting().add(copyAccountingEntry(accountingEntry));
                    } else {
                        LOG.debug("buildNewGroupEntries, not adding accounting entry: " + accountingEntry.toString());
                    }
                }
                LOG.debug("buildNewGroupEntries, total transaction for " + newDetailEntry.getSourceDocNbr() + " detail: " + transactionTotal);
                if (transactionTotal.isGreaterThan(KualiDecimal.ZERO)) {
                    validNewGroup = true;
                    newGroupEntry.getDetail().add(newDetailEntry);
                }
            }
            
            if (validNewGroup) {
                baseEntry.getGroup().add(newGroupEntry);
            } else {
                LOG.debug("buildNewGroupEntries, not adding group for" + newGroupEntry.getPayeeName());
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
        newHeaderEntry.setChart(headerEntry.getChart());
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

}
