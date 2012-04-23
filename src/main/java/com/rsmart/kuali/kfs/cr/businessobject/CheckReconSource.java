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
package com.rsmart.kuali.kfs.cr.businessobject;

import java.io.Serializable;
import java.util.LinkedHashMap;

import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;

/**
 * CheckReconSource
 * 
 * @author Derek Helbert
 */
public class CheckReconSource extends PersistableBusinessObjectBase implements Serializable {

    private Integer id; // CRS_ID
    
    private String sourceCode; // SRC_CD
    
    private String sourceName; // SRC_NAME
    
    
    public Integer getId() {
        return id;
    }


    public void setId(Integer id) {
        this.id = id;
    }


    public String getSourceCode() {
        return sourceCode;
    }


    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }


    public String getSourceName() {
        return sourceName;
    }


    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }


    @Override
    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();

        m.put("sourceCd", getSourceCode());
        m.put("sourceName", getSourceName());
        
        return m;
    }

}
