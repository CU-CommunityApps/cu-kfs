package edu.cornell.kfs.pmw.batch.service.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorSupplierDiversity;

import edu.cornell.kfs.pmw.batch.businessobject.KfsToPMWSupplierDiversityDTO;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.dataaccess.KfsSupplierDiversityDao;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksVendorSupplierDiversityService;
import edu.cornell.kfs.vnd.businessobject.CuVendorSupplierDiversityExtension;

public class PaymentWorksVendorSupplierDiversityServiceImpl implements PaymentWorksVendorSupplierDiversityService {
    private static final Logger LOG = LogManager.getLogger(PaymentWorksVendorSupplierDiversityServiceImpl.class);
    
    protected KfsSupplierDiversityDao kfsSupplierDiversityDao;

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
                    LOG.error("buildDiversityListFromClassifications, no active supplier diversity for divsersity classification " + diversityClass);
                }
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("buildDiversityListFromClassifications, the diversity classification list was empty.");
        }
        return diversities;
    }
    
    protected List<KfsToPMWSupplierDiversityDTO> findSupplierDiversityInMap(String classification, List<KfsToPMWSupplierDiversityDTO> diversityList) {
        return diversityList.stream()
                .filter(dto -> StringUtils.equalsIgnoreCase(classification, dto.getPaymentWorksSuppliertDiversityDescription()))
                .collect(Collectors.toList());
        
    }
    
    private VendorSupplierDiversity buildVendorSupplierDiversity(String vendorSupplierDiversityCode) {
        VendorSupplierDiversity vendorSupplierDiversity = new VendorSupplierDiversity();
        vendorSupplierDiversity.setVendorSupplierDiversityCode(vendorSupplierDiversityCode);
        vendorSupplierDiversity.setActive(true);
        CuVendorSupplierDiversityExtension diversityExtension = new CuVendorSupplierDiversityExtension();
        diversityExtension.setVendorSupplierDiversityCode(vendorSupplierDiversityCode);
        /*
         * @todo do something real here
         */
        Date vendorSupplierDiversityExpirationDate = new Date(Calendar.getInstance().getTimeInMillis());
        vendorSupplierDiversityExpirationDate.setYear(vendorSupplierDiversityExpirationDate.getYear() + 1);
        diversityExtension.setVendorSupplierDiversityExpirationDate(vendorSupplierDiversityExpirationDate);
        
        vendorSupplierDiversity.setExtension(diversityExtension);
        return vendorSupplierDiversity;
    }

    public void setKfsSupplierDiversityDao(KfsSupplierDiversityDao kfsSupplierDiversityDao) {
        this.kfsSupplierDiversityDao = kfsSupplierDiversityDao;
    }

}
