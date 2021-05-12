/*
 * Copyright 2005 The Kuali Foundation
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.sys.businessobject.FiscalYearBasedBusinessObject;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;

/**
 * 
 */
public class AccountReversion extends Reversion implements MutableInactivatable, FiscalYearBasedBusinessObject {

    
    private String accountNumber;
    
    private Account account;
    private List<Account> accounts; // This is only used by the "global" document
    private List<AccountReversionDetail> accountReversionDetails;

    /**
     * Default constructor.
     */
    public AccountReversion() {
        accounts = new ArrayList<Account>();
        accountReversionDetails = new ArrayList<AccountReversionDetail>();
    }   

    public List<AccountReversionDetail> getAccountReversionDetails() {
        return accountReversionDetails;
    }

    public void addAccountReversionDetail(AccountReversionDetail ard) {
        accountReversionDetails.add(ard);
    }

    public void setAccountReversionDetails(List<AccountReversionDetail> accountReversionDetails) {
        this.accountReversionDetails = accountReversionDetails;
    }

    public ReversionCategoryInfo getReversionDetail(String categoryCode) {
        for (AccountReversionDetail element : accountReversionDetails) {
            if (element.getAccountReversionCategoryCode().equals(categoryCode)) {
                if (!element.isActive()) {
                    return null; // don't send back inactive details
                } else {
                    return element;
                }
            }
        }
        return null;
    }



    /**
     * Gets the organizationCode attribute.
     * 
     * @return Returns the organizationCode
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * Sets the organizationCode attribute.
     * 
     * @param organizationCode The organizationCode to set.
     */
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }


    

    /**
     * Gets the organization attribute.
     * 
     * @return Returns the organization
     */
    public List<Account> getAccounts() {
        return accounts;
    }

    /**
     * Sets the organization attribute.
     * 
     * @param organization The organization to set.
     * @deprecated
     */
    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

   

    /**
     * @see org.kuali.kfs.kns.bo.BusinessObjectBase#toStringMapper()
     */
    protected LinkedHashMap toStringMapper() {
        
        LinkedHashMap m = super.toStringMapper();
        
        m.put("accountNumber", this.accountNumber);
        return m;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    /**
     * This method (a hack by any other name...) returns a string so that an account reversion can have a link to view its own
     * inquiry page after a look up
     * 
     * @return the String "View Account Reversion"
     */
    public String getAccountReversionViewer() {
        return "View Account Reversion";
    }


    @Override
    public String getSourceAttribute() {
        
        return accountNumber;
    }
}
