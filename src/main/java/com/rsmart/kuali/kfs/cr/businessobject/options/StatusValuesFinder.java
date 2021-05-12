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
import java.util.List;

import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;

import com.rsmart.kuali.kfs.cr.CRConstants;

/**
 * Status Values Finder
 * 
 * @author Derek Helbert
 * @version $Revision$
 */
public class StatusValuesFinder extends KeyValuesBase {
    
    /**
     * 
     * @see org.kuali.core.lookup.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List<KeyValue> getKeyValues() {
    	List<KeyValue> keyValuesList = new ArrayList<KeyValue>();
        keyValuesList.add( new ConcreteKeyValue(CRConstants.CANCELLED, "Cancelled") );
        keyValuesList.add( new ConcreteKeyValue(CRConstants.CLEARED, "Cleared") );
        keyValuesList.add( new ConcreteKeyValue(CRConstants.EXCP, "Exception") );
        keyValuesList.add( new ConcreteKeyValue(CRConstants.ISSUED, "Issued") );
        keyValuesList.add( new ConcreteKeyValue(CRConstants.STALE, "Stale") );
        keyValuesList.add( new ConcreteKeyValue(CRConstants.STOP, "Stopped") );
        keyValuesList.add( new ConcreteKeyValue(CRConstants.VOIDED, "Voided") );        
        return keyValuesList;
    }
}
