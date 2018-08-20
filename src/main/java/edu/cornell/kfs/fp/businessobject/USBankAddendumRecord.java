/*
 * Copyright 2016 The Kuali Foundation.
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
package edu.cornell.kfs.fp.businessobject;

import java.io.IOException;
import java.text.ParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class USBankAddendumRecord extends PersistableBusinessObjectBase {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LogManager.getLogger(USBankAddendumRecord.class);
    protected String recordId;

    /**
     * @return the recordId
     */
    public String getRecordId() {
      return recordId;
    }

    /**
     * @param recordType the recordType to set
     */
    public void setRecordId(String recordId) {
      this.recordId = recordId;
    }
    
    /**
     * Parses a supposed record addendum line
     * 
     * @param line The current line
     * @param lineCount The current line number
     * @throws Exception When there is a string parsing error or a missing required field
     */
    public void parse(String line, int lineCount) throws ParseException {
      throw new UnsupportedOperationException("This class has not implemented its #parse method.");
    }
    
}
