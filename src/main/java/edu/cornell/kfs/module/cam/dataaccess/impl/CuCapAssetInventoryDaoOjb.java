package edu.cornell.kfs.module.cam.dataaccess.impl;

import edu.cornell.kfs.module.cam.CuCamsConstants;
import edu.cornell.kfs.module.cam.dataaccess.CuCapAssetInventoryDao;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.Query;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.Building;
import org.kuali.kfs.sys.businessobject.Room;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import java.util.List;
import java.util.Locale;

public class CuCapAssetInventoryDaoOjb extends PlatformAwareDaoBaseOjb implements CuCapAssetInventoryDao {

    private static final Logger LOG = LogManager.getLogger(CuCapAssetInventoryDaoOjb.class);

    public List<Building> getBuildings(String campusCode, String queryCode, String queryName) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(CuCamsConstants.CapAssetApi.CAMPUS_CODE_PARAMETER, campusCode);
        criteria.addEqualTo(CuCamsConstants.CapAssetApi.ACTIVE, true);
        if (StringUtils.isNotEmpty(queryName)) {
            criteria.addLike(getDbPlatform().getUpperCaseFunction() + "(" + KFSPropertyConstants.BUILDING_NAME + ")", "%" + queryName.toUpperCase(Locale.US) + "%");
        } else if (StringUtils.isNotEmpty(queryCode)) {
            criteria.addLike(getDbPlatform().getUpperCaseFunction() + "(" + KFSPropertyConstants.BUILDING_CODE + ")", "%" + queryCode.toUpperCase(Locale.US) + "%");
        }

        Query query = QueryFactory.newQuery(Building.class, criteria);
        return (List<Building>) getPersistenceBrokerTemplate().getCollectionByQuery(query);
    }

    public List<Room> getBuildingRooms(String campusCode, String buildingCode) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(CuCamsConstants.CapAssetApi.CAMPUS_CODE_PARAMETER, campusCode);
        criteria.addEqualTo(KFSPropertyConstants.BUILDING_CODE, buildingCode);
        criteria.addEqualTo(CuCamsConstants.CapAssetApi.ACTIVE, true);
        Query query = QueryFactory.newQuery(Room.class, criteria);
        return (List<Room>) getPersistenceBrokerTemplate().getCollectionByQuery(query);
    }

    public Asset getAssetByTagNumber(String assetTagNumber) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(CuCamsConstants.CapAssetApi.CAMPUS_TAG_NUMBER, StringUtils.upperCase(assetTagNumber));
        Query query = QueryFactory.newQuery(Asset.class, criteria);
        ((QueryByCriteria) query).setCriteria(criteria);
        return (Asset) getPersistenceBrokerTemplate().getObjectByQuery(query);
    }

}
