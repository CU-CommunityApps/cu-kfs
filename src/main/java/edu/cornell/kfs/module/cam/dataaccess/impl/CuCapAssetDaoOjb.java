package edu.cornell.kfs.module.cam.dataaccess.impl;

import edu.cornell.kfs.module.cam.CuCamsConstants;
import edu.cornell.kfs.module.cam.dataaccess.CuCapAssetDao;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.Query;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetCondition;
import org.kuali.kfs.sys.businessobject.Building;
import org.kuali.kfs.sys.businessobject.Room;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import java.util.List;

public class CuCapAssetDaoOjb extends PlatformAwareDaoBaseOjb implements CuCapAssetDao {

    private static final Logger LOG = LogManager.getLogger(CuCapAssetDaoOjb.class);

    public List<Building> getBuildings(String campusCode, String queryCode, String queryName) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(CuCamsConstants.CapAssetApi.CAMPUS_CODE_PARAMETER, campusCode);
        if (StringUtils.isNotEmpty(queryName)) {
            criteria.addLike(getDbPlatform().getUpperCaseFunction() + "(" + "buildingName" + ")", "%" + queryName.toUpperCase() + "%");
        } else if (StringUtils.isNotEmpty(queryCode)) {
            criteria.addLike(getDbPlatform().getUpperCaseFunction() + "(" + "buildingCode" + ")", "%" + queryCode.toUpperCase() + "%");
        }

        Query query = QueryFactory.newQuery(Building.class, criteria);
        return (List<Building>) getPersistenceBrokerTemplate().getCollectionByQuery(query);
    }

    public List<Room> getBuildingRooms(String campusCode, String buildingCode) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(CuCamsConstants.CapAssetApi.CAMPUS_CODE_PARAMETER, campusCode);
        criteria.addEqualTo("buildingCode", buildingCode);
        Query query = QueryFactory.newQuery(Room.class, criteria);
        return (List<Room>) getPersistenceBrokerTemplate().getCollectionByQuery(query);
    }

    public List<AssetCondition> getAssetConditions() {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(CuCamsConstants.CapAssetApi.ACTIVE, CuCamsConstants.CapAssetApi.YES);
        Query query = QueryFactory.newQuery(AssetCondition.class, criteria);
        return (List<AssetCondition>) getPersistenceBrokerTemplate().getCollectionByQuery(query);
    }

    public Asset getAssetByTagNumber(String assetTagNumber) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(CuCamsConstants.CapAssetApi.CAMPUS_TAG_NUMBER, assetTagNumber);
        Query query = QueryFactory.newQuery(Asset.class, criteria);
        ((QueryByCriteria) query).setCriteria(criteria);
        return (Asset) getPersistenceBrokerTemplate().getObjectByQuery(query);
    }

}
