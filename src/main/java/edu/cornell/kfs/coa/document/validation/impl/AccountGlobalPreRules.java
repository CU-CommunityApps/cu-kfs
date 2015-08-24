package edu.cornell.kfs.coa.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.SubFundGroup;
import org.kuali.kfs.coa.document.validation.impl.MaintenancePreRulesBase;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.coa.businessobject.CuAccountGlobal;

public class AccountGlobalPreRules extends MaintenancePreRulesBase {
	
    protected boolean doCustomPreRules(MaintenanceDocument maintenanceDocument) {
        CuAccountGlobal accountGlobal = (CuAccountGlobal) maintenanceDocument.getNewMaintainableObject().getBusinessObject();
        checkForDefaultSubFundGroupStatus(accountGlobal);
        

        return true;
    }
    
    protected void checkForDefaultSubFundGroupStatus(CuAccountGlobal accountGlobal) {
        String restrictedStatusCode = "";

        // if subFundGroupCode was not entered, then we have nothing
        // to do here, so exit
        if (ObjectUtils.isNull(accountGlobal.getSubFundGroup()) || StringUtils.isBlank(accountGlobal.getSubFundGroupCode())) {
            return;
        }
        SubFundGroup subFundGroup = accountGlobal.getSubFundGroup();

        // KULCOA-1112 : if the sub fund group has a restriction code, override whatever the user selected
        if (StringUtils.isNotBlank(subFundGroup.getAccountRestrictedStatusCode())) {
            restrictedStatusCode = subFundGroup.getAccountRestrictedStatusCode().trim();
            String subFundGroupCd = subFundGroup.getSubFundGroupCode();
            accountGlobal.setAccountRestrictedStatusCode(restrictedStatusCode);
        }

    }

}
