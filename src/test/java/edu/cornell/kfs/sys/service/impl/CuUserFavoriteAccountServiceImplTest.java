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
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLineBase;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderAccount;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.BusinessObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
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
        final String principalId = FavoriteAccountFixture.FAVORITE_ACCOUNT_1.createFavoriteAccount().getUserProcurementProfile().getPrincipalId();
        FavoriteAccount favoriteAccount = userFavoriteAccountService.getFavoriteAccount(principalId);
        Assert.assertTrue("Favorite Account should exist", favoriteAccount != null);
        Assert.assertTrue("should be Primary favorite account", favoriteAccount.getPrimaryInd());
        
        favoriteAccount = userFavoriteAccountService.getFavoriteAccount("2");
        Assert.assertTrue("Favorite Account should not exist", favoriteAccount == null);
    }

    @Test
    public void testPopulatedNewRequisitionAccount() {
    	final String accountType = PurapConstants.PurapDocTypeCodes.REQUISITION_DOCUMENT_TYPE;
        final Class<TestableRequisitionAccount> accountClass = TestableRequisitionAccount.class;
        validateAccount(accountType, accountClass);
    }

    @Test
    public void testPopulatedNewPurchaseOrderAccount() {
    	final String accountType = PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_DOCUMENT;
        final Class<PurchaseOrderAccount> accountClass = PurchaseOrderAccount.class;
        validateAccount(accountType, accountClass);
    }

    @Test
    public void testPopulatedNewIWantAccount() {
        final String accountType = CUPurapConstants.IWNT_DOC_TYPE;
        final Class<IWantAccount> accountClass = IWantAccount.class;
        validateAccount(accountType, accountClass);
    }

    private void validateAccount(String accountType, Class accountClass) {
        final FavoriteAccount favoriteAccount = FavoriteAccountFixture.FAVORITE_ACCOUNT_1.createFavoriteAccount();
        final GeneralLedgerPendingEntrySourceDetail acct = userFavoriteAccountService.getPopulatedNewAccount(favoriteAccount, accountClass);
        Assert.assertNotNull("Account should be populated", acct);
        Assert.assertTrue("Account should be " + accountType + " Account", accountClass.isInstance(acct));
        Assert.assertEquals("Account Number should be populated", favoriteAccount.getAccountNumber(), acct.getAccountNumber());
        Assert.assertEquals("Object Code should be populated", favoriteAccount.getFinancialObjectCode(), acct.getFinancialObjectCode());

        if (acct instanceof IWantAccount) {
            final KualiDecimal accountLinePercent = ((IWantAccount) acct).getAmountOrPercent();
            Assert.assertEquals("Incorrect percentage (comparison against 100% should have been zero)", 0, accountLinePercent.compareTo(new KualiDecimal(100)));
            Assert.assertEquals("Amount-or-Percent indicator should be Percent", CUPurapConstants.PERCENT, ((IWantAccount) acct).getUseAmountOrPercent());
        } else {
            final BigDecimal accountLinePercent = ((PurApAccountingLineBase) acct).getAccountLinePercent();
            Assert.assertEquals("Incorrect percentage (comparison against 100% should have been zero)", 0, accountLinePercent.compareTo(new BigDecimal(100)));
        }
    }

    @Test
    public void testPopulatedNewAccountNull() {
        GeneralLedgerPendingEntrySourceDetail acct = userFavoriteAccountService.getPopulatedNewAccount(null, PurchaseOrderAccount.class);
        Assert.assertNull("Account should not be populated", acct);
    }

    @Test
    public void testSelectedFavoriteAccount() {
        FavoriteAccount favoriteAccount1 = FavoriteAccountFixture.FAVORITE_ACCOUNT_1.createFavoriteAccount();
        FavoriteAccount favoriteAccount2 = userFavoriteAccountService.getSelectedFavoriteAccount(favoriteAccount1.getAccountLineIdentifier());
        Assert.assertTrue("Favorite Account should exist", favoriteAccount2 != null);
        Assert.assertTrue("should have the same PK", favoriteAccount1.getAccountLineIdentifier().equals(favoriteAccount2.getAccountLineIdentifier()));
    }

    private class MockBusinessObjectServiceImpl extends BusinessObjectService {

        @Override
        public <T extends BusinessObject> T findByPrimaryKey(Class<T> clazz, Map<String, ?> primaryKeys) {
            Integer accountLineIdentifier = (Integer) primaryKeys.get("accountLineIdentifier");
            if (accountLineIdentifier.equals(13)) {
                return (T) FavoriteAccountFixture.FAVORITE_ACCOUNT_1.createFavoriteAccount();
            } else {
                return null;
            }
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
