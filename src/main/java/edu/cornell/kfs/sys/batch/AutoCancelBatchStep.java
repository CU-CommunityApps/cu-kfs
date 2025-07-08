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
package edu.cornell.kfs.sys.batch;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.sys.dataaccess.AutoCancelBatchDao;

/**
 * Auto Cancel Batch Step
 * Super User Cancel Saved documents that are older than NNN days based on system parameter.
 * Cancel only specific doc types identified by another system parameter. Route log annotation
 * will indicate cancelled by AutoCancelBatchStep.
 * 
 * @author CSU - John Walker
 * @author Cornell - Dennis Friends
 */
public class AutoCancelBatchStep extends AbstractStep {
	private static final Logger LOG = LogManager.getLogger(AutoCancelBatchStep.class);

    private AutoCancelBatchDao autoCancelBatchDao;
    
    /**
     * Execute
     * 
     * @param jobName Job Name
     * @param jobRunDate Job Date
     * @see org.kuali.kfs.kns.bo.Step#execute(java.lang.String, java.util.Date)
     */
    @Transactional
    public boolean execute(String jobName, LocalDateTime jobRunDate) throws InterruptedException {
        LOG.info("Started AutoCancelBatchStep @ " + (LocalDateTime.now().toString()));

        try {
            LOG.info("Started AutoCancelBatchStep : Canceling FYIs and Acknowledgements @ " + (LocalDateTime.now().toString()));
        	autoCancelBatchDao.cancelFYIsAndAcknowledgements();
            LOG.info("Completed AutoCancelBatchStep : Canceling FYIs and Acknowledgements @ " + (LocalDateTime.now().toString()));
            LOG.info("Started AutoCancelBatchStep : Canceling stale documents @ " + (LocalDateTime.now().toString()));
        	autoCancelBatchDao.cancelDocuments();
            LOG.info("Completed AutoCancelBatchStep : Canceling stale documents @ " + (LocalDateTime.now().toString()));
        } catch (Exception e) {
			LOG.error("Unable to cancel documents. Encountered the following error: ", e);
			return false;
		}

        LOG.info("Completed AutoCancelBatchStep @ " + (LocalDateTime.now().toString()));

        return true;
    }

	/**
	 * @return the autoCancelBatchDao
	 */
	public AutoCancelBatchDao getAutoCancelBatchDao() {
		return autoCancelBatchDao;
	}

	/**
	 * @param autoCancelBatchDao the autoCancelBatchDao to set
	 */
	public void setAutoCancelBatchDao(AutoCancelBatchDao autoCancelBatchDao) {
		this.autoCancelBatchDao = autoCancelBatchDao;
	}

}
