package edu.cornell.kfs.paymentworks.service.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.paymentworks.service.impl.PaymentWorksUtilityServiceImpl;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksCustomFieldDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksCustomFieldsDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksFieldChangeDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksFieldChangesDTO;

public class PaymentWorksUtilityServiceImplTest {
	
	PaymentWorksUtilityServiceImpl paymentworksService;
	private static final String PAYMENT_WORKS_FIELD_NAME = "field name";
	private static final String PAYMENT_WORKS_FIELD_FROM_VALUE = "old value";
	private static final String PAYMENT_WORKS_FIELD_TO_VALUE = "new value";

	@Before
	public void setUp() throws Exception {
		paymentworksService = new PaymentWorksUtilityServiceImpl();
		Logger.getLogger(PaymentWorksUtilityServiceImpl.class).setLevel(Level.DEBUG);
	}

	@After
	public void tearDown() throws Exception {
		paymentworksService = null;
	}

	@Test
	public void pojoToJsonString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{\"field_changes\":[{\"field_name\":\"").append(PAYMENT_WORKS_FIELD_NAME).append("\",\"from_value\":\"").append(PAYMENT_WORKS_FIELD_FROM_VALUE)
			.append("\",\"to_value\":\"").append(PAYMENT_WORKS_FIELD_TO_VALUE).append("\"}]}");
		assertEquals("Conversion wasn't what was expected", builder.toString(), paymentworksService.pojoToJsonString(buildPaymentWorksFieldChangesDTO()));
	}

	@Test
	public void convertFieldArrayToMapPaymentWorksCustomFieldsDTO() {
		PaymentWorksCustomFieldsDTO customFields = new PaymentWorksCustomFieldsDTO();
		List<PaymentWorksCustomFieldDTO> custom_fields = new ArrayList<PaymentWorksCustomFieldDTO>();
		PaymentWorksCustomFieldDTO dto = new PaymentWorksCustomFieldDTO();
		String label = "the label";
		String value = "super cool value";
		dto.setField_id("12344");
		dto.setField_label(label);
		dto.setField_value(value);
		custom_fields.add(dto);
		customFields.setCustom_fields(custom_fields);
		Map<String, String> results = paymentworksService.convertFieldArrayToMap(customFields);
		assertEquals("the value was not what we expected", value, results.get(label));
	}

	@Test
	public void convertFieldArrayToMapPaymentWorksFieldChangesDTO() {
		Map<String, String> results = paymentworksService.convertFieldArrayToMap(buildPaymentWorksFieldChangesDTO());
		assertEquals("the value was not what we expected", PAYMENT_WORKS_FIELD_TO_VALUE, results.get(PAYMENT_WORKS_FIELD_NAME));
	}

	@Test
	public void convertFieldArrayToMapFromValues() {
		Map<String, String> results = paymentworksService.convertFieldArrayToMapFromValues(buildPaymentWorksFieldChangesDTO());
		assertEquals("the value was not what we expected", PAYMENT_WORKS_FIELD_FROM_VALUE, results.get(PAYMENT_WORKS_FIELD_NAME));
	}
	
	private PaymentWorksFieldChangesDTO buildPaymentWorksFieldChangesDTO() {
		PaymentWorksFieldChangesDTO fieldChanges = new PaymentWorksFieldChangesDTO();
		List<PaymentWorksFieldChangeDTO> field_changes = new ArrayList<PaymentWorksFieldChangeDTO>();
		PaymentWorksFieldChangeDTO dto = new PaymentWorksFieldChangeDTO();
		dto.setField_name(PAYMENT_WORKS_FIELD_NAME);
		dto.setFrom_value(PAYMENT_WORKS_FIELD_FROM_VALUE);
		dto.setTo_value(PAYMENT_WORKS_FIELD_TO_VALUE);
		field_changes.add(dto);
		fieldChanges.setField_changes(field_changes);
		return fieldChanges;
	}
}
