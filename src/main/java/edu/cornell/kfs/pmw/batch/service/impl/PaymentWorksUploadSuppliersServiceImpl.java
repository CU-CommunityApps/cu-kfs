package edu.cornell.kfs.pmw.batch.service.impl;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.service.BusinessObjectService;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvException;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.PaymentWorksUploadFileColumn;
import edu.cornell.kfs.pmw.batch.PaymentWorksParameterConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksPropertiesConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.dataaccess.PaymentWorksVendorDao;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportVendorItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksUploadSuppliersBatchReportData;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksUploadSuppliersReportService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksUploadSuppliersService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceCallsService;
import edu.cornell.kfs.sys.util.EnumConfiguredMappingStrategy;

public class PaymentWorksUploadSuppliersServiceImpl implements PaymentWorksUploadSuppliersService {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksUploadSuppliersServiceImpl.class);

    private PaymentWorksUploadSuppliersReportService paymentWorksUploadSuppliersReportService;
    private PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    private PaymentWorksWebServiceCallsService paymentWorksWebServiceCallsService;
    private BusinessObjectService businessObjectService;
    private PaymentWorksVendorDao paymentWorksVendorDao;

    @Override
    public void uploadPreparedVendorsToPaymentWorks() {
        Collection<PaymentWorksVendor> vendorsToUpload = findPaymentWorksVendorsReadyForUpload();
        
        if (CollectionUtils.isEmpty(vendorsToUpload)) {
            LOG.info("uploadPreparedVendorsToPaymentWorks, did not find any vendors to upload back to PaymentWorks");
            sendEmailThatNoPreparedKfsVendorsWereFoundToUpload();
        } else {
            LOG.info("uploadPreparedVendorsToPaymentWorks, found " + vendorsToUpload.size() + " vendors to upload to PaymentWorks");
            performSupplierUploadProcessing(vendorsToUpload);
        }
    }

    private Collection<PaymentWorksVendor> findPaymentWorksVendorsReadyForUpload() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(PaymentWorksPropertiesConstants.PaymentWorksVendor.SUPPLIER_UPLOAD_STATUS,
                PaymentWorksConstants.SupplierUploadStatus.READY_FOR_UPLOAD);
        
        return businessObjectService.findMatching(PaymentWorksVendor.class, criteria);
    }

    private void sendEmailThatNoPreparedKfsVendorsWereFoundToUpload() {
        List<String> emailBodyItems = Collections.singletonList(paymentWorksBatchUtilityService.retrievePaymentWorksParameterValue(
                PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_NO_PREPARED_VENDORS_FOUND_EMAIL_BODY));
        List<String> emailSubjectItems = Collections.singletonList(paymentWorksBatchUtilityService.retrievePaymentWorksParameterValue(
                PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_NO_PREPARED_VENDORS_FOUND_EMAIL_SUBJECT));
        paymentWorksUploadSuppliersReportService.sendEmailThatNoDataWasFoundToProcess(emailSubjectItems, emailBodyItems);
    }

    private void performSupplierUploadProcessing(Collection<PaymentWorksVendor> vendors) {
        PaymentWorksUploadSuppliersBatchReportData reportData = new PaymentWorksUploadSuppliersBatchReportData();
        reportData.getRecordsFoundToProcessSummary().setRecordCount(vendors.size());
        boolean uploadSucceeded = uploadVendorsToPaymentWorks(vendors, reportData);
        updateVendorStatusesInKfs(vendors, uploadSucceeded, reportData);
        paymentWorksUploadSuppliersReportService.generateAndEmailProcessingReport(reportData);
    }

    protected boolean uploadVendorsToPaymentWorks(Collection<PaymentWorksVendor> vendors, PaymentWorksUploadSuppliersBatchReportData reportData) {
        InputStream vendorCsvDataStream = null;
        
        try {
            checkForPotentialVendorDeletionAttempts(vendors);
            byte[] vendorCsvData = generateVendorCsvDataForUpload(vendors);
            vendorCsvDataStream = new ByteArrayInputStream(vendorCsvData);
            int receivedVendorCount = paymentWorksWebServiceCallsService.uploadVendorsToPaymentWorks(vendorCsvDataStream);
            reportData.getRecordsProcessedByPaymentWorksSummary().setRecordCount(receivedVendorCount);
            if (receivedVendorCount != vendors.size()) {
                LOG.warn("uploadVendorsToPaymentWorks, " + vendors.size()
                        + " vendors were sent to PaymentWorks, but PaymentWorks reported that it received "
                        + receivedVendorCount + " vendors instead");
                reportData.getGlobalMessages().add(paymentWorksBatchUtilityService.retrievePaymentWorksParameterValue(
                        PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_VENDOR_COUNT_MISMATCH_MESSAGE));
            }
            return true;
        } catch (RuntimeException e) {
            LOG.error("uploadVendorsToPaymentWorks, error encountered when uploading vendors to PaymentWorks", e);
            reportData.getGlobalMessages().add(paymentWorksBatchUtilityService.retrievePaymentWorksParameterValue(
                    PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_VENDOR_UPLOAD_FAILURE_MESSAGE));
            return false;
        } finally {
            IOUtils.closeQuietly(vendorCsvDataStream);
        }
    }

    private void checkForPotentialVendorDeletionAttempts(Collection<PaymentWorksVendor> vendors) {
        boolean vendorDeletionAttemptExists = vendors.stream()
                .anyMatch((vendor) -> StringUtils.equalsIgnoreCase(
                        PaymentWorksConstants.SUPPLIER_UPLOAD_DELETE_INDICATOR, vendor.getRequestingCompanyLegalNameForProcessing()));
        if (vendorDeletionAttemptExists) {
            LOG.error("checkForPotentialVendorDeletionAttempts, discovered a potential attempt to delete a vendor via the supplier upload");
            throw new RuntimeException("Detected a potential attempt to delete a vendor via the supplier upload");
        }
    }

    private byte[] generateVendorCsvDataForUpload(Collection<PaymentWorksVendor> vendors) {
        ByteArrayOutputStream outputStream = null;
        OutputStreamWriter streamWriter = null;
        BufferedWriter bufferedWriter = null;
        
        try {
            outputStream = new ByteArrayOutputStream();
            streamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            bufferedWriter = new BufferedWriter(streamWriter);
            writeVendorsToCsvFormat(bufferedWriter, vendors);
            bufferedWriter.flush();
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(bufferedWriter);
            IOUtils.closeQuietly(streamWriter);
            IOUtils.closeQuietly(outputStream);
        }
        
        return outputStream.toByteArray();
    }

    private void writeVendorsToCsvFormat(Writer writer, Collection<PaymentWorksVendor> vendors) throws CsvException {
        EnumConfiguredMappingStrategy<PaymentWorksVendor, PaymentWorksUploadFileColumn> mappingStrategy
                = new EnumConfiguredMappingStrategy<>(PaymentWorksUploadFileColumn.class,
                        PaymentWorksUploadFileColumn::getHeaderLabel, PaymentWorksUploadFileColumn::getPmwVendorPropertyName);
        mappingStrategy.setType(PaymentWorksVendor.class);

        StatefulBeanToCsv<PaymentWorksVendor> csvWriter = new StatefulBeanToCsvBuilder<PaymentWorksVendor>(writer)
            .withMappingStrategy(mappingStrategy)
            .build();
        
        csvWriter.write(new ArrayList<>(vendors));
    }

    private void updateVendorStatusesInKfs(Collection<PaymentWorksVendor> vendors, boolean uploadSucceeded,
            PaymentWorksUploadSuppliersBatchReportData reportData) {
        try {
            List<Integer> ids = getVendorStagingIds(vendors);
            if (uploadSucceeded) {
                paymentWorksVendorDao.updateSupplierUploadStatusesForVendorsInStagingTable(ids,
                        PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.CONNECTED.getText(),
                        PaymentWorksConstants.SupplierUploadStatus.VENDOR_UPLOADED);
                generateAndAddVendorReportItems(reportData.getRecordsProcessed(), vendors);
                reportData.setUpdatedKfsAndPaymentWorksSuccessfully(true);
            } else {
                paymentWorksVendorDao.updateSupplierUploadStatusesForVendorsInStagingTable(ids,
                        PaymentWorksConstants.SupplierUploadStatus.UPLOAD_FAILED);
                generateAndAddVendorReportItems(reportData.getRecordsWithProcessingErrors(), vendors);
                reportData.setUpdatedKfsAndPaymentWorksSuccessfully(false);
            }
        } catch (RuntimeException e) {
            LOG.error("updateVendorStatusesInKfs, failed to update vendor statuses in KFS", e);
            reportData.getGlobalMessages().add(paymentWorksBatchUtilityService.retrievePaymentWorksParameterValue(
                    PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_VENDOR_KFS_UPDATE_FAILURE_MESSAGE));
            reportData.getRecordsProcessed().clear();
            reportData.getRecordsWithProcessingErrors().clear();
            generateAndAddVendorReportItems(reportData.getRecordsWithProcessingErrors(), vendors);
            reportData.setUpdatedKfsAndPaymentWorksSuccessfully(false);
        }
    }

    private List<Integer> getVendorStagingIds(Collection<PaymentWorksVendor> vendors) {
        return vendors.stream()
                .map(PaymentWorksVendor::getId)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void generateAndAddVendorReportItems(List<PaymentWorksBatchReportVendorItem> itemsList, Collection<PaymentWorksVendor> vendors) {
        vendors.stream()
                .map(paymentWorksUploadSuppliersReportService::createBatchReportVendorItem)
                .forEach(itemsList::add);
    }

    public void setPaymentWorksBatchUtilityService(PaymentWorksBatchUtilityService paymentWorksBatchUtilityService) {
        this.paymentWorksBatchUtilityService = paymentWorksBatchUtilityService;
    }

    public void setPaymentWorksUploadSuppliersReportService(PaymentWorksUploadSuppliersReportService paymentWorksUploadSuppliersReportService) {
        this.paymentWorksUploadSuppliersReportService = paymentWorksUploadSuppliersReportService;
    }

    public void setPaymentWorksWebServiceCallsService(PaymentWorksWebServiceCallsService paymentWorksWebServiceCallsService) {
        this.paymentWorksWebServiceCallsService = paymentWorksWebServiceCallsService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setPaymentWorksVendorDao(PaymentWorksVendorDao paymentWorksVendorDao) {
        this.paymentWorksVendorDao = paymentWorksVendorDao;
    }

}
