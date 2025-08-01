/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.fp.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.Building;
import org.kuali.kfs.sys.businessobject.Campus;
import org.kuali.kfs.sys.businessobject.Room;

import edu.cornell.kfs.fp.businessobject.CapitalAssetInformationDetailExtendedAttribute;

import java.util.HashMap;
import java.util.Map;

public class CapitalAssetInformationDetail extends PersistableBusinessObjectBase {

    //primary key fields..
    protected String documentNumber;
    protected Integer capitalAssetLineNumber;
    protected Integer itemLineNumber;
    protected String campusCode;
    protected String buildingCode;
    protected String buildingRoomNumber;
    protected String buildingSubRoomNumber;
    protected String capitalAssetTagNumber;
    protected String capitalAssetSerialNumber;

    protected Campus campus;
    protected Building building;
    protected Room room;
    protected CapitalAssetInformation capitalAssetInformation;

    // ==== CU Customization: Add handling of the extended attribute's primary key fields. ====

    @Override
    protected void beforeInsert() {
        super.beforeInsert();
        populatePrimaryKeyOnExtendedAttribute();
    }

    @Override
    protected void beforeUpdate() {
        super.beforeUpdate();
        populatePrimaryKeyOnExtendedAttribute();
    }

    private void populatePrimaryKeyOnExtendedAttribute() {
        CapitalAssetInformationDetailExtendedAttribute extension =
                (CapitalAssetInformationDetailExtendedAttribute) getExtension();
        extension.setDocumentNumber(getDocumentNumber());
        extension.setCapitalAssetLineNumber(getCapitalAssetLineNumber());
        extension.setItemLineNumber(getItemLineNumber());
    }

    // ==== End CU Customization ====

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(final String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Integer getItemLineNumber() {
        return itemLineNumber;
    }

    public void setItemLineNumber(final Integer itemLineNumber) {
        this.itemLineNumber = itemLineNumber;
    }

    public String getCampusCode() {
        return campusCode;
    }

    public void setCampusCode(final String campusCode) {
        this.campusCode = campusCode;
    }

    public String getBuildingCode() {
        return buildingCode;
    }

    public void setBuildingCode(final String buildingCode) {
        this.buildingCode = buildingCode;
    }

    public String getBuildingRoomNumber() {
        return buildingRoomNumber;
    }

    public void setBuildingRoomNumber(final String buildingRoomNumber) {
        this.buildingRoomNumber = buildingRoomNumber;
    }

    public String getCapitalAssetTagNumber() {
        return capitalAssetTagNumber;
    }

    public void setCapitalAssetTagNumber(final String capitalAssetTagNumber) {
        this.capitalAssetTagNumber = capitalAssetTagNumber;
    }

    public String getCapitalAssetSerialNumber() {
        return capitalAssetSerialNumber;
    }

    public void setCapitalAssetSerialNumber(final String capitalAssetSerialNumber) {
        this.capitalAssetSerialNumber = capitalAssetSerialNumber;
    }

    public Campus getCampus() {
        return campus;
    }

    public void setCampus(final Campus campus) {
        this.campus = campus;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(final Building building) {
        this.building = building;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(final Room room) {
        this.room = room;
    }

    public CapitalAssetInformation getCapitalAssetInformation() {
        return capitalAssetInformation;
    }

    public void setCapitalAssetInformation(final CapitalAssetInformation capitalAssetInformation) {
        this.capitalAssetInformation = capitalAssetInformation;
    }

    public String getBuildingSubRoomNumber() {
        return buildingSubRoomNumber;
    }

    public void setBuildingSubRoomNumber(final String buildingSubRoomNumber) {
        this.buildingSubRoomNumber = buildingSubRoomNumber;
    }

    public Integer getCapitalAssetLineNumber() {
        return capitalAssetLineNumber;
    }

    public void setCapitalAssetLineNumber(final Integer capitalAssetLineNumber) {
        this.capitalAssetLineNumber = capitalAssetLineNumber;
    }

    /**
     * Returns a map with the primitive field names as the key and the primitive values as the map value.
     *
     * @return Map a map with the primitive field names as the key and the primitive values as the map value.
     */
    public Map<String, Object> getValuesMap() {
        final Map<String, Object> simpleValues = new HashMap<>();
        simpleValues.put(KFSPropertyConstants.DOCUMENT_NUMBER, getDocumentNumber());
        simpleValues.put(KFSPropertyConstants.CAPITAL_ASSET_LINE_NUMBER, getCapitalAssetLineNumber());
        simpleValues.put(KFSPropertyConstants.ITEM_LINE_NUMBER, getItemLineNumber());
        simpleValues.put(KFSPropertyConstants.CAMPUS_CODE, getCampusCode());
        simpleValues.put(KFSPropertyConstants.BUILDING_CODE, getBuildingCode());
        simpleValues.put(KFSPropertyConstants.BUILDING_ROOM_NUMBER, getBuildingRoomNumber());
        return simpleValues;
    }
}
