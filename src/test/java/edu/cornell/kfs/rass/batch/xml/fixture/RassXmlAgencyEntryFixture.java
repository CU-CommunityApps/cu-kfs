package edu.cornell.kfs.rass.batch.xml.fixture;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.cg.businessobject.Agency;

import edu.cornell.kfs.module.cg.businessobject.AgencyExtendedAttribute;
import edu.cornell.kfs.rass.RassTestConstants;
import edu.cornell.kfs.rass.batch.xml.RassXmlAgencyEntry;

public enum RassXmlAgencyEntryFixture {
    SOME("468", "Some", "Some University", "U", "", "Some University", "USA"),
    DoS("579", "SM", "United States Department of Something ", "F", "", "United States Department of Something ", "USA"),
    TEST("101", "Test", "Test University", "U", null, null, "USA"),
    SOME_V2("468", "Some", "Some University", "U", "", "Some Other University", "USA", false),
    LIMITED_LTD("555", "Limited", "Limited Ltd.", "C", "468", "Limited Ltd.", "USA", false),
    UNLIMITED_LTD("556", "Unlimited", "Unlimited Ltd.", "C", "555", "Unlimited Ltd.", "USA", false),
    FIJI_DOT("975", "Fiji_DoT", "Fiji Department of Treasury", "G", "", "Fiji Treasury Department", "Foreign", false),
    DoS_LONG_DESC("579", "SM", "United States Department of Something with Very...", "F", "",
            "Some United States Government Department with a...", "USA", false),
    SOME_V2_MISSING_REQ_FIELD("468", "Some", "", "U", "", "Some Other University", "USA", false),
    LONG_KEY(RassTestConstants.LONG_OBJECT_KEY, "LongKey", "Long Key", "C", "", "Long key to force doc description truncate", "USA", false),
    FORCE_ERROR(RassTestConstants.ERROR_OBJECT_KEY, "ForceError", "Force Error", "C", "", "Force an error!", "USA", false);

    public final String number;
    public final String reportingName;
    public final String fullName;
    public final String typeCode;
    public final String reportsToAgencyNumber;
    public final String commonName;
    public final String agencyOrigin;
    public final boolean existsByDefaultForSearching;
    
    private RassXmlAgencyEntryFixture(String number, String reportingName, String fullName, String typeCode, String reportsToAgencyNumber, String commonName, String agencyOrigin) {
        this(number, reportingName, fullName, typeCode, reportsToAgencyNumber, commonName, agencyOrigin, true);
    }
    
    private RassXmlAgencyEntryFixture(String number, String reportingName, String fullName, String typeCode, String reportsToAgencyNumber,
            String commonName, String agencyOrigin, boolean existsByDefaultForSearching) {
        this.number = number;
        this.reportingName = reportingName;
        this.fullName = fullName;
        this.typeCode = typeCode;
        this.reportsToAgencyNumber = reportsToAgencyNumber;
        this.commonName = commonName;
        this.agencyOrigin = agencyOrigin;
        this.existsByDefaultForSearching = existsByDefaultForSearching;
    }
    
    public RassXmlAgencyEntry toRassXmlAgencyEntry() {
        RassXmlAgencyEntry agency = new RassXmlAgencyEntry();
        agency.setNumber(number);
        agency.setReportingName(reportingName);
        agency.setFullName(fullName);
        agency.setTypeCode(typeCode);
        agency.setReportsToAgencyNumber(reportsToAgencyNumber);
        agency.setCommonName(commonName);
        agency.setAgencyOrigin(agencyOrigin);
        return agency;
    }

    public Agency toAgency() {
        Agency agency = new Agency();
        agency.setAgencyNumber(defaultToNullIfBlank(number));
        agency.setReportingName(defaultToNullIfBlank(reportingName));
        agency.setFullName(defaultToNullIfBlank(getTruncatedFullName()));
        agency.setAgencyTypeCode(defaultToNullIfBlank(typeCode));
        agency.setReportsToAgencyNumber(defaultToNullIfBlank(reportsToAgencyNumber));
        agency.setActive(true);
        
        AgencyExtendedAttribute agencyExtension = new AgencyExtendedAttribute();
        agencyExtension.setAgencyNumber(defaultToNullIfBlank(number));
        agencyExtension.setAgencyCommonName(defaultToNullIfBlank(getTruncatedCommonName()));
        agencyExtension.setAgencyOriginCode(defaultToNullIfBlank(agencyOrigin));
        agency.setExtension(agencyExtension);
        
        return agency;
    }

    public String getTruncatedFullName() {
        return StringUtils.left(fullName, RassTestConstants.DEFAULT_DD_FIELD_MAX_LENGTH);
    }

    public String getTruncatedCommonName() {
        return StringUtils.left(commonName, RassTestConstants.DEFAULT_DD_FIELD_MAX_LENGTH);
    }

    private String defaultToNullIfBlank(String value) {
        return StringUtils.defaultIfBlank(value, null);
    }

}
