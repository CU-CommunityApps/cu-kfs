package edu.cornell.kfs.sys.document.validation.impl;

import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.krad.util.ObjectUtils;
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
    private ISOCountryService isoCountryService = null;
    private CountryService countryService = null;

    @Override
    public void setupConvenienceObjects() {
        oldISOFIPSCountryMap = (ISOFIPSCountryMap) super.getOldBo();
        newISOFIPSCountryMap = (ISOFIPSCountryMap) super.getNewBo();
    }

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        boolean isValid = super.processCustomRouteDocumentBusinessRules(document);
        setupConvenienceObjects();
        isValid &= processActiveIndicatorBusinessRules(newISOFIPSCountryMap);
        return isValid;
    }

    /**
     * Prevent an Active mapping if either country is inactive in their respective table.
     * Logic written so all failures/error messages are presented to user at once.
     * @param newISOFIPSCountryMap
     * @return
     */
    protected boolean processActiveIndicatorBusinessRules(ISOFIPSCountryMap newISOFIPSCountryMap) {
        boolean isValid = true;
        boolean requestToMakeMapActive = newISOFIPSCountryMap.isActive();

        boolean isoCountryDoesNotExist = !getIsoCountryService().isoCountryExists(newISOFIPSCountryMap.getIsoCountryCode());
        boolean fipsCountryDoesNotExist = !getCountryService().countryExists(newISOFIPSCountryMap.getFipsCountryCode());
 
        
        boolean isoCountryIsNotActive = !getIsoCountryService().isISOCountryActive(newISOFIPSCountryMap.getIsoCountryCode());
        boolean fipsCountryIsNotActive = !getCountryService().isCountryActive(newISOFIPSCountryMap.getFipsCountryCode());

        if (requestToMakeMapActive) {
            if (isoCountryDoesNotExist) {
                GlobalVariables.getMessageMap().putError(KFSConstants.MAINTENANCE_NEW_MAINTAINABLE +
                        CUKFSPropertyConstants.ISOFIPSCountryMap.ISO_COUNTRY_CODE,
                        CUKFSKeyConstants.ACTIVE_MAP_ERROR_COUNTRY_DOES_NOT_EXIST, CUKFSConstants.ISO, newISOFIPSCountryMap.getIsoCountryCode());
                isValid = false;
            } else if (isoCountryIsNotActive) {
                GlobalVariables.getMessageMap().putError(KFSConstants.MAINTENANCE_NEW_MAINTAINABLE +
                        CUKFSPropertyConstants.ISOFIPSCountryMap.ISO_COUNTRY_CODE,
                        CUKFSKeyConstants.ACTIVE_MAP_ERROR_COUNTRY_INACTIVE, CUKFSConstants.ISO, newISOFIPSCountryMap.getIsoCountryCode());
                isValid = false;
            }
            if (fipsCountryDoesNotExist) {
                GlobalVariables.getMessageMap().putError(KFSConstants.MAINTENANCE_NEW_MAINTAINABLE +
                        CUKFSPropertyConstants.ISOFIPSCountryMap.FIPS_COUNTRY_CODE,
                        CUKFSKeyConstants.ACTIVE_MAP_ERROR_COUNTRY_DOES_NOT_EXIST, CUKFSConstants.FIPS, newISOFIPSCountryMap.getFipsCountryCode());
                isValid = false;
            } else if (fipsCountryIsNotActive) {
                GlobalVariables.getMessageMap().putError(KFSConstants.MAINTENANCE_NEW_MAINTAINABLE +
                        CUKFSPropertyConstants.ISOFIPSCountryMap.FIPS_COUNTRY_CODE,
                        CUKFSKeyConstants.ACTIVE_MAP_ERROR_COUNTRY_INACTIVE, CUKFSConstants.FIPS, newISOFIPSCountryMap.getFipsCountryCode());
                isValid = false;
            }
        }
        return isValid;
    }

    public ISOCountryService getIsoCountryService() {
        if (ObjectUtils.isNull(isoCountryService)) {
            this.isoCountryService = SpringContext.getBean(ISOCountryService.class);
        } 
        return isoCountryService;
    }

    public CountryService getCountryService() {
        if (ObjectUtils.isNull(countryService)) {
            this.countryService = SpringContext.getBean(CountryService.class);
        } 
        return countryService;
    }

}
