package edu.cornell.kfs.rass.batch.xml;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.xmladapters.KualiDecimalXmlAdapter;
import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

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
    
    @XmlElement(name = "Direct_Cost_Amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    private KualiDecimal directCostAmount;
    
    @XmlElement(name = "Indirect_Cost_Amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    private KualiDecimal indirectCostAMount;
    
    @XmlElement(name = "Total_Amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    private KualiDecimal totalAMount;
    
    @XmlElement(name = "Grant_Description", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String grantDescription;
    
    @XmlElement(name = "Organization_Code", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String organizationCode;
    
    @XmlElement(name = "Cost_Share_Required", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String costShareRequiredString;
    
    @XmlElement(name = "Final_Fiscal_Report_Due_Date", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(StringToJavaDateAdapter.class)
    private Date finalReportDueDate;
    
    @XmlElementWrapper(name = "PI_and_CoPIs", namespace = StringUtils.EMPTY, required = true)
    @XmlElement(name = "PI_or_CoPI", namespace = StringUtils.EMPTY, required = true)
    private List<RassXMLAwardPiCoPiEntry> principalAndCoPrincipalInvestigators;
    
    public RassXmlAwardEntry() {
        principalAndCoPrincipalInvestigators = new ArrayList<RassXMLAwardPiCoPiEntry>();
    }
    

}
