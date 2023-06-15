package edu.cornell.kfs.module.purap.batch.service.impl;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.FileStorageService;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerAddressTypeForXML;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerLegalStructure;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerUploadSuppliersProcessingMode;
import edu.cornell.kfs.module.purap.JaggaerConstants;
import edu.cornell.kfs.module.purap.batch.dataaccess.JaggaerUploadDao;
import edu.cornell.kfs.module.purap.batch.service.JaggaerGenerateSupplierXmlService;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.Address;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.AddressList;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.Authentication;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.Header;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.IsoCountryCode;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.JaggaerBasicValue;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.JaggaerBuilder;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.State;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.Supplier;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierRequestMessage;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierSyncMessage;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.ISOFIPSConversionService;
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
    protected JaggaerUploadDao jaggaerUploadDao;
    protected ISOFIPSConversionService isoFipsConversionService;

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

        List<VendorDetail> vendorDetails = jaggaerUploadDao.findVendors(processingMode, processingDate);

        for (VendorDetail detail : vendorDetails) {
            if (detail.isActiveIndicator()) {
                Supplier supplier = new Supplier();
                supplier.setErpNumber(JaggaerBuilder.buildERPNumber(detail.getVendorNumber()));
                supplier.setName(JaggaerBuilder.buildName(detail.getVendorName()));
                supplier.setCountryOfOrigin(buildCountryOfOrigin(detail));
                supplier.setActive(JaggaerBuilder.buildActive(getDefaultActiveValue()));
                supplier.setLegalStructure(buildJaggerLegalStructure(detail));
                supplier.setWebSiteURL(JaggaerBuilder.buildJaggaerBasicValue(detail.getVendorUrlAddress()));
                supplier.setAddressList(buildAddressList(detail));
    
                suppliers.add(supplier);
            }
        }

        return suppliers;
    }

    private JaggaerBasicValue buildCountryOfOrigin(VendorDetail detail) {
        String isoCountryCode = convertToISOCountry(detail.getVendorHeader().getVendorCorpCitizenCode());
        return JaggaerBuilder.buildJaggaerBasicValue(isoCountryCode);
    }

    private String convertToISOCountry(String fipsCountryCode) {
        if (StringUtils.isBlank(fipsCountryCode)) {
            LOG.debug("convertToISOCountry, empty FIPS country found returning US as default");
            fipsCountryCode = KFSConstants.COUNTRY_CODE_UNITED_STATES;
        }

        String isoCountry = StringUtils.EMPTY;

        try {
            isoCountry = isoFipsConversionService.convertFIPSCountryCodeToActiveISOCountryCode(fipsCountryCode);
        } catch (RuntimeException runtimeException) {
            LOG.error("convertToISOCountry, returning empty string, unable to get ISO country for FIPS country "
                    + fipsCountryCode, runtimeException);
        }

        return isoCountry;
    }

    private String getDefaultActiveValue() {
        /*
         * @todo before merging this code, this should pull from a parameter
         */
        return "No";
    }

    private JaggaerBasicValue buildJaggerLegalStructure(VendorDetail detail) {
        String kfsOwnerShipCode = detail.getVendorHeader().getVendorOwnershipCode();
        JaggaerLegalStructure legalStructure = JaggaerLegalStructure
                .findJaggaerLegalStructureByKFSOwnershipCode(kfsOwnerShipCode);
        return JaggaerBuilder.buildJaggaerBasicValue(legalStructure.jaggaerLegalStructureName);
    }

    private AddressList buildAddressList(VendorDetail detail) {
        AddressList addressList = new AddressList();

        for (VendorAddress vendorAddress : detail.getVendorAddresses()) {
            if (vendorAddress.isActive()) {
                Address jaggaerAddress = new Address();
                jaggaerAddress.setErpNumber(JaggaerBuilder
                        .buildERPNumber(String.valueOf(vendorAddress.getVendorAddressGeneratedIdentifier())));
                jaggaerAddress.setType(JaggaerAddressTypeForXML.findJaggaerAddressTypeForXMLByKfsAddressType(
                        vendorAddress.getVendorAddressTypeCode()).jaggaerAddressType);
                jaggaerAddress.setActive(JaggaerBuilder.buildActive(getDefaultAddressActiveValue()));
                jaggaerAddress.setIsoCountryCode(buildIsoCountry(vendorAddress.getVendorCountryCode()));
                jaggaerAddress.setAddressLine1(JaggaerBuilder.buildAddressLine(vendorAddress.getVendorLine1Address()));
                jaggaerAddress.setAddressLine2(JaggaerBuilder.buildAddressLine(vendorAddress.getVendorLine2Address()));
                jaggaerAddress.setCity(JaggaerBuilder.buildCity(vendorAddress.getVendorCityName()));
                jaggaerAddress.setState(buildState(vendorAddress));
                jaggaerAddress.setPostalCode(JaggaerBuilder.buildPostalCode(vendorAddress.getVendorZipCode()));
                jaggaerAddress.setNotes(buildAddressNote(vendorAddress));

                addressList.getAddresses().add(jaggaerAddress);
            }
        }
        return addressList;
    }

    private String getDefaultAddressActiveValue() {
        /*
         * @todo before merging this code, this should pull from a parameter
         */
        return "Yes";
    }

    private IsoCountryCode buildIsoCountry(String fipsCountryCode) {
        String isoCountry = convertToISOCountry(fipsCountryCode);
        return JaggaerBuilder.buildIsoCountryCode(isoCountry);
    }

    private State buildState(VendorAddress vendorAddress) {
        String stateToUse = StringUtils.isNotBlank(vendorAddress.getVendorStateCode())
                ? vendorAddress.getVendorStateCode()
                : vendorAddress.getVendorAddressInternationalProvinceName();
        return JaggaerBuilder.buildState(stateToUse);
    }

    private JaggaerBasicValue buildAddressNote(VendorAddress vendorAddress) {
        String addressTypeCode = vendorAddress.getVendorAddressTypeCode();
        String noteText = findAddressNoteTextStarter() + StringUtils.SPACE + addressTypeCode;
        return JaggaerBuilder.buildJaggaerBasicValue(noteText);
    }

    private String findAddressNoteTextStarter() {
        /*
         * @todo before merging this code, this should pull from a parameter
         */
        return "KFS Vendor Address Type is";
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
                supplierRequest.getSuppliers().addAll(supplierChunk);

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

    public void setJaggaerUploadDao(JaggaerUploadDao jaggaerUploadDao) {
        this.jaggaerUploadDao = jaggaerUploadDao;
    }

    public void setIsoFipsConversionService(ISOFIPSConversionService isoFipsConversionService) {
        this.isoFipsConversionService = isoFipsConversionService;
    }

}
