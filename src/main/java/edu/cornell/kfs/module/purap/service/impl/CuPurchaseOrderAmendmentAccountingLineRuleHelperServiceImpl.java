package edu.cornell.kfs.module.purap.service.impl;

import org.kuali.kfs.module.purap.service.impl.PurchasingAccountingLineRuleHelperServiceImpl;
import org.kuali.kfs.sys.businessobject.AccountingLine;

public class CuPurchaseOrderAmendmentAccountingLineRuleHelperServiceImpl extends PurchasingAccountingLineRuleHelperServiceImpl {

    /**
     * Overridden to behave like the PurapAccountingLineRuleHelperServiceImpl "grandparent" class and just return true,
     * to skip the expired account validation from the PurchasingAccountingLineRuleHelperServiceImpl parent class.
     * 
     * @see org.kuali.kfs.module.purap.service.impl.PurchasingAccountingLineRuleHelperServiceImpl#hasRequiredOverrides(
     * org.kuali.kfs.sys.businessobject.AccountingLine, java.lang.String)
     */
    @Override
    public boolean hasRequiredOverrides(AccountingLine line, String overrideCode) {
        return true;
    }

}
