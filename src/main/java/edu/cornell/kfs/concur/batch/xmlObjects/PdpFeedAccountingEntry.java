package edu.cornell.kfs.concur.batch.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
@XmlRootElement(name = "accounting", namespace = "http://www.kuali.org/kfs/pdp/payment")
public class PdpFeedAccountingEntry {

    @XmlElement(name = "coa_cd", namespace = "http://www.kuali.org/kfs/pdp/payment")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String coaCd;
    @XmlElement(name = "account_nbr", namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String accountNbr;
    @XmlElement(name = "sub_account_nbr", namespace = "http://www.kuali.org/kfs/pdp/payment")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String subAccountNbr;
    @XmlElement(name = "object_cd", namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String objectCd;
    @XmlElement(name = "sub_object_cd", namespace = "http://www.kuali.org/kfs/pdp/payment")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String subObjectCd;
    @XmlElement(name = "org_ref_id", namespace = "http://www.kuali.org/kfs/pdp/payment")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String orgRefId;
    @XmlElement(name = "project_cd", namespace = "http://www.kuali.org/kfs/pdp/payment")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String projectCd;
    @XmlElement(namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String amount;

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

    public String getAmount() {
        return amount;
    }

    public void setAmount(String value) {
        this.amount = value;
    }

}
