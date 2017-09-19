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
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.krad.util.GlobalVariables;

import edu.cornell.kfs.paymentworks.PaymentWorksConstants;
import edu.cornell.kfs.paymentworks.batch.report.NewVendorSummary;
import edu.cornell.kfs.paymentworks.batch.report.NewVendorSummaryLine;
import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.service.PaymentWorksKfsService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksUtilityService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksVendorService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksWebService;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksNewVendorDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksNewVendorDetailDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksNewVendorUpdateVendorStatus;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class PaymentWorksRetrieveNewVendorStep extends AbstractStep {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksRetrieveNewVendorStep.class);

    protected PaymentWorksVendorService paymentWorksVendorService;
    protected PaymentWorksWebService paymentWorksWebService;
    protected PaymentWorksKfsService paymentWorksKfsService;
    protected PaymentWorksUtilityService paymentWorksUtilityService;
    protected ReportWriterService reportWriterService;

    private NewVendorSummary newVendorSummary;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        newVendorSummary = new NewVendorSummary();
        PaymentWorksNewVendorDetailDTO newVendorDetail = null;
        PaymentWorksVendor paymentWorksNewVendor = null;
        boolean routed = false;
        boolean jobCompletedSuccessfully = true;
        List<PaymentWorksNewVendorDTO> results = paymentWorksWebService.getPendingNewVendorRequestsFromPaymentWorks();

        for (PaymentWorksNewVendorDTO newVendor : results) {
            if (!paymentWorksVendorService.isExistingPaymentWorksVendor(newVendor.getId(), PaymentWorksConstants.TransactionType.NEW_VENDOR)) {
                try {
                    newVendorDetail = paymentWorksWebService.getVendorDetailFromPaymentWorks(newVendor.getId());
                    paymentWorksNewVendor = paymentWorksVendorService.savePaymentWorksVendorRecord(newVendorDetail);
                    routed = paymentWorksKfsService.routeNewVendor(paymentWorksNewVendor);
                    if (routed) {
                        processVendor(paymentWorksNewVendor, PaymentWorksConstants.PaymentWorksNewVendorStatus.APPROVED,
                                PaymentWorksConstants.PaymentWorksStatusText.APPROVED,
                                PaymentWorksConstants.ProcessStatus.VENDOR_CREATED, routed);
                    } else {
                        processVendor(paymentWorksNewVendor, PaymentWorksConstants.PaymentWorksNewVendorStatus.REJECTED,
                                PaymentWorksConstants.PaymentWorksStatusText.REJECTED,
                                PaymentWorksConstants.ProcessStatus.VENDOR_REJECTED, routed);
                        jobCompletedSuccessfully = false;
                    }
                    LOG.info("paymentWorksNewVendor.isCustomFieldConversionErrors()" + paymentWorksNewVendor.isCustomFieldConversionErrors());
                    jobCompletedSuccessfully = jobCompletedSuccessfully && !paymentWorksNewVendor.isCustomFieldConversionErrors();
                } catch (Exception e) {
                    LOG.error("Error processing new vendor(" + newVendor.getId() + "): " + e.getMessage());
                    routed = false;
                    jobCompletedSuccessfully = false;
                    GlobalVariables.getMessageMap().clearErrorMessages();
                }
            }
        }
        writePaymentWorksNewVendorSummaryReport();
        if (!jobCompletedSuccessfully) {
            throw new RuntimeException("There was an error processing new vendors from PaymentWorks");
        }
        return jobCompletedSuccessfully;
    }

    protected void processVendor(PaymentWorksVendor paymentWorksNewVendor, String requestStatus,
            String requestStatusText, String processStatus, boolean routed) {
        paymentWorksNewVendor.setRequestStatus(requestStatusText);
        paymentWorksNewVendor.setProcessStatus(processStatus);
        paymentWorksNewVendor = paymentWorksVendorService.updatePaymentWorksVendor(paymentWorksNewVendor);

        updatePaymentWorksVendorStatus(paymentWorksNewVendor.getVendorRequestId(), requestStatus);
        addSummaryLine(paymentWorksNewVendor, routed);

        if (routed) {
            paymentWorksKfsService.sendVendorInitiatedEmail(paymentWorksNewVendor.getDocumentNumber(),
                    paymentWorksNewVendor.getRequestingCompanyLegalName(), paymentWorksNewVendor.getEmailAddress());
        }
    }

    protected void addSummaryLine(PaymentWorksVendor paymentWorksNewVendor, boolean approved) {
        NewVendorSummaryLine summaryLine = new NewVendorSummaryLine();
        summaryLine.setVendorRequestId(paymentWorksNewVendor.getVendorRequestId());
        summaryLine.setVendorName(paymentWorksNewVendor.getRequestingCompanyLegalName());
        summaryLine.setDocumentNumber(StringUtils.defaultString(paymentWorksNewVendor.getDocumentNumber()));
        summaryLine.setErrorMessage(getPaymentWorksUtilityService().getGlobalErrorMessage());

        if (approved) {
            newVendorSummary.getApprovedVendors().add(summaryLine);
        } else {
            newVendorSummary.getRejectedVendors().add(summaryLine);
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

    protected File writePaymentWorksNewVendorSummaryReport() {
        if (reportWriterService == null) {
            throw new IllegalStateException("ReportWriterService not configured for PaymentWorks New Vendor service.");
        } else {
            reportWriterService.initialize();
        }

        List<NewVendorSummaryLine> approvedVendors = newVendorSummary.getApprovedVendors();
        List<NewVendorSummaryLine> rejectedVendors = newVendorSummary.getRejectedVendors();

        writeNewVendorCreatedSummaryDetailRecords("New Vendor Request Documents Created", approvedVendors);
        writeNewVendorRejectedSummaryDetailRecords("New Vendor Requests Rejected", rejectedVendors);

        reportWriterService.writeStatisticLine("%d vendor request documents created", approvedVendors.size());
        reportWriterService.writeStatisticLine("%d vendor requests rejected", rejectedVendors.size());
        reportWriterService.writeStatisticLine("%d total vendor requests processed", approvedVendors.size() + rejectedVendors.size());

        return reportWriterService.getReportFile();
    }

    protected void writeNewVendorCreatedSummaryDetailRecords(String subtitle, List<NewVendorSummaryLine> records) {
        String rowFormat = "%-15s %-30s %-14s";
        String hdrRowFormat = "%-15s %-30s %-14s";
        Object[] headerArgs = { "Vendor Req ID", "Vendor Name", "Document Number" };
        boolean firstPage = true;

        for (NewVendorSummaryLine newVendorSummaryLine : records) {
            if (reportWriterService.isNewPage() || firstPage) {
                firstPage = false;
                reportWriterService.setNewPage(false);
                reportWriterService.writeSubTitle(subtitle);
                reportWriterService.writeNewLines(1);
                reportWriterService.writeFormattedMessageLine(hdrRowFormat, headerArgs);
            }

            reportWriterService.writeFormattedMessageLine(rowFormat, newVendorSummaryLine.getVendorRequestId(),
                    newVendorSummaryLine.getVendorName(), newVendorSummaryLine.getDocumentNumber());
        }
        reportWriterService.writeNewLines(1);
    }

    protected void writeNewVendorRejectedSummaryDetailRecords(String subtitle, List<NewVendorSummaryLine> records) {
        String rowFormat = "%-15s %-30s %s";
        String hdrRowFormat = "%-15s %-30s %s";
        Object[] headerArgs = { "Vendor Req ID", "Vendor Name", "Error Msg" };
        boolean firstPage = true;

        for (NewVendorSummaryLine newVendorSummaryLine : records) {
            if (reportWriterService.isNewPage() || firstPage) {
                firstPage = false;
                reportWriterService.setNewPage(false);
                reportWriterService.writeSubTitle(subtitle);
                reportWriterService.writeNewLines(1);
                reportWriterService.writeFormattedMessageLine(hdrRowFormat, headerArgs);
            }

            reportWriterService.writeFormattedMessageLine(rowFormat, newVendorSummaryLine.getVendorRequestId(),
                    newVendorSummaryLine.getVendorName(), newVendorSummaryLine.getErrorMessage());
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
