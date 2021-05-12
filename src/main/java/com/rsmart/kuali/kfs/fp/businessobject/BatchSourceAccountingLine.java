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
package com.rsmart.kuali.kfs.fp.businessobject;

import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.core.api.util.type.KualiDecimal;


/**
 * Provides String setter methods for population from XML (batch)
 */
public class BatchSourceAccountingLine extends SourceAccountingLine {

    /**
     * Takes a <code>String</code> and attempt to format as <code>KualiDecimal</code> for setting the amount field
     * 
     * @param amount as string
     */
    public void setAmount(String amount) {
        super.setAmount(new KualiDecimal(amount));
    }

}
