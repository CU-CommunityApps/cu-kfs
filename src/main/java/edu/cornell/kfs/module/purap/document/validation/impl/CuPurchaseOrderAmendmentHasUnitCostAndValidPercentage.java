package edu.cornell.kfs.module.purap.document.validation.impl;

import java.math.BigDecimal;
import java.util.List;

import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderAmendmentDocument;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.krad.util.GlobalVariables;

import edu.cornell.kfs.module.purap.document.CuPurchaseOrderAmendmentDocument;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class CuPurchaseOrderAmendmentHasUnitCostAndValidPercentage extends GenericValidation {

	public boolean validate(AttributedDocumentEvent event) {
		boolean valid = true;
		PurchasingAccountsPayableDocument purapDocument = (PurchasingAccountsPayableDocument)event.getDocument();
        List<PurApItem> items = purapDocument.getItems();
        for (PurApItem item : items) {
        	if (item.isConsideredEntered()) {
	        	BigDecimal unitPrice = ((PurchaseOrderItem)item).getItemUnitPrice();
	        	List<PurApAccountingLine> lines = item.getSourceAccountingLines();
	        	//check if unit price is zero or null and item has accounts associated with it
	        	if ( (unitPrice==null || (unitPrice.compareTo(BigDecimal.ZERO)==0)) && lines.size()>0) { 
	                GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_TAB_ERRORS, CUKFSKeyConstants.ERROR_NO_UNIT_COST_WITH_ACCOUNTS, item.getItemIdentifierString()/*documentType*/);
	                valid = false;
	        	}
	        	if ( (unitPrice!=null && unitPrice.compareTo(BigDecimal.ZERO)!=0) && lines.size()==0) {
	        		GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_TAB_ERRORS, CUKFSKeyConstants.ERROR_UNIT_COST_W_O_ACCOUNT, item.getItemIdentifierString());
	        		valid = false;
	        	}
	        	BigDecimal totalPercent = new BigDecimal(0);
	        	for (PurApAccountingLine accountingLine : lines) {
	        		totalPercent = totalPercent.add( accountingLine.getAccountLinePercent() );        		
	        		//if an account distribution is zero percent, invalid
	        		if (accountingLine.getAccountLinePercent().compareTo(BigDecimal.ZERO)==0) {
	                    GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY, CUKFSKeyConstants.ERROR_NO_ZERO_PERCENT_ACCOUNT_LINES_ALLOWED, item.getItemIdentifierString());
	        			valid = false;
	        		}
	        	}
	        	//if total percent is not 100, error
	        	if (totalPercent.compareTo(new BigDecimal(100)) != 0) {
	        		// KFSPTS-1769.  if it is spawnpoa for unordered item, then don't check
	        		if (!((purapDocument instanceof CuPurchaseOrderAmendmentDocument) && ((CuPurchaseOrderAmendmentDocument)purapDocument).isSpawnPoa())) {
	                    GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY, PurapKeyConstants.ERROR_ITEM_ACCOUNTING_TOTAL, item.getItemIdentifierString());
		                valid = false;
	        		}
	        	}
        	}
        }
		
		return valid;
	}
	

}
