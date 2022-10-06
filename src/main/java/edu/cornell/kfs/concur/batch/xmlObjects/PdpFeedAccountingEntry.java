package edu.cornell.kfs.concur.batch.xmlObjects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.sys.xmladapters.KualiDecimalXmlAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "coaCd",
    "accountNbr",
    "subAccountNbr",
    "objectCd",
    "subObjectCd",
    "orgRefId",
    "projectCd",
    "amount"
})
@XmlRootElement(name = "accounting", namespace = ConcurConstants.PDP_XML_NAMESPACE)
public class PdpFeedAccountingEntry {

    @XmlElement(name = "coa_cd", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String coaCd;
    @XmlElement(name = "account_nbr", namespace = ConcurConstants.PDP_XML_NAMESPACE, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String accountNbr;
    @XmlElement(name = "sub_account_nbr", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String subAccountNbr;
    @XmlElement(name = "object_cd", namespace = ConcurConstants.PDP_XML_NAMESPACE, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String objectCd;
    @XmlElement(name = "sub_object_cd", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String subObjectCd;
    @XmlElement(name = "org_ref_id", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String orgRefId;
    @XmlElement(name = "project_cd", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String projectCd;
    @XmlElement(namespace = ConcurConstants.PDP_XML_NAMESPACE, required = true)
    @XmlSchemaType(name = "normalizedString")
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal amount;

    public String getCoaCd() {
        return coaCd;
    }

    public void setCoaCd(String value) {
        this.coaCd = value;
    }

    public String getAccountNbr() {
        return accountNbr;
    }

    public void setAccountNbr(String value) {
        this.accountNbr = value;
    }

    public String getSubAccountNbr() {
        return subAccountNbr;
    }

    public void setSubAccountNbr(String value) {
        this.subAccountNbr = value;
    }

    public String getObjectCd() {
        return objectCd;
    }

    public void setObjectCd(String value) {
        this.objectCd = value;
    }

    public String getSubObjectCd() {
        return subObjectCd;
    }

    public void setSubObjectCd(String value) {
        this.subObjectCd = value;
    }

    public String getOrgRefId() {
        return orgRefId;
    }

    public void setOrgRefId(String value) {
        this.orgRefId = value;
    }

    public String getProjectCd() {
        return projectCd;
    }

    public void setProjectCd(String value) {
        this.projectCd = value;
    }

    public KualiDecimal getAmount() {
        return amount;
    }

    public void setAmount(KualiDecimal value) {
        this.amount = value;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder("Chart: ").append(coaCd).append(" Account: ").append(accountNbr);
        sb.append(" Sub Account: ").append(subAccountNbr).append(" Object: ").append(objectCd);
        sb.append(" Sub Object: ").append(subObjectCd).append(" project: ").append(projectCd);
        sb.append(" Org Ref Id: ").append(orgRefId).append(" Amount: ").append(amount);
        return sb.toString();
    }

}
