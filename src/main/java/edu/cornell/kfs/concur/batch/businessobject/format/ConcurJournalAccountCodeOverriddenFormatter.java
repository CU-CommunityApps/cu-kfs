package edu.cornell.kfs.concur.batch.businessobject.format;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.core.web.format.Formatter;

import edu.cornell.kfs.concur.ConcurConstants;

public class ConcurJournalAccountCodeOverriddenFormatter extends Formatter {
    
    @Override
    protected Object convertToObject(String target) {
        Boolean wasOverridden = new Boolean(StringUtils.equalsIgnoreCase(target, ConcurConstants.PENDING_CLIENT));
        return wasOverridden;
    }
}
