package edu.cornell.kfs.sys.service.impl.fixture;

import edu.cornell.kfs.sys.businessobject.FavoriteAccount;

public enum FavoriteAccountFixture {

    FAVORITE_ACCOUNT_1("Office Account","IT","4163742","6550","","","","",13,8,true,null,true, UserProcurementProfileFixture.USER_PROCUREMENT_PROFILE_8);

    private final String description;
    private final String chartOfAccountsCode;
    private final String accountNumber;
    private final String financialObjectCode;
    private final String subAccountNumber;
    private final String financialSubObjectCode;
    private final String projectCode;
    private final String organizationReferenceId;
    private final Integer accountLineIdentifier;
    private final Integer userProfileId;
    private final Boolean primaryInd;
    private final Integer currentYear;
    private final boolean active;
    private final UserProcurementProfileFixture userProcurementProfileFixture;

    FavoriteAccountFixture(String description, String chartOfAccountsCode, String accountNumber, String financialObjectCode, String subAccountNumber, String financialSubObjectCode, String projectCode, String organizationReferenceId, Integer accountLineIdentifier, Integer userProfileId, Boolean primaryInd, Integer currentYear, boolean active, UserProcurementProfileFixture userProcurementProfileFixture) {
        this.description = description;
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.accountNumber = accountNumber;
        this.financialObjectCode = financialObjectCode;
        this.subAccountNumber = subAccountNumber;
        this.financialSubObjectCode = financialSubObjectCode;
        this.projectCode = projectCode;
        this.organizationReferenceId = organizationReferenceId;
        this.accountLineIdentifier = accountLineIdentifier;
        this.userProfileId = userProfileId;
        this.primaryInd = primaryInd;
        this.currentYear = currentYear;
        this.active = active;
        this.userProcurementProfileFixture = userProcurementProfileFixture;
    }

    public FavoriteAccount createFavoriteAccount() {
        FavoriteAccount favoriteAccount = new FavoriteAccount();
        favoriteAccount.setDescription(this.description);
        favoriteAccount.setChartOfAccountsCode(this.chartOfAccountsCode);
        favoriteAccount.setAccountNumber(this.accountNumber);
        favoriteAccount.setFinancialObjectCode(this.financialObjectCode);
        favoriteAccount.setSubAccountNumber(this.subAccountNumber);
        favoriteAccount.setFinancialSubObjectCode(this.financialSubObjectCode);
        favoriteAccount.setProjectCode(this.projectCode);
        favoriteAccount.setOrganizationReferenceId(this.organizationReferenceId);
        favoriteAccount.setAccountLineIdentifier(this.accountLineIdentifier);
        favoriteAccount.setUserProfileId(this.userProfileId);
        favoriteAccount.setPrimaryInd(this.primaryInd);
        favoriteAccount.setCurrentYear(this.currentYear);
        favoriteAccount.setActive(this.active);
        favoriteAccount.setUserProcurementProfile(this.userProcurementProfileFixture.createUserProcurementProfile());

        return favoriteAccount;
    }

}
