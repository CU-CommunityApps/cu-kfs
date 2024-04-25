package edu.cornell.kfs.kim.impl.identity;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;

import edu.cornell.kfs.kim.util.CuKimUtils;

public class PersonExtension extends PersistableBusinessObjectBase
        implements PersistableBusinessObjectExtension {

    private static final long serialVersionUID = 1L;

    private String principalId;
    private String altAddressTypeCode;
    private String altAddressLine1;
    private String altAddressLine2;
    private String altAddressLine3;
    private String altAddressCity;
    private String altAddressStateProvinceCode;
    private String altAddressPostalCode;
    private String altAddressCountryCode;
    private boolean suppressName;
    private boolean suppressEmail;
    private boolean suppressPhone;
    private boolean suppressPersonal;

    private List<PersonAffiliation> affiliations;

    public PersonExtension() {
        affiliations = new ArrayList<>();
    }

    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    public String getAltAddressTypeCode() {
        return altAddressTypeCode;
    }

    public void setAltAddressTypeCode(String altAddressTypeCode) {
        this.altAddressTypeCode = altAddressTypeCode;
    }

    public String getAltAddressLine1() {
        return altAddressLine1;
    }

    public String getAltAddressLine1MaskedIfNecessary() {
        return canViewAltAddress() ? altAddressLine1 : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    private boolean canViewAltAddress() {
        return StringUtils.equals(altAddressTypeCode, KimConstants.AddressTypes.WORK)
                || CuKimUtils.canSystemCallOrCurrentUserOverridePrivacyPreferencesForUser(principalId);
    }

    public void setAltAddressLine1(String altAddressLine1) {
        this.altAddressLine1 = altAddressLine1;
    }

    public String getAltAddressLine2() {
        return altAddressLine2;
    }

    public String getAltAddressLine2MaskedIfNecessary() {
        return canViewAltAddress() ? altAddressLine2 : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public void setAltAddressLine2(String altAddressLine2) {
        this.altAddressLine2 = altAddressLine2;
    }

    public String getAltAddressLine3() {
        return altAddressLine3;
    }

    public String getAltAddressLine3MaskedIfNecessary() {
        return canViewAltAddress() ? altAddressLine3 : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public void setAltAddressLine3(String altAddressLine3) {
        this.altAddressLine3 = altAddressLine3;
    }

    public String getAltAddressCity() {
        return altAddressCity;
    }

    public String getAltAddressCityMaskedIfNecessary() {
        return canViewAltAddress() ? altAddressCity : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public void setAltAddressCity(String altAddressCity) {
        this.altAddressCity = altAddressCity;
    }

    public String getAltAddressStateProvinceCode() {
        return altAddressStateProvinceCode;
    }

    public String getAltAddressStateProvinceCodeMaskedIfNecessary() {
        return canViewAltAddress() ? altAddressStateProvinceCode : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public void setAltAddressStateProvinceCode(String altAddressStateProvinceCode) {
        this.altAddressStateProvinceCode = altAddressStateProvinceCode;
    }

    public String getAltAddressPostalCode() {
        return altAddressPostalCode;
    }

    public String getAltAddressPostalCodeMaskedIfNecessary() {
        return canViewAltAddress() ? altAddressPostalCode : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public void setAltAddressPostalCode(String altAddressPostalCode) {
        this.altAddressPostalCode = altAddressPostalCode;
    }

    public String getAltAddressCountryCode() {
        return altAddressCountryCode;
    }

    public String getAltAddressCountryCodeMaskedIfNecessary() {
        return canViewAltAddress() ? altAddressCountryCode : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public void setAltAddressCountryCode(String altAddressCountryCode) {
        this.altAddressCountryCode = altAddressCountryCode;
    }

    public boolean isSuppressName() {
        return suppressName;
    }

    public void setSuppressName(boolean suppressName) {
        this.suppressName = suppressName;
    }

    public boolean isSuppressEmail() {
        return suppressEmail;
    }

    public void setSuppressEmail(boolean suppressEmail) {
        this.suppressEmail = suppressEmail;
    }

    public boolean isSuppressPhone() {
        return suppressPhone;
    }

    public void setSuppressPhone(boolean suppressPhone) {
        this.suppressPhone = suppressPhone;
    }

    public boolean isSuppressPersonal() {
        return suppressPersonal;
    }

    public void setSuppressPersonal(boolean suppressPersonal) {
        this.suppressPersonal = suppressPersonal;
    }

    public List<PersonAffiliation> getAffiliations() {
        return affiliations;
    }

    public void setAffiliations(List<PersonAffiliation> affiliations) {
        this.affiliations = affiliations;
    }

}
