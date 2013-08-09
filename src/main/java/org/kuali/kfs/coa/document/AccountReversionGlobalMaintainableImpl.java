/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.kfs.coa.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.kuali.kfs.coa.businessobject.AccountReversion;
import org.kuali.kfs.coa.businessobject.AccountReversionGlobal;
import org.kuali.kfs.coa.businessobject.AccountReversionGlobalAccount;
import org.kuali.kfs.coa.businessobject.AccountReversionGlobalDetail;
import org.kuali.kfs.coa.businessobject.OrganizationReversion;
import org.kuali.kfs.coa.businessobject.OrganizationReversionCategory;
import org.kuali.kfs.coa.businessobject.OrganizationReversionGlobal;
import org.kuali.kfs.coa.businessobject.OrganizationReversionGlobalDetail;
import org.kuali.kfs.coa.businessobject.ReversionCategory;
import org.kuali.kfs.coa.service.AccountReversionService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemGlobalMaintainable;
import org.kuali.rice.krad.bo.PersistableBusinessObject;
import org.kuali.rice.krad.maintenance.MaintenanceLock;

/**
 * This class provides some specific functionality for the {@link AccountReversionGlobal} maintenance document inner class for
 * doing comparisons on {@link AccounReversionCategory} generateMaintenanceLocks - generates the appropriate maintenance locks
 * on {@link AccountReversion} setBusinessObject - populates the {@link AccountReversionGlobalDetail}s
 * isRelationshipRefreshable - makes sure that {@code accountReversionGlobalDetails} isn't wiped out accidentally
 * processGlobalsAfterRetrieve - provides special handling for the details (which aren't a true collection)
 */
public class AccountReversionGlobalMaintainableImpl extends FinancialSystemGlobalMaintainable {
    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AccountReversionGlobalMaintainableImpl.class);

    /**
     * This class is an inner class for comparing two {@link OrganizationReversionCategory}s
     */
    private class CategoryComparator implements Comparator<AccountReversionGlobalDetail> {
        public int compare(AccountReversionGlobalDetail detailA, AccountReversionGlobalDetail detailB) {
            ReversionCategory categoryA = detailA.getReversionCategory();
            ReversionCategory categoryB = detailB.getReversionCategory();

            String code0 = categoryA.getReversionCategoryCode();
            String code1 = categoryB.getReversionCategoryCode();

            return code0.compareTo(code1);
        }
    }

    /**
     * This implementation locks all account reversions that would be accessed by this global account reversion. It does
     * not lock any AccountReversionDetail objects, as we expect that those will be inaccessible
     * 
     * @see org.kuali.rice.kns.maintenance.KualiGlobalMaintainableImpl#generateMaintenaceLocks()
     */
    @Override
    public List<MaintenanceLock> generateMaintenanceLocks() {
        List<MaintenanceLock> locks = new ArrayList<MaintenanceLock>();
        AccountReversionGlobal globalAcctRev = (AccountReversionGlobal) this.getBusinessObject();
        if (globalAcctRev.getUniversityFiscalYear() != null && globalAcctRev.getAccountReversionGlobalAccounts() != null && globalAcctRev.getAccountReversionGlobalAccounts().size() > 0) { // only generate locks if we're going to have primary keys
            for (AccountReversionGlobalAccount acctRevAcct : globalAcctRev.getAccountReversionGlobalAccounts()) {
                MaintenanceLock maintenanceLock = new MaintenanceLock();
                maintenanceLock.setDocumentNumber(globalAcctRev.getDocumentNumber());

                StringBuffer lockRep = new StringBuffer();
                lockRep.append(AccountReversion.class.getName());
                lockRep.append(KFSConstants.Maintenance.AFTER_CLASS_DELIM);
                lockRep.append("chartOfAccountsCode");
                lockRep.append(KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
                lockRep.append(acctRevAcct.getChartOfAccountsCode());
                lockRep.append(KFSConstants.Maintenance.AFTER_VALUE_DELIM);
                lockRep.append("universityFiscalYear");
                lockRep.append(KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
                lockRep.append(globalAcctRev.getUniversityFiscalYear().toString());
                lockRep.append(KFSConstants.Maintenance.AFTER_VALUE_DELIM);
                lockRep.append("accountNumber");
                lockRep.append(KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
                lockRep.append(acctRevAcct.getAccountNumber());
                lockRep.append(KFSConstants.Maintenance.AFTER_VALUE_DELIM);

                maintenanceLock.setLockingRepresentation(lockRep.toString());
                locks.add(maintenanceLock);
            }
        }

        return locks;
    }

    /**
     * Just like AccountReversionMaintainableImpl's setBusinessObject method populates the list of details so there is one
     * detail per active Account Reversion Category, this method populates a list of Account Reversion Change details.
     * 
     * @see org.kuali.rice.kns.maintenance.KualiMaintainableImpl#setBusinessObject(org.kuali.rice.kns.bo.PersistableBusinessObject)
     */
    @Override
    public void setBusinessObject(PersistableBusinessObject businessObject) {
        super.setBusinessObject(businessObject);
        AccountReversionService accountReversionService = SpringContext.getBean(AccountReversionService.class);
        AccountReversionGlobal globalAcctRev = (AccountReversionGlobal) businessObject;
        List<AccountReversionGlobalDetail> details = globalAcctRev.getAccountReversionGlobalDetails();
        LOG.debug("Details size before adding categories = " + details.size());

        if (details == null) {
            details = new ArrayList<AccountReversionGlobalDetail>();
            globalAcctRev.setAccountReversionGlobalDetails(details);
        }

        if (details.size() == 0) {

            Collection<ReversionCategory> categories = accountReversionService.getCategoryList();
            for (ReversionCategory category : categories) {
                if (category.isActive()) {
                    AccountReversionGlobalDetail detail = new AccountReversionGlobalDetail();
                    detail.setAccountReversionCategoryCode(category.getReversionCategoryCode());
                    detail.setReversionCategory(category);
                    detail.setParentGlobalAccountReversion(globalAcctRev);
                    details.add(detail);
                }
            }
            LOG.debug("Details size after adding categories = " + details.size());
            Collections.sort(details, new CategoryComparator());
        }
        super.setBusinessObject(businessObject);
    }

    /**
     * Prevents Account Reversion Change Details from being refreshed by a look up (because doing that refresh before a save
     * would wipe out the list of account reversion change details).
     * 
     * @see org.kuali.rice.kns.maintenance.KualiMaintainableImpl#isRelationshipRefreshable(java.lang.Class, java.lang.String)
     */
    @Override
    protected boolean isRelationshipRefreshable(Class boClass, String relationshipName) {
        if (relationshipName.equals("accountReversionGlobalDetails")) {
            return false;
        }
        else {
            return super.isRelationshipRefreshable(boClass, relationshipName);
        }
    }

    /**
     * The account reversion detail collection does not behave like a true collection (no add lines). The records on the collection
     * should not have the delete option.
     * 
     * @see org.kuali.rice.kns.maintenance.KualiGlobalMaintainableImpl#processGlobalsAfterRetrieve()
     */
    @Override
    protected void processGlobalsAfterRetrieve() {
        super.processGlobalsAfterRetrieve();
        for (AccountReversionGlobalDetail changeDetail : ((AccountReversionGlobal) businessObject).getAccountReversionGlobalDetails()) {
            changeDetail.setNewCollectionRecord(false);
        }
    }

    @Override
    public Class<? extends PersistableBusinessObject> getPrimaryEditedBusinessObjectClass() {
        return AccountReversion.class;
    }
}
