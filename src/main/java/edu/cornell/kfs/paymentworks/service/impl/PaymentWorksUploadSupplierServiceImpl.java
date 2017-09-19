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
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.paymentworks.PaymentWorksConstants;
import edu.cornell.kfs.paymentworks.batch.report.SupplierUploadSummary;
import edu.cornell.kfs.paymentworks.batch.report.SupplierUploadSummaryLine;
import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.service.PaymentWorksNewVendorConversionService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksUploadSupplierService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksUtilityService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksVendorService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksWebService;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksNewVendorUpdateVendorStatus;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksSupplierUploadDTO;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class PaymentWorksUploadSupplierServiceImpl implements PaymentWorksUploadSupplierService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksUploadSupplierServiceImpl.class);

    protected PaymentWorksNewVendorConversionService paymentWorksNewVendorConversionService;
    protected DateTimeService dateTimeService;
    protected ReportWriterService reportWriterService;
    protected PaymentWorksVendorService paymentWorksVendorService;
    protected PaymentWorksWebService paymentWorksWebService;
    protected ParameterService parameterService;
    protected PaymentWorksUtilityService paymentWorksUtilityService;

    @Override
    public List<PaymentWorksSupplierUploadDTO> createPaymentWorksSupplierUploadList(Collection<PaymentWorksVendor> newVendors) {
        List<PaymentWorksSupplierUploadDTO> paymentWorksSupplierUploadList = new ArrayList<PaymentWorksSupplierUploadDTO>();
        for (PaymentWorksVendor newVendor : newVendors) {
            PaymentWorksVendor vendorToCopy = newVendor;

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

        String vendorNumber = vendorToCopy.getVendorHeaderGeneratedIdentifier() != null ? vendorToCopy.getVendorHeaderGeneratedIdentifier().toString() : StringUtils.EMPTY;
        String siteCode = vendorToCopy.getVendorDetailAssignedIdentifier() != null ? vendorToCopy.getVendorDetailAssignedIdentifier().toString() : StringUtils.EMPTY;

        paymentWorksSupplierUploadDTO.setVendorNum(vendorNumber);
        paymentWorksSupplierUploadDTO.setSiteCode(siteCode);
        paymentWorksSupplierUploadDTO.setSupplierName(vendorToCopy.getRequestingCompanyLegalName());
        paymentWorksSupplierUploadDTO.setSendToPaymentWorks(getPaymentWorksUtilityService().shouldVendorBeSentToPaymentWorks(vendorToCopy));

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
    public String createSupplierUploadFile(List<PaymentWorksSupplierUploadDTO> paymentWorksSupplierUploadList, String directoryPath) {
        String newFileName = directoryPath + File.separator + PaymentWorksConstants.SUPPLIER_FILE_NAME + buildFileExtensionWithDate(getDateTimeService().getCurrentDate());

        checkDirectory(directoryPath);

        BufferedWriter out = null;
        FileWriter fstream = null;
        try {
            fstream = new FileWriter(newFileName);
            out = new BufferedWriter(fstream);

            out.write(findSupplierUpLoadFileHeader());
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

    protected String findSupplierUpLoadFileHeader() {
        String header = getParameterService().getParameterValueAsString(
                PaymentWorksConstants.PAYMENT_WORKS_NAMESPACE_CODE, "PaymentWorksUploadSupplierService",
                PaymentWorksConstants.PaymentWorksParameters.SUPPLIER_UPLOAD_FILE_HEADER);
        LOG.info("findSupplierUpLoadFileHeader, the header is " + header);
        return header;
    }

    protected void writeDetailLineForEachUploadDTO(List<PaymentWorksSupplierUploadDTO> paymentWorksSupplierUploadList, BufferedWriter out) throws IOException {
        for (PaymentWorksSupplierUploadDTO supplier : paymentWorksSupplierUploadList) {
            if (supplier.isSendToPaymentWorks()) {
                out.write(supplier.toString());
                out.newLine();
            }

        }
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
    public void uploadNewVendorApprovedSupplierFile(SupplierUploadSummary supplierUploadSummary) {
        Collection<PaymentWorksVendor> approvedVendors = getPaymentWorksVendorService().getPaymentWorksVendorRecords(
                PaymentWorksConstants.ProcessStatus.VENDOR_APPROVED, null,
                PaymentWorksConstants.TransactionType.NEW_VENDOR);

        boolean uploaded = false;
        if (ObjectUtils.isNotNull(approvedVendors) && !approvedVendors.isEmpty()) {
            List<PaymentWorksSupplierUploadDTO> paymentWorksSupplierUploadList = createPaymentWorksSupplierUploadList(approvedVendors);

            uploaded = getPaymentWorksWebService().uploadSuppliers(paymentWorksSupplierUploadList);

            if (uploaded) {
                LOG.info("Supplier was uploaded!");
                for (PaymentWorksVendor newVendor : approvedVendors) {
                    String supplierStatusType = findSupplierStatusType(newVendor);
                    boolean updateStatus = !StringUtils.startsWith(newVendor.getVendorRequestId(), PaymentWorksConstants.VENDOR_REQUEST_ID_KFS_TEMP_ID_STARTER);

                    processVendor(newVendor, PaymentWorksConstants.PaymentWorksNewVendorStatus.PROCESSED,
                            PaymentWorksConstants.PaymentWorksStatusText.PROCESSED,
                            PaymentWorksConstants.ProcessStatus.SUPPLIER_UPLOADED, supplierStatusType, updateStatus,
                            supplierUploadSummary);
                }
            } else {
                LOG.error("uploadNewVendorApprovedSupplierFile, the vendors were not uploaded.");
            }
        }
    }

    protected String findSupplierStatusType(PaymentWorksVendor newVendor) {
        return !StringUtils.startsWith(newVendor.getVendorRequestId(), PaymentWorksConstants.VENDOR_REQUEST_ID_KFS_TEMP_ID_STARTER)
                        ? PaymentWorksConstants.SupplierUploadSummaryTypes.KFS_NEW_VENDORS
                        : PaymentWorksConstants.SupplierUploadSummaryTypes.PAYMENT_WORKS_NEW_VENDORS;
    }

    @Override
    public void updateNewVendorDisapprovedStatus(SupplierUploadSummary supplierUploadSummary) {
        Collection<PaymentWorksVendor> disapprovedVendors = getPaymentWorksVendorService().getPaymentWorksVendorRecords(
                PaymentWorksConstants.ProcessStatus.VENDOR_DISAPPROVED, PaymentWorksConstants.PaymentWorksStatusText.APPROVED,
                PaymentWorksConstants.TransactionType.NEW_VENDOR);

        for (PaymentWorksVendor newVendor : disapprovedVendors) {
            processVendor(newVendor, PaymentWorksConstants.PaymentWorksNewVendorStatus.REJECTED,
                    PaymentWorksConstants.PaymentWorksStatusText.REJECTED, PaymentWorksConstants.ProcessStatus.VENDOR_DISAPPROVED,
                    PaymentWorksConstants.SupplierUploadSummaryTypes.DISAPPROVED_VENDORS, true, supplierUploadSummary);
        }
    }

    @Override
    public void uploadVendorUpdateApprovedSupplierFile(SupplierUploadSummary supplierUploadSummary) {
        Collection<PaymentWorksVendor> approvedVendors = getPaymentWorksVendorService().getPaymentWorksVendorRecords(
                PaymentWorksConstants.ProcessStatus.VENDOR_APPROVED, null, PaymentWorksConstants.TransactionType.VENDOR_UPDATE);
        boolean uploaded = false;
        if (ObjectUtils.isNotNull(approvedVendors) && !approvedVendors.isEmpty()) {
            List<PaymentWorksSupplierUploadDTO> paymentWorksSupplierUploadList = createPaymentWorksSupplierUploadList(approvedVendors);
            uploaded = getPaymentWorksWebService().uploadSuppliers(paymentWorksSupplierUploadList);

            if (uploaded) {
                for (PaymentWorksVendor newVendor : approvedVendors) {
                    processVendor(newVendor, PaymentWorksConstants.PaymentWorksUpdateStatus.PROCESSED,
                            PaymentWorksConstants.PaymentWorksStatusText.PROCESSED, PaymentWorksConstants.ProcessStatus.SUPPLIER_UPLOADED,
                            PaymentWorksConstants.SupplierUploadSummaryTypes.VENDOR_UPDATES, false, supplierUploadSummary);
                }
            } else {
                LOG.error("uploadVendorUpdateApprovedSupplierFile, the vendors were not uploaded.");
            }
        }
    }

    protected void processVendor(PaymentWorksVendor paymentWorksNewVendor, String requestStatus,
            String requestStatusText, String processStatus, String supplierUploadSummaryType,
            boolean updatePaymentWorksStatus, SupplierUploadSummary supplierUploadSummary) {
        addSummaryLine(paymentWorksNewVendor, supplierUploadSummaryType, supplierUploadSummary);

        paymentWorksNewVendor.setRequestStatus(requestStatusText);
        paymentWorksNewVendor.setProcessStatus(processStatus);
        paymentWorksNewVendor = getPaymentWorksVendorService().updatePaymentWorksVendor(paymentWorksNewVendor);

        if (updatePaymentWorksStatus) {
            updatePaymentWorksVendorStatus(paymentWorksNewVendor.getVendorRequestId(), requestStatus);
        }
    }

    protected void addSummaryLine(PaymentWorksVendor paymentWorksVendor, String supplierUploadSummaryType, SupplierUploadSummary supplierUploadSummary) {
        SupplierUploadSummaryLine summaryLine = new SupplierUploadSummaryLine();
        summaryLine.setVendorRequestId(paymentWorksVendor.getVendorRequestId());
        summaryLine.setVendorName(paymentWorksVendor.getRequestingCompanyLegalName());
        summaryLine.setDocumentNumber(paymentWorksVendor.getDocumentNumber());
        summaryLine.setVendorNumber(paymentWorksVendor.getVendorHeaderGeneratedIdentifier() + KFSConstants.DASH + paymentWorksVendor.getVendorDetailAssignedIdentifier());
        boolean shouldBeSentToPaymentWorks = getPaymentWorksUtilityService().shouldVendorBeSentToPaymentWorks(paymentWorksVendor);
        summaryLine.setSendToPaymentWorks(shouldBeSentToPaymentWorks);

        if (StringUtils.equals(supplierUploadSummaryType, PaymentWorksConstants.SupplierUploadSummaryTypes.PAYMENT_WORKS_NEW_VENDORS)) {
            supplierUploadSummary.getPaymentWorksNewVendors().add(summaryLine);
        } else if (StringUtils.equals(supplierUploadSummaryType, PaymentWorksConstants.SupplierUploadSummaryTypes.KFS_NEW_VENDORS)) {
            supplierUploadSummary.getKfsNewVendors().add(summaryLine);
        } else if (StringUtils.equals(supplierUploadSummaryType, PaymentWorksConstants.SupplierUploadSummaryTypes.VENDOR_UPDATES)) {
            supplierUploadSummary.getVendorUpdates().add(summaryLine);
        } else if (StringUtils.equals(supplierUploadSummaryType, PaymentWorksConstants.SupplierUploadSummaryTypes.DISAPPROVED_VENDORS)) {
            supplierUploadSummary.getNewVendorDisapproved().add(summaryLine);
        }

        if (shouldBeSentToPaymentWorks) {
            supplierUploadSummary.getUploadedVendors().add(summaryLine);
        }
    }

    protected void updatePaymentWorksVendorStatus(String vendorRequestId, String requestStatus) {
        List<PaymentWorksNewVendorUpdateVendorStatus> updateNewVendorStatusList = new ArrayList<PaymentWorksNewVendorUpdateVendorStatus>();
        PaymentWorksNewVendorUpdateVendorStatus updateNewVendorStatus = new PaymentWorksNewVendorUpdateVendorStatus();
        updateNewVendorStatus.setId(new Integer(vendorRequestId));
        updateNewVendorStatus.setRequest_status(new Integer(requestStatus));
        updateNewVendorStatusList.add(updateNewVendorStatus);

        getPaymentWorksWebService().updateNewVendorStatusInPaymentWorks(updateNewVendorStatusList);
    }

    @Override
    public File writePaymentWorksSupplierUploadSummaryReport(SupplierUploadSummary supplierUploadSummary) {
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
        reportWriterService.writeStatisticLine("%d total transactions processed", paymentWorksNewVendors.size()
                + kfsNewVendors.size() + newVendorDisapproved.size() + vendorUpdates.size());

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

    public ReportWriterService getReportWriterService() {
        return reportWriterService;
    }

    public void setReportWriterService(ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
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

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public PaymentWorksUtilityService getPaymentWorksUtilityService() {
        return paymentWorksUtilityService;
    }

    public void setPaymentWorksUtilityService(PaymentWorksUtilityService paymentWorksUtilityService) {
        this.paymentWorksUtilityService = paymentWorksUtilityService;
    }

}
