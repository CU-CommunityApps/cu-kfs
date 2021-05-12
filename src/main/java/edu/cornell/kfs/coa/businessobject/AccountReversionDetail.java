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

package edu.cornell.kfs.coa.businessobject;

import java.util.LinkedHashMap;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.sys.businessobject.FiscalYearBasedBusinessObject;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;

/**
 * 
 */
public class AccountReversionDetail extends ReversionDetail implements MutableInactivatable, ReversionCategoryInfo, FiscalYearBasedBusinessObject {

    private String accountNumber;
    private String accountReversionCategoryCode;
    private String accountReversionCode;
    private String accountReversionObjectCode;
    
    private Account account;
    AccountReversion accountReversion;

    /**
     * Default constructor.
     */
    public AccountReversionDetail() {

    }

    /**
	 * @return the accountNumber
	 */
	public String getAccountNumber() {
		return accountNumber;
	}

	/**
	 * @param accountNumber the accountNumber to set
	 */
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	/**
	 * @return the accountReversionCategoryCode
	 */
	public String getAccountReversionCategoryCode() {
		return accountReversionCategoryCode;
	}

	/**
	 * @param accountReversionCategoryCode the accountReversionCategoryCode to set
	 */
	public void setAccountReversionCategoryCode(String accountReversionCategoryCode) {
		this.accountReversionCategoryCode = accountReversionCategoryCode;
	}

	/**
	 * @return the accountReversionCode
	 */
	public String getAccountReversionCode() {
		return accountReversionCode;
	}

	/**
	 * @param accountReversionCode the accountReversionCode to set
	 */
	public void setAccountReversionCode(String accountReversionCode) {
		this.accountReversionCode = accountReversionCode;
	}

	/**
	 * @return the accountReversionObjectCode
	 */
	public String getAccountReversionObjectCode() {
		return accountReversionObjectCode;
	}

	/**
	 * @param accountReversionObjectCode the accountReversionObjectCode to set
	 */
	public void setAccountReversionObjectCode(String accountReversionObjectCode) {
		this.accountReversionObjectCode = accountReversionObjectCode;
	}

	/**
	 * @return the account
	 */
	public Account getAccount() {
		return account;
	}

	/**
	 * @param account the account to set
	 */
	public void setAccount(Account account) {
		this.account = account;
	}

	/**
	 * @return the accountReversion
	 */
	public AccountReversion getAccountReversion() {
		return accountReversion;
	}

	/**
	 * @param accountReversion the accountReversion to set
	 */
	public void setAccountReversion(AccountReversion accountReversion) {
		this.accountReversion = accountReversion;
	}

	/**
     * @see org.kuali.kfs.kns.bo.BusinessObjectBase#toStringMapper()
     */
    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();
        return m;
    }

	public String getReversionObjectCode() {
		return getAccountReversionObjectCode();
	}

	public String getReversionCode() {
		return getAccountReversionCode();
	}

}
