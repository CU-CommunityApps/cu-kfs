package edu.cornell.kfs.coa.document.validation.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.document.validation.impl.GlobalDocumentRuleBase;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;

import edu.cornell.kfs.coa.businessobject.OrganizationGlobal;
import edu.cornell.kfs.coa.businessobject.OrganizationGlobalDetail;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

/**
 * Business rules for the Organization Global document.
 * These rules have been copied and tweaked from those in KFS's OrgRules class.
 */
@SuppressWarnings("deprecation")
public class OrganizationGlobalRule extends GlobalDocumentRuleBase {
	private static final Logger LOG = LogManager.getLogger(OrganizationGlobalRule.class);

    private static final String ORG_MANAGER_PRINCIPAL_NAME = "organizationManagerUniversal.principalName";

    protected OrganizationGlobal orgGlobal;

    @Override
    public void setupConvenienceObjects() {
        orgGlobal = (OrganizationGlobal) super.getNewBo();
    }



    @Override
    protected boolean processCustomApproveDocumentBusinessRules(MaintenanceDocument document) {
        LOG.info("processCustomApproveDocumentBusinessRules called");
        
        boolean success = true;
        
        success &= checkOrganizationManager(document, orgGlobal.getOrganizationManagerUniversal());
        success &= checkPostalCode(document, orgGlobal.getOrganizationZipCode(), orgGlobal.getOrganizationCountryCode());
        success &= checkOrganizationDetails(document, orgGlobal.getOrganizationGlobalDetails());
        
        return success;
    }

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        LOG.info("processCustomRouteDocumentBusinessRules called");
        
        boolean success = true;
        
        success &= checkOrganizationManager(document, orgGlobal.getOrganizationManagerUniversal());
        success &= checkPostalCode(document, orgGlobal.getOrganizationZipCode(), orgGlobal.getOrganizationCountryCode());
        success &= checkOrganizationDetails(document, orgGlobal.getOrganizationGlobalDetails());
        
        return success;
    }



    /**
     * Validates that a valid organization manager was specified, if non-null.
     * The base MaintenanceDocumentDictionaryServiceImpl class does have some logic
     * that could help handle this automatically; however, that particular code
     * only applies to required fields, and the org manager field is optional
     * on the Organization Global document.
     * 
     * @param document The current document.
     * @param orgManager The organization manager.
     * @return true if the organization manager is null or has a blank principal name or references a valid person, false otherwise. 
     */
    protected boolean checkOrganizationManager(MaintenanceDocument document, Person orgManager) {
        if (ObjectUtils.isNotNull(orgManager) && StringUtils.isNotBlank(orgManager.getPrincipalName()) && StringUtils.isBlank(orgManager.getEntityId())) {
            // If org manager has a non-blank principal name but a blank entity ID, then it's not actually tied to a valid principal.
            putFieldError(ORG_MANAGER_PRINCIPAL_NAME, KFSKeyConstants.ERROR_EXISTENCE,
                    getDataDictionaryService().getAttributeLabel(OrganizationGlobal.class, ORG_MANAGER_PRINCIPAL_NAME));
            return false;
        }
        
        return true;
    }

    /**
     * Validates that country code and zip code are either both blank or both non-blank.
     * Since the Organization Global document has the country and zip codes as optional,
     * this check is necessary to prevent invalid updates, due to some of the related
     * auto-validation not applying unless both zip code and country code are given.
     * 
     * @param document The current document.
     * @param postalCode The postal/zip code to check.
     * @param countryCode The country code to check.
     * @return true if postal code and country code are both blank or both non-blank, false otherwise.
     */
    protected boolean checkPostalCode(MaintenanceDocument document, String postalCode, String countryCode) {
        if (StringUtils.isBlank(postalCode) != StringUtils.isBlank(countryCode)) {
            putFieldError(StringUtils.isBlank(postalCode) ? "organizationZipCode" : "organizationCountryCode",
                    CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ORGANIZATION_COUNTRY_AND_ZIP_MISMATCH);
            return false;
        }
        
        return true;
    }

    /**
     * Validates the organization details, similar to how AccountGlobalRule
     * validates its account details.
     * 
     * @param document The current document.
     * @param details The organization details to validate.
     * @return true if validation succeeds, false otherwise.
     */
    protected boolean checkOrganizationDetails(MaintenanceDocument document, List<OrganizationGlobalDetail> details) {
        // This method is a modified copy of a related method on AccountGlobalRule.
        boolean success = true;
        
        if (details.isEmpty()) {
            putFieldError(KFSConstants.MAINTENANCE_ADD_PREFIX + "organizationGlobalDetails.organizationCode",
                    CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ORGANIZATION_NO_ORGANIZATIONS);
            success = false;
            
        } else {
            final int BUILDER_SIZE = 75;
            int i = 0;
            for (OrganizationGlobalDetail detail : details) {
                String errorPath = new StringBuilder(BUILDER_SIZE).append(MAINTAINABLE_ERROR_PREFIX)
                        .append("organizationGlobalDetails[").append(i).append(']').toString();
                GlobalVariables.getMessageMap().addToErrorPath(errorPath);
                success &= checkOrganizationDetail(detail);
                GlobalVariables.getMessageMap().removeFromErrorPath(errorPath);
                i++;
            }
        }
        
        return success;
    }

    /**
     * Validates a single organization detail, similar to how AccountGlobalRule
     * validates its individual account details.
     * 
     * @param detail The organization detail to validate.
     * @return true if validation succeeds, false otherwise.
     */
    protected boolean checkOrganizationDetail(OrganizationGlobalDetail detail) {
        // This method is a modified copy of a related method on AccountGlobalRule.
        boolean success = true;
        int oldErrorCount = GlobalVariables.getMessageMap().getErrorCount();
        getDictionaryValidationService().validateBusinessObject(detail);
        if (StringUtils.isNotBlank(detail.getOrganizationCode()) && StringUtils.isNotBlank(detail.getChartOfAccountsCode())) {
            detail.refreshReferenceObject("organization");
            if (ObjectUtils.isNull(detail.getOrganization())) {
                GlobalVariables.getMessageMap().putError("organizationCode", CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_ORGANIZATION_INVALID_ORGANIZATION,
                        detail.getChartOfAccountsCode(), detail.getOrganizationCode());
            }
        }
        
        success &= GlobalVariables.getMessageMap().getErrorCount() == oldErrorCount;
        
        return success;
    }

}
