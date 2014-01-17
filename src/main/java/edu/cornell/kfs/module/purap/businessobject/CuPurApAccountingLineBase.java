package edu.cornell.kfs.module.purap.businessobject;

import org.kuali.kfs.module.purap.businessobject.PurApAccountingLineBase;

public class CuPurApAccountingLineBase extends PurApAccountingLineBase {

    // KFSPTS-2200
    private boolean discountTradeIn;
    
    public boolean isDiscountTradeIn() {
        return discountTradeIn;
    }

    public void setDiscountTradeIn(boolean discountTradeIn) {
        this.discountTradeIn = discountTradeIn;
    }

}
