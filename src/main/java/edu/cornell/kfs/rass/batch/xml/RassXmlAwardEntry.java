package edu.cornell.kfs.rass.batch.xml;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.xmladapters.NullableKualiDecimalXmlAdapter;
import edu.cornell.kfs.sys.xmladapters.RassStringToJavaShortDateTimeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Award", namespace = StringUtils.EMPTY)
public class RassXmlAwardEntry {
    
    @XmlElement(name = "Proposal_Number", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String proposalNumber;
    
    @XmlElement(name = "Status", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String status;
    
    @XmlElement(name = "Agency_Number", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String agencyNumber;
    
    @XmlElement(name = "Project_Title", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String projectTitle;
    
    @XmlElement(name = "Start_Date", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(RassStringToJavaShortDateTimeAdapter.class)
    private Date startDate;
    
    @XmlElement(name = "Stop_Date", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(RassStringToJavaShortDateTimeAdapter.class)
    private Date stopDate;
    
    @XmlElement(name = "Direct_Cost_Amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NullableKualiDecimalXmlAdapter.class)
    private KualiDecimal directCostAmount;
    
    @XmlElement(name = "Indirect_Cost_Amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NullableKualiDecimalXmlAdapter.class)
    private KualiDecimal indirectCostAMount;
    
    @XmlElement(name = "Total_Amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NullableKualiDecimalXmlAdapter.class)
    private KualiDecimal totalAMount;
    
    @XmlElement(name = "Grant_Number", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String grantNumber;
    
    @XmlElement(name = "Grant_Description", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String grantDescription;
    
    @XmlElement(name = "Federal_Pass_Through", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String federalPassThrough;
    
    @XmlElement(name = "Federal_Pass_Through_Agency_Number", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String federalPassThroughAgencyNumber;
    
    @XmlElement(name = "CFDA_Number", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String cfdaNumber;
    
    @XmlElement(name = "Organization_Code", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String organizationCode;
    
    @XmlElement(name = "Cost_Share_Required", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String costShareRequiredString;
    
    @XmlElement(name = "Final_Fiscal_Report_Due_Date", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(RassStringToJavaShortDateTimeAdapter.class)
    private Date finalReportDueDate;
    
    @XmlElementWrapper(name = "PI_and_CoPIs", namespace = StringUtils.EMPTY, required = true)
    @XmlElement(name = "PI_or_CoPI", namespace = StringUtils.EMPTY, required = true)
    private List<RassXMLAwardPiCoPiEntry> principalAndCoPrincipalInvestigators;
    
    public RassXmlAwardEntry() {
        principalAndCoPrincipalInvestigators = new ArrayList<RassXMLAwardPiCoPiEntry>();
    }

    public String getProposalNumber() {
        return proposalNumber;
    }

    public void setProposalNumber(String proposalNumber) {
        this.proposalNumber = proposalNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAgencyNumber() {
        return agencyNumber;
    }

    public void setAgencyNumber(String agencyNumber) {
        this.agencyNumber = agencyNumber;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStopDate() {
        return stopDate;
    }

    public void setStopDate(Date stopDate) {
        this.stopDate = stopDate;
    }

    public KualiDecimal getDirectCostAmount() {
        return directCostAmount;
    }

    public void setDirectCostAmount(KualiDecimal directCostAmount) {
        this.directCostAmount = directCostAmount;
    }

    public KualiDecimal getIndirectCostAMount() {
        return indirectCostAMount;
    }

    public void setIndirectCostAMount(KualiDecimal indirectCostAMount) {
        this.indirectCostAMount = indirectCostAMount;
    }

    public KualiDecimal getTotalAMount() {
        return totalAMount;
    }

    public void setTotalAMount(KualiDecimal totalAMount) {
        this.totalAMount = totalAMount;
    }

    public String getGrantNumber() {
        return grantNumber;
    }

    public void setGrantNumber(String grantNumber) {
        this.grantNumber = grantNumber;
    }

    public String getGrantDescription() {
        return grantDescription;
    }

    public void setGrantDescription(String grantDescription) {
        this.grantDescription = grantDescription;
    }

    public String getFederalPassThrough() {
        return federalPassThrough;
    }

    public void setFederalPassThrough(String federalPassThrough) {
        this.federalPassThrough = federalPassThrough;
    }

    public String getFederalPassThroughAgencyNumber() {
        return federalPassThroughAgencyNumber;
    }

    public void setFederalPassThroughAgencyNumber(String federalPassThroughAgencyNumber) {
        this.federalPassThroughAgencyNumber = federalPassThroughAgencyNumber;
    }

    public String getCfdaNumber() {
        return cfdaNumber;
    }

    public void setCfdaNumber(String cfdaNumber) {
        this.cfdaNumber = cfdaNumber;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public String getCostShareRequiredString() {
        return costShareRequiredString;
    }

    public void setCostShareRequiredString(String costShareRequiredString) {
        this.costShareRequiredString = costShareRequiredString;
    }

    public Date getFinalReportDueDate() {
        return finalReportDueDate;
    }

    public void setFinalReportDueDate(Date finalReportDueDate) {
        this.finalReportDueDate = finalReportDueDate;
    }

    public List<RassXMLAwardPiCoPiEntry> getPrincipalAndCoPrincipalInvestigators() {
        return principalAndCoPrincipalInvestigators;
    }

    public void setPrincipalAndCoPrincipalInvestigators(List<RassXMLAwardPiCoPiEntry> principalAndCoPrincipalInvestigators) {
        this.principalAndCoPrincipalInvestigators = principalAndCoPrincipalInvestigators;
    }
    
    @Override
    public boolean equals(Object o) {
        
        /*
    private List<RassXMLAwardPiCoPiEntry> principalAndCoPrincipalInvestigators;
         */
        if (o instanceof RassXmlAwardEntry) {
            RassXmlAwardEntry other = (RassXmlAwardEntry) o;
            return StringUtils.equals(proposalNumber, other.getProposalNumber()) &&
                    StringUtils.equals(status, other.getStatus()) &&
                    StringUtils.equals(projectTitle, other.getProjectTitle()) &&
                    Objects.equals(startDate, other.getStartDate()) &&
                    Objects.equals(stopDate, other.getStopDate()) &&
                    Objects.equals(directCostAmount, other.getDirectCostAmount()) &&
                    Objects.equals(indirectCostAMount, other.getIndirectCostAMount()) &&
                    Objects.equals(totalAMount, other.getTotalAMount()) &&
                    StringUtils.equals(grantNumber, other.getGrantNumber()) &&
                    StringUtils.equals(grantDescription, other.getGrantDescription()) &&
                    StringUtils.equals(federalPassThrough, other.getFederalPassThrough()) &&
                    StringUtils.equals(federalPassThroughAgencyNumber, other.getFederalPassThroughAgencyNumber()) &&
                    StringUtils.equals(cfdaNumber, other.getCfdaNumber()) &&
                    StringUtils.equals(organizationCode, other.getOrganizationCode()) &&
                    StringUtils.equals(costShareRequiredString, other.getCostShareRequiredString()) &&
                    Objects.equals(finalReportDueDate, other.getFinalReportDueDate()) &&
                    Objects.equals(principalAndCoPrincipalInvestigators, other.getPrincipalAndCoPrincipalInvestigators());
                    
        } else {
            return false;
        }    
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RassXmlAwardEntry: [proposalNumber: ").append(proposalNumber);
        sb.append(", status:").append(status);
        sb.append(", projectTitle:").append(projectTitle);
        sb.append(", startDate:").append(startDate);
        sb.append(", stopDate:").append(stopDate);
        sb.append(", directCostAmount:").append(directCostAmount);
        sb.append(", indirectCostAMount:").append(indirectCostAMount);
        sb.append(", totalAMount:").append(totalAMount);
        sb.append(", grantNumber:").append(grantNumber);
        sb.append(", grantDescription:").append(grantDescription);
        sb.append(", federalPassThrough:").append(federalPassThrough);
        sb.append(", federalPassThroughAgencyNumber:").append(federalPassThroughAgencyNumber);
        sb.append(", cfdaNumber:").append(cfdaNumber);
        sb.append(", organizationCode:").append(organizationCode);
        sb.append(", costShareRequiredString:").append(costShareRequiredString);
        sb.append(", finalReportDueDate:").append(finalReportDueDate);
        if (CollectionUtils.isNotEmpty(principalAndCoPrincipalInvestigators)) {
            for (RassXMLAwardPiCoPiEntry pi : principalAndCoPrincipalInvestigators) {
                sb.append(KFSConstants.SQUARE_BRACKET_LEFT).append(pi.toString()).append(KFSConstants.SQUARE_BRACKET_RIGHT);
            }
        } else {
            sb.append(KFSConstants.SQUARE_BRACKET_LEFT).append("NO INVESTIGATIORS").append(KFSConstants.SQUARE_BRACKET_RIGHT);
        }
        sb.append(KFSConstants.SQUARE_BRACKET_RIGHT);
        return sb.toString();
    }
    

}
