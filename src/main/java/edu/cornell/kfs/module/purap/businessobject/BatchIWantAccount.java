package edu.cornell.kfs.module.purap.businessobject;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

public class BatchIWantAccount extends IWantAccount {
	
	public void setAmountOrPercent(String amountOrPercent){
		this.setAmountOrPercent(new KualiDecimal(amountOrPercent));
	}

}
