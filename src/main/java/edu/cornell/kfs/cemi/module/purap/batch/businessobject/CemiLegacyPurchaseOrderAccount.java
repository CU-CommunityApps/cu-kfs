package edu.cornell.kfs.cemi.module.purap.batch.businessobject;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

public class CemiLegacyPurchaseOrderAccount {

    private Integer accountIdentifier;
    private KualiDecimal itemAccountOutstandingEncumbranceAmount;

    public Integer getAccountIdentifier() {
        return accountIdentifier;
    }

    public void setAccountIdentifier(final Integer accountIdentifier) {
        this.accountIdentifier = accountIdentifier;
    }

    public KualiDecimal getItemAccountOutstandingEncumbranceAmount() {
        return itemAccountOutstandingEncumbranceAmount;
    }

    public void setItemAccountOutstandingEncumbranceAmount(
            final KualiDecimal itemAccountOutstandingEncumbranceAmount) {
        this.itemAccountOutstandingEncumbranceAmount = itemAccountOutstandingEncumbranceAmount;
    }

}
