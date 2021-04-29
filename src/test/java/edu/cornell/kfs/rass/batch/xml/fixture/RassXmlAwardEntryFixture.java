package edu.cornell.kfs.rass.batch.xml.fixture;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.AwardAccount;
import org.kuali.kfs.module.cg.businessobject.AwardFundManager;
import org.kuali.kfs.module.cg.businessobject.AwardOrganization;
import org.kuali.kfs.module.cg.businessobject.AwardProjectDirector;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.module.cg.businessobject.ProposalOrganization;
import org.kuali.kfs.module.cg.businessobject.ProposalProjectDirector;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;
import edu.cornell.kfs.rass.RassConstants;
import edu.cornell.kfs.rass.RassTestConstants;
import edu.cornell.kfs.rass.batch.xml.RassXmlAwardEntry;
import edu.cornell.kfs.sys.fixture.XmlDocumentFixtureUtils;
import edu.cornell.kfs.sys.xmladapters.RassStringToJavaShortDateTimeAdapter;

public enum RassXmlAwardEntryFixture {
    FIRST("141414", "OS", "2345", "First Example Project", "2017-12-31", null, 5300000.000, 700000.000, 6000000.000, StringUtils.EMPTY,
            StringUtils.EMPTY, StringUtils.EMPTY, null, StringUtils.EMPTY, primaryOrg("3434"), Boolean.TRUE, null, "2017-12-31",
            "2022-12-31", 9000000.000, "1234S",  true, false, "COST", false, false, piFixtures(RassXMLAwardPiCoPiEntryFixture.cah292_PRIMARY)),
    SECOND("141414", "OS", "2345", "First Example Project", "2017-12-31", null, 5300000.000, 700000.000, 6000000.000, StringUtils.EMPTY,
            StringUtils.EMPTY, StringUtils.EMPTY, null, StringUtils.EMPTY, primaryOrg("3434"), Boolean.TRUE, null, "2017-12-31",
            "2022-12-31", 9000000.000, "1234S", true, false, "COST", false, false, piFixtures(RassXMLAwardPiCoPiEntryFixture.cah292_PRIMARY)),
    ANOTHER("141415", "RS", "24680", "Another Example", null, "2050-12-31", 0.0, 0.0, 0.0, StringUtils.EMPTY,
            StringUtils.EMPTY, "GRT", null, StringUtils.EMPTY, primaryOrg("2374"), Boolean.TRUE, "2022-09-30", null, null, null, 
            StringUtils.EMPTY, false, false, StringUtils.EMPTY, false, false, piFixtures(RassXMLAwardPiCoPiEntryFixture.NO_NAME_PRIMARY)),
    NULL_AMOUNTS("35656", "RS", "24680", "Award with empty totals", null, "2050-12-31", 0.0, null, null, StringUtils.EMPTY,
            StringUtils.EMPTY, "GRT", null, StringUtils.EMPTY, primaryOrg("2374"), Boolean.TRUE, "2022-09-30", null, null, null, 
            StringUtils.EMPTY, false, false, StringUtils.EMPTY, false, false, piFixtures(RassXMLAwardPiCoPiEntryFixture.jdh34_CO_PI)),

    SAMPLE_PROJECT("556677", "OS", RassXmlAgencyEntryFixture.SOME, "Some University's Sample Project", "2019-01-15", "2019-12-24",
            25000.00, 5000.00, 30000.00, "H", "98765", "GRT", Boolean.FALSE, StringUtils.EMPTY,
            primaryOrg("1500"), Boolean.TRUE, "2019-11-30", "2017-12-31", "2022-12-31", 9000000.000, "12345", true, false, "COST", false, false,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY)),
    SAMPLE_PROJECT_MISSING_REQ_FIELD("556677", "OS", StringUtils.EMPTY, "Some University's Sample Project", "2019-01-15", "2019-12-24",
            25000.00, 5000.00, 30000.00, "H", "98765", "GRT", Boolean.FALSE, StringUtils.EMPTY,
            primaryOrg("1500"), Boolean.TRUE, "2019-11-30", null, null, null, StringUtils.EMPTY, false, false, StringUtils.EMPTY, false, false,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY)),
    SAMPLE_PROJECT_NULL_FIELDS_TEST("556677", "OS", RassXmlAgencyEntryFixture.SOME, "Some University's Sample Project", "2019-01-15", "2019-12-24",
            25000.00, 5000.00, 30000.00, "H", "98765", "GRT", null, StringUtils.EMPTY,
            primaryOrg("1500"), Boolean.TRUE, "2019-11-30", "2017-12-31", "2022-12-31", 9000000.000, "12345", null, null, null, false, false,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY, RassXMLAwardPiCoPiEntryFixture.mls398_CO_PI_WITH_NULL_FLAG)),
    SAMPLE_PROJECT_SKIP_DUE_TO_NULLS("556677", "OS", RassXmlAgencyEntryFixture.SOME, "Some University's Sample Project", "2019-01-15", "2019-12-24",
            25000.00, 5000.00, null, "H", "98765", "GRT", Boolean.FALSE, StringUtils.EMPTY,
            primaryOrg("1500"), null, "2019-11-30", "2017-12-31", "2022-12-31", 9000000.000, "12345", true, false, "COST", false, false,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY, RassXMLAwardPiCoPiEntryFixture.mls398_CO_PI_WITH_NULL_FLAG)),
    SAMPLE_PROJECT_SKIP_DUE_TO_NULLS_V2("556677", "OS", RassXmlAgencyEntryFixture.SOME, "Some University's Sample Project", "2019-01-15", "2019-12-24",
            25000.00, 5000.00, null, "H", "98765", "GRT", Boolean.FALSE, StringUtils.EMPTY,
            primaryOrg("1500"), Boolean.TRUE, "2019-11-30", "2017-12-31", "2022-12-31", 9000000.000, "12345", true, false, "COST", false, false,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY, RassXMLAwardPiCoPiEntryFixture.mls398_CO_PI_WITH_NULL_FLAG)),
    SAMPLE_PROJECT_SKIP_DUE_TO_NULLS_V3("556677", "OS", RassXmlAgencyEntryFixture.SOME, "Some University's Sample Project", "2019-01-15", "2019-12-24",
            25000.00, 5000.00, 30000.00, "H", "98765", "GRT", Boolean.FALSE, StringUtils.EMPTY,
            primaryOrg("1500"), null, "2019-11-30", "2017-12-31", "2022-12-31", 9000000.000, "12345", true, false, "COST", false, false,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY, RassXMLAwardPiCoPiEntryFixture.mls398_CO_PI_WITH_NULL_FLAG)),
    SOME_DEPARTMENT_PROJECT("123789", "OS", RassXmlAgencyEntryFixture.SOME, "Some Internal Department Project", "2019-03-01", "2020-11-30",
            45000.00, 30000.00, 75000.00, "H", "34343", "GRT", Boolean.TRUE, RassXmlAgencyEntryFixture.TEST.number,
            primaryOrg("2211"), Boolean.FALSE, StringUtils.EMPTY, null, null, 0.000, null, true, true, null, true, true,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY, RassXMLAwardPiCoPiEntryFixture.mls398_CO_PI)),
    SOME_DEPARTMENT_PROJECT_V2("123789", "OS", RassXmlAgencyEntryFixture.SOME, "Some Internal Department Project", "2019-03-01", "2020-11-30",
            45000.00, 30000.00, 75000.00, "H", "34343", "CON", Boolean.TRUE, RassXmlAgencyEntryFixture.TEST.number,
            primaryOrg("2211"), Boolean.FALSE, "2020-10-12", null, null, 0.000, StringUtils.EMPTY, false, false, StringUtils.EMPTY, false, false,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY, RassXMLAwardPiCoPiEntryFixture.mls398_CO_PI)),
    SOME_DEPARTMENT_PROJECT_V3_ORG_CHANGE("123789", "OS", RassXmlAgencyEntryFixture.SOME, "Some Internal Department Project", "2019-03-01",
            "2020-11-30", 45000.00, 30000.00, 75000.00, "H", "34343", "GRT", Boolean.TRUE, RassXmlAgencyEntryFixture.TEST.number,
            organizations(organization("2211", Boolean.FALSE), organization("2555", Boolean.TRUE)),
            Boolean.FALSE, StringUtils.EMPTY,  null, null, 0.000, StringUtils.EMPTY, false, false, StringUtils.EMPTY, false, false,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY, RassXMLAwardPiCoPiEntryFixture.mls398_CO_PI)),
    SOME_DEPARTMENT_PROJECT_V4_DIRECTOR_CHANGE("123789", "OS", RassXmlAgencyEntryFixture.SOME, "Some Internal Department Project", "2019-03-01",
            "2020-11-30", 45000.00, 30000.00, 75000.00, "H", "34343", "GRT", Boolean.TRUE, RassXmlAgencyEntryFixture.TEST.number,
            primaryOrg("2211"), Boolean.FALSE, StringUtils.EMPTY, null, null, 0.000, StringUtils.EMPTY, false, false, StringUtils.EMPTY, false, false,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_CO_PI_INACTIVE, RassXMLAwardPiCoPiEntryFixture.mls398_CO_PI_INACTIVE,
                    RassXMLAwardPiCoPiEntryFixture.kan2_PRIMARY)),
    SOME_DEPARTMENT_PROJECT_V5_DIRECTOR_CHANGE2("123789", "OS", RassXmlAgencyEntryFixture.SOME, "Some Internal Department Project", "2019-03-01",
            "2020-11-30", 45000.00, 30000.00, 75000.00, "H", "34343", "GRT", Boolean.TRUE, RassXmlAgencyEntryFixture.TEST.number,
            primaryOrg("2211"), Boolean.FALSE, StringUtils.EMPTY, null, null, 0.000, StringUtils.EMPTY, false, false, StringUtils.EMPTY, false, false,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_CO_PI_INACTIVE, RassXMLAwardPiCoPiEntryFixture.mls398_PRIMARY)),
    SOME_DEPARTMENT_PROJECT_MISSING_REQ_FIELD("123789", "OS", RassXmlAgencyEntryFixture.SOME, "Some Internal Department Project", "2019-03-01", "2020-11-30",
            45000.00, 30000.00, 75000.00, "H", "34343", "GRT", Boolean.TRUE, RassXmlAgencyEntryFixture.TEST.number,
            primaryOrg("2211"), Boolean.FALSE, StringUtils.EMPTY, null, null, null, null, true, true, null, false, false,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY, RassXMLAwardPiCoPiEntryFixture.mls398_CO_PI)),
    SOME_DEPARTMENT_PROJECT_GRANT_NUM_CHANGE("123789", "OS", RassXmlAgencyEntryFixture.SOME, "Some Internal Department Project", "2019-03-01", "2020-11-30",
            45000.00, 30000.00, 75000.00, "H", "34347", "GRT", Boolean.TRUE, RassXmlAgencyEntryFixture.TEST.number,
            primaryOrg("2211"), Boolean.FALSE, StringUtils.EMPTY, null, null, 0.000, null, true, true, null, false, false,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY, RassXMLAwardPiCoPiEntryFixture.mls398_CO_PI)),
    SOME_DEPARTMENT_PROJECT_GRANT_NUM_CHANGE_ALT("123789", RassTestConstants.PROPOSAL_AWARDED_STATUS, RassXmlAgencyEntryFixture.SOME, "Some Internal Department Project", "2019-03-01", "2020-11-30",
            45000.00, 30000.00, 75000.00, "H", "34347", "GRT", Boolean.TRUE, RassXmlAgencyEntryFixture.TEST.number,
            primaryOrg("2211"), Boolean.FALSE, StringUtils.EMPTY, null, null, 0.000, null, true, true, null, false, false,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY, RassXMLAwardPiCoPiEntryFixture.mls398_CO_PI)),
    SOME_DEPARTMENT_PROJECT_GRANT_NUM_CHANGE2("123789", "OS", RassXmlAgencyEntryFixture.SOME, "Some Internal Department Project", "2019-03-01", "2020-11-30",
            45000.00, 30000.00, 75000.00, "H", "34347", "CON", Boolean.TRUE, RassXmlAgencyEntryFixture.TEST.number,
            primaryOrg("2211"), Boolean.FALSE, StringUtils.EMPTY, null, null, 0.000, null, true, true, null, false, false,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY, RassXMLAwardPiCoPiEntryFixture.mls398_CO_PI)),
    SOME_DEPARTMENT_PROJECT_GRANT_NUM_CLEAR("123789", "OS", RassXmlAgencyEntryFixture.SOME, "Some Internal Department Project", "2019-03-01", "2020-11-30",
            45000.00, 30000.00, 75000.00, "H", null, "GRT", Boolean.TRUE, RassXmlAgencyEntryFixture.TEST.number,
            primaryOrg("2211"), Boolean.FALSE, StringUtils.EMPTY, null, null, 0.000, null, true, true, null, false, false,
            piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY, RassXMLAwardPiCoPiEntryFixture.mls398_CO_PI)),
    ZERO_AMOUNT_COST_SHARE_YES_PROPOSAL_EXISTS_AWARD_EXISTS("151516", "OS", "2345", "Total amount zero cost share Y proposal exists", "2017-12-31", "2019-12-31", 5300000.000, 700000.000, 0.000, StringUtils.EMPTY,
            StringUtils.EMPTY, StringUtils.EMPTY, false, StringUtils.EMPTY, primaryOrg("3434"), Boolean.TRUE, null, "2017-12-31",
            "2022-12-31", 0.000, "1234S",  true, false, "COST", true, true, piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY)),
    ZERO_AMOUNT_COST_SHARE_YES_PROPOSAL_EXISTS_AWARD_NOT_EXISTS("151517", "OS", "2345", "Total amount zero cost share Y proposal exists", "2017-12-31", "2019-12-31", 5300000.000, 700000.000, 0.000, StringUtils.EMPTY,
            StringUtils.EMPTY, StringUtils.EMPTY, false, StringUtils.EMPTY, primaryOrg("3434"), Boolean.TRUE, null, "2017-12-31",
            "2022-12-31", 0.000, "1234S",  true, false, "COST", true, false, piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY)),
    ZERO_AMOUNT_COST_SHARE_NO_PROPOSAL_EXISTS_AWARD_EXISTS("151518", "OS", "2345", "Total amount zero cost share Y proposal exists", "2017-12-31", "2019-12-31", 5300000.000, 700000.000, 0.000, StringUtils.EMPTY,
            StringUtils.EMPTY, StringUtils.EMPTY, false, StringUtils.EMPTY, primaryOrg("3434"), Boolean.FALSE, null, "2017-12-31",
            "2022-12-31", 0.000, "1234S",  true, false, "COST", true, true, piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY)),
    ZERO_AMOUNT_COST_SHARE_YES_PROPOSAL_DOES_NOT_EXIST_AWARD_EXISTS("151519", "OS", "2345", "Total amount zero cost share Y proposal exists", "2017-12-31", "2019-12-31", 5300000.000, 700000.000, 0.000, StringUtils.EMPTY,
            StringUtils.EMPTY, StringUtils.EMPTY, false, StringUtils.EMPTY, primaryOrg("3434"), Boolean.TRUE, null, "2017-12-31",
            "2022-12-31", 0.000, "1234S",  true, false, "COST", false, true, piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY)),
    ZERO_AMOUNT_COST_SHARE_YES_PROPOSAL_DOES_NOT_EXIST_AWARD_NOT_EXISTS("151520", "OS", "2345", "Total amount zero cost share Y proposal exists", "2017-12-31", "2019-12-31", 5300000.000, 700000.000, 0.000, StringUtils.EMPTY,
            StringUtils.EMPTY, StringUtils.EMPTY, false, StringUtils.EMPTY, primaryOrg("3434"), Boolean.TRUE, null, "2017-12-31",
            "2022-12-31", 0.000, "1234S",  true, false, "COST", false, false, piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY)),
    ZERO_AMOUNT_COST_SHARE_NO_PROPOSAL_DOES_NOT_EXIST("151615", "OS", "2345", "Total amount zero cost share Y proposal exists", "2017-12-31", "2019-12-31", 5300000.000, 700000.000, 0.000, StringUtils.EMPTY,
            StringUtils.EMPTY, StringUtils.EMPTY, false, StringUtils.EMPTY, primaryOrg("3434"), Boolean.FALSE, null, "2017-12-31",
            "2022-12-31", 0.000, "1234S",  true, false, "COST", false, false, piFixtures(RassXMLAwardPiCoPiEntryFixture.mgw3_PRIMARY));

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
    public final String organizationCode;
    public final Boolean costShareRequired;
    public final DateTime finalReportDueDate;
    public final DateTime budgetStartDate;
    public final DateTime budgetStopDate;
    public final KualiDecimal budgetTotalAmount;
    public final String primeAgreementNumber;
    public final String pricingType;
    public final Boolean everify;
    public final Boolean finalFinancialReportRequired;
    public final boolean proposalExistsByDefaultForSearching;
    public final boolean awardExistsByDefaultForSearching;
    public final List<RassXMLAwardPiCoPiEntryFixture> piFixtures;
    public final List<Pair<String, Boolean>> organizations;
    
    private RassXmlAwardEntryFixture(String proposalNumber, String status, RassXmlAgencyEntryFixture agency, String projectTitle,
            String startDateString, String stopDateString, Double directCostAmount, Double indirectCostAmount,
            Double totalAmount, String purpose, String grantNumber, String grantDescription, Boolean federalPassThrough,
            String federalPassThroughAgencyNumber, Pair<String, Boolean>[] organizations,
            Boolean costShareRequired, String finalReportDueDateString, String budgetStartDateString, String budgetStopDateString,
            Double budgetTotalAmount, String primeAgreementNumber, Boolean everify, Boolean finalFinancialReportRequired, String pricingType,
            boolean proposalExistsByDefaultForSearching, boolean awardExistsByDefaultForSearching, RassXMLAwardPiCoPiEntryFixture[] piFixtures) {
        this(proposalNumber, status, agency.number, projectTitle, startDateString, stopDateString, directCostAmount, indirectCostAmount,
                totalAmount, purpose, grantNumber, grantDescription, federalPassThrough, federalPassThroughAgencyNumber,
                organizations, costShareRequired, finalReportDueDateString, budgetStartDateString, budgetStopDateString,
                budgetTotalAmount, primeAgreementNumber, everify, finalFinancialReportRequired, pricingType, proposalExistsByDefaultForSearching, awardExistsByDefaultForSearching, piFixtures);
    }
    
    private RassXmlAwardEntryFixture(String proposalNumber, String status, String agencyNumber, String projectTitle,
            String startDateString, String stopDateString, Double directCostAmount, Double indirectCostAmount,
            Double totalAmount, String purpose, String grantNumber, String grantDescription, Boolean federalPassThrough,
            String federalPassThroughAgencyNumber, Pair<String, Boolean>[] organizations,
            Boolean costShareRequired, String finalReportDueDateString, String budgetStartDateString, String budgetStopDateString,
            Double budgetTotalAmount, String primeAgreementNumber, Boolean everify, Boolean finalFinancialReportRequired, String pricingType,
            boolean proposalExistsByDefaultForSearching, boolean awardExistsByDefaultForSearching, RassXMLAwardPiCoPiEntryFixture[] piFixtures) {
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
        this.organizationCode = findPrimaryOrgCode(organizations);
        this.costShareRequired = costShareRequired;
        this.finalReportDueDate = parseShortDate(finalReportDueDateString);
        this.proposalExistsByDefaultForSearching = proposalExistsByDefaultForSearching; 
        this.awardExistsByDefaultForSearching = awardExistsByDefaultForSearching;
        this.piFixtures = XmlDocumentFixtureUtils.toImmutableList(piFixtures);
        this.organizations = XmlDocumentFixtureUtils.toImmutableList(organizations);
        this.budgetStartDate = parseShortDate(budgetStartDateString);
        this.budgetStopDate = parseShortDate(budgetStopDateString);
        this.budgetTotalAmount = buildKualiDecimalFromDouble(budgetTotalAmount);
        this.primeAgreementNumber = primeAgreementNumber;
        this.pricingType = pricingType;
        this.everify = everify;
        this.finalFinancialReportRequired = finalFinancialReportRequired;
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
    
    private static Pair<String,Boolean>[] primaryOrg(String orgCode) {
        return organizations(organization(orgCode, Boolean.TRUE));
    }
    
    @SafeVarargs
    private static Pair<String, Boolean>[] organizations(Pair<String, Boolean>... organizations) {
        return organizations;
    }
    
    private static Pair<String, Boolean> organization(String orgCode, Boolean primaryOrg) {
        return Pair.of(orgCode, primaryOrg);
    }
    
    private static String findPrimaryOrgCode(Pair<String, Boolean>[] organizations) {
        for (Pair<String, Boolean> organization : organizations) {
            if (organization.getRight()) {
                return organization.getLeft();
            }
        }
        return StringUtils.EMPTY;
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
        award.setOrganizationCode(organizationCode);
        award.setCostShareRequired(costShareRequired);
        award.setFinalReportDueDate(buildDateFromDateTime(finalReportDueDate));
        award.setEverify(everify);
        award.setFinalFinancialReportRequired(finalFinancialReportRequired);
        award.setBudgetStartDate(buildDateFromDateTime(budgetStartDate));
        award.setBudgetStopDate(buildDateFromDateTime(budgetStopDate));
        award.setBudgetTotalAmount(budgetTotalAmount);
        award.setPricingType(pricingType);
        award.setPrimeAgreementNumber(primeAgreementNumber);
        
        for (RassXMLAwardPiCoPiEntryFixture fixture : piFixtures) {
            if (fixture.getNullSafeActive()) {
                award.getPrincipalAndCoPrincipalInvestigators().add(fixture.toRassXMLAwardPiCoPiEntry());
            }
        }
        return award;
    }

    public Proposal toProposal() {
        Proposal proposal = new Proposal();
        
        proposal.setProposalAwardTypeCode(RassTestConstants.DEFAULT_PROPOSAL_AWARD_TYPE);
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
        proposal.setProposalFederalPassThroughIndicator(getNullSafeFederalPassThrough());
        proposal.setFederalPassThroughAgencyNumber(defaultToNullIfBlank(federalPassThroughAgencyNumber));
        
        List<ProposalOrganization> proposalOrganizations = proposal.getProposalOrganizations();
        proposalOrganizations.add(buildPrimaryProposalOrganization());
        
        List<ProposalProjectDirector> proposalProjectDirectors = proposal.getProposalProjectDirectors();
        buildProposalProjectDirectorsStream()
                .forEach(proposalProjectDirectors::add);
        
        return proposal;
    }

    private ProposalOrganization buildPrimaryProposalOrganization() {
        ProposalOrganization proposalOrganization = new ProposalOrganization();
        
        proposalOrganization.setChartOfAccountsCode(RassConstants.PROPOSAL_ORG_CHART);
        proposalOrganization.setOrganizationCode(defaultToNullIfBlank(organizationCode));
        proposalOrganization.setProposalNumber(defaultToNullIfBlank(proposalNumber));
        proposalOrganization.setProposalPrimaryOrganizationIndicator(true);
        proposalOrganization.setActive(true);
        
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
        projectDirector.setProposalPrimaryProjectDirectorIndicator(piFixture.getNullSafePrimary());
        projectDirector.setActive(piFixture.getNullSafeActive());
        
        return projectDirector;
    }

    public Award toAward() {
        Award award = new Award();
        
        award.setProposalAwardTypeCode(RassTestConstants.DEFAULT_PROPOSAL_AWARD_TYPE);
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
        award.setFederalPassThroughIndicator(getNullSafeFederalPassThrough());
        award.setFederalPassThroughAgencyNumber(defaultToNullIfBlank(federalPassThroughAgencyNumber));
        award.setInstrumentTypeCode(defaultToNullIfBlank(pricingType));

        AwardExtendedAttribute extension = new AwardExtendedAttribute();
        extension.setProposalNumber(defaultToNullIfBlank(proposalNumber));
        extension.setCostShareRequired(getNullSafeCostShareRequired());
        extension.setFinalFiscalReportDate(getFinalReportDueDateAsSqlDate());
        extension.setEverify(getNullSafeEverify());
        extension.setFinalFinancialReportRequired(getNullSafeFinalFinancialReportRequired());
        extension.setPrimeAgreementNumber(defaultToNullIfBlank(primeAgreementNumber));
        extension.setBudgetBeginningDate(buildSqlDateFromDateTime(budgetStartDate));
        extension.setBudgetEndingDate(buildSqlDateFromDateTime(budgetStopDate));
        extension.setBudgetTotalAmount(budgetTotalAmount);
        award.setExtension(extension);
        
        List<AwardOrganization> awardOrganizations = award.getAwardOrganizations();
        buildAwardOrganizationsStream()
                .forEach(awardOrganizations::add);
        
        List<AwardProjectDirector> awardProjectDirectors = award.getAwardProjectDirectors();
        buildAwardProjectDirectorsStream()
                .forEach(awardProjectDirectors::add);
        
        award.getAwardAccounts().add(buildDefaultAwardAccount());
        award.getAwardFundManagers().add(buildDefaultAwardFundManager());
        
        return award;
    }

    private Stream<AwardOrganization> buildAwardOrganizationsStream() {
        return organizations.stream()
                .map(orgData -> buildAwardOrganization(orgData.getLeft(), orgData.getRight()));
    }

    private AwardOrganization buildAwardOrganization(String orgCode, Boolean activeAndPrimary) {
        AwardOrganization awardOrganization = new AwardOrganization();
        
        awardOrganization.setChartOfAccountsCode(RassConstants.PROPOSAL_ORG_CHART);
        awardOrganization.setOrganizationCode(defaultToNullIfBlank(orgCode));
        awardOrganization.setProposalNumber(defaultToNullIfBlank(proposalNumber));
        awardOrganization.setAwardPrimaryOrganizationIndicator(activeAndPrimary);
        awardOrganization.setActive(activeAndPrimary);
        
        return awardOrganization;
    }

    private Stream<AwardProjectDirector> buildAwardProjectDirectorsStream() {
        return piFixtures.stream()
                .map(this::buildAwardProjectDirector);
    }

    private AwardProjectDirector buildAwardProjectDirector(RassXMLAwardPiCoPiEntryFixture piFixture) {
        AwardProjectDirector projectDirector = new AwardProjectDirector();
        
        projectDirector.setPrincipalId(defaultToNullIfBlank(piFixture.projectDirectorPrincipalName));
        projectDirector.setProposalNumber(defaultToNullIfBlank(proposalNumber));
        projectDirector.setAwardPrimaryProjectDirectorIndicator(piFixture.getNullSafePrimary());
        projectDirector.setActive(piFixture.getNullSafeActive());
        
        return projectDirector;
    }

    private AwardAccount buildDefaultAwardAccount() {
        AwardAccount awardAccount = new AwardAccount();
        awardAccount.setProposalNumber(proposalNumber);
        awardAccount.setPrincipalId(RassTestConstants.DEFAULT_PROJECT_DIRECTOR_PRINCIPAL_ID);
        awardAccount.setChartOfAccountsCode(RassTestConstants.DEFAULT_AWARD_CHART);
        awardAccount.setAccountNumber(RassTestConstants.DEFAULT_AWARD_ACCOUNT);
        return awardAccount;
    }

    private AwardFundManager buildDefaultAwardFundManager() {
        AwardFundManager fundManager = new AwardFundManager();
        fundManager.setProposalNumber(proposalNumber);
        fundManager.setPrincipalId(RassTestConstants.DEFAULT_FUND_MANAGER_PRINCIPAL_ID);
        fundManager.setPrimaryFundManagerIndicator(true);
        return fundManager;
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

    public java.sql.Date getBudgetEndingDateAsSqlDate() {
        return buildSqlDateFromDateTime(budgetStopDate);
    }
    
    public java.sql.Date getBudgetBeginningDateAsSqlDate() {
        return buildSqlDateFromDateTime(budgetStartDate);
    }

    public java.sql.Date getStopDateAsSqlDate() {
        return buildSqlDateFromDateTime(stopDate);
    }

    public java.sql.Date getFinalReportDueDateAsSqlDate() {
        return buildSqlDateFromDateTime(finalReportDueDate);
    }

    public Boolean getNullSafeFederalPassThrough() {
        return defaultToFalseIfNull(federalPassThrough);
    }

    public Boolean getNullSafeCostShareRequired() {
        return defaultToFalseIfNull(costShareRequired);
    }

    public Boolean getNullSafeEverify() {
        return defaultToFalseIfNull(everify);
    }

    public Boolean getNullSafeFinalFinancialReportRequired() {
        return defaultToFalseIfNull(finalFinancialReportRequired);
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
