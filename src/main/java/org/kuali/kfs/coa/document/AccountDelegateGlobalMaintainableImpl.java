/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.coa.document;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.COAKeyConstants;
import org.kuali.kfs.coa.COAParameterConstants;
import org.kuali.kfs.coa.businessobject.AccountDelegate;
import org.kuali.kfs.coa.businessobject.AccountDelegateGlobal;
import org.kuali.kfs.coa.businessobject.AccountDelegateGlobalDetail;
import org.kuali.kfs.coa.businessobject.AccountDelegateModel;
import org.kuali.kfs.coa.businessobject.AccountDelegateModelDetail;
import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;
import org.kuali.kfs.coa.service.AccountDelegateService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemGlobalMaintainable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class overrides the base {@link FinancialSystemGlobalMaintainable} to generate the specific maintenance locks
 * for Global delegates and to help with using delegate models
 */
public class AccountDelegateGlobalMaintainableImpl extends FinancialSystemGlobalMaintainable {

    /**
     * show the max account delegates info message
     */
    @Override
    public void processAfterNew(MaintenanceDocument document, Map<String, String[]> requestParameters) {
        super.processAfterNew(document, requestParameters);
        this.displayMaxAccountDelegatesInfoMessage();
    }

    /**
     * Informational message about max account delegates
     */
    protected void displayMaxAccountDelegatesInfoMessage() {
        String maxAccountDelegatesString = SpringContext.getBean(ParameterService.class).getParameterValueAsString(
                AccountDelegateGlobal.class, COAParameterConstants.ACCOUNT_DELEGATES_LIMIT);
        if (maxAccountDelegatesString != null && !maxAccountDelegatesString.isEmpty()) {
            GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_ERRORS,
                    COAKeyConstants.INFO_DOCUMENT_DELEGATE_ACCOUNT_DELEGATES_LIMIT, maxAccountDelegatesString);
        }
    }

    @Override
    public void setupNewFromExisting(MaintenanceDocument document, Map<String, String[]> parameters) {
        super.setupNewFromExisting(document, parameters);

        AccountDelegateGlobal globalDelegate = (AccountDelegateGlobal) this.getBusinessObject();
        globalDelegate.setVersionNumber(1L);
        this.setBusinessObject(globalDelegate);
        // 1. if model name, chart of accounts, and org code are all present
        // then let's see if we've actually got a model record
        if (StringUtils.isNotBlank(globalDelegate.getModelName())
                && StringUtils.isNotBlank(globalDelegate.getModelChartOfAccountsCode())
                && StringUtils.isNotBlank(globalDelegate.getModelOrganizationCode())) {
            Map<String, String> pkMap = new HashMap<>();
            pkMap.put("accountDelegateModelName", globalDelegate.getModelName());
            pkMap.put("chartOfAccountsCode", globalDelegate.getModelChartOfAccountsCode());
            pkMap.put("organizationCode", globalDelegate.getModelOrganizationCode());

            AccountDelegateModel globalDelegateTemplate = SpringContext.getBean(BusinessObjectService.class)
                    .findByPrimaryKey(AccountDelegateModel.class, pkMap);
            if (globalDelegateTemplate != null) {
                // 2. if there is a model record, then let's populate the global delegate
                // based on that
                for (AccountDelegateModelDetail model : globalDelegateTemplate.getAccountDelegateModelDetails()) {
                    // only populate with active models
                    if (model.isActive()) {
                        AccountDelegateGlobalDetail newDelegate = new AccountDelegateGlobalDetail(model);
                        // allow deletion of the new delegate from the global delegate
                        newDelegate.setNewCollectionRecord(true);
                        globalDelegate.getDelegateGlobals().add(newDelegate);
                    }
                }
            }
        }

        refreshAccounts();
    }

    @Override
    public String getLockingDocumentId() {
        String lock = super.getLockingDocumentId();
        if (StringUtils.isNotBlank(lock)) {
            return lock;
        } else {
            AccountDelegateService accountDelegateService = SpringContext.getBean(AccountDelegateService.class);
            lock = accountDelegateService.getLockingDocumentId(this, getDocumentNumber());
            return lock;
        }
    }

    /**
     * This creates the particular locking representation for this global document.
     */
    @Override
    public List<MaintenanceLock> generateMaintenanceLocks() {
        // create locking rep for each combination of account and object code
        List<MaintenanceLock> maintenanceLocks = new ArrayList<>();
        AccountDelegateGlobal delegateGlobal = (AccountDelegateGlobal) getBusinessObject();

        // hold all the locking representations in a set to make sure we don't get any duplicates
        Set<String> lockingRepresentations = new HashSet<>();

        MaintenanceLock maintenanceLock;
        if (ObjectUtils.isNotNull(delegateGlobal)) {
            for (AccountGlobalDetail accountGlobalDetail : delegateGlobal.getAccountGlobalDetails()) {
                for (AccountDelegateGlobalDetail delegateGlobalDetail : delegateGlobal.getDelegateGlobals()) {
                    StringBuilder lockRep = new StringBuilder();
                    lockRep.append(AccountDelegate.class.getName());
                    lockRep.append(KFSConstants.Maintenance.AFTER_CLASS_DELIM);
                    lockRep.append("chartOfAccountsCode");
                    lockRep.append(KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
                    lockRep.append(accountGlobalDetail.getChartOfAccountsCode());
                    lockRep.append(KFSConstants.Maintenance.AFTER_VALUE_DELIM);
                    lockRep.append("accountNumber");
                    lockRep.append(KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
                    lockRep.append(accountGlobalDetail.getAccountNumber());
                    lockRep.append(KFSConstants.Maintenance.AFTER_VALUE_DELIM);
                    lockRep.append("financialDocumentTypeCode");
                    lockRep.append(KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
                    lockRep.append(delegateGlobalDetail.getFinancialDocumentTypeCode());
                    lockRep.append(KFSConstants.Maintenance.AFTER_VALUE_DELIM);
                    lockRep.append("accountDelegateSystemId");
                    lockRep.append(KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
                    lockRep.append(delegateGlobalDetail.getAccountDelegateUniversalId());
                    // FIXME above is a bit dangerous b/c it hard codes the attribute names, which could change (particularly
                    // accountDelegateSystemId) - guess they should either be constants or obtained by looping through Delegate keys;
                    // however, I copied this from elsewhere which had them hard-coded, so I'm leaving it for now

                    if (!lockingRepresentations.contains(lockRep.toString())) {
                        maintenanceLock = new MaintenanceLock();
                        maintenanceLock.setDocumentNumber(delegateGlobal.getDocumentNumber());
                        maintenanceLock.setLockingRepresentation(lockRep.toString());
                        maintenanceLocks.add(maintenanceLock);
                        lockingRepresentations.add(lockRep.toString());
                    }

                    lockRep = new StringBuilder();
                    lockRep.append(AccountDelegate.class.getName());
                    lockRep.append(KFSConstants.Maintenance.AFTER_CLASS_DELIM);
                    lockRep.append("chartOfAccountsCode");
                    lockRep.append(KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
                    lockRep.append(accountGlobalDetail.getChartOfAccountsCode());
                    lockRep.append(KFSConstants.Maintenance.AFTER_VALUE_DELIM);
                    lockRep.append("accountNumber");
                    lockRep.append(KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
                    lockRep.append(accountGlobalDetail.getAccountNumber());
                    lockRep.append(KFSConstants.Maintenance.AFTER_VALUE_DELIM);
                    lockRep.append("financialDocumentTypeCode");
                    lockRep.append(KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
                    lockRep.append(delegateGlobalDetail.getFinancialDocumentTypeCode());
                    lockRep.append(KFSConstants.Maintenance.AFTER_VALUE_DELIM);
                    lockRep.append("accountsDelegatePrmrtIndicator");
                    lockRep.append(KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
                    lockRep.append("true");

                    if (!lockingRepresentations.contains(lockRep.toString())) {
                        maintenanceLock = new MaintenanceLock();
                        maintenanceLock.setDocumentNumber(delegateGlobal.getDocumentNumber());
                        maintenanceLock.setLockingRepresentation(lockRep.toString());
                        maintenanceLocks.add(maintenanceLock);
                        lockingRepresentations.add(lockRep.toString());
                    }

                    lockRep = new StringBuilder();
                    lockRep.append(AccountDelegateGlobal.class.getName());
                    lockRep.append(KFSConstants.Maintenance.AFTER_CLASS_DELIM);
                    lockRep.append(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE);
                    lockRep.append(KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
                    lockRep.append(accountGlobalDetail.getChartOfAccountsCode());
                    lockRep.append(KFSConstants.Maintenance.AFTER_VALUE_DELIM);
                    lockRep.append(KFSPropertyConstants.ACCOUNT_NUMBER);
                    lockRep.append(KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
                    lockRep.append(accountGlobalDetail.getAccountNumber());

                    if (!lockingRepresentations.contains(lockRep.toString())) {
                        maintenanceLock = new MaintenanceLock();
                        maintenanceLock.setDocumentNumber(delegateGlobal.getDocumentNumber());
                        maintenanceLock.setLockingRepresentation(lockRep.toString());
                        maintenanceLocks.add(maintenanceLock);
                        lockingRepresentations.add(lockRep.toString());
                    }
                }
            }
        }
        return maintenanceLocks;
    }

    @Override
    public Class<? extends PersistableBusinessObject> getPrimaryEditedBusinessObjectClass() {
        return AccountDelegate.class;
    }

    /**
     * Overridden to update the delegations for currently routing documents; this also guarantees that the business
     * objects to change will be saved in a separate transaction
     */
    @Override
    public void saveBusinessObject() {
        final AccountDelegateGlobal accountDelegateGlobal = (AccountDelegateGlobal) this.getBusinessObject();
        final AccountDelegateService accountDelegateService = SpringContext.getBean(AccountDelegateService.class);

        accountDelegateService.saveInactivationsForGlobalMaintenanceDocument(accountDelegateGlobal.generateDeactivationsToPersist());
        final List<PersistableBusinessObject> delegatesToChange =
                accountDelegateGlobal.generateGlobalChangesToPersist();
        accountDelegateService.saveChangesForGlobalMaintenanceDocument(delegatesToChange);

        final Set<String> accountNumbers = new HashSet<>();
        final Set<String> documentTypes = new HashSet<>();
        delegatesToChange.forEach(delegateToChange -> {
            accountNumbers.add(((AccountDelegate) delegateToChange).getAccountNumber());
            documentTypes.add(((AccountDelegate) delegateToChange).getFinancialDocumentTypeCode());
        });
        accountDelegateService.updateDelegationRole(getDocumentNumber(), accountNumbers, documentTypes);

        refreshAccounts();
    }

    @Override
    public void addNewLineToCollection(String collectionName) {
        super.addNewLineToCollection(collectionName);
        if (StringUtils.equals(collectionName, KFSPropertyConstants.ACCOUNT_CHANGE_DETAILS)) {
            refreshAccounts();
        }
    }

    @Override
    public void processAfterCopy(MaintenanceDocument document, Map<String, String[]> parameters) {
        super.processAfterCopy(document, parameters);
        refreshAccounts();
    }

    @Override
    public void processAfterEdit(MaintenanceDocument document, Map<String, String[]> requestParameters) {
        super.processAfterEdit(document, requestParameters);
        refreshAccounts();
    }

    @Override
    public void processAfterRetrieve() {
        super.processAfterRetrieve();
        refreshAccounts();
    }

    protected void refreshAccounts() {
        if (ObjectUtils.isNotNull(getBusinessObject())
                && !CollectionUtils.isEmpty(((AccountDelegateGlobal) getBusinessObject()).getAccountGlobalDetails())) {
            for (AccountGlobalDetail accountGlobalDetail : ((AccountDelegateGlobal) getBusinessObject())
                    .getAccountGlobalDetails()) {
                accountGlobalDetail.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);
            }
        }
    }
}
