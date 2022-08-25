package edu.cornell.kfs.sys.document.validation.impl;

import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.businessobject.ISOFIPSCountryMap;
import edu.cornell.kfs.sys.service.CountryService;
import edu.cornell.kfs.sys.service.ISOCountryService;

/**
 * CU Generic ISO-FIPS Country modification
 */
public class ISOFIPSCountryMapRule extends MaintenanceDocumentRuleBase {

    protected ISOFIPSCountryMap oldISOFIPSCountryMap;
    protected ISOFIPSCountryMap newISOFIPSCountryMap;

    @Override
    public void setupConvenienceObjects() {
        oldISOFIPSCountryMap = (ISOFIPSCountryMap) super.getOldBo();
        newISOFIPSCountryMap = (ISOFIPSCountryMap) super.getNewBo();
    }

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        boolean isValid = super.processCustomRouteDocumentBusinessRules(document);
        MessageMap errorMap = GlobalVariables.getMessageMap();

        isValid &= !errorMap.hasErrors();

        if (isValid) {
            setupConvenienceObjects();
            isValid = processActiveIndicatorBusinessRules(newISOFIPSCountryMap);
        }

        return isValid;
    }

    /**
     * Prevent an Active mapping from being created if either country is inactive in their respective table.
     * Logic written so all failures/error messages are presented to user at once.
     * @param newISOFIPSCountryMap
     * @return
     */
    protected boolean processActiveIndicatorBusinessRules(ISOFIPSCountryMap newISOFIPSCountryMap) {
        boolean isValid = true;
        boolean requestToMakeMapActive = newISOFIPSCountryMap.isActive();

        boolean isoCountryIsNotActive = !SpringContext.getBean(ISOCountryService.class).isISOCountryActive(newISOFIPSCountryMap.getIsoCountryCode());
        boolean fipsCountryIsNotActive = !SpringContext.getBean(CountryService.class).isCountryActive(newISOFIPSCountryMap.getFipsCountryCode());

        if (requestToMakeMapActive) {
            if (isoCountryIsNotActive) {
                GlobalVariables.getMessageMap().putError(KFSConstants.MAINTENANCE_NEW_MAINTAINABLE +
                        CUKFSPropertyConstants.ISOFIPSCountryMap.ISO_COUNTRY_CODE,
                        CUKFSKeyConstants.ISOFIPSCountryMapConstants.ACTIVE_MAP_ERROR_COUNTRY_INACTIVE, CUKFSConstants.ISO, newISOFIPSCountryMap.getIsoCountryCode());
                isValid = false;
            }
            if (fipsCountryIsNotActive) {
                GlobalVariables.getMessageMap().putError(KFSConstants.MAINTENANCE_NEW_MAINTAINABLE +
                        CUKFSPropertyConstants.ISOFIPSCountryMap.FIPS_COUNTRY_CODE,
                        CUKFSKeyConstants.ISOFIPSCountryMapConstants.ACTIVE_MAP_ERROR_COUNTRY_INACTIVE, CUKFSConstants.FIPS, newISOFIPSCountryMap.getFipsCountryCode());
                isValid = false;
            }
        }
        return isValid;
    }

}
