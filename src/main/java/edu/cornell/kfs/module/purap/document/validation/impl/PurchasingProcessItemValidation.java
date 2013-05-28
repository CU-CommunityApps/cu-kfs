/**
 * 
 */
package edu.cornell.kfs.module.purap.document.validation.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.PurchasingDocument;
import org.kuali.kfs.sys.KFSConstants;
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
//        errorMap.addToErrorPath(PurapConstants.ITEM_TAB_ERRORS);
        
        // KFSPTS-1719 :  remove this validation 
//        // Check that item isn't a non-qty item on an e-invoice vendor order
	        // Check that there aren't any req items that already have non-qty values entered
	        List<PurApItem> reqItems = purDocument.getItems();
	        // KFSPTS-2096
	        if(!reqItems.isEmpty()) {
	        	String itemTypeCode = KFSConstants.EMPTY_STRING;
	        	int i = 0;
	            for(PurApItem item : reqItems) {
	            	if (StringUtils.isEmpty(itemTypeCode) && (PurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE.equalsIgnoreCase(item.getItemTypeCode()) ||
	            			PurapConstants.ItemTypeCodes.ITEM_TYPE_ITEM_CODE.equalsIgnoreCase(item.getItemTypeCode()))) {
	            		itemTypeCode = item.getItemTypeCode();
	            	}
	                if(StringUtils.isNotBlank(itemTypeCode) && (PurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE.equalsIgnoreCase(item.getItemTypeCode()) ||
	            			PurapConstants.ItemTypeCodes.ITEM_TYPE_ITEM_CODE.equalsIgnoreCase(item.getItemTypeCode())) && !itemTypeCode.equalsIgnoreCase(item.getItemTypeCode())) {
	                    // Throw error that mix qty and no-qty
	                 //   errorMap.addToErrorPath("document.item["+ i + "].itemTypeCode");
	                    errorMap.putError("document.item["+ i + "].itemTypeCode", CUPurapKeyConstants.PURAP_MIX_ITEM_QTY_NONQTY); 
	               //     errorMap.removeFromErrorPath("document.item["+ i + "].itemTypeCode");
	                    valid &= false;
	                }
	            	if (PurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE.equalsIgnoreCase(item.getItemTypeCode()) ||
	            			PurapConstants.ItemTypeCodes.ITEM_TYPE_ITEM_CODE.equalsIgnoreCase(item.getItemTypeCode())) {
	            		i++;
	            	}
	            }
	        }
//        errorMap.clearErrorPath();
        return valid;

	}	
	
}
