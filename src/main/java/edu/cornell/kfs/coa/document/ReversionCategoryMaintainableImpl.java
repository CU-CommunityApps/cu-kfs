/*
 * Copyright 2009 The Kuali Foundation
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
package edu.cornell.kfs.coa.document;

import java.util.HashMap;
import java.util.Map;


import org.kuali.kfs.coa.service.OrganizationReversionDetailTrickleDownInactivationService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.coa.businessobject.ReversionCategory;


/**
 * A Maintainable for the  Reversion Category maintenance document
 */
public class ReversionCategoryMaintainableImpl extends FinancialSystemMaintainable {

    /**
     * Determines if this maint doc is inactivating an  reversion category
     * @return true if the document is inactivating an active  reversion category, false otherwise
     */
    protected boolean isInactivatingReversionCategory() {
        // the account has to be closed on the new side when editing in order for it to be possible that we are closing the account
        if (KRADConstants.MAINTENANCE_EDIT_ACTION.equals(getMaintenanceAction()) && !((ReversionCategory) getBusinessObject()).isActive()) {
            ReversionCategory existingReversionCategoryFromDB = retrieveExistingReversionCategory();
            if (ObjectUtils.isNotNull(existingReversionCategoryFromDB)) {
                // now see if the original account was not closed, in which case, we are closing the account
                if (existingReversionCategoryFromDB.isActive()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Determines if this maint doc is activating an reversion category
     * @return true if the document is activating an inactive reversion category, false otherwise
     */
    protected boolean isActivatingReversionCategory() {
        // the account has to be closed on the new side when editing in order for it to be possible that we are closing the account
        if (KRADConstants.MAINTENANCE_EDIT_ACTION.equals(getMaintenanceAction()) && ((ReversionCategory) getBusinessObject()).isActive()) {
            ReversionCategory existingReversionCategoryFromDB = retrieveExistingReversionCategory();
            if (ObjectUtils.isNotNull(existingReversionCategoryFromDB)) {
                // now see if the original account was not closed, in which case, we are closing the account
                if (!existingReversionCategoryFromDB.isActive()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Grabs the old version of this reversion category from the database
     * @return the old version of this reversion category
     */
    protected ReversionCategory retrieveExistingReversionCategory() {
        final ReversionCategory orgRevCategory = (ReversionCategory)getBusinessObject();
        Map<String, Object> pkMap = new HashMap<String, Object>();
        pkMap.put("reversionCategoryCode", ((ReversionCategory)getBusinessObject()).getReversionCategoryCode());
        final ReversionCategory oldRevCategory = (ReversionCategory)SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(ReversionCategory.class, pkMap);
        return oldRevCategory;
    }

    /**
     * Overridden to trickle down inactivation or activation to details
     * @see org.kuali.kfs.kns.maintenance.MaintainableImpl#saveBusinessObject()
     */
    @Override
    public void saveBusinessObject() {
        final boolean isActivatingOrgReversionCategory = isActivatingReversionCategory();
        final boolean isInactivatingOrgReversionCategory = isInactivatingReversionCategory();
        
        super.saveBusinessObject();
        
//        if (isActivatingOrgReversionCategory) {
//            SpringContext.getBean(OrganizationReversionDetailTrickleDownInactivationService.class).trickleDownActiveOrganizationReversionDetails((ReversionCategory)getBusinessObject(), documentNumber);
//        } else if (isInactivatingOrgReversionCategory) {
//            SpringContext.getBean(OrganizationReversionDetailTrickleDownInactivationService.class).trickleDownInactiveOrganizationReversionDetails((ReversionCategory)getBusinessObject(), documentNumber);
//        }
    }

}
