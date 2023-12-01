package edu.cornell.kfs.module.purap.service.impl;

import java.util.Map;

import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceItem;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceRejectItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.service.impl.ElectronicInvoiceItemHolder;
import org.kuali.kfs.module.purap.service.impl.ElectronicInvoiceOrderHolder;

public class CuElectronicInvoiceItemHolder extends ElectronicInvoiceItemHolder {
	// TODO : rejectitem and invoiceitem are not sure yet.  need further check.
    private ElectronicInvoiceRejectItem rejectItem;
    private ElectronicInvoiceItem invoiceItem;

    public CuElectronicInvoiceItemHolder(
            final ElectronicInvoiceRejectItem rejectItem, final Map itemTypeMappings,
            final PurchaseOrderItem poItem, final ElectronicInvoiceOrderHolder orderHolder){
    	super(rejectItem, itemTypeMappings, poItem, orderHolder);
    	this.rejectItem = rejectItem;
    }
    
    public CuElectronicInvoiceItemHolder(
            final ElectronicInvoiceItem invoiceItem, final Map itemTypeMappings,
            final PurchaseOrderItem poItem, final ElectronicInvoiceOrderHolder orderHolder){
    	super(invoiceItem, itemTypeMappings, poItem, orderHolder);
    	this.invoiceItem = invoiceItem;

    }
    
    // KFSPTS-1719
    public String getReferenceDescription() {
    	if (invoiceItem == null) {
    		return rejectItem.getInvoiceReferenceItemDescription();
    	}
    	return invoiceItem.getReferenceDescription();
    }
    
    public String getInvLineNumber() {
    	if (invoiceItem == null) {
    		return rejectItem.getInvoiceItemLineNumber().toString();
    	}
    	return invoiceItem.getInvoiceLineNumber();
    }
    
}
