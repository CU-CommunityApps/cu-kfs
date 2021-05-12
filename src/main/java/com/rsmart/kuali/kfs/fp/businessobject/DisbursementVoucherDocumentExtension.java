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

import java.util.LinkedHashMap;

import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;
import org.kuali.kfs.sys.KFSPropertyConstants;


/**
 * Extension attributes for Disbursement Voucher Document
 */
public class DisbursementVoucherDocumentExtension extends PersistableBusinessObjectBase
        implements PersistableBusinessObjectExtension {
    private String documentNumber;
    private KualiInteger batchId;

    private DisbursementVoucherBatch disbursementVoucherBatch;

    /**
     * Default constructor.
     */
    public DisbursementVoucherDocumentExtension() {

    }

    /**
     * Gets the documentNumber attribute.
     * 
     * @return Returns the documentNumber.
     */
    public String getDocumentNumber() {
        return documentNumber;
    }

    /**
     * Sets the documentNumber attribute value.
     * 
     * @param documentNumber The documentNumber to set.
     */
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
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
     * @return Returns the disbursementVoucherBatch.
     */
    public DisbursementVoucherBatch getDisbursementVoucherBatch() {
        return disbursementVoucherBatch;
    }

    /**
     * @param disbursementVoucherBatch The disbursementVoucherBatch to set.
     */
    public void setDisbursementVoucherBatch(DisbursementVoucherBatch disbursementVoucherBatch) {
        this.disbursementVoucherBatch = disbursementVoucherBatch;
    }

    /**
     * @see org.kuali.kfs.kns.bo.PersistableBusinessObjectExtensionBase#toStringMapper()
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();

        m.put(KFSPropertyConstants.DOCUMENT_NUMBER, this.documentNumber);

        return m;
    }

}
