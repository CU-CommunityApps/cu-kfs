/*
 * Copyright 2008 The Kuali Foundation
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
package org.kuali.kfs.sys.document.validation.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.RequisitionStatuses;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLineBase;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.Correctable;
import org.kuali.kfs.sys.document.authorization.AccountingLineAuthorizer;
import org.kuali.kfs.sys.document.authorization.AccountingLineAuthorizerBase;
import org.kuali.kfs.sys.document.datadictionary.AccountingLineGroupDefinition;
import org.kuali.kfs.sys.document.datadictionary.FinancialSystemTransactionalDocumentEntry;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AddAccountingLineEvent;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.sys.document.validation.event.DeleteAccountingLineEvent;
import org.kuali.kfs.sys.document.validation.event.UpdateAccountingLineEvent;
import org.kuali.kfs.sys.identity.KfsKimAttributes;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;
import org.kuali.rice.kim.service.RoleManagementService;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.rule.event.KualiDocumentEvent;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.service.DocumentService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;

/**
 * A validation that checks whether the given accounting line is accessible to the given user or not
 */
public class AccountingLineAccessibleValidation extends GenericValidation {
    protected DataDictionaryService dataDictionaryService;
    protected AccountingDocument accountingDocumentForValidation;
    protected AccountingLine accountingLineForValidation;
    
    /**
     * Indicates what is being done to an accounting line. This allows the same method to be used for different actions.
     */
    public enum AccountingLineAction {
        ADD(KFSKeyConstants.ERROR_ACCOUNTINGLINE_INACCESSIBLE_ADD), DELETE(KFSKeyConstants.ERROR_ACCOUNTINGLINE_INACCESSIBLE_DELETE), UPDATE(KFSKeyConstants.ERROR_ACCOUNTINGLINE_INACCESSIBLE_UPDATE);

        public final String accessibilityErrorKey;

        AccountingLineAction(String accessabilityErrorKey) {
            this.accessibilityErrorKey = accessabilityErrorKey;
        }
    }

    /**
     * Validates that the given accounting line is accessible for editing by the current user.
     * <strong>This method expects a document as the first parameter and an accounting line as the second</strong>
     * @see org.kuali.kfs.sys.document.validation.Validation#validate(java.lang.Object[])
     */
    public boolean validate(AttributedDocumentEvent event) {        
        final Person currentUser = GlobalVariables.getUserSession().getPerson();
        
        if (accountingDocumentForValidation instanceof Correctable) {
            final String errorDocumentNumber = ((FinancialSystemDocumentHeader)accountingDocumentForValidation.getDocumentHeader()).getFinancialDocumentInErrorNumber();
            if (StringUtils.isNotBlank(errorDocumentNumber))
                return true;
        }
        
        final AccountingLineAuthorizer accountingLineAuthorizer = lookupAccountingLineAuthorizer();
        final boolean lineIsAccessible = accountingLineAuthorizer.hasEditPermissionOnAccountingLine(accountingDocumentForValidation, accountingLineForValidation, getAccountingLineCollectionProperty(), currentUser, true);
        final boolean isAccessible = accountingLineAuthorizer.hasEditPermissionOnField(accountingDocumentForValidation, accountingLineForValidation, getAccountingLineCollectionProperty(), KFSPropertyConstants.ACCOUNT_NUMBER, lineIsAccessible, true, currentUser);
        boolean valid = true;
        boolean isContractManagementNode = isContractManagerNode(event.getDocument());
        // report errors
        if (!isAccessible) {
        	// KFSPTS-2253
        	if (!isContractManagementNode) {
            final String principalName = currentUser.getPrincipalName();
            
            final String[] chartErrorParams = new String[] { getDataDictionaryService().getAttributeLabel(accountingLineForValidation.getClass(), KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE), accountingLineForValidation.getChartOfAccountsCode(),  principalName};
            // KFSPTS-1273 : fixing an exisiting issue.  Limit to REQ and POA.  Broader solution need more work.
            if (CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorPath()) && event instanceof UpdateAccountingLineEvent) {
                GlobalVariables.getMessageMap().putError(event.getErrorPathPrefix() + "." + KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, convertEventToMessage(event), chartErrorParams);
        	} else {
                GlobalVariables.getMessageMap().putError(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, convertEventToMessage(event), chartErrorParams);
        	}
            
            final String[] accountErrorParams = new String[] { getDataDictionaryService().getAttributeLabel(accountingLineForValidation.getClass(), KFSPropertyConstants.ACCOUNT_NUMBER), accountingLineForValidation.getAccountNumber(), principalName };
            // KFSPTS-1273 : fixing an exisiting issue.  Limit to REQ and POA.  Broader solution need more work.
            if (CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorPath()) && event instanceof UpdateAccountingLineEvent) {
                GlobalVariables.getMessageMap().putError(event.getErrorPathPrefix() + "." + KFSPropertyConstants.ACCOUNT_NUMBER, convertEventToMessage(event), accountErrorParams);
        	} else {
        		 GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ACCOUNT_NUMBER, convertEventToMessage(event), accountErrorParams);
        	}
        	}  // end KFSPTS-2253
           
        } else 
        if (event instanceof AddAccountingLineEvent && isAccountNode(event.getDocument()) && !isAccountingLineFo(event.getDocument()) && !isDiscountTradeInAccount()) {
            final String principalName = currentUser.getPrincipalName();
            final String[] chartErrorParams = new String[] { getDataDictionaryService().getAttributeLabel(accountingLineForValidation.getClass(), KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE), accountingLineForValidation.getChartOfAccountsCode(),  principalName};
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, convertEventToMessage(event), chartErrorParams);
            final String[] accountErrorParams = new String[] { getDataDictionaryService().getAttributeLabel(accountingLineForValidation.getClass(), KFSPropertyConstants.ACCOUNT_NUMBER), accountingLineForValidation.getAccountNumber(), principalName };
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ACCOUNT_NUMBER, convertEventToMessage(event), accountErrorParams);
        	valid=false;
        }

        return (isAccessible || isContractManagementNode) && valid;
    }
    
    /**
     * Returns the name of the accounting line group which holds the proper authorizer to do the KIM check
     * @return the name of the accouting line group to get the authorizer from
     */
    protected String getGroupName() {
        return (accountingLineForValidation.isSourceAccountingLine() ? KFSConstants.SOURCE_ACCOUNTING_LINES_GROUP_NAME : KFSConstants.TARGET_ACCOUNTING_LINES_GROUP_NAME);
    }
    
    /**
     * @return hopefully, the best accounting line authorizer implementation to do the KIM check for to see if lines are accessible
     */
    protected AccountingLineAuthorizer lookupAccountingLineAuthorizer() {
        final String groupName = getGroupName();
        final Map<String, AccountingLineGroupDefinition> groups = ((FinancialSystemTransactionalDocumentEntry)dataDictionaryService.getDataDictionary().getDictionaryObjectEntry(accountingDocumentForValidation.getClass().getName())).getAccountingLineGroups();
        
        if (groups.isEmpty()) return new AccountingLineAuthorizerBase(); // no groups? just use the default...
        if (groups.containsKey(groupName)) return groups.get(groupName).getAccountingLineAuthorizer(); // we've got the group

        final Set<String> groupNames = groups.keySet(); // we've got groups, just not the proper name; try our luck and get the first group iterator
        final Iterator<String> groupNameIterator = groupNames.iterator();
        final String firstGroupName = groupNameIterator.next();
        return groups.get(firstGroupName).getAccountingLineAuthorizer();
    }
    
    /**
     * Determines the property of the accounting line collection from the error prefixes
     * @return the accounting line collection property
     */
    protected String getAccountingLineCollectionProperty() {
        String propertyName = null;
        if (GlobalVariables.getMessageMap().getErrorPath().size() > 0) {
            propertyName = ((String)GlobalVariables.getMessageMap().getErrorPath().get(0)).replaceFirst(".*?document\\.", "");
        } else {
            propertyName = accountingLineForValidation.isSourceAccountingLine() ? KFSConstants.PermissionAttributeValue.SOURCE_ACCOUNTING_LINES.value : KFSConstants.PermissionAttributeValue.TARGET_ACCOUNTING_LINES.value;
        }
        if (propertyName.equals("newSourceLine")) return KFSConstants.PermissionAttributeValue.SOURCE_ACCOUNTING_LINES.value;
        if (propertyName.equals("newTargetLine")) return KFSConstants.PermissionAttributeValue.TARGET_ACCOUNTING_LINES.value;
        return propertyName;
    }
    
    /**
     * Determines what error message should be shown based on the event that required this validation
     * @param event the event to use to determine the error message
     * @return the key of the error message to display
     */
    protected String convertEventToMessage(KualiDocumentEvent event) {
        if (event instanceof AddAccountingLineEvent) {
            return AccountingLineAction.ADD.accessibilityErrorKey;
        } else if (event instanceof UpdateAccountingLineEvent) {
            return AccountingLineAction.UPDATE.accessibilityErrorKey;
        } else if (event instanceof DeleteAccountingLineEvent) {
            return AccountingLineAction.DELETE.accessibilityErrorKey;
        } else {
            return "";
        }
    }

    /**
     * Gets the accountingDocumentForValidation attribute. 
     * @return Returns the accountingDocumentForValidation.
     */
    public AccountingDocument getAccountingDocumentForValidation() {
        return accountingDocumentForValidation;
    }

    /**
     * Sets the accountingDocumentForValidation attribute value.
     * @param accountingDocumentForValidation The accountingDocumentForValidation to set.
     */
    public void setAccountingDocumentForValidation(AccountingDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;
    }

    /**
     * Gets the accountingLineForValidation attribute. 
     * @return Returns the accountingLineForValidation.
     */
    public AccountingLine getAccountingLineForValidation() {
        return accountingLineForValidation;
    }

    /**
     * Sets the accountingLineForValidation attribute value.
     * @param accountingLineForValidation The accountingLineForValidation to set.
     */
    public void setAccountingLineForValidation(AccountingLine accountingLineForValidation) {
        this.accountingLineForValidation = accountingLineForValidation;
    }

    /**
     * Gets the dataDictionaryService attribute. 
     * @return Returns the dataDictionaryService.
     */
    public DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    /**
     * Sets the dataDictionaryService attribute value.
     * @param dataDictionaryService The dataDictionaryService to set.
     */
    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }
   
    /*
     * KFSPTS-1273
     */
    private boolean isAccountingLineFo(Document document) {
        Person currentUser = GlobalVariables.getUserSession().getPerson();
        List<String> roleIds = new ArrayList<String>();
		roleIds.add(SpringContext.getBean(RoleManagementService.class).getRoleIdByName(KFSConstants.ParameterNamespaces.KFS,
				KFSConstants.SysKimConstants.FISCAL_OFFICER_KIM_ROLE_NAME));
		roleIds.add(SpringContext.getBean(RoleManagementService.class).getRoleIdByName(KFSConstants.ParameterNamespaces.KFS,
				KFSConstants.SysKimConstants.FISCAL_OFFICER_PRIMARY_DELEGATE_KIM_ROLE_NAME));
		roleIds.add(SpringContext.getBean(RoleManagementService.class).getRoleIdByName(KFSConstants.ParameterNamespaces.KFS,
				KFSConstants.SysKimConstants.FISCAL_OFFICER_SECONDARY_DELEGATE_KIM_ROLE_NAME));

		AttributeSet roleQualifier = new AttributeSet();
		roleQualifier.put(KfsKimAttributes.DOCUMENT_NUMBER,document.getDocumentNumber());
		roleQualifier.put(KfsKimAttributes.DOCUMENT_TYPE_NAME, document.getDocumentHeader().getWorkflowDocument().getDocumentType());
		roleQualifier.put(KfsKimAttributes.FINANCIAL_DOCUMENT_TOTAL_AMOUNT,
				((FinancialSystemDocumentHeader)document.getDocumentHeader()).getFinancialDocumentTotalAmount().toString());
		roleQualifier.put(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE,accountingLineForValidation.getChartOfAccountsCode());
		roleQualifier.put(KfsKimAttributes.ACCOUNT_NUMBER,accountingLineForValidation.getAccountNumber());
              
        return SpringContext.getBean(RoleManagementService.class).principalHasRole(currentUser.getPrincipalId(), roleIds, roleQualifier);
    }

    private boolean isAccountNode(Document document) {
		KualiWorkflowDocument workflowDocument = document.getDocumentHeader()
				.getWorkflowDocument();
		if (workflowDocument == null) {
			try {
				workflowDocument = SpringContext.getBean(DocumentService.class).getByDocumentHeaderId(document.getDocumentNumber())
						.getDocumentHeader().getWorkflowDocument();
			} catch (Exception e) {

			}
		}
	    return workflowDocument != null && workflowDocument.getRouteHeader() != null && StringUtils.equals(RequisitionStatuses.NODE_ACCOUNT, workflowDocument.getRouteHeader().getCurrentRouteNodeNames());
    }
    
    // KFSPTS-2200 
    /*
     * Discount is prorated and account is recreated when saving or approving document.  This caused problem for FO to approve account that does not belong to him/her.
     * The validation will treat it as new account added by fo, and cause error.  This is to ignore these system created account
     */
    private boolean isDiscountTradeInAccount() {
    			return (accountingLineForValidation instanceof PurApAccountingLineBase) && ((PurApAccountingLineBase)accountingLineForValidation).isDiscountTradeIn();
    }
    

    // KFSPTS-2253
    
    private boolean isContractManagerNode(Document document) {
		KualiWorkflowDocument workflowDocument = document.getDocumentHeader()
				.getWorkflowDocument();
		if (workflowDocument == null) {
			try {
				workflowDocument = SpringContext.getBean(DocumentService.class).getByDocumentHeaderId(document.getDocumentNumber())
						.getDocumentHeader().getWorkflowDocument();
			} catch (Exception e) {

			}
		}
	    return workflowDocument != null && workflowDocument.getRouteHeader() != null && StringUtils.equals("ContractManagement", workflowDocument.getRouteHeader().getCurrentRouteNodeNames());
    }

}

