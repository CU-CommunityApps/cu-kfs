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
package com.rsmart.kuali.kfs.fp.batch;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import com.rsmart.kuali.kfs.fp.batch.service.DisbursementVoucherDocumentBatchService;

/**
 * Calls the <code>DisbursementVoucherDocumentBatchService</code> to pick up incoming DV files and process
 */
public class LoadDisbursementVouchersStep extends AbstractStep {
	private static final Logger LOG = LogManager.getLogger(LoadDisbursementVouchersStep.class);

    private DisbursementVoucherDocumentBatchService disbursementVoucherDocumentBatchService;

    /**
     * @see org.kuali.kfs.kns.bo.Step#execute(java.lang.String, java.util.Date)
     */
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        LOG.debug("execute() started");
        disbursementVoucherDocumentBatchService.processDisbursementVoucherFiles();

        return true;
    }

    /**
     * Sets the disbursementVoucherDocumentBatchService attribute value.
     * 
     * @param disbursementVoucherDocumentBatchService The disbursementVoucherDocumentBatchService to set.
     */
    public void setDisbursementVoucherDocumentBatchService(DisbursementVoucherDocumentBatchService disbursementVoucherDocumentBatchService) {
        this.disbursementVoucherDocumentBatchService = disbursementVoucherDocumentBatchService;
    }

}
