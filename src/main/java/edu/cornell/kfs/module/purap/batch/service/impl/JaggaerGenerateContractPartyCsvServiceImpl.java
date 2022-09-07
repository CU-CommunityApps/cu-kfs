package edu.cornell.kfs.module.purap.batch.service.impl;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;

import com.opencsv.CSVWriter;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerAddressHeader;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContactHeader;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractUploadProcessingMode;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerPartyHeader;
import edu.cornell.kfs.module.purap.batch.dataaccess.JaggaerUploadDao;
import edu.cornell.kfs.module.purap.batch.service.JaggaerGenerateContractPartyCsvService;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractAddressUploadDto;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractPartyUploadDto;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractUploadBaseDto;
import edu.cornell.kfs.sys.CUKFSConstants;

public class JaggaerGenerateContractPartyCsvServiceImpl implements JaggaerGenerateContractPartyCsvService {
    private static final Logger LOG = LogManager.getLogger();
    
    private String jaggaerUploadCreationDirectory;
    private JaggaerUploadDao jaggaerUploadDao;
    private DateTimeService dateTimeService;

    @Override
    public List<JaggaerContractUploadBaseDto> getJaggerContractsDto(JaggaerContractUploadProcessingMode processingMode, String processingDate) {
        List<JaggaerContractPartyUploadDto> vendors = jaggaerUploadDao.findJaggaerContractParty(processingMode, processingDate);
        int numberOfVendors = vendors.size();
        List<JaggaerContractAddressUploadDto> addresses = jaggaerUploadDao.findJaggaerContractAddress(processingMode, processingDate);
        int numberOfAddresses = addresses.size();
        
        List<JaggaerContractUploadBaseDto> uploadDtos = new ArrayList<>();
        
        for (JaggaerContractPartyUploadDto vendor : vendors) {
            uploadDtos.add(vendor);
            List<JaggaerContractAddressUploadDto> vendorAddresses = findVendorAddresses(addresses, vendor.getERPNumber());
            if (CollectionUtils.isNotEmpty(vendorAddresses)) {
                for (JaggaerContractAddressUploadDto vendorAddress : vendorAddresses) {
                    vendorAddress.setName(vendor.getContractPartyName());
                    uploadDtos.add(vendorAddress);
                }
            } else {
                LOG.warn("getJaggerContractsDto, no address for vendor " + vendor.getERPNumber());
            }
        }
        int totalNumberOfDetails = uploadDtos.size();
        LOG.info("getJaggerContractsDto, numberOfVendors: " + numberOfVendors + ",  numberOfAddresses: " + numberOfAddresses + ", totalNumberOfDetails: " + totalNumberOfDetails);
        if (totalNumberOfDetails != numberOfVendors + numberOfAddresses) {
            LOG.warn("getJaggerContractsDto, ALERT!, the total number of details is not the sum of number of vendors and number of addresses");
        }
        return uploadDtos;
    }
    
    protected List<JaggaerContractAddressUploadDto> findVendorAddresses(List<JaggaerContractAddressUploadDto> allAddresses, String erpNumber) {
        return allAddresses.stream()
                .filter(address -> StringUtils.equalsIgnoreCase(address.getERPNumber(), erpNumber))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void generateCsvFile(List<JaggaerContractUploadBaseDto> jaggaerUploadDtos, JaggaerContractUploadProcessingMode processingMode) {
        String csvFileName = generateCsvOutputFileName(processingMode);
        String fullyQualifiedCreationDirectoryFileName = fullyQualifyFileNameToCreationDirectory(csvFileName);
        LOG.info("generateCsvFile: fullyQualifiedOutputFile = " + fullyQualifiedCreationDirectoryFileName);
        
        List<String[]> csvData = buildDataToPrint(jaggaerUploadDtos);

        try (FileWriter fileWriter = new FileWriter(fullyQualifiedCreationDirectoryFileName, StandardCharsets.UTF_8);
                CSVWriter writer = new CSVWriter(fileWriter);) {
            writer.writeAll(csvData);
        } catch (IOException e) {
            LOG.error("generateCsvFile, Error writing to file " + fullyQualifiedCreationDirectoryFileName, e);
            throw new RuntimeException(e);
        }

    }
    
    private String generateCsvOutputFileName(JaggaerContractUploadProcessingMode processingMode) {
        SimpleDateFormat sdf = new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_yyyyMMdd_HHmmss, Locale.US);
        StringBuilder filename = new StringBuilder(processingMode.csvFileName);
        filename.append("_").append(sdf.format(dateTimeService.getCurrentDate())).append(CUKFSConstants.FILE_EXTENSIONS.CSV_FILE_EXTENSION);
        return filename.toString();
    }
    
    private String fullyQualifyFileNameToCreationDirectory(String csvFileName) {
        StringBuilder filename = new StringBuilder(jaggaerUploadCreationDirectory);
        filename.append(csvFileName);
        return filename.toString();
    }
    
    private List<String[]> buildDataToPrint(List<JaggaerContractUploadBaseDto> jaggaerUploadDtos) {
        List<String[]> csvDataRows = new ArrayList<>();
        addHeaderRows(csvDataRows);
        
        for (JaggaerContractUploadBaseDto dto : jaggaerUploadDtos) {
            if (dto instanceof JaggaerContractPartyUploadDto) {
                JaggaerContractPartyUploadDto vendorDto = (JaggaerContractPartyUploadDto) dto;
                csvDataRows.add(builderVendorCSVRowArray(vendorDto));
            } else if (dto instanceof JaggaerContractAddressUploadDto) {
                JaggaerContractAddressUploadDto addressDto = (JaggaerContractAddressUploadDto) dto;
                csvDataRows.add(builderAddressCSVRowArray(addressDto));
            } else {
                throw new IllegalArgumentException("Unexpected DTO class found: " + dto.getClass());
            }
        }
        return csvDataRows;
    }
    
    protected void addHeaderRows(List<String[]> csvDataRows) {
        csvDataRows.add(buildPartyHeader());
        csvDataRows.add(buildAddressHeader());
        csvDataRows.add(buildContactHeader());
    }
    
    protected String[] buildPartyHeader() {
        return Arrays.stream(JaggaerPartyHeader.values())
                .map(JaggaerPartyHeader::getHeaderName)
                .toArray(String[]::new);
    }
    
    protected String[] buildAddressHeader() {
        return Arrays.stream(JaggaerAddressHeader.values())
                .map(JaggaerAddressHeader::getHeaderName)
                .toArray(String[]::new);
    }
    
    protected String[] buildContactHeader() {
        return Arrays.stream(JaggaerContactHeader.values())
                .map(JaggaerContactHeader::getHeaderName)
                .toArray(String[]::new);
    }

    protected String[] builderVendorCSVRowArray(JaggaerContractPartyUploadDto vendorDto) {
        String[] record = { vendorDto.getRowType().rowType, 
                cleanString(vendorDto.getOverrideDupError()),
                cleanString(vendorDto.getERPNumber()),
                cleanString(vendorDto.getSciQuestID()), 
                cleanString(vendorDto.getContractPartyName()),
                cleanString(vendorDto.getDoingBusinessAs()),
                cleanString(vendorDto.getOtherNames()),
                cleanString(vendorDto.getCountryOfOrigin()), 
                cleanString(vendorDto.getActive()),
                vendorDto.getContractPartyType().partyTypeName, 
                cleanString(vendorDto.getPrimary()),
                vendorDto.getLegalStructure().jaggaerLegalStructureName,
                cleanString(vendorDto.getTaxIDType()),
                cleanString(vendorDto.getTaxIdentificationNumber()),
                cleanString(vendorDto.getVATRegistrationNumber()),
                cleanString(vendorDto.getWebsiteURL()) };
        return record;
    }
    
    protected String[] builderAddressCSVRowArray(JaggaerContractAddressUploadDto addressDto) {
        String[] record = { addressDto.getRowType().rowType, 
                cleanString(addressDto.getAddressID()),
                cleanString(addressDto.getSciQuestID()),
                cleanString(addressDto.getName()),
                addressDto.getAddressType().jaggaerAddressType, 
                cleanString(addressDto.getPrimaryType()),
                cleanString(addressDto.getActive()),
                cleanString(addressDto.getCountry()), 
                cleanString(addressDto.getStreetLine1()), 
                cleanString(addressDto.getStreetLine2()),
                cleanString(addressDto.getStreetLine3()),
                cleanString(addressDto.getCity()),
                cleanString(addressDto.getState()), 
                cleanString(addressDto.getPostalCode()), 
                cleanString(addressDto.getPhone()),
                cleanString(addressDto.getTollFreeNumber()), 
                cleanString(addressDto.getFax()),
                cleanString(addressDto.getNotes()) };
        return record;
    }
    
    private String cleanString(String stringValue) {
        return StringUtils.isBlank(stringValue) ? StringUtils.EMPTY : stringValue;
    }

    public void setJaggaerUploadCreationDirectory(String jaggaerUploadCreationDirectory) {
        this.jaggaerUploadCreationDirectory = jaggaerUploadCreationDirectory;
    }

    public void setJaggaerUploadDao(JaggaerUploadDao jaggaerUploadDao) {
        this.jaggaerUploadDao = jaggaerUploadDao;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
