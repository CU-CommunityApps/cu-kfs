package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorContact;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants;
import edu.cornell.kfs.cemi.vnd.CemiVendorConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactHeaderBo;

public class CemiEntityContactHeaderBoFactory {

    private List<VendorContact> mergedContacts;
    private Map<String, String> tenantedContactTypeMappings;
    private String supplierId;
    private int contactIndex;

    public CemiEntityContactHeaderBoFactory(final List<VendorContact> mergedContacts,
            final Map<String, String> tenantedContactTypeMappings, final String supplierId, final int contactIndex) {
        Validate.isTrue(CollectionUtils.isNotEmpty(mergedContacts), "mergedContacts cannot be null or empty");
        Validate.isTrue(MapUtils.isNotEmpty(tenantedContactTypeMappings),
                "tenantedContactTypeMappings cannot be null or empty");
        Validate.notBlank(supplierId, "supplierId cannot be blank");
        Validate.isTrue(contactIndex > 0, "contactIndex must be a positive integer");
        this.mergedContacts = mergedContacts;
        this.tenantedContactTypeMappings = tenantedContactTypeMappings;
        this.supplierId = supplierId;
        this.contactIndex = contactIndex;
    }

    public static CemiEntityContactHeaderBo createHeaderBoFrom(final List<VendorContact> mergedContacts,
            final Map<String, String> tenantedContactTypeMappings, final String supplierId, final int contactIndex) {
        final CemiEntityContactHeaderBoFactory factory = new CemiEntityContactHeaderBoFactory(
                mergedContacts, tenantedContactTypeMappings, supplierId, contactIndex);
        return factory.createCemiEntityContactHeaderBo();
    }

    public CemiEntityContactHeaderBo createCemiEntityContactHeaderBo() {
        final String spreadsheetKey = supplierId;
        final String nameRowId = Integer.toString(contactIndex);
        final VendorContact firstVendorContact = mergedContacts.get(0);
        final List<String> nameSegments = determineNameSegments(firstVendorContact.getVendorContactName());
        final List<String> tenantedContactTypes = determineTenantedContactTypes();

        final CemiEntityContactHeaderBo headerBo = new CemiEntityContactHeaderBo();

        headerBo.setMergedContacts(mergedContacts);
        headerBo.setMergedTenantedContactTypes(tenantedContactTypes);

        headerBo.setSpreadsheetKey(spreadsheetKey);
        headerBo.setAddOnly(CemiBaseConstants.EMPTY_STRING);
        headerBo.setExistingBusinessEntityContactId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setPrimaryBillToContact(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDefaultBillToContact(CemiBaseConstants.EMPTY_STRING);
        headerBo.setNewBusinessEntityContactId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setSupplier(supplierId);
        headerBo.setBillableEntity(CemiBaseConstants.EMPTY_STRING);
        headerBo.setFinancialInstitution(CemiBaseConstants.EMPTY_STRING);
        headerBo.setTaxAuthority(CemiBaseConstants.EMPTY_STRING);
        headerBo.setNameRowId(nameRowId);
        headerBo.setFormattedName(CemiBaseConstants.EMPTY_STRING);
        headerBo.setReportingName(CemiBaseConstants.EMPTY_STRING);
        headerBo.setCountry(CemiVendorConstants.COUNTRY_CODE_UNITED_STATES);
        headerBo.setTitle(CemiBaseConstants.EMPTY_STRING);
        headerBo.setTitleDescriptor(CemiBaseConstants.EMPTY_STRING);
        headerBo.setSalutation(CemiBaseConstants.EMPTY_STRING);
        headerBo.setFirstName(nameSegments.get(0));
        headerBo.setMiddleName(CemiBaseConstants.EMPTY_STRING);
        headerBo.setLastName(nameSegments.get(1));
        headerBo.setSecondaryLastName(CemiBaseConstants.EMPTY_STRING);
        headerBo.setTertiaryLastName(CemiBaseConstants.EMPTY_STRING);
        headerBo.setLocalName(CemiBaseConstants.EMPTY_STRING);
        headerBo.setLocalScript(CemiBaseConstants.EMPTY_STRING);
        headerBo.setLocalFirstName(CemiBaseConstants.EMPTY_STRING);
        headerBo.setLocalMiddleName(CemiBaseConstants.EMPTY_STRING);
        headerBo.setLocalLastName(CemiBaseConstants.EMPTY_STRING);
        headerBo.setLocalSecondaryLastName(CemiBaseConstants.EMPTY_STRING);
        headerBo.setLocalFirstName2(CemiBaseConstants.EMPTY_STRING);
        headerBo.setLocalMiddleName2(CemiBaseConstants.EMPTY_STRING);
        headerBo.setLocalLastName2(CemiBaseConstants.EMPTY_STRING);
        headerBo.setLocalSecondaryLastName2(CemiBaseConstants.EMPTY_STRING);
        headerBo.setSocialSuffix(CemiBaseConstants.EMPTY_STRING);
        headerBo.setSocialSuffixDescriptor(CemiBaseConstants.EMPTY_STRING);
        headerBo.setAcademicSuffix(CemiBaseConstants.EMPTY_STRING);
        headerBo.setHereditarySuffix(CemiBaseConstants.EMPTY_STRING);
        headerBo.setHonorarySuffix(CemiBaseConstants.EMPTY_STRING);
        headerBo.setProfessionalSuffix(CemiBaseConstants.EMPTY_STRING);
        headerBo.setReligiousSuffix(CemiBaseConstants.EMPTY_STRING);
        headerBo.setRoyalSuffix(CemiBaseConstants.EMPTY_STRING);
        headerBo.setFullNameForSingaporeAndMalaysia(CemiBaseConstants.EMPTY_STRING);

        return headerBo;
    }

    /*
     * TODO: In future extracts, we may need more in-depth name-splitting logic to handle special cases
     *       (middle names, multiple concatenated names, company names, etc.)
     */
    private List<String> determineNameSegments(final String vendorContactName) {
        if (Strings.CI.contains(vendorContactName, KFSConstants.BLANK_SPACE)) {
            return List.of(
                    StringUtils.substringBefore(vendorContactName, KFSConstants.BLANK_SPACE),
                    StringUtils.substringAfter(vendorContactName, KFSConstants.BLANK_SPACE)
            );
        } else {
            return List.of(StringUtils.defaultString(vendorContactName), CemiBaseConstants.EMPTY_STRING);
        }
    }

    private List<String> determineTenantedContactTypes() {
        final String[] tenantedTypes = mergedContacts.stream()
                .map(VendorContact::getVendorContactTypeCode)
                .filter(StringUtils::isNotBlank)
                .map(tenantedContactTypeMappings::get)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toArray(String[]::new);

        return CemiUtils.createListPaddedToMinimumSizeIfNecessary(
                CemiEntityContactConstants.MAX_TENANTED_TYPES, tenantedTypes);
    }

}
