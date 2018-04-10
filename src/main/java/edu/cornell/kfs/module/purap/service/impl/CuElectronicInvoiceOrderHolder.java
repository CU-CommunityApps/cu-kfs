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
    private List<CuElectronicInvoiceItemHolder> items = new ArrayList<CuElectronicInvoiceItemHolder>();

    public CuElectronicInvoiceOrderHolder(ElectronicInvoiceRejectDocument rejectDocument,
            Map itemTypeMappings,
            Map itemTypes) {
          super(rejectDocument, itemTypeMappings, itemTypes);
          for (int i = 0; i < rejectDocument.getInvoiceRejectItems().size(); i++) {
              
              ElectronicInvoiceRejectItem invoiceRejectItem = rejectDocument.getInvoiceRejectItems().get(i);
              
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
    
    public CuElectronicInvoiceOrderHolder(ElectronicInvoice eInvoice,
            ElectronicInvoiceOrder invoiceOrder,
            PurchaseOrderDocument poDocument,
            Map itemTypeMappings,
            Map itemTypes,
            boolean validateHeader){
    	super(eInvoice, invoiceOrder, poDocument, itemTypeMappings, itemTypes, validateHeader);
        for (int i = 0; i < invoiceOrder.getInvoiceItems().size(); i++) {

            ElectronicInvoiceItem orderItem = invoiceOrder.getInvoiceItems().get(i);
            
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
        	CuElectronicInvoiceItemHolder[] returnItems = new CuElectronicInvoiceItemHolder[items.size()];
            items.toArray(returnItems);
            return returnItems;
        }
        return null;
    }
    
    public CuElectronicInvoiceItemHolder getItemByLineNumber(int lineNumber){
        
        if (items != null){
            for (int i = 0; i < items.size(); i++) {
            	CuElectronicInvoiceItemHolder itemHolder = items.get(i);
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

	public void setNonMatchItems(List<CuElectronicInvoiceItemHolder> nonMatchItems) {
		this.nonMatchItems = nonMatchItems;
	}

	public CuElectronicInvoiceItemHolder getMisMatchItem() {
		return misMatchItem;
	}

	public void setMisMatchItem(CuElectronicInvoiceItemHolder misMatchItem) {
		this.misMatchItem = misMatchItem;
	}
    
}
