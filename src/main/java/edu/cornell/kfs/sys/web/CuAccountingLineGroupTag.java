package edu.cornell.kfs.sys.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants.RequisitionStatuses;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestAccount;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLineBase;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderAccount;
import org.kuali.kfs.module.purap.businessobject.RequisitionAccount;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.datadictionary.AccountingLineGroupDefinition;
import org.kuali.kfs.sys.document.web.AccountingLineGroupTag;
import org.kuali.kfs.sys.document.web.AccountingLineTableRow;
import org.kuali.kfs.sys.document.web.RenderableAccountingLineContainer;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.KRADConstants;

public class CuAccountingLineGroupTag extends AccountingLineGroupTag {

    
    protected RenderableAccountingLineContainer buildContainerForLine(AccountingLineGroupDefinition groupDefinition,
            AccountingDocument accountingDocument, AccountingLine accountingLine, Person currentUser, Integer count, boolean topLine) {
        final String accountingLinePropertyName = count == null ? getNewLinePropertyName() : getCollectionItemPropertyName() + "[" + count.toString() + "]";
        final boolean newLine = (count == null);
        final List<AccountingLineTableRow> rows = getRenderableElementsForLine(groupDefinition, accountingLine, newLine, topLine, accountingLinePropertyName);

        final boolean pageIsEditable = getForm().getDocumentActions().containsKey(KRADConstants.KUALI_ACTION_CAN_EDIT);
        
     // KFSPTS-1273 : this method is final, so can't be overriden by REQ Auth.  This is a fix for REQ existing issue.  a broader solution need more work.
        // the authorizer is called by validation rule and rendertag, so has to do it here.  otherwise the invalid account will be saved.
        boolean isExistingReqAcctline = false;
        String updatedAccountNumber = KFSConstants.EMPTY_STRING;
        if (accountingLine instanceof RequisitionAccount && ((RequisitionAccount) accountingLine).getAccountIdentifier() != null) {
            if (accountingDocument.getDocumentHeader().getWorkflowDocument().getCurrentNodeNames().equals(RequisitionStatuses.NODE_ACCOUNT)) {
                RequisitionAccount dbAcctLine = (RequisitionAccount) getAccountFromDb((RequisitionAccount) accountingLine, RequisitionAccount.class);
                if (dbAcctLine != null && !StringUtils.equals(accountingLine.getAccountNumber(), dbAcctLine.getAccountNumber())) {
                    updatedAccountNumber = accountingLine.getAccountNumber();
                    accountingLine.setAccountNumber(dbAcctLine.getAccountNumber());
                    isExistingReqAcctline = true;
                }
            }
        }
        if (accountingLine instanceof PurchaseOrderAccount && ((PurchaseOrderAccount) accountingLine).getAccountIdentifier() != null) {
            if (accountingDocument.getDocumentHeader().getWorkflowDocument().getCurrentNodeNames().equals(RequisitionStatuses.NODE_ACCOUNT)) {
                PurchaseOrderAccount dbAcctLine = (PurchaseOrderAccount) getAccountFromDb((PurchaseOrderAccount) accountingLine, PurchaseOrderAccount.class);
                if (dbAcctLine != null && !StringUtils.equals(accountingLine.getAccountNumber(), dbAcctLine.getAccountNumber())) {
                    updatedAccountNumber = accountingLine.getAccountNumber();
                    accountingLine.setAccountNumber(dbAcctLine.getAccountNumber());
                    isExistingReqAcctline = true;
                }
            }
        }
        if (accountingLine instanceof PaymentRequestAccount && ((PaymentRequestAccount) accountingLine).getAccountIdentifier() != null) {
            if (accountingDocument.getDocumentHeader().getWorkflowDocument().getCurrentNodeNames().contains(RequisitionStatuses.NODE_ACCOUNT)) {
                PaymentRequestAccount dbAcctLine = (PaymentRequestAccount) getAccountFromDb((PaymentRequestAccount) accountingLine,
                        PaymentRequestAccount.class);
                if (dbAcctLine != null && !StringUtils.equals(accountingLine.getAccountNumber(), dbAcctLine.getAccountNumber())) {
                    updatedAccountNumber = accountingLine.getAccountNumber();
                    accountingLine.setAccountNumber(dbAcctLine.getAccountNumber());
                    isExistingReqAcctline = true;
                }
            }
        }
        if (isExistingReqAcctline) {
            accountingLine.setAccountNumber(updatedAccountNumber);
        }

        return new RenderableAccountingLineContainer(getForm(), accountingLine, accountingLinePropertyName, rows, count, groupDefinition.getGroupLabel(),
                getErrors(), groupDefinition.getAccountingLineAuthorizer(), groupDefinition.getAccountingLineAuthorizer()
                    .hasEditPermissionOnAccountingLine(getDocument(), accountingLine, getCollectionPropertyName(), currentUser, pageIsEditable));        
    }
    
    private PurApAccountingLineBase getAccountFromDb(PurApAccountingLineBase accountingLine, Class clazz) {
        Map<String, Object> primaryKeys = new HashMap<String, Object>();
        primaryKeys.put("accountIdentifier", accountingLine.getAccountIdentifier());
        return (PurApAccountingLineBase)SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(clazz, primaryKeys);
    }
}
