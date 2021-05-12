package edu.cornell.kfs.concur.batch.businessobject.format;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.web.format.Formatter;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class ConcurJournalAccountCodeFormatter extends Formatter {
    private static final long serialVersionUID = -2058566831766280380L;
    private static final Logger LOG = LogManager.getLogger(ConcurJournalAccountCodeFormatter.class);
    
    protected ParameterService parameterService;

    @Override
    protected Object convertToObject(String target) {
        if (StringUtils.equalsIgnoreCase(target, ConcurConstants.PENDING_CLIENT)) {
            String overrideObjectCode = getParameterService().getParameterValueAsString(CUKFSConstants.ParameterNamespaces.CONCUR, 
                    CUKFSParameterKeyConstants.ALL_COMPONENTS, ConcurParameterConstants.CONCUR_PENDING_CLIENT_OBJECT_CODE_OVERRIDE);
            LOG.error("replacePendingClientWithOverride, found a Pending Client object code, replacing it with " + overrideObjectCode);
            return overrideObjectCode;
        }
        return target;
    }

    public ParameterService getParameterService() {
        if (parameterService == null) {
            parameterService = SpringContext.getBean(ParameterService.class);
        }
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
    
}
