package edu.cornell.kfs.fp.batch.service.impl;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventLocator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.springframework.beans.factory.InitializingBean;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItem;
import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItemDetail;
import edu.cornell.kfs.fp.batch.service.CreateAccountingDocumentValidationService;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentListWrapper;

public class CreateAccountingDocumentValidationServiceImpl implements CreateAccountingDocumentValidationService,
        InitializingBean {
    private static final Logger LOG = LogManager.getLogger(CreateAccountingDocumentValidationServiceImpl.class);
    
    private static final String EXCEPTION_CLASSNAME_GROUP = "exceptionClassname";
    private static final String DETAIL_MESSAGE_GROUP = "detailMessage";
    
    protected ConfigurationService configurationService;
    protected Pattern exceptionMessagePattern;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (configurationService == null) {
            throw new IllegalStateException("configurationService was not set");
        }
        String regex = getStringProperty(CuFPKeyConstants.VALIDATION_CREATE_ACCOUNTING_DOCUMENT_EXCEPTION_MESSAGE_REGEX);
        if (StringUtils.isBlank(regex)) {
            throw new IllegalStateException("Exception message regex has not been defined or is blank");
        }
        exceptionMessagePattern = Pattern.compile(regex);
    }

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
        allValidationChecksPassed &= documentWasParsedWithoutErrors(accountingXmlDocument, reportItemDetail);
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
    
    private boolean documentWasParsedWithoutErrors(AccountingXmlDocumentEntry accountingXmlDocument,
            CreateAccountingDocumentReportItemDetail reportItemDetail) {
        if (CollectionUtils.isNotEmpty(accountingXmlDocument.getValidationErrors())) {
            for (ValidationEvent validationError : accountingXmlDocument.getValidationErrors()) {
                reportItemDetail.appendErrorMessageToExistingErrorMessage(
                        buildValidationErrorMessageForUser(validationError));
            }
            LOG.info("documentWasParsedWithoutErrors: Detected one or more parsing validation errors.");
            return false;
        } else {
            return true;
        }
    }
    
    private String buildValidationErrorMessageForUser(ValidationEvent validationError) {
        String subMessage = getErrorSubMessage(validationError);
        int lineNumber = getErrorLineNumber(validationError);
        if (lineNumber != -1) {
            return formatMessageFromProperty(CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_XML_ADAPTER_ERROR,
                    lineNumber, subMessage);
        } else {
            return subMessage;
        }
    }
    
    private String getErrorSubMessage(ValidationEvent validationError) {
        String eventMessage = getErrorEventMessage(validationError);
        LOG.warn("getErrorSubMessage: Detected validation error when parsing XML document: "
                + eventMessage);
        Matcher matcher = exceptionMessagePattern.matcher(eventMessage);
        if (!matcher.matches()) {
            return eventMessage;
        }
        
        String detailMessage = matcher.group(DETAIL_MESSAGE_GROUP);
        String exceptionClassname = matcher.group(EXCEPTION_CLASSNAME_GROUP);
        if (StringUtils.isBlank(exceptionClassname)) {
            LOG.warn("getErrorSubMessage: Pattern matched message but it does not contain exception classname");
        }
        return getUserFriendlySubMessage(detailMessage, exceptionClassname);
    }
    
    private String getUserFriendlySubMessage(String detailMessage, String exceptionClassname) {
        if (StringUtils.isNotBlank(detailMessage)) {
            return detailMessage;
        } else if (StringUtils.equals(NumberFormatException.class.getName(), exceptionClassname)) {
            return getStringProperty(CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_GENERIC_NUMERIC_ERROR);
        } else {
            return getStringProperty(CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_GENERIC_ERROR);
        }
    }
    
    private String getErrorEventMessage(ValidationEvent event) {
        String errorMessage = event.getMessage();
        if (StringUtils.isBlank(errorMessage)) {
            Throwable linkedException = event.getLinkedException();
            if (linkedException != null) {
                errorMessage = linkedException.getMessage();
            }
            if (StringUtils.isBlank(errorMessage)) {
                errorMessage = getStringProperty(CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_GENERIC_ERROR);
            }
        }
        return errorMessage;
    }
    
    private int getErrorLineNumber(ValidationEvent event) {
        ValidationEventLocator locator = event.getLocator();
        return locator != null ? locator.getLineNumber() : -1;
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

    private String formatMessageFromProperty(String propertyKey, Object... arguments) {
        String patternText = getStringProperty(propertyKey);
        return MessageFormat.format(patternText, arguments);
    }

    private String getStringProperty(String propertyKey) {
        return configurationService.getPropertyValueAsString(propertyKey);
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }
    
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
