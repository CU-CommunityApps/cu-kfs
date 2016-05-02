package edu.cornell.kfs.module.purap.document.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLineBase;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.service.impl.PurapServiceImpl;
import org.kuali.kfs.module.purap.util.PurApItemUtils;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.NonTransactional;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kns.util.KNSGlobalVariables;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.exception.InfrastructureException;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.module.purap.businessobject.IWantView;

@NonTransactional
public class CuPurapServiceImpl extends PurapServiceImpl {

    // ==== CU Customization (KFSPTS-1656): Save IWantDocument routing data. ====
    @Override
    public void saveRoutingDataForRelatedDocuments(Integer accountsPayablePurchasingDocumentLinkIdentifier) {
        super.saveRoutingDataForRelatedDocuments(accountsPayablePurchasingDocumentLinkIdentifier);
        
        try {
            // Save IWNT routing data.
            @SuppressWarnings("unchecked")
            List<IWantView> iWantViews = getRelatedViews(IWantView.class, accountsPayablePurchasingDocumentLinkIdentifier);
            for (Iterator<IWantView> iterator = iWantViews.iterator(); iterator.hasNext();) {
                IWantView view = (IWantView) iterator.next();
                Document doc = documentService.getByDocumentHeaderId(view.getDocumentNumber());
                doc.getDocumentHeader().getWorkflowDocument().saveDocumentData();
            }

            
        } catch (WorkflowException e) {
            throw new InfrastructureException("unable to save routing data for related docs", e);
        }
    }

    // ==== CU Customization (KFSPTS-1656): Get IWantDocument views. ====
    @Override
    public List<String> getRelatedDocumentIds(Integer accountsPayablePurchasingDocumentLinkIdentifier) {
        List<String> documentIdList = super.getRelatedDocumentIds(accountsPayablePurchasingDocumentLinkIdentifier);

        // Get IWNT views.
        @SuppressWarnings("unchecked")
        List<IWantView> iWantViews = getRelatedViews(IWantView.class, accountsPayablePurchasingDocumentLinkIdentifier);
        for (Iterator<IWantView> iterator = iWantViews.iterator(); iterator.hasNext();) {
            IWantView view = (IWantView) iterator.next();
            documentIdList.add(view.getDocumentNumber());
        }

        return documentIdList;
    }
    
    public void prorateForTradeInAndFullOrderDiscount(PurchasingAccountsPayableDocument purDoc) {

        if (purDoc instanceof VendorCreditMemoDocument){
            throw new RuntimeException("This method not applicable for VCM documents");
        }

        //TODO: are we throwing sufficient errors in this method?
        PurApItem fullOrderDiscount = null;
        PurApItem tradeIn = null;
        KualiDecimal totalAmount = KualiDecimal.ZERO;
        KualiDecimal totalTaxAmount = KualiDecimal.ZERO;

        List<PurApAccountingLine> distributedAccounts = null;
        List<SourceAccountingLine> summaryAccounts = null;

        // iterate through below the line and grab FoD and TrdIn.
        for (PurApItem item : purDoc.getItems()) {
            if (item.getItemTypeCode().equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_ORDER_DISCOUNT_CODE)) {
                fullOrderDiscount = item;
            }
            else if (item.getItemTypeCode().equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_TRADE_IN_CODE)) {
                tradeIn = item;
            }
        }
        // If Discount is not null or zero get proration list for all non misc items and set (if not empty?)
        if (fullOrderDiscount != null &&
            fullOrderDiscount.getExtendedPrice() != null &&
            fullOrderDiscount.getExtendedPrice().isNonZero()) {

            // empty
            KNSGlobalVariables.getMessageList().add("Full order discount accounts cleared and regenerated");
            fullOrderDiscount.getSourceAccountingLines().clear();
            //total amount is pretax dollars
            totalAmount = purDoc.getTotalDollarAmountAboveLineItems().subtract(purDoc.getTotalTaxAmountAboveLineItems());
            totalTaxAmount = purDoc.getTotalTaxAmountAboveLineItems();

            //Before we generate account summary, we should update the account amounts first.
            purapAccountingService.updateAccountAmounts(purDoc);

            //calculate tax
            boolean salesTaxInd = SpringContext.getBean(ParameterService.class).getParameterValueAsBoolean( KfsParameterConstants.PURCHASING_DOCUMENT.class, PurapParameterConstants.ENABLE_SALES_TAX_IND);
            boolean useTaxIndicator = purDoc.isUseTaxIndicator();

            if(salesTaxInd == true && (ObjectUtils.isNull(fullOrderDiscount.getItemTaxAmount()) && useTaxIndicator == false)){
                KualiDecimal discountAmount = fullOrderDiscount.getExtendedPrice();
                KualiDecimal discountTaxAmount = discountAmount.divide(totalAmount).multiply(totalTaxAmount);

                fullOrderDiscount.setItemTaxAmount(discountTaxAmount);
            }

            //generate summary
            summaryAccounts = purapAccountingService.generateSummary(PurApItemUtils.getAboveTheLineOnly(purDoc.getItems()));

            if (summaryAccounts.size() == 0) {
                if (purDoc.shouldGiveErrorForEmptyAccountsProration()) {
                    GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY, PurapKeyConstants.ERROR_SUMMARY_ACCOUNTS_LIST_EMPTY, "full order discount");
                }
            } else {
                //prorate accounts
                distributedAccounts = purapAccountingService.generateAccountDistributionForProration(summaryAccounts, totalAmount.add(totalTaxAmount), 2, fullOrderDiscount.getAccountingLineClass());

                for (PurApAccountingLine distributedAccount : distributedAccounts) {
                    // KFSPTS-2200 : set item, so it can be verified as discount when validating
                    if (distributedAccount instanceof PurApAccountingLineBase) {
                        ((PurApAccountingLineBase)distributedAccount).setDiscountTradeIn(true);
                    }
                   BigDecimal percent = distributedAccount.getAccountLinePercent();
                    BigDecimal roundedPercent = new BigDecimal(Math.round(percent.doubleValue()));
                    distributedAccount.setAccountLinePercent(roundedPercent);
                }

                //update amounts on distributed accounts
                purapAccountingService.updateAccountAmountsWithTotal(distributedAccounts, totalAmount, fullOrderDiscount.getTotalAmount());

                fullOrderDiscount.setSourceAccountingLines(distributedAccounts);
            }
        } else if(fullOrderDiscount != null &&
                 (fullOrderDiscount.getExtendedPrice() == null || fullOrderDiscount.getExtendedPrice().isZero())) {
           fullOrderDiscount.getSourceAccountingLines().clear();
        }

        // If tradeIn is not null or zero get proration list for all non misc items and set (if not empty?)
        if (tradeIn != null && tradeIn.getExtendedPrice() != null && tradeIn.getExtendedPrice().isNonZero()) {

            tradeIn.getSourceAccountingLines().clear();

            totalAmount = purDoc.getTotalDollarAmountForTradeIn();
            KualiDecimal tradeInTotalAmount = tradeIn.getTotalAmount();
            //Before we generate account summary, we should update the account amounts first.
            purapAccountingService.updateAccountAmounts(purDoc);

            //Before generating the summary, lets replace the object code in a cloned accounts collection sothat we can
            //consolidate all the modified object codes during summary generation.
            List<PurApItem> clonedTradeInItems = new ArrayList<PurApItem>();
            Collection<String> objectSubTypesRequiringQty = new ArrayList<String>( SpringContext.getBean(ParameterService.class).getParameterValuesAsString(KfsParameterConstants.PURCHASING_DOCUMENT.class, PurapParameterConstants.OBJECT_SUB_TYPES_REQUIRING_QUANTITY) );
            Collection<String> purchasingObjectSubTypes = new ArrayList<String>( SpringContext.getBean(ParameterService.class).getParameterValuesAsString( KfsParameterConstants.CAPITAL_ASSET_BUILDER_DOCUMENT.class, PurapParameterConstants.PURCHASING_OBJECT_SUB_TYPES) );

             String tradeInCapitalObjectCode = SpringContext.getBean(ParameterService.class).getParameterValueAsString(PurapConstants.PURAP_NAMESPACE, "Document", "TRADE_IN_OBJECT_CODE_FOR_CAPITAL_ASSET");
             String tradeInCapitalLeaseObjCd = SpringContext.getBean(ParameterService.class).getParameterValueAsString(PurapConstants.PURAP_NAMESPACE, "Document", "TRADE_IN_OBJECT_CODE_FOR_CAPITAL_LEASE");

            for(PurApItem item : purDoc.getTradeInItems()){
               PurApItem cloneItem = (PurApItem)ObjectUtils.deepCopy(item);
               List<PurApAccountingLine> sourceAccountingLines = cloneItem.getSourceAccountingLines();
               for(PurApAccountingLine accountingLine : sourceAccountingLines){
                  if(objectSubTypesRequiringQty.contains(accountingLine.getObjectCode().getFinancialObjectSubTypeCode())){
                         accountingLine.setFinancialObjectCode(tradeInCapitalObjectCode);
                  }else if(purchasingObjectSubTypes.contains(accountingLine.getObjectCode().getFinancialObjectSubTypeCode())){
                        accountingLine.setFinancialObjectCode(tradeInCapitalLeaseObjCd);
                  }
               }
               clonedTradeInItems.add(cloneItem);
            }


            summaryAccounts = purapAccountingService.generateSummary(clonedTradeInItems);
            if (summaryAccounts.size() == 0) {
                if (purDoc.shouldGiveErrorForEmptyAccountsProration()) {
                    GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY, PurapKeyConstants.ERROR_SUMMARY_ACCOUNTS_LIST_EMPTY, "trade in");
                }
            }
            else {
                distributedAccounts = purapAccountingService.generateAccountDistributionForProration(summaryAccounts, totalAmount, 2, tradeIn.getAccountingLineClass());
                for (PurApAccountingLine distributedAccount : distributedAccounts) {
                    // KFSPTS-2200 : set item, so it can be verified as discount when validating
                    if (distributedAccount instanceof PurApAccountingLineBase) {
                        ((PurApAccountingLineBase)distributedAccount).setDiscountTradeIn(true);
                    }
                    BigDecimal percent = distributedAccount.getAccountLinePercent();
                    BigDecimal roundedPercent = new BigDecimal(Math.round(percent.doubleValue()));
                    distributedAccount.setAccountLinePercent(roundedPercent);
                    // set the accountAmount same as tradeIn amount not line item's amount
                    resetAccountAmount(distributedAccount, tradeInTotalAmount);
                }
                tradeIn.setSourceAccountingLines(distributedAccounts);
            }
        }
    }

    private void resetAccountAmount(PurApAccountingLine distributedAccount, KualiDecimal tradeInTotalAmount) {
        BigDecimal pct = distributedAccount.getAccountLinePercent();
        BigDecimal amount = tradeInTotalAmount.bigDecimalValue().multiply(pct).divide(new BigDecimal(100));
        distributedAccount.setAmount(new KualiDecimal(amount));
    }

    
}
