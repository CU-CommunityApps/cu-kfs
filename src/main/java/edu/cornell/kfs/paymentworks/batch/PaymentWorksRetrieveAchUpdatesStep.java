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
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.paymentworks.PaymentWorksConstants;
import edu.cornell.kfs.paymentworks.batch.report.AchUpdateSummary;
import edu.cornell.kfs.paymentworks.batch.report.AchUpdateSummaryLine;
import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.service.PaymentWorksKfsService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksVendorService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksWebService;
import edu.cornell.kfs.paymentworks.util.PaymentWorksUtil;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksUpdateVendorStatus;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesDTO;
import edu.cornell.kfs.sys.service.ReportWriterService;

@Transactional
public class PaymentWorksRetrieveAchUpdatesStep extends AbstractStep {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(PaymentWorksRetrieveAchUpdatesStep.class);

	private PaymentWorksVendorService paymentWorksVendorService;
	private PaymentWorksWebService paymentWorksWebService;
	private PaymentWorksKfsService paymentWorksKfsService;
	private ReportWriterService reportWriterService;

	private AchUpdateSummary achUpdateSummary;

	@Override
	public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {

		achUpdateSummary = new AchUpdateSummary();

		boolean routed = false;

		// get list of pending vendors updates
		List<PaymentWorksVendorUpdatesDTO> results = paymentWorksWebService.getPendingAchUpdatesFromPaymentWorks();

		// loop through list
		for (PaymentWorksVendorUpdatesDTO vendorUpdate : results) {

			// check if already exists in DB
			if (!paymentWorksVendorService.isExistingPaymentWorksVendor(vendorUpdate.getId(),
					PaymentWorksConstants.TransactionType.ACH_UPDATE)) {

				try {
					// save vendor detail record
					PaymentWorksVendor paymentWorksVendor = paymentWorksVendorService.savePaymentWorksVendorRecord(
							vendorUpdate, PaymentWorksConstants.ProcessStatus.ACH_UPDATE_COMPLETE,
							PaymentWorksConstants.TransactionType.ACH_UPDATE);

					if (ObjectUtils.isNotNull(vendorUpdate.getField_changes().getField_changes())) {
						// make direct updates
						routed = paymentWorksKfsService.directAchEdit(vendorUpdate,
								paymentWorksVendor.getVendorNumberList());
					} else {
						GlobalVariables.getMessageMap().putError("vendorRequestId", KFSKeyConstants.ERROR_CUSTOM,
								"No field changes provided by PaymentWorks");
						routed = false;
					}

					// process vendor record staging, status, summary
					if (routed) {
						processVendor(paymentWorksVendor, PaymentWorksConstants.PaymentWorksUpdateStatus.PROCESSED,
								PaymentWorksConstants.PaymentWorksStatusText.PROCESSED,
								PaymentWorksConstants.ProcessStatus.ACH_UPDATE_COMPLETE, routed);
					} else {
						processVendor(paymentWorksVendor, PaymentWorksConstants.PaymentWorksUpdateStatus.PROCESSED,
								PaymentWorksConstants.PaymentWorksStatusText.PROCESSED,
								PaymentWorksConstants.ProcessStatus.ACH_UPDATE_REJECTED, routed);
					}

				} catch (Exception e) {
					LOG.error("Error processing ACH update (" + vendorUpdate.getId() + "): " + e.getMessage());
					routed = false;
					GlobalVariables.getMessageMap().clearErrorMessages();
				}
			}
		}

		// send email summary
		paymentWorksKfsService.sendSummaryEmail(writePaymentWorksAchUpdateSummaryReport(achUpdateSummary),
				"PaymentWorks ACH Update Summary Report");

		return true;

	}

	protected void processVendor(PaymentWorksVendor paymentWorksNewVendor, String requestStatus,
			String requestStatusText, String processStatus, boolean routed) {
		paymentWorksNewVendor.setRequestStatus(requestStatusText);
		paymentWorksNewVendor.setProcessStatus(processStatus);
		paymentWorksNewVendor = paymentWorksVendorService.updatePaymentWorksVendor(paymentWorksNewVendor);

		updatePaymentWorksVendorStatus(paymentWorksNewVendor.getVendorRequestId(), requestStatus);

		addSummaryLine(paymentWorksNewVendor, routed);
	}

	protected void addSummaryLine(PaymentWorksVendor paymentWorksVendor, boolean approved) {

		AchUpdateSummaryLine summaryLine = new AchUpdateSummaryLine();
		summaryLine.setVendorRequestId(paymentWorksVendor.getVendorRequestId());
		summaryLine.setVendorName(StringUtils.defaultString(paymentWorksVendor.getVendorName()));
		summaryLine.setVendorNumber(StringUtils.defaultString(paymentWorksVendor.getVendorNumberList()));
		summaryLine.setErrorMessage(new PaymentWorksUtil().getGlobalErrorMessage());

		if (approved) {
			achUpdateSummary.getApprovedVendors().add(summaryLine);
		} else {
			achUpdateSummary.getRejectedVendors().add(summaryLine);
		}
	}

	protected void updatePaymentWorksVendorStatus(String vendorRequestId, String requestStatus) {

		// create update list of one
		List<PaymentWorksUpdateVendorStatus> updateNewVendorStatusList = new ArrayList<PaymentWorksUpdateVendorStatus>();
		PaymentWorksUpdateVendorStatus updateNewVendorStatus = new PaymentWorksUpdateVendorStatus();
		updateNewVendorStatus.setId(new Integer(vendorRequestId));
		updateNewVendorStatus.setStatus(new Integer(requestStatus));
		updateNewVendorStatusList.add(updateNewVendorStatus);

		paymentWorksWebService.updateVendorUpdatesStatusInPaymentWorks(updateNewVendorStatusList);
	}

	protected File writePaymentWorksAchUpdateSummaryReport(AchUpdateSummary achUpdateSummary) {

		if (reportWriterService == null) {
			throw new IllegalStateException("ReportWriterService not configured for PaymentWorks ACH Update service.");
		} else {
			reportWriterService.initialize();
		}

		List<AchUpdateSummaryLine> vendorsApproved = achUpdateSummary.getApprovedVendors();
		List<AchUpdateSummaryLine> vendorsRejected = achUpdateSummary.getRejectedVendors();

		writeAchUpdateEditSummaryDetailRecords("ACH Update Direct Edits", vendorsApproved);
		writeAchUpdateRejectedSummaryDetailRecords("ACH Updates Rejected", vendorsRejected);

		reportWriterService.writeStatisticLine("%d ach update direct edits", vendorsApproved.size());
		reportWriterService.writeStatisticLine("%d ach updates rejected", vendorsRejected.size());
		reportWriterService.writeStatisticLine("%d total ach updates processed",
				vendorsRejected.size() + vendorsApproved.size());

		return reportWriterService.getReportFile();
	}

	protected void writeAchUpdateEditSummaryDetailRecords(String subtitle, List<AchUpdateSummaryLine> records) {
		String rowFormat = "%-15s %-30s %-20s";
		String hdrRowFormat = "%-15s %-30s %-20s";
		Object[] headerArgs = { "Vendor Req ID", "Vendor Name", "Vendor Number(s)" };

		boolean firstPage = true;

		for (AchUpdateSummaryLine vendorUpdateSummaryLine : records) {
			if (reportWriterService.isNewPage() || firstPage) {
				firstPage = false;
				reportWriterService.setNewPage(false);
				reportWriterService.writeSubTitle(subtitle);
				reportWriterService.writeNewLines(1);
				reportWriterService.writeFormattedMessageLine(hdrRowFormat, headerArgs);
			}

			reportWriterService.writeFormattedMessageLine(rowFormat, vendorUpdateSummaryLine.getVendorRequestId(),
					vendorUpdateSummaryLine.getVendorName(), vendorUpdateSummaryLine.getVendorNumber());
		}

		reportWriterService.writeNewLines(1);

	}

	protected void writeAchUpdateRejectedSummaryDetailRecords(String subtitle, List<AchUpdateSummaryLine> records) {
		String rowFormat = "%-15s %-30s %-20s %s";
		String hdrRowFormat = "%-15s %-30s %-20s %s";
		Object[] headerArgs = { "Vendor Req ID", "Vendor Name", "Vendor Number(s)", "Error Msg" };

		boolean firstPage = true;

		for (AchUpdateSummaryLine vendorUpdateSummaryLine : records) {
			if (reportWriterService.isNewPage() || firstPage) {
				firstPage = false;
				reportWriterService.setNewPage(false);
				reportWriterService.writeSubTitle(subtitle);
				reportWriterService.writeNewLines(1);
				reportWriterService.writeFormattedMessageLine(hdrRowFormat, headerArgs);
			}

			reportWriterService.writeFormattedMessageLine(rowFormat, vendorUpdateSummaryLine.getVendorRequestId(),
					vendorUpdateSummaryLine.getVendorName(), vendorUpdateSummaryLine.getVendorNumber(),
					vendorUpdateSummaryLine.getErrorMessage());
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
