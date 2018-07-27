package edu.cornell.kfs.pmw.batch.service.impl;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.kuali.kfs.krad.service.BusinessObjectService;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvException;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.PaymentWorksUploadFileColumn;
import edu.cornell.kfs.pmw.batch.PaymentWorksPropertiesConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.dataaccess.PaymentWorksVendorDao;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksNewVendorUploadSuppliersService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceCallsService;
import edu.cornell.kfs.sys.util.EnumConfiguredMappingStrategy;

public class PaymentWorksNewVendorUploadSuppliersServiceImpl implements PaymentWorksNewVendorUploadSuppliersService {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksNewVendorUploadSuppliersServiceImpl.class);

    private PaymentWorksWebServiceCallsService paymentWorksWebServiceCallsService;
    private BusinessObjectService businessObjectService;
    private PaymentWorksVendorDao paymentWorksVendorDao;

    @Override
    public void uploadPreparedVendorsToPaymentWorks() {
        Collection<PaymentWorksVendor> vendorsToUpload = findPaymentWorksVendorsReadyForUpload();
        if (CollectionUtils.isEmpty(vendorsToUpload)) {
            LOG.info("uploadPreparedVendorsToPaymentWorks, did not find any vendors to upload back to PaymentWorks");
        } else {
            LOG.info("uploadPreparedVendorsToPaymentWorks, found " + vendorsToUpload.size() + " vendors to upload to PaymentWorks");
            uploadVendorsToPaymentWorks(vendorsToUpload);
        }
    }

    private Collection<PaymentWorksVendor> findPaymentWorksVendorsReadyForUpload() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(PaymentWorksPropertiesConstants.PaymentWorksVendor.SUPPLIER_UPLOAD_STATUS,
                PaymentWorksConstants.SupplierUploadStatus.READY_FOR_UPLOAD);
        
        return businessObjectService.findMatching(PaymentWorksVendor.class, criteria);
    }

    private void uploadVendorsToPaymentWorks(Collection<PaymentWorksVendor> vendors) {
        try {
            byte[] vendorCsvData = generateVendorCsvDataForUpload(vendors);
            int receivedVendorCount = paymentWorksWebServiceCallsService.uploadVendorsToPaymentWorks(vendorCsvData);
            if (receivedVendorCount != vendors.size()) {
                LOG.warn("uploadVendorsToPaymentWorks, " + vendors.size()
                        + " vendors were sent to PaymentWorks, but PaymentWorks reported that it received "
                        + receivedVendorCount + " vendors instead");
            }
        } catch (RuntimeException e) {
            LOG.error("uploadVendorsToPaymentWorks, error encountered when uploading vendors to PaymentWorks", e);
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

        StatefulBeanToCsv<PaymentWorksVendor> csvWriter = new StatefulBeanToCsvBuilder<PaymentWorksVendor>(writer)
            .withMappingStrategy(mappingStrategy)
            .build();
        
        csvWriter.write(new ArrayList<>(vendors));
    }

    private void updateVendorStatusesInKfs(PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType pmwStatus, String uploadStatus) {
        try {
            // TODO: Fill in!
        } catch (RuntimeException e) {
            LOG.error("updateVendorStatusesInKfs, failed to update vendor statuses in KFS", e);
        }
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
