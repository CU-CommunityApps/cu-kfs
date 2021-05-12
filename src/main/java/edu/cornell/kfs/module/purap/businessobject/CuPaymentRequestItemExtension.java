package edu.cornell.kfs.module.purap.businessobject;

import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;

public class CuPaymentRequestItemExtension extends PersistableBusinessObjectBase
        implements PersistableBusinessObjectExtension {

    private Integer itemIdentifier;
    // KFSPTS-1719
    private Integer invLineNumber;
    // TODO : probably need to do more when add an item, the extension should be set this preqItem
    private PaymentRequestItem preqItem; 
    

    public KualiDecimal getPoOutstandingAmountForDisplay() {
        PurchaseOrderItem poi = getPreqItem().getPurchaseOrderItem();
        if(ObjectUtils.isNull(getPreqItem().getPurchaseOrderItemUnitPrice()) || KualiDecimal.ZERO.equals(getPreqItem().getPurchaseOrderItemUnitPrice())){
            return null;
        }else{
        	if (getPreqItem().getPurchaseOrderItemUnitPrice().compareTo(poi.getItemUnitPrice()) == 0) {
            return this.getPoOutstandingAmount(poi);
        	} else {
        		// KFSPTS-1719 : if no qty with several int no qty. be caureful to make change here
        		// maybe should create another method of preq
        		// this may cause problem when doing preq validation because it may return 0.  this is suppose for preqitemtag
        		return new KualiDecimal(getPreqItem().getPurchaseOrderItemUnitPrice()).subtract(getPreqItem().getExtendedPrice());
        	}
        }
    }

    private KualiDecimal getPoOutstandingAmount(PurchaseOrderItem poi) {
        if (poi == null) {
            return KualiDecimal.ZERO;
        }
        else {
            return poi.getItemOutstandingEncumberedAmount();
        }
    }
 

	public Integer getLineNumber() {
		if (invLineNumber == null && getPreqItem().getItemLineNumber() != null) {
			invLineNumber = getPreqItem().getItemLineNumber();
			return getPreqItem().getItemLineNumber();
		}
		return invLineNumber;
	}

	public Integer getItemIdentifier() {
		return itemIdentifier;
	}

	public void setItemIdentifier(Integer itemIdentifier) {
		this.itemIdentifier = itemIdentifier;
	}

	public Integer getInvLineNumber() {
		return invLineNumber;
	}

	public void setInvLineNumber(Integer invLineNumber) {
		this.invLineNumber = invLineNumber;
	}

	public PaymentRequestItem getPreqItem() {
		if (ObjectUtils.isNull(preqItem)) {
			this.refreshReferenceObject("preqItem");
		}
		return preqItem;
	}

	public void setPreqItem(PaymentRequestItem preqItem) {
		this.preqItem = preqItem;
	}

}
