package edu.cornell.kfs.sys.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.db18;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderAccount;
import org.kuali.kfs.module.purap.businessobject.RequisitionAccount;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.service.BusinessObjectService;

import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.service.UserFavoriteAccountService;

@ConfigureContext(session = db18)
public class CuUserFavoriteAccountServiceImplTest extends KualiTestBase {
    private UserFavoriteAccountService userFavoriteAccountService;
    @Override
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
        userFavoriteAccountService = SpringContext.getBean(UserFavoriteAccountService.class);
    }

    /*
     * test get user primary favorite account
     * 
     */
    public void testPrimaryFavoriteAccount() {
        Map<String, String> fieldMap = new HashMap<String, String>();
        fieldMap.put("primaryInd", "Y");
        List<FavoriteAccount> favoriteAccounts = (List<FavoriteAccount>)SpringContext.getBean(BusinessObjectService.class).findMatching(FavoriteAccount.class, fieldMap);
        FavoriteAccount favoriteAccount = userFavoriteAccountService.getFavoriteAccount(favoriteAccounts.get(0).getUserProcurementProfile().getPrincipalId());
        assertTrue("Favorite Account should exist", favoriteAccount != null);
        assertTrue("should be Primary favorite account", favoriteAccount.getPrimaryInd());
        
        favoriteAccount = userFavoriteAccountService.getFavoriteAccount("2");
        assertTrue("Favorite Account should not exist", favoriteAccount == null);

    }
    
    /*
     * test populate user favorite account to accounting line
     */
    public void testPopulatedNewAccount() {
        Map<String, String> fieldMap = new HashMap<String, String>();
        fieldMap.put("primaryInd", "Y");
        FavoriteAccount favoriteAccount = ((List<FavoriteAccount>)SpringContext.getBean(BusinessObjectService.class).findMatching(FavoriteAccount.class, fieldMap)).get(0);
        PurApAccountingLine acct = userFavoriteAccountService.getPopulatedNewAccount(favoriteAccount, true);
        assertTrue("Account should be populated", acct != null);
        assertTrue("Account should be REQS Acount", acct instanceof RequisitionAccount);
        assertTrue("Account Number should be populated", StringUtils.equals(acct.getAccountNumber(), favoriteAccount.getAccountNumber()));
        assertTrue("Object Code should be populated", StringUtils.equals(acct.getFinancialObjectCode(), favoriteAccount.getFinancialObjectCode()));
        assertTrue("Percent should be 100", acct.getAccountLinePercent().compareTo(new BigDecimal(100)) == 0);
        
        acct = userFavoriteAccountService.getPopulatedNewAccount(favoriteAccount, false);
        assertTrue("Account should be populated", acct != null);
        assertTrue("Account should be PO Acount", acct instanceof PurchaseOrderAccount);
        assertTrue("Account Number should be populated", StringUtils.equals(acct.getAccountNumber(), favoriteAccount.getAccountNumber()));
        assertTrue("Object Code should be populated", StringUtils.equals(acct.getFinancialObjectCode(), favoriteAccount.getFinancialObjectCode()));
        assertTrue("Percent should be 100", acct.getAccountLinePercent().compareTo(new BigDecimal(100)) == 0);
 
        acct = userFavoriteAccountService.getPopulatedNewAccount(null, false);
        assertTrue("Account should not be populated", acct == null);
       

    }
    
    /*
     * test retrieve favorite account based on the selected favorite account PK
     */
    public void testSelectedFavoriteAccount() {
        FavoriteAccount favoriteAccount1 = ((List<FavoriteAccount>)SpringContext.getBean(BusinessObjectService.class).findAll(FavoriteAccount.class)).get(0);
        
        FavoriteAccount favoriteAccount2 = userFavoriteAccountService.getSelectedFavoriteAccount(favoriteAccount1.getAccountLineIdentifier());
        assertTrue("Favorite Account should exist", favoriteAccount2 != null);
        assertTrue("should have the same PK", favoriteAccount1.getAccountLineIdentifier().equals(favoriteAccount2.getAccountLineIdentifier()));

    }
    

}
