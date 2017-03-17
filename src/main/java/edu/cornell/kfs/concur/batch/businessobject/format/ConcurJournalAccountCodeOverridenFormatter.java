package edu.cornell.kfs.concur.batch.businessobject.format;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.web.format.Formatter;

import edu.cornell.kfs.concur.ConcurConstants;

public class ConcurJournalAccountCodeOverridenFormatter extends Formatter {
    
    @Override
    protected Object convertToObject(String target) {
        Boolean wasOverriden = new Boolean(StringUtils.equalsIgnoreCase(target, ConcurConstants.PENDING_CLIENT));
        return wasOverriden;
    }
}
