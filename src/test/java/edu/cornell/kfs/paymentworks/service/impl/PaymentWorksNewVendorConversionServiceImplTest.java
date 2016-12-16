package edu.cornell.kfs.paymentworks.service.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.kns.service.impl.DataDictionaryServiceImpl;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.impl.VendorServiceImpl;
import org.kuali.kfs.vnd.service.impl.PhoneNumberServiceImpl;
import org.kuali.rice.krad.bo.BusinessObject;

import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.service.impl.PaymentWorksNewVendorConversionServiceImpl;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksCorpAddressDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksCustomFieldDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksCustomFieldsDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksNewVendorDetailDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksRequestingCompanyDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksTaxClassificationDTO;

public class PaymentWorksNewVendorConversionServiceImplTest {
	
	private PaymentWorksNewVendorConversionServiceImpl paymentWorksNewVenderConversionService;
	
	protected static final String ADDRESS_LINE_1 = "120 Maple";
	protected static final String ADDRESS_LINE_2 = "Room xyz";
	protected static final String CITY_NAME = "Ithaca";
	protected static final String COUNTRY_CODE_US = "US";
	protected static final String STATE_CODE_NY = "NY";

	@Before
	public void setUp() throws Exception {
		paymentWorksNewVenderConversionService = new PaymentWorksNewVendorConversionServiceImpl();
		paymentWorksNewVenderConversionService.setSequenceAccessorService(new TestableSequenceAccessorService());
		paymentWorksNewVenderConversionService.setVendorService(new TestableVendorService());
	}

	@After
	public void tearDown() throws Exception {
		paymentWorksNewVenderConversionService = null;
	}

	@Test
	public void createVendorDetail() {
		PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
		pmwVendor.setRequestingCompanyTaxCountry(COUNTRY_CODE_US);
		pmwVendor.setCorpAddressStreet1(ADDRESS_LINE_1);
		pmwVendor.setRequestingCompanyTinType("0");
		pmwVendor.setRequestingCompanyTin("000000000");
		
		VendorDetail detail = paymentWorksNewVenderConversionService.createVendorDetail(pmwVendor);
		assertEquals("The address should be 120 Maple", ADDRESS_LINE_1, detail.getVendorAddresses().get(0).getVendorLine1Address());
	}
	
	@Test 
	public void createPaymentWorksVendorFromVendor() {
		VendorDetail vendorDetail = new VendorDetail();
		String docNumber = "1234";
		PaymentWorksVendor pmwVendor = paymentWorksNewVenderConversionService.createPaymentWorksVendor(vendorDetail, docNumber);
		assertEquals("The address should be 120 Maple", ADDRESS_LINE_1, pmwVendor.getCorpAddressStreet1());
	}
	
	@Test 
	public void createPaymentWorksNewVendorFromDTO() {
		PaymentWorksNewVendorDetailDTO newVendorDetailDTO = new PaymentWorksNewVendorDetailDTO();
		
		PaymentWorksRequestingCompanyDTO reqCompanyDTO = new PaymentWorksRequestingCompanyDTO();
		reqCompanyDTO.setId("123");
		reqCompanyDTO.setDuns("duns");
		reqCompanyDTO.setLegal_name("Foo Bar");

		PaymentWorksCorpAddressDTO corp_address = new PaymentWorksCorpAddressDTO();
		corp_address.setCity(CITY_NAME);
		corp_address.setStreet1(ADDRESS_LINE_1);
		reqCompanyDTO.setCorp_address(corp_address);
		
		PaymentWorksTaxClassificationDTO tax_classification = new PaymentWorksTaxClassificationDTO();
		tax_classification.setName("tax class name");
		reqCompanyDTO.setTax_classification(tax_classification);
		
		newVendorDetailDTO.setRequesting_company(reqCompanyDTO);
		
		PaymentWorksUtilityServiceImpl paymentWordsUtilityService = new PaymentWorksUtilityServiceImpl();
		paymentWordsUtilityService.setDataDictionaryService(new TestableDataDictionaryService());
		paymentWordsUtilityService.setPhoneNumberService(new PhoneNumberServiceImpl());
		
		paymentWorksNewVenderConversionService.setPaymentWorksUtilityService(paymentWordsUtilityService);
		PaymentWorksVendor pmwVendor = paymentWorksNewVenderConversionService.createPaymentWorksVendor(newVendorDetailDTO);
		assertEquals("The address should be 120 Maple", ADDRESS_LINE_1, pmwVendor.getCorpAddressStreet1());
	}
	
	@Test
	public void isVendorTaxableYes() {
		PaymentWorksVendor paymentWorksVendor = new PaymentWorksVendor();
		assertTrue("Vendor should be taxable", paymentWorksNewVenderConversionService.isVendorTaxable(paymentWorksVendor));
	}
	
	@Test
	public void isVendorTaxableNo() {
		PaymentWorksVendor paymentWorksVendor = new PaymentWorksVendor();
		paymentWorksVendor.setRequestingCompanyTaxClassificationCode("1");
		paymentWorksVendor.setVendorType("Other");
		assertFalse("Vendor should be taxable", paymentWorksNewVenderConversionService.isVendorTaxable(paymentWorksVendor));
	}
	
	private PaymentWorksCustomFieldDTO buildPaymentWorksCustomFieldDTO(String field_label, String field_value) {
		PaymentWorksCustomFieldDTO dto = new PaymentWorksCustomFieldDTO();
		dto.setField_label(field_label);
		dto.setField_value(field_value);
		return dto;
	}
	
	private class TestableSequenceAccessorService implements SequenceAccessorService {

		@Override
		public Long getNextAvailableSequenceNumber(String sequenceName, Class<? extends BusinessObject> clazz) {
			return getNextAvailableSequenceNumber(sequenceName);
		}

		@Override
		public Long getNextAvailableSequenceNumber(String sequenceName) {
			return new Long(123);
		}
		
	}
	
	private class TestableVendorService extends VendorServiceImpl {
		@Override
		public VendorAddress getVendorDefaultAddress(Integer vendorHeaderId, Integer vendorDetailId, String addressType, String campus) {
			VendorAddress address = new VendorAddress();
			address.setActive(true);
			address.setVendorAddressTypeCode(addressType);
			address.setVendorLine1Address(PaymentWorksNewVendorConversionServiceImplTest.ADDRESS_LINE_1);
			address.setVendorLine2Address(PaymentWorksNewVendorConversionServiceImplTest.ADDRESS_LINE_2);
			address.setVendorCityName(PaymentWorksNewVendorConversionServiceImplTest.CITY_NAME);
			address.setVendorCountryCode(PaymentWorksNewVendorConversionServiceImplTest.COUNTRY_CODE_US);
			address.setVendorStateCode(PaymentWorksNewVendorConversionServiceImplTest.STATE_CODE_NY);
			return address;
		}
		
		@Override
		public VendorDetail getVendorDetail(Integer headerId, Integer detailId) {
			VendorDetail detail = new VendorDetail();
			detail.setActiveIndicator(true);
			detail.setDefaultAddressCountryCode(PaymentWorksNewVendorConversionServiceImplTest.COUNTRY_CODE_US);
			detail.setDefaultAddressLine1(PaymentWorksNewVendorConversionServiceImplTest.ADDRESS_LINE_1);
			detail.setDefaultAddressCountryCode(PaymentWorksNewVendorConversionServiceImplTest.COUNTRY_CODE_US);
			detail.setVendorFirstLastNameIndicator(true);
			detail.setVendorFirstName("FOO");
			detail.setVendorLastName("BAR");
			return detail;
		}
		
	}
	
	private class TestableDataDictionaryService extends DataDictionaryServiceImpl {
		@Override
		public Integer getAttributeMaxLength(String entryName, String attributeName){
			return 50;
		}
	}

}
