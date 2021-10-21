package edu.cornell.kfs.coa.batch.dataaccess.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.batch.FiscalYearMakerStep;
import org.kuali.kfs.sys.batch.dataaccess.impl.FiscalYearMakerImpl;
import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;

public class AccountReversionBaseFiscalYearMaker extends FiscalYearMakerImpl {
    private static final Logger LOG = LogManager.getLogger();
    
    private ParameterService parameterService;
    
    @Override
    public Criteria createSelectionCriteria(Integer baseFiscalYear) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("starting createSelectionCriteria() for bo class " + businessObjectClass.getName());
        }

        Criteria criteria = new Criteria();
        addYearCriteria(criteria, baseFiscalYear, false);

        // add active criteria if the business object class supports the inactivateable interface
        List<String> fields = getPropertyNames();
        if (MutableInactivatable.class.isAssignableFrom(businessObjectClass) && fields.contains(
                KFSPropertyConstants.ACTIVE) && !carryForwardInactive && onlySelectActiveAccountReversions()) {
            criteria.addEqualTo(KFSPropertyConstants.ACTIVE, KFSConstants.ACTIVE_INDICATOR);
        }

        return criteria;
    }
    
    protected boolean onlySelectActiveAccountReversions() {
        boolean replaceMode = parameterService.getParameterValueAsBoolean(FiscalYearMakerStep.class,
                KFSConstants.FISCAL_YEAR_MAKER_REPLACE_MODE);
        if (LOG.isDebugEnabled()) {
            LOG.info("onlySelectActiveAccountReversions, returning " + replaceMode);
        }
        return replaceMode;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
