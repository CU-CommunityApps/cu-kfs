package edu.cornell.kfs.module.purap.batch.service.impl.fixture;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractPartyType;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractPartyUploadRowType;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerLegalStructure;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractPartyUploadDto;
import edu.cornell.kfs.sys.CUKFSConstants;

public enum VendorFixture {
    BASIC_VENDOR("12345-0", "Acme Testing Company", CUKFSConstants.COUNTRY_CODE_US, "http://www.google.com"),
    BASIC_VENDOR_WITH_TAX_ID("98765-0", "Jane Doe Paint Services", CUKFSConstants.COUNTRY_CODE_US, "65479", "http://www.yahoo.com"),
    FULL_VENDOR(JaggaerContractPartyUploadRowType.PARTY, StringUtils.EMPTY, "active", "123456-0",
            CUPurapConstants.FALSE_STRING, "Acme Inc", "Acme", "other name", CUKFSConstants.COUNTRY_CODE_US, JaggaerContractPartyType.SUPPLIER,
            "primary", JaggaerLegalStructure.C_CORPORATION, "foo type", "369852", "vat number", "www.google.com"),
    FULL_VENDOR_FOR_CSV(JaggaerContractPartyUploadRowType.PARTY, "vendor SciQuest-id", "active", "66666-0",
            CUPurapConstants.FALSE_STRING, "Yankee Fan Company", "Yankees", "Babe Ruth's Team", CUKFSConstants.COUNTRY_CODE_US, JaggaerContractPartyType.SUPPLIER,
            "primary", JaggaerLegalStructure.NON_PROFIT, "test tax type", "353454", "vat number", "www.yankees.com");

    public final JaggaerContractPartyUploadRowType rowType;
    public final String sciQuestID;
    public final String active;
    public final String ERPNumber;
    public final String overrideDupError;
    public final String contractPartyName;
    public final String doingBusinessAs;
    public final String otherNames;
    public final String countryOfOrigin;
    public final JaggaerContractPartyType contractPartyType;
    public final String primary;
    public final JaggaerLegalStructure legalStructure;
    public final String taxIDType;
    public final String taxIdentificationNumber;
    public final String VATRegistrationNumber;
    public final String websiteURL;

    private VendorFixture(String eRPNumber, String contractPartyName, String countryOfOrigin, String websiteURL) {
        this(JaggaerContractPartyUploadRowType.PARTY, StringUtils.EMPTY, StringUtils.EMPTY, eRPNumber,
                StringUtils.EMPTY, contractPartyName, StringUtils.EMPTY, StringUtils.EMPTY, countryOfOrigin,
                JaggaerContractPartyType.SUPPLIER, StringUtils.EMPTY, JaggaerLegalStructure.C_CORPORATION,
                StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, websiteURL);
    }

    private VendorFixture(String eRPNumber, String contractPartyName, String countryOfOrigin,
            String taxIdentificationNumber, String websiteURL) {
        this(JaggaerContractPartyUploadRowType.PARTY, StringUtils.EMPTY, StringUtils.EMPTY, eRPNumber,
                StringUtils.EMPTY, contractPartyName, StringUtils.EMPTY, StringUtils.EMPTY, countryOfOrigin,
                JaggaerContractPartyType.SUPPLIER, StringUtils.EMPTY, JaggaerLegalStructure.C_CORPORATION,
                StringUtils.EMPTY, taxIdentificationNumber, StringUtils.EMPTY, websiteURL);
    }

    private VendorFixture(JaggaerContractPartyUploadRowType rowType, String sciQuestID, String active, String eRPNumber,
            String overrideDupError, String contractPartyName, String doingBusinessAs, String otherNames,
            String countryOfOrigin, JaggaerContractPartyType contractPartyType, String primary,
            JaggaerLegalStructure legalStructure, String taxIDType, String taxIdentificationNumber,
            String vATRegistrationNumber, String websiteURL) {
        this.rowType = rowType;
        this.sciQuestID = sciQuestID;
        this.active = active;
        this.ERPNumber = eRPNumber;
        this.overrideDupError = overrideDupError;
        this.contractPartyName = contractPartyName;
        this.doingBusinessAs = doingBusinessAs;
        this.otherNames = otherNames;
        this.countryOfOrigin = countryOfOrigin;
        this.contractPartyType = contractPartyType;
        this.primary = primary;
        this.legalStructure = legalStructure;
        this.taxIDType = taxIDType;
        this.taxIdentificationNumber = taxIdentificationNumber;
        VATRegistrationNumber = vATRegistrationNumber;
        this.websiteURL = websiteURL;
    }

    public JaggaerContractPartyUploadDto toJaggaerContractPartyUploadDto() {
        JaggaerContractPartyUploadDto dto = new JaggaerContractPartyUploadDto();
        dto.setRowType(rowType);
        dto.setSciQuestID(sciQuestID);
        dto.setActive(active);
        dto.setERPNumber(ERPNumber);
        dto.setOverrideDupError(overrideDupError);
        dto.setContractPartyName(contractPartyName);
        dto.setDoingBusinessAs(doingBusinessAs);
        dto.setOtherNames(otherNames);
        dto.setCountryOfOrigin(countryOfOrigin);
        dto.setContractPartyType(contractPartyType);
        dto.setPrimary(primary);
        dto.setLegalStructure(legalStructure);
        dto.setTaxIDType(taxIDType);
        dto.setTaxIdentificationNumber(taxIdentificationNumber);
        dto.setVATRegistrationNumber(VATRegistrationNumber);
        dto.setWebsiteURL(websiteURL);
        return dto;
    }

}