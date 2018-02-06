package edu.cornell.kfs.pmw.batch.service.impl;

import java.text.MessageFormat;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.web.format.FormatException;

import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksKeyConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksPropertiesConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksFieldMapping;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportRawDataItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorRequestsBatchReportData;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksDtoToPaymentWorksVendorConversionService;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksAddressBaseDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksBankAccountDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksCustomFieldDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksCustomFieldsDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksNewVendorRequestDetailDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksRemittanceAddressDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksRequestingCompanyDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksTaxClassificationDTO;

public class PaymentWorksDtoToPaymentWorksVendorConversionServiceImpl implements PaymentWorksDtoToPaymentWorksVendorConversionService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksDtoToPaymentWorksVendorConversionServiceImpl.class);
    
    protected BusinessObjectService businessObjectService;
    protected ConfigurationService configurationService;
    
    @Override
    public PaymentWorksVendor createPaymentWorksVendorFromPaymentWorksNewVendorRequestDetailDTO(PaymentWorksNewVendorRequestDetailDTO pmwNewVendorRequestDetailDTO, PaymentWorksNewVendorRequestsBatchReportData reportData) {
        PaymentWorksVendor stgVendor = new PaymentWorksVendor();
        if (newVendorDetailExists(pmwNewVendorRequestDetailDTO)) {
            stgVendor.setPmwVendorRequestId(pmwNewVendorRequestDetailDTO.getPaymentWorksVendorId());
            populateNewVendorRequestingCompanyAttributes(stgVendor, pmwNewVendorRequestDetailDTO);
            extractCustomFields(stgVendor, pmwNewVendorRequestDetailDTO, reportData);
        }
        else {
            reportData.getPendingPaymentWorksVendorsThatCouldNotBeProcessed().incrementRecordCount();
            reportData.addPmwVendorsThatCouldNotBeProcessed(new PaymentWorksBatchReportRawDataItem(pmwNewVendorRequestDetailDTO.toString(),
                    MessageFormat.format(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_DETAIL_WAS_NOT_FOUND_ERROR_MESSAGE), pmwNewVendorRequestDetailDTO.getPaymentWorksVendorId())));
        }
        return stgVendor;
    }
    
    private void populateNewVendorRequestingCompanyAttributes(PaymentWorksVendor stgNewVendor, PaymentWorksNewVendorRequestDetailDTO pmwNewVendorRequestDetailDTO) {
        if (requestingCompanyExists(pmwNewVendorRequestDetailDTO)) {
            PaymentWorksRequestingCompanyDTO pmwRequestingCompanyDTO = pmwNewVendorRequestDetailDTO.getRequestingCompany();
            stgNewVendor.setRequestingCompanyId(pmwRequestingCompanyDTO.getId());
            stgNewVendor.setRequestingCompanyLegalName(pmwRequestingCompanyDTO.getCompanyLegalName());
            stgNewVendor.setRequestingCompanyDesc(pmwRequestingCompanyDTO.getDescription());
            stgNewVendor.setRequestingCompanyName(pmwRequestingCompanyDTO.getCompanyName());
            stgNewVendor.setRequestingCompanyLegalLastName(pmwRequestingCompanyDTO.getLegalLastName());
            stgNewVendor.setRequestingCompanyLegalFirstName(pmwRequestingCompanyDTO.getLegalFirstName());
            stgNewVendor.setRequestingCompanyUrl(pmwRequestingCompanyDTO.getUrl());
            stgNewVendor.setRequestingCompanyTin(pmwRequestingCompanyDTO.getTin());
            stgNewVendor.setRequestingCompanyTinType(pmwRequestingCompanyDTO.getTinType());
            stgNewVendor.setRequestingCompanyTaxCountry(pmwRequestingCompanyDTO.getTaxCountry());
            stgNewVendor.setRequestingCompanyW8W9(pmwRequestingCompanyDTO.getW8w9Url());
            stgNewVendor.setRequestingCompanyTelephone(pmwRequestingCompanyDTO.getTelephone());
            stgNewVendor.setRequestingCompanyDuns(pmwRequestingCompanyDTO.getDuns());
            stgNewVendor.setRequestingCompanyCorporateEmail(pmwRequestingCompanyDTO.getCorporateEmail());
            populateNewVendorCorporateAddressAttributes(stgNewVendor, pmwRequestingCompanyDTO);
            populateNewVendorRemittanceAddressAttributes(stgNewVendor, pmwRequestingCompanyDTO);
            populateNewVendorTaxClassificationAttributes(stgNewVendor, pmwRequestingCompanyDTO);
        }
    }
    
    private void populateNewVendorCorporateAddressAttributes(PaymentWorksVendor stgNewVendor, PaymentWorksRequestingCompanyDTO pmwRequestingCompanyDTO) {
        if (corporateAddressExists(pmwRequestingCompanyDTO)) {
            PaymentWorksAddressBaseDTO pmwCorpAddressDTO = pmwRequestingCompanyDTO.getCorporateAddress();
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
            PaymentWorksRemittanceAddressDTO pmwRemittanceAddressDTO = pmwRequestingCompanyDTO.getRemittanceAddresses().getRemittanceAddresses().get(0);
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
            PaymentWorksTaxClassificationDTO pmwTaxClassificationDTO = pmwRequestingCompanyDTO.getTaxClassification();
            stgNewVendor.setRequestingCompanyTaxClassificationName(pmwTaxClassificationDTO.getName());
            stgNewVendor.setRequestingCompanyTaxClassificationCode(pmwTaxClassificationDTO.getCode());
        }
    }

    private void populateNewVendorBankAccountAttributes(PaymentWorksVendor stgNewVendor, PaymentWorksRemittanceAddressDTO pmwRemittanceAddressDTO) {
        if (bankAccountDataExists(pmwRemittanceAddressDTO)) {
            PaymentWorksBankAccountDTO pmwBankAccountDTO = pmwRemittanceAddressDTO.getBankAccount();
            stgNewVendor.setBankAcctBankName(pmwBankAccountDTO.getBankName());
            stgNewVendor.setBankAcctBankAccountNumber(pmwBankAccountDTO.getBankAccountNumber());
            stgNewVendor.setBankAcctBankValidationFile(pmwBankAccountDTO.getValidationFile());
            stgNewVendor.setBankAcctAchEmail(pmwBankAccountDTO.getAchEmail());
            stgNewVendor.setBankAcctRoutingNumber(pmwBankAccountDTO.getRoutingNumber());
            stgNewVendor.setBankAcctType(pmwBankAccountDTO.getBankAccountType());
            stgNewVendor.setBankAcctAuthorized(pmwBankAccountDTO.getAuthorized());
            stgNewVendor.setBankAcctSwiftCode(pmwBankAccountDTO.getSwiftCode());
            stgNewVendor.setBankAcctNameOnAccount(pmwBankAccountDTO.getNameOnAccount());
            populateNewVendorBankAddressAttributes(stgNewVendor, pmwBankAccountDTO);
        }
    }

    private void populateNewVendorBankAddressAttributes(PaymentWorksVendor stgNewVendor, PaymentWorksBankAccountDTO pmwBankAccountDTO) {
        if (bankAddressExists(pmwBankAccountDTO)) {
            PaymentWorksAddressBaseDTO pmwBankAddressDTO = pmwBankAccountDTO.getBankAddress();
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
        return (ObjectUtils.isNotNull(paymentWorksNewVendorDetailDTO.getRequestingCompany()));
    }
    
    private boolean singleRemittanceAddressExists(PaymentWorksRequestingCompanyDTO pmwRequestingCompanyDTO) {
        return (ObjectUtils.isNotNull(pmwRequestingCompanyDTO.getRemittanceAddresses()) &&
                ObjectUtils.isNotNull(pmwRequestingCompanyDTO.getRemittanceAddresses().getRemittanceAddresses()) &&
                pmwRequestingCompanyDTO.getRemittanceAddresses().getRemittanceAddresses().size() == 1);
    }

    private boolean corporateAddressExists(PaymentWorksRequestingCompanyDTO pmwRequestingCompanyDTO) {
        return (ObjectUtils.isNotNull(pmwRequestingCompanyDTO.getCorporateAddress()));
    }

    private boolean bankAccountDataExists(PaymentWorksRemittanceAddressDTO pmwRemittanceAddressDTO) {
        return (ObjectUtils.isNotNull(pmwRemittanceAddressDTO.getBankAccount()));
    }

    private boolean bankAddressExists(PaymentWorksBankAccountDTO pmwBankAccountDTO) {
        return (ObjectUtils.isNotNull(pmwBankAccountDTO.getBankAddress()));
    }
    
    private boolean taxClassificationExists(PaymentWorksRequestingCompanyDTO pmwRequestingCompanyDTO) {
        return (ObjectUtils.isNotNull(pmwRequestingCompanyDTO.getTaxClassification()));
    }

    private void extractCustomFields(PaymentWorksVendor stgNewVendor, PaymentWorksNewVendorRequestDetailDTO pmwNewVendorDetailDTO, PaymentWorksNewVendorRequestsBatchReportData reportData) {
        List<String> customFieldErrors = new ArrayList<String>();
        processEachPaymentWorksCustomFieldReceived(stgNewVendor, pmwNewVendorDetailDTO, customFieldErrors);
        if (errorsGeneratedForValueConversionOfPaymentWorksCustomFieldToPaymentWorksVendorAttribute(stgNewVendor)) {
            reportData.getPendingPaymentWorksVendorsThatCouldNotBeProcessed().incrementRecordCount();
            reportData.addPmwVendorsThatCouldNotBeProcessed(new PaymentWorksBatchReportRawDataItem(stgNewVendor.toString(), customFieldErrors));
        }
    }

    private Map<String, String> createCustomFieldIdCustomFieldValueMapForReceivedPmwVendorData(PaymentWorksCustomFieldsDTO pmwCustomFieldsDTO) {
        Map<String, String> customFieldMap = new HashMap<String, String>();
        if (ObjectUtils.isNotNull(pmwCustomFieldsDTO) && ObjectUtils.isNotNull(pmwCustomFieldsDTO.getCustomFields())) {
            pmwCustomFieldsDTO.getCustomFields().stream()
                                                .forEach(paymentWorksCustomFieldDTO -> {
                                                     customFieldMap.put(paymentWorksCustomFieldDTO.getFieldId(), paymentWorksCustomFieldDTO.getFieldValue());
                                                 });
        }
        return customFieldMap;
    }

    private void processEachPaymentWorksCustomFieldReceived(PaymentWorksVendor stgNewVendor, PaymentWorksNewVendorRequestDetailDTO pmwNewVendorDetailDTO, List<String> customFieldErrors) {
        Map<String, String> customFieldsDTOMap = createCustomFieldIdCustomFieldValueMapForReceivedPmwVendorData(pmwNewVendorDetailDTO.getCustomFields());
        for (String paymentWorksCustomFieldId : customFieldsDTOMap.keySet()) {
            String customFieldValueFromPaymentWorks = customFieldsDTOMap.get(paymentWorksCustomFieldId);
            PaymentWorksFieldMapping fieldMapping = findPaymentWorksFieldMapping(paymentWorksCustomFieldId);
            if (ObjectUtils.isNotNull(fieldMapping)) {
                setPaymentWorksVendorCustomFieldAttributeToReceivedValue(stgNewVendor, paymentWorksCustomFieldId, fieldMapping, customFieldsDTOMap, customFieldErrors);
            } 
            else {
                stgNewVendor.setCustomFieldConversionErrors(true);
                customFieldErrors.add(MessageFormat.format(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_REQUEST_CUSTOM_FIELD_MISSING_ERROR_MESSAGE), paymentWorksCustomFieldId));
                LOG.error("processEachPaymentWorksCustomFieldReceived: Unable to find KFS staging table column for PaymentWorks custom field '" + paymentWorksCustomFieldId + "'");
            }
        }
    }
    
    private void setPaymentWorksVendorCustomFieldAttributeToReceivedValue(PaymentWorksVendor stgNewVendor, String paymentWorksCustomFieldId, PaymentWorksFieldMapping fieldMapping, Map<String, String> customFieldsDTOMap, List<String> customFieldErrors) {
        Object propertyValueForSetter = convertStringValueToObjectForPropertySetting(customFieldsDTOMap.get(paymentWorksCustomFieldId));
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
