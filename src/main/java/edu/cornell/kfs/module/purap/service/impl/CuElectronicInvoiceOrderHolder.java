package edu.cornell.kfs.module.purap.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.module.purap.businessobject.ElectronicInvoice;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceItem;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceOrder;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceRejectItem;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.ElectronicInvoiceRejectDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.service.impl.ElectronicInvoiceOrderHolder;

public class CuElectronicInvoiceOrderHolder extends ElectronicInvoiceOrderHolder {
    // KFSPTS-1719
    private List<CuElectronicInvoiceItemHolder> nonMatchItems;
    private CuElectronicInvoiceItemHolder misMatchItem;
    private final List<CuElectronicInvoiceItemHolder> items = new ArrayList<CuElectronicInvoiceItemHolder>();

    public CuElectronicInvoiceOrderHolder(
            final ElectronicInvoiceRejectDocument rejectDocument,
            final Map itemTypeMappings,
            final Map itemTypes) {
          super(rejectDocument, itemTypeMappings, itemTypes);
          for (int i = 0; i < rejectDocument.getInvoiceRejectItems().size(); i++) {
              
              final ElectronicInvoiceRejectItem invoiceRejectItem = rejectDocument.getInvoiceRejectItems().get(i);
              
              PurApItem poItem = null;
              if (rejectDocument.getCurrentPurchaseOrderDocument() != null){
                  try{
                      poItem = rejectDocument.getCurrentPurchaseOrderDocument().getItemByLineNumber(invoiceRejectItem.getInvoiceReferenceItemLineNumber());
                  }catch(NullPointerException e){
                      /**
                       * Not needed to handle this invalid item here, this will be handled in the matching process 
                       */
                  }
              }
              
              items.add(new CuElectronicInvoiceItemHolder(invoiceRejectItem,itemTypeMappings,poItem == null ? null : (PurchaseOrderItem)poItem,this));
          }
    }
    
    public CuElectronicInvoiceOrderHolder(
            final ElectronicInvoice eInvoice,
            final ElectronicInvoiceOrder invoiceOrder,
            final PurchaseOrderDocument poDocument,
            final Map itemTypeMappings,
            final Map itemTypes,
            final boolean validateHeader){
    	super(eInvoice, invoiceOrder, poDocument, itemTypeMappings, itemTypes, validateHeader);
        for (int i = 0; i < invoiceOrder.getInvoiceItems().size(); i++) {

            final ElectronicInvoiceItem orderItem = invoiceOrder.getInvoiceItems().get(i);
            
            PurApItem poItem = null;
            if (poDocument != null){
                try{
                    poItem = poDocument.getItemByLineNumber(orderItem.getReferenceLineNumberInteger());
                }catch(NullPointerException e){
                    /**
                     * Not needed to handle this invalid item here, this will be handled in the matching process 
                     */
                }
            }
            
            items.add(new CuElectronicInvoiceItemHolder(orderItem,itemTypeMappings,poItem == null ? null : (PurchaseOrderItem)poItem,this));
        }
    }

    public CuElectronicInvoiceItemHolder[] getItems() {
        if (items != null){
        	final CuElectronicInvoiceItemHolder[] returnItems = new CuElectronicInvoiceItemHolder[items.size()];
            items.toArray(returnItems);
            return returnItems;
        }
        return null;
    }
    
    public CuElectronicInvoiceItemHolder getItemByLineNumber(final int lineNumber){
        
        if (items != null){
            for (int i = 0; i < items.size(); i++) {
                final CuElectronicInvoiceItemHolder itemHolder = items.get(i);
                if (itemHolder.getInvoiceItemLineNumber() != null && itemHolder.getInvoiceItemLineNumber().intValue() == lineNumber) {
                    return itemHolder;
                }
            }
        }
        return null;
    }
	public List<CuElectronicInvoiceItemHolder> getNonMatchItems() {
		if (nonMatchItems == null) {
			nonMatchItems = new ArrayList<CuElectronicInvoiceItemHolder>();
		}
		return nonMatchItems;
	}

	public void setNonMatchItems(final List<CuElectronicInvoiceItemHolder> nonMatchItems) {
		this.nonMatchItems = nonMatchItems;
	}

	public CuElectronicInvoiceItemHolder getMisMatchItem() {
		return misMatchItem;
	}

	public void setMisMatchItem(final CuElectronicInvoiceItemHolder misMatchItem) {
		this.misMatchItem = misMatchItem;
	}
    
}
