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
package edu.cornell.kfs.gl.batch.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;


import org.kuali.kfs.coa.businessobject.OrganizationReversion;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.ReportWriterService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.coa.businessobject.Reversion;
import edu.cornell.kfs.gl.CuGeneralLedgerConstants;
import edu.cornell.kfs.gl.batch.service.ReversionProcess;
import edu.cornell.kfs.gl.batch.service.ReversionProcessService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

/**
 * The base implementation of OrganizationReversionProcessService
 */
@Transactional
public class AccountReversionProcessServiceImpl implements ReversionProcessService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AccountReversionProcessServiceImpl.class);
    
    private static final String ACCOUNT_REVERSION_PRIOR_YEAR_ACCOUNT_PROCESS_BEAN_NAME = "glAccountReversionPriorYearAccountProcess";
    private static final String ACCOUNT_REVERSION_CURRENT_YEAR_ACCOUNT_PROCESS_BEAN_NAME = "glAccountReversionCurrentYearAccountProcess";

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private ReportWriterService accountReversionReportWriterService;
    private ParameterService parameterService;
    private ConfigurationService configurationService;

    /**
     * Gets the organizationReversionReportWriterService attribute. 
     * @return Returns the organizationReversionReportWriterService.
     */
    public ReportWriterService getAccountReversionReportWriterService() {
        return accountReversionReportWriterService;
    }

    /**
     * Sets the organizationReversionReportWriterService attribute value.
     * @param organizationReversionReportWriterService The organizationReversionReportWriterService to set.
     */
    public void setAccountReversionReportWriterService(ReportWriterService accountReversionReportWriterService) {
        this.accountReversionReportWriterService = accountReversionReportWriterService;
    }

    /**
     * Runs the Organization Reversion Year End Process for the end of a fiscal year (ie, a process that
     * runs before the fiscal year end, and thus uses current account, etc.)
     * 
     * @param outputGroup the origin entry group that this process should save entries to
     * @param jobParameters the parameters used in the process
     * @param organizationReversionCounts a Map of named statistics generated by running the process
     * @see org.kuali.kfs.gl.batch.service.ReversionProcessService#organizationReversionProcessEndOfYear(org.kuali.kfs.gl.businessobject.OriginEntryGroup, java.util.Map, java.util.Map)
     */
    public void reversionPriorYearAccountProcess(Map jobParameters, Map<String, Integer> reversionCounts) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("accountReversionProcessEndOfYear() started");
        }
        ReversionProcess orp = SpringContext.getBean(ReversionProcess.class,AccountReversionProcessServiceImpl.ACCOUNT_REVERSION_PRIOR_YEAR_ACCOUNT_PROCESS_BEAN_NAME);

        //orp.reversionProcess(jobParameters, reversionCounts, AccountReversion.class);
        orp.reversionProcess(jobParameters, reversionCounts);
        
        writeReports(orp, jobParameters, reversionCounts);
    }

    /**
     * Organization Reversion Year End Process for the beginning of a fiscal year (ie, the process as it runs
     * after the fiscal year end, thus using prior year account, etc.)
     * 
     * @param jobParameters the parameters used in the process
     * @param organizationReversionCounts a Map of named statistics generated by running the process
     * @see org.kuali.kfs.gl.batch.service.ReversionProcessService#organizationReversionProcessBeginningOfYear(org.kuali.kfs.gl.businessobject.OriginEntryGroup, java.util.Map, java.util.Map)
     */
    public void reversionCurrentYearAccountProcess(Map jobParameters, Map<String, Integer> reversionCounts) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("accountReversionProcessEndOfYear() started");
        }
        ReversionProcess orp = SpringContext.getBean(ReversionProcess.class,AccountReversionProcessServiceImpl.ACCOUNT_REVERSION_CURRENT_YEAR_ACCOUNT_PROCESS_BEAN_NAME);

        LOG.info("processing account reversions for current year accounts");
        orp.reversionProcess(jobParameters, reversionCounts);
        
        writeReports(orp, jobParameters, reversionCounts);
    }

    /**
     * Returns a Map with the properly initialized parameters for an organization reversion job that is about to run
     * @return a Map holding parameters for the job
     * @see org.kuali.kfs.gl.batch.service.ReversionProcessService#getJobParameters()
     */
    public Map getJobParameters() {
        // Get job parameters
        Map jobParameters = new HashMap();
        String strTransactionDate = getParameterService().getParameterValueAsString(KfsParameterConstants.GENERAL_LEDGER_BATCH.class, GeneralLedgerConstants.ANNUAL_CLOSING_TRANSACTION_DATE_PARM);
        jobParameters.put(KFSConstants.UNALLOC_OBJECT_CD, getParameterService().getParameterValueAsString(Reversion.class, CuGeneralLedgerConstants.ReversionProcess.UNALLOC_OBJECT_CODE_PARM));
        jobParameters.put(CUKFSConstants.CASH_REVERSION_OBJECT_CD, getParameterService().getParameterValueAsString(Reversion.class, CuGeneralLedgerConstants.ReversionProcess.CASH_REVERSION_OBJECT_CODE_PARM));
        jobParameters.put(KFSConstants.BEG_BUD_CASH_OBJECT_CD, getParameterService().getParameterValueAsString(Reversion.class, CuGeneralLedgerConstants.ReversionProcess.CARRY_FORWARD_OBJECT_CODE));
        jobParameters.put(KFSConstants.FUND_BAL_OBJECT_CD, getParameterService().getParameterValueAsString(KfsParameterConstants.GENERAL_LEDGER_BATCH.class, GeneralLedgerConstants.ANNUAL_CLOSING_FUND_BALANCE_OBJECT_CODE_PARM));
        String strUniversityFiscalYear = getParameterService().getParameterValueAsString(KfsParameterConstants.GENERAL_LEDGER_BATCH.class, GeneralLedgerConstants.ANNUAL_CLOSING_FISCAL_YEAR_PARM);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            java.util.Date jud = sdf.parse(strTransactionDate);
            jobParameters.put(KFSConstants.TRANSACTION_DT, new java.sql.Date(jud.getTime()));
        }
        catch (ParseException e) {
            throw new IllegalArgumentException("TRANSACTION_DT is an invalid date");
        }
        try {
            jobParameters.put(KFSConstants.UNIV_FISCAL_YR, new Integer(strUniversityFiscalYear));
        }
        catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("UNIV_FISCAL_YR is an invalid year");
        }
        return jobParameters;
    }
    
    /**
     * 
     * @param organizationReversionProcess
     * @param jobParameters
     * @param counts
     */
    public void writeReports(ReversionProcess reversionProcess, Map jobParameters, Map<String, Integer> counts) {
        // write job parameters
        for (Object jobParameterKeyAsObject : jobParameters.keySet()) {
            if (jobParameterKeyAsObject != null) {
                final String jobParameterKey = jobParameterKeyAsObject.toString();
                getAccountReversionReportWriterService().writeParameterLine("%32s %10s", jobParameterKey, jobParameters.get(jobParameterKey));
            }
        }
        
        // write statistics
        getAccountReversionReportWriterService().writeStatisticLine("NUMBER OF GLBL RECORDS READ....: %10d", counts.get("balancesRead"));
        getAccountReversionReportWriterService().writeStatisticLine("NUMBER OF GLBL RECORDS SELECTED: %10d", counts.get("balancesSelected"));
        getAccountReversionReportWriterService().writeStatisticLine("NUMBER OF SEQ RECORDS WRITTEN..: %10d", counts.get("recordsWritten"));
        getAccountReversionReportWriterService().pageBreak();
        
        // write ledger report
        getAccountReversionReportWriterService().writeSubTitle(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.MESSAGE_REPORT_YEAR_END_ACCOUNT_REVERSION_LEDGER_TITLE_LINE));
        reversionProcess.writeLedgerSummaryReport(getAccountReversionReportWriterService());
    }

    /**
     * Sets the implementation of ParameterService to use
     * @param parameterService an implementation of ParameterService
     */
    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    /**
     * Sets the implementation of the ConfigurationService to use
     * @param configurationService an implementation of the ConfigurationService
     */
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * Gets the parameterService attribute. 
     * @return Returns the parameterService.
     */
    public ParameterService getParameterService() {
        return parameterService;
    }

    /**
     * Gets the configurationService attribute. 
     * @return Returns the configurationService.
     */
    public ConfigurationService getConfigurationService() {
        return configurationService;
    }
}
