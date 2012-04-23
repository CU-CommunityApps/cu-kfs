/*
 * Copyright 2012 The Kuali Foundation.
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
package org.kuali.kfs.module.bc.batch;

import java.util.Date;

import org.kuali.kfs.module.bc.BCConstants;
import org.kuali.kfs.module.bc.BCParameterKeyConstants;
import org.kuali.kfs.module.bc.batch.service.GLBudgetLoadService;
import org.kuali.kfs.module.bc.util.BudgetParameterFinder;
import org.kuali.kfs.sys.batch.AbstractStep;

public class TrusteeBudgetConstructionGeneralLedgerLoadBatchStep extends AbstractStep {

    private GLBudgetLoadService glBudgetLoadService;

    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        if(this.getParameterService().getIndicatorParameter(BCConstants.BUDGET_CONSTRUCTION_NAMESPACE, BCParameterKeyConstants.BUDGET_CONSTRUCTION_PARAM_DTL, BCParameterKeyConstants.BC_TRUSTEE_ONLY_BUDGET)) {
            glBudgetLoadService.loadPendingBudgetConstructionGeneralLedger();
            return true;
        } else {
            return false;
        }
        
    }

    public void setGLBudgetLoadService(GLBudgetLoadService glBudgetLoadService) {
        this.glBudgetLoadService = glBudgetLoadService;
    }

}
