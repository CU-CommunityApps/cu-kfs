package edu.cornell.kfs.module.cam.service.impl;

import edu.cornell.kfs.fp.businessobject.CapitalAssetInformationDetailExtendedAttribute;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformationDetail;
import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.CamsKeyConstants;
import org.kuali.kfs.module.cam.CamsParameterConstants;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetType;
import org.kuali.kfs.module.cam.service.impl.CapitalAssetManagementModuleServiceImpl;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.Building;
import org.kuali.kfs.sys.businessobject.Campus;
import org.kuali.kfs.sys.businessobject.Room;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.kew.service.KEWServiceLocator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CuCapitalAssetManagementModuleServiceImpl extends CapitalAssetManagementModuleServiceImpl {

    protected boolean checkNewCapitalAssetFieldsExist(
            final CapitalAssetInformation capitalAssetInformation,
            final AccountingDocument accountingDocument, final int caLineIndex) {
        boolean valid = true;

        if (StringUtils.isBlank(capitalAssetInformation.getCapitalAssetTypeCode())) {
            final String label = this.getDataDictionaryService().getAttributeLabel(CapitalAssetInformation.class,
                    KFSPropertyConstants.CAPITAL_ASSET_TYPE_CODE);
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.CAPITAL_ASSET_TYPE_CODE,
                    KFSKeyConstants.ERROR_REQUIRED, label);
            valid = false;
        }

        if (capitalAssetInformation.getCapitalAssetQuantity() == null
                || capitalAssetInformation.getCapitalAssetQuantity() <= 0) {
            final String label = this.getDataDictionaryService().getAttributeLabel(CapitalAssetInformation.class,
                    KFSPropertyConstants.CAPITAL_ASSET_QUANTITY);
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.CAPITAL_ASSET_QUANTITY,
                    KFSKeyConstants.ERROR_REQUIRED, label);
            valid = false;
        }

        //VENDOR_IS_REQUIRED_FOR_NON_MOVEABLE_ASSET parameter determines if we need to check
        //vendor name entered.
        String vendorNameRequired = getParameterService().getParameterValueAsString(Asset.class, CamsParameterConstants.VENDOR_REQUIRED_FOR_NON_MOVABLE_ASSET_IND);

        if ("Y".equalsIgnoreCase(vendorNameRequired)) {
            // skip vendor name required validation for procurement card document
            // skip vendor name required validation for FP Docs
            final String pid = KEWServiceLocator.getDocumentTypeService().findByDocumentId(accountingDocument.getDocumentNumber()).getDocTypeParentId();
            final String docType = KEWServiceLocator.getDocumentTypeService().findById(pid).getName();

            if (!("FP".equals(docType)) && !(accountingDocument instanceof ProcurementCardDocument) && StringUtils.isBlank(capitalAssetInformation.getVendorName())) {
                final String label = this.getDataDictionaryService().getAttributeLabel(CapitalAssetInformation.class, KFSPropertyConstants.VENDOR_NAME);
                GlobalVariables.getMessageMap().putError(KFSPropertyConstants.VENDOR_NAME, KFSKeyConstants.ERROR_REQUIRED, label);
                valid = false;
            }
        }

        //MANUFACTURER_IS_REQUIRED_FOR_NON_MOVEABLE_ASSET parameter determines if we need to check
        //vendor name entered.
        final String manufacturerNameRequired = getParameterService().getParameterValueAsString(Asset.class, CamsParameterConstants.MANUFACTURER_REQUIRED_FOR_NON_MOVABLE_ASSET_IND);

        if ("Y".equalsIgnoreCase(manufacturerNameRequired)) {
            if (StringUtils.isBlank(capitalAssetInformation.getCapitalAssetManufacturerName())) {
                final String label = this.getDataDictionaryService().getAttributeLabel(CapitalAssetInformation.class,
                        KFSPropertyConstants.CAPITAL_ASSET_MANUFACTURE_NAME);
                GlobalVariables.getMessageMap().putError(KFSPropertyConstants.CAPITAL_ASSET_MANUFACTURE_NAME,
                        KFSKeyConstants.ERROR_REQUIRED, label);
                valid = false;
            }
        }

        if (StringUtils.isBlank(capitalAssetInformation.getCapitalAssetDescription())) {
            final String label = this.getDataDictionaryService().getAttributeLabel(CapitalAssetInformation.class,
                    CamsPropertyConstants.Asset.CAPITAL_ASSET_DESCRIPTION);
            GlobalVariables.getMessageMap().putError(CamsPropertyConstants.Asset.CAPITAL_ASSET_DESCRIPTION,
                    KFSKeyConstants.ERROR_REQUIRED, label);
            valid = false;
        }

        int index = 0;
        final List<CapitalAssetInformationDetail> capitalAssetInformationDetails = capitalAssetInformation.getCapitalAssetInformationDetails();
        for (final CapitalAssetInformationDetail dtl : capitalAssetInformationDetails) {
            final String errorPathPrefix = KFSPropertyConstants.DOCUMENT + "." +
                    KFSPropertyConstants.CAPITAL_ASSET_INFORMATION + "[" + caLineIndex + "]." +
                    KFSPropertyConstants.CAPITAL_ASSET_INFORMATION_DETAILS;
            final CapitalAssetInformationDetailExtendedAttribute capDetailExt = (CapitalAssetInformationDetailExtendedAttribute) dtl.getExtension();

            final String buildingCd = dtl.getBuildingCode();
            final String roomCd = dtl.getBuildingRoomNumber();
            final String assetLocationCityName = capDetailExt.getAssetLocationCityName();
            final String assetLocationCountryCode = capDetailExt.getAssetLocationCountryCode();
            final String assetLocationStateCode = capDetailExt.getAssetLocationStateCode();
            final String assetLocationStreetAddress = capDetailExt.getAssetLocationStreetAddress();
            final String assetLocationZipCode = capDetailExt.getAssetLocationZipCode();
            
            // Room is not required for non-moveable
            final AssetType assetType = getAssetType(capitalAssetInformation.getCapitalAssetTypeCode());

            if (StringUtils.isBlank(assetLocationCityName) && StringUtils.isBlank(assetLocationStateCode) && StringUtils.isBlank(assetLocationCountryCode) && StringUtils.isBlank(assetLocationStreetAddress) && StringUtils.isBlank(assetLocationZipCode)) {
                // Building code required for moveable assets or assets that require building
                if (ObjectUtils.isNull(assetType) || assetType.isMovingIndicator() || assetType.isRequiredBuildingIndicator()) {
                    if (StringUtils.isBlank(dtl.getBuildingCode())) {
                        final String label = this.getDataDictionaryService().getAttributeLabel(Building.class, KFSPropertyConstants.BUILDING_CODE);
                        GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(errorPathPrefix + "[" + index + "]" + "." + KFSPropertyConstants.BUILDING_CODE, KFSKeyConstants.ERROR_REQUIRED, label);
                        valid = false;
                    }
                }
                // Room is not required for non-moveable
                if (ObjectUtils.isNull(assetType) || assetType.isMovingIndicator()) {
                    // Room is required for moveable
                    if (StringUtils.isBlank(roomCd)) {
                        final String label = this.getDataDictionaryService().getAttributeLabel(Room.class, KFSPropertyConstants.BUILDING_ROOM_NUMBER);
                        GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(errorPathPrefix + "[" + index + "]" + "." + KFSPropertyConstants.BUILDING_ROOM_NUMBER, KFSKeyConstants.ERROR_REQUIRED, label);
                        valid = false;
                    }
                }

            }
            
            if (StringUtils.isNotBlank(assetLocationCityName) || StringUtils.isNotBlank(assetLocationStateCode) || StringUtils.isNotBlank(assetLocationCountryCode) || StringUtils.isNotBlank(assetLocationStreetAddress) || StringUtils.isNotBlank(assetLocationZipCode)) {
                if (StringUtils.isNotBlank(buildingCd)) {       
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(errorPathPrefix + "[" + index + "]" + "." + KFSPropertyConstants.BUILDING_CODE, KFSKeyConstants.ERROR_CUSTOM, "Building Code not allowed with off-campus Asset Location Address");
                    valid = false;
                }
                if (StringUtils.isNotBlank(roomCd)) {
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(errorPathPrefix + "[" + index + "]" + "." + KFSPropertyConstants.BUILDING_ROOM_NUMBER, KFSKeyConstants.ERROR_CUSTOM, "Room Number not allowed with off-campus Asset Location Address");
                    valid = false;
                   
                }
                if (StringUtils.isBlank(assetLocationCityName)) {
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(errorPathPrefix + "[" + index + "]" + "." + "extension.assetLocationCityName", KFSKeyConstants.ERROR_CUSTOM, "City Name is required with off-campus Asset Location Address");
                    valid = false;
                }
                if (StringUtils.isBlank(assetLocationStateCode)) {
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(errorPathPrefix + "[" + index + "]" + "." + "extension.assetLocationStateCode", KFSKeyConstants.ERROR_CUSTOM, "State Code is required with off-campus Asset Location Address");
                    valid = false;
                }
                if (StringUtils.isBlank(assetLocationCountryCode)) {
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(errorPathPrefix + "[" + index + "]" + "." + "extension.assetLocationCountryCode", KFSKeyConstants.ERROR_CUSTOM, "Country Code is required with off-campus Asset Location Address");
                    valid = false;
                }
                if (StringUtils.isBlank(assetLocationStreetAddress)) {
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(errorPathPrefix + "[" + index + "]" + "." + "extension.assetLocationStreetAddress", KFSKeyConstants.ERROR_CUSTOM, "Street Address is required with off-campus Asset Location Address");
                    valid = false;
                }
                if (StringUtils.isBlank(assetLocationZipCode)) {
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(errorPathPrefix + "[" + index + "]" + "." + "extension.assetLocationZipCode", KFSKeyConstants.ERROR_CUSTOM, "Zipcode is required with off-campus Asset Location Address");
                    valid = false;
                }
            }

            index++;
        }

        return valid;
    }

    protected boolean validateAssetTagLocationLines(
            final CapitalAssetInformation capitalAssetInformation, 
            final int capitalAssetIndex, final AccountingDocument accountingDocument) {
        boolean valid = true;

        final List<CapitalAssetInformationDetail> capitalAssetInformationDetails = capitalAssetInformation.getCapitalAssetInformationDetails();
        int index = 0;

        for (final CapitalAssetInformationDetail dtl : capitalAssetInformationDetails) {
            final CapitalAssetInformationDetailExtendedAttribute capDetailExt = (CapitalAssetInformationDetailExtendedAttribute) dtl.getExtension();
            
            final String assetLocationCityName = capDetailExt.getAssetLocationCityName();
            final String assetLocationCountryCode = capDetailExt.getAssetLocationCountryCode();
            final String assetLocationStateCode = capDetailExt.getAssetLocationStateCode();
            final String assetLocationStreetAddress = capDetailExt.getAssetLocationStreetAddress();
            final String assetLocationZipCode = capDetailExt.getAssetLocationZipCode();
            // We have to explicitly call this DD service to upper case each field. This may not be the best place and maybe form
            // populate is a better place but we CAMS team don't own FP document. This is the best we can do for now.
            businessObjectDictionaryService.performForceUppercase(dtl);
            final String errorPathPrefix = KFSPropertyConstants.DOCUMENT + "." + KFSPropertyConstants.CAPITAL_ASSET_INFORMATION + "[" + capitalAssetIndex + "]." + KFSPropertyConstants.CAPITAL_ASSET_INFORMATION_DETAILS;
            if (StringUtils.isNotBlank(dtl.getCampusCode())) {
                final Campus campus = businessObjectService.findBySinglePrimaryKey(Campus.class, dtl.getCampusCode());
                if (ObjectUtils.isNull(campus)) {
                    valid = false;
                    final String label = this.getDataDictionaryService().getAttributeLabel(Campus.class, KFSPropertyConstants.CODE);
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(errorPathPrefix + "[" + index + "]" + "." + KFSPropertyConstants.CAMPUS_CODE, KFSKeyConstants.ERROR_EXISTENCE, label);
                }
            }
            Map<String, String> params;

            if (StringUtils.isNotBlank(dtl.getCampusCode()) && StringUtils.isNotBlank(dtl.getBuildingCode()) && StringUtils.isBlank(assetLocationCityName) && StringUtils.isBlank(assetLocationStateCode) && StringUtils.isBlank(assetLocationCountryCode) && StringUtils.isBlank(assetLocationStreetAddress) && StringUtils.isBlank(assetLocationZipCode)) {              
                params = new HashMap<String, String>();
                params.put(KFSPropertyConstants.CAMPUS_CODE, dtl.getCampusCode());
                params.put(KFSPropertyConstants.BUILDING_CODE, dtl.getBuildingCode());
                final Building building = businessObjectService.findByPrimaryKey(Building.class, params);
                // Check if building is valid
                if (ObjectUtils.isNull(building)) {
                    valid = false;
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(errorPathPrefix + "[" + index + "]" + "." + KFSPropertyConstants.BUILDING_CODE, CamsKeyConstants.AssetLocationGlobal.ERROR_INVALID_BUILDING_CODE, dtl.getBuildingCode(), dtl.getCampusCode());
                }
            }

            final AssetType assetType = getAssetType(capitalAssetInformation.getCapitalAssetTypeCode());
            if (StringUtils.isBlank(assetLocationCityName) && StringUtils.isBlank(assetLocationStateCode) && StringUtils.isBlank(assetLocationCountryCode) && StringUtils.isBlank(assetLocationStreetAddress) && StringUtils.isBlank(assetLocationZipCode)) {
                // If building was specified but was not required for this asset type display an error
                if (StringUtils.isNotBlank(dtl.getBuildingCode()) && ObjectUtils.isNotNull(assetType) && !assetType.isMovingIndicator() && !assetType.isRequiredBuildingIndicator()) {
                    valid = false;
                    final String label = this.getDataDictionaryService().getAttributeLabel(Building.class, KFSPropertyConstants.BUILDING_CODE);
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(errorPathPrefix + "[" + index + "]" + "." + KFSPropertyConstants.BUILDING_CODE, CamsKeyConstants.AssetLocation.ERROR_ASSET_LOCATION_BUILDING_NON_MOVABLE, label);
                }

                if (StringUtils.isNotBlank(dtl.getCampusCode()) && StringUtils.isNotBlank(dtl.getBuildingCode()) && StringUtils.isNotBlank(dtl.getBuildingRoomNumber())) {
                    params = new HashMap<>();
                    params.put(KFSPropertyConstants.CAMPUS_CODE, dtl.getCampusCode());
                    params.put(KFSPropertyConstants.BUILDING_CODE, dtl.getBuildingCode());
                    params.put(KFSPropertyConstants.BUILDING_ROOM_NUMBER, dtl.getBuildingRoomNumber());
                    final Room room = businessObjectService.findByPrimaryKey(Room.class, params);
                    // Check if room is valid
                    if (ObjectUtils.isNull(room)) {
                        valid = false;
                        GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(errorPathPrefix + "[" + index + "]" + "." + KFSPropertyConstants.BUILDING_ROOM_NUMBER, CamsKeyConstants.AssetLocationGlobal.ERROR_INVALID_ROOM_NUMBER, dtl.getBuildingRoomNumber(), dtl.getBuildingCode(), dtl.getCampusCode());
                    }
                }

                // If room was specified but was not required for this asset type display an error
                if (StringUtils.isNotBlank(dtl.getBuildingRoomNumber()) && ObjectUtils.isNotNull(assetType) && !assetType.isMovingIndicator()) {
                    valid = false;
                    final String label = this.getDataDictionaryService().getAttributeLabel(Room.class, KFSPropertyConstants.BUILDING_ROOM_NUMBER);
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(errorPathPrefix + "[" + index + "]" + "." + KFSPropertyConstants.BUILDING_ROOM_NUMBER, CamsKeyConstants.AssetLocation.ERROR_ASSET_LOCATION_ROOM_NUMBER_NON_MOVABLE, label);
                }
            }
            index++;
        }

        return valid;
    }

    /**
     * Build the appropriate note text being set to the purchase order document.
     * Our version includes all assetNumbers, not just the first and last.
     *
     * @param documentType
     * @param assetNumbers
     * @return
     */
    @Override
    protected String buildNoteTextForPurApDoc(final String documentType, final List<Long> assetNumbers) {
        final StringBuffer noteText = new StringBuffer();

        if (CamsConstants.DocumentTypeName.ASSET_ADD_GLOBAL.equalsIgnoreCase(documentType)) {
            noteText.append("Asset Numbers have been created for this document: ");
        } else {
            noteText.append("Existing Asset Numbers have been applied for this document: ");
        }

        if (assetNumbers != null && assetNumbers.size() > 0) {
            int i = 0;
            for (final Long assetNumber : assetNumbers) {
                if (i++ == 0) {
                    noteText.append(assetNumber.toString());
                } else {
                    noteText.append(",").append(assetNumber.toString());
                }
            }
        }

        return noteText.toString();
    }

}
