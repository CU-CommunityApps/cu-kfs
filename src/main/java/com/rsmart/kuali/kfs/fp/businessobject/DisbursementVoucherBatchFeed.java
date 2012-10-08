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

import java.util.ArrayList;
import java.util.Collection;

import com.rsmart.kuali.kfs.fp.document.BatchDisbursementVoucherDocument;

public class DisbursementVoucherBatchFeed {
    private String unitCode;
    private Collection<BatchDisbursementVoucherDocument> batchDisbursementVoucherDocuments;

    public DisbursementVoucherBatchFeed() {
        super();
        batchDisbursementVoucherDocuments = new ArrayList<BatchDisbursementVoucherDocument>();
    }

    /**
     * Gets the unitCode attribute.
     * 
     * @return Returns the unitCode.
     */
    public String getUnitCode() {
        return unitCode;
    }

    /**
     * Sets the unitCode attribute value.
     * 
     * @param unitCode The unitCode to set.
     */
    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    /**
     * Gets the batchDisbursementVoucherDocuments attribute.
     * 
     * @return Returns the batchDisbursementVoucherDocuments.
     */
    public Collection<BatchDisbursementVoucherDocument> getBatchDisbursementVoucherDocuments() {
        return batchDisbursementVoucherDocuments;
    }

    /**
     * Sets the batchDisbursementVoucherDocuments attribute value.
     * 
     * @param batchDisbursementVoucherDocuments The batchDisbursementVoucherDocuments to set.
     */
    public void setBatchDisbursementVoucherDocuments(Collection<BatchDisbursementVoucherDocument> batchDisbursementVoucherDocuments) {
        this.batchDisbursementVoucherDocuments = batchDisbursementVoucherDocuments;
    }

    /**
     * Adds document to batch collection
     * 
     * @param batchDisbursementVoucherDocument document to add
     */
    public void addDisbursementVoucher(BatchDisbursementVoucherDocument batchDisbursementVoucherDocument) {
        this.batchDisbursementVoucherDocuments.add(batchDisbursementVoucherDocument);
    }

}
