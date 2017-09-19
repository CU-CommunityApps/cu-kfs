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
import edu.cornell.kfs.paymentworks.batch.report.VendorUpdateSummary;
import edu.cornell.kfs.paymentworks.batch.report.VendorUpdateSummaryLine;
import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.service.PaymentWorksKfsService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksUtilityService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksVendorService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksWebService;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksUpdateVendorStatus;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesDTO;
import edu.cornell.kfs.sys.service.ReportWriterService;

@Transactional
public class PaymentWorksRetrieveVendorUpdatesStep extends AbstractStep {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksRetrieveVendorUpdatesStep.class);

    private PaymentWorksVendorService paymentWorksVendorService;
    private PaymentWorksWebService paymentWorksWebService;
    private PaymentWorksKfsService paymentWorksKfsService;
    private PaymentWorksUtilityService paymentWorksUtilityService;
    private ReportWriterService reportWriterService;
    private VendorUpdateSummary vendorUpdateSummary;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {

        vendorUpdateSummary = new VendorUpdateSummary();

        // do direct only updates first to prevent dirty data on document
        // created data
        // that could happen before the direct edit
        processAddressVendorUpdates();
        processCompanyVendorUpdates();
        return true;
    }

    /**
     * Can make direct edits, these are address vendor updates
     *
     * @param updateVendorUpdateStatusList
     * @param vendorUpdateSummary
     */
    protected void processAddressVendorUpdates() {

        boolean routed = false;

        List<PaymentWorksVendorUpdatesDTO> results = paymentWorksWebService.getPendingAddressVendorUpdatesFromPaymentWorks();

        for (PaymentWorksVendorUpdatesDTO vendorUpdate : results) {

            if (!paymentWorksVendorService.isExistingPaymentWorksVendor(vendorUpdate.getId(), PaymentWorksConstants.TransactionType.VENDOR_UPDATE)) {

                try {
                    PaymentWorksVendor paymentWorksVendor = paymentWorksVendorService.savePaymentWorksVendorRecord(
                            vendorUpdate, PaymentWorksConstants.ProcessStatus.VENDOR_REQUESTED,
                            PaymentWorksConstants.TransactionType.VENDOR_UPDATE);

                    if (ObjectUtils.isNotNull(vendorUpdate.getField_changes().getField_changes())) {
                        // make direct updates
                        routed = paymentWorksKfsService.directVendorEdit(paymentWorksVendor);
                    } else {
                        GlobalVariables.getMessageMap().putError("vendorRequestId", KFSKeyConstants.ERROR_CUSTOM,
                                "No field changes provided by PaymentWorks");
                        routed = false;
                    }

                    // process vendor record staging, status, summary
                    if (routed) {
                        processVendor(paymentWorksVendor, PaymentWorksConstants.PaymentWorksUpdateStatus.PROCESSED,
                                PaymentWorksConstants.PaymentWorksStatusText.PROCESSED,
                                PaymentWorksConstants.ProcessStatus.VENDOR_UPDATE_COMPLETE, routed, true);
                    } else {
                        processVendor(paymentWorksVendor, PaymentWorksConstants.PaymentWorksUpdateStatus.PROCESSED,
                                PaymentWorksConstants.PaymentWorksStatusText.PROCESSED,
                                PaymentWorksConstants.ProcessStatus.VENDOR_UPDATE_REJECTED, routed, true);
                    }

                } catch (Exception e) {
                    LOG.error("Error processing vendor address update (" + vendorUpdate.getId() + "): " + e.getMessage());
                    routed = false;
                    GlobalVariables.getMessageMap().clearErrorMessages();
                }
            }
        }
    }

    /**
     * Can make direct or document created edits, these are company vendor
     * updates
     *
     * @param updateVendorUpdateStatusList
     * @param vendorUpdateSummary
     */
    protected void processCompanyVendorUpdates() {

        boolean routed = false;
        boolean directEdit = false;

        // get list of pending vendors updates
        List<PaymentWorksVendorUpdatesDTO> results = paymentWorksWebService.getPendingCompanyVendorUpdatesFromPaymentWorks();

        // loop through list
        for (PaymentWorksVendorUpdatesDTO vendorUpdate : results) {

            // check if already exists in DB
            if (!paymentWorksVendorService.isExistingPaymentWorksVendor(vendorUpdate.getId(), PaymentWorksConstants.TransactionType.VENDOR_UPDATE)) {

                try {
                    // save vendor detail record
                    PaymentWorksVendor paymentWorksVendor = paymentWorksVendorService.savePaymentWorksVendorRecord(
                            vendorUpdate, PaymentWorksConstants.ProcessStatus.VENDOR_REQUESTED,
                            PaymentWorksConstants.TransactionType.VENDOR_UPDATE);

                    if (ObjectUtils.isNotNull(vendorUpdate.getField_changes().getField_changes())) {

                        if (paymentWorksVendorService.isVendorUpdateEligibleForRouting(paymentWorksVendor)) {
                            // route vendor edit document
                            routed = paymentWorksKfsService.routeVendorEdit(paymentWorksVendor);
                            directEdit = false;
                        } else {
                            // make direct updates
                            routed = paymentWorksKfsService.directVendorEdit(paymentWorksVendor);
                            directEdit = true;
                        }
                    } else {
                        GlobalVariables.getMessageMap().putError("vendorRequestId", KFSKeyConstants.ERROR_CUSTOM, "No field changes provided by PaymentWorks");
                        routed = false;
                    }

                    // process vendor record staging, status, summary
                    if (routed) {
                        processVendor(paymentWorksVendor, PaymentWorksConstants.PaymentWorksUpdateStatus.PROCESSED,
                                PaymentWorksConstants.PaymentWorksStatusText.PROCESSED,
                                directEdit ? PaymentWorksConstants.ProcessStatus.VENDOR_UPDATE_COMPLETE
                                        : PaymentWorksConstants.ProcessStatus.VENDOR_UPDATE_CREATED,
                                routed, directEdit);
                    } else {
                        processVendor(paymentWorksVendor, PaymentWorksConstants.PaymentWorksUpdateStatus.PROCESSED,
                                PaymentWorksConstants.PaymentWorksStatusText.PROCESSED,
                                PaymentWorksConstants.ProcessStatus.VENDOR_UPDATE_REJECTED, routed, directEdit);
                    }

                } catch (Exception e) {
                    LOG.error("Error processing vendor company update (" + vendorUpdate.getId() + "): " + e.getMessage());
                    routed = false;
                    GlobalVariables.getMessageMap().clearErrorMessages();
                }
            }
        }
    }

    protected void processVendor(PaymentWorksVendor paymentWorksVendor, String requestStatus, String requestStatusText, String processStatus, boolean routed, boolean directEdit) {
        paymentWorksVendor.setRequestStatus(requestStatusText);
        paymentWorksVendor.setProcessStatus(processStatus);
        paymentWorksVendor = paymentWorksVendorService.updatePaymentWorksVendor(paymentWorksVendor);

        updatePaymentWorksVendorStatus(paymentWorksVendor.getVendorRequestId(), requestStatus);

        addSummaryLine(paymentWorksVendor, routed, directEdit);
    }

    protected void addSummaryLine(PaymentWorksVendor paymentWorksVendor, boolean approved, boolean directEdit) {

        VendorUpdateSummaryLine summaryLine = new VendorUpdateSummaryLine();
        summaryLine.setVendorRequestId(paymentWorksVendor.getVendorRequestId());
        summaryLine.setGroupName(paymentWorksVendor.getGroupName());
        summaryLine.setVendorName(StringUtils.defaultString(paymentWorksVendor.getVendorName()));
        summaryLine.setVendorNumber(StringUtils.defaultString(paymentWorksVendor.getVendorNumberList()));
        summaryLine.setDocumentNumber(StringUtils.defaultString(paymentWorksVendor.getDocumentNumberList()));
        summaryLine.setErrorMessage(getPaymentWorksUtilityService().getGlobalErrorMessage());

        if (approved) {
            if (directEdit) {
                vendorUpdateSummary.getVendorsDirectUpdate().add(summaryLine);
            } else {
                vendorUpdateSummary.getVendorsCreated().add(summaryLine);
            }
        } else {
            vendorUpdateSummary.getVendorsRejected().add(summaryLine);
        }
    }

    protected void updatePaymentWorksVendorStatus(String vendorRequestId, String requestStatus) {
        List<PaymentWorksUpdateVendorStatus> updateNewVendorStatusList = new ArrayList<PaymentWorksUpdateVendorStatus>();
        PaymentWorksUpdateVendorStatus updateNewVendorStatus = new PaymentWorksUpdateVendorStatus();
        updateNewVendorStatus.setId(new Integer(vendorRequestId));
        updateNewVendorStatus.setStatus(new Integer(requestStatus));
        updateNewVendorStatusList.add(updateNewVendorStatus);

        getPaymentWorksWebService().updateNewVendorUpdatesStatusInPaymentWorks(updateNewVendorStatusList);
    }

    protected File writePaymentWorksVendorUpdateSummaryReport(VendorUpdateSummary vendorUpdateSummary) {

        if (reportWriterService == null) {
            throw new IllegalStateException("ReportWriterService not configured for PaymentWorks Vendor Update service.");
        } else {
            reportWriterService.initialize();
        }

        List<VendorUpdateSummaryLine> vendorsCreated = vendorUpdateSummary.getVendorsCreated();
        List<VendorUpdateSummaryLine> vendorsDirectUpdate = vendorUpdateSummary.getVendorsDirectUpdate();
        List<VendorUpdateSummaryLine> vendorsRejected = vendorUpdateSummary.getVendorsRejected();

        writeVendorUpdateEditSummaryDetailRecords("Vendor Update Documents Created", vendorsCreated);
        writeVendorUpdateEditSummaryDetailRecords("Vendor Update Direct Edits", vendorsDirectUpdate);
        writeVendorUpdateRejectedSummaryDetailRecords("Vendor Updates Rejected", vendorsRejected);

        reportWriterService.writeStatisticLine("%d vendor update documents created", vendorsCreated.size());
        reportWriterService.writeStatisticLine("%d vendor update direct edits", vendorsDirectUpdate.size());
        reportWriterService.writeStatisticLine("%d vendor updates rejected", vendorsRejected.size());
        reportWriterService.writeStatisticLine("%d total vendor updates processed",
                vendorsCreated.size() + vendorsRejected.size() + vendorsDirectUpdate.size());

        return reportWriterService.getReportFile();
    }

    protected void writeVendorUpdateEditSummaryDetailRecords(String subtitle, List<VendorUpdateSummaryLine> records) {
        String rowFormat = "%-15s %-30s %-20s %-20s";
        String hdrRowFormat = "%-15s %-30s %-20s %-20s";
        Object[] headerArgs = { "Vendor Req ID", "Vendor Name", "Document Number(s)", "Vendor Number(s)" };

        boolean firstPage = true;

        for (VendorUpdateSummaryLine vendorUpdateSummaryLine : records) {
            if (reportWriterService.isNewPage() || firstPage) {
                firstPage = false;
                reportWriterService.setNewPage(false);
                reportWriterService.writeSubTitle(subtitle);
                reportWriterService.writeNewLines(1);
                reportWriterService.writeFormattedMessageLine(hdrRowFormat, headerArgs);
            }

            reportWriterService.writeFormattedMessageLine(rowFormat, vendorUpdateSummaryLine.getVendorRequestId(),
                    vendorUpdateSummaryLine.getVendorName(), vendorUpdateSummaryLine.getDocumentNumber(),
                    vendorUpdateSummaryLine.getVendorNumber());
        }

        reportWriterService.writeNewLines(1);

    }

    protected void writeVendorUpdateRejectedSummaryDetailRecords(String subtitle, List<VendorUpdateSummaryLine> records) {
        String rowFormat = "%-15s %-30s %-20s %-20s %s";
        String hdrRowFormat = "%-15s %-30s %-20s %-20s %s";
        Object[] headerArgs = { "Vendor Req ID", "Vendor Name", "Vendor Number(s)", "Group Name", "Error Msg" };

        boolean firstPage = true;

        for (VendorUpdateSummaryLine vendorUpdateSummaryLine : records) {
            if (reportWriterService.isNewPage() || firstPage) {
                firstPage = false;
                reportWriterService.setNewPage(false);
                reportWriterService.writeSubTitle(subtitle);
                reportWriterService.writeNewLines(1);
                reportWriterService.writeFormattedMessageLine(hdrRowFormat, headerArgs);
            }

            reportWriterService.writeFormattedMessageLine(rowFormat, vendorUpdateSummaryLine.getVendorRequestId(),
                    vendorUpdateSummaryLine.getVendorName(), vendorUpdateSummaryLine.getVendorNumber(),
                    vendorUpdateSummaryLine.getGroupName(), vendorUpdateSummaryLine.getErrorMessage());
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

    public PaymentWorksUtilityService getPaymentWorksUtilityService() {
        return paymentWorksUtilityService;
    }

    public void setPaymentWorksUtilityService(PaymentWorksUtilityService paymentWorksUtilityService) {
        this.paymentWorksUtilityService = paymentWorksUtilityService;
    }

}
