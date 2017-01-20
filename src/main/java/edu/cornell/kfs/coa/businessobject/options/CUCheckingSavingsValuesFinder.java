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
 * This class returns list containg 22 = Checking or 32 = Savings
 */
@SuppressWarnings("serial")
public class CUCheckingSavingsValuesFinder extends KeyValuesBase {
	
	public static final class BANK_ACCOUNT_TYPES {
		public static final String PERSONAL_CHECKING = "22PPD";
		public static final String PERSONAL_SAVINGS = "32PPD";
		public static final String CORPORATE_CHECKING = "22CTX";
		public static final String CORPORATE_SAVINGS = "32CTX";
	}

    /**
     * Creates a simple list of static values for either checking or savings
     * 
     * @see org.kuali.kfs.kns.lookup.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        keyValues.add(new ConcreteKeyValue(BANK_ACCOUNT_TYPES.PERSONAL_CHECKING, "Personal Checking (" + BANK_ACCOUNT_TYPES.PERSONAL_CHECKING +  ")"));
        keyValues.add(new ConcreteKeyValue(BANK_ACCOUNT_TYPES.PERSONAL_SAVINGS, "Personal Savings (" + BANK_ACCOUNT_TYPES.PERSONAL_SAVINGS +  ")"));
        keyValues.add(new ConcreteKeyValue(BANK_ACCOUNT_TYPES.CORPORATE_CHECKING, "Corporate Checking (" + BANK_ACCOUNT_TYPES.CORPORATE_CHECKING +  ")"));
        keyValues.add(new ConcreteKeyValue(BANK_ACCOUNT_TYPES.CORPORATE_SAVINGS, "Corporate Savings (" + BANK_ACCOUNT_TYPES.CORPORATE_SAVINGS +  ")"));
        return keyValues;
    }

}
