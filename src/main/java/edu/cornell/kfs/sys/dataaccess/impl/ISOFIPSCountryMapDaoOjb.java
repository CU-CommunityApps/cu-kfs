package edu.cornell.kfs.sys.dataaccess.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import edu.cornell.kfs.sys.dataaccess.ISOFIPSCountryMapDao;
import edu.cornell.kfs.sys.businessobject.ISOFIPSCountryMap;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;


public class ISOFIPSCountryMapDaoOjb extends PlatformAwareDaoBaseOjb implements ISOFIPSCountryMapDao {
    
    private static final Logger LOG = LogManager.getLogger(ISOFIPSCountryMapDaoOjb.class);
    
    public List<ISOFIPSCountryMap> findActiveFipsCountryCodes(String isoCountryCode) {
        Criteria isoSearchCode = new Criteria();
        isoSearchCode.addEqualTo(CUKFSPropertyConstants.ISOFIPSCountryMap.ISO_COUNTRY_CODE, isoCountryCode);
        isoSearchCode.addEqualTo(CUKFSPropertyConstants.ISOFIPSCountryMap.ACTIVE, true);
        return (List) getPersistenceBrokerTemplate().getCollectionByQuery(QueryFactory.newQuery(ISOFIPSCountryMap.class, isoSearchCode));
    }

    public List<ISOFIPSCountryMap> findActiveIsoCountryCodes(String fipsCountryCode) {
        Criteria fipsSearchCode = new Criteria();
        fipsSearchCode.addEqualTo(CUKFSPropertyConstants.ISOFIPSCountryMap.FIPS_COUNTRY_CODE, fipsCountryCode);
        fipsSearchCode.addEqualTo(CUKFSPropertyConstants.ISOFIPSCountryMap.ACTIVE, true);
        return (List) getPersistenceBrokerTemplate().getCollectionByQuery(QueryFactory.newQuery(ISOFIPSCountryMap.class, fipsSearchCode));
    }

}
