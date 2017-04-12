package edu.cornell.kfs.sys.service.impl;

import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.util.FavoriteAccountLineBuilderTest;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.sys.businessobject.AccountingLineBase;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/*
 * Test-only UserFavoriteAccountService implementation.
 */
public class TestUserFavoriteAccountServiceImpl extends UserFavoriteAccountServiceImpl {

    @Override
    protected void populateAccountNumberOnPurApAccountingLine(FavoriteAccount account, PurApAccountingLine acctLine) {
        // To avoid Spring calls, we need to set the account number field manually instead of using the setter.
        try {
            Field accountNumberField = AccountingLineBase.class.getDeclaredField("accountNumber");
            accountNumberField.setAccessible(true);
            accountNumberField.set(acctLine, account.getAccountNumber());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void refreshReferenceObjectsForPopulatedAccountingLine(GeneralLedgerPendingEntrySourceDetail acctLine) {
        // Do nothing.
    }

}
