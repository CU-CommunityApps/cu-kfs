package edu.cornell.kfs.module.purap.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestAccount;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLineBase;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderAccount;
import org.kuali.kfs.module.purap.businessobject.RequisitionAccount;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocumentBase;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.validation.event.PurchasingAccountsPayableItemPreCalculateEvent;
import org.kuali.kfs.module.purap.service.impl.PurapAccountingServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.identity.KfsKimAttributes;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.rice.kim.api.role.RoleService;
import org.kuali.kfs.krad.service.KualiRuleService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.module.purap.service.CuPurapAccountingService;


public class CuPurapAccountingServiceImpl extends PurapAccountingServiceImpl implements CuPurapAccountingService {

    private RoleService roleService;
    
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
            fiscalOfficers.addAll(roleService.getRoleMemberPrincipalIds(KFSConstants.CoreModuleNamespaces.KFS,
                    KFSConstants.SysKimApiConstants.FISCAL_OFFICER_KIM_ROLE_NAME,roleQualifier));
            if (!fiscalOfficers.contains(personId)) {
                fiscalOfficers.addAll(roleService.getRoleMemberPrincipalIds(
                                        KFSConstants.CoreModuleNamespaces.KFS,KFSConstants.SysKimApiConstants.FISCAL_OFFICER_PRIMARY_DELEGATE_KIM_ROLE_NAME,
                                        roleQualifier));
            }
            if (!fiscalOfficers.contains(personId)) {
                fiscalOfficers.addAll(roleService.getRoleMemberPrincipalIds(KFSConstants.CoreModuleNamespaces.KFS,
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
        return (PurApAccountingLineBase)businessObjectService.findByPrimaryKey(clazz, primaryKeys);
    }
    
    /**
     * @see org.kuali.kfs.module.purap.service.PurapAccountingService#updateAccountAmounts(org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument)
     */
    @Override
    public void updateAccountAmounts(PurchasingAccountsPayableDocument document) {

        PurchasingAccountsPayableDocumentBase purApDocument = (PurchasingAccountsPayableDocumentBase) document;
        String accountDistributionMethod = purApDocument.getAccountDistributionMethod();

        WorkflowDocument workflowDocument = purApDocument.getDocumentHeader().getWorkflowDocument();

        Set<String> nodeNames = workflowDocument.getCurrentNodeNames();

        // the percent at fiscal approve
        // don't update if past the AP review level
        if ((document instanceof PaymentRequestDocument) && purapService.isFullDocumentEntryCompleted(document)) {
            if (nodeNames.contains(PaymentRequestStatuses.NODE_PAYMENT_METHOD_REVIEW)) {  
                // CU needs this update because the customization allows Treasury Manager to change unit/extended price and 'calculate'
                for (PurApItem item : document.getItems()) {
                    updatePreqItemAccountAmountsOnly(item);
                }
            } else {
               convertMoneyToPercent((PaymentRequestDocument) document);
            }
            return;
        }
        document.fixItemReferences();

        // if distribution method is sequential and document is PREQ or VCM then...
        if (((document instanceof PaymentRequestDocument) || (document instanceof VendorCreditMemoDocument)) && PurapConstants.AccountDistributionMethodCodes.SEQUENTIAL_CODE.equalsIgnoreCase(accountDistributionMethod)) {
            if (document instanceof VendorCreditMemoDocument) {
                VendorCreditMemoDocument cmDocument = (VendorCreditMemoDocument) document;
                cmDocument.updateExtendedPriceOnItems();

                for (PurApItem item : document.getItems()) {
                    for (PurApAccountingLine account : item.getSourceAccountingLines()) {
                        account.setAmount(KualiDecimal.ZERO);
                    }
                }
            }

            // update the accounts amounts for PREQ and distribution method = sequential
            for (PurApItem item : document.getItems()) {
                updatePreqItemAccountAmounts(item);
            }

            return;
        }

        // if distribution method is proportional and document is PREQ or VCM then...
        if (((document instanceof PaymentRequestDocument) || (document instanceof VendorCreditMemoDocument)) && PurapConstants.AccountDistributionMethodCodes.PROPORTIONAL_CODE.equalsIgnoreCase(accountDistributionMethod)) {
            // update the accounts amounts for PREQ and distribution method = sequential
            if (document instanceof VendorCreditMemoDocument) {
                VendorCreditMemoDocument cmDocument = (VendorCreditMemoDocument) document;
                cmDocument.updateExtendedPriceOnItems();

                for (PurApItem item : document.getItems()) {
                    for (PurApAccountingLine account : item.getSourceAccountingLines()) {
                        account.setAmount(KualiDecimal.ZERO);
                    }
                }
            }

            for (PurApItem item : document.getItems()) {
                boolean rulePassed = true;
                // check any business rules
                rulePassed &= kualiRuleService.applyRules(new PurchasingAccountsPayableItemPreCalculateEvent(document, item));

                if (rulePassed) {
                    updatePreqProportionalItemAccountAmounts(item);
                }
            }

            return;
        }

        //No recalculate if the account distribution method code is equal to "S" sequential ON REQ or POs..
        if (PurapConstants.AccountDistributionMethodCodes.SEQUENTIAL_CODE.equalsIgnoreCase(accountDistributionMethod)) {
            for (PurApItem item : document.getItems()) {
                boolean rulePassed = true;
                // check any business rules
                rulePassed &= kualiRuleService.applyRules(new PurchasingAccountsPayableItemPreCalculateEvent(document, item));

                return;
            }
        }

        // do recalculate only if the account distribution method code is not equal to "S" sequential method.
        if (!PurapConstants.AccountDistributionMethodCodes.SEQUENTIAL_CODE.equalsIgnoreCase(accountDistributionMethod)) {
            for (PurApItem item : document.getItems()) {
                boolean rulePassed = true;
                // check any business rules
                rulePassed &= kualiRuleService.applyRules(new PurchasingAccountsPayableItemPreCalculateEvent(document, item));

                if (rulePassed) {
                    updateItemAccountAmounts(item);
                }
            }
        }
    }

    /*
     * Updates PREQ accounting line amounts only when Treasury Manager is doing 'calculate'.
     * Currently, the foundation 'updateAccountAmounts' is based on the saved accounting line percentage which is rounded to 2 decimal digits.
     * If the total amount is changed by Treasury Manager, and the calculation is based on 'percentage' saved, then it will cause some amount fraction rounding issue.
     * This is reported in KFSPTS-3644.  You can see the detail explanation of the rounding issues.
     */
    private void updatePreqItemAccountAmountsOnly(PurApItem item) {
        List<PurApAccountingLine> sourceAccountingLines = item.getSourceAccountingLines();
        KualiDecimal totalAmount = item.getTotalAmount();
        if (ObjectUtils.isNull(totalAmount) || totalAmount.equals(KualiDecimal.ZERO)) {
            totalAmount = getExtendedPrice(item);
        }

        if (!totalAmount.equals(KualiDecimal.ZERO) && getItemAccountTotal(sourceAccountingLines).equals(KualiDecimal.ZERO)) {
           // item.refreshReferenceObject("sourceAccountingLines"); // this is not working
            // The item account totals can become '0' if Treasury Manager set the unit price to '0 by accident and do calculate.
            // We need to refresh the accounting line amount from DB in order to continue.
            refreshAccountAmount(item);
        }
        
        if (StringUtils.equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_MISC_CODE,item.getItemTypeCode()) || StringUtils.equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_FREIGHT_CODE,item.getItemTypeCode())
                || StringUtils.equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_SHIP_AND_HAND_CODE,item.getItemTypeCode())) {
            if (totalAmount.isNonZero() && getItemAccountTotal(sourceAccountingLines).isZero()) {
                updateMiscFrtSphdAccountAmountsWithTotal(sourceAccountingLines, totalAmount);
            } else {
                updatePreqAccountAmountsOnly(sourceAccountingLines, totalAmount);
            }
        } else {
            updatePreqAccountAmountsOnly(sourceAccountingLines, totalAmount);
        }
    }

    private KualiDecimal getItemAccountTotal(List<PurApAccountingLine> sourceAccountingLines) {
        KualiDecimal accountTotal = KualiDecimal.ZERO;
       for (PurApAccountingLine account : sourceAccountingLines) {
            accountTotal = accountTotal.add(account.getAmount());                
        }
       return accountTotal;

    }
    
    private KualiDecimal getExtendedPrice(PurApItem item) {
        KualiDecimal extendedPrice = KualiDecimal.ZERO;
        if (ObjectUtils.isNotNull(item.getItemUnitPrice())) {
            if (item.getItemType().isAmountBasedGeneralLedgerIndicator()) {
                // SERVICE ITEM: return unit price as extended price
                extendedPrice = new KualiDecimal(item.getItemUnitPrice().toString());
            }
            else if (ObjectUtils.isNotNull(item.getItemQuantity())) {
                BigDecimal calcExtendedPrice = item.getItemUnitPrice().multiply(item.getItemQuantity().bigDecimalValue());
                // ITEM TYPE (qty driven): return (unitPrice x qty)
                extendedPrice = new KualiDecimal(calcExtendedPrice.setScale(KualiDecimal.SCALE, KualiDecimal.ROUND_BEHAVIOR));
            }
        }
        return extendedPrice;
    }

    /*
     * the calculation is based on the percentage of new total amount divided by the existing item totals.
     * The calculated percentage is high precision (20 decimal digits).
     * Then this calculated percentage is used to update each accounting line amount accordingly.
     */
    private <T extends PurApAccountingLine> void updatePreqAccountAmountsOnly(List<T> sourceAccountingLines, KualiDecimal totalAmount) {
        if ((totalAmount != null) && KualiDecimal.ZERO.compareTo(totalAmount) != 0) {
            KualiDecimal accountTotal = getItemAccountTotal((List<PurApAccountingLine>)sourceAccountingLines);
            if ((!accountTotal.equals(totalAmount)  || isAnyAccountLinePercentEmpty((List<PurApAccountingLine>)sourceAccountingLines)) && !accountTotal.equals(KualiDecimal.ZERO)) {
                BigDecimal tmpPercent = totalAmount.bigDecimalValue().divide(accountTotal.bigDecimalValue(), PurapConstants.CREDITMEMO_PRORATION_SCALE.intValue(), KualiDecimal.ROUND_BEHAVIOR);

                int accountNum = 0;
                accountTotal = KualiDecimal.ZERO;
                BigDecimal percentTotalRoundUp = BigDecimal.ZERO;
                for (T account : sourceAccountingLines) {
                    if (accountNum++ < sourceAccountingLines.size() - 1) {
                        if (ObjectUtils.isNotNull(account.getAmount())) {
                            BigDecimal calcAmountBd = tmpPercent.multiply(account.getAmount().bigDecimalValue());
                            calcAmountBd = calcAmountBd.setScale(KualiDecimal.SCALE, KualiDecimal.ROUND_BEHAVIOR);
                            KualiDecimal calcAmount = new KualiDecimal(calcAmountBd);
                            account.setAmount(calcAmount);
                            if (ObjectUtils.isNull(account.getAccountLinePercent()) || account.getAccountLinePercent().compareTo(BigDecimal.ZERO) == 0) {
                                // Tax Item is regenerated if Treasury Manager 'calculate'
                                BigDecimal tmpAcctPercent = account.getAmount().bigDecimalValue().divide(totalAmount.bigDecimalValue(), PurapConstants.CREDITMEMO_PRORATION_SCALE.intValue(), KualiDecimal.ROUND_BEHAVIOR);
                                account.setAccountLinePercent(tmpAcctPercent.multiply(new BigDecimal(100)));
                            }
                            accountTotal = accountTotal.add(calcAmount);
                        }
                    } else {
                        account.setAmount(totalAmount.subtract(accountTotal));
                        if (ObjectUtils.isNull(account.getAccountLinePercent()) || account.getAccountLinePercent().compareTo(BigDecimal.ZERO) == 0) {
                            account.setAccountLinePercent(new BigDecimal(100).subtract(percentTotalRoundUp));
                        }
                   }
                    percentTotalRoundUp = percentTotalRoundUp.add(account.getAccountLinePercent());
                }
            }

        }
        else {
            for (T account : sourceAccountingLines) {
                account.setAmount(KualiDecimal.ZERO);
            }
        }
    }

    private boolean isAnyAccountLinePercentEmpty(List<PurApAccountingLine> sourceAccountingLines) {
        boolean isAccountLinePercentEmpty = false;;
        if (CollectionUtils.isNotEmpty(sourceAccountingLines)) {
            for (PurApAccountingLine account : sourceAccountingLines) {
                if (ObjectUtils.isNull(account.getAccountLinePercent()) || account.getAccountLinePercent().compareTo(BigDecimal.ZERO) == 0) {
                    isAccountLinePercentEmpty = true;
                    break;
                }
            }
        }
        return isAccountLinePercentEmpty;
    }
    
    private void refreshAccountAmount(PurApItem item) {
            Map fieldValues = new HashMap();
            fieldValues.put(PurapPropertyConstants.ITEM_IDENTIFIER, item.getItemIdentifier());
            PurApItem orgItem = businessObjectService.findByPrimaryKey(PaymentRequestItem.class, fieldValues);
            if (ObjectUtils.isNotNull(orgItem) && CollectionUtils.isNotEmpty(item.getSourceAccountingLines()) && CollectionUtils.isNotEmpty(orgItem.getSourceAccountingLines())) {
                for (PurApAccountingLine account : item.getSourceAccountingLines()) {
                    for (PurApAccountingLine orgAccount : orgItem.getSourceAccountingLines()) {
                        if (account.getAccountIdentifier().equals(orgAccount.getAccountIdentifier())) {
                            account.setAmount(orgAccount.getAmount());
                        }
                    }
                }
            }
    }

    /*
     * This is for MISC, Freight, SPHD item.  
     * Treasury can change the item amount.  If it is from 0 to some specific amount, then it has to use this method to 
     * calculate amount based on accounting line percentage.
     */
    private <T extends PurApAccountingLine> void updateMiscFrtSphdAccountAmountsWithTotal(List<T> sourceAccountingLines, KualiDecimal totalAmount) {
        if (ObjectUtils.isNotNull(totalAmount) && totalAmount.isNonZero()) {
            KualiDecimal accountTotal = KualiDecimal.ZERO;
            BigDecimal accountTotalPercent = BigDecimal.ZERO;
            T lastAccount = null;

            for (T account : sourceAccountingLines) {
                if (ObjectUtils.isNotNull(account.getAccountLinePercent()) || ObjectUtils.isNotNull(account.getAmount())) {
                    if (ObjectUtils.isNotNull(account.getAccountLinePercent())) {
                        BigDecimal pct = new BigDecimal(account.getAccountLinePercent().toString()).divide(new BigDecimal(100));
                        account.setAmount(new KualiDecimal(pct.multiply(new BigDecimal(totalAmount.toString())).setScale(KualiDecimal.SCALE, KualiDecimal.ROUND_BEHAVIOR)));
                    }
                }

                if (ObjectUtils.isNotNull(account.getAmount())) {
                    accountTotal = accountTotal.add(account.getAmount());
                }
                if (ObjectUtils.isNotNull(account.getAccountLinePercent())) {
                    accountTotalPercent = accountTotalPercent.add(account.getAccountLinePercent());
                }

                lastAccount = account;
            }

            // put excess on last account
            if (ObjectUtils.isNotNull(lastAccount)) {
                KualiDecimal difference = totalAmount.subtract(accountTotal);
                if (ObjectUtils.isNotNull(lastAccount.getAmount())) {
                    lastAccount.setAmount(lastAccount.getAmount().add(difference));
                }

                BigDecimal percentDifference = new BigDecimal(100).subtract(accountTotalPercent).setScale(BIG_DECIMAL_SCALE);
                if (ObjectUtils.isNotNull(lastAccount.getAccountLinePercent())) {
                    lastAccount.setAccountLinePercent(lastAccount.getAccountLinePercent().add(percentDifference));
                }
            }
        }
        else {
            for (T account : sourceAccountingLines) {
                account.setAmount(KualiDecimal.ZERO);
            }
        }
    }
    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

}
