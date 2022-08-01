package edu.cornell.kfs.sys.dataaccess.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import edu.cornell.kfs.sys.businessobject.ISOFIPSCountryMap;
import edu.cornell.kfs.sys.dataaccess.ISOFIPSCountryMapDao;


public class ISOFIPSCountryMapDaoOjb extends PlatformAwareDaoBaseOjb implements ISOFIPSCountryMapDao {
    
    private static final Logger LOG = LogManager.getLogger(ISOFIPSCountryMapDaoOjb.class); 
    
    public List<ISOFIPSCountryMap> findFipsCountries(String isoCountryCode) {
        Criteria isoSearchCode = new Criteria();
        isoSearchCode.addEqualTo("ISO_POSTAL_CNTRY_CD", isoCountryCode);
        QueryByCriteria queryToExecute = QueryFactory.newQuery(ISOFIPSCountryMap.class, isoSearchCode);
        
        return (List) getPersistenceBrokerTemplate().getCollectionByQuery(QueryFactory.newQuery(ISOFIPSCountryMap.class, isoSearchCode));
    }
    
    public List<ISOFIPSCountryMap> findIsoCountries(String fipsCountryCode) {
        Criteria fipsSearchCode = new Criteria();
        fipsSearchCode.addEqualTo("FIPS_POSTAL_CNTRY_CD", fipsCountryCode);
        QueryByCriteria queryToExecute = QueryFactory.newQuery(ISOFIPSCountryMap.class, fipsSearchCode);
        
        return (List) getPersistenceBrokerTemplate().getCollectionByQuery(QueryFactory.newQuery(ISOFIPSCountryMap.class, fipsSearchCode));
    }
    
}
