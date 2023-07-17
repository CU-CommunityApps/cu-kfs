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
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.FileStorageService;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerAddressTypeForXml;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerLegalStructure;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerUploadSuppliersProcessingMode;
import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.JaggaerConstants;
import edu.cornell.kfs.module.purap.batch.JaggaerGenerateSupplierXmlStep;
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
import edu.cornell.kfs.sys.exception.ManyFIPStoISOMappingException;
import edu.cornell.kfs.sys.exception.NoFIPStoISOMappingException;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.ISOFIPSConversionService;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import jakarta.xml.bind.JAXBException;

public class JaggaerGenerateSupplierXmlServiceImpl implements JaggaerGenerateSupplierXmlService {
    private static final Logger LOG = LogManager.getLogger();
    protected static final DateTimeFormatter DATE_FORMATTER_FOR_HEADER_DATE = DateTimeFormat
            .forPattern(CUKFSConstants.DATE_FORMAT_yyyy_MM_dd_T_HH_mm_ss_SSS_Z).withLocale(Locale.US).withZoneUTC();
    protected static final DateTimeFormatter DATE_FORMATTER_FOR_FILE_NAME = DateTimeFormat
            .forPattern(CUKFSConstants.DATE_FORMAT_yyyyMMdd_HHmmssSSS).withLocale(Locale.US);

    private String jaggaerXmlDirectory;

    protected DateTimeService dateTimeService;
    protected CUMarshalService cuMarshalService;
    protected FileStorageService fileStorageService;
    protected JaggaerUploadDao jaggaerUploadDao;
    protected ISOFIPSConversionService isoFipsConversionService;
    protected ParameterService parameterService;
    protected WebServiceCredentialService webServiceCredentialService;

    @Override
    public List<SupplierSyncMessage> getSupplierSyncMessages(JaggaerUploadSuppliersProcessingMode processingMode,
            Date processingDate, int maximumNumberOfSuppliersPerListItem) {
        List<Supplier> suppliers = getAllVendorsToUploadToJaggaer(processingMode, processingDate);
        LOG.info("getSupplierSyncMessages found {} suppliers.", suppliers.size());
        return buildSupplierSyncMessageList(suppliers, maximumNumberOfSuppliersPerListItem);
    }

    private List<Supplier> getAllVendorsToUploadToJaggaer(JaggaerUploadSuppliersProcessingMode processingMode,
            Date processingDate) {
        List<Supplier> suppliers = new ArrayList<>();

        List<VendorDetail> vendorDetails = jaggaerUploadDao.findVendors(processingMode, processingDate);
        int supplierCount = 0;
        for (VendorDetail detail : vendorDetails) {
            if (detail.isActiveIndicator()) {
                supplierCount++;
                if (supplierCount % 100 == 0) {
                    LOG.info("getAllVendorsToUploadToJaggaer, created supplier number " + supplierCount);
                }
                Supplier supplier = new Supplier();
                supplier.setErpNumber(JaggaerBuilder.buildErpNumber(detail.getVendorNumber()));
                supplier.setName(JaggaerBuilder.buildName(detail.getVendorName()));
                supplier.setCountryOfOrigin(buildCountryOfOrigin(detail));
                supplier.setActive(JaggaerBuilder.buildActive(getDefaultSupplierActiveValue()));
                supplier.setLegalStructure(buildJaggerLegalStructure(detail));
                supplier.setWebSiteURL(JaggaerBuilder.buildJaggaerBasicValue(detail.getVendorUrlAddress()));
                supplier.setAddressList(buildAddressList(detail));
    
                suppliers.add(supplier);
            }
        }

        return suppliers;
    }

    private JaggaerBasicValue buildCountryOfOrigin(VendorDetail detail) {
        String isoCountryCode = convertToValidCountryOfOrigin(detail.getVendorHeader().getVendorCorpCitizenCode());
        return JaggaerBuilder.buildJaggaerBasicValue(isoCountryCode);
    }
    
    private String convertToValidCountryOfOrigin(String countryCode) {
        if (StringUtils.isBlank(countryCode)) {
            LOG.debug("convertToValidCountryOfOrigin, empty countryCode received converting US as default");
            countryCode = KFSConstants.COUNTRY_CODE_UNITED_STATES;
        }
        
        String isoCountry = StringUtils.EMPTY;
        
        try {
            isoCountry = isoFipsConversionService.convertFIPSCountryCodeToActiveISOCountryCode(countryCode);
            
        } catch (NoFIPStoISOMappingException noFIPStoISOMappingException ) {
            LOG.error("convertToValidCountryOfOrigin, returning empty string, no valid mapping for FIPs country code "
                + countryCode, noFIPStoISOMappingException);
        } catch (ManyFIPStoISOMappingException manyFIPStoISOMappingException ) {
            LOG.error("convertToValidCountryOfOrigin, returning empty string, many ISO countries found for FIPS country code "
                    + countryCode, manyFIPStoISOMappingException);
        } catch (RuntimeException runtimeException) {
            LOG.error("convertToValidCountryOfOrigin, returning empty string, encountered a general error for fips country code "
                    + countryCode, runtimeException);
        }
        return isoCountry;
    }

    private String getDefaultSupplierActiveValue() {
        return getParameterValueString(CUPurapParameterConstants.JAGGAER_DEFAULT_SUPPLIER_ACTIVE_VALUE);
    }

    private JaggaerBasicValue buildJaggerLegalStructure(VendorDetail detail) {
        String kfsOwnerShipCode = detail.getVendorHeader().getVendorOwnershipCode();
        JaggaerLegalStructure legalStructure = JaggaerLegalStructure
                .findJaggaerLegalStructureByKfsOwnershipCode(kfsOwnerShipCode);
        return JaggaerBuilder.buildJaggaerBasicValue(legalStructure.jaggaerLegalStructureName);
    }

    private AddressList buildAddressList(VendorDetail detail) {
        AddressList addressList = new AddressList();

        for (VendorAddress vendorAddress : detail.getVendorAddresses()) {
            if (vendorAddress.isActive()) {
                Address jaggaerAddress = new Address();
                jaggaerAddress.setErpNumber(JaggaerBuilder
                        .buildErpNumber(String.valueOf(vendorAddress.getVendorAddressGeneratedIdentifier())));
                jaggaerAddress.setType(JaggaerAddressTypeForXml.findJaggaerAddressTypeForXmlByKfsAddressType(
                        vendorAddress.getVendorAddressTypeCode()).jaggaerAddressType);
                jaggaerAddress.setActive(JaggaerBuilder.buildActive(getDefaultSupplierAddressActiveValue()));
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

    private String getDefaultSupplierAddressActiveValue() {
        return getParameterValueString(CUPurapParameterConstants.JAGGAER_DEFAULT_SUPPLIER_ADDRESS_ACTIVE_VALUE);
    }

    private IsoCountryCode buildIsoCountry(String fipsCountryCode) {
        String isoCountry = convertToValidCountryOfOrigin(fipsCountryCode);
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
        return getParameterValueString(CUPurapParameterConstants.JAGGAER_DEFAULT_SUPPLIER_ADDRESS_NOTE_TEXT);
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

                message.getSupplierSyncMessageItems().add(supplierRequest);
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

    private Authentication buildAuthentication() {
        Authentication auth = new Authentication();
        auth.setIdentity(getWebserviceCredentialValue(CUPurapParameterConstants.JAGGAER_WEBSERVICE_UPLOAD_SUPPLIER_NAME));
        auth.setSharedSecret(getWebserviceCredentialValue(CUPurapParameterConstants.JAGGAER_WEBSERVICE_UPLOAD_SUPPLIER_PASSWORD));
        return auth;
    }
    
    private String getWebserviceCredentialValue(String key) {
        return webServiceCredentialService.getWebServiceCredentialValue(CUPurapParameterConstants.JAGGAER_WEBSERVICE_GROUP_CODE, key);
    }

    @Override
    public void generateXMLForSyncMessages(List<SupplierSyncMessage> messages) {
        for (SupplierSyncMessage message : messages) {
            String outputFileName = jaggaerXmlDirectory + findOutputFileNameStarter()
                    + DATE_FORMATTER_FOR_FILE_NAME.print(dateTimeService.getCurrentDate().getTime())
                    + CUKFSConstants.XML_FILE_EXTENSION;
            try {
                LOG.info("generateXMLForSyncMessages, created XML file {}", outputFileName);
                cuMarshalService.marshalObjectToXMLFragment(message, outputFileName);
                fileStorageService.createDoneFile(outputFileName);
            } catch (JAXBException | IOException e) {
                LOG.error("generateXMLForSyncMessages, unable to create {} output file", outputFileName, e);
                throw new RuntimeException(e);
            }
        }
    }
    
    private String findOutputFileNameStarter() {
        return getParameterValueString(CUPurapParameterConstants.JAGGAER_DEFAULT_SUPPLIER_OUTPUT_FILE_NAME_STARTER);
    }
    
    protected String getParameterValueString(String parameterName) {
        return parameterService.getParameterValueAsString(JaggaerGenerateSupplierXmlStep.class, parameterName);
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

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setWebServiceCredentialService(WebServiceCredentialService webServiceCredentialService) {
        this.webServiceCredentialService = webServiceCredentialService;
    }

}
