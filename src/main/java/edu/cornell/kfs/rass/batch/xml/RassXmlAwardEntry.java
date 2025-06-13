package edu.cornell.kfs.rass.batch.xml;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.xmladapters.BooleanNullPossibleXmlAdapter;
import edu.cornell.kfs.sys.xmladapters.KualiDecimalNullPossibleXmlAdapter;
import edu.cornell.kfs.sys.xmladapters.RassStringToJavaShortDateTimeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Award", namespace = StringUtils.EMPTY)
public class RassXmlAwardEntry implements RassXmlObject {
    
    @XmlElement(name = "Proposal_Number", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String proposalNumber;
    
    @XmlElement(name = "Status", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String status;
    
    @XmlElement(name = "Agency_Number", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String agencyNumber;
    
    @XmlElement(name = "Project_Title", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String projectTitle;
    
    @XmlElement(name = "Start_Date", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(RassStringToJavaShortDateTimeAdapter.class)
    private Date startDate;
    
    @XmlElement(name = "Stop_Date", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(RassStringToJavaShortDateTimeAdapter.class)
    private Date stopDate;
    
    @XmlElement(name = "Direct_Cost_Amount", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(KualiDecimalNullPossibleXmlAdapter.class)
    private KualiDecimal directCostAmount;
    
    @XmlElement(name = "Indirect_Cost_Amount", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(KualiDecimalNullPossibleXmlAdapter.class)
    private KualiDecimal indirectCostAmount;
    
    @XmlElement(name = "Total_Amount", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(KualiDecimalNullPossibleXmlAdapter.class)
    private KualiDecimal totalAmount;
    
    @XmlElement(name = "Purpose", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String purpose;
    
    @XmlElement(name = "Grant_Number", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String grantNumber;
    
    @XmlElement(name = "Grant_Description", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String grantDescription;
    
    @XmlElement(name = "Federal_Pass_Through", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(BooleanNullPossibleXmlAdapter.class)
    private Boolean federalPassThrough;
    
    @XmlElement(name = "Federal_Pass_Through_Agency_Number", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String federalPassThroughAgencyNumber;
    
    @XmlElement(name = "Organization_Code", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String organizationCode;
    
    @XmlElement(name = "Cost_Share_Required", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(BooleanNullPossibleXmlAdapter.class)
    private Boolean costShareRequired;
    
    @XmlElement(name = "Final_Fiscal_Report_Due_Date", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(RassStringToJavaShortDateTimeAdapter.class)
    private Date finalReportDueDate;
    
    @XmlElement(name = "Budget_Start", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(RassStringToJavaShortDateTimeAdapter.class)
    private Date budgetStartDate;
    
    @XmlElement(name = "Budget_Stop", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(RassStringToJavaShortDateTimeAdapter.class)
    private Date budgetStopDate;
    
    @XmlElement(name = "Budget_Total", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(KualiDecimalNullPossibleXmlAdapter.class)
    private KualiDecimal budgetTotalAmount;
    
    @XmlElement(name = "Prime_Agreement_Number", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String primeAgreementNumber;
    
    @XmlElement(name = "Pricing_Type", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String pricingType;
    
    @XmlElement(name = "E_Verify", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(BooleanNullPossibleXmlAdapter.class)
    private Boolean everify;
    
    @XmlElement(name = "Final_Financial_Report_Required", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(BooleanNullPossibleXmlAdapter.class)
    private Boolean finalFinancialReportRequired;
    
    @XmlElementWrapper(name = "PI_and_CoPIs", namespace = StringUtils.EMPTY)
    @XmlElement(name = "PI_or_CoPI", namespace = StringUtils.EMPTY)
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

    public KualiDecimal getIndirectCostAmount() {
        return indirectCostAmount;
    }

    public void setIndirectCostAmount(KualiDecimal indirectCostAmount) {
        this.indirectCostAmount = indirectCostAmount;
    }

    public KualiDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(KualiDecimal totalAmount) {
        this.totalAmount = totalAmount;
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

    public Boolean getFederalPassThrough() {
        return federalPassThrough;
    }

    public void setFederalPassThrough(Boolean federalPassThrough) {
        this.federalPassThrough = federalPassThrough;
    }

    public String getFederalPassThroughAgencyNumber() {
        return federalPassThroughAgencyNumber;
    }

    public void setFederalPassThroughAgencyNumber(String federalPassThroughAgencyNumber) {
        this.federalPassThroughAgencyNumber = federalPassThroughAgencyNumber;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public Boolean getCostShareRequired() {
        return costShareRequired;
    }

    public void setCostShareRequired(Boolean costShareRequired) {
        this.costShareRequired = costShareRequired;
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
        if (o instanceof RassXmlAwardEntry) {
            RassXmlAwardEntry other = (RassXmlAwardEntry) o;
            return StringUtils.equals(proposalNumber, other.getProposalNumber()) &&
                    StringUtils.equals(status, other.getStatus()) &&
                    StringUtils.equals(projectTitle, other.getProjectTitle()) &&
                    Objects.equals(startDate, other.getStartDate()) &&
                    Objects.equals(stopDate, other.getStopDate()) &&
                    Objects.equals(directCostAmount, other.getDirectCostAmount()) &&
                    Objects.equals(indirectCostAmount, other.getIndirectCostAmount()) &&
                    Objects.equals(totalAmount, other.getTotalAmount()) &&
                    StringUtils.equals(purpose, other.getPurpose()) &&
                    StringUtils.equals(grantNumber, other.getGrantNumber()) &&
                    StringUtils.equals(grantDescription, other.getGrantDescription()) &&
                    Objects.equals(federalPassThrough, other.getFederalPassThrough()) &&
                    StringUtils.equals(federalPassThroughAgencyNumber, other.getFederalPassThroughAgencyNumber()) &&
                    StringUtils.equals(organizationCode, other.getOrganizationCode()) &&
                    Objects.equals(costShareRequired, other.getCostShareRequired()) &&
                    Objects.equals(finalReportDueDate, other.getFinalReportDueDate()) &&
                    Objects.equals(budgetStartDate, other.getBudgetStartDate()) &&
                    Objects.equals(budgetStopDate, other.getBudgetStopDate()) &&
                    Objects.equals(budgetTotalAmount, other.getBudgetTotalAmount()) &&
                    StringUtils.equals(primeAgreementNumber, other.getPrimeAgreementNumber()) &&
                    StringUtils.equals(pricingType, other.getPricingType()) &&
                    Objects.equals(everify, other.getEverify()) &&
                    Objects.equals(finalFinancialReportRequired, other.getFinalFinancialReportRequired()) &&
                    Objects.equals(principalAndCoPrincipalInvestigators, other.getPrincipalAndCoPrincipalInvestigators());
                    
        } else {
            return false;
        }    
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(proposalNumber, status, projectTitle, startDate, stopDate, directCostAmount, indirectCostAmount, totalAmount, purpose, grantNumber, grantDescription,
                federalPassThrough, federalPassThroughAgencyNumber, organizationCode, costShareRequired, finalReportDueDate, principalAndCoPrincipalInvestigators);
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

	public String getPurpose() {
		return purpose;
	}
	
	public void setPurpose(String purpose) {
	    this.purpose = purpose;
	}
	
    public Date getBudgetStartDate() {
        return budgetStartDate;
    }

    public void setBudgetStartDate(Date budgetStartDate) {
        this.budgetStartDate = budgetStartDate;
    }

    public Date getBudgetStopDate() {
        return budgetStopDate;
    }

    public void setBudgetStopDate(Date budgetStopDate) {
        this.budgetStopDate = budgetStopDate;
    }

    public KualiDecimal getBudgetTotalAmount() {
        return budgetTotalAmount;
    }

    public void setBudgetTotalAmount(KualiDecimal budgetTotalAmount) {
        this.budgetTotalAmount = budgetTotalAmount;
    }

    public String getPricingType() {
        return pricingType;
    }

    public void setPricingType(String pricingType) {
        this.pricingType = pricingType;
    }

    public String getPrimeAgreementNumber() {
        return primeAgreementNumber;
    }

    public void setPrimeAgreementNumber(String primeAgreementNumber) {
        this.primeAgreementNumber = primeAgreementNumber;
    }

    public Boolean getEverify() {
        return everify;
    }

    public void setEverify(Boolean everify) {
        this.everify = everify;
    }

    public Boolean getFinalFinancialReportRequired() {
        return finalFinancialReportRequired;
    }

    public void setFinalFinancialReportRequired(Boolean finalFinancialReportRequired) {
        this.finalFinancialReportRequired = finalFinancialReportRequired;
    }

}
