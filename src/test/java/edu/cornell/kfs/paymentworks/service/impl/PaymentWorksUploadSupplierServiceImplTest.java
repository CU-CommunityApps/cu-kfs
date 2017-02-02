package edu.cornell.kfs.paymentworks.service.impl;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.rice.core.impl.datetime.DateTimeServiceImpl;

import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksSupplierUploadDTO;

public class PaymentWorksUploadSupplierServiceImplTest {

	private static final String VENDOR_1_NAME = "vendor 2 Name";
	private static final Integer VENDOR_1_SITE_CODE = 0;
	private static final Integer VENDOR_1_VENDOR_NUMBER = 1242;
	private static final String VENDOR_2_NAME = "vendor 2 name";
	private static final Integer VENDOR_2_SITE_CODE = 1;
	private static final Integer VENDOR_2_VENDOR_NUMBER = 1243;
	private static final String ZIP_ADDRESS = "14850";
	private static final String STATE_ADDRESS = "NY";
	private static final String COUNTRY_ADDRESS = "US";
	private static final String CITY_ADDRESS = "Ithaca";
	private static final String STREET_ADDRESS = "street";

	private static final String TEST_FILE_PATH_ROOT = "test";
	private static final String TEST_FILE_PATH = TEST_FILE_PATH_ROOT + "/outputFiles";
	private static final String DUMMY_TEST_FILE_NAME = "dummyFileToCreateDirectory.txt";
	private static final String TEST_PATH_AND_FILE = TEST_FILE_PATH + File.separator + DUMMY_TEST_FILE_NAME;

	private PaymentWorksUploadSupplierServiceImpl paymentWorksUploadSupplierService;

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(PaymentWorksUploadSupplierServiceImplTest.class);

	@Before
	public void setUp() throws Exception {
		Logger.getLogger(PaymentWorksUploadSupplierServiceImplTest.class).setLevel(Level.DEBUG);

		paymentWorksUploadSupplierService = new TestablePaymentWorksUploadSupplierServiceImpl();
		paymentWorksUploadSupplierService.setPaymentWorksNewVendorConversionService(new TestablePaymentWorksNewVendorConversionServiceImpl());
		paymentWorksUploadSupplierService.setDateTimeService(new TestableDateTimeService());
		paymentWorksUploadSupplierService.setPaymentWorksUtilityService(new TestablePaymentWorksUtilitySerice());
		FileUtils.forceMkdir(new File(TEST_PATH_AND_FILE));
	}

	@After
	public void tearDown() throws Exception {
		paymentWorksUploadSupplierService = null;
		FileUtils.forceDelete(new File(TEST_PATH_AND_FILE).getAbsoluteFile());
	}

	@Test
	public void createPaymentWorksSupplierUploadList() {
		List<PaymentWorksSupplierUploadDTO> dtos = paymentWorksUploadSupplierService
				.createPaymentWorksSupplierUploadList(buildNewVendorCollection());
		assertEquals("The size should be 2", 2, dtos.size());
		PaymentWorksSupplierUploadDTO dto = dtos.get(0);
		assertDTOValues(dtos.get(0), VENDOR_1_NAME, VENDOR_1_SITE_CODE, VENDOR_1_VENDOR_NUMBER);
		assertDTOValues(dtos.get(1), VENDOR_2_NAME, VENDOR_2_SITE_CODE, VENDOR_2_VENDOR_NUMBER);
	}

	private void assertDTOValues(PaymentWorksSupplierUploadDTO dto, String dtoDescription, Integer siteCode,
			Integer vendorNumber) {
		assertEquals(dtoDescription, vendorNumber.toString(), dto.getVendorNum());
		assertEquals(dtoDescription, siteCode.toString(), dto.getSiteCode());
		assertEquals(dtoDescription, STREET_ADDRESS, dto.getAddress1());
		assertEquals(dtoDescription, CITY_ADDRESS, dto.getCity());
	}

	private Collection<PaymentWorksVendor> buildNewVendorCollection() {
		Collection<PaymentWorksVendor> newVendors = new ArrayList<PaymentWorksVendor>();

		PaymentWorksVendor vendor1 = new PaymentWorksVendor();
		vendor1.setVendorName(VENDOR_1_NAME);
		vendor1.setVendorHeaderGeneratedIdentifier(VENDOR_1_VENDOR_NUMBER);
		vendor1.setVendorDetailAssignedIdentifier(VENDOR_1_SITE_CODE);
		vendor1.setRemittanceAddressStreet1(STREET_ADDRESS);
		vendor1.setRemittanceAddressCity(CITY_ADDRESS);
		vendor1.setRemittanceAddressCountry(COUNTRY_ADDRESS);
		vendor1.setRemittanceAddressState(STATE_ADDRESS);
		vendor1.setRemittanceAddressZipCode(ZIP_ADDRESS);
		newVendors.add(vendor1);

		PaymentWorksVendor vendor2 = new PaymentWorksVendor();
		vendor2.setVendorName(VENDOR_2_NAME);
		vendor2.setVendorHeaderGeneratedIdentifier(VENDOR_2_VENDOR_NUMBER);
		vendor2.setVendorDetailAssignedIdentifier(VENDOR_2_SITE_CODE);
		vendor2.setCorpAddressStreet1(STREET_ADDRESS);
		vendor2.setCorpAddressCity(CITY_ADDRESS);
		vendor2.setCorpAddressCountry(COUNTRY_ADDRESS);
		vendor2.setCorpAddressState(STATE_ADDRESS);
		vendor2.setCorpAddressZipCode(ZIP_ADDRESS);
		newVendors.add(vendor2);

		return newVendors;
	}

	@Test
	public void createSupplierUploadFile() {
		List<PaymentWorksSupplierUploadDTO> dtos = paymentWorksUploadSupplierService
				.createPaymentWorksSupplierUploadList(buildNewVendorCollection());
		String newFileName = paymentWorksUploadSupplierService.createSupplierUploadFile(dtos, TEST_FILE_PATH);

		try {
			BufferedReader inputReader = new BufferedReader(new FileReader(new String(newFileName)));
			String lineWeWant = null;
			for (int i = 0; i < 3; i++) {
				lineWeWant = inputReader.readLine();
				LOG.info("createSupplierUploadFile, the read line: " + lineWeWant);
			}
			boolean doesLineHaveTheRightVendorNumber = StringUtils.containsIgnoreCase(lineWeWant,
					VENDOR_2_VENDOR_NUMBER.toString());
			assertTrue("The second line should cotain the vendor number " + VENDOR_2_VENDOR_NUMBER,
					doesLineHaveTheRightVendorNumber);
		} catch (IOException e) {
			LOG.error("createSupplierUploadFile, unable to read file: " + newFileName, e);
			throw new RuntimeException(e);
		}

		Path path = Paths.get(newFileName);
		try {
			Files.delete(path);
			LOG.info("createSupplierUploadFile, deleted " + newFileName);
		} catch (IOException ex) {
			LOG.error("deleteSupplierUploadFile, Unable to delete the file: " + newFileName, ex);
			throw new RuntimeException(ex);
		}
	}

	private class TestablePaymentWorksNewVendorConversionServiceImpl extends PaymentWorksNewVendorConversionServiceImpl {
		@Override
		public PaymentWorksVendor createPaymentWorksVendorFromDetail(PaymentWorksVendor vendor) {
			return null;
		}
	}

	private class TestableDateTimeService extends DateTimeServiceImpl {
		public TestableDateTimeService() {
			timestampToStringFormatForFileName = "yyyyMMdd-HH-mm-ss-S";
		}
	}
	
	private class TestablePaymentWorksUploadSupplierServiceImpl extends PaymentWorksUploadSupplierServiceImpl {
		@Override
		protected String findSupplierUpLoadFileHeader() {
			return "Vendor,Site Code,Name,AccountingAddress,Address 2,AccountingCity,AccountingState,Country,AccountingZip,US Tax Number,VI Contact Email";
		}
	}
	
	private class TestablePaymentWorksUtilitySerice extends PaymentWorksUtilityServiceImpl {
		@Override
		public boolean shouldVendorBeSentToPaymentWorks(PaymentWorksVendor paymentWorksVendor) {
			return true;
		}
	}

}
