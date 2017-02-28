package edu.cornell.kfs.concur.batch.businessobject.format;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.sys.businessobject.format.KualiDecimalFormatter;

public class ConcurKualiDecimalFormatter extends KualiDecimalFormatter {
    private static final long serialVersionUID = 5281313124678744980L;
    private static Logger LOG = Logger.getLogger(ConcurKualiDecimalFormatter.class);

    @Override
    protected Object convertToObject(String target) {
        if (StringUtils.startsWith(target, "+")) {
            LOG.debug("convertToObject, the orginal target: " + target);
            target = StringUtils.remove(target, "+");
            LOG.debug("convertToObject, the converted target: " + target);
        }
        return super.convertToObject(target);
    }
}
