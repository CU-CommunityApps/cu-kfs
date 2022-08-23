package edu.cornell.kfs.module.purap.batch.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractUploadProcessingMode;
import edu.cornell.kfs.module.purap.batch.dataaccess.JaggaerUploadDao;
import edu.cornell.kfs.module.purap.batch.service.JaggaerGenerateContractPartyCsvService;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractAddressUploadDto;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractPartyUploadDto;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractUploadBaseDto;

public class JaggaerGenerateContractPartyCsvServiceImpl implements JaggaerGenerateContractPartyCsvService {
    private static final Logger LOG = LogManager.getLogger();
    
    protected JaggaerUploadDao jaggaerUploadDao;

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
    public void generateCsvFile(List<JaggaerContractUploadBaseDto> jaggaerUploadDtos) {
        LOG.warn("generateCsvFile, not implemented yet");
        
        for (JaggaerContractUploadBaseDto dto : jaggaerUploadDtos) {
            LOG.info("generateCsvFile, dto: " + dto.toString());
        }
    }

    public void setJaggaerUploadDao(JaggaerUploadDao jaggaerUploadDao) {
        this.jaggaerUploadDao = jaggaerUploadDao;
    }

}
