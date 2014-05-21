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
package edu.cornell.kfs.fp.businessobject.options;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;

import edu.cornell.kfs.fp.CuFPConstants;

/**
 * This class returns list of payment method key value pairs.
 * 
 * Customization for UA: addition of "A" type for credit card payments.
 * 
 * @author jonathan
 * @see org.kuali.kfs.fp.businessobject.options.PaymentMethodValuesFinder
 */
public class PaymentMethodsForDVValuesFinder extends PaymentMethodValuesFinder {
    static private Map<String,String> filterCriteria = new HashMap<String, String>();
    static {
        filterCriteria.put(CuFPConstants.ACTIVE, CuFPConstants.YES);
        filterCriteria.put(CuFPConstants.DISPLAY_ON_DV_DOCUMENT, CuFPConstants.YES);
    }    
    protected Map<String,String> getFilterCriteria() {
        return filterCriteria;
    }
    
    public List<KeyValue> getKeyValues() {
        List<KeyValue> labels = super.getKeyValues();
        if (StringUtils.isNotBlank((String)labels.get(0).getKey())) {
        	labels.add(0, new ConcreteKeyValue("", ""));
        }
       return labels;
    }

}
