package edu.cornell.kfs.sys.service.impl;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.businessobject.IWantAccount;
import edu.cornell.kfs.module.purap.businessobject.TestableRequisitionAccount;
import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.service.impl.fixture.FavoriteAccountFixture;
import edu.cornell.kfs.sys.service.impl.fixture.UserProcurementProfileFixture;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.krad.service.impl.BusinessObjectServiceImpl;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderAccount;
import org.kuali.kfs.sys.businessobject.AccountingLineBase;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.bo.BusinessObject;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CuUserFavoriteAccountServiceImplTest {
    private UserFavoriteAccountServiceImpl userFavoriteAccountService;

    @Before
    public void setUp() throws Exception {
        userFavoriteAccountService = new TestUserFavoriteAccountServiceImpl();
        userFavoriteAccountService.setBusinessObjectService(new MockBusinessObjectServiceImpl());
    }

    @Test
    public void testPrimaryFavoriteAccount() {
        List<FavoriteAccount> favoriteAccounts = buildFavoriteAccountList();
        FavoriteAccount favoriteAccount = userFavoriteAccountService.getFavoriteAccount(favoriteAccounts.get(0).getUserProcurementProfile().getPrincipalId());
        Assert.assertTrue("Favorite Account should exist", favoriteAccount != null);
        Assert.assertTrue("should be Primary favorite account", favoriteAccount.getPrimaryInd());
        
        favoriteAccount = userFavoriteAccountService.getFavoriteAccount("2");
        Assert.assertTrue("Favorite Account should not exist", favoriteAccount == null);
    }

    private List<FavoriteAccount> buildFavoriteAccountList() {
        List<FavoriteAccount> favoriteAccounts = new ArrayList<>();

        favoriteAccounts.add(FavoriteAccountFixture.FAVORITE_ACCOUNT_1.createFavoriteAccount());
        favoriteAccounts.add(FavoriteAccountFixture.FAVORITE_ACCOUNT_2.createFavoriteAccount());
        favoriteAccounts.add(FavoriteAccountFixture.FAVORITE_ACCOUNT_3.createFavoriteAccount());
        favoriteAccounts.add(FavoriteAccountFixture.FAVORITE_ACCOUNT_4.createFavoriteAccount());
        favoriteAccounts.add(FavoriteAccountFixture.FAVORITE_ACCOUNT_5.createFavoriteAccount());
        favoriteAccounts.add(FavoriteAccountFixture.FAVORITE_ACCOUNT_6.createFavoriteAccount());
        favoriteAccounts.add(FavoriteAccountFixture.FAVORITE_ACCOUNT_7.createFavoriteAccount());
        favoriteAccounts.add(FavoriteAccountFixture.FAVORITE_ACCOUNT_8.createFavoriteAccount());
        favoriteAccounts.add(FavoriteAccountFixture.FAVORITE_ACCOUNT_9.createFavoriteAccount());
        favoriteAccounts.add(FavoriteAccountFixture.FAVORITE_ACCOUNT_10.createFavoriteAccount());

        return favoriteAccounts;
    }

    @Test
    public void testPopulatedNewAccount() {
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("primaryInd", "Y");
        FavoriteAccount favoriteAccount = FavoriteAccountFixture.FAVORITE_ACCOUNT_1.createFavoriteAccount();

        GeneralLedgerPendingEntrySourceDetail iwntAccount;
        
        PurApAccountingLine acct = userFavoriteAccountService.getPopulatedNewAccount(favoriteAccount, TestableRequisitionAccount.class);
        Assert.assertNotNull("Account should be populated", acct);
        Assert.assertTrue("Account should be REQS Account", acct instanceof TestableRequisitionAccount);
        Assert.assertEquals("Account Number should be populated", favoriteAccount.getAccountNumber(), acct.getAccountNumber());
        Assert.assertEquals("Object Code should be populated", favoriteAccount.getFinancialObjectCode(), acct.getFinancialObjectCode());
        Assert.assertEquals("Incorrect percentage (comparison against 100% should have been zero)", 0, acct.getAccountLinePercent().compareTo(new BigDecimal(100)));

        acct = userFavoriteAccountService.getPopulatedNewAccount(favoriteAccount, PurchaseOrderAccount.class);
        Assert.assertNotNull("Account should be populated", acct);
        Assert.assertTrue("Account should be PO Account", acct instanceof PurchaseOrderAccount);
        Assert.assertEquals("Account Number should be populated", favoriteAccount.getAccountNumber(), acct.getAccountNumber());
        Assert.assertEquals("Object Code should be populated", favoriteAccount.getFinancialObjectCode(), acct.getFinancialObjectCode());
        Assert.assertEquals("Incorrect percentage (comparison against 100% should have been zero)", 0, acct.getAccountLinePercent().compareTo(new BigDecimal(100)));

        iwntAccount = userFavoriteAccountService.getPopulatedNewAccount(favoriteAccount, IWantAccount.class);
        Assert.assertNotNull("Account should be populated", iwntAccount);
        Assert.assertTrue("Account should be IWNT Account", iwntAccount instanceof IWantAccount);
        Assert.assertEquals("Account Number should be populated", favoriteAccount.getAccountNumber(), iwntAccount.getAccountNumber());
        Assert.assertEquals("Object Code should be populated", favoriteAccount.getFinancialObjectCode(), iwntAccount.getFinancialObjectCode());
        Assert.assertEquals("Amount-or-Percent indicator should be Percent",
                CUPurapConstants.PERCENT, ((IWantAccount) iwntAccount).getUseAmountOrPercent());
        Assert.assertEquals("Incorrect percentage (comparison against 100% should have been zero)",
                0, ((IWantAccount) iwntAccount).getAmountOrPercent().compareTo(new KualiDecimal(100)));

        acct = userFavoriteAccountService.getPopulatedNewAccount(null, PurchaseOrderAccount.class);
        Assert.assertNull("Account should not be populated", acct);
    }

    @Test
    public void testSelectedFavoriteAccount() {
        FavoriteAccount favoriteAccount1 = FavoriteAccountFixture.FAVORITE_ACCOUNT_1.createFavoriteAccount();
        FavoriteAccount favoriteAccount2 = userFavoriteAccountService.getSelectedFavoriteAccount(favoriteAccount1.getAccountLineIdentifier());
        Assert.assertTrue("Favorite Account should exist", favoriteAccount2 != null);
        Assert.assertTrue("should have the same PK", favoriteAccount1.getAccountLineIdentifier().equals(favoriteAccount2.getAccountLineIdentifier()));
    }

    private class TestUserFavoriteAccountServiceImpl extends UserFavoriteAccountServiceImpl {
//        @Override
//        public FavoriteAccount getSelectedFavoriteAccount(Integer accountLineIdentifier) {
//            if (TEST_FAVORITE_ACCOUNT_LINE_ID.equals(accountLineIdentifier)) {
//                return testFavoriteAccount;
//            } else if (TEST_ALT_FAVORITE_ACCOUNT_LINE_ID.equals(accountLineIdentifier)) {
//                return testAltFavoriteAccount;
//            }
//            return null;
//        }

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

    private class MockBusinessObjectServiceImpl extends BusinessObjectServiceImpl {

        @Override
        public <T extends BusinessObject> T findByPrimaryKey(Class<T> clazz, Map<String, ?> primaryKeys) {
            return (T)FavoriteAccountFixture.FAVORITE_ACCOUNT_1.createFavoriteAccount();
        }

        @Override
        public <T extends BusinessObject> Collection<T> findMatching(Class<T> clazz, Map<String, ?> fieldValues) {
            String principalId = (String) fieldValues.get("principalId");
            if (StringUtils.equals("1008950", principalId)) {
                Collection<T> results = new ArrayList<>();
                results.add((T) UserProcurementProfileFixture.USER_PROCUREMENT_PROFILE_8.createUserProcurementProfile());
                return results;
            } else {
                return null;
            }
        }
    }

}
