package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorContact;

import edu.cornell.kfs.cemi.vnd.CemiVendorConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactHeaderBo;

public class CemiEntityContactHeaderBoFactory {

    private VendorContact vendorContact;
    private String supplierId;
    private int contactIndex;

    public CemiEntityContactHeaderBoFactory(final VendorContact vendorContact, final String supplierId,
            final int contactIndex) {
        this.vendorContact = vendorContact;
        this.supplierId = supplierId;
        this.contactIndex = contactIndex;
    }

    public static CemiEntityContactHeaderBo createHeaderBoFrom(final VendorContact vendorContact,
            final String supplierId, final int contactIndex) {
        final CemiEntityContactHeaderBoFactory factory = new CemiEntityContactHeaderBoFactory(
                vendorContact, supplierId, contactIndex);
        return factory.createCemiEntityContactHeaderBo();
    }

    public CemiEntityContactHeaderBo createCemiEntityContactHeaderBo() {
        Validate.validState(ObjectUtils.isNotNull(vendorContact), "Vendor Contact cannot be null");
        Validate.validState(StringUtils.isNotBlank(supplierId), "Supplier ID cannot be blank");
        Validate.validState(contactIndex > 0, "Contact Index must be a positive integer");

        final String spreadsheetKey = supplierId;
        final String nameRowId = Integer.toString(contactIndex);
        final List<String> nameSegments = determineNameSegments(vendorContact.getVendorContactName());

        final CemiEntityContactHeaderBo headerBo = new CemiEntityContactHeaderBo();

        headerBo.setSpreadsheetKey(spreadsheetKey);
        headerBo.setAddOnly(null);
        headerBo.setExistingBusinessEntityContactId(null);
        headerBo.setPrimaryBillToContact(null);
        headerBo.setDefaultBillToContact(null);
        headerBo.setNewBusinessEntityContactId(null);
        headerBo.setSupplier(supplierId);
        headerBo.setBillableEntity(null);
        headerBo.setFinancialInstitution(null);
        headerBo.setTaxAuthority(null);
        headerBo.setNameRowId(nameRowId);
        headerBo.setFormattedName(null);
        headerBo.setReportingName(null);
        headerBo.setCountry(CemiVendorConstants.COUNTRY_CODE_UNITED_STATES);
        headerBo.setTitle(null);
        headerBo.setTitleDescriptor(null);
        headerBo.setSalutation(null);
        headerBo.setFirstName(nameSegments.get(0));
        headerBo.setMiddleName(null);
        headerBo.setLastName(nameSegments.get(1));
        headerBo.setSecondaryLastName(null);
        headerBo.setTertiaryLastName(null);
        headerBo.setLocalName(null);
        headerBo.setLocalScript(null);
        headerBo.setLocalFirstName(null);
        headerBo.setLocalMiddleName(null);
        headerBo.setLocalLastName(null);
        headerBo.setLocalSecondaryLastName(null);
        headerBo.setLocalFirstName2(null);
        headerBo.setLocalMiddleName2(null);
        headerBo.setLocalLastName2(null);
        headerBo.setLocalSecondaryLastName2(null);
        headerBo.setSocialSuffix(null);
        headerBo.setSocialSuffixDescriptor(null);
        headerBo.setAcademicSuffix(null);
        headerBo.setHereditarySuffix(null);
        headerBo.setHonorarySuffix(null);
        headerBo.setProfessionalSuffix(null);
        headerBo.setReligiousSuffix(null);
        headerBo.setRoyalSuffix(null);
        headerBo.setFullNameForSingaporeAndMalaysia(null);

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
            return List.of(StringUtils.defaultString(vendorContactName), KFSConstants.EMPTY_STRING);
        }
    }

}
