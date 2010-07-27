/*
 * Copyright 2010 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.coa.businessobject;

import org.kuali.rice.kns.bo.PersistableBusinessObjectExtensionBase;

public class AccountExtendedAttribute extends PersistableBusinessObjectExtensionBase {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5894118108397111352L;
	
	private String chartOfAccountsCode;
    private String accountNumber;
    private String programCode;
    private String appropriationAccountNumber;
    private String subFundGroupCode;

	private SubFundProgram subFundProgram;
    private AppropriationAccount appropriationAccount;
    
    public AccountExtendedAttribute() {
        
    }
    
    /**
     * Gets the chartOfAccountsCode attribute. 
     * @return Returns the chartOfAccountsCode.
     */
    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }
    /**
     * Sets the chartOfAccountsCode attribute value.
     * @param chartOfAccountsCode The chartOfAccountsCode to set.
     */
    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }
    /**
     * Gets the accountNumber attribute. 
     * @return Returns the accountNumber.
     */
    public String getAccountNumber() {
        return accountNumber;
    }
    /**
     * Sets the accountNumber attribute value.
     * @param accountNumber The accountNumber to set.
     */
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }


	/**
	 * @return the subFundProgram
	 */
	public SubFundProgram getSubFundProgram() {
		return subFundProgram;
	}


	/**
	 * @param subFundProgram the subFundProgram to set
	 */
	public void setSubFundProgram(SubFundProgram subFundProgram) {
		this.subFundProgram = subFundProgram;
	}


	/**
	 * @return the appropriationAccount
	 */
	public AppropriationAccount getAppropriationAccount() {
		return appropriationAccount;
	}


	/**
	 * @param appropriationAccount the appropriationAccount to set
	 */
	public void setAppropriationAccount(AppropriationAccount appropriationAccount) {
		this.appropriationAccount = appropriationAccount;
	}

	/**
	 * @return the programCode
	 */
	public String getProgramCode() {
		return programCode;
	}


	/**
	 * @param programCode the programCode to set
	 */
	public void setProgramCode(String programCode) {
		this.programCode = programCode;
	}


	/**
	 * @return the appropriationAccountNumber
	 */
	public String getAppropriationAccountNumber() {
		return appropriationAccountNumber;
	}


	/**
	 * @param appropriationAccountNumber the appropriationAccountNumber to set
	 */
	public void setAppropriationAccountNumber(String appropriationAccountNumber) {
		this.appropriationAccountNumber = appropriationAccountNumber;
	}
	/**
	 * @return the subFundGroupCode
	 */
	public String getSubFundGroupCode() {
	
		return subFundGroupCode;
	}
	/**
	 * @param subFundGroupCode the subFundGroupCode to set
	 */
	public void setSubFundGroupCode(String subFundGroupCode) {
		this.subFundGroupCode = subFundGroupCode;
	}


}
