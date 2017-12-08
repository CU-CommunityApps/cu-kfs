package edu.cornell.kfs.pdp.businessobject.inquiry;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.kns.web.ui.Field;
import org.kuali.kfs.kns.web.ui.Section;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.businessobject.inquiry.PaymentDetailInquirable;
import org.kuali.rice.location.api.country.Country;

import edu.cornell.kfs.pdp.CUPdpPropertyConstants;

@SuppressWarnings("deprecation")
public class CuPaymentDetailInquirable extends PaymentDetailInquirable {
    private static final long serialVersionUID = -5852448786635585758L;

    /**
     * Overridden to prevent exceptions when the country field is blank,
     * or when it contains a country name instead of a country code.
     * 
     * @see org.kuali.kfs.pdp.businessobject.inquiry.PaymentDetailInquirable#convertCountryForDisplay(org.kuali.kfs.kns.web.ui.Section)
     */
    @Override
    protected void convertCountryForDisplay(Section section) {
        section.getRows().stream()
                .flatMap((row) -> row.getFields().stream())
                .filter(this::isPaymentGroupCountryField)
                .findFirst()
                .ifPresent(this::convertCountryForDisplayIfNecessary);
    }

    protected boolean isPaymentGroupCountryField(Field field) {
        return StringUtils.equalsIgnoreCase(CUPdpPropertyConstants.PAYMENT_COUNTRY, field.getPropertyName());
    }

    protected void convertCountryForDisplayIfNecessary(Field field) {
        String propertyValue = field.getPropertyValue();
        if (StringUtils.isNotBlank(propertyValue)) {
            Country country = getCountryService().getCountry(propertyValue);
            if (ObjectUtils.isNotNull(country)) {
                field.setPropertyValue(country.getName());
            }
        }
    }

}
