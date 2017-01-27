package edu.cornell.kfs.paymentworks.service.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.rice.core.impl.datetime.DateTimeServiceImpl;

import edu.cornell.kfs.paymentworks.PaymentWorksConstants;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksFieldChangeDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksFieldChangesDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesDTO;

public class PaymentWorksAchConversionServiceImplTest {
	
	private PaymentWorksAchServiceImpl paymentWorksAchConversionServiceImpl;
	
	private static final String VENDOR_NUMBER = "1244532";
	private static final String ROUTING_NUMBER = "routingNum";
	private static final String ACCOUNT_NUMBER = "acctNUm";

	@Before
	public void setUp() throws Exception {
		Logger.getLogger(PaymentWorksAchServiceImpl.class).setLevel(Level.DEBUG);
		paymentWorksAchConversionServiceImpl = new PaymentWorksAchServiceImpl();
		paymentWorksAchConversionServiceImpl.setPaymentWorksUtilityService(new PaymentWorksUtilityServiceImpl());
		paymentWorksAchConversionServiceImpl.setDateTimeService(new DateTimeServiceImpl());
	}

	@After
	public void tearDown() throws Exception {
		paymentWorksAchConversionServiceImpl = null;
	}

	@Test
	public void createPayeeAchAccountPaymentWorksVendorUpdatesDTOString() {
		PayeeACHAccount account = paymentWorksAchConversionServiceImpl.createPayeeAchAccount(buildPaymentWorksVendorUpdatesDTO(), VENDOR_NUMBER);
		assertAccountValues(account);
	}

	protected PaymentWorksVendorUpdatesDTO buildPaymentWorksVendorUpdatesDTO() {
		PaymentWorksVendorUpdatesDTO vendorUpdate = new PaymentWorksVendorUpdatesDTO();
		PaymentWorksFieldChangesDTO fieldChangesDTO = new PaymentWorksFieldChangesDTO();
		List<PaymentWorksFieldChangeDTO> field_changes = new ArrayList<PaymentWorksFieldChangeDTO>();
		
		PaymentWorksFieldChangeDTO routingNumberDTO = new PaymentWorksFieldChangeDTO();
		routingNumberDTO.setField_name(PaymentWorksConstants.FieldNames.ROUTING_NUMBER);
		routingNumberDTO.setTo_value(ROUTING_NUMBER);
		field_changes.add(routingNumberDTO);
		
		PaymentWorksFieldChangeDTO accountNumberDTO  = new PaymentWorksFieldChangeDTO();
		accountNumberDTO.setField_name(PaymentWorksConstants.FieldNames.ACCOUNT_NUMBER);
		accountNumberDTO.setTo_value(ACCOUNT_NUMBER);
		field_changes.add(accountNumberDTO);
		
		fieldChangesDTO.setField_changes(field_changes);
		vendorUpdate.setField_changes(fieldChangesDTO);
		return vendorUpdate;
	}
	
	@Test
	public void createPayeeAchAccountPayeeACHAccountStringString() {
		PayeeACHAccount payeeAchAccountOld = new PayeeACHAccount();
		payeeAchAccountOld.setPayeeIdNumber(VENDOR_NUMBER);
		PayeeACHAccount account = paymentWorksAchConversionServiceImpl.createPayeeAchAccount(payeeAchAccountOld, ROUTING_NUMBER, ACCOUNT_NUMBER);
		assertAccountValues(account);
	}

	protected void assertAccountValues(PayeeACHAccount account) {
		assertEquals(VENDOR_NUMBER, account.getPayeeIdNumber());
		assertEquals(ROUTING_NUMBER, account.getBankRoutingNumber());
		assertEquals(ACCOUNT_NUMBER, account.getBankAccountNumber());
	}

}
