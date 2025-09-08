/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.krad.util;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.CoreApiServiceLocator;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.core.api.util.Truth;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.CoreFrameworkServiceLocator;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.service.KualiModuleService;
import org.kuali.kfs.krad.service.ModuleService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * ====
 * CU Customization: Backported the FINP-10948 changes.
 *                   This overlay can be removed when we upgrade to the 2024-05-22 financials patch.
 * ====
 * 
 * Miscellaneous Utility Methods
 */
public final class KRADUtils {

    private static final KualiDecimal ONE_HUNDRED = new KualiDecimal("100.00");
    private static KualiModuleService kualiModuleService;

    private KRADUtils() {
        throw new UnsupportedOperationException("do not call");
    }

    public static String getBusinessTitleForClass(final Class<? extends Object> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("The getBusinessTitleForClass method of KRADUtils requires a " +
                    "non-null class");
        }
        final String className = clazz.getSimpleName();

        final StringBuffer label = new StringBuffer(className.substring(0, 1));
        for (int i = 1; i < className.length(); i++) {
            if (Character.isLowerCase(className.charAt(i))) {
                label.append(className.charAt(i));
            } else {
                label.append(" ").append(className.charAt(i));
            }
        }
        return label.toString().trim();
    }

    /**
     * ====
     * CU Customization: Updated this method with the FINP-10948 changes.
     * ====
     * 
     * Convert the given money amount into a integer string. Since the return string cannot have decimal points or
     * commas, those are removed. Done this way so leading zeros for amounts less than 1 are not lost.
     * For example, 320.15 is converted into 32015 and 0.15 is converted into 015.
     *
     * @return an integer string of the given money amount.
     */
    public static String convertDecimalIntoInteger(final KualiDecimal decimalNumber) {
        final NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        final String formattedAmount = formatter.format(decimalNumber);
        return StringUtils.replace(formattedAmount, ",", "").replace(".", "");
    }

    public static Integer getIntegerValue(final String numberStr) {
        Integer numberInt;
        try {
            numberInt = Integer.valueOf(numberStr);
        } catch (final NumberFormatException nfe) {
            final Double numberDbl = Double.valueOf(numberStr);
            numberInt = numberDbl.intValue();
        }
        return numberInt;
    }

    /**
     * Attempt to coerce a String attribute value to the given propertyType.  If the transformation can't be made,
     * either because the propertyType is null or because the transformation required exceeds this method's very small
     * bag of tricks, then null is returned.
     *
     * @param propertyType   the Class to coerce the attributeValue to
     * @param attributeValue the String value to coerce
     * @return an instance of the propertyType class, or null the transformation can't be made.
     */
    public static Object hydrateAttributeValue(final Class<?> propertyType, final String attributeValue) {
        Object attributeValueObject = null;
        if (propertyType != null && attributeValue != null) {
            if (String.class.equals(propertyType)) {
                // it's already a String
                attributeValueObject = attributeValue;
            } else if (Boolean.class.equals(propertyType) || Boolean.TYPE.equals(propertyType)) {
                attributeValueObject = Truth.strToBooleanIgnoreCase(attributeValue);
            } else {
                // try to create one with KRADUtils for other misc data types
                attributeValueObject = KRADUtils.createObject(propertyType, new Class[]{String.class},
                        new Object[]{attributeValue});
                // if that didn't work, we'll get a null back
            }
        }
        return attributeValueObject;
    }

    public static Object createObject(final Class<?> clazz, final Class<?>[] argumentClasses, final Object[] argumentValues) {
        if (clazz == null) {
            return null;
        }
        if (argumentClasses.length == 1 && argumentClasses[0] == String.class) {
            if (argumentValues.length == 1 && argumentValues[0] != null) {
                if (clazz == String.class) {
                    // this means we're trying to create a String from a String don't new up Strings, it's a bad idea
                    return argumentValues[0];
                } else {
                    // maybe it's a type that supports valueOf?
                    Method valueOfMethod = null;
                    try {
                        valueOfMethod = clazz.getMethod("valueOf", String.class);
                    } catch (final NoSuchMethodException e) {
                        // ignored
                    }
                    if (valueOfMethod != null) {
                        try {
                            return valueOfMethod.invoke(null, argumentValues[0]);
                        } catch (final Exception e) {
                            // ignored
                        }
                    }
                }
            }
        }
        try {
            final Constructor<?> constructor = clazz.getConstructor(argumentClasses);
            return constructor.newInstance(argumentValues);
        } catch (final Exception e) {
            // ignored
        }
        return null;
    }

    public static String joinWithQuotes(final List<String> list) {
        if (list == null || list.size() == 0) {
            return "";
        }

        return KRADConstants.SINGLE_QUOTE + StringUtils.join(list.iterator(), KRADConstants.SINGLE_QUOTE + "," +
                KRADConstants.SINGLE_QUOTE) + KRADConstants.SINGLE_QUOTE;
    }

    private static KualiModuleService getKualiModuleService() {
        if (kualiModuleService == null) {
            kualiModuleService = KRADServiceLocatorWeb.getKualiModuleService();
        }
        return kualiModuleService;
    }

    /**
     * TODO this method will probably need to be exposed in a public KRADUtils class as it is used by several
     * different modules.  That will have to wait until ModuleService and KualiModuleService are moved to core though.
     */
    public static String getNamespaceCode(final Class<? extends Object> clazz) {
        final ModuleService moduleService = getKualiModuleService().getResponsibleModuleService(clazz);
        if (moduleService == null) {
            return KFSConstants.CoreModuleNamespaces.KFS;
        }
        return moduleService.getModuleConfiguration().getNamespaceCode();
    }

    public static Map<String, String> getNamespaceAndComponentSimpleName(final Class<? extends Object> clazz) {
        final Map<String, String> map = new HashMap<>();
        map.put(org.kuali.kfs.krad.util.KRADConstants.NAMESPACE_CODE, getNamespaceCode(clazz));
        map.put(org.kuali.kfs.krad.util.KRADConstants.COMPONENT_NAME, getComponentSimpleName(clazz));
        return map;
    }

    public static Map<String, String> getNamespaceAndActionClass(final Class<? extends Object> clazz) {
        final Map<String, String> map = new HashMap<>();
        map.put(org.kuali.kfs.krad.util.KRADConstants.NAMESPACE_CODE, getNamespaceCode(clazz));
        map.put(org.kuali.kfs.krad.util.KRADConstants.ACTION_CLASS, clazz.getName());
        return map;
    }

    private static String getComponentSimpleName(final Class<? extends Object> clazz) {
        return clazz.getSimpleName();
    }

    /**
     * Parses a string that is in map format (commas separating map entries, colon separates map key/value) to a new
     * map instance
     *
     * @param parameter string parameter to parse
     * @return Map<String, String> instance populated from string parameter
     */
    public static Map<String, String> convertStringParameterToMap(final String parameter) {
        final Map<String, String> map = new HashMap<>();

        if (StringUtils.isNotBlank(parameter)) {
            if (StringUtils.contains(parameter, ",")) {
                final String[] fieldConversions = StringUtils.split(parameter, ",");

                for (final String fieldConversionStr : fieldConversions) {
                    if (StringUtils.isNotBlank(fieldConversionStr)) {
                        if (StringUtils.contains(fieldConversionStr, ":")) {
                            final String[] fieldConversion = StringUtils.split(fieldConversionStr, ":");
                            map.put(fieldConversion[0], fieldConversion[1]);
                        } else {
                            map.put(fieldConversionStr, fieldConversionStr);
                        }
                    }
                }
            } else if (StringUtils.contains(parameter, ":")) {
                final String[] fieldConversion = StringUtils.split(parameter, ":");
                map.put(fieldConversion[0], fieldConversion[1]);
            } else {
                map.put(parameter, parameter);
            }
        }

        return map;
    }

    /**
     * Parses a string that is in list format (commas separating list entries) to a new List instance
     *
     * @param parameter string parameter to parse
     * @return List&lt;String&gt; instance populated from string parameter
     */
    public static List<String> convertStringParameterToList(final String parameter) {
        final List<String> list = new ArrayList<>();

        if (StringUtils.isNotBlank(parameter)) {
            if (StringUtils.contains(parameter, ",")) {
                final String[] parameters = StringUtils.split(parameter, ",");
                final List arraysList = Arrays.asList(parameters);
                list.addAll(arraysList);
            } else {
                list.add(parameter);
            }
        }

        return list;
    }

    /**
     * Translates the given Map of String keys and String array values to a Map of String key and values. If the
     * String array contains more than one value, the single string is built by joining the values with the vertical
     * bar character
     *
     * @param requestParameters Map of request parameters to translate
     * @return Map<String, String> translated Map
     */
    public static Map<String, String> translateRequestParameterMap(final Map<String, String[]> requestParameters) {
        final Map<String, String> parameters = new HashMap<>();

        for (final Map.Entry<String, String[]> parameter : requestParameters.entrySet()) {
            final String parameterValue;
            if (parameter.getValue().length > 1) {
                parameterValue = StringUtils.join(parameter.getValue(), "|");
            } else {
                parameterValue = parameter.getValue()[0];
            }
            parameters.put(parameter.getKey(), parameterValue);
        }

        return parameters;
    }

    /**
     * Retrieves parameter values from the request that match the requested names. In addition, based on the object
     * class an authorization check is performed to determine if the values are secure and should be decrypted. If
     * true, the value is decrypted before returning
     *
     * @param parameterNames    names of the parameters whose values should be retrieved from the request
     * @param parentObjectClass object class that contains the parameter names as properties and should be consulted
     *                          for security checks
     * @param requestParameters all request parameters to pull from
     * @return Map<String, String> populated with parameter name/value pairs pulled from the request
     */
    public static Map<String, String> getParametersFromRequest(
            final List<String> parameterNames, final Class<?> parentObjectClass,
            final Map<String, String> requestParameters) {
        final Map<String, String> parameterValues = new HashMap<>();

        for (final String keyPropertyName : parameterNames) {
            if (requestParameters.get(keyPropertyName) != null) {
                String keyValue = requestParameters.get(keyPropertyName);

                // Check if this element was encrypted, if it was decrypt it
                if (KRADServiceLocatorWeb.getDataObjectAuthorizationService()
                        .attributeValueNeedsToBeEncryptedOnFormsAndLinks(parentObjectClass, keyPropertyName)) {
                    try {
                        keyValue = StringUtils.removeEnd(keyValue, EncryptionService.ENCRYPTION_POST_PREFIX);
                        keyValue = CoreApiServiceLocator.getEncryptionService().decrypt(keyValue);
                    } catch (final GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    }
                }

                parameterValues.put(keyPropertyName, keyValue);
            }
        }

        return parameterValues;
    }

    public static boolean containsSensitiveDataPatternMatch(final String fieldValue) {
        if (StringUtils.isBlank(fieldValue)) {
            return false;
        }
        final ParameterService parameterService = CoreFrameworkServiceLocator.getParameterService();
        final Collection<String> sensitiveDataPatterns = parameterService.getParameterValuesAsString(
                KFSConstants.CoreModuleNamespaces.KFS, KfsParameterConstants.DOCUMENT_COMPONENT,
                KRADConstants.SystemGroupParameterNames.SENSITIVE_DATA);
        for (final String pattern : sensitiveDataPatterns) {
            if (Pattern.compile(pattern).matcher(fieldValue).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the UserSession object from the HttpServletRequest object's associated session.
     * <p>
     * In some cases (different threads) the UserSession cannot be retrieved from GlobalVariables but can still be
     * accessed via the session object.
     */
    public static UserSession getUserSessionFromRequest(final HttpServletRequest request) {
        return (UserSession) request.getSession().getAttribute(org.kuali.kfs.krad.util.KRADConstants.USER_SESSION_KEY);
    }

    /**
     * Gets the principal id for the user from the HttpServletRequest object's associated session.
     * <p>
     * In some cases (different threads) the UserSession cannot be retrieved from GlobalVariables but can still be
     * accessed via the session object.
     */
    public static String getPrincipalIdFromRequest(final HttpServletRequest request) {
        return getUserSessionFromRequest(request).getPerson().getPrincipalId();
    }

    /**
     * @return whether the deploy environment is production
     */
    public static boolean isProductionEnvironment() {
        final Environment environment = SpringContext.getBean(Environment.class);
        return environment.isProductionEnvironment();
    }
}
