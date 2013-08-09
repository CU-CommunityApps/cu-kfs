/*
 * Copyright 2008 The Kuali Foundation.
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
package com.rsmart.kuali.kfs.cr.businessobject.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.rice.krad.service.KeyValuesService;
import org.kuali.rice.core.api.util.ConcreteKeyValue;

/**
 * Bank Account Number Values Finder
 * 
 * @author Derek Helbert
 * @version $Revision$
 */
public class BankAccountNumberValuesFinder extends KeyValuesBase {
    
    /**
     * Get Key Values
     * 
     * @return List of KeyLabelPair
     * 
     * @see org.kuali.core.lookup.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List<KeyValue> getKeyValues() {
        Collection<Bank> banks = SpringContext.getBean(KeyValuesService.class).findAll(Bank.class);
        List<KeyValue> bankKeyLabels = new ArrayList<KeyValue>();
        bankKeyLabels.add(new ConcreteKeyValue("", ""));
        for (Bank bank : banks) {
            if(bank.isActive()) {
                bankKeyLabels.add( new ConcreteKeyValue(bank.getBankAccountNumber(), bank.getBankName()) );
            }
        }

        return bankKeyLabels;
    }

}
