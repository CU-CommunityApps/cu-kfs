package edu.cornell.kfs.sys.businessobject.format;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.businessobject.format.KualiDecimalFormatter;
import org.kuali.rice.core.web.format.FormatException;

public class RemovePlusSignKualiDecimalFormatter extends KualiDecimalFormatter {
    private static final long serialVersionUID = 5281313124678744980L;
    private static final Logger LOG = LogManager.getLogger(RemovePlusSignKualiDecimalFormatter.class);

    @Override
    protected Object convertToObject(String target) {
        if (StringUtils.startsWith(target, "+")) {
            LOG.debug("convertToObject, the orginal target: " + target);
            target = StringUtils.remove(target, "+");
            LOG.debug("convertToObject, the converted target: " + target);
        }
        if (StringUtils.isNotBlank(target)) {
            try {
                return super.convertToObject(target);
            } catch (FormatException fe) {
                LOG.error("convertToObject, found an invalid number: " + target, fe);
            }
        }
        return null;
    }
}
