package edu.cornell.kfs.rass.batch.xml.fixture;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.AwardOrganization;
import org.kuali.kfs.module.cg.businessobject.AwardProjectDirector;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.module.cg.businessobject.ProposalOrganization;
import org.kuali.kfs.module.cg.businessobject.ProposalProjectDirector;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;
import edu.cornell.kfs.rass.RassConstants;
import edu.cornell.kfs.rass.batch.xml.RassXmlAwardEntry;
import edu.cornell.kfs.sys.fixture.XmlDocumentFixtureUtils;
import edu.cornell.kfs.sys.xmladapters.RassStringToJavaShortDateTimeAdapter;

public enum RassXmlAwardEntryFixture {
    FIRST("141414", "OS", "2345", "First Example Project", "2017-12-31", null, 5300000.000, 700000.000, 6000000.000, StringUtils.EMPTY,
            StringUtils.EMPTY, StringUtils.EMPTY, null, StringUtils.EMPTY, StringUtils.EMPTY, "3434", Boolean.TRUE, null, false,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.cah292_PRIMARY)),
    ANOTHER("141415", "RS", "24680", "Another Example", null, "2050-12-31", 0.0, 0.0, 0.0, StringUtils.EMPTY,
            StringUtils.EMPTY, "GRT", null, StringUtils.EMPTY, StringUtils.EMPTY, "2374", Boolean.TRUE, "2022-09-30", false,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.NO_NAME_PRIMARY)),
    NULL_AMOUNTS("35656", "RS", "24680", "Award with empty totals", null, "2050-12-31", 0.0, null, null, StringUtils.EMPTY,
            StringUtils.EMPTY, "GRT", null, StringUtils.EMPTY, StringUtils.EMPTY, "2374", Boolean.TRUE, "2022-09-30", false,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.jdh34_CO_PI)),

    SAMPLE_PROJECT("556677", "OS", RassXmlAgencyEntryFixture.SOME, "Some University's Sample Project", "2019-01-15", "2019-12-24",
            25000.00, 5000.00, 30000.00, "H", "98765", "GRT", Boolean.FALSE, StringUtils.EMPTY, "53135",
            "1500", Boolean.TRUE, "2019-11-30", false,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY));

    public final String proposalNumber;
    public final String status;
    public final String agencyNumber;
    public final String projectTitle;
    public final DateTime startDate;
    public final DateTime stopDate;
    public final KualiDecimal directCostAmount;
    public final KualiDecimal indirectCostAmount;
    public final KualiDecimal totalAmount;
    public final String purpose;
    public final String grantNumber;
    public final String grantDescription;
    public final Boolean federalPassThrough;
    public final String federalPassThroughAgencyNumber;
    public final String cfdaNumber;
    public final String organizationCode;
    public final Boolean costShareRequired;
    public final DateTime finalReportDueDate;
    public final boolean existsByDefaultForSearching;
    public final List<RassXMLAwardPiCoPiEntryFixture> piFixtures;
    
    private RassXmlAwardEntryFixture(String proposalNumber, String status, RassXmlAgencyEntryFixture agency, String projectTitle,
            String startDateString, String stopDateString, Double directCostAmount, Double indirectCostAmount,
            Double totalAmount, String purpose, String grantNumber, String grantDescription, Boolean federalPassThrough,
            String federalPassThroughAgencyNumber, String cfdaNumber, String organizationCode,
            Boolean costShareRequired, String finalReportDueDateString,
            boolean existsByDefaultForSearching, RassXMLAwardPiCoPiEntryFixture[] piFixtures) {
        this(proposalNumber, status, agency.number, projectTitle, startDateString, stopDateString, directCostAmount, indirectCostAmount,
                totalAmount, purpose, grantNumber, grantDescription, federalPassThrough, federalPassThroughAgencyNumber, cfdaNumber,
                organizationCode, costShareRequired, finalReportDueDateString, existsByDefaultForSearching, piFixtures);
    }
    
    private RassXmlAwardEntryFixture(String proposalNumber, String status, String agencyNumber, String projectTitle,
            String startDateString, String stopDateString, Double directCostAmount, Double indirectCostAmount,
            Double totalAmount, String purpose, String grantNumber, String grantDescription, Boolean federalPassThrough,
            String federalPassThroughAgencyNumber, String cfdaNumber, String organizationCode,
            Boolean costShareRequired, String finalReportDueDateString,
            boolean existsByDefaultForSearching, RassXMLAwardPiCoPiEntryFixture[] piFixtures) {
        this.proposalNumber = proposalNumber;
        this.status = status;
        this.agencyNumber = agencyNumber;
        this.projectTitle = projectTitle;
        this.startDate = parseShortDate(startDateString);
        this.stopDate = parseShortDate(stopDateString);
        this.directCostAmount = buildKualiDecimalFromDouble(directCostAmount);
        this.indirectCostAmount = buildKualiDecimalFromDouble(indirectCostAmount);
        this.totalAmount = buildKualiDecimalFromDouble(totalAmount);
        this.purpose = purpose;
        this.grantNumber = grantNumber;
        this.grantDescription = grantDescription;
        this.federalPassThrough = federalPassThrough;
        this.federalPassThroughAgencyNumber = federalPassThroughAgencyNumber;
        this.cfdaNumber = cfdaNumber;
        this.organizationCode = organizationCode;
        this.costShareRequired = costShareRequired;
        this.finalReportDueDate = parseShortDate(finalReportDueDateString);
        this.existsByDefaultForSearching = existsByDefaultForSearching;
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
            return RassStringToJavaShortDateTimeAdapter.parseToDateTime(dateString);
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
        award.setPurpose(purpose);
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

    public Proposal toProposal() {
        Proposal proposal = new Proposal();
        
        proposal.setProposalNumber(defaultToNullIfBlank(proposalNumber));
        proposal.setProposalStatusCode(defaultToNullIfBlank(status));
        proposal.setAgencyNumber(defaultToNullIfBlank(agencyNumber));
        proposal.setProposalProjectTitle(defaultToNullIfBlank(projectTitle));
        proposal.setProposalBeginningDate(getStartDateAsSqlDate());
        proposal.setProposalEndingDate(getStopDateAsSqlDate());
        proposal.setProposalDirectCostAmount(directCostAmount);
        proposal.setProposalIndirectCostAmount(indirectCostAmount);
        proposal.setProposalPurposeCode(defaultToNullIfBlank(purpose));
        proposal.setGrantNumber(defaultToNullIfBlank(grantNumber));
        proposal.setProposalFederalPassThroughIndicator(defaultToFalseIfNull(federalPassThrough));
        proposal.setFederalPassThroughAgencyNumber(defaultToNullIfBlank(federalPassThroughAgencyNumber));
        proposal.setCfdaNumber(defaultToNullIfBlank(cfdaNumber));
        
        List<ProposalOrganization> proposalOrganizations = proposal.getProposalOrganizations();
        proposalOrganizations.add(buildProposalOrganization());
        
        List<ProposalProjectDirector> proposalProjectDirectors = proposal.getProposalProjectDirectors();
        buildProposalProjectDirectorsStream()
                .forEach(proposalProjectDirectors::add);
        
        return proposal;
    }

    private ProposalOrganization buildProposalOrganization() {
        ProposalOrganization proposalOrganization = new ProposalOrganization();
        
        proposalOrganization.setChartOfAccountsCode(RassConstants.PROPOSAL_ORG_CHART);
        proposalOrganization.setOrganizationCode(defaultToNullIfBlank(organizationCode));
        proposalOrganization.setProposalNumber(defaultToNullIfBlank(proposalNumber));
        proposalOrganization.setProposalPrimaryOrganizationIndicator(true);
        
        return proposalOrganization;
    }

    private Stream<ProposalProjectDirector> buildProposalProjectDirectorsStream() {
        return piFixtures.stream()
                .map(this::buildProposalProjectDirector);
    }

    private ProposalProjectDirector buildProposalProjectDirector(RassXMLAwardPiCoPiEntryFixture piFixture) {
        ProposalProjectDirector projectDirector = new ProposalProjectDirector();
        
        projectDirector.setPrincipalId(defaultToNullIfBlank(piFixture.projectDirectorPrincipalName));
        projectDirector.setProposalNumber(defaultToNullIfBlank(proposalNumber));
        projectDirector.setProposalPrimaryProjectDirectorIndicator(defaultToFalseIfNull(piFixture.primary));
        
        return projectDirector;
    }

    public Award toAward() {
        Award award = new Award();
        
        award.setProposalNumber(defaultToNullIfBlank(proposalNumber));
        award.setAwardStatusCode(defaultToNullIfBlank(status));
        award.setAgencyNumber(defaultToNullIfBlank(agencyNumber));
        award.setAwardProjectTitle(defaultToNullIfBlank(projectTitle));
        award.setAwardBeginningDate(getStartDateAsSqlDate());
        award.setAwardEndingDate(getStopDateAsSqlDate());
        award.setAwardDirectCostAmount(directCostAmount);
        award.setAwardIndirectCostAmount(indirectCostAmount);
        award.setAwardPurposeCode(defaultToNullIfBlank(purpose));
        award.setGrantDescriptionCode(defaultToNullIfBlank(grantDescription));
        award.setFederalPassThroughIndicator(defaultToFalseIfNull(federalPassThrough));
        award.setFederalPassThroughAgencyNumber(defaultToNullIfBlank(federalPassThroughAgencyNumber));
        
        AwardExtendedAttribute extension = new AwardExtendedAttribute();
        extension.setProposalNumber(defaultToNullIfBlank(proposalNumber));
        extension.setCostShareRequired(defaultToFalseIfNull(costShareRequired));
        extension.setFinalFiscalReportDate(getFinalReportDueDateAsSqlDate());
        award.setExtension(extension);
        
        List<AwardOrganization> awardOrganizations = award.getAwardOrganizations();
        awardOrganizations.add(buildAwardOrganization());
        
        List<AwardProjectDirector> awardProjectDirectors = award.getAwardProjectDirectors();
        buildAwardProjectDirectorsStream()
                .forEach(awardProjectDirectors::add);
        
        return award;
    }

    private AwardOrganization buildAwardOrganization() {
        AwardOrganization proposalOrganization = new AwardOrganization();
        
        proposalOrganization.setChartOfAccountsCode(RassConstants.PROPOSAL_ORG_CHART);
        proposalOrganization.setOrganizationCode(defaultToNullIfBlank(organizationCode));
        proposalOrganization.setProposalNumber(defaultToNullIfBlank(proposalNumber));
        proposalOrganization.setAwardPrimaryOrganizationIndicator(true);
        
        return proposalOrganization;
    }

    private Stream<AwardProjectDirector> buildAwardProjectDirectorsStream() {
        return piFixtures.stream()
                .map(this::buildAwardProjectDirector);
    }

    private AwardProjectDirector buildAwardProjectDirector(RassXMLAwardPiCoPiEntryFixture piFixture) {
        AwardProjectDirector projectDirector = new AwardProjectDirector();
        
        projectDirector.setPrincipalId(defaultToNullIfBlank(piFixture.projectDirectorPrincipalName));
        projectDirector.setProposalNumber(defaultToNullIfBlank(proposalNumber));
        projectDirector.setAwardPrimaryProjectDirectorIndicator(defaultToFalseIfNull(piFixture.primary));
        
        return projectDirector;
    }

    private Date buildDateFromDateTime(DateTime dateTime) {
        if (dateTime != null) {
            return dateTime.toDate();
        } else {
            return null;
        }
    }

    public java.sql.Date getStartDateAsSqlDate() {
        return buildSqlDateFromDateTime(startDate);
    }

    public java.sql.Date getStopDateAsSqlDate() {
        return buildSqlDateFromDateTime(stopDate);
    }

    public java.sql.Date getFinalReportDueDateAsSqlDate() {
        return buildSqlDateFromDateTime(finalReportDueDate);
    }

    private java.sql.Date buildSqlDateFromDateTime(DateTime dateTime) {
        if (dateTime != null) {
            return new java.sql.Date(dateTime.getMillis());
        } else {
            return null;
        }
    }

    private String defaultToNullIfBlank(String value) {
        return StringUtils.defaultIfBlank(value, null);
    }

    private Boolean defaultToFalseIfNull(Boolean value) {
        if (value == null) {
            return Boolean.FALSE;
        } else {
            return value;
        }
    }

}
