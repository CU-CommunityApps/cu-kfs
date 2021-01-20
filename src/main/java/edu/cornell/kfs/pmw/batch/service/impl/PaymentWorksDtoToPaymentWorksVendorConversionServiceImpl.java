package edu.cornell.kfs.pmw.batch.service.impl;

import java.text.MessageFormat;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.kuali.rice.core.api.config.property.ConfigurationService;

import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.service.impl.exception.FormatException;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksKeyConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksPropertiesConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksFieldMapping;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportRawDataItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorRequestsBatchReportData;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksDtoToPaymentWorksVendorConversionService;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksBankAccountDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksBankAddressDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksCorpAddressDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksCustomFieldDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksCustomFieldsDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksNewVendorRequestDetailDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksRemittanceAddressDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksRequestingCompanyDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksTaxClassificationDTO;

public class PaymentWorksDtoToPaymentWorksVendorConversionServiceImpl implements PaymentWorksDtoToPaymentWorksVendorConversionService {
	private static final Logger LOG = LogManager.getLogger(PaymentWorksDtoToPaymentWorksVendorConversionServiceImpl.class);
	
	private static final String DASH = "-";
    
    protected BusinessObjectService businessObjectService;
    protected ConfigurationService configurationService;
    
    @Override
    public PaymentWorksVendor createPaymentWorksVendorFromPaymentWorksNewVendorRequestDetailDTO(PaymentWorksNewVendorRequestDetailDTO pmwNewVendorRequestDetailDTO, PaymentWorksNewVendorRequestsBatchReportData reportData) {
        PaymentWorksVendor stgVendor = new PaymentWorksVendor();
        if (newVendorDetailExists(pmwNewVendorRequestDetailDTO)) {
            stgVendor.setPmwVendorRequestId(pmwNewVendorRequestDetailDTO.getId());
            populateNewVendorRequestingCompanyAttributes(stgVendor, pmwNewVendorRequestDetailDTO);
            extractCustomFields(stgVendor, pmwNewVendorRequestDetailDTO, reportData);
        }
        else {
            reportData.getRecordsThatCouldNotBeProcessedSummary().incrementRecordCount();
            reportData.addPmwVendorThatCouldNotBeProcessed(new PaymentWorksBatchReportRawDataItem(pmwNewVendorRequestDetailDTO.toString(),
                    MessageFormat.format(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_DETAIL_WAS_NOT_FOUND_ERROR_MESSAGE), pmwNewVendorRequestDetailDTO.getId())));
        }
        return stgVendor;
    }
    
    private void populateNewVendorRequestingCompanyAttributes(PaymentWorksVendor stgNewVendor, PaymentWorksNewVendorRequestDetailDTO pmwNewVendorRequestDetailDTO) {
        if (requestingCompanyExists(pmwNewVendorRequestDetailDTO)) {
            PaymentWorksRequestingCompanyDTO pmwRequestingCompanyDTO = pmwNewVendorRequestDetailDTO.getRequesting_company();
            stgNewVendor.setRequestingCompanyId(pmwRequestingCompanyDTO.getId());
            stgNewVendor.setRequestingCompanyLegalName(pmwRequestingCompanyDTO.getLegal_name());
            stgNewVendor.setRequestingCompanyDesc(pmwRequestingCompanyDTO.getDesc());
            stgNewVendor.setRequestingCompanyName(pmwRequestingCompanyDTO.getName());
            stgNewVendor.setRequestingCompanyLegalLastName(pmwRequestingCompanyDTO.getLegal_last_name());
            stgNewVendor.setRequestingCompanyLegalFirstName(pmwRequestingCompanyDTO.getLegal_first_name());
            stgNewVendor.setRequestingCompanyUrl(pmwRequestingCompanyDTO.getUrl());
            stgNewVendor.setRequestingCompanyTin(pmwRequestingCompanyDTO.getTin());
            stgNewVendor.setRequestingCompanyTinType(pmwRequestingCompanyDTO.getTin_type());
            stgNewVendor.setRequestingCompanyTaxCountry(pmwRequestingCompanyDTO.getTax_country());
            stgNewVendor.setRequestingCompanyW8W9(pmwRequestingCompanyDTO.getW8_w9());
            stgNewVendor.setRequestingCompanyTelephone(pmwRequestingCompanyDTO.getTelephone());
            stgNewVendor.setRequestingCompanyDuns(pmwRequestingCompanyDTO.getDuns());
            stgNewVendor.setRequestingCompanyCorporateEmail(pmwRequestingCompanyDTO.getCorporate_email());
            populateNewVendorCorporateAddressAttributes(stgNewVendor, pmwRequestingCompanyDTO);
            populateNewVendorRemittanceAddressAttributes(stgNewVendor, pmwRequestingCompanyDTO);
            populateNewVendorTaxClassificationAttributes(stgNewVendor, pmwRequestingCompanyDTO);
        }
    }
    
    private void populateNewVendorCorporateAddressAttributes(PaymentWorksVendor stgNewVendor, PaymentWorksRequestingCompanyDTO pmwRequestingCompanyDTO) {
        if (corporateAddressExists(pmwRequestingCompanyDTO)) {
            PaymentWorksCorpAddressDTO pmwCorpAddressDTO = pmwRequestingCompanyDTO.getCorp_address();
            stgNewVendor.setCorpAddressStreet1(pmwCorpAddressDTO.getStreet1());
            stgNewVendor.setCorpAddressStreet2(pmwCorpAddressDTO.getStreet2());
            stgNewVendor.setCorpAddressCity(pmwCorpAddressDTO.getCity());
            stgNewVendor.setCorpAddressState(pmwCorpAddressDTO.getState());
            stgNewVendor.setCorpAddressCountry(pmwCorpAddressDTO.getCountry());
            stgNewVendor.setCorpAddressZipCode(pmwCorpAddressDTO.getZipcode());
            stgNewVendor.setCorpAddressValidated(pmwCorpAddressDTO.getValidated());
        }
    }

    private void populateNewVendorRemittanceAddressAttributes(PaymentWorksVendor stgNewVendor, PaymentWorksRequestingCompanyDTO pmwRequestingCompanyDTO) {
        if (singleRemittanceAddressExists(pmwRequestingCompanyDTO)) {
            PaymentWorksRemittanceAddressDTO pmwRemittanceAddressDTO = pmwRequestingCompanyDTO.getRemittance_addresses().getRemittance_address().get(0);
            stgNewVendor.setRemittanceAddressStreet1(pmwRemittanceAddressDTO.getStreet1());
            stgNewVendor.setRemittanceAddressStreet2(pmwRemittanceAddressDTO.getStreet2());
            stgNewVendor.setRemittanceAddressCity(pmwRemittanceAddressDTO.getCity());
            stgNewVendor.setRemittanceAddressState(pmwRemittanceAddressDTO.getState());
            stgNewVendor.setRemittanceAddressCountry(pmwRemittanceAddressDTO.getCountry());
            stgNewVendor.setRemittanceAddressZipCode(pmwRemittanceAddressDTO.getZipcode());
            stgNewVendor.setRemittanceAddressValidated(pmwRemittanceAddressDTO.getValidated());
            populateNewVendorBankAccountAttributes(stgNewVendor, pmwRemittanceAddressDTO);
        }
    }
    
    private void populateNewVendorTaxClassificationAttributes(PaymentWorksVendor stgNewVendor, PaymentWorksRequestingCompanyDTO pmwRequestingCompanyDTO) {
        if (taxClassificationExists(pmwRequestingCompanyDTO)) {
            PaymentWorksTaxClassificationDTO pmwTaxClassificationDTO = pmwRequestingCompanyDTO.getTax_classification();
            stgNewVendor.setRequestingCompanyTaxClassificationName(pmwTaxClassificationDTO.getName());
            stgNewVendor.setRequestingCompanyTaxClassificationCode(pmwTaxClassificationDTO.getCode());
        }
    }

    private void populateNewVendorBankAccountAttributes(PaymentWorksVendor stgNewVendor, PaymentWorksRemittanceAddressDTO pmwRemittanceAddressDTO) {
        if (bankAccountDataExists(pmwRemittanceAddressDTO)) {
            PaymentWorksBankAccountDTO pmwBankAccountDTO = pmwRemittanceAddressDTO.getBank_acct();
            stgNewVendor.setBankAcctBankName(pmwBankAccountDTO.getBank_name());
            stgNewVendor.setBankAcctBankAccountNumber(sanitizeBankAcctBankAccountNumberValue(pmwBankAccountDTO.getBank_acct_num()));
            stgNewVendor.setBankAcctBankValidationFile(pmwBankAccountDTO.getValidation_file());
            stgNewVendor.setBankAcctAchEmail(pmwBankAccountDTO.getAch_email());
            stgNewVendor.setBankAcctRoutingNumber(pmwBankAccountDTO.getRouting_num());
            stgNewVendor.setBankAcctType(pmwBankAccountDTO.getBank_acct_type());
            stgNewVendor.setBankAcctAuthorized(pmwBankAccountDTO.getAuthorized());
            stgNewVendor.setBankAcctSwiftCode(pmwBankAccountDTO.getSwift_code());
            stgNewVendor.setBankAcctNameOnAccount(pmwBankAccountDTO.getName_on_acct());
            populateNewVendorBankAddressAttributes(stgNewVendor, pmwBankAccountDTO);
        }
    }
    
    protected String sanitizeBankAcctBankAccountNumberValue(String pmwBankAccountNumber) {
        if (StringUtils.isNotBlank(pmwBankAccountNumber) && StringUtils.contains(pmwBankAccountNumber, DASH)) {
            String sanitizedPmwBankAccountNumberValue = StringUtils.replace(pmwBankAccountNumber, DASH, StringUtils.EMPTY);
            LOG.info("sanitizeBankAcctBankAccountNumberValue: the PaymentWorks bank account number value: " + pmwBankAccountNumber + " has been sanitized to: "
                    + sanitizedPmwBankAccountNumberValue);
            return sanitizedPmwBankAccountNumberValue;
        }
        return pmwBankAccountNumber;

    }

    private void populateNewVendorBankAddressAttributes(PaymentWorksVendor stgNewVendor, PaymentWorksBankAccountDTO pmwBankAccountDTO) {
        if (bankAddressExists(pmwBankAccountDTO)) {
            PaymentWorksBankAddressDTO pmwBankAddressDTO = pmwBankAccountDTO.getBank_address();
            stgNewVendor.setBankAddressStreet1(pmwBankAddressDTO.getStreet1());
            stgNewVendor.setBankAddressStreet2(pmwBankAddressDTO.getStreet2());
            stgNewVendor.setBankAddressCity(pmwBankAddressDTO.getCity());
            stgNewVendor.setBankAddressState(pmwBankAddressDTO.getState());
            stgNewVendor.setBankAddressCountry(pmwBankAddressDTO.getCountry());
            stgNewVendor.setBankAddressZipCode(pmwBankAddressDTO.getZipcode());
            stgNewVendor.setBankAddressValidated(pmwBankAddressDTO.getValidated());
        }
    }

    private boolean newVendorDetailExists(PaymentWorksNewVendorRequestDetailDTO paymentWorksNewVendorDetailDTO) {
        return (ObjectUtils.isNotNull(paymentWorksNewVendorDetailDTO));
    }
    
    private boolean requestingCompanyExists(PaymentWorksNewVendorRequestDetailDTO paymentWorksNewVendorDetailDTO) {
        return (ObjectUtils.isNotNull(paymentWorksNewVendorDetailDTO.getRequesting_company()));
    }
    
    private boolean singleRemittanceAddressExists(PaymentWorksRequestingCompanyDTO pmwRequestingCompanyDTO) {
        return (ObjectUtils.isNotNull(pmwRequestingCompanyDTO.getRemittance_addresses()) &&
                ObjectUtils.isNotNull(pmwRequestingCompanyDTO.getRemittance_addresses().getRemittance_address()) &&
                pmwRequestingCompanyDTO.getRemittance_addresses().getRemittance_address().size() == 1);
    }

    private boolean corporateAddressExists(PaymentWorksRequestingCompanyDTO pmwRequestingCompanyDTO) {
        return (ObjectUtils.isNotNull(pmwRequestingCompanyDTO.getCorp_address()));
    }

    private boolean bankAccountDataExists(PaymentWorksRemittanceAddressDTO pmwRemittanceAddressDTO) {
        return (ObjectUtils.isNotNull(pmwRemittanceAddressDTO.getBank_acct()));
    }

    private boolean bankAddressExists(PaymentWorksBankAccountDTO pmwBankAccountDTO) {
        return (ObjectUtils.isNotNull(pmwBankAccountDTO.getBank_address()));
    }
    
    private boolean taxClassificationExists(PaymentWorksRequestingCompanyDTO pmwRequestingCompanyDTO) {
        return (ObjectUtils.isNotNull(pmwRequestingCompanyDTO.getTax_classification()));
    }

    private void extractCustomFields(PaymentWorksVendor stgNewVendor, PaymentWorksNewVendorRequestDetailDTO pmwNewVendorDetailDTO, PaymentWorksNewVendorRequestsBatchReportData reportData) {
        List<String> customFieldErrors = new ArrayList<String>();
        processEachPaymentWorksCustomFieldReceived(stgNewVendor, pmwNewVendorDetailDTO, customFieldErrors);
        if (errorsGeneratedForValueConversionOfPaymentWorksCustomFieldToPaymentWorksVendorAttribute(stgNewVendor)) {
            reportData.getRecordsThatCouldNotBeProcessedSummary().incrementRecordCount();
            reportData.addPmwVendorThatCouldNotBeProcessed(new PaymentWorksBatchReportRawDataItem(stgNewVendor.toString(), customFieldErrors));
        }
    }

    private Map<String, String> createCustomFieldIdCustomFieldValueMapForReceivedPmwVendorData(PaymentWorksCustomFieldsDTO pmwCustomFieldsDTO) {
        Map<String, String> customFieldMap = new HashMap<String, String>();
        if (ObjectUtils.isNotNull(pmwCustomFieldsDTO) && ObjectUtils.isNotNull(pmwCustomFieldsDTO.getCustom_fields())) {
            pmwCustomFieldsDTO.getCustom_fields().stream()
                                                 .forEach(paymentWorksCustomFieldDTO -> {
                                                     customFieldMap.put(paymentWorksCustomFieldDTO.getField_id(), paymentWorksCustomFieldDTO.getField_value());
                                                 });
        }
        return customFieldMap;
    }

    private void processEachPaymentWorksCustomFieldReceived(PaymentWorksVendor stgNewVendor, PaymentWorksNewVendorRequestDetailDTO pmwNewVendorDetailDTO, List<String> customFieldErrors) {
        for (PaymentWorksCustomFieldDTO customField : pmwNewVendorDetailDTO.getCustom_fields().getCustom_fields()) {
            String customFieldId = customField.getField_id();
            PaymentWorksFieldMapping fieldMapping = findPaymentWorksFieldMapping(customFieldId);
            if (ObjectUtils.isNotNull(fieldMapping)) {
                String customFieldValue = findCustomFieldValue(customField, fieldMapping);
                setPaymentWorksVendorCustomFieldAttributeToReceivedValue(stgNewVendor, customFieldId, customFieldValue, fieldMapping, customFieldErrors);
            } else {
                stgNewVendor.setCustomFieldConversionErrors(true);
                customFieldErrors.add(MessageFormat.format(getConfigurationService().getPropertyValueAsString(
                        PaymentWorksKeyConstants.NEW_VENDOR_REQUEST_CUSTOM_FIELD_MISSING_ERROR_MESSAGE), customFieldId));
                LOG.error("processEachPaymentWorksCustomFieldReceived: Unable to find KFS staging table column for PaymentWorks custom field '" + 
                        customFieldId + "'");
            }
        }
    }

    protected String findCustomFieldValue(PaymentWorksCustomFieldDTO customField, PaymentWorksFieldMapping fieldMapping) {
        String customFieldValue = getValueOutOfPaymentWorksCustomFieldDTO(customField, fieldMapping);
        return customFieldValue;
    }
    
    protected String getValueOutOfPaymentWorksCustomFieldDTO(PaymentWorksCustomFieldDTO dto, PaymentWorksFieldMapping fieldMapping) {
        if (StringUtils.equalsIgnoreCase(fieldMapping.getCustomAttributeValueToUse(), PaymentWorksConstants.CustomAttributeValueToUse.FILE)) {
            return dto.getFile();
        } else if (StringUtils.equalsIgnoreCase(fieldMapping.getCustomAttributeValueToUse(), PaymentWorksConstants.CustomAttributeValueToUse.FIELD_VALUE)) {
            return dto.getField_value();
        } else {
            throw new IllegalArgumentException("The custom attrbute value to use is not valid: " + fieldMapping.getCustomAttributeValueToUse());
        }
    }
    
    private void setPaymentWorksVendorCustomFieldAttributeToReceivedValue(PaymentWorksVendor stgNewVendor, String paymentWorksCustomFieldId, String customFieldValue, 
            PaymentWorksFieldMapping fieldMapping, List<String> customFieldErrors) {
        Object propertyValueForSetter = convertStringValueToObjectForPropertySetting(customFieldValue);
        if (ObjectUtils.isNotNull(propertyValueForSetter)) {
            try {
                ObjectUtils.setObjectProperty(stgNewVendor, fieldMapping.getKfsPaymentWorksStagingTableColumn(), propertyValueForSetter);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("setPaymentWorksVendorCustomFieldAttributeToReceivedValue: setting KFS field " + fieldMapping.getKfsPaymentWorksStagingTableColumn()
                            + "' for PaymentWorks fieldId '" + paymentWorksCustomFieldId
                            + " and is of object type " + propertyValueForSetter.getClass());
                }
            } catch (FormatException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                stgNewVendor.setCustomFieldConversionErrors(true);
                customFieldErrors.add(MessageFormat.format(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_REQUEST_CUSTOM_FIELD_CONVERSION_EXCEPTION_ERROR_MESSAGE), paymentWorksCustomFieldId,  e.toString()));
                LOG.error("setPaymentWorksVendorCustomFieldAttributeToReceivedValue: Unable to set KFS staging table column '" + fieldMapping.getKfsPaymentWorksStagingTableColumn()
                        + "' for PaymentWorks fieldId '" + paymentWorksCustomFieldId + "'  Due to the error: "
                        + e.getMessage(), e);
            }
        }
    }

    private boolean errorsGeneratedForValueConversionOfPaymentWorksCustomFieldToPaymentWorksVendorAttribute(PaymentWorksVendor stgNewVendor) {
        return stgNewVendor.isCustomFieldConversionErrors();
    }
    
    protected PaymentWorksFieldMapping findPaymentWorksFieldMapping(String pmwCustomFieldId) {
        Map fieldValues = new HashMap();
        fieldValues.put(PaymentWorksPropertiesConstants.PaymentWorksFieldMapping.PMW_FIELD_ID, StringUtils.trim(pmwCustomFieldId));
        Collection mappings = getBusinessObjectService().findMatching(PaymentWorksFieldMapping.class, fieldValues);
        if (!mappings.isEmpty()) {
            return (PaymentWorksFieldMapping) mappings.iterator().next();
        }
        return null;
    }
    
    protected Object convertStringValueToObjectForPropertySetting(String value) {
        if (StringUtils.equalsIgnoreCase(value, PaymentWorksConstants.PaymentWorksCustomFieldBooleanPrimitive.YES) || 
            StringUtils.equalsIgnoreCase(value, PaymentWorksConstants.PaymentWorksCustomFieldBooleanPrimitive.NO)) {
            return StringUtils.equalsIgnoreCase(value, PaymentWorksConstants.PaymentWorksCustomFieldBooleanPrimitive.YES);
        } else if (StringUtils.isBlank(value)) {
            return null;
        } else {
            return value;
        }
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}
