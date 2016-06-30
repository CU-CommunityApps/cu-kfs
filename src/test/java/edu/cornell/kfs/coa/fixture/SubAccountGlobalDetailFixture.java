package edu.cornell.kfs.coa.fixture;

import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.kfs.coa.businessobject.SubAccount;

import edu.cornell.kfs.coa.businessobject.SubAccountGlobalDetail;

public enum SubAccountGlobalDetailFixture {
	SUB_ACCOUNT_GLOBAL_DETAIL_1111111_2222222_98_2(A21SubAccountFixture.A21_SUB_ACCOUNT_1111111_2222222_98_2.getA21SubAccount()),
	SUB_ACCOUNT_GLOBAL_1111111_98_INACTIVE_98_INACTIVE_3333333_100_PERCENT_ACTIVE(A21SubAccountFixture.A21_SUB_ACCOUNT_1111111_98_INACTIVE_98_INACTIVE_3333333_100_PERCENT_ACTIVE.getA21SubAccount());
	
	public A21SubAccount a21SubAccount;
	
	private SubAccountGlobalDetailFixture(A21SubAccount a21SubAccount) {
		this.a21SubAccount = a21SubAccount;
	}
	
	public SubAccountGlobalDetail getSubAccountGlobalDetail(){
		SubAccountGlobalDetail subAccountGlobalDetail = new SubAccountGlobalDetail();
		SubAccount subAccount = new SubAccount();
		subAccount.setA21SubAccount(a21SubAccount);
		subAccountGlobalDetail.setSubAccount(subAccount);
		return subAccountGlobalDetail;
	}
	
	public A21SubAccount getA21SubAccount(){
		return a21SubAccount;
	}


}
