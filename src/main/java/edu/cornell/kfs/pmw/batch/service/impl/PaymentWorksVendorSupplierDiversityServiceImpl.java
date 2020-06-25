package edu.cornell.kfs.pmw.batch.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.SupplierDiversity;
import org.kuali.kfs.vnd.businessobject.VendorSupplierDiversity;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.dataaccess.KfsSupplierDiversityDao;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksVendorSupplierDiversityService;
import edu.cornell.kfs.vnd.businessobject.CuVendorSupplierDiversityExtension;

public class PaymentWorksVendorSupplierDiversityServiceImpl implements PaymentWorksVendorSupplierDiversityService {
    private static final Logger LOG = LogManager.getLogger(PaymentWorksVendorSupplierDiversityServiceImpl.class);
    
    protected KfsSupplierDiversityDao kfsSupplierDiversityDao;

    @Override
    public List<VendorSupplierDiversity> buildSuppplierDivsersityListFromPaymentWorksVendor(PaymentWorksVendor pmwVendor) {
        Map<String, SupplierDiversity> diversityMap = kfsSupplierDiversityDao.buildPmwToKfsSupplierDiversityMap();
        
        List<VendorSupplierDiversity> diversities = new ArrayList<VendorSupplierDiversity>();
        List<String> vendorDiversityCodesAlreadyAdded = new ArrayList<String>();
        diversities.addAll(buildDiversityListFromClassifications(pmwVendor.getFederalDivsersityClassifications(), diversityMap, 
                vendorDiversityCodesAlreadyAdded));
        diversities.addAll(buildDiversityListFromClassifications(pmwVendor.getStateDivsersityClassifications(), diversityMap, 
                vendorDiversityCodesAlreadyAdded));
        
        return diversities;
    }
    
    protected List<VendorSupplierDiversity> buildDiversityListFromClassifications(String classifications, Map<String, SupplierDiversity> diversityMap, 
            List<String> vendorDiversityCodesAlreadyAdded) {
        List<VendorSupplierDiversity> diversities = new ArrayList<VendorSupplierDiversity>();
        if (StringUtils.isNotBlank(classifications)) {
            for (String diversityClass : StringUtils.split(classifications, KFSConstants.COMMA)) {
                SupplierDiversity diversity = findSupplierDiversityInMap(StringUtils.trim(diversityClass), diversityMap);
                if (ObjectUtils.isNotNull(diversity)) {
                    if (!vendorDiversityCodesAlreadyAdded.contains(diversity.getVendorSupplierDiversityCode())) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("buildDiversityListFromClassifications, found a classification: " + diversityClass + 
                                    " diversity.getVendorSupplierDiversityCode(): '" + diversity.getVendorSupplierDiversityCode() + "'");
                        }
                        vendorDiversityCodesAlreadyAdded.add(diversity.getVendorSupplierDiversityCode());
                        diversities.add(buildVendorSupplierDiversity(diversity.getVendorSupplierDiversityCode()));
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
    
    protected SupplierDiversity findSupplierDiversityInMap(String classification, Map<String, SupplierDiversity> diversityMap) {
        SupplierDiversity diversity = null;
        if (MapUtils.isNotEmpty(diversityMap) && diversityMap.containsKey(classification)) {
            diversity = diversityMap.get(classification);
        }
        return diversity;
        
    }
    
    private VendorSupplierDiversity buildVendorSupplierDiversity(String vendorSupplierDiversityCode) {
        VendorSupplierDiversity vendorSupplierDiversity = new VendorSupplierDiversity();
        vendorSupplierDiversity.setVendorSupplierDiversityCode(vendorSupplierDiversityCode);
        vendorSupplierDiversity.setActive(true);
        CuVendorSupplierDiversityExtension diversityExtension = new CuVendorSupplierDiversityExtension();
        diversityExtension.setVendorSupplierDiversityCode(vendorSupplierDiversityCode);
        vendorSupplierDiversity.setExtension(diversityExtension);
        return vendorSupplierDiversity;
    }

    public void setKfsSupplierDiversityDao(KfsSupplierDiversityDao kfsSupplierDiversityDao) {
        this.kfsSupplierDiversityDao = kfsSupplierDiversityDao;
    }

}
