/*
 * Copyright 2006 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.sys.service.impl.fixture;

import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.businessobject.UserProcurementProfile;
import edu.emory.mathcs.backport.java.util.Arrays;

import java.util.ArrayList;
import java.util.List;

public enum UserProcurementProfileFixture {

    USER_PROCUREMENT_PROFILE_7(7, "1025490", true),
    USER_PROCUREMENT_PROFILE_8(8, "1008950", true),
    USER_PROCUREMENT_PROFILE_17(17, "1012806", true),
    USER_PROCUREMENT_PROFILE_38(38, "1518406", true),
    USER_PROCUREMENT_PROFILE_40(40, "37093", true),
    USER_PROCUREMENT_PROFILE_50(50, "1009178", true),
    USER_PROCUREMENT_PROFILE_51(51, "2550773", true),
    USER_PROCUREMENT_PROFILE_52(52, "35616", true),
    USER_PROCUREMENT_PROFILE_88(88, "1012585", true),
    USER_PROCUREMENT_PROFILE_95(95, "1012538", true);

    private Integer userProfileId;
    private String principalId;
//    private List<FavoriteAccount> favoriteAccounts;
//    private String personName;
//    private boolean personSelected;
    private boolean active;

    UserProcurementProfileFixture(Integer userProfileId, String principalId, boolean active) {
//        UserProcurementProfileFixture(Integer userProfileId, String principalId, List<FavoriteAccount> favoriteAccounts, String personName, boolean personSelected, boolean active) {
        this.userProfileId = userProfileId;
        this.principalId = principalId;
//        this.favoriteAccounts = favoriteAccounts;
//        this.personName = personName;
//        this.personSelected = personSelected;
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
//        userProcurementProfile.setPersonName(this.personName);
//        userProcurementProfile.setPersonSelected(this.personSelected);
        userProcurementProfile.setActive(this.active);

        return userProcurementProfile;
    }

}
