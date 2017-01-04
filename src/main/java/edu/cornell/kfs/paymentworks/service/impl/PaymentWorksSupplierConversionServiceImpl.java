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
	
	protected PaymentWorksNewVendorConversionService paymentWorksNewVendorConversionService;
	protected DateTimeService dateTimeService;
	
	@Override
	public List<PaymentWorksSupplierUploadDTO> createPaymentWorksSupplierUploadList(Collection<PaymentWorksVendor> newVendors) {

		List<PaymentWorksSupplierUploadDTO> paymentWorksSupplierUploadList = new ArrayList<PaymentWorksSupplierUploadDTO>();
		PaymentWorksSupplierUploadDTO paymentWorksSupplierUploadDTO = null;
		PaymentWorksVendor vendorToCopy = null;

		for (PaymentWorksVendor newVendor : newVendors) {
			paymentWorksSupplierUploadDTO = new PaymentWorksSupplierUploadDTO();
			vendorToCopy = newVendor;

			// copy record from KFS vendor doc if exists
			PaymentWorksVendor newVendorFromDetail = getPaymentWorksNewVendorConversionService().createPaymentWorksVendorFromDetail(newVendor);
			if (ObjectUtils.isNotNull(newVendorFromDetail)) {
				vendorToCopy = newVendorFromDetail;
			}

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

			paymentWorksSupplierUploadList.add(paymentWorksSupplierUploadDTO);
		}

		return paymentWorksSupplierUploadList;
	}
	
	@Override
	public String createSupplierUploadFile(List<PaymentWorksSupplierUploadDTO> paymentWorksSupplierUploadList,String directoryPath) {

		FileWriter fstream = null;
		String newFileName = directoryPath + File.separator + PaymentWorksConstants.SUPPLIER_FILE_NAME
				+ buildFileExtensionWithDate(getDateTimeService().getCurrentDate());

		// ensure directory exists
		checkDirectory(directoryPath);

		BufferedWriter out = null;

		try {
			fstream = new FileWriter(newFileName);
			out = new BufferedWriter(fstream);

			// write header
			out.write(PaymentWorksConstants.SUPPLIER_FILE_HEADER_ROW);
			out.newLine();

			// write each supplier
			for (PaymentWorksSupplierUploadDTO supplier : paymentWorksSupplierUploadList) {
				// only send if marked for submittal
				if (supplier.isSendToPaymentWorks()) {
					out.write(supplier.toString());
					out.newLine();
				}

			}

			out.flush();

			// close file
			out.close();
			fstream.close();

		} catch (Exception e) {
			try {
				out.flush();
				// close file
				out.close();
				fstream.close();
			} catch (IOException ex1) {
			}
		}

		return newFileName;
	}

	protected void checkDirectory(String directoryPath) {
		try {
			/**
			 * Create, if not there
			 */

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
