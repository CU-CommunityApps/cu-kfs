package edu.cornell.kfs.fp.batch.service.impl;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItem;
import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItemDetail;
import edu.cornell.kfs.fp.batch.service.CreateAccountingDocumentValidationService;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentListWrapper;

public class CreateAccountingDocumentValidationServiceImpl implements CreateAccountingDocumentValidationService {
    private static final Logger LOG = LogManager.getLogger(CreateAccountingDocumentValidationServiceImpl.class);
    
    protected ConfigurationService configurationService;
    
    @Override
    public boolean isValidXmlFileHeaderData(AccountingXmlDocumentListWrapper accountingXmlDocument, CreateAccountingDocumentReportItem reportItem) {
        boolean allValidationChecksPassed = isReportEmailAddressValid(accountingXmlDocument.getReportEmail(), reportItem);
        allValidationChecksPassed &= isOverviewValid(accountingXmlDocument.getOverview(), reportItem);
        return allValidationChecksPassed;
    }
    
    @Override
    public boolean isAllRequiredDataValid(AccountingXmlDocumentEntry accountingXmlDocument, CreateAccountingDocumentReportItemDetail reportItemDetail) {
        boolean allValidationChecksPassed = isIndexNumberValid(accountingXmlDocument.getIndex(), reportItemDetail);
        allValidationChecksPassed &= isDocumentTypeCodeValid(accountingXmlDocument.getDocumentTypeCode(), reportItemDetail);
        allValidationChecksPassed &= isDescriptionValid(accountingXmlDocument.getDescription(), reportItemDetail);
        allValidationChecksPassed &= isExplanationValid(accountingXmlDocument.getExplanation(), reportItemDetail);
        return allValidationChecksPassed;
    }
    
    private boolean isReportEmailAddressValid(String reportEmailAddress, CreateAccountingDocumentReportItem reportItem) {
        boolean emailAddressIsValid = true;
        if (StringUtils.isNotBlank(reportEmailAddress)) {
            try {
                InternetAddress emailAddr = new InternetAddress(reportEmailAddress);
                emailAddr.validate();
            } catch (AddressException ae) {
                appendValidationErrorToExistingReportItemMessage(reportItem, MessageFormat.format(
                        getConfigurationService().getPropertyValueAsString(
                        CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_INVALID_DATA_FOR_ELEMENT), reportEmailAddress,
                        CuFPConstants.CreateAccountingDocumentValidatedDataElements.REPORT_EMAIL));
                emailAddressIsValid = false;
                LOG.info("isReportEmailAddressValid: Detected invalid reportEmailAddress of: " + reportEmailAddress);
            }
        } else{
            appendValidationErrorToExistingReportItemMessage(reportItem, MessageFormat.format(
                    getConfigurationService().getPropertyValueAsString(
                    CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_NULL_BLANK_DATA_ELEMENT),
                    CuFPConstants.CreateAccountingDocumentValidatedDataElements.REPORT_EMAIL)); 
            emailAddressIsValid = false;
            LOG.info("isReportEmailAddressValid: Detected null reportEmailAddress.");
        }
        return emailAddressIsValid;
    }
    
    private boolean isOverviewValid(String overview, CreateAccountingDocumentReportItem reportItem) {
        boolean overviewIsValid = true;
        if (StringUtils.isBlank(overview)) {
            appendValidationErrorToExistingReportItemMessage(reportItem, MessageFormat.format(
                    getConfigurationService().getPropertyValueAsString(
                    CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_NULL_BLANK_DATA_ELEMENT),
                    CuFPConstants.CreateAccountingDocumentValidatedDataElements.OVERVIEW));
            overviewIsValid = false;
            LOG.info("isOverviewValid: Detected null overview.");
        }
        return overviewIsValid;
    }
    
    private boolean isIndexNumberValid(Long index, CreateAccountingDocumentReportItemDetail reportItemDetail) {
        boolean indexIsValid = true;
        if (ObjectUtils.isNull(index)) {
            reportItemDetail.appendErrorMessageToExistingErrorMessage(MessageFormat.format(
                    getConfigurationService().getPropertyValueAsString(CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_NULL_BLANK_DATA_ELEMENT),
                    CuFPConstants.CreateAccountingDocumentValidatedDataElements.INDEX));
            indexIsValid = false;
            LOG.info("isIndexNumberValid: Detected null index.");
        }
        return indexIsValid;
    }

    private boolean isDocumentTypeCodeValid(String documentTypeCode, CreateAccountingDocumentReportItemDetail reportItemDetail) {
        boolean documentTypeCodeIsValid = true;
        if (StringUtils.isBlank(documentTypeCode)) {
            reportItemDetail.appendErrorMessageToExistingErrorMessage(MessageFormat.format(
                    getConfigurationService().getPropertyValueAsString(CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_NULL_BLANK_DATA_ELEMENT),
                    CuFPConstants.CreateAccountingDocumentValidatedDataElements.DOCUMENT_TYPE));
            documentTypeCodeIsValid = false;
            LOG.info("isDocumentTypeCodeValid: Detected null documentTypeCode.");
        }
        return documentTypeCodeIsValid;
    }
    
    private boolean isDescriptionValid(String description, CreateAccountingDocumentReportItemDetail reportItemDetail) {
        boolean descriptionIsValid = true;
        if (StringUtils.isBlank(description)) {
            reportItemDetail.appendErrorMessageToExistingErrorMessage(MessageFormat.format(
                    getConfigurationService().getPropertyValueAsString(CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_NULL_BLANK_DATA_ELEMENT),
                    CuFPConstants.CreateAccountingDocumentValidatedDataElements.DESCRIPTION));
            descriptionIsValid = false;
            LOG.info("isDescriptionValid: Detected null description.");
        }
        return descriptionIsValid;
    }
    
    private boolean isExplanationValid(String explanation, CreateAccountingDocumentReportItemDetail reportItemDetail) {
        boolean explanationIsValid = true;
        if (StringUtils.isBlank(explanation)) {
            reportItemDetail.appendErrorMessageToExistingErrorMessage(MessageFormat.format(
                    getConfigurationService().getPropertyValueAsString(CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_NULL_BLANK_DATA_ELEMENT),
                    CuFPConstants.CreateAccountingDocumentValidatedDataElements.EXPLANATION));
            explanationIsValid = false;
            LOG.info("isExplanationValid: Detected null description.");
        }
        return explanationIsValid;
    }
    
    private void appendValidationErrorToExistingReportItemMessage(CreateAccountingDocumentReportItem reportItem, String additionalReportItemMessageContent) {
        String newErrorMessage;
        if (StringUtils.isBlank(org.apache.commons.lang3.ObjectUtils.defaultIfNull(reportItem.getValidationErrorMessage(), KFSConstants.EMPTY_STRING))) {
            newErrorMessage = additionalReportItemMessageContent;
        } else {
            newErrorMessage = reportItem.getValidationErrorMessage() + KFSConstants.NEWLINE + KFSConstants.NEWLINE + additionalReportItemMessageContent;
        }
        reportItem.setValidationErrorMessage(newErrorMessage);
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }
    
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
   
}
