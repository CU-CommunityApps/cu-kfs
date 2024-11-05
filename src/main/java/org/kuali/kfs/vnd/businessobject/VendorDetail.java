/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.vnd.businessobject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.LookupService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.businessobject.PaymentMethod;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.document.service.VendorService;

import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains all data for a specific parent or division Vendor, including a link to the {@link VendorHeader}, which only
 * contains information about the parent company, but can be shared between division Vendors.
 *
 * @see org.kuali.kfs.vnd.businessobject.VendorHeader
 */
public class VendorDetail extends PersistableBusinessObjectBase implements VendorRoutingComparable {

    private static final Logger LOG = LogManager.getLogger();

    private Integer vendorHeaderGeneratedIdentifier;
    private Integer vendorDetailAssignedIdentifier;
    // not persisted in the db
    private String vendorNumber;
    private boolean vendorParentIndicator;
    private String vendorName;
    // not persisted in the db
    private String vendorFirstName;
    // not persisted in the db
    private String vendorLastName;
    // not persisted in the db
    private String vendorStateForLookup;

    private boolean activeIndicator;
    private String vendorInactiveReasonCode;
    private String vendorDunsNumber;
    private String vendorPaymentTermsCode;
    private String vendorShippingTitleCode;
    private String vendorShippingPaymentTermsCode;
    private Boolean vendorConfirmationIndicator;
    private Boolean vendorPrepaymentIndicator;
    private Boolean vendorCreditCardIndicator;
    private KualiDecimal vendorMinimumOrderAmount;
    private String vendorUrlAddress;
    private String vendorRemitName;
    private Boolean vendorRestrictedIndicator;
    private String vendorRestrictedReasonText;
    private Date vendorRestrictedDate;
    private String vendorRestrictedPersonIdentifier;
    // not persisted in the db
    private String vendorSoldToNumber;
    private Integer vendorSoldToGeneratedIdentifier;
    private Integer vendorSoldToAssignedIdentifier;
    private String vendorSoldToName;
    private boolean vendorFirstLastNameIndicator;
    private boolean taxableIndicator;
    private String defaultPaymentMethodCode;

    private List<VendorAddress> vendorAddresses;
    private List<VendorAlias> vendorAliases;
    private List<VendorContact> vendorContacts;
    private List<VendorContract> vendorContracts;
    private List<VendorCustomerNumber> vendorCustomerNumbers;
    private List<VendorPhoneNumber> vendorPhoneNumbers;
    private List<VendorShippingSpecialCondition> vendorShippingSpecialConditions;
    private List<VendorCommodityCode> vendorCommodities;

    private VendorHeader vendorHeader;
    private VendorInactiveReason vendorInactiveReason;
    private PaymentTermType vendorPaymentTerms;
    private ShippingTitle vendorShippingTitle;
    private ShippingPaymentTerms vendorShippingPaymentTerms;
    private VendorDetail soldToVendorDetail;
    private Person vendorRestrictedPerson;
    private PaymentMethod defaultPaymentMethod;

    // these nine are not persisted in the db
    private String vendorParentName;
    private String defaultAddressLine1;
    private String defaultAddressLine2;
    private String defaultAddressCity;
    private String defaultAddressStateCode;
    private String defaultAddressInternationalProvince;
    private String defaultAddressPostalCode;
    private String defaultAddressCountryCode;
    private String defaultFaxNumber;

    private List boNotes;

    public VendorDetail() {
        vendorAddresses = new ArrayList<>();
        vendorAliases = new ArrayList<>();
        vendorContacts = new ArrayList<>();
        vendorContracts = new ArrayList<>();
        vendorHeader = new VendorHeader();
        vendorCustomerNumbers = new ArrayList<>();
        vendorPhoneNumbers = new ArrayList<>();
        vendorShippingSpecialConditions = new ArrayList<>();
        vendorCommodities = new ArrayList<>();
        vendorParentIndicator = true;
    }

    public Integer getVendorHeaderGeneratedIdentifier() {
        return vendorHeaderGeneratedIdentifier;
    }

    public void setVendorHeaderGeneratedIdentifier(final Integer vendorHeaderGeneratedIdentifier) {
        this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
    }

    public Integer getVendorDetailAssignedIdentifier() {
        return vendorDetailAssignedIdentifier;
    }

    public void setVendorDetailAssignedIdentifier(final Integer vendorDetailAssignedIdentifier) {
        this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
    }

    /**
     * A concatenation of the vendorHeaderGeneratedIdentifier, a dash, and the vendorDetailAssignedIdentifier
     *
     * @return the vendorNumber.
     */
    public String getVendorNumber() {
        String headerId = "";
        String detailId = "";
        String vendorNumber = "";
        if (ObjectUtils.isNotNull(vendorHeaderGeneratedIdentifier)) {
            headerId = vendorHeaderGeneratedIdentifier.toString();
        }
        if (ObjectUtils.isNotNull(vendorDetailAssignedIdentifier)) {
            detailId = vendorDetailAssignedIdentifier.toString();
        }
        if (StringUtils.isNotEmpty(headerId) && StringUtils.isNotEmpty(detailId)) {
            vendorNumber = headerId + "-" + detailId;
        }

        return vendorNumber;
    }

    /**
     * Sets the vendorNumber attribute value.
     * If vendorNumber is empty, clears header and detail IDs.
     * If vendorNumber is invalid, leaves header and detail IDs as were.
     *
     * @param vendorNumber The vendorNumber to set.
     */
    public void setVendorNumber(final String vendorNumber) {
        this.vendorNumber = vendorNumber;

        if (StringUtils.isEmpty(vendorNumber)) {
            vendorHeaderGeneratedIdentifier = null;
            vendorDetailAssignedIdentifier = null;
            return;
        }

        final int dashInd = vendorNumber.indexOf('-');
        // make sure there's at least one char before and after '-'
        if (dashInd > 0 && dashInd < vendorNumber.length() - 1) {
            try {
                vendorHeaderGeneratedIdentifier = new Integer(vendorNumber.substring(0, dashInd));
                vendorDetailAssignedIdentifier = new Integer(vendorNumber.substring(dashInd + 1));
            } catch (final NumberFormatException e) {
                // in case of invalid number format
            }
        }
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(final String vendorName) {
        this.vendorName = vendorName;
    }

    public Integer getVendorSoldToGeneratedIdentifier() {
        return vendorSoldToGeneratedIdentifier;
    }

    public void setVendorSoldToGeneratedIdentifier(final Integer vendorSoldToGeneratedIdentifier) {
        this.vendorSoldToGeneratedIdentifier = vendorSoldToGeneratedIdentifier;
    }

    public Integer getVendorSoldToAssignedIdentifier() {
        return vendorSoldToAssignedIdentifier;
    }

    public void setVendorSoldToAssignedIdentifier(final Integer vendorSoldToAssignedIdentifier) {
        this.vendorSoldToAssignedIdentifier = vendorSoldToAssignedIdentifier;
    }

    public String getVendorSoldToNumber() {
        String headerId = "";
        String detailId = "";
        String vendorSoldToNumber = "";

        if (vendorSoldToGeneratedIdentifier != null) {
            headerId = vendorSoldToGeneratedIdentifier.toString();
        }
        if (vendorSoldToAssignedIdentifier != null) {
            detailId = vendorSoldToAssignedIdentifier.toString();
        }
        if (StringUtils.isNotEmpty(headerId) && StringUtils.isNotEmpty(detailId)) {
            vendorSoldToNumber = headerId + "-" + detailId;
        }

        return vendorSoldToNumber;
    }

    /**
     * Sets the vendorSoldToNumber attribute value.
     * If vendorSoldToNumber is empty, clears soldToVendor header and detail IDs.
     * If vendorSoldToNumber is invalid, leaves soldToVendor header and detail IDs as were.
     *
     * @param vendorSoldToNumber The vendorSoldToNumber to set.
     */
    public void setVendorSoldToNumber(final String vendorSoldToNumber) {
        this.vendorSoldToNumber = vendorSoldToNumber;

        if (StringUtils.isEmpty(vendorSoldToNumber)) {
            vendorSoldToGeneratedIdentifier = null;
            vendorSoldToAssignedIdentifier = null;
            return;
        }

        final int dashInd = vendorSoldToNumber.indexOf('-');
        // make sure there's at least one char before and after '-'
        if (dashInd > 0 && dashInd < vendorSoldToNumber.length() - 1) {
            try {
                vendorSoldToGeneratedIdentifier = new Integer(vendorSoldToNumber.substring(0, dashInd));
                vendorSoldToAssignedIdentifier = new Integer(vendorSoldToNumber.substring(dashInd + 1));
            } catch (final NumberFormatException e) {
                // in case of invalid number format
            }
        }
    }

    public String getVendorSoldToName() {
        return vendorSoldToName;
    }

    public void setVendorSoldToName(final String vendorSoldToName) {
        this.vendorSoldToName = vendorSoldToName;
    }

    public String getAltVendorName() {
        return vendorName;
    }

    public void setAltVendorName(final String altVendorName) {
        vendorName = altVendorName;
    }

    public String getVendorRemitName() {
        return vendorRemitName;
    }

    public void setVendorRemitName(final String vendorRemitName) {
        this.vendorRemitName = vendorRemitName;
    }

    public boolean isVendorParentIndicator() {
        return vendorParentIndicator;
    }

    public void setVendorParentIndicator(final boolean vendorParentIndicator) {
        this.vendorParentIndicator = vendorParentIndicator;
    }

    public boolean isTaxableIndicator() {
        return taxableIndicator;
    }

    public void setTaxableIndicator(final boolean taxableIndicator) {
        this.taxableIndicator = taxableIndicator;
    }

    public String getDefaultPaymentMethodCode() {
        return defaultPaymentMethodCode;
    }

    public void setDefaultPaymentMethodCode(final String defaultPaymentMethodCode) {
        this.defaultPaymentMethodCode = defaultPaymentMethodCode;
    }

    public boolean isVendorDebarred() {
        return ObjectUtils.isNotNull(getVendorHeader().getVendorDebarredIndicator())
                && getVendorHeader().getVendorDebarredIndicator();
    }

    public boolean isActiveIndicator() {
        return activeIndicator;
    }

    public void setActiveIndicator(final boolean activeIndicator) {
        this.activeIndicator = activeIndicator;
    }

    public String getVendorInactiveReasonCode() {
        return vendorInactiveReasonCode;
    }

    public void setVendorInactiveReasonCode(final String vendorInactiveReasonCode) {
        this.vendorInactiveReasonCode = vendorInactiveReasonCode;
    }

    public String getVendorPaymentTermsCode() {
        return vendorPaymentTermsCode;
    }

    public void setVendorPaymentTermsCode(final String vendorPaymentTermsCode) {
        this.vendorPaymentTermsCode = vendorPaymentTermsCode;
    }

    public String getVendorShippingTitleCode() {
        return vendorShippingTitleCode;
    }

    public void setVendorShippingTitleCode(final String vendorShippingTitleCode) {
        this.vendorShippingTitleCode = vendorShippingTitleCode;
    }

    public String getVendorShippingPaymentTermsCode() {
        return vendorShippingPaymentTermsCode;
    }

    public void setVendorShippingPaymentTermsCode(final String vendorShippingPaymentTermsCode) {
        this.vendorShippingPaymentTermsCode = vendorShippingPaymentTermsCode;
    }

    public Boolean getVendorConfirmationIndicator() {
        return vendorConfirmationIndicator;
    }

    public void setVendorConfirmationIndicator(final Boolean vendorConfirmationIndicator) {
        this.vendorConfirmationIndicator = vendorConfirmationIndicator;
    }

    public Boolean getVendorPrepaymentIndicator() {
        return vendorPrepaymentIndicator;
    }

    public void setVendorPrepaymentIndicator(final Boolean vendorPrepaymentIndicator) {
        this.vendorPrepaymentIndicator = vendorPrepaymentIndicator;
    }

    public Boolean getVendorCreditCardIndicator() {
        return vendorCreditCardIndicator;
    }

    public void setVendorCreditCardIndicator(final Boolean vendorCreditCardIndicator) {
        this.vendorCreditCardIndicator = vendorCreditCardIndicator;
    }

    public KualiDecimal getVendorMinimumOrderAmount() {
        return vendorMinimumOrderAmount;
    }

    public void setVendorMinimumOrderAmount(final KualiDecimal vendorMinimumOrderAmount) {
        this.vendorMinimumOrderAmount = vendorMinimumOrderAmount;
    }

    public String getVendorUrlAddress() {
        return vendorUrlAddress;
    }

    public void setVendorUrlAddress(final String vendorUrlAddress) {
        this.vendorUrlAddress = vendorUrlAddress;
    }

    public Boolean getVendorRestrictedIndicator() {
        return vendorRestrictedIndicator;
    }

    public void setVendorRestrictedIndicator(final Boolean vendorRestrictedIndicator) {
        this.vendorRestrictedIndicator = vendorRestrictedIndicator;
    }

    public String getVendorRestrictedReasonText() {
        return vendorRestrictedReasonText;
    }

    public void setVendorRestrictedReasonText(final String vendorRestrictedReasonText) {
        this.vendorRestrictedReasonText = vendorRestrictedReasonText;
    }

    public Date getVendorRestrictedDate() {
        return vendorRestrictedDate;
    }

    public void setVendorRestrictedDate(final Date vendorRestrictedDate) {
        this.vendorRestrictedDate = vendorRestrictedDate;
    }

    public String getVendorRestrictedPersonIdentifier() {
        return vendorRestrictedPersonIdentifier;
    }

    public void setVendorRestrictedPersonIdentifier(final String vendorRestrictedPersonIdentifier) {
        this.vendorRestrictedPersonIdentifier = vendorRestrictedPersonIdentifier;
    }

    public String getVendorDunsNumber() {
        return vendorDunsNumber;
    }

    public void setVendorDunsNumber(final String vendorDunsNumber) {
        this.vendorDunsNumber = vendorDunsNumber;
    }

    public VendorHeader getVendorHeader() {
        return vendorHeader;
    }

    public void setVendorHeader(final VendorHeader vendorHeader) {
        this.vendorHeader = vendorHeader;
    }

    public PaymentTermType getVendorPaymentTerms() {
        return vendorPaymentTerms;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setVendorPaymentTerms(final PaymentTermType vendorPaymentTerms) {
        this.vendorPaymentTerms = vendorPaymentTerms;
    }

    public ShippingTitle getVendorShippingTitle() {
        return vendorShippingTitle;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setVendorShippingTitle(final ShippingTitle vendorShippingTitle) {
        this.vendorShippingTitle = vendorShippingTitle;
    }

    public ShippingPaymentTerms getVendorShippingPaymentTerms() {
        return vendorShippingPaymentTerms;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setVendorShippingPaymentTerms(final ShippingPaymentTerms vendorShippingPaymentTerms) {
        this.vendorShippingPaymentTerms = vendorShippingPaymentTerms;
    }

    public VendorInactiveReason getVendorInactiveReason() {
        return vendorInactiveReason;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setVendorInactiveReason(final VendorInactiveReason vendorInactiveReason) {
        this.vendorInactiveReason = vendorInactiveReason;
    }

    public List<VendorAddress> getVendorAddresses() {
        return vendorAddresses;
    }

    public void setVendorAddresses(final List<VendorAddress> vendorAddresses) {
        this.vendorAddresses = vendorAddresses;
    }

    public List<VendorContact> getVendorContacts() {
        return vendorContacts;
    }

    public void setVendorContacts(final List<VendorContact> vendorContacts) {
        this.vendorContacts = vendorContacts;
    }

    public List<VendorContract> getVendorContracts() {
        return vendorContracts;
    }

    public void setVendorContracts(final List<VendorContract> vendorContracts) {
        this.vendorContracts = vendorContracts;
    }

    public List<VendorCustomerNumber> getVendorCustomerNumbers() {
        return vendorCustomerNumbers;
    }

    public void setVendorCustomerNumbers(final List<VendorCustomerNumber> vendorCustomerNumbers) {
        this.vendorCustomerNumbers = vendorCustomerNumbers;
    }

    public List<VendorShippingSpecialCondition> getVendorShippingSpecialConditions() {
        return vendorShippingSpecialConditions;
    }

    public void setVendorShippingSpecialConditions(final List<VendorShippingSpecialCondition> vendorShippingSpecialConditions) {
        this.vendorShippingSpecialConditions = vendorShippingSpecialConditions;
    }

    public List<VendorCommodityCode> getVendorCommodities() {
        return vendorCommodities;
    }

    public String getVendorCommoditiesAsString() {
        final StringBuilder sb = new StringBuilder("[");

        boolean first = true;
        for (final VendorCommodityCode vcc : getVendorCommodities()) {
            if (!first) {
                sb.append(", ");
            } else {
                first = false;
            }
            // CU customization KFSPTS-3487
            sb.append(vcc.getCommodityCode().getCommodityDescription());
        }
        sb.append(']');
        return sb.toString();
    }

    public void setVendorCommodities(final List<VendorCommodityCode> vendorCommodities) {
        this.vendorCommodities = vendorCommodities;
    }

    public List<VendorAlias> getVendorAliases() {
        return vendorAliases;
    }

    public void setVendorAliases(final List<VendorAlias> vendorAliases) {
        this.vendorAliases = vendorAliases;
    }

    public List<VendorPhoneNumber> getVendorPhoneNumbers() {
        return vendorPhoneNumbers;
    }

    public void setVendorPhoneNumbers(final List<VendorPhoneNumber> vendorPhoneNumbers) {
        this.vendorPhoneNumbers = vendorPhoneNumbers;
    }

    public String getVendorFirstName() {
        return vendorFirstName;
    }

    public void setVendorFirstName(final String vendorFirstName) {
        this.vendorFirstName = vendorFirstName;
    }

    public String getVendorLastName() {
        return vendorLastName;
    }

    public void setVendorLastName(final String vendorLastName) {
        this.vendorLastName = vendorLastName;
    }

    public VendorDetail getSoldToVendorDetail() {
        return soldToVendorDetail;
    }

    public void setSoldToVendorDetail(final VendorDetail soldToVendorDetail) {
        this.soldToVendorDetail = soldToVendorDetail;
    }

    public boolean isVendorFirstLastNameIndicator() {
        return vendorFirstLastNameIndicator;
    }

    public void setVendorFirstLastNameIndicator(final boolean vendorFirstLastNameIndicator) {
        this.vendorFirstLastNameIndicator = vendorFirstLastNameIndicator;
    }

    public String getVendorStateForLookup() {
        return vendorStateForLookup;
    }

    public void setVendorStateForLookup(final String vendorStateForLookup) {
        this.vendorStateForLookup = vendorStateForLookup;
    }

    public Person getVendorRestrictedPerson() {
        return vendorRestrictedPerson;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setVendorRestrictedPerson(final Person vendorRestrictedPerson) {
        this.vendorRestrictedPerson = vendorRestrictedPerson;
    }

    public PaymentMethod getDefaultPaymentMethod() {
        return defaultPaymentMethod;
    }

    public void setDefaultPaymentMethod(final PaymentMethod defaultPaymentMethod) {
        this.defaultPaymentMethod = defaultPaymentMethod;
    }

    public String getDefaultAddressLine1() {
        return defaultAddressLine1;
    }

    public void setDefaultAddressLine1(final String defaultAddressLine1) {
        this.defaultAddressLine1 = defaultAddressLine1;
    }

    public String getDefaultAddressCity() {
        return defaultAddressCity;
    }

    public void setDefaultAddressCity(final String defaultAddressCity) {
        this.defaultAddressCity = defaultAddressCity;
    }

    public String getDefaultAddressLine2() {
        return defaultAddressLine2;
    }

    public void setDefaultAddressLine2(final String defaultAddressLine2) {
        this.defaultAddressLine2 = defaultAddressLine2;
    }

    public String getDefaultAddressPostalCode() {
        return defaultAddressPostalCode;
    }

    public void setDefaultAddressPostalCode(final String defaultAddressPostalCode) {
        this.defaultAddressPostalCode = defaultAddressPostalCode;
    }

    public String getDefaultAddressStateCode() {
        return defaultAddressStateCode;
    }

    public void setDefaultAddressStateCode(final String defaultAddressStateCode) {
        this.defaultAddressStateCode = defaultAddressStateCode;
    }

    public String getDefaultAddressInternationalProvince() {
        return defaultAddressInternationalProvince;
    }

    public void setDefaultAddressInternationalProvince(final String defaultAddressInternationalProvince) {
        this.defaultAddressInternationalProvince = defaultAddressInternationalProvince;
    }

    public String getDefaultAddressCountryCode() {
        return defaultAddressCountryCode;
    }

    public void setDefaultAddressCountryCode(final String defaultAddressCountryCode) {
        this.defaultAddressCountryCode = defaultAddressCountryCode;
    }

    public String getDefaultFaxNumber() {
        return defaultFaxNumber;
    }

    public void setDefaultFaxNumber(final String defaultFaxNumber) {
        this.defaultFaxNumber = defaultFaxNumber;
    }

    @Override
    public boolean isEqualForRouting(final Object toCompare) {
        LOG.debug("Entering isEqualForRouting.");
        if (ObjectUtils.isNull(toCompare) || !(toCompare instanceof VendorDetail)) {
            return false;
        } else {
            final VendorDetail detail = (VendorDetail) toCompare;
            return new EqualsBuilder().append(
                    getVendorHeaderGeneratedIdentifier(), detail.getVendorHeaderGeneratedIdentifier()).append(
                    getVendorDetailAssignedIdentifier(), detail.getVendorDetailAssignedIdentifier()).append(
                    isVendorParentIndicator(), detail.isVendorParentIndicator()).append(
                    getVendorName(), detail.getVendorName()).append(
                    getVendorLastName(), detail.getVendorLastName()).append(
                    getVendorFirstName(), detail.getVendorFirstName()).append(
                    isActiveIndicator(), detail.isActiveIndicator()).append(
                    getVendorInactiveReasonCode(), detail.getVendorInactiveReasonCode()).append(
                    getVendorDunsNumber(), detail.getVendorDunsNumber()).append(
                    getVendorPaymentTermsCode(), detail.getVendorPaymentTermsCode()).append(
                    getVendorShippingTitleCode(), detail.getVendorShippingTitleCode()).append(
                    getVendorShippingPaymentTermsCode(), detail.getVendorShippingPaymentTermsCode()).append(
                    getVendorConfirmationIndicator(), detail.getVendorConfirmationIndicator()).append(
                    getVendorPrepaymentIndicator(), detail.getVendorPrepaymentIndicator()).append(
                    getVendorCreditCardIndicator(), detail.getVendorCreditCardIndicator()).append(
                    getVendorMinimumOrderAmount(), detail.getVendorMinimumOrderAmount()).append(
                    getVendorUrlAddress(), detail.getVendorUrlAddress()).append(
                    getVendorRemitName(), detail.getVendorRemitName()).append(
                    getVendorRestrictedIndicator(), detail.getVendorRestrictedIndicator()).append(
                    getVendorRestrictedReasonText(), detail.getVendorRestrictedReasonText()).append(
                    getVendorRestrictedDate(), detail.getVendorRestrictedDate()).append(
                    getVendorRestrictedPersonIdentifier(), detail.getVendorRestrictedPersonIdentifier()).append(
                    getVendorSoldToGeneratedIdentifier(), detail.getVendorSoldToGeneratedIdentifier()).append(
                    getVendorSoldToAssignedIdentifier(), detail.getVendorSoldToAssignedIdentifier()).append(
                    getVendorSoldToName(), detail.getVendorSoldToName()).append( // KFSUPGRADE-779
                    isTaxableIndicator(), detail.isTaxableIndicator()).append( 
                    ((VendorDetailExtension)getExtension()).isInsuranceRequiredIndicator(),((VendorDetailExtension)detail.getExtension()).isInsuranceRequiredIndicator()).append(
                    ((VendorDetailExtension)getExtension()).getInsuranceRequirementsCompleteIndicator(),((VendorDetailExtension)detail.getExtension()).getInsuranceRequirementsCompleteIndicator()).append(
                    ((VendorDetailExtension)getExtension()).getCornellAdditionalInsuredIndicator(),((VendorDetailExtension)detail.getExtension()).getCornellAdditionalInsuredIndicator()).append(
                    ((VendorDetailExtension)getExtension()).getGeneralLiabilityCoverageAmount(),((VendorDetailExtension)detail.getExtension()).getGeneralLiabilityCoverageAmount()).append(
                    ((VendorDetailExtension)getExtension()).getGeneralLiabilityExpiration(),((VendorDetailExtension)detail.getExtension()).getGeneralLiabilityExpiration()).append(
                    ((VendorDetailExtension)getExtension()).getAutomobileLiabilityCoverageAmount(),((VendorDetailExtension)detail.getExtension()).getAutomobileLiabilityCoverageAmount()).append(
                    ((VendorDetailExtension)getExtension()).getAutomobileLiabilityExpiration(),((VendorDetailExtension)detail.getExtension()).getAutomobileLiabilityExpiration()).append(
                    ((VendorDetailExtension)getExtension()).getWorkmansCompCoverageAmount(),((VendorDetailExtension)detail.getExtension()).getWorkmansCompCoverageAmount()).append(
                    ((VendorDetailExtension)getExtension()).getWorkmansCompExpiration(),((VendorDetailExtension)detail.getExtension()).getWorkmansCompExpiration()).append(
                    ((VendorDetailExtension)getExtension()).getExcessLiabilityUmbExpiration(),((VendorDetailExtension)detail.getExtension()).getExcessLiabilityUmbExpiration()).append(
                    ((VendorDetailExtension)getExtension()).getExcessLiabilityUmbrellaAmount(),((VendorDetailExtension)detail.getExtension()).getExcessLiabilityUmbrellaAmount()).append(
                    ((VendorDetailExtension)getExtension()).getHealthOffSiteCateringLicenseReq(),((VendorDetailExtension)detail.getExtension()).getHealthOffSiteCateringLicenseReq()).append(
                    ((VendorDetailExtension)getExtension()).getHealthOffSiteLicenseExpirationDate(),((VendorDetailExtension)detail.getExtension()).getHealthOffSiteLicenseExpirationDate()).append(
                    ((VendorDetailExtension)getExtension()).getInsuranceNotes(),((VendorDetailExtension)detail.getExtension()).getInsuranceNotes()).append(
                    ((VendorDetailExtension)getExtension()).getMerchantNotes(),((VendorDetailExtension)detail.getExtension()).getMerchantNotes()).append( // end KFSUPGRADE-779
                    getDefaultPaymentMethodCode(), detail.getDefaultPaymentMethodCode()).append( // end KFSUPGRADE-779
                    isVendorFirstLastNameIndicator(), detail.isVendorFirstLastNameIndicator()).isEquals();
        }
    }

    /**
     * The vendor is B2B if they have a contract that has the B2B indicator set to Yes. This method returns true if this
     * vendor is a B2B vendor and false otherwise.
     *
     * @return true if this vendor is a B2B vendor and false otherwise.
     */
    public boolean isB2BVendor() {
        for (final VendorContract contract : getVendorContracts()) {
            if (contract.getVendorB2bIndicator()) {
                return true;
            }
        }
        return false;
    }

    @JsonIgnore
    public VendorDetail getVendorParent() {
        final Map<String, String> tmpValues = new HashMap<>();
        tmpValues.put(VendorPropertyConstants.VENDOR_HEADER_GENERATED_ID,
                getVendorHeaderGeneratedIdentifier().toString());
        tmpValues.put(VendorPropertyConstants.VENDOR_PARENT_INDICATOR, "Y");
        return SpringContext.getBean(LookupService.class).findObjectBySearch(VendorDetail.class, tmpValues);
    }

    public String getVendorParentName() {
        if (StringUtils.isNotBlank(vendorParentName)) {
            return vendorParentName;
        } else if (isVendorParentIndicator()) {
            setVendorParentName(getVendorName());
            return vendorParentName;
        } else {
            setVendorParentName(getVendorParent().getVendorName());
            return vendorParentName;
        }
    }

    public void setVendorParentName(final String vendorParentName) {
        this.vendorParentName = vendorParentName;
    }

    public String getVendorAliasesAsString() {
        final StringBuilder sb = new StringBuilder("[");

        boolean first = true;
        for (final VendorAlias vsd : getVendorAliases()) {
            if (vsd.isActive()) {
                if (!first) {
                    sb.append(", ");
                } else {
                    first = false;
                }
                sb.append(vsd.getVendorAliasName());
            }
        }
        sb.append(']');
        return sb.toString();
    }

    public List<Note> getBoNotes() {
        final VendorService vendorService = SpringContext.getBean(VendorService.class);
        return vendorService.getVendorNotes(this);
    }

    public void setBoNotes(final List boNotes) {
        this.boNotes = boNotes;
    }

}
