package edu.cornell.kfs.concur.batch.service.impl;

import java.sql.Date;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kns.service.DataDictionaryService;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountExtractPdpEntryService;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedAccountingEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedDetailEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedGroupEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedHeaderEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedPayeeIdEntry;
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
        headerEntry.setChart(getConcurParamterValue(ConcurConstants.CUSTOMER_PROFILE_LOCATION_PARAMETER_NAME));
        headerEntry.setCreationDate(formatDate(batchDate));
        headerEntry.setSubUnit(getConcurParamterValue(ConcurConstants.CUSTOMER_PROFILE_SUB_UNIT_PARAMETER_NAME));
        headerEntry.setUnit(getConcurParamterValue(ConcurConstants.CUSTOMER_PROFILE_UNIT_PARAMETER_NAME));
        return headerEntry;
    }

    @Override
    public PdpFeedGroupEntry buildPdpFeedGroupEntry(ConcurStandardAccountingExtractDetailLine line) {
        PdpFeedGroupEntry currentGroup = new PdpFeedGroupEntry();
        currentGroup.setPayeeName(buildPayeeName(line.getEmployeeLastName(), line.getEmployeeFirstName(), 
                line.getEmployeeMiddleInitital()));
        currentGroup.setPayeeId(buildPayeeIdEntry(line));
        currentGroup.setPaymentDate(formatDate(line.getBatchDate()));
        currentGroup.setCombineGroupInd(ConcurConstants.StandardAccountingExtractPdpConstants.COMBINED_GROUP_INDICATOR);
        currentGroup.setBankCode(ConcurConstants.StandardAccountingExtractPdpConstants.BANK_CODE);
        currentGroup.setCustomerInstitutionIdentifier(StringUtils.EMPTY);
        return currentGroup;
    }
    
    protected String buildPayeeName(String lastName, String firstName, String middleInitial) {
        String separator = KFSConstants.COMMA + KFSConstants.BLANK_SPACE;
        String fullName = lastName + separator + firstName;
        if (StringUtils.isNotBlank(middleInitial)) {
            fullName = fullName + separator + middleInitial + KFSConstants.DELIMITER;
        }
        if(fullName.length() > getPayeeNameFieldSize()) {
            fullName = StringUtils.substring(fullName, 0, getPayeeNameFieldSize());
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
        if (StringUtils.equalsIgnoreCase(line.getEmployeeStatus(), ConcurConstants.StandardAccountingExtractPdpConstants.EMPLOYEE_STATUS_CODE)) {
            payeeIdEntry.setIdType(ConcurConstants.StandardAccountingExtractPdpConstants.EMPLOYEE_PAYEE_STATUS_TYPE_CODE);
        } else {
            payeeIdEntry.setIdType(ConcurConstants.StandardAccountingExtractPdpConstants.NON_EMPLOYEE_PAYEE_STATUS_TYPE_CODE);
        }
        return payeeIdEntry;
    }

    @Override
    public PdpFeedDetailEntry buildPdpFeedDetailEntry(ConcurStandardAccountingExtractDetailLine line) {
        PdpFeedDetailEntry currentDetailEntry  = new PdpFeedDetailEntry();
        currentDetailEntry.setSourceDocNbr(buildSourceDocumentNumber(line.getReportId()));
        currentDetailEntry.setFsOriginCd(getConcurParamterValue(ConcurConstants.DEFAULT_ORIGINATION_CODE_PARAMETER_NAME));
        currentDetailEntry.setFdocTypCd(getConcurParamterValue(ConcurConstants.DEFAULT_SAW_PDP_DOCUMENT_TYPE_PARAMETER_NAME));
        currentDetailEntry.setInvoiceNbr("");
        currentDetailEntry.setPoNbr("");
        currentDetailEntry.setInvoiceDate(formatDate(line.getBatchDate()));
        return currentDetailEntry;
    }
    
    @Override
    public String buildSourceDocumentNumber(String reportId) {
        String sourceDocNumber = getConcurParamterValue(ConcurConstants.DEFAULT_SAW_PDP_DOCUMENT_TYPE_PARAMETER_NAME) + reportId;
        return StringUtils.substring(sourceDocNumber, 0, ConcurConstants.SOURCE_DOCUMENT_NUMBER_FIELD_SIZE);
    }

    @Override
    public PdpFeedAccountingEntry buildPdpFeedAccountingEntry(ConcurStandardAccountingExtractDetailLine line) {
        PdpFeedAccountingEntry currentAccountingEntry =  new PdpFeedAccountingEntry();
        currentAccountingEntry.setCoaCd(line.getChartOfAccountsCode());
        currentAccountingEntry.setAccountNbr(line.getAccountNumber());
        currentAccountingEntry.setObjectCd(line.getJournalAccountCode());
        currentAccountingEntry.setSubObjectCd(line.getSubObjectCode());
        currentAccountingEntry.setOrgRefId(line.getOrgRefId());
        currentAccountingEntry.setProjectCd(line.getProjectCode());
        currentAccountingEntry.setAmount(KualiDecimal.ZERO.toString());
        return currentAccountingEntry;
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
    
    public String getConcurParamterValue(String parameterName) {
        String paramterValue = getParameterService().getParameterValueAsString(CUKFSConstants.ParameterNamespaces.CONCUR, 
                CUKFSParameterKeyConstants.ALL_COMPONENTS, parameterName);
        if (LOG.isInfoEnabled()) { 
            LOG.info("getConcurParamterValue, parameterName: " + parameterName + " paramterValue: " + paramterValue);
        }
        return paramterValue;
    }

    public void setPayeeNameFieldSize(Integer payeeNameFieldSize) {
        this.payeeNameFieldSize = payeeNameFieldSize;
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
