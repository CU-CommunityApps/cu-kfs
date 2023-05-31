package edu.cornell.kfs.module.purap.batch.service.impl;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.sys.service.FileStorageService;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerUploadSuppliersProcessingMode;
import edu.cornell.kfs.module.purap.JaggaerConstants;
import edu.cornell.kfs.module.purap.batch.service.JaggaerGenerateSupplierXmlService;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.Address;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.AddressList;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.Authentication;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.Header;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.JaggaerBuilder;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.Supplier;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierRequestMessage;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierSyncMessage;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.CUMarshalService;
import jakarta.xml.bind.JAXBException;

public class JaggaerGenerateSupplierXmlServiceImpl implements JaggaerGenerateSupplierXmlService {
    private static final Logger LOG = LogManager.getLogger();
    protected static final DateTimeFormatter DATE_FORMATTER_FOR_HEADER_DATE = DateTimeFormat
            .forPattern(CUKFSConstants.DATE_FORMAT_yyyy_MM_dd_T_HH_mm_ss_SSS_Z).withLocale(Locale.US).withZoneUTC();
    protected static final DateTimeFormatter DATE_FORMATTER_FOR_FILE_NAME = DateTimeFormat
            .forPattern(CUKFSConstants.DATE_FORMAT_yyyyMMdd_HHmmssSSS).withLocale(Locale.US);

    private static final String JAGGAER_UPLOAD_FILE_NAME = "jaggaerSupplierUploadFile_";

    private String jaggaerXmlDirectory;

    protected DateTimeService dateTimeService;
    protected CUMarshalService cuMarshalService;
    protected FileStorageService fileStorageService;

    @Override
    public List<SupplierSyncMessage> getSupplierSyncMessages(JaggaerUploadSuppliersProcessingMode processingMode,
            Date processingDate, int maximumNumberOfSuppliersPerListItem) {
        List<Supplier> suppliers = getAllVendorsToUploadToJaggaer(processingMode, processingDate);
        LOG.info("getJaggaerContractsDto found {} suppliers.", suppliers.size());
        return buildSupplierSyncMessageList(suppliers, maximumNumberOfSuppliersPerListItem);
    }

    private List<Supplier> getAllVendorsToUploadToJaggaer(JaggaerUploadSuppliersProcessingMode processingMode,
            Date processingDate) {
        List<Supplier> suppliers = new ArrayList<>();

        /*
         * @todo fully implement this function in KFSPTS-28266
         * 
         * for now just create some testing data
         */

        suppliers.add(buildTestSupplier("13456-0", "Acme test company", "123 main street", "321 foo lane"));
        suppliers.add(buildTestSupplier("13654-0", "foo test comapany", "789 main street", "456 foo lane"));
        suppliers.add(buildTestSupplier("13456-1", "Acme test company part 2", "951 main street", "753 foo lane"));

        return suppliers;
    }

    private Supplier buildTestSupplier(String erpNumber, String name, String addressLine1, String addressLine2) {
        Supplier supplier = new Supplier();
        supplier.setActive(JaggaerBuilder.buildActive(JaggaerConstants.YES));
        supplier.setErpNumber(JaggaerBuilder.buildERPNumber(erpNumber));
        supplier.setName(JaggaerBuilder.buildName(name));
        supplier.setAddressList(new AddressList());
        supplier.getAddressList().getAddressDetails().add(buildTestAddress(addressLine1));
        supplier.getAddressList().getAddressDetails().add(buildTestAddress(addressLine2));
        return supplier;
    }

    private Address buildTestAddress(String addressLine) {
        Address address = new Address();
        address.setActive(JaggaerBuilder.buildActive(JaggaerConstants.YES));
        address.setAddressLine1(JaggaerBuilder.buildAddressLine(addressLine));
        return address;
    }

    protected List<SupplierSyncMessage> buildSupplierSyncMessageList(List<Supplier> suppliers,
            int maximumNumberOfSuppliersPerListItem) {
        List<SupplierSyncMessage> messages = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(suppliers)) {
            for (List<Supplier> supplierChunk : ListUtils.partition(suppliers, maximumNumberOfSuppliersPerListItem)) {
                SupplierSyncMessage message = new SupplierSyncMessage();
                message.setVersion(JaggaerConstants.XML_VERSION);
                message.setHeader(buildHeader());
                SupplierRequestMessage supplierRequest = new SupplierRequestMessage();
                supplierRequest.getSupplierDetails().addAll(supplierChunk);

                message.getSupplierRequestMessageItems().add(supplierRequest);
                messages.add(message);
            }
        }
        LOG.info("buildSupplierSyncMessageList, found {} suppliers, and returning {} SupplierSyncMessages", suppliers.size(), messages.size());
        return messages;
    }

    private Header buildHeader() {
        Header header = new Header();
        header.setMessageId(UUID.randomUUID().toString());
        header.setTimestamp(DATE_FORMATTER_FOR_HEADER_DATE.print(dateTimeService.getCurrentDate().getTime()));
        header.setAuthentication(buildAuthentication());
        return header;
    }

    /*
     * @todo implement this with real values after we get credential details from
     * Jaggaer on KFSPTS-28268 if not sooner in the project
     */
    private Authentication buildAuthentication() {
        Authentication auth = new Authentication();
        auth.setIdentity("CU - Identity");
        auth.setSharedSecret("CU - Share Secret");
        return auth;
    }

    @Override
    public void generateXMLForSyncMessages(List<SupplierSyncMessage> messages) {
        for (SupplierSyncMessage message : messages) {
            String outputFileName = jaggaerXmlDirectory + JAGGAER_UPLOAD_FILE_NAME
                    + DATE_FORMATTER_FOR_FILE_NAME.print(dateTimeService.getCurrentDate().getTime())
                    + CUKFSConstants.XML_FILE_EXTENSION;
            try {
                LOG.info("generateXMLForSyncMessages, created XML file {}", outputFileName);
                cuMarshalService.marshalObjectToXML(message, outputFileName);
                fileStorageService.createDoneFile(outputFileName);
            } catch (JAXBException | IOException e) {
                LOG.error("generateXMLForSyncMessages, unable to create {} output file", outputFileName, e);
                throw new RuntimeException(e);
            }
        }
    }

    public void setJaggaerXmlDirectory(String jaggaerXmlDirectory) {
        this.jaggaerXmlDirectory = jaggaerXmlDirectory;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setCuMarshalService(CUMarshalService cuMarshalService) {
        this.cuMarshalService = cuMarshalService;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

}
