package edu.cornell.kfs.module.purap.document.validation.impl;

import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;

import org.kuali.kfs.module.purap.document.validation.impl.PurchasingAccountsPayableAccountPercentBetween0And100Validation;
import org.kuali.kfs.krad.util.GlobalVariables;

public class CuPurchasingAccountsPayableAccountPercentBetween0And100Validation extends PurchasingAccountsPayableAccountPercentBetween0And100Validation{

private PurApAccountingLine accountingLine;
    
    @Override
    public boolean validate(final AttributedDocumentEvent event) {
        boolean valid = true;
        final double pct = accountingLine.getAccountLinePercent().doubleValue();
        
        if (pct <= 0 || pct > 100) {
            if (CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorPath())) {
                // ReviewAccountingEvent & UpdateAccountingLineEvent need this for work around now.
                // the messagamap's errorpath got cleared somehow
                GlobalVariables.getMessageMap().putError(event.getErrorPathPrefix() + "." + PurapPropertyConstants.ACCOUNT_LINE_PERCENT, PurapKeyConstants.ERROR_ITEM_PERCENT, "%", accountingLine.getAccountNumber());
            } else {
                GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ACCOUNT_LINE_PERCENT, PurapKeyConstants.ERROR_ITEM_PERCENT, "%", accountingLine.getAccountNumber());
            }
            valid = false;
        }

        return valid;
    }

    public PurApAccountingLine getAccountingLine() {
        return accountingLine;
    }

    public void setAccountingLine(final PurApAccountingLine accountingLine) {
        this.accountingLine = accountingLine;
    }

}
