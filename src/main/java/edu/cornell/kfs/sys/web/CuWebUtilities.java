package edu.cornell.kfs.sys.web;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.kim.CuKimPropertyConstants;
import edu.cornell.kfs.sys.CUKFSConstants;

public final class CuWebUtilities {

    private static final Set<String> MASKABLE_PERSON_FIELDS = Set.of(
            "name",
            "firstName",
            "middleName",
            "lastName",
            "addressLine1",
            "addressLine2",
            "addressLine3",
            "addressCity",
            "addressStateProvinceCode",
            "addressPostalCode",
            "addressCountryCode",
            "emailAddress",
            "phoneNumber",
            "altAddressLine1",
            "altAddressLine2",
            "altAddressLine3",
            "altAddressCity",
            "altAddressStateProvinceCode",
            "altAddressPostalCode",
            "altAddressCountryCode"
    );

    private static final String NOT_FOUND_MESSAGE = "not found";
    private static final String AD_HOC_ROUTE_PERSON_PROPERTY_FRAGMENT = "HocRoutePerson";

    private CuWebUtilities() {
        throw new UnsupportedOperationException("do not call");
    }

    public static String convertPersonNameForDisplayIfNecessary(String personName, ActionForm kualiForm,
            String personNameFieldName) {
        if (StringUtils.isBlank(personName)
                || StringUtils.equalsIgnoreCase(personName, KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK)
                || StringUtils.containsIgnoreCase(personName, NOT_FOUND_MESSAGE)) {
            return personName;
        } else {
            return (String) ObjectUtils.getPropertyValue(kualiForm, personNameFieldName);
        }
    }

    public static String convertPersonFieldConversionsForMasking(String fieldConversions) {
        if (StringUtils.isBlank(fieldConversions)) {
            return fieldConversions;
        }
        return Arrays.stream(StringUtils.split(fieldConversions, KFSConstants.COMMA))
                .map(CuWebUtilities::updateFieldConversion)
                .collect(Collectors.joining(KFSConstants.COMMA));
    }

    private static String updateFieldConversion(String fieldConversion) {
        String personFieldName = StringUtils.substringBefore(fieldConversion, CUKFSConstants.COLON);
        String formFieldName = StringUtils.substringAfter(fieldConversion, CUKFSConstants.COLON);
        if (MASKABLE_PERSON_FIELDS.contains(personFieldName)) {
            if (StringUtils.endsWith(formFieldName, KFSConstants.DELIMITER + personFieldName)
                    && !StringUtils.contains(formFieldName, AD_HOC_ROUTE_PERSON_PROPERTY_FRAGMENT)) {
                formFieldName += CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX;
            }
            personFieldName += CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX;
        }
        return StringUtils.join(personFieldName, CUKFSConstants.COLON, formFieldName);
    }

    public static String convertPersonLookupParametersForMasking(String lookupParameters) {
        if (StringUtils.isBlank(lookupParameters)) {
            return lookupParameters;
        }
        return Arrays.stream(StringUtils.split(lookupParameters, KFSConstants.COMMA))
                .map(CuWebUtilities::updateLookupParameter)
                .collect(Collectors.joining(KFSConstants.COMMA));
    }

    private static String updateLookupParameter(String lookupParameter) {
        String personFieldName = StringUtils.substringAfter(lookupParameter, CUKFSConstants.COLON);
        String formFieldName = StringUtils.substringBefore(lookupParameter, CUKFSConstants.COLON);
        if (MASKABLE_PERSON_FIELDS.contains(personFieldName)) {
            if (StringUtils.endsWith(formFieldName, KFSConstants.DELIMITER + personFieldName)
                    && !StringUtils.contains(formFieldName, AD_HOC_ROUTE_PERSON_PROPERTY_FRAGMENT)) {
                formFieldName += CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX;
            }
            personFieldName += CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX;
        }
        return StringUtils.join(formFieldName, CUKFSConstants.COLON, personFieldName);
    }
    
    public static long convertLocalDateTimeToMilliseconds(LocalDateTime localDateTime) {
        long milliseconds = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return milliseconds;
    }

    public static String urlEncode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

}
