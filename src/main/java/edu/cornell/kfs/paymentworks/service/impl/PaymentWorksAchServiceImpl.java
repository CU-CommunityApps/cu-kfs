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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.paymentworks.PaymentWorksConstants;
import edu.cornell.kfs.paymentworks.batch.report.AchUpdateSummary;
import edu.cornell.kfs.paymentworks.batch.report.AchUpdateSummaryLine;
import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.service.PaymentWorksAchService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksKfsService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksUtilityService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksVendorService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksWebService;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksUpdateVendorStatus;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesDTO;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class PaymentWorksAchServiceImpl implements PaymentWorksAchService {
	private static final String ROW_FORMAT_EDIT_SUMMARY = "%-15s %-30s %-20s";
	private static final String ROW_FORMAT_REJECT_SUMMARY = ROW_FORMAT_EDIT_SUMMARY + " %s";
	
	private class HEADER_ARGUMENTS {
		private static final String ERROR_MESSAGE = "Error Msg";
		private static final String VENDOR_NUMBERS = "Vendor Number(s)";
		private static final String VENDOR_NAME = "Vendor Name";
		private static final String VENDOR_REQUEST_ID = "Vendor Req ID";
	}
	
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksAchServiceImpl.class);
	
	protected PaymentWorksUtilityService paymentWorksUtilityService;
	protected PaymentWorksVendorService paymentWorksVendorService;
	protected PaymentWorksKfsService paymentWorksKfsService;
	protected PaymentWorksWebService paymentWorksWebService;
	protected DateTimeService dateTimeService;
	protected ReportWriterService reportWriterService;
	
	@Override
	public PayeeACHAccount createPayeeAchAccount(PaymentWorksVendorUpdatesDTO vendorUpdate, String vendorNumber) {
		PayeeACHAccount payeeAchAccount = new PayeeACHAccount();
		Map<String, String> fieldChanges = getPaymentWorksUtilityService().convertFieldArrayToMap(vendorUpdate.getField_changes());
		logFieldChanges(fieldChanges);
		payeeAchAccount.setPayeeIdentifierTypeCode(PdpConstants.PayeeIdTypeCodes.VENDOR_ID);
		payeeAchAccount.setPayeeIdNumber(vendorNumber);
		payeeAchAccount.setBankRoutingNumber(fieldChanges.get(PaymentWorksConstants.FieldNames.ROUTING_NUMBER));
		payeeAchAccount.setBankAccountNumber(fieldChanges.get(PaymentWorksConstants.FieldNames.ACCOUNT_NUMBER));
		payeeAchAccount.setBankAccountTypeCode(PdpConstants.ACH_TRANSACTION_TYPE_DEFAULT);
		payeeAchAccount.setAchTransactionType(PdpConstants.DisbursementTypeCodes.ACH);
		payeeAchAccount.setActive(true);
		payeeAchAccount.setLastUpdateUserId(PaymentWorksConstants.SOURCE_USER);
		payeeAchAccount.setLastUpdate(getDateTimeService().getCurrentTimestamp());
		return payeeAchAccount;
	}
	
	private void logFieldChanges(Map<String, String> fieldChanges) {
		if (LOG.isDebugEnabled()) {
			String keyAndStarterString = "logFieldChanges key: '";
			String valueString = "'  value: '";
			String closer = "'";
			for (String key : fieldChanges.keySet()) {
				LOG.debug(new StringBuilder(keyAndStarterString).append(key).append(valueString).append(key).append(closer).toString());
			}
		}
	}

	@Override
	public PayeeACHAccount createPayeeAchAccount(PayeeACHAccount payeeAchAccountOld, String routingNumber, String accountNumber) {
		PayeeACHAccount payeeAchAccount = (PayeeACHAccount) ObjectUtils.deepCopy(payeeAchAccountOld);
		payeeAchAccount.setBankRoutingNumber(StringUtils.defaultIfEmpty(routingNumber, payeeAchAccount.getBankRoutingNumber()));
		payeeAchAccount.setBankAccountNumber(StringUtils.defaultIfEmpty(accountNumber, payeeAchAccount.getBankAccountNumber()));
		payeeAchAccount.setObjectId(null);
		payeeAchAccount.setVersionNumber(null);
		payeeAchAccount.setAchAccountGeneratedIdentifier(null);
		payeeAchAccount.setActive(true);
		payeeAchAccount.setLastUpdateUserId(PaymentWorksConstants.SOURCE_USER);
		payeeAchAccount.setLastUpdate(getDateTimeService().getCurrentTimestamp());
		return payeeAchAccount;
	}
	
	@Override
	public boolean processACHUpdates(List<PaymentWorksVendorUpdatesDTO> achUpdates) {
		boolean routed = false;
		for (PaymentWorksVendorUpdatesDTO vendorUpdate : achUpdates) {
			if (!getPaymentWorksVendorService().isExistingPaymentWorksVendor(vendorUpdate.getId(), PaymentWorksConstants.TransactionType.ACH_UPDATE)) {
				try {
					routed = processSingleACHUpdate(vendorUpdate) && routed;
				} catch (Exception e) {
					LOG.error("Error processing ACH update (" + vendorUpdate.getId() + "): " + e.getMessage());
					routed = false;
					GlobalVariables.getMessageMap().clearErrorMessages();
				}
			}
		}
		return routed;
	}

	protected boolean processSingleACHUpdate(PaymentWorksVendorUpdatesDTO vendorUpdate) {
		boolean routed;
		PaymentWorksVendor paymentWorksVendor = getPaymentWorksVendorService().savePaymentWorksVendorRecord(
				vendorUpdate, PaymentWorksConstants.ProcessStatus.ACH_UPDATE_COMPLETE, PaymentWorksConstants.TransactionType.ACH_UPDATE);

		if (ObjectUtils.isNotNull(vendorUpdate.getField_changes().getField_changes())) {
			routed = getPaymentWorksKfsService().directAchEdit(vendorUpdate, paymentWorksVendor.getVendorNumberList());
		} else {
			GlobalVariables.getMessageMap().putError("vendorRequestId", KFSKeyConstants.ERROR_CUSTOM, "No field changes provided by PaymentWorks");
			routed = false;
		}

		if (routed) {
			processVendor(paymentWorksVendor, PaymentWorksConstants.PaymentWorksUpdateStatus.PROCESSED,
					PaymentWorksConstants.PaymentWorksStatusText.PROCESSED, PaymentWorksConstants.ProcessStatus.ACH_UPDATE_COMPLETE, routed);
		} else {
			processVendor(paymentWorksVendor, PaymentWorksConstants.PaymentWorksUpdateStatus.PROCESSED,
					PaymentWorksConstants.PaymentWorksStatusText.PROCESSED, PaymentWorksConstants.ProcessStatus.ACH_UPDATE_REJECTED, routed);
		}
		return routed;
	}
	
	protected void processVendor(PaymentWorksVendor paymentWorksNewVendor, String requestStatus,
			String requestStatusText, String processStatus, boolean routed) {
		paymentWorksNewVendor.setRequestStatus(requestStatusText);
		paymentWorksNewVendor.setProcessStatus(processStatus);
		paymentWorksNewVendor = getPaymentWorksVendorService().updatePaymentWorksVendor(paymentWorksNewVendor);

		updatePaymentWorksVendorStatus(paymentWorksNewVendor.getVendorRequestId(), requestStatus);

		addSummaryLine(paymentWorksNewVendor, routed);
	}
	
	protected void updatePaymentWorksVendorStatus(String vendorRequestId, String requestStatus) {
		List<PaymentWorksUpdateVendorStatus> updateNewVendorStatusList = new ArrayList<PaymentWorksUpdateVendorStatus>();
		PaymentWorksUpdateVendorStatus updateNewVendorStatus = new PaymentWorksUpdateVendorStatus();
		updateNewVendorStatus.setId(new Integer(vendorRequestId));
		updateNewVendorStatus.setStatus(new Integer(requestStatus));
		updateNewVendorStatusList.add(updateNewVendorStatus);
		getPaymentWorksWebService().updateVendorUpdatesStatusInPaymentWorks(updateNewVendorStatusList);
	}
	
	protected void addSummaryLine(PaymentWorksVendor paymentWorksVendor, boolean approved) {
		AchUpdateSummaryLine summaryLine = new AchUpdateSummaryLine();
		summaryLine.setVendorRequestId(paymentWorksVendor.getVendorRequestId());
		summaryLine.setVendorName(StringUtils.defaultString(paymentWorksVendor.getVendorName()));
		summaryLine.setVendorNumber(StringUtils.defaultString(paymentWorksVendor.getVendorNumberList()));
		summaryLine.setErrorMessage(getPaymentWorksUtilityService().getGlobalErrorMessage());
		
		AchUpdateSummary achUpdateSummary = new AchUpdateSummary();
		if (approved) {
			achUpdateSummary.getApprovedVendors().add(summaryLine);
		} else {
			achUpdateSummary.getRejectedVendors().add(summaryLine);
		}
		writePaymentWorksAchUpdateSummaryReport(achUpdateSummary);
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
		reportWriterService.writeStatisticLine("%d total ach updates processed", vendorsRejected.size() + vendorsApproved.size());

		return reportWriterService.getReportFile();
	}
	
	protected void writeAchUpdateEditSummaryDetailRecords(String subtitle, List<AchUpdateSummaryLine> records) {
		Object[] headerArgs = {HEADER_ARGUMENTS.VENDOR_REQUEST_ID, HEADER_ARGUMENTS.VENDOR_NAME, HEADER_ARGUMENTS.VENDOR_NUMBERS};
		boolean firstPage = true;

		for (AchUpdateSummaryLine vendorUpdateSummaryLine : records) {
			if (reportWriterService.isNewPage() || firstPage) {
				firstPage = false;
				writeFirstLine(subtitle, ROW_FORMAT_EDIT_SUMMARY, headerArgs);
			}
			reportWriterService.writeFormattedMessageLine(ROW_FORMAT_EDIT_SUMMARY, vendorUpdateSummaryLine.getVendorRequestId(),
					vendorUpdateSummaryLine.getVendorName(), vendorUpdateSummaryLine.getVendorNumber());
		}

		reportWriterService.writeNewLines(1);
	}

	protected void writeFirstLine(String subtitle, String hdrRowFormat, Object[] headerArgs) {
		reportWriterService.setNewPage(false);
		reportWriterService.writeSubTitle(subtitle);
		reportWriterService.writeNewLines(1);
		reportWriterService.writeFormattedMessageLine(hdrRowFormat, headerArgs);
	}
	
	protected void writeAchUpdateRejectedSummaryDetailRecords(String subtitle, List<AchUpdateSummaryLine> records) {
		Object[] headerArgs = {HEADER_ARGUMENTS.VENDOR_REQUEST_ID, HEADER_ARGUMENTS.VENDOR_NAME, HEADER_ARGUMENTS.VENDOR_NUMBERS, HEADER_ARGUMENTS.ERROR_MESSAGE};
		boolean firstPage = true;

		for (AchUpdateSummaryLine vendorUpdateSummaryLine : records) {
			if (reportWriterService.isNewPage() || firstPage) {
				firstPage = false;
				writeFirstLine(subtitle, ROW_FORMAT_REJECT_SUMMARY, headerArgs);
			}
			reportWriterService.writeFormattedMessageLine(ROW_FORMAT_REJECT_SUMMARY, vendorUpdateSummaryLine.getVendorRequestId(),
					vendorUpdateSummaryLine.getVendorName(), vendorUpdateSummaryLine.getVendorNumber(),
					vendorUpdateSummaryLine.getErrorMessage());
		}

		reportWriterService.writeNewLines(1);
	}

	public PaymentWorksUtilityService getPaymentWorksUtilityService() {
		return paymentWorksUtilityService;
	}

	public void setPaymentWorksUtilityService(PaymentWorksUtilityService paymentWorksUtilityService) {
		this.paymentWorksUtilityService = paymentWorksUtilityService;
	}

	public DateTimeService getDateTimeService() {
		return dateTimeService;
	}

	public void setDateTimeService(DateTimeService dateTimeService) {
		this.dateTimeService = dateTimeService;
	}

	public PaymentWorksVendorService getPaymentWorksVendorService() {
		return paymentWorksVendorService;
	}

	public void setPaymentWorksVendorService(PaymentWorksVendorService paymentWorksVendorService) {
		this.paymentWorksVendorService = paymentWorksVendorService;
	}
	
	public PaymentWorksKfsService getPaymentWorksKfsService() {
		return paymentWorksKfsService;
	}

	public void setPaymentWorksKfsService(PaymentWorksKfsService paymentWorksKfsService) {
		this.paymentWorksKfsService = paymentWorksKfsService;
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
}
