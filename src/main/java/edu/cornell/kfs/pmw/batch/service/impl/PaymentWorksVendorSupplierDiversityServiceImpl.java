package edu.cornell.kfs.pmw.batch.service.impl;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorSupplierDiversity;
import org.kuali.kfs.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.pmw.batch.businessobject.KfsToPMWSupplierDiversityDTO;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.dataaccess.KfsSupplierDiversityDao;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksVendorSupplierDiversityService;

public class PaymentWorksVendorSupplierDiversityServiceImpl implements PaymentWorksVendorSupplierDiversityService {
    private static final Logger LOG = LogManager.getLogger(PaymentWorksVendorSupplierDiversityServiceImpl.class);
    
    protected KfsSupplierDiversityDao kfsSupplierDiversityDao;
    protected PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    protected ConfigurationService configurationService;

    @Override
    public List<VendorSupplierDiversity> buildSupplierDiversityListFromPaymentWorksVendor(PaymentWorksVendor pmwVendor) {
        List<VendorSupplierDiversity> diversities = new ArrayList<VendorSupplierDiversity>();
        List<String> vendorDiversityCodesAlreadyAdded = new ArrayList<String>();
        diversities.addAll(buildDiversityListFromClassifications(pmwVendor.getFederalDiversityClassifications(), 
                kfsSupplierDiversityDao.buildPmwToKfsFederalSupplierDiversityListForForeignForm(), 
                vendorDiversityCodesAlreadyAdded));
        diversities.addAll(buildDiversityListFromClassifications(pmwVendor.getStateDiversityClassifications(), 
                kfsSupplierDiversityDao.buildPmwToKfsNewYorkSupplierDiversityListForForeignForm(), 
                vendorDiversityCodesAlreadyAdded));
        return diversities;
    }
    
    protected List<VendorSupplierDiversity> buildDiversityListFromClassifications(String classifications, List<KfsToPMWSupplierDiversityDTO> diversityMap, 
            List<String> vendorDiversityCodesAlreadyAdded) {
        List<VendorSupplierDiversity> diversities = new ArrayList<VendorSupplierDiversity>();
        if (StringUtils.isNotBlank(classifications)) {
            for (String diversityClass : StringUtils.split(classifications, KFSConstants.COMMA)) {
                List<KfsToPMWSupplierDiversityDTO> diversityList = findSupplierDiversityInMap(StringUtils.trim(diversityClass), diversityMap);
                if (CollectionUtils.isNotEmpty(diversityList)) {
                    for (KfsToPMWSupplierDiversityDTO dto : diversityList) {
                        if (!vendorDiversityCodesAlreadyAdded.contains(dto.getKfsSupplierDiversityCode())) {
                            vendorDiversityCodesAlreadyAdded.add(dto.getKfsSupplierDiversityCode());
                            diversities.add(buildVendorSupplierDiversity(dto.getKfsSupplierDiversityCode()));
                        }
                    }
                } else {
                    LOG.error("buildDiversityListFromClassifications, no active supplier diversity for classification " + diversityClass);
                }
            }
        }
        return diversities;
    }
    
    protected List<KfsToPMWSupplierDiversityDTO> findSupplierDiversityInMap(String classification, List<KfsToPMWSupplierDiversityDTO> diversityList) {
        return diversityList.stream()
                .filter(dto -> StringUtils.equalsIgnoreCase(classification, dto.getPaymentWorksSupplierDiversityDescription()))
                .collect(Collectors.toList());
    }
    
    protected VendorSupplierDiversity buildVendorSupplierDiversity(String vendorSupplierDiversityCode) {
        VendorSupplierDiversity vendorSupplierDiversity = new VendorSupplierDiversity();
        vendorSupplierDiversity.setVendorSupplierDiversityCode(vendorSupplierDiversityCode);
        vendorSupplierDiversity.setActive(true);
        vendorSupplierDiversity.setVendorSupplierDiversityCode(vendorSupplierDiversityCode);
        vendorSupplierDiversity.setCertificationExpirationDate(buildSqlDateOneYearFromToday());
        
        return vendorSupplierDiversity;
    }
    
    protected Date buildSqlDateOneYearFromToday() {
      return createDateOneYearFromDate(Date.valueOf(LocalDate.now()));
    }
    
    protected Date createDateOneYearFromDate(Date inputSqlDate) {
        return Date.valueOf(inputSqlDate.toLocalDate().plusYears(1));
    }

    public void setKfsSupplierDiversityDao(KfsSupplierDiversityDao kfsSupplierDiversityDao) {
        this.kfsSupplierDiversityDao = kfsSupplierDiversityDao;
    }

    public void setPaymentWorksBatchUtilityService(PaymentWorksBatchUtilityService paymentWorksBatchUtilityService) {
        this.paymentWorksBatchUtilityService = paymentWorksBatchUtilityService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
