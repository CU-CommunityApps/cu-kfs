package edu.cornell.kfs.pdp.businessobject.inquiry;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kns.web.ui.Field;
import org.kuali.kfs.kns.web.ui.Section;
import org.kuali.kfs.pdp.businessobject.inquiry.PaymentDetailInquirable;

import edu.cornell.kfs.pdp.CUPdpPropertyConstants;

@SuppressWarnings("deprecation")
public class CuPaymentDetailInquirable extends PaymentDetailInquirable {
    private static final long serialVersionUID = -5852448786635585758L;

    /**
     * Overridden to handle a space in the country code.
     */
    @Override
    protected void convertCountryForDisplay(final Section section) {
        section.getRows().stream()
                .flatMap((row) -> row.getFields().stream())
                .filter(this::isPaymentGroupCountryField)
                .findFirst()
                .ifPresent(this::resetBlankPropertyValueToEmptyString);
        super.convertCountryForDisplay(section);
    }

    protected boolean isPaymentGroupCountryField(final Field field) {
        return StringUtils.equalsIgnoreCase(CUPdpPropertyConstants.PAYMENT_COUNTRY, field.getPropertyName());
    }
    
    protected void resetBlankPropertyValueToEmptyString(final Field field) {
        if (StringUtils.isBlank(field.getPropertyValue())) {
            field.setPropertyValue(StringUtils.EMPTY);
        }
    }

}
