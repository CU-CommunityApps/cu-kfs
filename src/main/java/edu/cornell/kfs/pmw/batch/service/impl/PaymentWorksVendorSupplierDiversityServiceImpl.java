package edu.cornell.kfs.pmw.batch.service.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorSupplierDiversity;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.pmw.batch.businessobject.KfsToPMWSupplierDiversityDTO;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.dataaccess.KfsSupplierDiversityDao;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksVendorSupplierDiversityService;
import edu.cornell.kfs.vnd.businessobject.CuVendorSupplierDiversityExtension;

public class PaymentWorksVendorSupplierDiversityServiceImpl implements PaymentWorksVendorSupplierDiversityService {
    private static final Logger LOG = LogManager.getLogger(PaymentWorksVendorSupplierDiversityServiceImpl.class);
    
    protected KfsSupplierDiversityDao kfsSupplierDiversityDao;
    protected PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    protected ConfigurationService configurationService;

    @Override
    public List<VendorSupplierDiversity> buildSuppplierDivsersityListFromPaymentWorksVendor(PaymentWorksVendor pmwVendor) {
        List<VendorSupplierDiversity> diversities = new ArrayList<VendorSupplierDiversity>();
        List<String> vendorDiversityCodesAlreadyAdded = new ArrayList<String>();
        diversities.addAll(buildDiversityListFromClassifications(pmwVendor.getFederalDivsersityClassifications(), 
                kfsSupplierDiversityDao.buildPmwToKfsFederalSupplierDiversityMapForForeignForm(), 
                vendorDiversityCodesAlreadyAdded));
        diversities.addAll(buildDiversityListFromClassifications(pmwVendor.getStateDivsersityClassifications(), 
                kfsSupplierDiversityDao.buildPmwToKfsNewYorkSupplierDiversityMapForForeignForm(), 
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
                        if (!vendorDiversityCodesAlreadyAdded.contains(dto.getKfsSuppliertDiversityCode())) {
                            vendorDiversityCodesAlreadyAdded.add(dto.getKfsSuppliertDiversityCode());
                            diversities.add(buildVendorSupplierDiversity(dto.getKfsSuppliertDiversityCode()));
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
                .filter(dto -> StringUtils.equalsIgnoreCase(classification, dto.getPaymentWorksSuppliertDiversityDescription()))
                .collect(Collectors.toList());
    }
    
    protected VendorSupplierDiversity buildVendorSupplierDiversity(String vendorSupplierDiversityCode) {
        VendorSupplierDiversity vendorSupplierDiversity = new VendorSupplierDiversity();
        vendorSupplierDiversity.setVendorSupplierDiversityCode(vendorSupplierDiversityCode);
        vendorSupplierDiversity.setActive(true);
        
        CuVendorSupplierDiversityExtension diversityExtension = new CuVendorSupplierDiversityExtension();
        diversityExtension.setVendorSupplierDiversityCode(vendorSupplierDiversityCode);
        diversityExtension.setVendorSupplierDiversityExpirationDate(buildDateOneYearFromToday());
        
        vendorSupplierDiversity.setExtension(diversityExtension);
        return vendorSupplierDiversity;
    }
    
    protected Date buildDateOneYearFromToday() {
        GregorianCalendar calendarDateWithYearAdded = new GregorianCalendar();
        calendarDateWithYearAdded.clear();
        calendarDateWithYearAdded.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
        calendarDateWithYearAdded.add(GregorianCalendar.YEAR, 1);
        return new Date(calendarDateWithYearAdded.getTimeInMillis());
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
