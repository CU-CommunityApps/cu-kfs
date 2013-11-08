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
package org.kuali.kfs.fp.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;

import edu.cornell.kfs.fp.CuFPConstants;

/**
 * This class returns list of currency type value pairs.
 */
public class CurrencyTypeValuesFinder extends KeyValuesBase {

    /*
     * @see org.kuali.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List getKeyValues() {
        List keyValues = new ArrayList();
        keyValues.add(new KeyLabelPair(CuFPConstants.CURRENCY_CODE_U, CuFPConstants.CURRENCY_US_DOLLAR));
        keyValues.add(new KeyLabelPair(CuFPConstants.CURRENCY_CODE_C, CuFPConstants.CURRENCY_US_DOLLAR_TO_FOREIGN));
        keyValues.add(new KeyLabelPair(CuFPConstants.CURRENCY_CODE_F, CuFPConstants.CURRENCY_FOREIGN));

        return keyValues;
    }

}
