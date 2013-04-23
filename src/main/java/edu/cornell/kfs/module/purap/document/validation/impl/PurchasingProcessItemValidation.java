/**
 * 
 */
package edu.cornell.kfs.module.purap.document.validation.impl;

import java.util.List;

import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.PurchasingDocument;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.MessageMap;
import org.kuali.rice.kns.util.ObjectUtils;

import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;

/**
 * @author dwf5
 *
 */
public class PurchasingProcessItemValidation extends PurchasingAccountsPayableProcessItemValidation {

	/* (non-Javadoc)
	 * @see org.kuali.kfs.sys.document.validation.Validation#validate(org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent)
	 */
	public boolean validate(AttributedDocumentEvent event) {
        boolean valid = true;
        PurchasingDocument purDocument = (PurchasingDocument) event.getDocument();
        MessageMap errorMap = GlobalVariables.getMessageMap();
        errorMap.clearErrorPath();
        errorMap.addToErrorPath(PurapConstants.ITEM_TAB_ERRORS);
        
        // Check that item isn't a non-qty item on an e-invoice vendor order
    	VendorDetail vendor = purDocument.getVendorDetail();
        if(ObjectUtils.isNotNull(vendor) && ((VendorDetailExtension)vendor.getExtension()).isEinvoiceVendorIndicator()) {
	        // Check that there aren't any req items that already have non-qty values entered
	        List<PurApItem> reqItems = purDocument.getItems();
	        if(!reqItems.isEmpty()) {
	            for(PurApItem item : reqItems) {
	                if(PurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE.equalsIgnoreCase(item.getItemTypeCode())) {
	                    // Throw error that the non-qty items are not allowed if the vendor is an einvoice vendor
	                    errorMap.addToErrorPath(PurapPropertyConstants.NEW_PURCHASING_ITEM_LINE);
	                    errorMap.putError(PurapPropertyConstants.ITEM_TYPE, CUPurapKeyConstants.PURAP_ITEM_NONQTY, item.getItemLineNumber().toString(), purDocument.getVendorName()); 
	                    errorMap.removeFromErrorPath(PurapPropertyConstants.NEW_PURCHASING_ITEM_LINE);
	                    valid &= false;
	                }
	            }
	        }
        }
        errorMap.clearErrorPath();
        return valid;

	}	
	
}
