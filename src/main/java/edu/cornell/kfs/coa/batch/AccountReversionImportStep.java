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
import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.context.SpringContext;
import org.springframework.util.StopWatch;

import edu.cornell.kfs.coa.service.AccountReversionImportService;

/**
 * A step that runs the reversion and carry forward process. The end of year version of the process is supposed to be run before the
 * end of a fiscal year for reporting purposes; therefore, it uses current year accounts instead of prior year accounts.
 */
public class AccountReversionImportStep extends AbstractStep {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AccountReversionImportStep.class);
   
    /**
     * @see org.kuali.kfs.sys.batch.AbstractWrappedBatchStep#getCustomBatchExecutor()
     */
   
            public boolean execute(String str, Date date) {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start("AccountReversionImportStep");
                File f = new File("/infra/platform/app1/cynergy_home/work/staging/sys/AccountReversion.csv");
                AccountReversionImportService aris = SpringContext.getBean(AccountReversionImportService.class);
                aris.importAccountReversions(f);
                
                stopWatch.stop();
                LOG.info("AccountReversionImportStep took " + (stopWatch.getTotalTimeSeconds() / 60.0) + " minutes to complete");
                return true;
            }
    }

  
