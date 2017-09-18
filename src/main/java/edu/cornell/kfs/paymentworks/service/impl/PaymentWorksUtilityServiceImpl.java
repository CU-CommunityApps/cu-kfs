/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2014 The Kuali Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.paymentworks.service.impl;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.vnd.service.PhoneNumberService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kns.service.DataDictionaryService;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.util.AutoPopulatingList;

import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.service.PaymentWorksUtilityService;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksCustomFieldDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksCustomFieldsDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksFieldChangeDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksFieldChangesDTO;
import edu.cornell.kfs.vnd.CUVendorConstants;

public class PaymentWorksUtilityServiceImpl implements PaymentWorksUtilityService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksUtilityServiceImpl.class);

    protected DataDictionaryService dataDictionaryService;
    protected PhoneNumberService phoneNumberService;
    protected ConfigurationService configurationService;
    protected VendorService vendorService;

    @Override
    public String getGlobalErrorMessage() {
        String errorMessage = "";

        if (GlobalVariables.getMessageMap().getErrorCount() > 0) {
            errorMessage = StringEscapeUtils.unescapeHtml(getAutoPopulatingErrorMessages(GlobalVariables.getMessageMap().getErrorMessages()));
            GlobalVariables.getMessageMap().clearErrorMessages();
        }

        return errorMessage;
    }

    @Override
    public String getAutoPopulatingErrorMessages(Map<String, AutoPopulatingList<ErrorMessage>> errorMap) {

        AutoPopulatingList<ErrorMessage> errorMessages = null;
        ErrorMessage errorMessage = null;
        StringBuffer errorList = new StringBuffer("");
        String errorText = null;

        for (Map.Entry<String, AutoPopulatingList<ErrorMessage>> errorEntry : errorMap.entrySet()) {

            errorMessages = errorEntry.getValue();

            for (int i = 0; i < errorMessages.size(); i++) {

                errorMessage = errorMessages.get(i);

                // get error text
                errorText = getConfigurationService().getPropertyValueAsString(errorMessage.getErrorKey());
                // apply parameters
                errorText = MessageFormat.format(errorText, (Object[]) errorMessage.getMessageParameters());

                // add key and error message together
                errorList.append(errorText + "\n");
            }
        }

        return errorList.toString();
    }

    @Override
    public String pojoToJsonString(Object object) {
        ObjectMapper mapper = new ObjectMapper();

        // Object to JSON in String
        String jsonVendorStatusString = null;
        try {
            jsonVendorStatusString = mapper.writeValueAsString(object);
        } catch (Exception e) {
            jsonVendorStatusString = null;
        }

        return jsonVendorStatusString;
    }

    @Override
    public Map<String, String> convertFieldArrayToMap(PaymentWorksCustomFieldsDTO customFields) {

        Map<String, String> customFieldMap = new HashMap<String, String>();

        if (ObjectUtils.isNotNull(customFields) && ObjectUtils.isNotNull(customFields.getCustom_fields())) {
            for (PaymentWorksCustomFieldDTO customField : customFields.getCustom_fields()) {
                customFieldMap.put(customField.getField_label(), customField.getField_value());
            }
        }

        return customFieldMap;
    }

    @Override
    public Map<String, String> convertFieldArrayToMap(PaymentWorksFieldChangesDTO fieldChanges) {

        Map<String, String> customFieldMap = new HashMap<String, String>();

        if (ObjectUtils.isNotNull(fieldChanges) && ObjectUtils.isNotNull(fieldChanges.getField_changes())) {
            for (PaymentWorksFieldChangeDTO fieldChange : fieldChanges.getField_changes()) {
                customFieldMap.put(fieldChange.getField_name(), fieldChange.getTo_value());
            }
        }

        return customFieldMap;
    }

    @Override
    public Map<String, String> convertFieldArrayToMapFromValues(PaymentWorksFieldChangesDTO fieldChanges) {

        Map<String, String> customFieldMap = new HashMap<String, String>();

        if (ObjectUtils.isNotNull(fieldChanges) && ObjectUtils.isNotNull(fieldChanges.getField_changes())) {
            for (PaymentWorksFieldChangeDTO fieldChange : fieldChanges.getField_changes()) {
                customFieldMap.put(fieldChange.getField_name(), fieldChange.getFrom_value());
            }
        }

        return customFieldMap;
    }

    @Override
    public String trimFieldToMax(String field, String fieldName) {
        String returnField = field;

        try {
            returnField = StringUtils.substring(returnField, 0,
                    getDataDictionaryService().getAttributeMaxLength(PaymentWorksVendor.class.getName(), fieldName));
        } catch (Exception e) {
            // ignore and return original value
        }

        return returnField;
    }

    @Override
    public String convertPhoneNumber(String phoneNumber) {
        return getPhoneNumberService().formatNumberIfPossible(phoneNumber);
    }

    @Override
    public boolean shouldVendorBeSentToPaymentWorks(VendorDetail vendorDetail) {
        boolean sendToPaymentWorks = vendorDetail.isActiveIndicator()
                && isVendorTypeSendableToPaymentWorks(vendorDetail.getVendorHeader().getVendorTypeCode());

        LOG.debug("shouldVendorBeSentToPaymentWorks1, sendToPaymentWorks: " + sendToPaymentWorks);
        return sendToPaymentWorks;
    }

    protected boolean isVendorTypeSendableToPaymentWorks(String vendorTypeCode) {
        return StringUtils.equalsIgnoreCase(vendorTypeCode, CUVendorConstants.PROC_METHOD_DV)
                || StringUtils.equalsIgnoreCase(vendorTypeCode, CUVendorConstants.PROC_METHOD_PO);
    }

    @Override
    public boolean shouldVendorBeSentToPaymentWorks(PaymentWorksVendor paymentWorksVendor) {
        Integer headerId = paymentWorksVendor.getVendorHeaderGeneratedIdentifier();
        Integer detailId = paymentWorksVendor.getVendorDetailAssignedIdentifier();
        VendorDetail vendorDetail = getVendorService().getVendorDetail(headerId, detailId);
        if (ObjectUtils.isNotNull(vendorDetail)) {
            return shouldVendorBeSentToPaymentWorks(vendorDetail);
        }
        LOG.error("shouldVendorBeSentToPaymentWorks2, unable to find a vendor by headerId: " + headerId
                + " and detailId: " + detailId);
        return false;
    }

    protected DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public PhoneNumberService getPhoneNumberService() {
        return phoneNumberService;
    }

    public void setPhoneNumberService(PhoneNumberService phoneNumberService) {
        this.phoneNumberService = phoneNumberService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public VendorService getVendorService() {
        return vendorService;
    }

    public void setVendorService(VendorService vendorService) {
        this.vendorService = vendorService;
    }
}
