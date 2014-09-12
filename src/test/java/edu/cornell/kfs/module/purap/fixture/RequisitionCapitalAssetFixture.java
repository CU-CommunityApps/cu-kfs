package edu.cornell.kfs.module.purap.fixture;

import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItemCapitalAsset;
import org.kuali.kfs.module.purap.businessobject.RequisitionCapitalAssetItem;

public enum RequisitionCapitalAssetFixture{

    REC1 {
        @Override
        public RequisitionCapitalAssetItem newRecord() {
        	RequisitionCapitalAssetItem obj = new RequisitionCapitalAssetItem();
        	
            obj.setCapitalAssetItemIdentifier(1200);
            obj.setCapitalAssetSystemIdentifier(1100);
            return obj;
        };
    };
    
    public abstract RequisitionCapitalAssetItem newRecord();
}
