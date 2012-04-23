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

import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;
import org.kuali.rice.core.util.KeyLabelPair;

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
    public List getKeyValues() {
        List keyValuesList = new ArrayList();
        keyValuesList.add( new KeyLabelPair(CRConstants.CANCELLED, "Cancelled") );
        keyValuesList.add( new KeyLabelPair(CRConstants.CLEARED, "Cleared") );
        keyValuesList.add( new KeyLabelPair(CRConstants.EXCP, "Exception") );
        keyValuesList.add( new KeyLabelPair(CRConstants.ISSUED, "Issued") );
        keyValuesList.add( new KeyLabelPair(CRConstants.STALE, "Stale") );
        keyValuesList.add( new KeyLabelPair(CRConstants.STOP, "Stopped") );
        keyValuesList.add( new KeyLabelPair(CRConstants.VOIDED, "Voided") );        
        return keyValuesList;
    }
}
