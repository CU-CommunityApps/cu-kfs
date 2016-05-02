package edu.cornell.kfs.module.cam.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.cam.CamsKeyConstants;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.cam.businessobject.BarcodeInventoryErrorDetail;
import org.kuali.kfs.module.cam.document.validation.impl.BarcodeInventoryErrorDocumentRule;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.kns.service.DataDictionaryService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.rice.location.api.campus.Campus;
import org.kuali.rice.location.api.campus.CampusService;

/**
 * Custom subclass of BarcodeInventoryErrorDocumentRule that fixes the campus code
 * validation to prevent passing a blank campus code to the campus service, which
 * can cause an exception to be thrown.
 */
@SuppressWarnings("deprecation")
public class CuBarcodeInventoryErrorDocumentRule extends BarcodeInventoryErrorDocumentRule {

    /**
     * Overridden to avoid calling the CampusService if the campus code is blank,
     * and to instead automatically treat a blank campus code as a validation failure.
     * 
     * @see org.kuali.kfs.module.cam.document.validation.impl.BarcodeInventoryErrorDocumentRule#validateCampusCode(String, BarcodeInventoryErrorDetail)
     */
    @Override
    protected boolean validateCampusCode(String campusCode, BarcodeInventoryErrorDetail detail) {
        boolean result = true;
        String label = SpringContext.getBean(DataDictionaryService.class).getDataDictionary().getBusinessObjectEntry(BarcodeInventoryErrorDetail.class.getName()).getAttributeDefinition(CamsPropertyConstants.BarcodeInventory.CAMPUS_CODE).getLabel();

        // ==== CU Customization: Do not call the CampusService if the campus code is blank. ====
        Campus campus = null;
        //HashMap<String, Object> fields = new HashMap<String, Object>();
        //fields.put(KFSPropertyConstants.CAMPUS_CODE, detail.getCampusCode());
        if (StringUtils.isNotBlank(campusCode)) {
            campus = SpringContext.getBean(CampusService.class).getCampus(campusCode/*RICE_20_REFACTORME  fields */);
        }

        if (ObjectUtils.isNull(campus)) {
            GlobalVariables.getMessageMap().putError(CamsPropertyConstants.BarcodeInventory.CAMPUS_CODE, CamsKeyConstants.BarcodeInventory.ERROR_INVALID_FIELD, label);
            result = false;
        }
        else if (!campus.isActive()) {
            GlobalVariables.getMessageMap().putError(CamsPropertyConstants.BarcodeInventory.CAMPUS_CODE, CamsKeyConstants.BarcodeInventory.ERROR_INACTIVE_FIELD, label);
            result &= false;
        }
        return result;
    }

}
