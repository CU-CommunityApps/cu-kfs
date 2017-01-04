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
package edu.cornell.kfs.paymentworks.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.paymentworks.service.PaymentWorksUploadSuppliersService;

public class PaymentWorksUploadSuppliersStep extends AbstractStep {
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksUploadSuppliersStep.class);
	
	protected PaymentWorksUploadSuppliersService paymentWorksUploadSuppliersService;

	@Override
	public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
		boolean results = getPaymentWorksUploadSuppliersService().uploadNewVendorApprovedSupplierFile();
		results = getPaymentWorksUploadSuppliersService().updateNewVendorDisapprovedStatus() && results;
		results = getPaymentWorksUploadSuppliersService().uploadVendorUpdateApprovedSupplierFile() && results;
		
		if (!results) {
			throw new RuntimeException("There was an error uploading files to PaymentWorks.");
		}
		LOG.debug("execute, the final results of this batch job are: " + results);
		return results;
	}

	public PaymentWorksUploadSuppliersService getPaymentWorksUploadSuppliersService() {
		return paymentWorksUploadSuppliersService;
	}

	public void setPaymentWorksUploadSuppliersService(PaymentWorksUploadSuppliersService paymentWorksUploadSuppliersService) {
		this.paymentWorksUploadSuppliersService = paymentWorksUploadSuppliersService;
	}

}
