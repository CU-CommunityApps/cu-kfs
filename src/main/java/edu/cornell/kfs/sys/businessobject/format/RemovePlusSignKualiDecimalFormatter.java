package edu.cornell.kfs.sys.businessobject.format;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.sys.businessobject.format.KualiDecimalFormatter;
import org.kuali.rice.core.api.util.RiceKeyConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.web.format.FormatException;

public class RemovePlusSignKualiDecimalFormatter extends KualiDecimalFormatter {
    private static final long serialVersionUID = 5281313124678744980L;
    private static Logger LOG = Logger.getLogger(RemovePlusSignKualiDecimalFormatter.class);
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("\\-?[0-9,]*\\.?[0-9]*");

    @Override
    protected Object convertToObject(String target) {
        if (StringUtils.startsWith(target, "+")) {
            LOG.debug("convertToObject, the orginal target: " + target);
            target = StringUtils.remove(target, "+");
            LOG.debug("convertToObject, the converted target: " + target);
        }
        try {
            return super.convertToObject(target);
        } catch (FormatException fe) {
            LOG.error("convertToObject, found an invalid number: " + target, fe);
            return null;
        }
    }
}
