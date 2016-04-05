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
package com.rsmart.kuali.kfs.fp.batch.service;

import java.util.Collection;

import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.krad.util.MessageMap;

import com.rsmart.kuali.kfs.fp.businessobject.DisbursementVoucherBatchFeed;
import com.rsmart.kuali.kfs.fp.businessobject.DisbursementVoucherBatchStatus;

/**
 * Service for processing disbursement voucher batch files
 */
public interface DisbursementVoucherDocumentBatchService {

    /**
     * Picks up disbursement voucher batch files in the configured directory, parses each batch file and loads dv data
     */
    public void processDisbursementVoucherFiles();

    /**
     * Creates disbursement voucher documents for the give batch documents, performs validation, routes documents and
     * finally creates audit report
     * 
     * @param batchFeed parsed object containing the feed unit and collection of documents
     * @param batchStatus populated with information for audit report
     * @param incomingFileName name of xml file which contents were parsed from
     * @param errorMap Map to add errors
     */
    public void loadDisbursementVouchers(DisbursementVoucherBatchFeed batchFeed, DisbursementVoucherBatchStatus batchStatus, String incomingFileName, MessageMap errorMap);

    /**
     * Generates a report for the disbursement voucher batch to the file system
     * 
     * @param batchStatus populated with information for audit report
     */
    public void generateAuditReport(DisbursementVoucherBatchStatus batchStatus);

}
