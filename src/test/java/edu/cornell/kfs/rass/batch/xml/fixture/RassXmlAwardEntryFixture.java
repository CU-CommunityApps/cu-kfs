package edu.cornell.kfs.rass.batch.xml.fixture;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormatter;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.rass.batch.xml.RassXmlAwardEntry;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapperMarshalTest;
import edu.cornell.kfs.sys.xmladapters.KualiDecimalXmlAdapter;
import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

public enum RassXmlAwardEntryFixture {
    FIRST("141414", "OS", "2345", "First Example Project", null, null, new KualiDecimal(5300000.000), new KualiDecimal(700000.000), new KualiDecimal(6000000.000),
            StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, "3434", "Y", null),
    ANOTHER("141415", "RS", "24680", "Another Example", null, null, new KualiDecimal(0), new KualiDecimal(0), new KualiDecimal(0),
            StringUtils.EMPTY, "GRT", StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, "2374", "Y", "2022-09-30");
    public final String proposalNumber;
    public final String status;
    public final String agencyNumber;
    public final String projectTitle;
    public final Date startDate;
    public final Date stopDate;
    public final KualiDecimal directCostAmount;
    public final KualiDecimal indirectCostAMount;
    public final KualiDecimal totalAMount;
    public final String grantNumber;
    public final String grantDescription;
    public final String federalPassThrough;
    public final String federalPassThroughAgencyNumber;
    public final String cfdaNumber;
    public final String organizationCode;
    public final String costShareRequiredString;
    public final Date finalReportDueDate;
    
    private RassXmlAwardEntryFixture(String proposalNumber, String status, String agencyNumber, String projectTitle,
            Date startDate, Date stopDate, KualiDecimal directCostAmount, KualiDecimal indirectCostAMount,
            KualiDecimal totalAMount, String grantNumber, String grantDescription, String federalPassThrough,
            String federalPassThroughAgencyNumber, String cfdaNumber, String organizationCode,
            String costShareRequiredString, String finalReportDueDateString) {
        DateTimeFormatter dateformatter = RassXmlDocumentWrapperMarshalTest.getRASSShortDateTimeFormatter();
        this.proposalNumber = proposalNumber;
        this.status = status;
        this.agencyNumber = agencyNumber;
        this.projectTitle = projectTitle;
        this.startDate = startDate;
        this.stopDate = stopDate;
        this.directCostAmount = directCostAmount;
        this.indirectCostAMount = indirectCostAMount;
        this.totalAMount = totalAMount;
        this.grantNumber = grantNumber;
        this.grantDescription = grantDescription;
        this.federalPassThrough = federalPassThrough;
        this.federalPassThroughAgencyNumber = federalPassThroughAgencyNumber;
        this.cfdaNumber = cfdaNumber;
        this.organizationCode = organizationCode;
        this.costShareRequiredString = costShareRequiredString;
        if (StringUtils.isNotBlank(finalReportDueDateString)) {
            this.finalReportDueDate = dateformatter.parseDateTime(finalReportDueDateString).toDate();
        } else {
            this.finalReportDueDate = null;
        }
    }
    
    public RassXmlAwardEntry toRassXmlAwardEntry() {
        RassXmlAwardEntry award = new RassXmlAwardEntry();
        award.setProposalNumber(proposalNumber);
        award.setStatus(status);
        award.setAgencyNumber(agencyNumber);
        award.setProjectTitle(projectTitle);
        award.setStartDate(startDate);
        award.setStopDate(stopDate);
        award.setDirectCostAmount(directCostAmount);
        award.setIndirectCostAMount(indirectCostAMount);
        award.setTotalAMount(totalAMount);
        award.setGrantNumber(grantNumber);
        award.setGrantDescription(grantDescription);
        award.setFederalPassThrough(federalPassThrough);
        award.setFederalPassThroughAgencyNumber(federalPassThroughAgencyNumber);
        award.setCfdaNumber(cfdaNumber);
        award.setOrganizationCode(organizationCode);
        award.setCostShareRequiredString(costShareRequiredString);
        award.setFinalReportDueDate(finalReportDueDate);
        return award;
    }
}
