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

import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.kfs.krad.service.KeyValuesService;

import com.rsmart.kuali.kfs.cr.businessobject.CheckReconSource;

public class CheckReconSrcValuesFinder extends KeyValuesBase {
    private static final long serialVersionUID = 4683657441172386943L;
    private KeyValuesService keyValuesService;

    public List<KeyValue> getKeyValues() {
        Collection<CheckReconSource> sources = keyValuesService.findAll(CheckReconSource.class);
        List<KeyValue> srcKeyLabels = new ArrayList<KeyValue>();
        srcKeyLabels.add(new ConcreteKeyValue("", ""));
        for (CheckReconSource src : sources) {
           srcKeyLabels.add(new ConcreteKeyValue(src.getSourceCode(), src.getSourceName()));
        }

        return srcKeyLabels;
    }

    public void setKeyValuesService(KeyValuesService keyValuesService) {
        this.keyValuesService = keyValuesService;
    }
}
