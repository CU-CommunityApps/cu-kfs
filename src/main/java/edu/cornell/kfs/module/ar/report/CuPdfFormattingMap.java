package edu.cornell.kfs.module.ar.report;

import java.util.Map;
import java.util.Objects;

import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.report.PdfFormattingMap;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public class CuPdfFormattingMap extends PdfFormattingMap {

    public CuPdfFormattingMap(Map<?, ?> mapToWrap) {
        super(mapToWrap);
    }

    /**
     * Overridden to properly return currency-formatted strings for KualiDecimal values,
     * but otherwise has the same code and functionality as in the superclass.
     */
    @Override
    protected String stringifyValue(Object value) {
        if (ObjectUtils.isNull(value)) {
            return KFSConstants.EMPTY_STRING;
        } else if (value instanceof String) {
            return (String) value;
        } else if (value instanceof java.util.Date) {
            return getDateTimeService().toDateString((java.util.Date) value);
        } else if (value instanceof Boolean || Boolean.TYPE.equals(value.getClass())) {
            return stringifyBooleanForContractsGrantsInvoiceTemplate((Boolean) value);
        } else if (value instanceof KualiDecimal) {
            // Cornell Customization: Add missing "return" keyword to this statement.
            return getContractsGrantsBillingUtilityService().formatForCurrency((KualiDecimal) value);
        }
        return Objects.toString(value);
    }

}
