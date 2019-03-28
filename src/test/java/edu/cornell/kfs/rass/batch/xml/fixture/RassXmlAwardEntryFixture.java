package edu.cornell.kfs.rass.batch.xml.fixture;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.rass.batch.xml.RassXmlAwardEntry;
import edu.cornell.kfs.sys.fixture.XmlDocumentFixtureUtils;
import edu.cornell.kfs.sys.xmladapters.RassStringToJavaShortDateTimeAdapter;

public enum RassXmlAwardEntryFixture {
    FIRST("141414", "OS", "2345", "First Example Project", "2017-12-31", null, 5300000.000, 700000.000, 6000000.000,
            StringUtils.EMPTY, StringUtils.EMPTY, null, StringUtils.EMPTY, StringUtils.EMPTY, "3434", Boolean.TRUE, null,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.cah292_PRIMARY)),
    ANOTHER("141415", "RS", "24680", "Another Example", null, "2050-12-31", 0.0, 0.0, 0.0,
            StringUtils.EMPTY, "GRT", null, StringUtils.EMPTY, StringUtils.EMPTY, "2374", Boolean.TRUE, "2022-09-30",
            piFixtures(RassXMLAwardPiCoPiEntryFixture.NO_NAME_PRIMARY)),
    NULL_AMOUNTS("35656", "RS", "24680", "Award with empty totals", null, "2050-12-31", 0.0, null, null,
            StringUtils.EMPTY, "GRT", null, StringUtils.EMPTY, StringUtils.EMPTY, "2374", Boolean.TRUE, "2022-09-30",
            piFixtures(RassXMLAwardPiCoPiEntryFixture.jdh34_CO_PI));
    public final String proposalNumber;
    public final String status;
    public final String agencyNumber;
    public final String projectTitle;
    public final DateTime startDate;
    public final DateTime stopDate;
    public final KualiDecimal directCostAmount;
    public final KualiDecimal indirectCostAmount;
    public final KualiDecimal totalAmount;
    public final String grantNumber;
    public final String grantDescription;
    public final Boolean federalPassThrough;
    public final String federalPassThroughAgencyNumber;
    public final String cfdaNumber;
    public final String organizationCode;
    public final Boolean costShareRequired;
    public final DateTime finalReportDueDate;
    public final List<RassXMLAwardPiCoPiEntryFixture> piFixtures;
    
    private RassXmlAwardEntryFixture(String proposalNumber, String status, String agencyNumber, String projectTitle,
            String startDateString, String stopDateString, Double directCostAmount, Double indirectCostAmount,
            Double totalAmount, String grantNumber, String grantDescription, Boolean federalPassThrough,
            String federalPassThroughAgencyNumber, String cfdaNumber, String organizationCode,
            Boolean costShareRequired, String finalReportDueDateString, RassXMLAwardPiCoPiEntryFixture[] piFixtures) {
        this.proposalNumber = proposalNumber;
        this.status = status;
        this.agencyNumber = agencyNumber;
        this.projectTitle = projectTitle;
        this.startDate = parseShortDate(startDateString);
        this.stopDate = parseShortDate(stopDateString);
        this.directCostAmount = buildKualiDecimalFromDouble(directCostAmount);
        this.indirectCostAmount = buildKualiDecimalFromDouble(indirectCostAmount);
        this.totalAmount = buildKualiDecimalFromDouble(totalAmount);
        this.grantNumber = grantNumber;
        this.grantDescription = grantDescription;
        this.federalPassThrough = federalPassThrough;
        this.federalPassThroughAgencyNumber = federalPassThroughAgencyNumber;
        this.cfdaNumber = cfdaNumber;
        this.organizationCode = organizationCode;
        this.costShareRequired = costShareRequired;
        this.finalReportDueDate = parseShortDate(finalReportDueDateString);
        this.piFixtures = XmlDocumentFixtureUtils.toImmutableList(piFixtures);
    }
    
    private KualiDecimal buildKualiDecimalFromDouble(Double amount) {
        if (amount != null) {
            return new KualiDecimal(amount);
        } else {
            return null;
        }
    }
    
    private static RassXMLAwardPiCoPiEntryFixture[] piFixtures(RassXMLAwardPiCoPiEntryFixture... fixtures) {
        return fixtures;
    }
    
    private DateTime parseShortDate(String dateString) {
        if (StringUtils.isNotBlank(dateString)) {
            return new DateTime(RassStringToJavaShortDateTimeAdapter.parseToDateTime(dateString).toDate());
        } else {
            return null;
        }
        
    }
    
    public RassXmlAwardEntry toRassXmlAwardEntry() {
        RassXmlAwardEntry award = new RassXmlAwardEntry();
        award.setProposalNumber(proposalNumber);
        award.setStatus(status);
        award.setAgencyNumber(agencyNumber);
        award.setProjectTitle(projectTitle);
        award.setStartDate(buildDateFromDateTime(startDate));
        award.setStopDate(buildDateFromDateTime(stopDate));
        award.setDirectCostAmount(directCostAmount);
        award.setIndirectCostAmount(indirectCostAmount);
        award.setTotalAmount(totalAmount);
        award.setGrantNumber(grantNumber);
        award.setGrantDescription(grantDescription);
        award.setFederalPassThrough(federalPassThrough);
        award.setFederalPassThroughAgencyNumber(federalPassThroughAgencyNumber);
        award.setCfdaNumber(cfdaNumber);
        award.setOrganizationCode(organizationCode);
        award.setCostShareRequired(costShareRequired);
        award.setFinalReportDueDate(buildDateFromDateTime(finalReportDueDate));
        for (RassXMLAwardPiCoPiEntryFixture fixture : piFixtures) {
            award.getPrincipalAndCoPrincipalInvestigators().add(fixture.toRassXMLAwardPiCoPiEntry());
        }
        return award;
    }
    
    private Date buildDateFromDateTime(DateTime dateTime) {
        if (dateTime != null) {
            return dateTime.toDate();
        } else {
            return null;
        }
    }
}
