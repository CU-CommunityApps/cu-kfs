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
package org.kuali.kfs.pdp.businessobject.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.fp.FPKeyConstants;
import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.lookup.Lookupable;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.businessobject.ACHPayee;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.service.impl.DefaultSearchService;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.springframework.util.MultiValueMap;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A custom search service that queries Vendor and/or Person tables and uses the results to construct a list of
 * ACHPayee results.
 */
public class ACHPayeeSearchService extends DefaultSearchService {
    private final DataDictionaryService dataDictionaryService;
    private final DisbursementVoucherPayeeService disbursementVoucherPayeeService;
    private final Lookupable vendorLookupable;
    private final PersonService personService;

    public ACHPayeeSearchService(
            final DataDictionaryService dataDictionaryService,
            final DisbursementVoucherPayeeService disbursementVoucherPayeeService,
            final Lookupable vendorLookupable,
            final PersonService personService
    ) {
        this.dataDictionaryService = dataDictionaryService;
        this.disbursementVoucherPayeeService = disbursementVoucherPayeeService;
        this.vendorLookupable = vendorLookupable;
        this.personService = personService;
    }

    @Override
    public Pair<Collection<? extends BusinessObjectBase>, Integer> getSearchResults(
            final Class<? extends BusinessObjectBase> businessObjectClass,
            final MultiValueMap<String, String> fieldValues,
            final int skip, final int limit, final String sortField, final boolean sortAscending
    ) {
        final List<DisbursementPayee> searchResults = new ArrayList<>();

        final String payeeTypeCode = fieldValues.getFirst(KFSPropertyConstants.PAYEE_TYPE_CODE);
        if (StringUtils.isBlank(payeeTypeCode)) {
            GlobalVariables.getMessageMap().putInfo(KFSPropertyConstants.PAYEE_TYPE_CODE,
                    PdpKeyConstants.MESSAGE_PDP_ACH_PAYEE_LOOKUP_NO_PAYEE_TYPE);
        }

        if (shouldGetVendorsAsPayees(fieldValues, payeeTypeCode)) {
            searchResults.addAll(getVendorsAsPayees(fieldValues));
        } else if (shouldGetPersonAsPayees(fieldValues, payeeTypeCode)) {
            searchResults.addAll(getPersonAsPayees(fieldValues.toSingleValueMap()));
        } else {
            searchResults.addAll(getVendorsAsPayees(fieldValues));
            searchResults.addAll(getPersonAsPayees(fieldValues.toSingleValueMap()));
        }

        return Pair.of(searchResults, searchResults.size());
    }

    private static Boolean shouldGetVendorsAsPayees(
            final MultiValueMap<? super String, String> fieldValues, final String payeeTypeCode
    ) {
        return StringUtils.isNotBlank(fieldValues.getFirst(KFSPropertyConstants.VENDOR_NUMBER))
            || StringUtils.isNotBlank(fieldValues.getFirst(KFSPropertyConstants.VENDOR_NAME))
            || StringUtils.isNotBlank(payeeTypeCode) && PdpConstants.PayeeIdTypeCodes.VENDOR_ID.equals(payeeTypeCode);
    }

    private static Boolean shouldGetPersonAsPayees(
            final MultiValueMap<? super String, String> fieldValues, final String payeeTypeCode
    ) {
        return StringUtils.isNotBlank(fieldValues.getFirst(KIMPropertyConstants.Person.EMPLOYEE_ID))
               || StringUtils.isNotBlank(fieldValues.getFirst(KIMPropertyConstants.Person.ENTITY_ID))
               || StringUtils.isNotBlank(payeeTypeCode)
                  && (PdpConstants.PayeeIdTypeCodes.EMPLOYEE.equals(payeeTypeCode)
                      || PdpConstants.PayeeIdTypeCodes.ENTITY.equals(payeeTypeCode));
    }

    //CU customization to change from private to protected
    protected DisbursementPayee getPayeeFromPerson(final Person personDetail, final Map<String, String> fieldValues) {
        final DisbursementPayee payee = disbursementVoucherPayeeService.getPayeeFromPerson(personDetail);
        payee.setPaymentReasonCode(fieldValues.get(KFSPropertyConstants.PAYMENT_REASON_CODE));

        final String payeeTypeCode = fieldValues.get(KFSPropertyConstants.PAYEE_TYPE_CODE);

        final ACHPayee achPayee = new ACHPayee();

        final String payeeIdNumber;
        final String achPayeeTypeCode;
        if (PdpConstants.PayeeIdTypeCodes.ENTITY.equals(payeeTypeCode)) {
            payeeIdNumber = personDetail.getEntityId();
            achPayeeTypeCode = PdpConstants.PayeeIdTypeCodes.ENTITY;
        } else {
            payeeIdNumber = personDetail.getEmployeeId();
            achPayeeTypeCode = PdpConstants.PayeeIdTypeCodes.EMPLOYEE;
        }

        achPayee.setPayeeIdNumber(payeeIdNumber);
        achPayee.setPayeeTypeCode(achPayeeTypeCode);
        achPayee.setPayeeName(payee.getPayeeName());
        achPayee.setPrincipalId(payee.getPrincipalId());
        achPayee.setTaxNumber(payee.getTaxNumber());
        achPayee.setAddress(payee.getAddress());
        achPayee.setActive(payee.isActive());

        return achPayee;
    }

    public void validateSearchParameters(final Map<String, String> fieldValues) {
        final String vendorName = fieldValues.get(KFSPropertyConstants.VENDOR_NAME);
        final String vendorNumber = fieldValues.get(KFSPropertyConstants.VENDOR_NUMBER);
        final String entityId = fieldValues.get(KIMPropertyConstants.Person.ENTITY_ID);

        // only can use the vendor name and vendor number fields or the employee id field, but not both.
        final boolean isVendorInfoEntered = StringUtils.isNotBlank(vendorName) || StringUtils.isNotBlank(vendorNumber);
        if (StringUtils.isNotBlank(entityId) && isVendorInfoEntered) {
            final String messageKey = FPKeyConstants.ERROR_DV_VENDOR_EMPLOYEE_CONFUSION;

            final String vendorNameLabel = getAttributeLabel(KFSPropertyConstants.VENDOR_NAME);
            final String vendorNumberLabel = getAttributeLabel(KFSPropertyConstants.VENDOR_NUMBER);
            final String entityIdLabel = getAttributeLabel(KIMPropertyConstants.Person.ENTITY_ID);

            GlobalVariables.getMessageMap().putError(KIMPropertyConstants.Person.ENTITY_ID, messageKey, entityIdLabel,
                    vendorNameLabel, vendorNumberLabel);
        }

        final String employeeId = fieldValues.get(KIMPropertyConstants.Person.EMPLOYEE_ID);
        final String payeeTypeCode = fieldValues.get(KFSPropertyConstants.PAYEE_TYPE_CODE);
        final boolean isEmployeeInfoEntered = StringUtils.isNotBlank(employeeId) || StringUtils.isNotBlank(entityId);
        final boolean payeeTypeEntered = StringUtils.isNotBlank(payeeTypeCode);

        if (payeeTypeEntered
            && PdpConstants.PayeeIdTypeCodes.VENDOR_ID.equals(payeeTypeCode)
            && isEmployeeInfoEntered) {
            final String messageKey = PdpKeyConstants.ERROR_PAYEE_LOOKUP_VENDOR_EMPLOYEE_CONFUSION;

            final String employeeIdLabel = getAttributeLabel(KIMPropertyConstants.Person.EMPLOYEE_ID);
            final String entityIdLabel = getAttributeLabel(KIMPropertyConstants.Person.ENTITY_ID);
            final String payeeTypeLabel = getAttributeLabel(KFSPropertyConstants.PAYEE_TYPE_CODE);

            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.PAYEE_TYPE_CODE, messageKey, payeeTypeLabel,
                    payeeTypeCode, employeeIdLabel, entityIdLabel);
        } else if (payeeTypeEntered
                   && (PdpConstants.PayeeIdTypeCodes.EMPLOYEE.equals(payeeTypeCode)
                       || PdpConstants.PayeeIdTypeCodes.ENTITY.equals(payeeTypeCode)) && isVendorInfoEntered) {
            final String messageKey = PdpKeyConstants.ERROR_PAYEE_LOOKUP_VENDOR_EMPLOYEE_CONFUSION;

            final String vendorNameLabel = getAttributeLabel(KFSPropertyConstants.VENDOR_NAME);
            final String vendorNumberLabel = getAttributeLabel(KFSPropertyConstants.VENDOR_NUMBER);
            final String payeeTypeLabel = getAttributeLabel(KFSPropertyConstants.PAYEE_TYPE_CODE);

            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.PAYEE_TYPE_CODE, messageKey, payeeTypeLabel,
                    payeeTypeCode, vendorNameLabel, vendorNumberLabel);
        }

        if (GlobalVariables.getMessageMap().hasErrors()) {
            throw new ValidationException("errors in search criteria");
        }
    }

    // Scope loosened for CSU
    protected String getAttributeLabel(final String attributeName) {
        return dataDictionaryService.getAttributeLabel(ACHPayee.class, attributeName);
    }

    // Scope  loosened for CSU
    protected List<DisbursementPayee> getVendorsAsPayees(final MultiValueMap<String, String> fieldValues) {
        final List<DisbursementPayee> payeeList = new ArrayList<>();

        final Map<String, String> fieldsForLookup = getVendorFieldValues(fieldValues.toSingleValueMap());
        vendorLookupable.setBusinessObjectClass(VendorDetail.class);
        vendorLookupable.validateSearchParameters(fieldsForLookup);

        final List<? extends BusinessObject> vendorList = vendorLookupable.getSearchResults(fieldsForLookup);
        for (final BusinessObject vendor : vendorList) {
            final VendorDetail vendorDetail = (VendorDetail) vendor;
            final DisbursementPayee payee = getPayeeFromVendor(vendorDetail);
            payeeList.add(payee);
        }

        return payeeList;
    }

    private ACHPayee getPayeeFromVendor(final VendorDetail vendorDetail) {
        final String addressPattern = "{0}, {1}, {2} {3}";

        final VendorAddress vendorAddress = vendorDetail.getVendorAddresses().get(0);
        final String address = MessageFormat.format(addressPattern, vendorAddress.getVendorLine1Address(),
                vendorAddress.getVendorCityName(), vendorAddress.getVendorStateCode(),
                vendorAddress.getVendorCountryCode());

        final ACHPayee achPayee = new ACHPayee();
        achPayee.setActive(vendorDetail.isActiveIndicator());
        achPayee.setPayeeIdNumber(vendorDetail.getVendorNumber());
        achPayee.setPayeeTypeCode(PdpConstants.PayeeIdTypeCodes.VENDOR_ID);
        achPayee.setPayeeName(vendorDetail.getAltVendorName());
        achPayee.setTaxNumber(vendorDetail.getVendorHeader().getVendorTaxNumber());
        achPayee.setAddress(address);

        return achPayee;
    }

    // Scope loosened for CSU
    protected List<DisbursementPayee> getPersonAsPayees(final Map<String, String> fieldValues) {
        final List<DisbursementPayee> payeeList = new ArrayList<>();

        final Map<String, String> fieldsForLookup = getPersonFieldValues(fieldValues);
        final List<Person> persons = personService.findPeople(fieldsForLookup);

        for (final Person personDetail : persons) {
            final DisbursementPayee payee = getPayeeFromPerson(personDetail, fieldValues);
            payeeList.add(payee);
        }

        return payeeList;
    }

    //CU customization change from private to protected
    protected Map<String, String> getPersonFieldValues(final Map<String, String> fieldValues) {
        final Map<String, String> personFieldValues = new HashMap<>();
        personFieldValues.put(
                KIMPropertyConstants.Person.FIRST_NAME,
                fieldValues.get(KIMPropertyConstants.Person.FIRST_NAME)
        );
        personFieldValues.put(
                KIMPropertyConstants.Person.LAST_NAME,
                fieldValues.get(KIMPropertyConstants.Person.LAST_NAME)
        );
        personFieldValues.put(
                KIMPropertyConstants.Person.EMPLOYEE_ID,
                fieldValues.get(KIMPropertyConstants.Person.EMPLOYEE_ID)
        );
        personFieldValues.put(KFSPropertyConstants.ACTIVE, fieldValues.get(KFSPropertyConstants.ACTIVE));
        final Map<String, String> fieldConversionMap =
                disbursementVoucherPayeeService.getFieldConversionBetweenPayeeAndPerson();
        replaceFieldKeys(personFieldValues, fieldConversionMap);

        personFieldValues.put(KIMPropertyConstants.Person.EMPLOYEE_STATUS_CODE, KFSConstants.EMPLOYEE_ACTIVE_STATUS);

        return personFieldValues;
    }

    private Map<String, String> getVendorFieldValues(final Map<String, String> fieldValues) {
        final Map<String, String> vendorFieldValues = new HashMap<>();
        vendorFieldValues.put(KFSPropertyConstants.TAX_NUMBER, fieldValues.get(KFSPropertyConstants.TAX_NUMBER));
        vendorFieldValues.put(KFSPropertyConstants.VENDOR_NAME, fieldValues.get(KFSPropertyConstants.VENDOR_NAME));
        vendorFieldValues.put(KFSPropertyConstants.VENDOR_NUMBER, fieldValues.get(KFSPropertyConstants.VENDOR_NUMBER));
        vendorFieldValues.put(
                KIMPropertyConstants.Person.FIRST_NAME,
                fieldValues.get(KIMPropertyConstants.Person.FIRST_NAME)
        );
        vendorFieldValues.put(
                KIMPropertyConstants.Person.LAST_NAME,
                fieldValues.get(KIMPropertyConstants.Person.LAST_NAME)
        );
        vendorFieldValues.put(KFSPropertyConstants.ACTIVE, fieldValues.get(KFSPropertyConstants.ACTIVE));

        final Map<String, String> fieldConversionMap =
                disbursementVoucherPayeeService.getFieldConversionBetweenPayeeAndVendor();
        replaceFieldKeys(vendorFieldValues, fieldConversionMap);

        final String vendorName = getVendorName(vendorFieldValues);
        if (StringUtils.isNotBlank(vendorName)) {
            vendorFieldValues.put(KFSPropertyConstants.VENDOR_NAME, vendorName);
        }

        vendorFieldValues.remove(VendorPropertyConstants.VENDOR_FIRST_NAME);
        vendorFieldValues.remove(VendorPropertyConstants.VENDOR_LAST_NAME);

        return vendorFieldValues.entrySet().stream()
                .filter(vendorField -> StringUtils.isNotBlank(vendorField.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static String getVendorName(final Map<String, String> vendorFieldValues) {
        String firstName = vendorFieldValues.get(VendorPropertyConstants.VENDOR_FIRST_NAME);
        final String lastName = vendorFieldValues.get(VendorPropertyConstants.VENDOR_LAST_NAME);

        if (StringUtils.isNotBlank(lastName)) {
            if (StringUtils.isBlank(firstName)) {
                firstName = KFSConstants.WILDCARD_CHARACTER;
            }
            return lastName + VendorConstants.NAME_DELIM + firstName;
        } else if (StringUtils.isNotBlank(firstName)) {
            return KFSConstants.WILDCARD_CHARACTER + VendorConstants.NAME_DELIM + firstName;
        }

        return StringUtils.EMPTY;
    }

    private static void replaceFieldKeys(
            final Map<? super String, String> fieldValues, final Map<String, String> fieldConversionMap
    ) {
        for (final Map.Entry<String, String> entry : fieldConversionMap.entrySet()) {
            final String key = entry.getKey();
            if (fieldValues.containsKey(key)) {
                final String value = fieldValues.get(key);
                final String newKey = entry.getValue();
                fieldValues.remove(key);
                fieldValues.put(newKey, value);
            }
        }
    }
}
