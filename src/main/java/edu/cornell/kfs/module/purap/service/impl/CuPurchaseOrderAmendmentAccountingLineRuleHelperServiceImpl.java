package edu.cornell.kfs.module.purap.service.impl;

import org.kuali.kfs.module.purap.service.impl.PurchasingAccountingLineRuleHelperServiceImpl;
import org.kuali.kfs.sys.businessobject.AccountingLine;

public class CuPurchaseOrderAmendmentAccountingLineRuleHelperServiceImpl extends PurchasingAccountingLineRuleHelperServiceImpl {

    /**
     * Overridden to allow expired accounts if the users have configured the appropriate override fields on the document,
     * and to skip the no-expired-accounts validation from the PurchasingAccountingLineRuleHelperServiceImpl parent class.
     * 
     * @see org.kuali.kfs.module.purap.service.impl.PurchasingAccountingLineRuleHelperServiceImpl#hasRequiredOverrides(
     * org.kuali.kfs.sys.businessobject.AccountingLine, java.lang.String)
     */
    @Override
    public boolean hasRequiredOverrides(AccountingLine line, String overrideCode) {
        return hasAccountRequiredOverrides(line, overrideCode);
    }

}
