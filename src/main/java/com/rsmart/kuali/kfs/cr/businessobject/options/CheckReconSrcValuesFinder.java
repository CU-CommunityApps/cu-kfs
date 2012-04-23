/*
 * Copyright 2009 The Kuali Foundation.
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
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;
import org.kuali.rice.kns.service.KeyValuesService;
import org.kuali.rice.core.util.KeyLabelPair;

import com.rsmart.kuali.kfs.cr.businessobject.CheckReconSource;

public class CheckReconSrcValuesFinder extends KeyValuesBase {

    /**
     * Get Key Values
     * 
     * @return List of KeyLabelPair
     * 
     * @see org.kuali.core.lookup.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List<KeyLabelPair> getKeyValues() {
        Collection<CheckReconSource> sources = SpringContext.getBean(KeyValuesService.class).findAll(CheckReconSource.class);
        List<KeyLabelPair> srcKeyLabels = new ArrayList<KeyLabelPair>();
        srcKeyLabels.add(new KeyLabelPair("", ""));
        for (CheckReconSource src : sources) {
           srcKeyLabels.add( new KeyLabelPair(src.getSourceCode(), src.getSourceName()) );
        }

        return srcKeyLabels;
    }
}
