package edu.cornell.kfs.module.purap.businessobject;

import java.math.BigDecimal;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

public class BatchIWantItem extends IWantItem {
	
	public void setItemUnitPrice(String itemUnitPrice){
		this.setItemUnitPrice(new BigDecimal(itemUnitPrice));
	}

	public void setItemQuantity(String itemQuantity){
		this.setItemQuantity(new KualiDecimal(itemQuantity));
	}
}
