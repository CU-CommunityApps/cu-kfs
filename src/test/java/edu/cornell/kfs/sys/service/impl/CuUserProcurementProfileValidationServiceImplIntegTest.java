package edu.cornell.kfs.sys.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.mgw3;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.fixture.UserNameFixture;

import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.businessobject.UserProcurementProfile;
import edu.cornell.kfs.sys.service.UserProcurementProfileValidationService;

@ConfigureContext(session = mgw3)
public class CuUserProcurementProfileValidationServiceImplIntegTest extends KualiIntegTestBase {
    // TODO : validateBo is not referenced; so it can be removed from service
    // validateAccount : only referenced by validateAccounts in this service, so it should not be a published method. it 
    // should be removed to service amd make it private

    private UserProcurementProfileValidationService userProcurementProfileValidationService;
    @Override
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
        userProcurementProfileValidationService = SpringContext.getBean(UserProcurementProfileValidationService.class);
    }

    @Override
    protected void tearDown() throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
    }

    public void testEmptyFavoriteAccounts() {
        assertTrue("Validated empty favorite accounts",userProcurementProfileValidationService.validateAccounts(new ArrayList<FavoriteAccount>()));
    }

    /*
     * test failed scenario for 'validateAccounts'
     */
    public void testFavoriteAccounts_Invalid() {
        List<FavoriteAccount> favoriteAccounts = new ArrayList<FavoriteAccount>();
        favoriteAccounts.add(createFavoriteAccount("IT", "G264700", "6540", true));
        favoriteAccounts.add(createFavoriteAccount("IT", "G264700", "6540", false));
        assertFalse("Validate duplicate accounts",userProcurementProfileValidationService.validateAccounts(favoriteAccounts));
        favoriteAccounts.remove(1);
        favoriteAccounts.add(createFavoriteAccount("IT", "abc1234", "6540", false));
        assertFalse("Validate invalid account",userProcurementProfileValidationService.validateAccounts(favoriteAccounts));
        favoriteAccounts.remove(1);
        favoriteAccounts.add(createFavoriteAccount("IT", "G254700", "6540", true));
        assertFalse("Validate 2 primary favorite accounts",userProcurementProfileValidationService.validateAccounts(favoriteAccounts));
        favoriteAccounts.remove(1);
        favoriteAccounts.add(createFavoriteAccount("CS", "G254700", "6540", false));
        assertFalse("Validate wrong campus account",userProcurementProfileValidationService.validateAccounts(favoriteAccounts));
        favoriteAccounts.remove(1);
        favoriteAccounts.add(createFavoriteAccount("IT", "G254700", "abcd", false));
        assertFalse("Validate invalid object code",userProcurementProfileValidationService.validateAccounts(favoriteAccounts));
        favoriteAccounts.remove(1);
        favoriteAccounts.add(createFavoriteAccount("IT", "G254700", "6540", false));
        favoriteAccounts.get(1).setSubAccountNumber("abc");
        assertFalse("Validate invalid subaccount#",userProcurementProfileValidationService.validateAccounts(favoriteAccounts));
        favoriteAccounts.remove(1);
        favoriteAccounts.add(createFavoriteAccount("IT", "G254700", "6540", false));
        favoriteAccounts.get(1).setProjectCode("abc");
        assertFalse("Validate invalid project code",userProcurementProfileValidationService.validateAccounts(favoriteAccounts));
    }

    /*
     * test validateAccounts 
     */
    public void testFavoriteAccounts_Valid() {
        List<FavoriteAccount> favoriteAccounts = new ArrayList<FavoriteAccount>();
        favoriteAccounts.add(createFavoriteAccount("IT", "G264750", "6540", true));
        favoriteAccounts.add(createFavoriteAccount("IT", "G254700", "6540", false));
        assertTrue("Validated 2 favorite accounts",userProcurementProfileValidationService.validateAccounts(favoriteAccounts));
        favoriteAccounts.remove(0);
        assertTrue("Validated 1 favorite accounts",userProcurementProfileValidationService.validateAccounts(favoriteAccounts));
    }
    
    public void testHasMaintainFavoriteAccountPermission() {
        assertTrue("Can Maintain User procurement profile",userProcurementProfileValidationService.canMaintainUserProcurementProfile());
    }

    public void testNoMaintainFavoriteAccountPermission() {
        try {
            changeCurrentUser(UserNameFixture.kfs);
        } catch (Exception e) {
            
        }
        assertFalse("Can Not Maintain User procurement profile",userProcurementProfileValidationService.canMaintainUserProcurementProfile());
    }

    public void testUserProfileExtist() {
        List<UserProcurementProfile> userProcurementProfile = (List<UserProcurementProfile>)SpringContext.getBean(BusinessObjectService.class).findAll(UserProcurementProfile.class);
        if (CollectionUtils.isNotEmpty(userProcurementProfile)) {
            assertTrue("User procurement profile Exist",userProcurementProfileValidationService.validateUserProfileExist(userProcurementProfile.get(0).getPrincipalId()));
        } else {
            assertTrue("No user profile to test", true);
        }
    }

    public void testUserProfileNotExtist() {
        // "2" is KFS's principalId
        assertFalse("User procurement profile Not Exist",userProcurementProfileValidationService.validateUserProfileExist("2"));
    }

    private FavoriteAccount createFavoriteAccount(String chartOfAccountsCode, String accountNumber, String financialObjectCode, boolean primaryInd) {
        FavoriteAccount favoriteAccount = new FavoriteAccount();
        favoriteAccount.setAccountNumber(accountNumber);
        favoriteAccount.setChartOfAccountsCode(chartOfAccountsCode);
        favoriteAccount.setFinancialObjectCode(financialObjectCode);
        favoriteAccount.setPrimaryInd(primaryInd);
        
        return favoriteAccount;
    }
}
