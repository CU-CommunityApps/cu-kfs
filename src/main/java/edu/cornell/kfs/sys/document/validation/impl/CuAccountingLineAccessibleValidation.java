package edu.cornell.kfs.sys.document.validation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants.RequisitionStatuses;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.Correctable;
import org.kuali.kfs.sys.document.authorization.AccountingLineAuthorizer;
import org.kuali.kfs.sys.document.validation.event.AddAccountingLineEvent;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.sys.document.validation.event.UpdateAccountingLineEvent;
import org.kuali.kfs.sys.document.validation.impl.AccountingLineAccessibleValidation;
import org.kuali.kfs.sys.identity.KfsKimAttributes;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.role.RoleService;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.util.GlobalVariables;

import edu.cornell.kfs.module.purap.CUPurapWorkflowConstants.PaymentRequestDocument.NodeDetailEnum;
import edu.cornell.kfs.module.purap.businessobject.CuPurApAccountingLineBase;

public class CuAccountingLineAccessibleValidation extends AccountingLineAccessibleValidation {

    @Override
    public boolean validate(AttributedDocumentEvent event) {
        final Person currentUser = GlobalVariables.getUserSession().getPerson();

        if (accountingDocumentForValidation instanceof Correctable) {
            final String errorDocumentNumber = ((FinancialSystemDocumentHeader) 
                    accountingDocumentForValidation.getDocumentHeader()).getFinancialDocumentInErrorNumber();
            if (StringUtils.isNotBlank(errorDocumentNumber)) {
                return true;
            }
        }

        final AccountingLineAuthorizer accountingLineAuthorizer = lookupAccountingLineAuthorizer();
        final boolean lineIsAccessible = accountingLineAuthorizer.hasEditPermissionOnAccountingLine(accountingDocumentForValidation,
                accountingLineForValidation, getAccountingLineCollectionProperty(), currentUser, true);
        final boolean isAccessible = accountingLineAuthorizer.hasEditPermissionOnField(accountingDocumentForValidation,
                accountingLineForValidation, getAccountingLineCollectionProperty(), KFSPropertyConstants.ACCOUNT_NUMBER, lineIsAccessible, true, currentUser);
        boolean valid = true;
        boolean isExceptionNode = isExceptionNode(event.getDocument());
        
        if (!isAccessible) {
            
            // if only object code changed and the user has edit permissions on object code, that's ok
            if (event instanceof UpdateAccountingLineEvent) {
                final boolean isObjectCodeAccessible = accountingLineAuthorizer.hasEditPermissionOnField(accountingDocumentForValidation,
                        accountingLineForValidation, getAccountingLineCollectionProperty(),
                                KFSPropertyConstants.FINANCIAL_OBJECT_CODE, lineIsAccessible, true, currentUser);
                final boolean onlyObjectCodeChanged = onlyObjectCodeChanged(((UpdateAccountingLineEvent) event)
                        .getAccountingLine(), ((UpdateAccountingLineEvent) event).getUpdatedAccountingLine());

                if (isObjectCodeAccessible && onlyObjectCodeChanged) {
                    return true;
                }
            }

            // report errors
         // KFSPTS-2253
            if (!isExceptionNode) {
            final String principalName = currentUser.getPrincipalName();

            final String[] accountErrorParams = new String[] { getDataDictionaryService().getAttributeLabel(accountingLineForValidation.getClass(),
                    KFSPropertyConstants.ACCOUNT_NUMBER), accountingLineForValidation.getChartOfAccountsCode() + "-"
                            + accountingLineForValidation.getAccountNumber(), principalName };
           // KFSPTS-1273 : fixing an exisiting issue.  Limit to REQ and POA.  Broader solution need more work.
            if (CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorPath()) && event instanceof UpdateAccountingLineEvent) {
                GlobalVariables.getMessageMap().putError(event.getErrorPathPrefix() + "." + KFSPropertyConstants.ACCOUNT_NUMBER, convertEventToMessage(event), accountErrorParams);
            } else {
                 GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ACCOUNT_NUMBER, convertEventToMessage(event), accountErrorParams);
            }
            }  // end KFSPTS-2253
           
        } else if (event instanceof AddAccountingLineEvent && isAccountNode(event.getDocument()) && !isAccountingLineFo(event.getDocument()) && !isDiscountTradeInAccount()) {
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
     * KFSPTS-1273
     */
    private boolean isAccountingLineFo(Document document) {
        Person currentUser = GlobalVariables.getUserSession().getPerson();
        List<String> roleIds = new ArrayList<String>();
        roleIds.add(SpringContext.getBean(RoleService.class).getRoleIdByNamespaceCodeAndName(KFSConstants.ParameterNamespaces.KFS,
                KFSConstants.SysKimApiConstants.FISCAL_OFFICER_KIM_ROLE_NAME));
        roleIds.add(SpringContext.getBean(RoleService.class).getRoleIdByNamespaceCodeAndName(KFSConstants.ParameterNamespaces.KFS,
                KFSConstants.SysKimApiConstants.FISCAL_OFFICER_PRIMARY_DELEGATE_KIM_ROLE_NAME));
        roleIds.add(SpringContext.getBean(RoleService.class).getRoleIdByNamespaceCodeAndName(KFSConstants.ParameterNamespaces.KFS,
                KFSConstants.SysKimApiConstants.FISCAL_OFFICER_SECONDARY_DELEGATE_KIM_ROLE_NAME));

        Map<String,String> roleQualifier = new HashMap<String,String>(); 
        roleQualifier.put(KimConstants.AttributeConstants.DOCUMENT_NUMBER,document.getDocumentNumber());
        roleQualifier.put(KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME, document.getDocumentHeader().getWorkflowDocument().getDocumentTypeId()); 
        roleQualifier.put(KfsKimAttributes.FINANCIAL_DOCUMENT_TOTAL_AMOUNT,
                ((FinancialSystemDocumentHeader)document.getDocumentHeader()).getFinancialDocumentTotalAmount().toString());
        roleQualifier.put(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE,accountingLineForValidation.getChartOfAccountsCode());
        roleQualifier.put(KfsKimAttributes.ACCOUNT_NUMBER,accountingLineForValidation.getAccountNumber());
              
        return SpringContext.getBean(RoleService.class).principalHasRole(currentUser.getPrincipalId(), roleIds, roleQualifier);
    }

    private boolean isAccountNode(Document document) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        if (workflowDocument == null) {
            try {
                workflowDocument = SpringContext.getBean(DocumentService.class).getByDocumentHeaderId(document.getDocumentNumber())
                        .getDocumentHeader().getWorkflowDocument();
            } catch (Exception e) {

            }
        }
        return workflowDocument != null && workflowDocument.getNodeNames().contains(RequisitionStatuses.NODE_ACCOUNT);
    }
    
    // KFSPTS-2200 
    /*
     * Discount is prorated and account is recreated when saving or approving document.  This caused problem for FO to approve account that does not belong to him/her.
     * The validation will treat it as new account added by fo, and cause error.  This is to ignore these system created account
     */
    private boolean isDiscountTradeInAccount() {
                return (accountingLineForValidation instanceof CuPurApAccountingLineBase) && ((CuPurApAccountingLineBase)accountingLineForValidation).isDiscountTradeIn();
    }
    

    // KFSPTS-2253
    
    private boolean isExceptionNode(Document document) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        if (workflowDocument == null) {
            try {
                workflowDocument = SpringContext.getBean(DocumentService.class).getByDocumentHeaderId(document.getDocumentNumber())
                        .getDocumentHeader().getWorkflowDocument();
            } catch (Exception e) {

            }
        }
        // KFSPTS-1891 : added treasury node
        return workflowDocument != null  && (
               workflowDocument.getCurrentNodeNames().contains("ContractManagement")) ||
                        workflowDocument.getCurrentNodeNames().contains(NodeDetailEnum.PAYMENT_METHOD_REVIEW.getName());
    }
    

}
