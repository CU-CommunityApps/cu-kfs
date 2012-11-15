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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.sql.Timestamp;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;

import org.kuali.kfs.fp.batch.ProcurementCardLoadStep;
import org.kuali.kfs.module.purap.PurapRuleConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemMaintenanceDocument;
import org.kuali.kfs.sys.document.FinancialSystemTransactionalDocument;
import org.kuali.kfs.sys.document.FinancialSystemTransactionalDocumentBase;
import org.kuali.rice.kew.actionitem.ActionItem;
import org.kuali.rice.kew.actionitem.dao.ActionItemDAO;
import org.kuali.rice.kew.actionrequest.ActionRequestValue;
import org.kuali.rice.kew.actionrequest.dao.ActionRequestDAO;
import org.kuali.rice.kew.actionrequest.service.ActionRequestService;
import org.kuali.rice.kew.actiontaken.ActionTakenValue;
import org.kuali.rice.kew.actiontaken.service.ActionTakenService;
import org.kuali.rice.kew.doctype.bo.DocumentType;
import org.kuali.rice.kew.dto.DocumentSearchCriteriaDTO;
import org.kuali.rice.kew.dto.DocumentSearchResultDTO;
import org.kuali.rice.kew.dto.DocumentSearchResultRowDTO;
import org.kuali.rice.kew.dto.KeyValueDTO;
import org.kuali.rice.kew.engine.node.RouteNodeInstance;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kew.util.KEWPropertyConstants;
import org.kuali.rice.kim.bo.entity.KimPrincipal;
import org.kuali.rice.kns.dao.PlatformAwareDao;
import org.kuali.rice.kns.dao.impl.PlatformAwareDaoBaseOjb;
import org.kuali.rice.kns.document.Document;

import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DocumentService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.ErrorMap;
import org.kuali.rice.kns.util.ErrorMessage;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.workflow.service.KualiWorkflowInfo;
import org.springframework.transaction.annotation.Transactional;
import org.springmodules.orm.ojb.PersistenceBrokerTemplate;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
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
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AutoCancelBatchStep.class);

    private AutoCancelBatchDao autoCancelBatchDao;
    
    /**
     * Execute
     * 
     * @param jobName Job Name
     * @param jobRunDate Job Date
     * @see org.kuali.kfs.sys.batch.Step#execute(java.lang.String, java.util.Date)
     */
    @Transactional
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        LOG.info("Started AutoCancelBatchStep @ " + (new Date()).toString());
        boolean results = true;
        try {
            LOG.info("Started AutoCancelBatchStep : Canceling FYIs and Acknowledgements @ " + (new Date()).toString());
        	results = autoCancelBatchDao.cancelFYIsAndAcknowledgements();
            LOG.info("Completed AutoCancelBatchStep : Canceling FYIs and Acknowledgements @ " + (new Date()).toString());
            LOG.info("Started AutoCancelBatchStep : Canceling stale documents @ " + (new Date()).toString());
        	autoCancelBatchDao.cancelDocuments();
            LOG.info("Completed AutoCancelBatchStep : Canceling stale documents @ " + (new Date()).toString());
        } catch (Exception e) {
			LOG.error("Unable to cancel documents. Encountered the following error: ", e);
			return false;
		}

        LOG.info("Completed AutoCancelBatchStep @ " + (new Date()).toString());

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
