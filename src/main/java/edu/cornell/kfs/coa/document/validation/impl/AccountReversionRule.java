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
package edu.cornell.kfs.coa.document.validation.impl;

import java.util.List;

import org.kuali.kfs.coa.businessobject.OrganizationReversion;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.coa.businessobject.AccountReversion;
import edu.cornell.kfs.coa.businessobject.AccountReversionDetail;

/**
 * This class implements the business rules specific to the {@link OrganizationReversion} Maintenance Document.
 */
public class AccountReversionRule extends MaintenanceDocumentRuleBase {

    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AccountReversionRule.class);

    protected AccountReversion oldAcctReversion;
    protected AccountReversion newAcctReversion;

    /**
     * No-Args Constructor for an AccountReversionRule.
     */
    public AccountReversionRule() {

    }

    /**
     * This performs rules checks on document route
     * <ul>
     * <li>{@link AccountReversionRule#validateDetailBusinessObjects(OrganizationReversion)}</li>
     * </ul>
     * This rule fails on business rule failures
     * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomRouteDocumentBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument)
     */
    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {

        boolean success = true;

        // make sure its a valid organization reversion MaintenanceDocument
        if (!isCorrectMaintenanceClass(document, AccountReversion.class)) {
            throw new IllegalArgumentException("Maintenance Document passed in was of the incorrect type.  Expected " + "'" + AccountReversion.class.toString() + "', received " + "'" + document.getOldMaintainableObject().getBoClass().toString() + "'.");
        }

        // get the real business object
        newAcctReversion = (AccountReversion) document.getNewMaintainableObject().getBusinessObject();

        // add check to validate document recursively to get to the collection attributes
        success &= validateDetailBusinessObjects(newAcctReversion);

        return success;
    }

    /**
     * Tests each option attached to the main business object and validates its properties.
     * 
     * @param orgReversion
     * @return false if any of the detail objects fail with their validation
     */
    protected boolean validateDetailBusinessObjects(AccountReversion orgReversion) {
        GlobalVariables.getMessageMap().addToErrorPath("document.newMaintainableObject");
        List<AccountReversionDetail> details = orgReversion.getAccountReversionDetails();
        int index = 0;
        int originalErrorCount = GlobalVariables.getMessageMap().getErrorCount();
        for (AccountReversionDetail dtl : details) {
            String errorPath = "accountReversionDetails[" + index + "]";
            GlobalVariables.getMessageMap().addToErrorPath(errorPath);
            validateAccountReversionDetail(dtl);
            GlobalVariables.getMessageMap().removeFromErrorPath(errorPath);
            index++;
        }
        GlobalVariables.getMessageMap().removeFromErrorPath("document.newMaintainableObject");
        return GlobalVariables.getMessageMap().getErrorCount() == originalErrorCount;
    }

    /**
     * 
     * This checks to make sure that the organization reversion object on the detail object actually exists
     * @param detail
     * @return false if the organization reversion object doesn't exist
     */
    protected boolean validateAccountReversionDetail(AccountReversionDetail detail) {
        boolean result = true; // let's assume this detail will pass the rule
        // 1. makes sure the financial object code exists
        detail.refreshReferenceObject("reversionObject");
        LOG.debug("reversion finanical object = " + detail.getReversionObject().getName());
        if (ObjectUtils.isNull(detail.getReversionObject())) {
            result = false;
            GlobalVariables.getMessageMap().putError("accountReversionObjectCode", KFSKeyConstants.ERROR_EXISTENCE, new String[] { "Financial Object Code: " + detail.getReversionObjectCode() });
        }
        return result;
    }

}

