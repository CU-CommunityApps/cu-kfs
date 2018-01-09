package edu.cornell.kfs.fp.document.service.impl;

import edu.cornell.kfs.fp.document.service.AccountFundsUpdateService;

import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.validation.impl.AccountingDocumentRuleBaseConstants.APPLICATION_PARAMETER;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.parameter.ParameterEvaluator;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;

public class AccountFundsUpdateServiceImpl implements AccountFundsUpdateService {
    private ParameterService parameterService;
//todo evaluate necessity of this file

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
}
