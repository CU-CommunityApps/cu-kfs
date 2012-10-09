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
import edu.cornell.kfs.coa.service.OrgHierarchyMissingOrgsReportService;


public class OrgHierarchyMissingOrgsStep extends AbstractStep {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(OrgHierarchyMissingOrgsStep.class);
   
    /**
     * @see org.kuali.kfs.sys.batch.AbstractWrappedBatchStep#getCustomBatchExecutor()
     */
   
            public boolean execute(String str, Date date) {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start("OrgHierarchyMissingOrgsStep");
                File baseOrgs = new File("/Users/kwk43/Desktop/baseOrgs.tsv");
                File orgHierarchies = new File("/Users/kwk43/Desktop/OrgReviewsforFPYE.tsv");

                OrgHierarchyMissingOrgsReportService omors = SpringContext.getBean(OrgHierarchyMissingOrgsReportService.class);
                omors.findMissing(baseOrgs, orgHierarchies);
                
                stopWatch.stop();
                LOG.info("OrgHierarchyMissingOrgsStep took " + (stopWatch.getTotalTimeSeconds() / 60.0) + " minutes to complete");
                return true;
            }
    }

  
