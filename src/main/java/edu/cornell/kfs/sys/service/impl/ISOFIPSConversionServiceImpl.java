package edu.cornell.kfs.sys.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.cornell.kfs.sys.businessobject.ISOFIPSCountryMap;
import edu.cornell.kfs.sys.dataaccess.ISOFIPSCountryMapDao;
import edu.cornell.kfs.sys.service.ISOFIPSConversionService;

public class ISOFIPSConversionServiceImpl implements ISOFIPSConversionService {
    
    private static final Logger LOG = LogManager.getLogger(ISOFIPSConversionServiceImpl.class);
    
    private ISOFIPSCountryMapDao isoFipsCountryMapDao;
    
    /*
     * ISO-to-FIPS mappings are many-ISO-to-one-FIPS.
     */
    public String convertISOCountryCodeToFIPSCountryCode(String isoCountryCode) {
        List <ISOFIPSCountryMap> mappingsFound = new ArrayList<ISOFIPSCountryMap>();
        Collection <ISOFIPSCountryMap> fipsCodesFound = isoFipsCountryMapDao.findFipsCountries(isoCountryCode);
        
        for (Iterator iter = fipsCodesFound.iterator(); iter.hasNext();) {
            ISOFIPSCountryMap fipsCodeMapping = (ISOFIPSCountryMap) iter.next();
            mappingsFound.add(fipsCodeMapping);
        }
        
        if (mappingsFound.isEmpty()) {
            LOG.error("convertISOCountryCodeToFIPSCountryCode: No ISO-to-FIPS Country mapping found for ISO Country code : " + isoCountryCode);
            throw new RuntimeException("No ISO-to-FIPS Country mapping found for ISO Country code : " + isoCountryCode);
        } else if (mappingsFound.size() > 1) {
            LOG.error("convertISOCountryCodeToFIPSCountryCode: More than one ISO-to-FIPS Country mapping found for ISO Country code : " + isoCountryCode);
            throw new RuntimeException("More than one ISO-to-FIPS Country mapping found for ISO Country code : " + isoCountryCode);
        } 
        String fipsCountryCodeFound = (mappingsFound.get(0)).getFipsCountryCode();
        LOG.info("convertISOCountryCodeToFIPSCountryCode: One ISO-to-FIPS Country generic mapping found: ISO Country code : " + isoCountryCode + " mapped to FIPS Country code : " + fipsCountryCodeFound);
        return fipsCountryCodeFound;
    }

    public String convertFIPSCountryCodeToISOCountryCode(String fipsCountryCode) { 
        List <ISOFIPSCountryMap> mappingsFound = new ArrayList<ISOFIPSCountryMap>();
        Collection <ISOFIPSCountryMap> isoCodesFound = isoFipsCountryMapDao.findIsoCountries(fipsCountryCode);
        
        for (Iterator iter = isoCodesFound.iterator(); iter.hasNext();) {
            ISOFIPSCountryMap isoCodeMapping = (ISOFIPSCountryMap) iter.next();
            mappingsFound.add(isoCodeMapping);
        }
        
        if (mappingsFound.isEmpty()) {
            LOG.error("convertFIPSCountryCodeToISOCountryCode: No FIPS-to-ISO Country mapping found for FIPS Country code : " + fipsCountryCode);
            throw new RuntimeException("No ISO-to-FIPS Country mapping found for FIPS Country code : " + fipsCountryCode);
        } else if (mappingsFound.size() > 1) {
            LOG.error("convertFIPSCountryCodeToISOCountryCode: More than one FIPS-to-ISO Country mapping found for FIPS Country code : " + fipsCountryCode);
            throw new RuntimeException("More than one FIPS-to-ISO Country mapping found for FIPS Country code : " + fipsCountryCode);
        } 
        String isoCountryCodeFound = (mappingsFound.get(0)).getIsoCountryCode();
        LOG.info("convertFIPSCountryCodeToISOCountryCode: One FIPS-to-ISO Country generic mapping found FIPS Country code : " + fipsCountryCode + " mapped to ISO Country code : " + isoCountryCodeFound);
        return isoCountryCodeFound;
    }

    public ISOFIPSCountryMapDao getIsoFipsCountryMapDao() {
        return isoFipsCountryMapDao;
    }

    public void setIsoFipsCountryMapDao(ISOFIPSCountryMapDao isoFipsCountryMapDao) {
        this.isoFipsCountryMapDao = isoFipsCountryMapDao;
    }
    
}
