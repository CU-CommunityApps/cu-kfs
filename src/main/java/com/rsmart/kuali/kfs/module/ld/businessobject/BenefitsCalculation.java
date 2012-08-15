/*
 * Copyright 2011 The Kuali Foundation.
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
package com.rsmart.kuali.kfs.module.ld.businessobject;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.ObjectCode;

public class BenefitsCalculation extends org.kuali.kfs.module.ld.businessobject.BenefitsCalculation {

    private Account laborAccountOffset;
    private ObjectCode laborObjectCodeOffset;
    
    /**
     * Gets the laborAccountOffset attribute. 
     * @return Returns the laborAccountOffset.
     */
    public Account getLaborAccountOffset() {
        return laborAccountOffset;
    }
    /**
     * Sets the laborAccountOffset attribute value.
     * @param laborAccountOffset The laborAccountOffset to set.
     */
    public void setLaborAccountOffset(Account laborAccountOffset) {
        this.laborAccountOffset = laborAccountOffset;
    }
    /**
     * Gets the laborObjectCodeOffset attribute. 
     * @return Returns the laborObjectCodeOffset.
     */
    public ObjectCode getLaborObjectCodeOffset() {
        return laborObjectCodeOffset;
    }
    /**
     * Sets the laborObjectCodeOffset attribute value.
     * @param laborObjectCodeOffset The laborObjectCodeOffset to set.
     */
    public void setLaborObjectCodeOffset(ObjectCode laborObjectCodeOffset) {
        this.laborObjectCodeOffset = laborObjectCodeOffset;
    }
    
}
