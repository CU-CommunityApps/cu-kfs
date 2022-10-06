package edu.cornell.kfs.concur.batch.xmlObjects;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import edu.cornell.kfs.concur.ConcurConstants;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "payeeName",
    "payeeId",
    "payeeOwnCd",
    "customerInstitutionIdentifier",
    "address1",
    "address2",
    "address3",
    "address4",
    "city",
    "state",
    "zip",
    "country",
    "campusAddressInd",
    "paymentDate",
    "attachmentInd",
    "immediateInd",
    "specialHandlingInd",
    "taxableInd",
    "nraInd",
    "combineGroupInd",
    "bankCode",
    "detail"
})
@XmlRootElement(name = "group", namespace = ConcurConstants.PDP_XML_NAMESPACE)
public class PdpFeedGroupEntry {

    @XmlElement(name = "payee_name", namespace = ConcurConstants.PDP_XML_NAMESPACE, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String payeeName;
    @XmlElement(name = "payee_id", namespace = ConcurConstants.PDP_XML_NAMESPACE, required = true)
    protected PdpFeedPayeeIdEntry payeeId;
    @XmlElement(name = "payee_own_cd", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String payeeOwnCd;
    @XmlElement(name = "customer_institution_identifier", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String customerInstitutionIdentifier;
    @XmlElement(namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String address1;
    @XmlElement(namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String address2;
    @XmlElement(namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String address3;
    @XmlElement(namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String address4;
    @XmlElement(namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String city;
    @XmlElement(namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String state;
    @XmlElement(namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String zip;
    @XmlElement(namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String country;
    @XmlElement(name = "campus_address_ind", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String campusAddressInd;
    @XmlElement(name = "payment_date", namespace = ConcurConstants.PDP_XML_NAMESPACE, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String paymentDate;
    @XmlElement(name = "attachment_ind", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String attachmentInd;
    @XmlElement(name = "immediate_ind", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String immediateInd;
    @XmlElement(name = "special_handling_ind", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String specialHandlingInd;
    @XmlElement(name = "taxable_ind", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String taxableInd;
    @XmlElement(name = "nra_ind", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String nraInd;
    @XmlElement(name = "combine_group_ind", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String combineGroupInd;
    @XmlElement(name = "bank_code", namespace = ConcurConstants.PDP_XML_NAMESPACE)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String bankCode;
    @XmlElement(namespace = ConcurConstants.PDP_XML_NAMESPACE, required = true)
    protected List<PdpFeedDetailEntry> detail;

    public String getPayeeName() {
        return payeeName;
    }

    public void setPayeeName(String value) {
        this.payeeName = value;
    }

    public PdpFeedPayeeIdEntry getPayeeId() {
        return payeeId;
    }

    public void setPayeeId(PdpFeedPayeeIdEntry value) {
        this.payeeId = value;
    }

    public String getPayeeOwnCd() {
        return payeeOwnCd;
    }

    public void setPayeeOwnCd(String value) {
        this.payeeOwnCd = value;
    }

    public String getCustomerInstitutionIdentifier() {
        return customerInstitutionIdentifier;
    }

    public void setCustomerInstitutionIdentifier(String value) {
        this.customerInstitutionIdentifier = value;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String value) {
        this.address1 = value;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String value) {
        this.address2 = value;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String value) {
        this.address3 = value;
    }

    public String getAddress4() {
        return address4;
    }

    public void setAddress4(String value) {
        this.address4 = value;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String value) {
        this.city = value;
    }

    public String getState() {
        return state;
    }

    public void setState(String value) {
        this.state = value;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String value) {
        this.zip = value;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String value) {
        this.country = value;
    }

    public String getCampusAddressInd() {
        return campusAddressInd;
    }

    public void setCampusAddressInd(String value) {
        this.campusAddressInd = value;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String value) {
        this.paymentDate = value;
    }

    public String getAttachmentInd() {
        return attachmentInd;
    }

    public void setAttachmentInd(String value) {
        this.attachmentInd = value;
    }

    public String getImmediateInd() {
        return immediateInd;
    }

    public void setImmediateInd(String value) {
        this.immediateInd = value;
    }

    public String getSpecialHandlingInd() {
        return specialHandlingInd;
    }

    public void setSpecialHandlingInd(String value) {
        this.specialHandlingInd = value;
    }

    public String getTaxableInd() {
        return taxableInd;
    }

    public void setTaxableInd(String value) {
        this.taxableInd = value;
    }

    public String getNraInd() {
        return nraInd;
    }

    public void setNraInd(String value) {
        this.nraInd = value;
    }

    public String getCombineGroupInd() {
        return combineGroupInd;
    }

    public void setCombineGroupInd(String value) {
        this.combineGroupInd = value;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String value) {
        this.bankCode = value;
    }

    public List<PdpFeedDetailEntry> getDetail() {
        if (detail == null) {
            detail = new ArrayList<PdpFeedDetailEntry>();
        }
        return this.detail;
    }

    public void setDetail(List<PdpFeedDetailEntry> detail) {
        this.detail = detail;
    }

}
