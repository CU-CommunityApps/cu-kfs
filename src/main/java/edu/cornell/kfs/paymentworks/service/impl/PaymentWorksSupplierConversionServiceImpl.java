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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.paymentworks.PaymentWorksConstants;
import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.service.PaymentWorksNewVendorConversionService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksSupplierConversionService;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksSupplierUploadDTO;

public class PaymentWorksSupplierConversionServiceImpl implements PaymentWorksSupplierConversionService {
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksSupplierConversionServiceImpl.class);
			
	protected PaymentWorksNewVendorConversionService paymentWorksNewVendorConversionService;
	protected DateTimeService dateTimeService;
	
	@Override
	public List<PaymentWorksSupplierUploadDTO> createPaymentWorksSupplierUploadList(Collection<PaymentWorksVendor> newVendors) {
		List<PaymentWorksSupplierUploadDTO> paymentWorksSupplierUploadList = new ArrayList<PaymentWorksSupplierUploadDTO>();

		for (PaymentWorksVendor newVendor : newVendors) {
			PaymentWorksVendor vendorToCopy = newVendor;

			// copy record from KFS vendor doc if exists
			PaymentWorksVendor newVendorFromDetail = getPaymentWorksNewVendorConversionService().createPaymentWorksVendorFromDetail(newVendor);
			if (ObjectUtils.isNotNull(newVendorFromDetail)) {
				vendorToCopy = newVendorFromDetail;
			}

			paymentWorksSupplierUploadList.add(buildPaymentWorksSupplierUploadDTO(vendorToCopy));
		}

		return paymentWorksSupplierUploadList;
	}

	protected PaymentWorksSupplierUploadDTO buildPaymentWorksSupplierUploadDTO(PaymentWorksVendor vendorToCopy) {
		PaymentWorksSupplierUploadDTO paymentWorksSupplierUploadDTO = new PaymentWorksSupplierUploadDTO();
		paymentWorksSupplierUploadDTO.setVendorNum(vendorToCopy.getVendorHeaderGeneratedIdentifier().toString());
		paymentWorksSupplierUploadDTO.setSupplierName(vendorToCopy.getRequestingCompanyLegalName());
		paymentWorksSupplierUploadDTO.setSiteCode(vendorToCopy.getVendorDetailAssignedIdentifier().toString());
		paymentWorksSupplierUploadDTO.setSendToPaymentWorks(vendorToCopy.isSendToPaymentWorks());

		// use remittance if it exists, otherwise corp
		if (StringUtils.isNotBlank(vendorToCopy.getRemittanceAddressStreet1())) {
			paymentWorksSupplierUploadDTO.setAddress1(vendorToCopy.getRemittanceAddressStreet1());
			paymentWorksSupplierUploadDTO.setAddress2(vendorToCopy.getRemittanceAddressStreet2());
			paymentWorksSupplierUploadDTO.setCity(vendorToCopy.getRemittanceAddressCity());
			paymentWorksSupplierUploadDTO.setState(vendorToCopy.getRemittanceAddressState());
			paymentWorksSupplierUploadDTO.setCountry(vendorToCopy.getRemittanceAddressCountry());
			paymentWorksSupplierUploadDTO.setZipcode(vendorToCopy.getRemittanceAddressZipCode());
		} else {
			paymentWorksSupplierUploadDTO.setAddress1(vendorToCopy.getCorpAddressStreet1());
			paymentWorksSupplierUploadDTO.setAddress2(vendorToCopy.getCorpAddressStreet2());
			paymentWorksSupplierUploadDTO.setCity(vendorToCopy.getCorpAddressCity());
			paymentWorksSupplierUploadDTO.setState(vendorToCopy.getCorpAddressState());
			paymentWorksSupplierUploadDTO.setCountry(vendorToCopy.getCorpAddressCountry());
			paymentWorksSupplierUploadDTO.setZipcode(vendorToCopy.getCorpAddressZipCode());

		}

		paymentWorksSupplierUploadDTO.setTin(vendorToCopy.getRequestingCompanyTin());
		return paymentWorksSupplierUploadDTO;
	}
	
	@Override
	public String createSupplierUploadFile(List<PaymentWorksSupplierUploadDTO> paymentWorksSupplierUploadList,String directoryPath) {
		String newFileName = directoryPath + File.separator + PaymentWorksConstants.SUPPLIER_FILE_NAME
				+ buildFileExtensionWithDate(getDateTimeService().getCurrentDate());

		checkDirectory(directoryPath);

		BufferedWriter out = null;
		FileWriter fstream = null;
		try {
			fstream = new FileWriter(newFileName);
			out = new BufferedWriter(fstream);

			out.write(PaymentWorksConstants.SUPPLIER_FILE_HEADER_ROW);
			out.newLine();

			writeDetailLineForEachUploadDTO(paymentWorksSupplierUploadList, out);

			out.flush();
			out.close();
			fstream.close();
		} catch (Exception e) {
			handleFileWritingException(out, fstream, e);
		}

		return newFileName;
	}

	protected void handleFileWritingException(BufferedWriter out, FileWriter fstream, Exception e) {
		LOG.error("handleFileWritingException, there was an error creating the supplier upload file: ", e);
		try {
			out.flush();
			out.close();
			fstream.close();
		} catch (IOException ex1) {
			LOG.error("handleFileWritingException, there was an error closing the buffered writer or the file writer: ", ex1);
		}
		throw new RuntimeException(e);
	}

	protected void writeDetailLineForEachUploadDTO(List<PaymentWorksSupplierUploadDTO> paymentWorksSupplierUploadList,
			BufferedWriter out) throws IOException {
		for (PaymentWorksSupplierUploadDTO supplier : paymentWorksSupplierUploadList) {
			if (supplier.isSendToPaymentWorks()) {
				out.write(supplier.toString());
				out.newLine();
			}

		}
	}

	protected void checkDirectory(String directoryPath) {
		try {
			File baseDir = new File(directoryPath);
			if (!baseDir.exists()) {
				FileUtils.forceMkdir(new File(directoryPath));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected String buildFileExtensionWithDate(java.util.Date date) {
		String formattedDateTime = getDateTimeService().toDateTimeStringForFilename(date);
		return "." + formattedDateTime + ".csv";
	}
	
	@Override
	public void deleteSupplierUploadFile(String fileName) {
		LOG.info("deleteSupplierUploadFile, about to delete " + fileName);
		Path path = Paths.get(fileName);
		try {
			Files.delete(path);
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}

	public PaymentWorksNewVendorConversionService getPaymentWorksNewVendorConversionService() {
		return paymentWorksNewVendorConversionService;
	}

	public void setPaymentWorksNewVendorConversionService(PaymentWorksNewVendorConversionService paymentWorksNewVendorConversionService) {
		this.paymentWorksNewVendorConversionService = paymentWorksNewVendorConversionService;
	}

	public DateTimeService getDateTimeService() {
		return dateTimeService;
	}

	public void setDateTimeService(DateTimeService dateTimeService) {
		this.dateTimeService = dateTimeService;
	}

}
