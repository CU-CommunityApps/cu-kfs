package edu.cornell.kfs.module.purap.document.service;

import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.vnd.businessobject.ContractManager;

public interface CuB2BPurchaseOrderService {
	
	/**
     * Returns the cxml of the Purchase Order for electronic transmission to the vendor
     * 
     * @param purchaseOrder         PurchaseOrderDocument - PO data
     * @param requisitionInitiator  Person - user that created the Requisition
     * @param password              String - password for PO transmission
     * @param contractManager       ContractManager - contract manager for the PO
     * @param contractManagerEmail  String - email address for the contract manager
     * @param vendorDuns            String - vendor DUNS number for the PO
     * @param includeNewFields      boolean 
     * @return String which is the cxml of the PO to send to the vendor
     */
    public String getCxml(PurchaseOrderDocument purchaseOrder, String requisitionInitiatorId, String password, ContractManager contractManager, String contractManagerEmail, String vendorDuns, boolean includeNewFields);

}
