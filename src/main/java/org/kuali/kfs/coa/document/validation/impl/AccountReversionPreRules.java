/*
 * Copyright 2006 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.coa.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.AccountReversion;
import org.kuali.kfs.coa.businessobject.AccountReversionDetail;
import org.kuali.kfs.coa.businessobject.OrganizationReversion;
import org.kuali.rice.kns.document.MaintenanceDocument;

/**
 * PreRules checks for the {@link OrganizationReversion} that needs to occur while still in the Struts processing. This includes defaults
 */
public class AccountReversionPreRules extends MaintenancePreRulesBase {


    public AccountReversionPreRules() {

    }

    /**
     * This calls the {@link AccountReversionPreRules#copyKeyAttributesToDetail(OrganizationReversion)}
     * @see org.kuali.kfs.coa.document.validation.impl.MaintenancePreRulesBase#doCustomPreRules(org.kuali.rice.kns.document.MaintenanceDocument)
     */
    @Override
    protected boolean doCustomPreRules(MaintenanceDocument document) {

        AccountReversion acctRev = (AccountReversion) document.getNewMaintainableObject().getBusinessObject();
        // copy year and chart to detail records
        copyKeyAttributesToDetail(acctRev);

        return true;
    }

    /**
     * 
     * This copies the chart of accounts, and the fiscal year from the parent {@link AccountReversion} to the 
     * {@link AccountReversionDetail} objects and refreshes the reference object on them if the values have 
     * been filled out
     * @param orgRev
     */
    protected void copyKeyAttributesToDetail(AccountReversion acctRev) {
        if (acctRev.getUniversityFiscalYear() != null && acctRev.getUniversityFiscalYear().intValue() != 0 && StringUtils.isNotBlank(acctRev.getChartOfAccountsCode())) {
            // loop over detail records, copying their details
            for (AccountReversionDetail dtl : acctRev.getAccountReversionDetails()) {
                dtl.setChartOfAccountsCode(acctRev.getChartOfAccountsCode());
                dtl.setUniversityFiscalYear(acctRev.getUniversityFiscalYear());
                // load the object, if possible
                if (StringUtils.isNotBlank(dtl.getReversionObjectCode())) {
                    dtl.refreshReferenceObject("reversionObject");
                }
            }
        }

    }

}
