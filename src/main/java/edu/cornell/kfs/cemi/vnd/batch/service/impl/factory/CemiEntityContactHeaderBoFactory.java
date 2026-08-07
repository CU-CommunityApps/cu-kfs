package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorContact;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
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

}
