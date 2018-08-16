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
package edu.cornell.kfs.gl.batch.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.ObjectCode;

import edu.cornell.kfs.gl.batch.service.ReversionCategoryLogic;

/**
 * The implementation of OrganizationReversionCategoryLogic for cash balances.
 * @see org.kuali.kfs.gl.batch.service.OrganizationReversionCategoryLogic
 */
public class CashReversionCategoryLogic implements ReversionCategoryLogic {
	private static final Logger LOG = LogManager.getLogger(CashReversionCategoryLogic.class);

    static final public String NAME = "Cash";
    static final public String CODE = "CASH";

    /**
     * Determines if the given object code is an object code for cash balances
     * 
     * @param oc the object code to qualify
     * @return true if it is a cash object code, false if otherwise
     * @see org.kuali.kfs.gl.batch.service.OrganizationReversionCategoryLogic#containsObjectCode(org.kuali.kfs.coa.businessobject.ObjectCode)
     */
    public boolean containsObjectCode(ObjectCode oc) {
        LOG.debug("containsObjectCode() started");
        boolean retVal;

        ObjectCode chartCashObject = oc.getChartOfAccounts().getFinancialCashObject();
        retVal = (chartCashObject.getChartOfAccountsCode().equals(oc.getChartOfAccountsCode()) && chartCashObject.getFinancialObjectCode().equals(oc.getFinancialObjectCode()));
    
        retVal |= (oc.getFinancialObjectTypeCode().equals("AS") || oc.getFinancialObjectTypeCode().equals("LI"));
        
        return retVal;
    }

    /**
     * Returns the code for this category, always "CASH"
     * 
     * @return the code for this category
     * @see org.kuali.kfs.gl.batch.service.OrganizationReversionCategoryLogic#getCode()
     */
    public String getCode() {
        return CODE;
    }

    /**
     * Returns the name of this category, always "Cash"
     * 
     * @return the name of this category
     * @see org.kuali.kfs.gl.batch.service.OrganizationReversionCategoryLogic#getName()
     */
    public String getName() {
        return NAME;
    }

    /**
     * Returns if this category represents an expense or not; it never does
     * 
     * @return false, as the cash category always represents non-expense
     * @see org.kuali.kfs.gl.batch.service.OrganizationReversionCategoryLogic#isExpense()
     */
    public boolean isExpense() {
        return false;
    }
}

