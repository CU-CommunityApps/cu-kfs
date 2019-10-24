/*
 * Copyright 2007 The Kuali Foundation
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
package edu.cornell.kfs.coa.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;

/**
* This class returns list containing 22 = Checking or 32 = Savings
 */
@SuppressWarnings("serial")
public class CuCheckingSavingsValuesFinder extends KeyValuesBase {
	
	public static final class BankAccountTypes {
		public static final String PERSONAL_CHECKING = "22PPD";
		public static final String PERSONAL_SAVINGS = "32PPD";
		public static final String CORPORATE_CHECKING = "22CTX";
		public static final String CORPORATE_SAVINGS = "32CTX";
	}

    /**
     * Creates a simple list of static values for either checking or savings
     */
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        keyValues.add(new ConcreteKeyValue(BankAccountTypes.PERSONAL_CHECKING, "Personal Checking (" + BankAccountTypes.PERSONAL_CHECKING +  ")"));
        keyValues.add(new ConcreteKeyValue(BankAccountTypes.PERSONAL_SAVINGS, "Personal Savings (" + BankAccountTypes.PERSONAL_SAVINGS +  ")"));
        keyValues.add(new ConcreteKeyValue(BankAccountTypes.CORPORATE_CHECKING, "Corporate Checking (" + BankAccountTypes.CORPORATE_CHECKING +  ")"));
        keyValues.add(new ConcreteKeyValue(BankAccountTypes.CORPORATE_SAVINGS, "Corporate Savings (" + BankAccountTypes.CORPORATE_SAVINGS +  ")"));
        return keyValues;
    }

}
