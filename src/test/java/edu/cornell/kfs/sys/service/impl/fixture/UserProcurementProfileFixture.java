package edu.cornell.kfs.sys.service.impl.fixture;

import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.businessobject.UserProcurementProfile;

import java.util.ArrayList;
import java.util.List;

public enum UserProcurementProfileFixture {

    USER_PROCUREMENT_PROFILE_8(8, "1008950", true);

    private final Integer userProfileId;
    private final String principalId;
    private final boolean active;

    UserProcurementProfileFixture(Integer userProfileId, String principalId, boolean active) {
        this.userProfileId = userProfileId;
        this.principalId = principalId;
        this.active = active;
    }

    public UserProcurementProfile createUserProcurementProfile() {
        UserProcurementProfile userProcurementProfile = new UserProcurementProfile();
        userProcurementProfile.setUserProfileId(this.userProfileId);
        userProcurementProfile.setPrincipalId(this.principalId);

        List<FavoriteAccount> favoriteAccounts = new ArrayList<>();
        FavoriteAccount favoriteAccount = new FavoriteAccount();
        favoriteAccount.setPrimaryInd(true);
        favoriteAccounts.add(favoriteAccount);

        userProcurementProfile.setFavoriteAccounts(favoriteAccounts);
        userProcurementProfile.setActive(this.active);

        return userProcurementProfile;
    }

}
