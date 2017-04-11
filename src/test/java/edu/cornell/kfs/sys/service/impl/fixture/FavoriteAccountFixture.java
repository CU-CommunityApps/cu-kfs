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
import org.kuali.kfs.fp.businessobject.VoucherSourceAccountingLine;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.context.TestUtils;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import java.util.ArrayList;
import java.util.List;

import static org.kuali.kfs.sys.KFSConstants.GL_CREDIT_CODE;
import static org.kuali.kfs.sys.KFSConstants.GL_DEBIT_CODE;

public enum FavoriteAccountFixture {

//    "DESCRIPTION","CHART_CD","ACCT_NBR","FIN_OBJ_CD","SUB_ACCT_NBR","FIN_SUB_OBJ_CD","PROJECT_CD","ORG_REF_ID","ACCT_LN_ID","USER_PROFILE_ID","PRIMARY_IND","NULL","ACTV_IND"
    FAVORITE_ACCOUNT_1("Office Account","IT","4163742","6550","","","","",13,8,true,null,true, UserProcurementProfileFixture.USER_PROCUREMENT_PROFILE_8.createUserProcurementProfile()),
    FAVORITE_ACCOUNT_2("","IT","R603882","6560","","","","",29,17,true,null,true, UserProcurementProfileFixture.USER_PROCUREMENT_PROFILE_17.createUserProcurementProfile()),
    FAVORITE_ACCOUNT_3("FSAD College OPTS","IT","3293005","6550","","","","",60,7,true,null,true, UserProcurementProfileFixture.USER_PROCUREMENT_PROFILE_7.createUserProcurementProfile()),
    FAVORITE_ACCOUNT_4("SUBACCT-CCSF PYROLYSIS-ANGENENT","IT","1238735","6540","","","","",62,38,true,null,true, UserProcurementProfileFixture.USER_PROCUREMENT_PROFILE_38.createUserProcurementProfile()),
    FAVORITE_ACCOUNT_5("Milk Method Improve Yr 26","IT","1438476","6540","","","","",65,40,true,null,true, UserProcurementProfileFixture.USER_PROCUREMENT_PROFILE_40.createUserProcurementProfile()),
    FAVORITE_ACCOUNT_6("Accounting","IT","G254700","6100","","","","",296,88,true,null,true, UserProcurementProfileFixture.USER_PROCUREMENT_PROFILE_88.createUserProcurementProfile()),
    FAVORITE_ACCOUNT_7("Level 4, Primary","IT","R154701","6520","","","","",144,50,true,null,true, UserProcurementProfileFixture.USER_PROCUREMENT_PROFILE_50.createUserProcurementProfile()),
    FAVORITE_ACCOUNT_8("","IT","S703710","6550","","","","",306,95,true,null,true, UserProcurementProfileFixture.USER_PROCUREMENT_PROFILE_95.createUserProcurementProfile()),
    FAVORITE_ACCOUNT_9("Van Galder -Gen Ex","IT","A894101","6550","","","","",158,51,true,null,true, UserProcurementProfileFixture.USER_PROCUREMENT_PROFILE_51.createUserProcurementProfile()),
    FAVORITE_ACCOUNT_10("Urban Semester Prog","IT","3023190","6550","","","","",162,52,true,null,true, UserProcurementProfileFixture.USER_PROCUREMENT_PROFILE_52.createUserProcurementProfile());

    private String description;
    private String chartOfAccountsCode;
    private String accountNumber;
    private String financialObjectCode;
    private String subAccountNumber;
    private String financialSubObjectCode;
    private String projectCode;
    private String organizationReferenceId;
    private Integer accountLineIdentifier;
    private Integer userProfileId; // user profile PK
    private Boolean primaryInd;
    private Integer currentYear;  // not sure about this field yet
    private boolean active;
    private UserProcurementProfile userProcurementProfile;

    FavoriteAccountFixture(String description, String chartOfAccountsCode, String accountNumber, String financialObjectCode, String subAccountNumber, String financialSubObjectCode, String projectCode, String organizationReferenceId, Integer accountLineIdentifier, Integer userProfileId, Boolean primaryInd, Integer currentYear, boolean active, UserProcurementProfile userProcurementProfile) {
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
        this.userProcurementProfile = userProcurementProfile;
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
        favoriteAccount.setUserProcurementProfile(this.userProcurementProfile);

        return favoriteAccount;
    }

}
