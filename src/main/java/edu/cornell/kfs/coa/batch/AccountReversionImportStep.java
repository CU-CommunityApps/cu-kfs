/*
 * Copyright 2006 The Kuali Foundation
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
package edu.cornell.kfs.coa.batch;

import java.io.File;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.context.SpringContext;
import org.springframework.util.StopWatch;

import edu.cornell.kfs.coa.service.AccountReversionImportService;
import edu.cornell.kfs.sys.batch.CuAbstractStep;

/**
 * A step that runs the reversion import process, which can be used to update the defined account reversions for a given
 * year.
 */
public class AccountReversionImportStep extends CuAbstractStep {

	private static final Logger LOG = LogManager.getLogger(AccountReversionImportStep.class);

    private String batchFileDirectoryName;

    /**
     * @see org.kuali.kfs.sys.batch.AbstractWrappedBatchStep#getCustomBatchExecutor()
     */
    public boolean execute(String str, LocalDateTime date) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("AccountReversionImportStep");
        File f = new File(this.batchFileDirectoryName + System.getProperty("file.separator") + "AccountReversion.csv");
        AccountReversionImportService aris = SpringContext.getBean(AccountReversionImportService.class);
        aris.importAccountReversions(f);
        
        
        addTimeStampToFileName(f, "AccountReversion.csv", this.batchFileDirectoryName );

        stopWatch.stop();
        LOG.info("AccountReversionImportStep took " + (stopWatch.getTotalTimeSeconds() / 60.0) + " minutes to complete");
        return true;
    }

    /**
     * Gets the batchFileDirectoryName.
     * 
     * @return batchFileDirectoryName
     */
    public String getBatchFileDirectoryName() {
        return batchFileDirectoryName;
    }

    /**
     * Sets the batchFileDirectoryName.
     * 
     * @param batchFileDirectoryName
     */
    public void setBatchFileDirectoryName(String batchFileDirectoryName) {
        this.batchFileDirectoryName = batchFileDirectoryName;
    }
}
