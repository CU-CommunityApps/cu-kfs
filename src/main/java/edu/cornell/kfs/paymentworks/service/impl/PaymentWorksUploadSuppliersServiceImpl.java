package edu.cornell.kfs.paymentworks.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.paymentworks.PaymentWorksConstants;
import edu.cornell.kfs.paymentworks.batch.report.SupplierUploadSummary;
import edu.cornell.kfs.paymentworks.batch.report.SupplierUploadSummaryLine;
import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.service.PaymentWorksKfsService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksSupplierConversionService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksUploadSuppliersService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksVendorService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksWebService;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksNewVendorUpdateVendorStatus;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksSupplierUploadDTO;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class PaymentWorksUploadSuppliersServiceImpl implements PaymentWorksUploadSuppliersService {
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksUploadSuppliersServiceImpl.class);
	
	protected PaymentWorksVendorService paymentWorksVendorService;
	protected PaymentWorksWebService paymentWorksWebService;
	protected ReportWriterService reportWriterService;
	protected PaymentWorksSupplierConversionService paymentWorksSupplierConversionService;

	@Override
	public boolean uploadNewVendorApprovedSupplierFile() {
		Collection<PaymentWorksVendor> approvedVendors = paymentWorksVendorService.getPaymentWorksVendorRecords(
				PaymentWorksConstants.ProcessStatus.VENDOR_APPROVED, null, PaymentWorksConstants.TransactionType.NEW_VENDOR);
		
		boolean uploaded = false;
		if (ObjectUtils.isNotNull(approvedVendors) && !approvedVendors.isEmpty()) {
			List<PaymentWorksSupplierUploadDTO> paymentWorksSupplierUploadList = 
					getPaymentWorksSupplierConversionService().createPaymentWorksSupplierUploadList(approvedVendors);

			uploaded = paymentWorksWebService.uploadSuppliers(paymentWorksSupplierUploadList);

			if (uploaded) {
				LOG.info("Supplier was uploaded!");
				for (PaymentWorksVendor newVendor : approvedVendors) {
					String supplierStatusType = findSupplierStatusType(newVendor);
					boolean updateStatus = !StringUtils.startsWith(newVendor.getVendorRequestId(), PaymentWorksConstants.VENDOR_REQUEST_ID_KFS_TEMP_ID_STARTER);

					processVendor(newVendor, PaymentWorksConstants.PaymentWorksNewVendorStatus.PROCESSED,
							PaymentWorksConstants.PaymentWorksStatusText.PROCESSED, PaymentWorksConstants.ProcessStatus.SUPPLIER_UPLOADED,
							supplierStatusType, updateStatus);
				}
			} else {
				LOG.error("uploadNewVendorApprovedSupplierFile, the vendors were not uploaded.");
			}
		}
		return uploaded;
	}
		
	protected String findSupplierStatusType(PaymentWorksVendor newVendor) {
		return !StringUtils.startsWith(newVendor.getVendorRequestId(), PaymentWorksConstants.VENDOR_REQUEST_ID_KFS_TEMP_ID_STARTER)
				? PaymentWorksConstants.SupplierUploadSummaryTypes.KFS_NEW_VENDORS
				: PaymentWorksConstants.SupplierUploadSummaryTypes.PAYMENT_WORKS_NEW_VENDORS;
	}

	@Override
	public boolean updateNewVendorDisapprovedStatus() {
		Collection<PaymentWorksVendor> disapprovedVendors = paymentWorksVendorService.getPaymentWorksVendorRecords(
				PaymentWorksConstants.ProcessStatus.VENDOR_DISAPPROVED, PaymentWorksConstants.PaymentWorksStatusText.APPROVED,
				PaymentWorksConstants.TransactionType.NEW_VENDOR);

		for (PaymentWorksVendor newVendor : disapprovedVendors) {
			processVendor(newVendor, PaymentWorksConstants.PaymentWorksNewVendorStatus.REJECTED,
					PaymentWorksConstants.PaymentWorksStatusText.REJECTED, PaymentWorksConstants.ProcessStatus.VENDOR_DISAPPROVED,
					PaymentWorksConstants.SupplierUploadSummaryTypes.DISAPPROVED_VENDORS, true);
		}
		return true;
	}

	@Override
	public boolean uploadVendorUpdateApprovedSupplierFile() {
		Collection<PaymentWorksVendor> approvedVendors = paymentWorksVendorService.getPaymentWorksVendorRecords(
				PaymentWorksConstants.ProcessStatus.VENDOR_APPROVED, null, PaymentWorksConstants.TransactionType.VENDOR_UPDATE);
		boolean uploaded = false;
		if (ObjectUtils.isNotNull(approvedVendors) && !approvedVendors.isEmpty()) {
			List<PaymentWorksSupplierUploadDTO> paymentWorksSupplierUploadList = getPaymentWorksSupplierConversionService().createPaymentWorksSupplierUploadList(approvedVendors);
			uploaded = paymentWorksWebService.uploadSuppliers(paymentWorksSupplierUploadList);

			if (uploaded) {
				for (PaymentWorksVendor newVendor : approvedVendors) {
					processVendor(newVendor, PaymentWorksConstants.PaymentWorksUpdateStatus.PROCESSED,
							PaymentWorksConstants.PaymentWorksStatusText.PROCESSED, PaymentWorksConstants.ProcessStatus.SUPPLIER_UPLOADED,
							PaymentWorksConstants.SupplierUploadSummaryTypes.VENDOR_UPDATES, false);
				}
			} else {
				LOG.error("uploadVendorUpdateApprovedSupplierFile, the vendors were not uploaded.");
			}
		}
		return uploaded;
	}
	
	protected void processVendor(PaymentWorksVendor paymentWorksNewVendor, String requestStatus,
			String requestStatusText, String processStatus, String supplierUploadSummaryType, boolean updatePaymentWorksStatus) {
		addSummaryLine(paymentWorksNewVendor, supplierUploadSummaryType);

		paymentWorksNewVendor.setRequestStatus(requestStatusText);
		paymentWorksNewVendor.setProcessStatus(processStatus);
		paymentWorksNewVendor = paymentWorksVendorService.updatePaymentWorksVendor(paymentWorksNewVendor);

		if (updatePaymentWorksStatus) {
			updatePaymentWorksVendorStatus(paymentWorksNewVendor.getVendorRequestId(), requestStatus);
		}
	}
	
	protected void addSummaryLine(PaymentWorksVendor paymentWorksVendor, String supplierUploadSummaryType) {
		SupplierUploadSummary supplierUploadSummary = new SupplierUploadSummary();
		SupplierUploadSummaryLine summaryLine = new SupplierUploadSummaryLine();
		summaryLine.setVendorRequestId(paymentWorksVendor.getVendorRequestId());
		summaryLine.setVendorName(paymentWorksVendor.getRequestingCompanyLegalName());
		summaryLine.setDocumentNumber(paymentWorksVendor.getDocumentNumber());
		summaryLine.setVendorNumber(paymentWorksVendor.getVendorHeaderGeneratedIdentifier() + KFSConstants.DASH
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
			throw new IllegalStateException("ReportWriterService not configured for PaymentWorks Supplier Upload service.");
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

		boolean firstLine = true;

		for (SupplierUploadSummaryLine supplierUploadSummaryLine : records) {
			if (reportWriterService.isNewPage() || firstLine) {
				firstLine = false;
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

	public PaymentWorksWebService getPaymentWorksWebService() {
		return paymentWorksWebService;
	}

	public void setPaymentWorksWebService(PaymentWorksWebService paymentWorksWebService) {
		this.paymentWorksWebService = paymentWorksWebService;
	}

	public ReportWriterService getReportWriterService() {
		return reportWriterService;
	}

	public void setReportWriterService(ReportWriterService reportWriterService) {
		this.reportWriterService = reportWriterService;
	}

	public PaymentWorksSupplierConversionService getPaymentWorksSupplierConversionService() {
		return paymentWorksSupplierConversionService;
	}

	public void setPaymentWorksSupplierConversionService(
			PaymentWorksSupplierConversionService paymentWorksSupplierConversionService) {
		this.paymentWorksSupplierConversionService = paymentWorksSupplierConversionService;
	}
	
	

}
