package edu.cornell.kfs.rass.batch.xml.fixture;

import edu.cornell.kfs.rass.batch.xml.RassXmlAgencyEntry;

public enum RassXmlAgencyEntryFixture {
    SOME("468", "Some", "Some University", "U", "", "Some University", "USA"),
    DoS("579", "SM", "United States Department of Something ", "F", "", "United States Department of Something ", "USA"),
    TEST("101", "Test", "Test University", "U", null, null, "USA");
    
    public final String number;
    public final String reportingName;
    public final String fullName;
    public final String typeCode;
    public final String reportsToAgencyNumber;
    public final String commonName;
    public final String agencyOrigin;
    
    private RassXmlAgencyEntryFixture(String number, String reportingName, String fullName, String typeCode, String reportsToAgencyNumber, String commonName, String agencyOrigin) {
        this.number = number;
        this.reportingName = reportingName;
        this.fullName = fullName;
        this.typeCode = typeCode;
        this.reportsToAgencyNumber = reportsToAgencyNumber;
        this.commonName = commonName;
        this.agencyOrigin = agencyOrigin;
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

}
