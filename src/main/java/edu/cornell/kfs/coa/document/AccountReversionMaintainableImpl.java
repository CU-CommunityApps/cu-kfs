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
package edu.cornell.kfs.coa.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.kns.web.ui.Field;
import org.kuali.kfs.kns.web.ui.Row;
import org.kuali.kfs.kns.web.ui.Section;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.maintenance.MaintenanceUtils;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;

import edu.cornell.kfs.coa.businessobject.AccountReversion;
import edu.cornell.kfs.coa.businessobject.AccountReversionDetail;
import edu.cornell.kfs.coa.businessobject.ReversionCategory;
import edu.cornell.kfs.coa.service.AccountReversionDetailTrickleDownInactivationService;
import edu.cornell.kfs.coa.service.AccountReversionService;

/**
 * This class provides some specific functionality for the {@link AccountReversion} maintenance document inner class for doing
 * comparisons on {@link AccountReversionCategory} populateBusinessObject setBusinessObject - pre-populate the static list of
 * details with each category isRelationshipRefreshable - makes sure that {@code accountReversionGlobalDetails} isn't wiped out
 * accidentally
 */
public class AccountReversionMaintainableImpl extends FinancialSystemMaintainable {
	private transient AccountReversionService accountReversionService;

    /**
     * This comparator is used internally for sorting the list of categories
     */
    private class categoryComparator implements Comparator<AccountReversionDetail> {

        public int compare(AccountReversionDetail detail0, AccountReversionDetail detail1) {

            ReversionCategory category0 = detail0.getReversionCategory();
            ReversionCategory category1 = detail1.getReversionCategory();

            String code0 = category0.getReversionCategoryCode();
            String code1 = category1.getReversionCategoryCode();

            return code0.compareTo(code1);
        }

    }
    
    @Override
    public void processAfterNew(MaintenanceDocument document, Map<String, String[]> requestParameters) {
        super.processAfterNew(document, requestParameters);

        AccountReversion accountReversion = (AccountReversion) getBusinessObject();
        List<AccountReversionDetail> details = accountReversion.getAccountReversionDetails();

        if (details == null) {
            details = new ArrayList<AccountReversionDetail>();
            accountReversion.setAccountReversionDetails(details);
        }

        if (details.size() == 0) {

            Collection<ReversionCategory> categories = SpringContext.getBean(AccountReversionService.class).getCategoryList();

            for (ReversionCategory category : categories) {
                if (category.isActive()) {
                    AccountReversionDetail detail = new AccountReversionDetail();
                    detail.setAccountReversionCategoryCode(category.getReversionCategoryCode());
                    detail.setReversionCategory(category);
                    details.add(detail);
                }
            }

            Collections.sort(details, new categoryComparator());
        }
    }

    /**
     * pre-populate the static list of details with each category
     * 
     * @see org.kuali.kfs.kns.maintenance.MaintainableImpl#setBusinessObject(org.kuali.kfs.kns.bo.BusinessObject)
     */
    public void setBusinessObject(PersistableBusinessObject businessObject) {

        AccountReversionService accountReversionService = SpringContext.getBean(AccountReversionService.class);
        AccountReversion accountReversion = (AccountReversion) businessObject;
        List<AccountReversionDetail> details = accountReversion.getAccountReversionDetails();

        if (details == null) {
            details = new ArrayList<AccountReversionDetail>();
            accountReversion.setAccountReversionDetails(details);
        }

        if (details.size() == 0) {

            Collection<ReversionCategory> categories = accountReversionService.getCategoryList();

            for (ReversionCategory category : categories) {
                if (category.isActive()) {
                    AccountReversionDetail detail = new AccountReversionDetail();
                    detail.setAccountReversionCategoryCode(category.getReversionCategoryCode());
                    detail.setReversionCategory(category);
                    details.add(detail);
                }
            }

            Collections.sort(details, new categoryComparator());

        }

        super.setBusinessObject(businessObject);
    }

    /**
     * A method that prevents lookups from refreshing the Organization Reversion Detail list (because, if it is refreshed before a
     * save...it ends up destroying the list).
     * 
     * @see org.kuali.kfs.kns.maintenance.MaintainableImpl#isRelationshipRefreshable(java.lang.Class, java.lang.String)
     */
    @Override
    protected boolean isRelationshipRefreshable(Class boClass, String relationshipName) {
        if (relationshipName.equals("accountReversionDetails")) {
            return false;
        }
        else {
            return super.isRelationshipRefreshable(boClass, relationshipName);
        }
    }
    
    /**
     * Determines if this maint doc is inactivating an organization reversion
     * @return true if the document is inactivating an active organization reversion, false otherwise
     */
    protected boolean isInactivatingAccountReversion() {
        // the account has to be closed on the new side when editing in order for it to be possible that we are closing the account
        if (KRADConstants.MAINTENANCE_EDIT_ACTION.equals(getMaintenanceAction()) && !((AccountReversion) getBusinessObject()).isActive()) {
            AccountReversion existingAccountReversionFromDB = retrieveExistingAccountReversion();
            if (ObjectUtils.isNotNull(existingAccountReversionFromDB)) {
                // now see if the original account was not closed, in which case, we are closing the account
                if (existingAccountReversionFromDB.isActive()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Determines if this maint doc is activating an organization reversion
     * @return true if the document is activating an inactive organization reversion, false otherwise
     */
    protected boolean isActivatingAccountReversion() {
        // the account has to be closed on the new side when editing in order for it to be possible that we are closing the account
        if (KRADConstants.MAINTENANCE_EDIT_ACTION.equals(getMaintenanceAction()) && ((AccountReversion) getBusinessObject()).isActive()) {
            AccountReversion existingAccountReversionFromDB = retrieveExistingAccountReversion();
            if (ObjectUtils.isNotNull(existingAccountReversionFromDB)) {
                // now see if the original account was not closed, in which case, we are closing the account
                if (!existingAccountReversionFromDB.isActive()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Grabs the old version of this org reversion from the database
     * @return the old version of this organization reversion
     */
    protected AccountReversion retrieveExistingAccountReversion() {
        final AccountReversion acctRev = (AccountReversion)getBusinessObject();
        final AccountReversion oldAcctRev = SpringContext.getBean(AccountReversionService.class).getByPrimaryId(acctRev.getUniversityFiscalYear(), acctRev.getChartOfAccountsCode(), acctRev.getAccountNumber());
        return oldAcctRev;
    }

    /**
     * Overridden to trickle down inactivation or activation to details
     * @see org.kuali.kfs.kns.maintenance.MaintainableImpl#saveBusinessObject()
     */
    @Override
    public void saveBusinessObject() {
        final boolean isActivatingOrgReversion = isActivatingAccountReversion();
        final boolean isInactivatingOrgReversion = isInactivatingAccountReversion();
        
        super.saveBusinessObject();
        
        if (isActivatingOrgReversion) {
            SpringContext.getBean(AccountReversionDetailTrickleDownInactivationService.class).trickleDownActiveAccountReversionDetails((AccountReversion)getBusinessObject(), getDocumentNumber());
        } else if (isInactivatingOrgReversion) {
            SpringContext.getBean(AccountReversionDetailTrickleDownInactivationService.class).trickleDownInactiveAccountReversionDetails((AccountReversion)getBusinessObject(), getDocumentNumber());
        }
    }

    /**
     * @see org.kuali.kfs.kns.maintenance.MaintainableImpl#getSections(org.kuali.kfs.kns.document.MaintenanceDocument, org.kuali.kfs.kns.maintenance.Maintainable)
     */
    @Override
    public List getSections(MaintenanceDocument document, Maintainable oldMaintainable) {
        List<Section> sections = super.getSections(document, oldMaintainable);
        
        if (accountReversionService == null) {
            accountReversionService = SpringContext.getBean(AccountReversionService.class);
        }
        
        for (Section section : sections) {
            for (Row row : section.getRows()) {
                List<Field> updatedFields = new ArrayList<Field>();
                for (Field field : row.getFields()) {
                    if (shouldIncludeField(field)) {
                        updatedFields.add(field);
                    }
                }
                row.setFields(updatedFields);
            }
        }
        return sections;
    }
    
    /**
     * Determines if the given field should be included in the updated row, once we take out inactive categories
     * @param field the field to check
     * @return true if the field should be included (ie, it doesn't describe an organization reversion with an inactive category); false otherwise
     */
    protected boolean shouldIncludeField(Field field) {
        boolean includeField = true;
        if (field.getContainerRows() != null) {
            for (Row containerRow : field.getContainerRows()) {
                for (Field containedField : containerRow.getFields()) {
                    if (containedField.getPropertyName().matches("accountReversionDetails\\[\\d+\\]\\.reversionCategory\\.reversionCategoryName")) {
                        final String categoryValue = containedField.getPropertyValue();
                        includeField = accountReversionService.isCategoryActiveByName(categoryValue);
                    }
                }
            }
        }
        return includeField;
    }
    
    @Override
    public void doRouteStatusChange(DocumentHeader documentHeader) {
        super.doRouteStatusChange(documentHeader);
        if (MaintenanceUtils.shouldClearCacheOnStatusChange(documentHeader)) {
            MaintenanceUtils.clearAllBlockingCache();
        }
    }
    
}
