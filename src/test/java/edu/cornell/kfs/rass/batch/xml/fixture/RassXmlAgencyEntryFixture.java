package edu.cornell.kfs.rass.batch.xml.fixture;

import org.kuali.kfs.module.cg.businessobject.Agency;

import edu.cornell.kfs.module.cg.businessobject.AgencyExtendedAttribute;
import edu.cornell.kfs.rass.batch.xml.RassXmlAgencyEntry;

public enum RassXmlAgencyEntryFixture {
    SOME("468", "Some", "Some University", "U", "", "Some University", "USA"),
    DoS("579", "SM", "United States Department of Something ", "F", "", "United States Department of Something ", "USA"),
    TEST("101", "Test", "Test University", "U", null, null, "USA"),
    SOME_V2("468", "Some", "Some University", "U", "", "Some Other University", "USA", false);

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
        agency.setAgencyNumber(number);
        agency.setReportingName(reportingName);
        agency.setFullName(fullName);
        agency.setAgencyTypeCode(typeCode);
        agency.setReportsToAgencyNumber(reportsToAgencyNumber);
        agency.setActive(true);
        
        AgencyExtendedAttribute agencyExtension = new AgencyExtendedAttribute();
        agencyExtension.setAgencyNumber(number);
        agencyExtension.setAgencyCommonName(commonName);
        agencyExtension.setAgencyOriginCode(agencyOrigin);
        agency.setExtension(agencyExtension);
        
        return agency;
    }

}
