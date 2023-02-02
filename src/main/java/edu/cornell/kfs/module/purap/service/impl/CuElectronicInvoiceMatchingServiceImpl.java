package edu.cornell.kfs.module.purap.service.impl;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.batch.ElectronicInvoiceStep;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceRejectReason;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.service.impl.ElectronicInvoiceItemHolder;
import org.kuali.kfs.module.purap.service.impl.ElectronicInvoiceMatchingServiceImpl;
import org.kuali.kfs.module.purap.service.impl.ElectronicInvoiceOrderHolder;
import org.kuali.kfs.module.purap.util.ElectronicInvoiceUtils;
import org.kuali.kfs.vnd.businessobject.PurchaseOrderCostSource;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.util.ObjectUtils;

public class CuElectronicInvoiceMatchingServiceImpl extends ElectronicInvoiceMatchingServiceImpl {
    private VendorService vendorService;

    protected void validateInvoiceItems(ElectronicInvoiceOrderHolder orderHolder){
        Set poLineNumbers = new HashSet();
        Set invLineNumbers = new HashSet();

        ElectronicInvoiceItemHolder[] itemHolders = orderHolder.getItems();
        if (itemHolders != null){
            for (int i = 0; i < itemHolders.length; i++) {
                validateInvoiceItem(itemHolders[i],poLineNumbers, invLineNumbers);
            }
        }
    }

    protected void validateInvoiceItem(ElectronicInvoiceItemHolder itemHolder, Set poLineNumbers, Set invLineNumbers){

        PurchaseOrderItem poItem = itemHolder.getPurchaseOrderItem();
        ElectronicInvoiceOrderHolder orderHolder = itemHolder.getInvoiceOrderHolder();

        boolean isNonMatching = false;
        if (poItem == null){
        	// investigating.  this should stay because the inv line should have the correcrt invitemline# set up
            String extraDescription = "Invoice Item Line Number:" + itemHolder.getInvoiceItemLineNumber();
            ElectronicInvoiceRejectReason rejectReason = createRejectReason(PurapConstants.ElectronicInvoice.NO_MATCHING_PO_ITEM,extraDescription,orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason,PurapConstants.ElectronicInvoice.RejectDocumentFields.INVOICE_ITEM_LINE_NUMBER,PurapKeyConstants.ERROR_REJECT_INVOICE__ITEM_NOMATCH);
            return;
        }

        // KFSPTS-1719 : if the invoice line number is duplicate, then error out.
        // only noqty item.  the old behavior for qty item does not check the duplicate inv line#
        if (invLineNumbers.contains(((CuElectronicInvoiceItemHolder)itemHolder).getInvLineNumber()) && poItem.isNoQtyItem()){
			String extraDescription = "Invoice Item Line Number:" + itemHolder.getInvoiceItemLineNumber();
			ElectronicInvoiceRejectReason rejectReason = createRejectReason(
					PurapConstants.ElectronicInvoice.DUPLIATE_INVOICE_LINE_ITEM, extraDescription, orderHolder.getFileName());
			orderHolder.addInvoiceOrderRejectReason(rejectReason,PurapConstants.ElectronicInvoice.RejectDocumentFields.INVOICE_ITEM_LINE_NUMBER,
							PurapKeyConstants.ERROR_REJECT_PO_ITEM_DUPLICATE);
			return;
        	
        } else {
        	invLineNumbers.add(((CuElectronicInvoiceItemHolder)itemHolder).getInvLineNumber());
        }
        if (poLineNumbers.contains(itemHolder.getInvoiceItemLineNumber())){
        	// TODO : investigating Do NOT commit.  Duplicate is not OK for qty item
			if (!poItem.isNoQtyItem()) {
	            String extraDescription = "Invoice Item Line Number:" + itemHolder.getInvoiceItemLineNumber();
	            ElectronicInvoiceRejectReason rejectReason = createRejectReason(PurapConstants.ElectronicInvoice.DUPLIATE_INVOICE_LINE_ITEM,extraDescription,orderHolder.getFileName());
	            orderHolder.addInvoiceOrderRejectReason(rejectReason,PurapConstants.ElectronicInvoice.RejectDocumentFields.INVOICE_ITEM_LINE_NUMBER,PurapKeyConstants.ERROR_REJECT_PO_ITEM_DUPLICATE);
	            return;
			} else {
				((CuElectronicInvoiceOrderHolder)orderHolder).getNonMatchItems().add((CuElectronicInvoiceItemHolder)itemHolder);
				isNonMatching = true;
			}
        }else{
            poLineNumbers.add(itemHolder.getInvoiceItemLineNumber());
        }
        // end 
        
        if (!poItem.isItemActiveIndicator()){
            String extraDescription = "PO Item Line Number:" + poItem.getItemLineNumber();
            ElectronicInvoiceRejectReason rejectReason = createRejectReason(PurapConstants.ElectronicInvoice.INACTIVE_LINE_ITEM,extraDescription,orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason,PurapConstants.ElectronicInvoice.RejectDocumentFields.INVOICE_ITEM_LINE_NUMBER,PurapKeyConstants.ERROR_REJECT_PO_ITEM_INACTIVE);
            return;
        }

        // KFSPTS-1719 skip this if po is noqty item
        if (!itemHolder.isCatalogNumberAcceptIndicatorEnabled() && !poItem.isNoQtyItem()){
            validateCatalogNumber(itemHolder);
            if (orderHolder.isInvoiceRejected()){
                return;
            }
        }

        if (!itemHolder.isUnitOfMeasureAcceptIndicatorEnabled()){
        	// KFSUPGRADE-479 : ignore UOM case
        	// KFSUPGRADE-485 : if po is no qty but inv is qty, then is it necessary to check here ?  should not matter
            if (!poItem.isNoQtyItem() && !StringUtils.equalsIgnoreCase(poItem.getItemUnitOfMeasureCode(), itemHolder.getInvoiceItemUnitOfMeasureCode())){
                String extraDescription = "Invoice UOM:" + itemHolder.getInvoiceItemUnitOfMeasureCode() + ", PO UOM:" + poItem.getItemUnitOfMeasureCode();
                ElectronicInvoiceRejectReason rejectReason = createRejectReason(PurapConstants.ElectronicInvoice.UNIT_OF_MEASURE_MISMATCH,extraDescription,orderHolder.getFileName());
                orderHolder.addInvoiceOrderRejectReason(rejectReason,PurapConstants.ElectronicInvoice.RejectDocumentFields.INVOICE_ITEM_UOM,PurapKeyConstants.ERROR_REJECT_UOM_MISMATCH);
                return;
            }
        }

        validateUnitPrice(itemHolder);

        if (orderHolder.isInvoiceRejected()){
            return;
        }

        validateSalesTax(itemHolder);

        if (orderHolder.isInvoiceRejected()){
            return;
        }

        // KFSPTS-1719, KFSUPGRADE-485 : this is more appropriate to check if item is non-qty.  because user can potentially enter qty for non-qty item
        if (!poItem.isNoQtyItem()) {
        //if (poItem.getItemQuantity() != null) {
            validateQtyBasedItem(itemHolder);
        }else{
            validateNonQtyBasedItem(itemHolder);
        }

    }

    protected void validateCatalogNumber(ElectronicInvoiceItemHolder itemHolder){
        PurchaseOrderItem poItem = itemHolder.getPurchaseOrderItem();
        ElectronicInvoiceOrderHolder orderHolder = itemHolder.getInvoiceOrderHolder();

        String invoiceCatalogNumberStripped = itemHolder.getCatalogNumberStripped();
        String poCatalogNumberStripped = ElectronicInvoiceUtils.stripSplChars(poItem.getItemCatalogNumber());

        /*
         * If Catalog number in invoice and po are not empty, create reject reason if it doesn't match
         */
        if (StringUtils.isNotBlank(invoiceCatalogNumberStripped) &&
            StringUtils.isNotBlank(poCatalogNumberStripped)){

            if (!StringUtils.equalsIgnoreCase(poCatalogNumberStripped, invoiceCatalogNumberStripped)){

                String extraDescription = "Invoice Catalog No:" + invoiceCatalogNumberStripped + ", PO Catalog No:" + poCatalogNumberStripped;
                ElectronicInvoiceRejectReason rejectReason = createRejectReason(PurapConstants.ElectronicInvoice.CATALOG_NUMBER_MISMATCH,extraDescription,orderHolder.getFileName());
                orderHolder.addInvoiceOrderRejectReason(rejectReason,PurapConstants.ElectronicInvoice.RejectDocumentFields.INVOICE_ITEM_CATALOG_NUMBER,PurapKeyConstants.ERROR_REJECT_CATALOG_MISMATCH);
            }

        }else{

            /*
             * If catalog number is empty in PO/&Invoice, check whether the catalog check is required for the requisition source.
             * If exists in param, create reject reason.
             * If not exists, continue with UOM and unit price match.
             */
            String reqSourceRequiringCatalogMatch = getParameterService().getParameterValueAsString(ElectronicInvoiceStep.class, PurapParameterConstants.ElectronicInvoiceParameters.REQUISITION_SOURCES_REQUIRING_CATALOG_MATCHING);
            String requisitionSourceCodeInPO = orderHolder.getPurchaseOrderDocument().getRequisitionSourceCode();

            if (StringUtils.isNotEmpty(reqSourceRequiringCatalogMatch)){
                String[] requisitionSourcesFromParam = StringUtils.split(reqSourceRequiringCatalogMatch,';');
                if (ArrayUtils.contains(requisitionSourcesFromParam, requisitionSourceCodeInPO)){
                    String extraDescription = "Invoice Catalog No:" + invoiceCatalogNumberStripped + ", PO Catalog No:" + poItem.getItemCatalogNumber();
                    ElectronicInvoiceRejectReason rejectReason = createRejectReason(PurapConstants.ElectronicInvoice.CATALOG_NUMBER_MISMATCH,extraDescription,orderHolder.getFileName());
                    orderHolder.addInvoiceOrderRejectReason(rejectReason,PurapConstants.ElectronicInvoice.RejectDocumentFields.INVOICE_ITEM_CATALOG_NUMBER,PurapKeyConstants.ERROR_REJECT_CATALOG_MISMATCH);
                }
            }
        }
    }

    protected void validateNonQtyBasedItem(ElectronicInvoiceItemHolder itemHolder){
        PurchaseOrderItem poItem = itemHolder.getPurchaseOrderItem();

        String fileName = itemHolder.getInvoiceOrderHolder().getFileName();
        ElectronicInvoiceOrderHolder orderHolder = itemHolder.getInvoiceOrderHolder();
        // KFSPTS-1719
        // Only validation is that the invoice amount (amount of PayReq) can not be greater than the extended cost minus amount paid 
       if (itemHolder.getInvoiceItemSubTotalAmount().setScale(KualiDecimal.SCALE, KualiDecimal.ROUND_BEHAVIOR).compareTo(poItem.getExtendedPrice().subtract(poItem.getItemInvoicedTotalAmount()).bigDecimalValue()) > 0) {
           String extraDescription = "Invoice Item Line Number:" + itemHolder.getInvoiceItemLineNumber();
           ElectronicInvoiceRejectReason rejectReason = createRejectReason(PurapConstants.ElectronicInvoice.PO_ITEM_AMT_LESSTHAN_INVOICE_ITEM_AMT,extraDescription,orderHolder.getFileName());
           orderHolder.addInvoiceOrderRejectReason(rejectReason,PurapConstants.ElectronicInvoice.RejectDocumentFields.INVOICE_ITEM_LINE_NUMBER,PurapKeyConstants.ERROR_REJECT_POITEM_LESS_OUTSTANDING_EMCUMBERED_AMOUNT);
           return;
    	   
       }

       if (KualiDecimal.ZERO.compareTo(poItem.getItemOutstandingEncumberedAmount()) >= 0) {
            //we have no dollars left encumbered on the po item
            String extraDescription = "Invoice Item Line Number:" + itemHolder.getInvoiceItemLineNumber();
            ElectronicInvoiceRejectReason rejectReason = createRejectReason(PurapConstants.ElectronicInvoice.OUTSTANDING_ENCUMBERED_AMT_AVAILABLE,extraDescription,orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason,PurapConstants.ElectronicInvoice.RejectDocumentFields.INVOICE_ITEM_LINE_NUMBER,PurapKeyConstants.ERROR_REJECT_POITEM_OUTSTANDING_EMCUMBERED_AMOUNT);
            return;
        }else{
            //we have encumbered dollars left on PO
        	// KFSUPGRADE-485
            if (itemHolder.getInvoiceItemSubTotalAmount().setScale(KualiDecimal.SCALE, KualiDecimal.ROUND_BEHAVIOR)
                    .compareTo(poItem.getItemOutstandingEncumberedAmount().bigDecimalValue()) > 0
            		|| getItemTotalAmount(itemHolder).setScale(KualiDecimal.SCALE, KualiDecimal.ROUND_BEHAVIOR)
            		        .compareTo(poItem.getItemOutstandingEncumberedAmount().bigDecimalValue()) > 0) {
                String extraDescription = "Invoice Item Line Number:" + itemHolder.getInvoiceItemLineNumber();
                ElectronicInvoiceRejectReason rejectReason = createRejectReason(PurapConstants.ElectronicInvoice.PO_ITEM_AMT_LESSTHAN_INVOICE_ITEM_AMT,extraDescription,orderHolder.getFileName());
                orderHolder.addInvoiceOrderRejectReason(rejectReason,PurapConstants.ElectronicInvoice.RejectDocumentFields.INVOICE_ITEM_LINE_NUMBER,PurapKeyConstants.ERROR_REJECT_POITEM_LESS_OUTSTANDING_EMCUMBERED_AMOUNT);
                return;
            }
        }
    }

    // KFSPTS-1719,KFSUPGRADE-485
    // trying to get the total inv amount for this poline
    private BigDecimal getItemTotalAmount(ElectronicInvoiceItemHolder itemHolder) {
        BigDecimal totalAmount = new BigDecimal(0);
    	Integer lineItemNumber = itemHolder.getInvoiceItemLineNumber();
    	if (lineItemNumber != null) {
    	 for (ElectronicInvoiceItemHolder item : itemHolder.getInvoiceOrderHolder().getItems()) {
    		 if (item.getInvoiceItemLineNumber() != null && item.getInvoiceItemLineNumber().equals(lineItemNumber)) {
    			 totalAmount = totalAmount.add(item.getInvoiceItemSubTotalAmount());
    		 }
    	 }
    	}
    	 return totalAmount;
    }
    
    protected void validateUnitPrice(ElectronicInvoiceItemHolder itemHolder){
        PurchaseOrderCostSource costSource = itemHolder.getInvoiceOrderHolder().getPurchaseOrderDocument().getPurchaseOrderCostSource();
        PurchaseOrderItem poItem = itemHolder.getPurchaseOrderItem();
        ElectronicInvoiceOrderHolder orderHolder = itemHolder.getInvoiceOrderHolder();

        String extraDescription = "Invoice Item Line Number:" + itemHolder.getInvoiceItemLineNumber();

        BigDecimal actualVariance = itemHolder.getInvoiceItemUnitPrice().subtract(poItem.getItemUnitPrice());

        BigDecimal lowerPercentage = null;
        if (costSource.getItemUnitPriceLowerVariancePercent() != null){
            //Checking for lower variance
            lowerPercentage = costSource.getItemUnitPriceLowerVariancePercent();
        }
        else {
            //If the cost source itemUnitPriceLowerVariancePercent is null then we'll use the exact match (100%).
            lowerPercentage = new BigDecimal(100);
        }

        BigDecimal lowerAcceptableVariance = lowerPercentage.divide(new BigDecimal(100))
                .multiply(poItem.getItemUnitPrice()).negate();

        // KFSUPGRADE-485
        if (!poItem.isNoQtyItem() && lowerAcceptableVariance.compareTo(actualVariance) > 0) {
            ElectronicInvoiceRejectReason rejectReason = createRejectReason(PurapConstants.ElectronicInvoice.INVOICE_AMT_LESSER_THAN_LOWER_VARIANCE, extraDescription, orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason, PurapConstants.ElectronicInvoice.RejectDocumentFields.INVOICE_ITEM_UNIT_PRICE, PurapKeyConstants.ERROR_REJECT_UNITPRICE_LOWERVARIANCE);
        }

        BigDecimal upperPercentage = null;

        if (costSource.getItemUnitPriceUpperVariancePercent() != null){
            //Checking for upper variance
            upperPercentage = costSource.getItemUnitPriceUpperVariancePercent();
        }
        else {
            //If the cost source itemUnitPriceLowerVariancePercent is null then we'll use the exact match (100%).
            upperPercentage = new BigDecimal(100);
        }
        BigDecimal upperAcceptableVariance = upperPercentage.divide(new BigDecimal(100))
                .multiply(poItem.getItemUnitPrice());

        if (upperAcceptableVariance.compareTo(actualVariance) < 0) {
            ElectronicInvoiceRejectReason rejectReason = createRejectReason(PurapConstants.ElectronicInvoice.INVOICE_AMT_GREATER_THAN_UPPER_VARIANCE, extraDescription, orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason, PurapConstants.ElectronicInvoice.RejectDocumentFields.INVOICE_ITEM_UNIT_PRICE, PurapKeyConstants.ERROR_REJECT_UNITPRICE_UPPERVARIANCE);
        }

    }

    // KFSUPGRADE-482
    
    protected void validatePurchaseOrderMatch(ElectronicInvoiceOrderHolder orderHolder){

        String poIDFieldName = PurapConstants.ElectronicInvoice.RejectDocumentFields.INVOICE_PO_ID;
        String poID = orderHolder.getInvoicePurchaseOrderID();

        if (StringUtils.isEmpty(poID)){
            ElectronicInvoiceRejectReason rejectReason = createRejectReason(PurapConstants.ElectronicInvoice.PO_ID_EMPTY,null,orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason,poIDFieldName,PurapKeyConstants.ERROR_REJECT_INVOICE_POID_EMPTY);
            return;
        }

        String extraDesc = "Invoice Order ID:" + poID;

        if (!NumberUtils.isDigits(poID)){
            ElectronicInvoiceRejectReason rejectReason = createRejectReason(PurapConstants.ElectronicInvoice.PO_ID_INVALID_FORMAT,extraDesc,orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason,poIDFieldName,PurapKeyConstants.ERROR_REJECT_INVOICE_POID_INVALID);
            return;
        }

        // KFSUPGRADE-482 : if number is too large
        try {       	
        	Integer.parseInt(poID);        	
        } catch (NumberFormatException nfe) {
            ElectronicInvoiceRejectReason rejectReason = createRejectReason(PurapConstants.ElectronicInvoice.PO_NOT_EXISTS,extraDesc,orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason,poIDFieldName,PurapKeyConstants.ERROR_REJECT_INVOICE__PO_NOT_EXISTS);
            return;
        }
        
        
        PurchaseOrderDocument poDoc = orderHolder.getPurchaseOrderDocument();

        if (poDoc == null){
            ElectronicInvoiceRejectReason rejectReason = createRejectReason(PurapConstants.ElectronicInvoice.PO_NOT_EXISTS,extraDesc,orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason,poIDFieldName,PurapKeyConstants.ERROR_REJECT_INVOICE__PO_NOT_EXISTS);
            return;
        }

        if (!poDoc.getApplicationDocumentStatus().equals(PurchaseOrderStatuses.APPDOC_OPEN)) {
            ElectronicInvoiceRejectReason rejectReason = createRejectReason(PurapConstants.ElectronicInvoice.PO_NOT_OPEN,null,orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason,poIDFieldName,PurapKeyConstants.ERROR_REJECT_INVOICE_PO_CLOSED);
            return;
        }

        if (!eInvoiceVendorMatchesPOVendor(poDoc, orderHolder)) {
            ElectronicInvoiceRejectReason rejectReason = createRejectReason(PurapConstants.ElectronicInvoice.PO_VENDOR_NOT_MATCHES_WITH_INVOICE_VENDOR,null,orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason);
            return;
        }

    }
    
    private boolean eInvoiceVendorMatchesPOVendor(PurchaseOrderDocument poDoc, ElectronicInvoiceOrderHolder orderHolder) {        
        VendorDetail vendorByDunsDetail = vendorService.getVendorByDunsNumber(orderHolder.getDunsNumber());

        if (poDoc.getVendorHeaderGeneratedIdentifier() == null || poDoc.getVendorDetailAssignedIdentifier() == null
                || ObjectUtils.isNull(vendorByDunsDetail)) {
            return false;
        }
        
        return poDoc.getVendorHeaderGeneratedIdentifier().equals(vendorByDunsDetail.getVendorHeaderGeneratedIdentifier());
    }

    public void setVendorService(VendorService vendorService) {
        super.setVendorService(vendorService);
        this.vendorService = vendorService;
    }



}
