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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.paymentworks.PaymentWorksConstants;
import edu.cornell.kfs.paymentworks.batch.report.SupplierUploadSummary;
import edu.cornell.kfs.paymentworks.batch.report.SupplierUploadSummaryLine;
import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.service.PaymentWorksKfsService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksVendorService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksWebService;
import edu.cornell.kfs.paymentworks.util.PaymentWorksSupplierConversionUtil;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksNewVendorUpdateVendorStatus;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksSupplierUploadDTO;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class PaymentWorksUploadSuppliersStep extends AbstractStep {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(PaymentWorksUploadSuppliersStep.class);

	private PaymentWorksVendorService paymentWorksVendorService;
	private PaymentWorksWebService paymentWorksWebService;
	private PaymentWorksKfsService paymentWorksKfsService;
	private ReportWriterService reportWriterService;
	private SupplierUploadSummary supplierUploadSummary;

	@Override
	public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
		supplierUploadSummary = new SupplierUploadSummary();

		uploadNewVendorApprovedSupplierFile();

		updateNewVendorDisapprovedStatus();

		uploadVendorUpdateApprovedSupplierFile();

		return true;
	}

	public void uploadNewVendorApprovedSupplierFile() {

		// get list of approved vendors
		Collection<PaymentWorksVendor> results = paymentWorksVendorService.getPaymentWorksVendorRecords(
				PaymentWorksConstants.ProcessStatus.VENDOR_APPROVED, null, PaymentWorksConstants.TransactionType.NEW_VENDOR);

		if (ObjectUtils.isNotNull(results) && !results.isEmpty()) {

			// convert to supplier upload DTO
			List<PaymentWorksSupplierUploadDTO> paymentWorksSupplierUploadList = new PaymentWorksSupplierConversionUtil()
					.createPaymentWorksSupplierUploadList(results);

			// upload list of vendors
			boolean uploaded = paymentWorksWebService.uploadSuppliers(paymentWorksSupplierUploadList);

			if (uploaded) {
				// loop through list and upload
				for (PaymentWorksVendor newVendor : results) {

					String supplierStatusType = !StringUtils.startsWith(newVendor.getVendorRequestId(), "KFS")
							? PaymentWorksConstants.SupplierUploadSummaryTypes.KFS_NEW_VENDORS
							: PaymentWorksConstants.SupplierUploadSummaryTypes.PAYMENT_WORKS_NEW_VENDORS;

					boolean updateStatus = !StringUtils.startsWith(newVendor.getVendorRequestId(), "KFS") ? true
							: false;

					processVendor(newVendor, PaymentWorksConstants.PaymentWorksNewVendorStatus.PROCESSED,
							PaymentWorksConstants.PaymentWorksStatusText.PROCESSED, PaymentWorksConstants.ProcessStatus.SUPPLIER_UPLOADED,
							supplierStatusType, updateStatus);
				}
			}
		}
	}

	public void uploadVendorUpdateApprovedSupplierFile() {

		// get list of approved vendors
		Collection<PaymentWorksVendor> results = paymentWorksVendorService.getPaymentWorksVendorRecords(
				PaymentWorksConstants.ProcessStatus.VENDOR_APPROVED, null, PaymentWorksConstants.TransactionType.VENDOR_UPDATE);

		if (ObjectUtils.isNotNull(results) && !results.isEmpty()) {
			// convert to supplier upload DTO
			List<PaymentWorksSupplierUploadDTO> paymentWorksSupplierUploadList = new PaymentWorksSupplierConversionUtil()
					.createPaymentWorksSupplierUploadList(results);

			// upload list of vendors
			boolean uploaded = paymentWorksWebService.uploadSuppliers(paymentWorksSupplierUploadList);

			if (uploaded) {
				// loop through list and upload
				for (PaymentWorksVendor newVendor : results) {

					processVendor(newVendor, PaymentWorksConstants.PaymentWorksUpdateStatus.PROCESSED,
							PaymentWorksConstants.PaymentWorksStatusText.PROCESSED, PaymentWorksConstants.ProcessStatus.SUPPLIER_UPLOADED,
							PaymentWorksConstants.SupplierUploadSummaryTypes.VENDOR_UPDATES, false);
				}
			}
		}
	}

	public void updateNewVendorDisapprovedStatus() {

		// get list of disapproved vendors
		Collection<PaymentWorksVendor> results = paymentWorksVendorService.getPaymentWorksVendorRecords(
				PaymentWorksConstants.ProcessStatus.VENDOR_DISAPPROVED, PaymentWorksConstants.PaymentWorksStatusText.APPROVED,
				PaymentWorksConstants.TransactionType.NEW_VENDOR);

		// loop through list and set status to rejected
		for (PaymentWorksVendor newVendor : results) {

			processVendor(newVendor, PaymentWorksConstants.PaymentWorksNewVendorStatus.REJECTED,
					PaymentWorksConstants.PaymentWorksStatusText.REJECTED, PaymentWorksConstants.ProcessStatus.VENDOR_DISAPPROVED,
					PaymentWorksConstants.SupplierUploadSummaryTypes.DISAPPROVED_VENDORS, true);
		}
	}

	protected void processVendor(PaymentWorksVendor paymentWorksNewVendor, String requestStatus,
			String requestStatusText, String processStatus, String supplierUploadSummaryType, boolean updatePaymentWorksStatus) {

		// add summary line (done before update of staging as there is a
		// transient value of sendToPaymentWorks)
		addSummaryLine(paymentWorksNewVendor, supplierUploadSummaryType);

		paymentWorksNewVendor.setRequestStatus(requestStatusText);
		paymentWorksNewVendor.setProcessStatus(processStatus);
		paymentWorksNewVendor = paymentWorksVendorService.updatePaymentWorksVendor(paymentWorksNewVendor);

		if (updatePaymentWorksStatus) {
			updatePaymentWorksVendorStatus(paymentWorksNewVendor.getVendorRequestId(), requestStatus);
		}
	}

	protected void addSummaryLine(PaymentWorksVendor paymentWorksVendor, String supplierUploadSummaryType) {

		// add summary line
		SupplierUploadSummaryLine summaryLine = new SupplierUploadSummaryLine();
		summaryLine.setVendorRequestId(paymentWorksVendor.getVendorRequestId());
		summaryLine.setVendorName(paymentWorksVendor.getRequestingCompanyLegalName());
		summaryLine.setDocumentNumber(paymentWorksVendor.getDocumentNumber());
		summaryLine.setVendorNumber(paymentWorksVendor.getVendorHeaderGeneratedIdentifier() + "-"
				+ paymentWorksVendor.getVendorDetailAssignedIdentifier());
		summaryLine.setSendToPaymentWorks(paymentWorksVendor.isSendToPaymentWorks());

		if (StringUtils.equals(supplierUploadSummaryType, PaymentWorksConstants.SupplierUploadSummaryTypes.PAYMENT_WORKS_NEW_VENDORS)) {
			supplierUploadSummary.getPaymentWorksNewVendors().add(summaryLine);
		} else if (StringUtils.equals(supplierUploadSummaryType,
				PaymentWorksConstants.SupplierUploadSummaryTypes.KFS_NEW_VENDORS)) {
			supplierUploadSummary.getKfsNewVendors().add(summaryLine);
		} else if (StringUtils.equals(supplierUploadSummaryType,
				PaymentWorksConstants.SupplierUploadSummaryTypes.VENDOR_UPDATES)) {
			supplierUploadSummary.getVendorUpdates().add(summaryLine);
		} else if (StringUtils.equals(supplierUploadSummaryType,
				PaymentWorksConstants.SupplierUploadSummaryTypes.DISAPPROVED_VENDORS)) {
			supplierUploadSummary.getNewVendorDisapproved().add(summaryLine);
		}

		if (paymentWorksVendor.isSendToPaymentWorks()) {
			supplierUploadSummary.getUploadedVendors().add(summaryLine);
		}
	}

	protected void updatePaymentWorksVendorStatus(String vendorRequestId, String requestStatus) {
		List<PaymentWorksNewVendorUpdateVendorStatus> updateNewVendorStatusList = new ArrayList<PaymentWorksNewVendorUpdateVendorStatus>();
		PaymentWorksNewVendorUpdateVendorStatus updateNewVendorStatus = new PaymentWorksNewVendorUpdateVendorStatus();
		updateNewVendorStatus.setId(new Integer(vendorRequestId));
		updateNewVendorStatus.setRequest_status(new Integer(requestStatus));
		updateNewVendorStatusList.add(updateNewVendorStatus);

		paymentWorksWebService.updateNewVendorStatusInPaymentWorks(updateNewVendorStatusList);
	}

	protected File writePaymentWorksSupplierUploadSummaryReport(SupplierUploadSummary supplierUploadSummary) {

		if (reportWriterService == null) {
			throw new IllegalStateException(
					"ReportWriterService not configured for PaymentWorks Supplier Upload service.");
		} else {
			reportWriterService.initialize();
		}

		List<SupplierUploadSummaryLine> paymentWorksNewVendors = supplierUploadSummary.getPaymentWorksNewVendors();
		List<SupplierUploadSummaryLine> kfsNewVendors = supplierUploadSummary.getKfsNewVendors();
		List<SupplierUploadSummaryLine> vendorUpdates = supplierUploadSummary.getVendorUpdates();
		List<SupplierUploadSummaryLine> newVendorDisapproved = supplierUploadSummary.getNewVendorDisapproved();
		List<SupplierUploadSummaryLine> uploadedVendors = supplierUploadSummary.getUploadedVendors();

		writeSupplierUploadSummaryDetailRecords("PaymentWorks New Vendor Approvals", paymentWorksNewVendors);
		writeSupplierUploadSummaryDetailRecords("KFS New Vendors Approvals", kfsNewVendors);
		writeSupplierUploadSummaryDetailRecords("PaymentWorks New Vendor Disapprovals", newVendorDisapproved);
		writeSupplierUploadSummaryDetailRecords("Vendor Edit Approvals", vendorUpdates);

		reportWriterService.writeStatisticLine("%d PaymentWorks new vendor approvals", paymentWorksNewVendors.size());
		reportWriterService.writeStatisticLine("%d KFS new vendor approvals", kfsNewVendors.size());
		reportWriterService.writeStatisticLine("%d PaymentWorks new vendor disapprovals", newVendorDisapproved.size());
		reportWriterService.writeStatisticLine("%d Vendor edit approvals", vendorUpdates.size());
		reportWriterService.writeStatisticLine("%d total transactions uploaded", uploadedVendors.size());
		reportWriterService.writeStatisticLine("%d total transactions processed",
				paymentWorksNewVendors.size() + kfsNewVendors.size() + newVendorDisapproved.size() + vendorUpdates.size());

		return reportWriterService.getReportFile();
	}

	protected void writeSupplierUploadSummaryDetailRecords(String subtitle, List<SupplierUploadSummaryLine> records) {
		String rowFormat = "%-15s %-30s %-15s %-15s %-9s";
		String hdrRowFormat = "%-15s %-30s %-15s %-15s %-9s";
		Object[] headerArgs = { "Vendor Req ID", "Vendor Name", "Document Number", "Vendor Number", "Uploaded" };

		boolean firstPage = true;

		for (SupplierUploadSummaryLine supplierUploadSummaryLine : records) {
			if (reportWriterService.isNewPage() || firstPage) {
				firstPage = false;
				reportWriterService.setNewPage(false);
				reportWriterService.writeSubTitle(subtitle);
				reportWriterService.writeNewLines(1);
				reportWriterService.writeFormattedMessageLine(hdrRowFormat, headerArgs);
			}

			reportWriterService.writeFormattedMessageLine(rowFormat, supplierUploadSummaryLine.getVendorRequestId(),
					supplierUploadSummaryLine.getVendorName(), supplierUploadSummaryLine.getDocumentNumber(),
					supplierUploadSummaryLine.getVendorNumber(),
					supplierUploadSummaryLine.isSendToPaymentWorks() ? "Yes" : "No");
		}

		reportWriterService.writeNewLines(1);

	}

	public PaymentWorksVendorService getPaymentWorksVendorService() {
		return paymentWorksVendorService;
	}

	public void setPaymentWorksVendorService(PaymentWorksVendorService paymentWorksVendorService) {
		this.paymentWorksVendorService = paymentWorksVendorService;
	}

	public ReportWriterService getReportWriterService() {
		return reportWriterService;
	}

	public void setReportWriterService(ReportWriterService reportWriterService) {
		this.reportWriterService = reportWriterService;
	}

	public PaymentWorksWebService getPaymentWorksWebService() {
		return paymentWorksWebService;
	}

	public void setPaymentWorksWebService(PaymentWorksWebService paymentWorksWebService) {
		this.paymentWorksWebService = paymentWorksWebService;
	}

	public PaymentWorksKfsService getPaymentWorksKfsService() {
		return paymentWorksKfsService;
	}

	public void setPaymentWorksKfsService(PaymentWorksKfsService paymentWorksKfsService) {
		this.paymentWorksKfsService = paymentWorksKfsService;
	}

}
