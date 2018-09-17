/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2018 Kuali, Inc.
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
package org.kuali.kfs.sys.document.validation.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.RequisitionStatuses;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestAccount;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLineBase;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
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
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A validation that checks whether the given accounting line is accessible to the given user or not
 */
public class AccountingLineAccessibleValidation extends GenericValidation {
	
    private static final Logger LOG = LogManager.getLogger(AccountingLineAccessibleValidation.class);
    
    protected DataDictionaryService dataDictionaryService;
    protected AccountingDocument accountingDocumentForValidation;
    protected AccountingLine accountingLineForValidation;

    /**
     * Indicates what is being done to an accounting line. This allows the same method to be used for different actions.
     */
    public enum AccountingLineAction {
        ADD(KFSKeyConstants.ERROR_ACCOUNTINGLINE_INACCESSIBLE_ADD),
        DELETE(KFSKeyConstants.ERROR_ACCOUNTINGLINE_INACCESSIBLE_DELETE),
        UPDATE(KFSKeyConstants.ERROR_ACCOUNTINGLINE_INACCESSIBLE_UPDATE);
    	
        public final String accessibilityErrorKey;

        AccountingLineAction(String accessabilityErrorKey) {
            this.accessibilityErrorKey = accessabilityErrorKey;
        }
    }

    /**
     * Validates that the given accounting line is accessible for editing by the current user.
     * <strong>This method expects a document as the first parameter and an accounting line as the second</strong>
     */
    @Override
    public boolean validate(AttributedDocumentEvent event) {
        final Person currentUser = GlobalVariables.getUserSession().getPerson();

        if (accountingDocumentForValidation instanceof Correctable) {
            final String errorDocumentNumber = ((FinancialSystemDocumentHeader) accountingDocumentForValidation
                .getDocumentHeader()).getFinancialDocumentInErrorNumber();
            if (StringUtils.isNotBlank(errorDocumentNumber)) {
                return true;
            }
        }

        final AccountingLineAuthorizer accountingLineAuthorizer = lookupAccountingLineAuthorizer();
        final Set<String> currentNodes = accountingDocumentForValidation.getDocumentHeader().getWorkflowDocument()
                .getCurrentNodeNames();
        final boolean lineIsAccessible = accountingLineAuthorizer
                .hasEditPermissionOnAccountingLine(accountingDocumentForValidation, accountingLineForValidation,
                    getAccountingLineCollectionProperty(), currentUser, true, currentNodes);
        final boolean isAccessible = accountingLineAuthorizer.hasEditPermissionOnField(accountingDocumentForValidation,
                accountingLineForValidation, getAccountingLineCollectionProperty(), KFSPropertyConstants.ACCOUNT_NUMBER,
                lineIsAccessible, true, currentUser, currentNodes);

        boolean valid = true;
        boolean isExceptionNode = isExceptionNode(event.getDocument());

        if (!isAccessible) {
            // if only object code changed and the user has edit permissions on object code, that's ok
            if (event instanceof UpdateAccountingLineEvent) {
                final boolean isObjectCodeAccessible = accountingLineAuthorizer
                        .hasEditPermissionOnField(accountingDocumentForValidation, accountingLineForValidation,
                            getAccountingLineCollectionProperty(), KFSPropertyConstants.FINANCIAL_OBJECT_CODE,
                            lineIsAccessible, true, currentUser, currentNodes);
                final boolean onlyObjectCodeChanged = onlyObjectCodeChanged(((UpdateAccountingLineEvent) event)
                        .getAccountingLine(), ((UpdateAccountingLineEvent) event).getUpdatedAccountingLine());

                if (isObjectCodeAccessible && onlyObjectCodeChanged) {
                    return true;
                }
            }

            if (isPreqDiscountRecreate(event)) {
                return true;
            }
            // report errors
            // KFSPTS-2253
            if (!isExceptionNode) {
            final String principalName = currentUser.getPrincipalName();
            
            final String[] chartErrorParams = new String[] { getDataDictionaryService().getAttributeLabel(accountingLineForValidation.getClass(), KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE), accountingLineForValidation.getChartOfAccountsCode(),  principalName};
            // KFSPTS-1273 : fixing an exisiting issue.  Limit to REQ and POA.  Broader solution need more work.
            if (event instanceof UpdateAccountingLineEvent) {
           //     if (CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorPath()) && event instanceof UpdateAccountingLineEvent) {
                GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(event.getErrorPathPrefix() + "." + KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, convertEventToMessage(event), chartErrorParams);
            } else {
                GlobalVariables.getMessageMap().putError(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, convertEventToMessage(event), chartErrorParams);
            }
            
            final String[] accountErrorParams = new String[] { getDataDictionaryService().getAttributeLabel(accountingLineForValidation.getClass(), KFSPropertyConstants.ACCOUNT_NUMBER), accountingLineForValidation.getAccountNumber(), principalName };
            // KFSPTS-1273 : fixing an exisiting issue.  Limit to REQ and POA.  Broader solution need more work.
            if (event instanceof UpdateAccountingLineEvent) {
                GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(event.getErrorPathPrefix() + "." + KFSPropertyConstants.ACCOUNT_NUMBER, convertEventToMessage(event), accountErrorParams);
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

        return (isAccessible || isExceptionNode) && valid;
    }

    /*
     * Returns true if validating a PREQ discountTradeIn account at the Account or Org Review node. If at the Account node,
     * the current user must also not be a FO or FO delegate for the account being validated.
     */
    private boolean isPreqDiscountRecreate(AttributedDocumentEvent event) {
        return (isAccountNode(event.getDocument()) ? !isAccountingLineFo(event.getDocument()) : isOrgReviewNode(event.getDocument()))
                && accountingLineForValidation instanceof PaymentRequestAccount  && isDiscountTradeInAccount();
    }
    /**
     * Checks to see if the object code is the only difference between the original accounting line and the updated
     * accounting line.
     * 
     * @param accountingLine
     * @param updatedAccountingLine
     * @return true if only the object code has changed on the accounting line, false otherwise
     */
    protected boolean onlyObjectCodeChanged(AccountingLine accountingLine, AccountingLine updatedAccountingLine) {
        if (accountingLine.isLike(updatedAccountingLine)) {
            return false;
        }

        AccountingLine updatedLine;
        try {
            updatedLine = updatedAccountingLine.getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            LOG.warn("Exception trying to create a new instance of the updatedLine ("
                + updatedAccountingLine.getClass() + ").");
            return false;
        }

        updatedLine.copyFrom(updatedAccountingLine);
        updatedLine.setFinancialObjectCode(accountingLine.getFinancialObjectCode());

        return accountingLine.isLike(updatedLine);
    }

    /**
     * Returns the name of the accounting line group which holds the proper authorizer to do the KIM check
     *
     * @return the name of the accouting line group to get the authorizer from
     */
    protected String getGroupName() {
        return (accountingLineForValidation.isSourceAccountingLine() ? KFSConstants.SOURCE_ACCOUNTING_LINES_GROUP_NAME :
            KFSConstants.TARGET_ACCOUNTING_LINES_GROUP_NAME);
    }

    /**
     * @return hopefully, the best accounting line authorizer implementation to do the KIM check for to see if lines
     * are accessible
     */
    protected AccountingLineAuthorizer lookupAccountingLineAuthorizer() {
        final String groupName = getGroupName();
        final Map<String, AccountingLineGroupDefinition> groups = ((FinancialSystemTransactionalDocumentEntry)
                dataDictionaryService.getDataDictionary().getDictionaryObjectEntry(accountingDocumentForValidation
                    .getClass().getName())).getAccountingLineGroups();
        
        if (groups.isEmpty())
         {
            return new AccountingLineAuthorizerBase(); // no groups? just use the default...
        }
        if (groups.containsKey(groupName))
         {
            return groups.get(groupName).getAccountingLineAuthorizer(); // we've got the group
        }

        // we've got groups, just not the proper name; try our luck and get the first group iterator
        final Set<String> groupNames = groups.keySet();
        final Iterator<String> groupNameIterator = groupNames.iterator();
        final String firstGroupName = groupNameIterator.next();
        return groups.get(firstGroupName).getAccountingLineAuthorizer();
    }

    /**
     * Determines the property of the accounting line collection from the error prefixes
     *
     * @return the accounting line collection property
     */
    protected String getAccountingLineCollectionProperty() {
        String propertyName;
        if (GlobalVariables.getMessageMap().getErrorPath().size() > 0) {
            propertyName = GlobalVariables.getMessageMap().getErrorPath().get(0).replaceFirst(".*?document\\."
                    , "");
        } else {
            propertyName = accountingLineForValidation.isSourceAccountingLine() ?
                    KFSConstants.PermissionAttributeValue.SOURCE_ACCOUNTING_LINES.value :
                    KFSConstants.PermissionAttributeValue.TARGET_ACCOUNTING_LINES.value;
        }
        if (propertyName.equals("newSourceLine")) {
            return KFSConstants.PermissionAttributeValue.SOURCE_ACCOUNTING_LINES.value;
        }
        if (propertyName.equals("newTargetLine")) {
            return KFSConstants.PermissionAttributeValue.TARGET_ACCOUNTING_LINES.value;
        }
        return propertyName;
    }

    /**
     * Determines what error message should be shown based on the event that required this validation
     *
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

    public AccountingDocument getAccountingDocumentForValidation() {
        return accountingDocumentForValidation;
    }

    public void setAccountingDocumentForValidation(AccountingDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;
    }

    public AccountingLine getAccountingLineForValidation() {
        return accountingLineForValidation;
    }

    public void setAccountingLineForValidation(AccountingLine accountingLineForValidation) {
        this.accountingLineForValidation = accountingLineForValidation;
    }

    public DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    // CU customization
    /*
     * KFSPTS-1273
     */
    private boolean isAccountingLineFo(Document document) {
        Person currentUser = GlobalVariables.getUserSession().getPerson();
        List<String> roleIds = new ArrayList<String>();
        roleIds.add(KimApiServiceLocator.getRoleService().getRoleIdByNamespaceCodeAndName(KFSConstants.ParameterNamespaces.KFS,
                KFSConstants.SysKimApiConstants.FISCAL_OFFICER_KIM_ROLE_NAME));
        roleIds.add(KimApiServiceLocator.getRoleService().getRoleIdByNamespaceCodeAndName(KFSConstants.ParameterNamespaces.KFS,
                KFSConstants.SysKimApiConstants.FISCAL_OFFICER_PRIMARY_DELEGATE_KIM_ROLE_NAME));
        roleIds.add(KimApiServiceLocator.getRoleService().getRoleIdByNamespaceCodeAndName(KFSConstants.ParameterNamespaces.KFS,
                KFSConstants.SysKimApiConstants.FISCAL_OFFICER_SECONDARY_DELEGATE_KIM_ROLE_NAME));
        Map<String,String> roleQualifier = new HashMap<String,String>();

        roleQualifier.put(KimConstants.AttributeConstants.DOCUMENT_NUMBER,document.getDocumentNumber());
        roleQualifier.put(KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME, document.getDocumentHeader().getWorkflowDocument().getDocumentTypeName());
        roleQualifier.put(KfsKimAttributes.FINANCIAL_DOCUMENT_TOTAL_AMOUNT,
                ((FinancialSystemDocumentHeader)document.getDocumentHeader()).getFinancialDocumentTotalAmount().toString());
        roleQualifier.put(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE,accountingLineForValidation.getChartOfAccountsCode());
        roleQualifier.put(KfsKimAttributes.ACCOUNT_NUMBER,accountingLineForValidation.getAccountNumber());
              
        return KimApiServiceLocator.getRoleService().principalHasRole(currentUser.getPrincipalId(), roleIds, roleQualifier);
    }

    // Determines whether the document is currently at the given route node.
    private boolean isDocumentAtNode(Document document, String nodeName) {
        WorkflowDocument workflowDocument = document.getDocumentHeader()
                .getWorkflowDocument();
        if (workflowDocument == null) {
            try {
                workflowDocument = SpringContext.getBean(DocumentService.class).getByDocumentHeaderId(document.getDocumentNumber())
                        .getDocumentHeader().getWorkflowDocument();
            } catch (Exception e) {

            }
        }
        
        return workflowDocument != null && workflowDocument.getDocument() != null
                && workflowDocument.getCurrentNodeNames().contains(nodeName);
    }

    private boolean isAccountNode(Document document) {
        return isDocumentAtNode(document, RequisitionStatuses.NODE_ACCOUNT);
    }

    private boolean isOrgReviewNode(Document document) {
        return isDocumentAtNode(document, RequisitionStatuses.NODE_ORG_REVIEW);
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
    
    private boolean isExceptionNode(Document document) {
        WorkflowDocument workflowDocument = document.getDocumentHeader()
                .getWorkflowDocument();
        if (workflowDocument == null) {
            try {
                workflowDocument = SpringContext.getBean(DocumentService.class).getByDocumentHeaderId(document.getDocumentNumber())
                        .getDocumentHeader().getWorkflowDocument();
            } catch (Exception e) {

            }
        }

        // KFSPTS-1891 : added treasury node
      return workflowDocument != null && workflowDocument.getDocument() != null && (
    		  workflowDocument.getCurrentNodeNames().contains("ContractManagement") ||
    		  workflowDocument.getCurrentNodeNames().contains(PurapConstants.PaymentRequestStatuses.NODE_PAYMENT_METHOD_REVIEW));
    }

}

