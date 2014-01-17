package edu.cornell.kfs.module.purap.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestAccount;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLineBase;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderAccount;
import org.kuali.kfs.module.purap.businessobject.RequisitionAccount;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.service.impl.PurapAccountingServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.identity.KfsKimAttributes;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.role.RoleService;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.GlobalVariables;

import edu.cornell.kfs.module.purap.service.CuPurapAccountingService;


public class CuPurapAccountingServiceImpl extends PurapAccountingServiceImpl implements CuPurapAccountingService {

    public boolean isFiscalOfficersForAllAcctLines(PurchasingAccountsPayableDocument document) {

        boolean isFoForAcctLines = true;
        String personId = GlobalVariables.getUserSession().getPrincipalId();
        for (SourceAccountingLine accountingLine : (List<SourceAccountingLine>)document.getSourceAccountingLines()) {
            List<String> fiscalOfficers = new ArrayList<String>();
            Map<String,String> roleQualifier = new HashMap<String,String>();
            // KFSPTS-1273 : this method is final, so can't be overriden by REQ Auth.  This is a fix for REQ existing issue.  a broader solution need more work.
            // the authorizer is called by validation rule and rendertag, so has to do it here.  otherwise the invalid account will be saved.
            boolean isExistingReqAcctline = false;
            String updatedAccountNumber = KFSConstants.EMPTY_STRING;
            if (accountingLine instanceof RequisitionAccount && ((RequisitionAccount) accountingLine).getAccountIdentifier() != null) {
                RequisitionAccount dbAcctLine = (RequisitionAccount) getAccountFromDb((RequisitionAccount) accountingLine, RequisitionAccount.class);
                if (dbAcctLine != null && !StringUtils.equals(accountingLine.getAccountNumber(), dbAcctLine.getAccountNumber())) {
                    updatedAccountNumber = accountingLine.getAccountNumber();
                    accountingLine.setAccountNumber(dbAcctLine.getAccountNumber());
                    isExistingReqAcctline = true;
                }
            }
            if (accountingLine instanceof PurchaseOrderAccount && ((PurchaseOrderAccount) accountingLine).getAccountIdentifier() != null) {
                PurchaseOrderAccount dbAcctLine = (PurchaseOrderAccount) getAccountFromDb((PurchaseOrderAccount) accountingLine, PurchaseOrderAccount.class);
                if (dbAcctLine != null && !StringUtils.equals(accountingLine.getAccountNumber(), dbAcctLine.getAccountNumber())) {
                    updatedAccountNumber = accountingLine.getAccountNumber();
                    accountingLine.setAccountNumber(dbAcctLine.getAccountNumber());
                    isExistingReqAcctline = true;
                }
            }
            if (accountingLine instanceof PaymentRequestAccount && ((PaymentRequestAccount) accountingLine).getAccountIdentifier() != null) {
                PaymentRequestAccount dbAcctLine = (PaymentRequestAccount) 
                        getAccountFromDb((PaymentRequestAccount) accountingLine, PaymentRequestAccount.class);
                if (dbAcctLine != null && !StringUtils.equals(accountingLine.getAccountNumber(), dbAcctLine.getAccountNumber())) {
                    updatedAccountNumber = accountingLine.getAccountNumber();
                    accountingLine.setAccountNumber(dbAcctLine.getAccountNumber());
                    isExistingReqAcctline = true;
                }
            }

            roleQualifier.put(KimConstants.AttributeConstants.DOCUMENT_NUMBER,document.getDocumentNumber());
            roleQualifier.put(KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME, document.getDocumentHeader().getWorkflowDocument().getDocumentTypeName());
            roleQualifier.put(KfsKimAttributes.FINANCIAL_DOCUMENT_TOTAL_AMOUNT,
                    document.getFinancialSystemDocumentHeader().getFinancialDocumentTotalAmount().toString());
            roleQualifier.put(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE,accountingLine.getChartOfAccountsCode());
            roleQualifier.put(KfsKimAttributes.ACCOUNT_NUMBER,accountingLine.getAccountNumber());
            fiscalOfficers.addAll(SpringContext.getBean(RoleService.class).getRoleMemberPrincipalIds(KFSConstants.ParameterNamespaces.KFS,
                    KFSConstants.SysKimApiConstants.FISCAL_OFFICER_KIM_ROLE_NAME,roleQualifier));
            if (!fiscalOfficers.contains(personId)) {
                fiscalOfficers.addAll(SpringContext.getBean(RoleService.class).getRoleMemberPrincipalIds(
                                        KFSConstants.ParameterNamespaces.KFS,KFSConstants.SysKimApiConstants.FISCAL_OFFICER_PRIMARY_DELEGATE_KIM_ROLE_NAME,
                                        roleQualifier));
            }
            if (!fiscalOfficers.contains(personId)) {
                fiscalOfficers.addAll(SpringContext.getBean(RoleService.class).getRoleMemberPrincipalIds(KFSConstants.ParameterNamespaces.KFS,
                                        KFSConstants.SysKimApiConstants.FISCAL_OFFICER_SECONDARY_DELEGATE_KIM_ROLE_NAME,roleQualifier));
            }
            if (isExistingReqAcctline) {
                accountingLine.setAccountNumber(updatedAccountNumber);
            }
            if (!fiscalOfficers.contains(personId)) {
                isFoForAcctLines = false;
                break;
            }
        }

        return isFoForAcctLines;
    }
    private PurApAccountingLineBase getAccountFromDb(PurApAccountingLineBase accountingLine, Class clazz) {
        Map<String, Object> primaryKeys = new HashMap<String, Object>();
        primaryKeys.put("accountIdentifier", accountingLine.getAccountIdentifier());
        return (PurApAccountingLineBase)SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(clazz, primaryKeys);
    }

}
