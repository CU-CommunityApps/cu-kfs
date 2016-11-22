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
package edu.cornell.kfs.paymentworks.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kns.service.DataDictionaryService;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.util.AutoPopulatingList;

import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksCustomFieldDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksCustomFieldsDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksFieldChangeDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksFieldChangesDTO;

public class PaymentWorksUtil {

	private DataDictionaryService dataDictionaryService;

	public String getGlobalErrorMessage() {
		String errorMessage = "";

		if (GlobalVariables.getMessageMap().getErrorCount() > 0) {
			errorMessage = StringEscapeUtils
					.unescapeHtml(getAutoPopulatingErrorMessages(GlobalVariables.getMessageMap().getErrorMessages()));
			GlobalVariables.getMessageMap().clearErrorMessages();
		}

		return errorMessage;
	}

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
				errorText = SpringContext.getBean(ConfigurationService.class)
						.getPropertyValueAsString(errorMessage.getErrorKey());
				// apply parameters
				errorText = MessageFormat.format(errorText, (Object[]) errorMessage.getMessageParameters());

				// add key and error message together
				errorList.append(errorText + "\n");
			}
		}

		return errorList.toString();
	}

	/**
	 * Converts a pojo object into a json string
	 *
	 * @param object
	 * @return
	 */
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

	/**
	 * Utility method to convert custom fields into a map for ease of access
	 *
	 * @param customFields
	 * @return
	 */
	protected Map<String, String> convertFieldArrayToMap(PaymentWorksCustomFieldsDTO customFields) {

		Map<String, String> customFieldMap = new HashMap<String, String>();

		if (ObjectUtils.isNotNull(customFields) && ObjectUtils.isNotNull(customFields.getCustom_fields())) {
			for (PaymentWorksCustomFieldDTO customField : customFields.getCustom_fields()) {
				customFieldMap.put(customField.getField_label(), customField.getField_value());
			}
		}

		return customFieldMap;
	}

	protected Map<String, String> convertFieldArrayToMap(PaymentWorksFieldChangesDTO fieldChanges) {

		Map<String, String> customFieldMap = new HashMap<String, String>();

		if (ObjectUtils.isNotNull(fieldChanges) && ObjectUtils.isNotNull(fieldChanges.getField_changes())) {
			for (PaymentWorksFieldChangeDTO fieldChange : fieldChanges.getField_changes()) {
				customFieldMap.put(fieldChange.getField_name(), fieldChange.getTo_value());
			}
		}

		return customFieldMap;
	}

	protected Map<String, String> convertFieldArrayToMapFromValues(PaymentWorksFieldChangesDTO fieldChanges) {

		Map<String, String> customFieldMap = new HashMap<String, String>();

		if (ObjectUtils.isNotNull(fieldChanges) && ObjectUtils.isNotNull(fieldChanges.getField_changes())) {
			for (PaymentWorksFieldChangeDTO fieldChange : fieldChanges.getField_changes()) {
				customFieldMap.put(fieldChange.getField_name(), fieldChange.getFrom_value());
			}
		}

		return customFieldMap;
	}

	protected String trimFieldToMax(String field, String fieldName) {
		String returnField = field;

		try {
			returnField = StringUtils.substring(returnField, 0,
					getDataDictionaryService().getAttributeMaxLength(PaymentWorksVendor.class.getName(), fieldName));
		} catch (Exception e) {
			// ignore and return original value
		}

		return returnField;
	}

	/*
	 * KPS-316 format telephone number to xxx-yyy-zzzz
	 */
	protected String convertPhoneNumber(String phoneNbr) {
		// first clear out any non-numeric characters
		String phone = phoneNbr.replaceAll("[^0-9]", "");
		StringBuilder p = new StringBuilder(phone.substring(0, 3)).append("-");
		p.append(phone.substring(3, 6)).append("-");
		p.append(phone.substring(6, phone.length()));
		phone = p.toString();
		return phone;
	}

	protected DataDictionaryService getDataDictionaryService() {

		if (ObjectUtils.isNull(dataDictionaryService)) {
			dataDictionaryService = SpringContext.getBean(DataDictionaryService.class);
		}

		return dataDictionaryService;
	}
}
