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

import java.sql.Timestamp;
import java.util.LinkedHashMap;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

import com.rsmart.kuali.kfs.fp.FPPropertyConstants;

/**
 * Holds information on a disbursement voucher batch run
 */
public class DisbursementVoucherBatch extends PersistableBusinessObjectBase {
    private KualiInteger batchId;
    private Timestamp processTimestamp;
    private String processPrincipalId;

    private Person processUser;

    public DisbursementVoucherBatch() {

    }


    /**
     * Gets the batchId attribute.
     * 
     * @return Returns the batchId.
     */
    public KualiInteger getBatchId() {
        return batchId;
    }


    /**
     * Sets the batchId attribute value.
     * 
     * @param batchId The batchId to set.
     */
    public void setBatchId(KualiInteger batchId) {
        this.batchId = batchId;
    }


    /**
     * Gets the processTimestamp attribute.
     * 
     * @return Returns the processTimestamp.
     */
    public Timestamp getProcessTimestamp() {
        return processTimestamp;
    }


    /**
     * Sets the processTimestamp attribute value.
     * 
     * @param processTimestamp The processTimestamp to set.
     */
    public void setProcessTimestamp(Timestamp processTimestamp) {
        this.processTimestamp = processTimestamp;
    }


    /**
     * Gets the processPrincipalId attribute.
     * 
     * @return Returns the processPrincipalId.
     */
    public String getProcessPrincipalId() {
        return processPrincipalId;
    }


    /**
     * Sets the processPrincipalId attribute value.
     * 
     * @param processPrincipalId The processPrincipalId to set.
     */
    public void setProcessPrincipalId(String processPrincipalId) {
        this.processPrincipalId = processPrincipalId;
    }


    /**
     * Gets the processUser attribute.
     * 
     * @return Returns the processUser.
     */
    public Person getProcessUser() {
        processUser = SpringContext.getBean(PersonService.class).updatePersonIfNecessary(processPrincipalId, processUser);

        return processUser;
    }


    /**
     * Sets the processUser attribute value.
     * 
     * @param processUser The processUser to set.
     */
    public void setProcessUser(Person processUser) {
        if (processUser != null) {
            processPrincipalId = processUser.getPrincipalId();
        }

        this.processUser = processUser;
    }


    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();
 
        m.put(FPPropertyConstants.BATCH_ID, this.batchId);

        return m;
    }
}
