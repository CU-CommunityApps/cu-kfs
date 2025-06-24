/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.module.cam.businessobject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.bo.GlobalBusinessObject;
import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetail;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.CamsKeyConstants;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.sys.CUKFSPropertyConstants;

public class AssetLocationGlobal extends PersistableBusinessObjectBase implements GlobalBusinessObject {

    private String documentNumber;
    private DocumentHeader documentHeader;
    private List<AssetLocationGlobalDetail> assetLocationGlobalDetails;

    public AssetLocationGlobal() {
        assetLocationGlobalDetails = new ArrayList<>();
    }

    @Override
    public String getDocumentNumber() {
        return documentNumber;
    }

    @Override
    public void setDocumentNumber(final String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public DocumentHeader getDocumentHeader() {
        return documentHeader;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setDocumentHeader(final DocumentHeader documentHeader) {
        this.documentHeader = documentHeader;
    }

    public List<AssetLocationGlobalDetail> getAssetLocationGlobalDetails() {
        return assetLocationGlobalDetails;
    }

    public void setAssetLocationGlobalDetails(final List<AssetLocationGlobalDetail> assetLocationGlobalDetails) {
        this.assetLocationGlobalDetails = assetLocationGlobalDetails;
    }

    @Override
    public List<PersistableBusinessObject> generateDeactivationsToPersist() {
        return null;
    }

    /**
     * @return a list of Assets to update
     */
    @Override
    public List<PersistableBusinessObject> generateGlobalChangesToPersist() {
        // the list of persist-ready BOs
        final List<PersistableBusinessObject> persistables = new ArrayList<>();

        // walk over each change detail record
        for (final AssetLocationGlobalDetail detail : assetLocationGlobalDetails) {
            boolean isCampusCodeChanged = false;
            boolean isBuildingCodeChanged = false;
            boolean isBuildingRoomNumberChanged = false;
            boolean isBuildingSubRoomNumberChanged = false;
            boolean isCampusTagNumberChanged = false;
            // load the object by keys
            final Asset asset = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(Asset.class,
                    detail.getPrimaryKeys());

            // if we got a valid asset, do the processing
            if (asset != null) {
                if (!StringUtils.equalsIgnoreCase(asset.getCampusCode(), detail.getCampusCode())) {
                    asset.setCampusCode(detail.getCampusCode());
                    isCampusCodeChanged = true;
                }

                if (!StringUtils.equalsIgnoreCase(asset.getBuildingCode(), detail.getBuildingCode())) {
                    asset.setBuildingCode(detail.getBuildingCode());
                    isBuildingCodeChanged = true;
                }

                if (!StringUtils.equalsIgnoreCase(asset.getBuildingRoomNumber(), detail.getBuildingRoomNumber())) {
                    asset.setBuildingRoomNumber(detail.getBuildingRoomNumber());
                    isBuildingRoomNumberChanged = true;
                }

                if (!StringUtils.equalsIgnoreCase(asset.getBuildingSubRoomNumber(), detail.getBuildingSubRoomNumber())) {
                    asset.setBuildingSubRoomNumber(detail.getBuildingSubRoomNumber());
                    isBuildingSubRoomNumberChanged = true;
                }

                if (!StringUtils.equalsIgnoreCase(detail.getCampusTagNumber(), asset.getCampusTagNumber())) {
                    asset.setOldTagNumber(asset.getCampusTagNumber());
                    asset.setCampusTagNumber(detail.getCampusTagNumber());
                    isCampusTagNumberChanged = true;
                }

                updateOffCampusWithOnCampusValues(asset);

                if (isCampusCodeChanged || isBuildingCodeChanged || isBuildingRoomNumberChanged
                        || isBuildingSubRoomNumberChanged || isCampusTagNumberChanged) {
                    asset.setLastInventoryDate(new Timestamp(SpringContext.getBean(DateTimeService.class)
                            .getCurrentSqlDate().getTime()));
                }

                persistables.add(asset);
            }
        }

        return persistables;
    }

    /**
     * KFSMI-6695 Location Global allows update to building, room when asset has off campus location fields
     * populated, after update assets has both on campus and off campus. An asset should not have both an on campus
     * address ( building, and room) and an off campus addresses. If the building and room are updated from location
     * global then the following fields should be set to null
     * WHERE the ast_loc_typ_cd = 'O' cm_ast_loc_t.ast_loc_cntnt_nm cm_ast_loc_t.ast_loc_strt_addr
     * cm_ast_loc_t.ast_loc_city_nm cm_ast_loc_t.ast_loc_state_cd cm_ast_loc_t.ast_loc_cntry_cd
     * cm_ast_loc_t.ast_loc_zip_cd
     *
     * @param asset
     */
    private void updateOffCampusWithOnCampusValues(final Asset asset) {
        if (asset.getAssetLocations() != null) {
            for (final AssetLocation location : asset.getAssetLocations()) {
                final boolean offCampus = CamsConstants.AssetLocationTypeCode.OFF_CAMPUS.equals(
                        location.getAssetLocationTypeCode());
                final boolean buildingOrRoom = StringUtils.isNotBlank(asset.getBuildingCode())
                                               || StringUtils.isNotBlank(asset.getBuildingRoomNumber());
                if (offCampus && buildingOrRoom) {
                    location.setAssetLocationContactName(null);
                    location.setAssetLocationStreetAddress(null);
                    location.setAssetLocationCityName(null);
                    location.setAssetLocationStateCode(null);
                    location.setAssetLocationCountryCode(null);
                    location.setAssetLocationZipCode(null);
                }
            }
        }
    }

    /*
     * CU Customization: Modified isPersistable() implementation to prevent the document from being saved
     * when any of the detail objects specify an invalid building or room. Also added related helper methods.
     * 
     * Some of the code and logic here has been duplicated from the AssetLocationGlobalRule class.
     */

    @Override
    public boolean isPersistable() {
        boolean canPersist = true;
        if (CollectionUtils.isNotEmpty(assetLocationGlobalDetails)) {
            int index = 0;
            for (final AssetLocationGlobalDetail detail : assetLocationGlobalDetails) {
                final String errorPath = StringUtils.join(KRADConstants.MAINTENANCE_NEW_MAINTAINABLE,
                        CamsPropertyConstants.AssetLocationGlobal.ASSET_LOCATION_GLOBAL_DETAILS, "[", index, "]");
                GlobalVariables.getMessageMap().addToErrorPath(errorPath);
                canPersist &= detailHasValidOrIncompleteForeignKeyValues(detail);
                GlobalVariables.getMessageMap().removeFromErrorPath(errorPath);
                index++;
            }
        }
        return canPersist;
    }

    private boolean detailHasValidOrIncompleteForeignKeyValues(final AssetLocationGlobalDetail detail) {
        boolean valid = true;
        final boolean hasCampusCode = StringUtils.isNotBlank(detail.getCampusCode());
        final boolean hasBuildingCode = StringUtils.isNotBlank(detail.getBuildingCode());
        final boolean hasRoomNumber = StringUtils.isNotBlank(detail.getBuildingRoomNumber());

        if (hasCampusCode && hasBuildingCode) {
            detail.refreshReferenceObject(KFSPropertyConstants.BUILDING);
            if (ObjectUtils.isNull(detail.getBuilding())) {
                GlobalVariables.getMessageMap().putError(CamsPropertyConstants.AssetLocationGlobal.BUILDING_CODE,
                        CamsKeyConstants.AssetLocationGlobal.ERROR_INVALID_BUILDING_CODE,
                        detail.getBuildingCode(), detail.getCampusCode());
                valid = false;
            }
        }

        if (hasCampusCode && hasBuildingCode && hasRoomNumber) {
            detail.refreshReferenceObject(CUKFSPropertyConstants.BUILDING_ROOM);
            if (ObjectUtils.isNull(detail.getBuildingRoom())) {
                GlobalVariables.getMessageMap().putError(CamsPropertyConstants.AssetLocationGlobal.BUILDING_ROOM_NUMBER,
                        CamsKeyConstants.AssetLocationGlobal.ERROR_INVALID_ROOM_NUMBER,
                        detail.getBuildingCode(), detail.getBuildingRoomNumber(), detail.getCampusCode());
                valid = false;
            }
        }

        return valid;
    }

    /*
     * End CU Customization
     */

    @Override
    public List<? extends GlobalBusinessObjectDetail> getAllDetailObjects() {
        return getAssetLocationGlobalDetails();
    }

    @Override
    public List<Collection<PersistableBusinessObject>> buildListOfDeletionAwareLists() {
        final List<Collection<PersistableBusinessObject>> managedLists = super.buildListOfDeletionAwareLists();
        managedLists.add(new ArrayList<>(getAssetLocationGlobalDetails()));
        return managedLists;
    }

}
