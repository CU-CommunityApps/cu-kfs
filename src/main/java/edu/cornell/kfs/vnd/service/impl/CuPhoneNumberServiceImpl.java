package edu.cornell.kfs.vnd.service.impl;

import org.kuali.kfs.vnd.VendorParameterConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.service.impl.PhoneNumberServiceImpl;
import org.kuali.kfs.krad.util.ObjectUtils;


public class CuPhoneNumberServiceImpl extends PhoneNumberServiceImpl {

    @Override
    public String formatNumberIfPossible(final String unformattedNumber) {
        if (ObjectUtils.isNull(unformattedNumber)) {
            return unformattedNumber;
        }
        final String formattedNumber = unformattedNumber.replaceAll("\\D", "");
        final Integer defaultPhoneNumberDigits =
                Integer.valueOf(parameterService.getParameterValueAsString(VendorDetail.class,
                        VendorParameterConstants.DEFAULT_PHONE_NUMBER_DIGITS
                ));
        if (formattedNumber.length() < defaultPhoneNumberDigits) {
            return unformattedNumber;
        }
        else if (formattedNumber.length() > defaultPhoneNumberDigits) { // assume phone number includes an extension and format using only the first 10 characters
            return formattedNumber.substring(0, 3) + "-" + formattedNumber.substring(3, 6) + "-" +
                    formattedNumber.substring(6, 10);
        }
        else {
            return formattedNumber.substring(0, 3) + "-" + formattedNumber.substring(3, 6) + "-" +
                    formattedNumber.substring(6, 10);
        }
    }

}
