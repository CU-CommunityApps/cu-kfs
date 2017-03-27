package edu.cornell.kfs.concur.batch.service.impl;

import java.io.File;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.kns.service.DataDictionaryService;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.ConcurRequestExtractPdpConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractFile;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestDetailFileLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestedCashAdvance;
import edu.cornell.kfs.concur.batch.service.ConcurCashAdvancePdpFeedFileService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestedCashAdvanceService;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedAccountingEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedDetailEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedFileBaseEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedGroupEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedHeaderEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedPayeeIdEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedTrailerEntry;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.service.CUMarshalService;

public class ConcurCashAdvancePdpFeedFileServiceImpl implements ConcurCashAdvancePdpFeedFileService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurCashAdvancePdpFeedFileServiceImpl.class);
    private int payeeNameFieldSize = 0;
    protected String paymentImportDirectory;
    protected DateTimeService DateTimeService;
    protected DataDictionaryService dataDictionaryService;
    protected ConcurRequestedCashAdvanceService concurRequestedCashAdvanceService;
    protected CUMarshalService cuMarshalService;
    protected ParameterService parameterService;

    @Transactional
    public boolean createPdpFeedFileForValidatedDetailFileLines(ConcurRequestExtractFile requestExtractFile) {
        boolean pdpFileSuccessfullyCreated = false;
        PdpFeedFileBaseEntry pdpFeedFileDataObject = buildPdpFeedBaseEntry(requestExtractFile);
        if (pdpFeedFileDataObject.getTrailer().getDetailCount().intValue() != 0) {
            String fullyQualifiedPdpFileName = buildFullyQualifiedPdpOutputFileName(getPaymentImportDirectory(), requestExtractFile.getFileName());
            pdpFileSuccessfullyCreated = createPdpFeedFile(pdpFeedFileDataObject, fullyQualifiedPdpFileName);
            if (pdpFileSuccessfullyCreated) {
                LOG.debug("fullyQualifiedPdpFileName [" + fullyQualifiedPdpFileName + "]  was created for requestExtractFile [" + requestExtractFile.getFileName() + "]");
                requestExtractFile.setFullyQualifiedPdpFileName(fullyQualifiedPdpFileName);
            }
            else {
                LOG.debug("FAILED TO CREATE: fullyQualifiedPdpFileName [" + fullyQualifiedPdpFileName + "] for requestExtractFile [" + requestExtractFile.getFileName() + "]");
            }
        }
        return pdpFileSuccessfullyCreated;
    }

    public void createDoneFileForPdpFile(String fullyQualifiedPdpFileName) throws IOException {
        String fullFilePath = StringUtils.replace(fullyQualifiedPdpFileName, ConcurConstants.XML_FILE_EXTENSION, GeneralLedgerConstants.BatchFileSystem.DONE_FILE_EXTENSION);
        FileUtils.touch(new File(fullFilePath));
    }

    public void setPaymentImportDirectory(String paymentImportDirectory) {
        this.paymentImportDirectory = paymentImportDirectory;
    }

    public String getPaymentImportDirectory() {
        return this.paymentImportDirectory;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public ConcurRequestedCashAdvanceService getConcurRequestedCashAdvanceService() {
        return concurRequestedCashAdvanceService;
    }

    public void setConcurRequestedCashAdvanceService(ConcurRequestedCashAdvanceService concurRequestedCashAdvanceService) {
        this.concurRequestedCashAdvanceService = concurRequestedCashAdvanceService;
    }

    public CUMarshalService getCuMarshalService() {
        return cuMarshalService;
    }

    public void setCuMarshalService(CUMarshalService cuMarshalService) {
        this.cuMarshalService = cuMarshalService;
    }

    public DateTimeService getDateTimeService() {
        return DateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        DateTimeService = dateTimeService;
    }

    public int getPayeeNameFieldSize() {
        if (payeeNameFieldSize == 0) {
            payeeNameFieldSize = retrievePayeeNameFieldSize();
        }
        return payeeNameFieldSize;
    }

    private PdpFeedFileBaseEntry buildPdpFeedBaseEntry(ConcurRequestExtractFile requestExtractFile) {
        int totalPdpDetailRecordsCount = 0;
        KualiDecimal totalPdpDetailRecordsAmount = KualiDecimal.ZERO;
        PdpFeedFileBaseEntry pdpBaseEntry = new PdpFeedFileBaseEntry();
        pdpBaseEntry.setHeader(buildPdpFeedHeaderEntry(requestExtractFile.getBatchDate()));
        List<PdpFeedGroupEntry> groupEntries = new ArrayList<PdpFeedGroupEntry>();
        for (ConcurRequestExtractRequestDetailFileLine detailFileLine : requestExtractFile.getRequestDetails()) {
            if (detailFileLine.getValidationResult().isCashAdvanceLine() && detailFileLine.getValidationResult().isValidCashAdvanceLine()) {
                PdpFeedDetailEntry pdpDetailEntry = buildPdpFeedDetailEntry(detailFileLine, buildPdpFeedAccountingEntry(detailFileLine));
                List<PdpFeedDetailEntry> pdpDetailEntries = new ArrayList<PdpFeedDetailEntry>();
                pdpDetailEntries.add(pdpDetailEntry);

                PdpFeedGroupEntry groupEntry = buildPdpFeedGroupEntry(detailFileLine, buildPdpFeedPayeeIdEntry(detailFileLine), pdpDetailEntries);
                groupEntries.add(groupEntry);

                ConcurRequestedCashAdvance duplicateTrackingCashAdvance =
                    new ConcurRequestedCashAdvance(detailFileLine.getRequestId(), detailFileLine.getEmployeeId(), detailFileLine.getRequestAmount(),
                                                   detailFileLine.getBatchDate(), pdpDetailEntry.getSourceDocNbr(), requestExtractFile.getFileName());
                getConcurRequestedCashAdvanceService().saveConcurRequestedCashAdvance(duplicateTrackingCashAdvance);

                totalPdpDetailRecordsCount++;
                totalPdpDetailRecordsAmount = totalPdpDetailRecordsAmount.add(detailFileLine.getRequestAmount());
            }
            else if (detailFileLine.getValidationResult().isCashAdvanceLine() && detailFileLine.getValidationResult().isNotValidCashAdvanceLine()) {
                LOG.info("Cash Advance was detected but validation failed for:  " + KFSConstants.NEWLINE + detailFileLine.toString());
            }
        }
        pdpBaseEntry.setGroup(groupEntries);
        pdpBaseEntry.setTrailer(buildPdpFeedTrailerEntry(totalPdpDetailRecordsCount, totalPdpDetailRecordsAmount));
        pdpBaseEntry.setVersion(ConcurConstants.FEED_FILE_ENTRY_HEADER_VERSION);
        return pdpBaseEntry;
    }

    private PdpFeedGroupEntry buildPdpFeedGroupEntry(ConcurRequestExtractRequestDetailFileLine detailFileLine, PdpFeedPayeeIdEntry pdpPayeeIdEntry, List<PdpFeedDetailEntry> pdpDetailEntries) {
        PdpFeedGroupEntry pdpGroupEntry = new PdpFeedGroupEntry();
        pdpGroupEntry.setPayeeName(formatPayeeName(detailFileLine.getLastName(), detailFileLine.getFirstName(), detailFileLine.getMiddleInitial()));
        pdpGroupEntry.setPayeeId(pdpPayeeIdEntry);
        pdpGroupEntry.setCustomerInstitutionIdentifier(StringUtils.EMPTY);
        pdpGroupEntry.setPaymentDate(formatDate(detailFileLine.getBatchDate()));
        pdpGroupEntry.setCombineGroupInd(ConcurConstants.COMBINED_GROUP_INDICATOR);
        pdpGroupEntry.setBankCode(ConcurConstants.BANK_CODE);
        pdpGroupEntry.setDetail(pdpDetailEntries);
        return pdpGroupEntry;
    }

    private PdpFeedDetailEntry buildPdpFeedDetailEntry(ConcurRequestExtractRequestDetailFileLine detailFileLine, PdpFeedAccountingEntry pdpAccountingEntry) {
        PdpFeedDetailEntry pdpDetailEntry  = new PdpFeedDetailEntry();
        pdpDetailEntry.setSourceDocNbr(formatSourceDocumentNumber(getConcurParamterValue(ConcurParameterConstants.CONCUR_REQUEST_EXTRACT_PDP_DOCUMENT_TYPE), detailFileLine.getRequestId()));
        pdpDetailEntry.setInvoiceNbr(StringUtils.EMPTY);
        pdpDetailEntry.setPoNbr(StringUtils.EMPTY);
        pdpDetailEntry.setInvoiceDate(formatDate(detailFileLine.getBatchDate()));
        pdpDetailEntry.setNetPaymentAmt(detailFileLine.getRequestAmount());
        pdpDetailEntry.setFsOriginCd(getConcurParamterValue(ConcurParameterConstants.CONCUR_AP_PDP_ORIGINATION_CODE));
        pdpDetailEntry.setFdocTypCd(getConcurParamterValue(ConcurParameterConstants.CONCUR_REQUEST_EXTRACT_PDP_DOCUMENT_TYPE));
        List<String> paymentTexts = new ArrayList<String>();
        paymentTexts.add(detailFileLine.getRequestEntryDescription());
        pdpDetailEntry.setPaymentText(paymentTexts);
        List<PdpFeedAccountingEntry> accountingLines = new ArrayList<PdpFeedAccountingEntry>();
        accountingLines.add(pdpAccountingEntry);
        pdpDetailEntry.setAccounting(accountingLines);
        return pdpDetailEntry;
    }

    private PdpFeedAccountingEntry buildPdpFeedAccountingEntry(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        PdpFeedAccountingEntry pdpAccountingEntry =  new PdpFeedAccountingEntry();
        pdpAccountingEntry.setCoaCd(detailFileLine.getChart());
        pdpAccountingEntry.setAccountNbr(detailFileLine.getAccountNumber());
        pdpAccountingEntry.setSubAccountNbr(detailFileLine.getSubAccountNumber());
        pdpAccountingEntry.setObjectCd(getConcurParamterValue(ConcurParameterConstants.DEFAULT_TRAVEL_REQUEST_OBJECT_CODE));
        pdpAccountingEntry.setSubObjectCd(detailFileLine.getSubObjectCode());
        pdpAccountingEntry.setOrgRefId(detailFileLine.getOrgRefId());
        pdpAccountingEntry.setProjectCd(detailFileLine.getProjectCode());
        pdpAccountingEntry.setAmount(detailFileLine.getRequestAmount());
        return pdpAccountingEntry;
    }

    private PdpFeedPayeeIdEntry buildPdpFeedPayeeIdEntry(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        PdpFeedPayeeIdEntry payeeIdEntry = new PdpFeedPayeeIdEntry();
        payeeIdEntry.setContent(detailFileLine.getEmployeeId());
        if (StringUtils.equalsIgnoreCase(detailFileLine.getPayeeIdType(), ConcurRequestExtractPdpConstants.ValidationConstants.EMPLOYEE_INDICATOR)) {
            payeeIdEntry.setIdType(ConcurConstants.EMPLOYEE_PAYEE_STATUS_TYPE_CODE);
        } else if (StringUtils.equalsIgnoreCase(detailFileLine.getPayeeIdType(), ConcurRequestExtractPdpConstants.ValidationConstants.NON_EMPLOYEE_INDICATOR)) {
            payeeIdEntry.setIdType(ConcurConstants.NON_EMPLOYEE_PAYEE_STATUS_TYPE_CODE);
        } else {
            LOG.error("Invalid PayeeIdType detected in buildPdpFeedPayeeIdEntry AFTER validation while building PDP output file:" + detailFileLine.getPayeeIdType());
        }
        return payeeIdEntry;
    }

    private PdpFeedHeaderEntry buildPdpFeedHeaderEntry(Date batchDate) {
        PdpFeedHeaderEntry header = new PdpFeedHeaderEntry();
        header.setChart(getConcurParamterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_LOCATION));
        header.setCreationDate(formatDate(batchDate));
        header.setSubUnit(getConcurParamterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_SUB_UNIT));
        header.setUnit(getConcurParamterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_UNIT));
        return header;
    }

    private PdpFeedTrailerEntry buildPdpFeedTrailerEntry(int totalDetailRecordCount, KualiDecimal totalDetailRecordAmount) {
        PdpFeedTrailerEntry trailer = new PdpFeedTrailerEntry();
        trailer.setDetailCount(totalDetailRecordCount);
        trailer.setDetailTotAmt(totalDetailRecordAmount);
        return trailer;
    }

    //common utility method
    private String formatPayeeName(String lastName, String firstName, String middleInitial) {
        String fullName = lastName + KFSConstants.COMMA + KFSConstants.BLANK_SPACE + firstName;
        if (StringUtils.isNotBlank(middleInitial)) {
            fullName = fullName + KFSConstants.BLANK_SPACE + middleInitial + KFSConstants.DELIMITER;
        }

        if (fullName.length() > getPayeeNameFieldSize()) {
            fullName = StringUtils.substring(fullName, 0, getPayeeNameFieldSize());
            fullName = removeLastPayeeNameCharacterWhenComma(fullName);
        }
        return fullName;
    }

    //needs to be a common service call
    private String removeLastPayeeNameCharacterWhenComma(String fullName) {
        if (fullName.lastIndexOf(KFSConstants.COMMA) >= getPayeeNameFieldSize()-2) {
            fullName = fullName.substring(0, fullName.lastIndexOf(KFSConstants.COMMA));
        }
        return fullName;
    }

    //common utility method
    private int retrievePayeeNameFieldSize() {
        return (getDataDictionaryService().getAttributeMaxLength(PaymentGroup.class, PdpConstants.PaymentDetail.PAYEE_NAME)).intValue();
    }

    //common utility method
    private String formatSourceDocumentNumber(String documentTypeCode, String requestId) {
        String sourceDocNumber = new String(documentTypeCode + requestId);
        return StringUtils.substring(sourceDocNumber, 0, ConcurConstants.SOURCE_DOCUMENT_NUMBER_FIELD_SIZE);
    }

    //common utility method
    private String getConcurParamterValue(String parameterName) {
        String parameterValue = getParameterService().getParameterValueAsString(CUKFSConstants.ParameterNamespaces.CONCUR, CUKFSParameterKeyConstants.ALL_COMPONENTS, parameterName);
        return parameterValue;
    }

    //common utility method
    private String formatDate(Date date) {
        return getDateTimeService().toString(date, ConcurConstants.DATE_FORMAT);
    }

    //common utility method
    protected String buildFullyQualifiedPdpOutputFileName(String paymentImportDirecotry, String pdpInputfileName) {
        String fullyQualifiedPdpOutputFileName = new String(paymentImportDirecotry + ConcurConstants.PDP_CONCUR_OUTPUT_FILE_NAME_PREFIX + pdpInputfileName);
        return StringUtils.replace(fullyQualifiedPdpOutputFileName, GeneralLedgerConstants.BatchFileSystem.TEXT_EXTENSION, ConcurConstants.XML_FILE_EXTENSION);
    }

    //common utility method
    private boolean createPdpFeedFile(PdpFeedFileBaseEntry pdpFeedFileBaseEntry, String fullyQualifiedPdpFileName) {
        boolean success = true;
        try {
            File pdpFeedFile = getCuMarshalService().marshalObjectToXML(pdpFeedFileBaseEntry, fullyQualifiedPdpFileName);
            LOG.info("Created PDP Feed file: " + fullyQualifiedPdpFileName);
            success = true;
        } catch (JAXBException | IOException e) {
            LOG.error("createPdpFeedFile.marshalObjectToXML: There was an error marshalling the PDP feed file: " + fullyQualifiedPdpFileName, e);
            success = false;
        }
        return success;
    }

}
