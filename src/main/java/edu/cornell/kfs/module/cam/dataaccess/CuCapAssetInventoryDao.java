package edu.cornell.kfs.module.cam.dataaccess;

import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.sys.businessobject.Building;
import org.kuali.kfs.sys.businessobject.Room;

import java.util.List;

public interface CuCapAssetInventoryDao {
    List<Building> getBuildings(String campusCode, String queryCode, String queryName);

    List<Room> getBuildingRooms(String campusCode, String buildingCode);

    Asset getAssetByTagNumber(String assetTagNumber);
}
