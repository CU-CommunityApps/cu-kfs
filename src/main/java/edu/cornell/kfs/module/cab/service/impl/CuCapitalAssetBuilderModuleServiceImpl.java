package edu.cornell.kfs.module.cab.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformationDetail;
import org.kuali.kfs.fp.document.CapitalAssetEditable;
import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.kfs.module.cab.CabParameterConstants;
import org.kuali.kfs.module.cab.service.impl.CapitalAssetBuilderModuleServiceImpl;
import org.kuali.kfs.module.cam.CamsKeyConstants;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetType;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.Building;
import org.kuali.kfs.sys.businessobject.Room;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;
import org.kuali.rice.location.api.campus.Campus;

import edu.cornell.kfs.fp.businessobject.CapitalAssetInformationDetailExtendedAttribute;

public class CuCapitalAssetBuilderModuleServiceImpl extends CapitalAssetBuilderModuleServiceImpl {

    protected boolean checkNewCapitalAssetFieldsExist(CapitalAssetInformation capitalAssetInformation, AccountingDocument accountingDocument, int caLineIndex) {
        boolean valid = true;

        if (StringUtils.isBlank(capitalAssetInformation.getCapitalAssetTypeCode())) {
            String label = this.getDataDictionaryService().getAttributeLabel(CapitalAssetInformation.class, KFSPropertyConstants.CAPITAL_ASSET_TYPE_CODE);
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.CAPITAL_ASSET_TYPE_CODE, KFSKeyConstants.ERROR_REQUIRED, label);
            valid = false;
        }

        if (capitalAssetInformation.getCapitalAssetQuantity() == null || capitalAssetInformation.getCapitalAssetQuantity() <= 0) {
            String label = this.getDataDictionaryService().getAttributeLabel(CapitalAssetInformation.class, KFSPropertyConstants.CAPITAL_ASSET_QUANTITY);
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.CAPITAL_ASSET_QUANTITY, KFSKeyConstants.ERROR_REQUIRED, label);
            valid = false;
        }

        //VENDOR_IS_REQUIRED_FOR_NON_MOVEABLE_ASSET parameter determines if we need to check
        //vendor name entered.
        String vendorNameRequired = getParameterService().getParameterValueAsString(Asset.class, CabParameterConstants.CapitalAsset.VENDOR_REQUIRED_FOR_NON_MOVEABLE_ASSET_IND);

        if ("Y".equalsIgnoreCase(vendorNameRequired)) {
        // skip vendor name required validation for procurement card document
            // skip vendor name required validation for FP Docs
            String pid = KEWServiceLocator.getDocumentTypeService().findByDocumentId(accountingDocument.getDocumentNumber()).getDocTypeParentId();
            String docType = KEWServiceLocator.getDocumentTypeService().findById(pid).getName();

            if (!(docType.equals("FP")) && !(accountingDocument instanceof ProcurementCardDocument) && StringUtils.isBlank(capitalAssetInformation.getVendorName())) {
                String label = this.getDataDictionaryService().getAttributeLabel(CapitalAssetInformation.class, KFSPropertyConstants.VENDOR_NAME);
                GlobalVariables.getMessageMap().putError(KFSPropertyConstants.VENDOR_NAME, KFSKeyConstants.ERROR_REQUIRED, label);
                valid = false;
            }
        }

        //MANUFACTURER_IS_REQUIRED_FOR_NON_MOVEABLE_ASSET parameter determines if we need to check
        //vendor name entered.
        String manufacturerNameRequired = getParameterService().getParameterValueAsString(Asset.class, CabParameterConstants.CapitalAsset.MANUFACTURER_REQUIRED_FOR_NON_MOVEABLE_ASSET_IND);

        if ("Y".equalsIgnoreCase(manufacturerNameRequired)) {
            if (StringUtils.isBlank(capitalAssetInformation.getCapitalAssetManufacturerName())) {
                String label = this.getDataDictionaryService().getAttributeLabel(CapitalAssetInformation.class, KFSPropertyConstants.CAPITAL_ASSET_MANUFACTURE_NAME);
                GlobalVariables.getMessageMap().putError(KFSPropertyConstants.CAPITAL_ASSET_MANUFACTURE_NAME, KFSKeyConstants.ERROR_REQUIRED, label);
                valid = false;
            }
        }

        if (StringUtils.isBlank(capitalAssetInformation.getCapitalAssetDescription())) {
            String label = this.getDataDictionaryService().getAttributeLabel(CapitalAssetInformation.class, CamsPropertyConstants.Asset.CAPITAL_ASSET_DESCRIPTION);
            GlobalVariables.getMessageMap().putError(CamsPropertyConstants.Asset.CAPITAL_ASSET_DESCRIPTION, KFSKeyConstants.ERROR_REQUIRED, label);
            valid = false;
        }

        int index = 0;
        List<CapitalAssetInformationDetail> capitalAssetInformationDetails = capitalAssetInformation.getCapitalAssetInformationDetails();
        for (CapitalAssetInformationDetail dtl : capitalAssetInformationDetails) {
            String errorPathPrefix = KFSPropertyConstants.DOCUMENT + "." + KFSPropertyConstants.CAPITAL_ASSET_INFORMATION + "["+ caLineIndex+"]." + KFSPropertyConstants.CAPITAL_ASSET_INFORMATION_DETAILS;
            CapitalAssetInformationDetailExtendedAttribute capDetailExt = (CapitalAssetInformationDetailExtendedAttribute) dtl.getExtension();

            String buildingCd = dtl.getBuildingCode();
        	String roomCd = dtl.getBuildingRoomNumber();
        	String assetLocationCityName = capDetailExt.getAssetLocationCityName(); 
    		String assetLocationCountryCode = capDetailExt.getAssetLocationCountryCode();
    		String assetLocationStateCode = capDetailExt.getAssetLocationStateCode();
    		String assetLocationStreetAddress = capDetailExt.getAssetLocationStreetAddress();
    		String assetLocationZipCode = capDetailExt.getAssetLocationZipCode();
            

            // Room is not required for non-moveable
            AssetType assetType = getAssetType(capitalAssetInformation.getCapitalAssetTypeCode());

            if (StringUtils.isBlank(assetLocationCityName) && StringUtils.isBlank(assetLocationStateCode) && StringUtils.isBlank(assetLocationCountryCode) && StringUtils.isBlank(assetLocationStreetAddress) && StringUtils.isBlank(assetLocationZipCode)) {
            	if (StringUtils.isBlank(buildingCd)) {
	                String label = this.getDataDictionaryService().getAttributeLabel(Building.class, KFSPropertyConstants.BUILDING_CODE);
	                GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(errorPathPrefix + "[" + index + "]" + "." + KFSPropertyConstants.BUILDING_CODE, KFSKeyConstants.ERROR_REQUIRED, label);
	                valid = false;
	            }
                // Room is not required for non-moveable
               if (ObjectUtils.isNull(assetType) || assetType.isMovingIndicator()) {
            	    if (StringUtils.isBlank(roomCd)) {
                        String label = this.getDataDictionaryService().getAttributeLabel(Room.class, KFSPropertyConstants.BUILDING_ROOM_NUMBER);
                        GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(errorPathPrefix + "[" + index + "]" + "." + KFSPropertyConstants.BUILDING_ROOM_NUMBER, KFSKeyConstants.ERROR_REQUIRED, label);
                        valid = false;
                    }
                }
               // Room number not allowed for non-moveable assets
               if (ObjectUtils.isNotNull(assetType) && !assetType.isMovingIndicator()) {
                   if (StringUtils.isNotBlank(dtl.getBuildingRoomNumber())) {
                       String label = this.getDataDictionaryService().getAttributeLabel(Room.class, KFSPropertyConstants.BUILDING_ROOM_NUMBER);
                       GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(errorPathPrefix + "[" + index + "]" + "." + KFSPropertyConstants.BUILDING_ROOM_NUMBER, CamsKeyConstants.AssetLocation.ERROR_ASSET_LOCATION_ROOM_NUMBER_NONMOVEABLE, label);
                       valid = false;
                   }
               }

            }
            
            if(StringUtils.isNotBlank(assetLocationCityName) || StringUtils.isNotBlank(assetLocationStateCode) || StringUtils.isNotBlank(assetLocationCountryCode) || StringUtils.isNotBlank(assetLocationStreetAddress) || StringUtils.isNotBlank(assetLocationZipCode)) {
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

    protected boolean validateAssetTagLocationLines(CapitalAssetInformation capitalAssetInformation, int capitalAssetIndex, AccountingDocument accountingDocument) {
        boolean valid = true;
        CapitalAssetEditable capitalAssetEditable = (CapitalAssetEditable) accountingDocument;
        List<CapitalAssetInformation> capitalAssets = capitalAssetEditable.getCapitalAssetInformation();

        List<CapitalAssetInformationDetail> capitalAssetInformationDetails = capitalAssetInformation.getCapitalAssetInformationDetails();
        int index = 0;

        for (CapitalAssetInformationDetail dtl : capitalAssetInformationDetails) {
        	CapitalAssetInformationDetailExtendedAttribute capDetailExt = (CapitalAssetInformationDetailExtendedAttribute) dtl.getExtension();

            
        	String assetLocationCityName = capDetailExt.getAssetLocationCityName(); 
    		String assetLocationCountryCode = capDetailExt.getAssetLocationCountryCode();
    		String assetLocationStateCode = capDetailExt.getAssetLocationStateCode();
    		String assetLocationStreetAddress = capDetailExt.getAssetLocationStreetAddress();
    		String assetLocationZipCode = capDetailExt.getAssetLocationZipCode();
            // We have to explicitly call this DD service to upper case each field. This may not be the best place and maybe form
            // populate is a better place but we CAMS team don't own FP document. This is the best we can do for now.
            businessObjectDictionaryService.performForceUppercase(dtl);
            String errorPathPrefix = KFSPropertyConstants.DOCUMENT + "." + KFSPropertyConstants.CAPITAL_ASSET_INFORMATION + "[" + capitalAssetIndex + "]." + KFSPropertyConstants.CAPITAL_ASSET_INFORMATION_DETAILS;
            if (StringUtils.isNotBlank(dtl.getCampusCode())) {
                Campus campus = campusService.getCampus(dtl.getCampusCode());
                if (ObjectUtils.isNull(campus)) {
                    valid = false;
                    String label = this.getDataDictionaryService().getAttributeLabel(Campus.class, KFSPropertyConstants.CODE);
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(errorPathPrefix + "[" + index + "]" + "." + KFSPropertyConstants.CAMPUS_CODE, KFSKeyConstants.ERROR_EXISTENCE, label);
                }
            }
            Map<String, String> params;
            params = new HashMap<String, String>();
            params.put(KFSPropertyConstants.CAMPUS_CODE, dtl.getCampusCode());
            params.put(KFSPropertyConstants.BUILDING_CODE, dtl.getBuildingCode());
            Building building = businessObjectService.findByPrimaryKey(Building.class, params);
            if (StringUtils.isBlank(assetLocationCityName) && StringUtils.isBlank(assetLocationStateCode) && StringUtils.isBlank(assetLocationCountryCode) && StringUtils.isBlank(assetLocationStreetAddress) && StringUtils.isBlank(assetLocationZipCode)) {
                if (ObjectUtils.isNull(building)) {
                    valid = false;
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(errorPathPrefix + "[" + index + "]" + "." + KFSPropertyConstants.BUILDING_CODE, CamsKeyConstants.AssetLocationGlobal.ERROR_INVALID_BUILDING_CODE, dtl.getBuildingCode(), dtl.getCampusCode());
                }
            }

            params = new HashMap<String, String>();
            params.put(KFSPropertyConstants.CAMPUS_CODE, dtl.getCampusCode());
            params.put(KFSPropertyConstants.BUILDING_CODE, dtl.getBuildingCode());
            params.put(KFSPropertyConstants.BUILDING_ROOM_NUMBER, dtl.getBuildingRoomNumber());
            Room room = businessObjectService.findByPrimaryKey(Room.class, params);
            AssetType assetType = getAssetType(capitalAssetInformation.getCapitalAssetTypeCode());
            if (StringUtils.isBlank(assetLocationCityName) && StringUtils.isBlank(assetLocationStateCode) && StringUtils.isBlank(assetLocationCountryCode) && StringUtils.isBlank(assetLocationStreetAddress) && StringUtils.isBlank(assetLocationZipCode)) {
                if (ObjectUtils.isNull(room) && (ObjectUtils.isNull(assetType) || assetType.isMovingIndicator())) {
                    valid = false;
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(errorPathPrefix + "[" + index + "]" + "." + KFSPropertyConstants.BUILDING_ROOM_NUMBER, CamsKeyConstants.AssetLocationGlobal.ERROR_INVALID_ROOM_NUMBER, dtl.getBuildingRoomNumber(), dtl.getBuildingCode(), dtl.getCampusCode());
                }
            }
            index++;
        }

        return valid;
    }

 
}
