package edu.cornell.kfs.module.purap.batch.service.impl;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;

import com.opencsv.CSVWriter;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractUploadProcessingMode;
import edu.cornell.kfs.module.purap.batch.dataaccess.JaggaerUploadDao;
import edu.cornell.kfs.module.purap.batch.service.JaggaerGenerateContractPartyCsvService;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractAddressUploadDto;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractPartyUploadDto;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractUploadBaseDto;
import edu.cornell.kfs.sys.CUKFSConstants;

public class JaggaerGenerateContractPartyCsvServiceImpl implements JaggaerGenerateContractPartyCsvService {
    private static final Logger LOG = LogManager.getLogger();
    
    private String jaggaerUploadCreationDriectory;
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
        LOG.warn("generateCsvFile, not implemented yet");
        
        String csvFileName = generateCsvOutputFileName(processingMode);
        String fullyQualifiedCreationDirectoryFileName = fullyQualifyFileNameToCreationDirectory(csvFileName);
        LOG.info("generateCsvFile: fullyQualifiedOutputFile = " + fullyQualifiedCreationDirectoryFileName);
        
        List<String[]> csvData = buildDataToPrint(jaggaerUploadDtos);

        try (FileWriter fileWriter = new FileWriter(fullyQualifiedCreationDirectoryFileName);
                CSVWriter writer = new CSVWriter(fileWriter);) {
            writer.writeAll(csvData);
        } catch (IOException e) {
            LOG.error("generateCsvFile, Error writing to file " + fullyQualifiedCreationDirectoryFileName, e);
            throw new RuntimeException(e);
        }

    }
    
    private String generateCsvOutputFileName(JaggaerContractUploadProcessingMode processingMode) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        StringBuilder filename = new StringBuilder(processingMode.csvFileName);
        filename.append("_").append(sdf.format(dateTimeService.getCurrentDate())).append(CUKFSConstants.FILE_EXTENSIONS.CSV_FILE_EXTENSION);
        return filename.toString();
    }
    
    private String fullyQualifyFileNameToCreationDirectory(String csvFileName) {
        StringBuilder filename = new StringBuilder(jaggaerUploadCreationDriectory);
        filename.append(csvFileName);
        return filename.toString();
    }
    
    private List<String[]> buildDataToPrint(List<JaggaerContractUploadBaseDto> jaggaerUploadDtos) {
        List<String[]> list = new ArrayList<>();
        
        for (JaggaerContractUploadBaseDto dto : jaggaerUploadDtos) {
            if (dto instanceof JaggaerContractPartyUploadDto) {
                JaggaerContractPartyUploadDto vendorDto = (JaggaerContractPartyUploadDto) dto;
                list.add(builderVendorCSVRowArray(vendorDto));
            } else if (dto instanceof JaggaerContractAddressUploadDto) {
                JaggaerContractAddressUploadDto addressDto = (JaggaerContractAddressUploadDto) dto;
                list.add(builderAddressCSVRowArray(addressDto));
            } else {
                throw new IllegalArgumentException("Unexpected DTO class found: " + dto.getClass());
            }
        }
        return list;
    }

    public String[] builderVendorCSVRowArray(JaggaerContractPartyUploadDto vendorDto) {
        String[] record = { vendorDto.getRowTypeDescription(), String.valueOf(vendorDto.isOverrideDupError()),
                vendorDto.getERPNumber(), vendorDto.getSciQuestID(), vendorDto.getContractPartyName(),
                vendorDto.getDoingBusinessAs(), vendorDto.getCountryOfOrigin(), vendorDto.getActive(),
                vendorDto.getContractPartyTypeName(), vendorDto.getPrimary(), vendorDto.getLegalStructureName(),
                vendorDto.getTaxIDType(), vendorDto.getTaxIdentificationNumber(), vendorDto.getVATRegistrationNumber(),
                vendorDto.getWebsiteURL() };
        return record;
    }
    
    public String[] builderAddressCSVRowArray(JaggaerContractAddressUploadDto addressDto) {
        String[] record = { addressDto.getRowTypeDescription(), addressDto.getAddressID(), addressDto.getSciQuestID(),
                addressDto.getName(), addressDto.getAddressTypeName(), addressDto.getPrimaryType(), addressDto.getActive(),
                addressDto.getCountry(), addressDto.getStreetLine1(), addressDto.getStreetLine2(), addressDto.getStreetLine3(),
                addressDto.getCity(), addressDto.getState(), addressDto.getPostalCode(), addressDto.getPhone(),
                addressDto.getTollFreeNumber(), addressDto.getFax(), addressDto.getNotes() };
        return record;
    }

    public void setJaggaerUploadCreationDriectory(String jaggaerUploadCreationDriectory) {
        this.jaggaerUploadCreationDriectory = jaggaerUploadCreationDriectory;
    }

    public void setJaggaerUploadDao(JaggaerUploadDao jaggaerUploadDao) {
        this.jaggaerUploadDao = jaggaerUploadDao;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
