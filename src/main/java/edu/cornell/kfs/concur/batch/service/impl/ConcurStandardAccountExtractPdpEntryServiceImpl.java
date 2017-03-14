package edu.cornell.kfs.concur.batch.service.impl;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.kns.service.DataDictionaryService;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountExtractPdpEntryService;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedAccountingEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedDetailEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedGroupEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedHeaderEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedPayeeIdEntry;

public class ConcurStandardAccountExtractPdpEntryServiceImpl implements ConcurStandardAccountExtractPdpEntryService {
    
    protected DataDictionaryService dataDictionaryService;
    
    private Integer payeeNameFieldSize;

    @Override
    public PdpFeedHeaderEntry buildPdpFeedHeaderEntry(Date batchDate) {
        PdpFeedHeaderEntry headerEntry = new PdpFeedHeaderEntry();
        headerEntry.setChart(ConcurConstants.StandardAccountingExtractPdpConstants.DEFAULT_CHART_CODE);
        headerEntry.setCreationDate(formatDate(batchDate));
        headerEntry.setSubUnit(ConcurConstants.StandardAccountingExtractPdpConstants.DEFAULT_SUB_UNIT);
        headerEntry.setUnit(ConcurConstants.StandardAccountingExtractPdpConstants.DEFAULT_UNIT);
        return headerEntry;
    }

    @Override
    public PdpFeedGroupEntry buildPdpFeedGroupEntry(ConcurStandardAccountingExtractDetailLine line) {
        PdpFeedGroupEntry currentGroup = new PdpFeedGroupEntry();
        currentGroup.setPayeeName(buildPayeeName(line.getEmployeeLastName(), line.getEmployeeFirstName(), 
                line.getEmployeeMiddleInitital()));
        currentGroup.setPayeeId(buildPayeeIdEntry(line));
        currentGroup.setPaymentDate(formatDate(line.getBatchDate()));
        currentGroup.setCombineGroupInd(ConcurConstants.StandardAccountingExtractPdpConstants.COMBINDED_GROUP_INDICATOR);
        currentGroup.setBankCode(ConcurConstants.StandardAccountingExtractPdpConstants.BANK_CODE);
        currentGroup.setCustomerInstitutionIdentifier("");
        return currentGroup;
    }
    
    protected String buildPayeeName(String lastName, String firstName, String middleInitial) {
        String seperator = KFSConstants.COMMA + KFSConstants.BLANK_SPACE;
        String fullName = lastName + seperator + firstName;
        if (StringUtils.isNotBlank(middleInitial)) {
            fullName = fullName + seperator + middleInitial + KFSConstants.DELIMITER;
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
        currentDetailEntry.setFsOriginCd(ConcurConstants.StandardAccountingExtractPdpConstants.FS_ORIGIN_CODE);
        currentDetailEntry.setFdocTypCd(ConcurConstants.StandardAccountingExtractPdpConstants.DOCUMENT_TYPE);
        currentDetailEntry.setInvoiceNbr("");
        currentDetailEntry.setPoNbr("");
        currentDetailEntry.setInvoiceDate(formatDate(line.getBatchDate()));
        return currentDetailEntry;
    }
    
    @Override
    public String buildSourceDocumentNumber(String reportId) {
        String sourceDocNumber = ConcurConstants.StandardAccountingExtractPdpConstants.DOCUMENT_TYPE + reportId;
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
        DateFormat df = new SimpleDateFormat(ConcurConstants.DATE_FORMAT);
        return df.format(date);
    }
    
    public Integer getPayeeNameFieldSize() {
        if (payeeNameFieldSize == null) {
            payeeNameFieldSize = getDataDictionaryService().getAttributeMaxLength(PaymentGroup.class, PdpConstants.PaymentDetail.PAYEE_NAME);
        }
        return payeeNameFieldSize;
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

}
