package edu.cornell.kfs.fp.batch.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dv_detail", namespace = StringUtils.EMPTY)
public class DisbursementVoucherDetailXml {
    
    @XmlElement(name = "contact_name", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String contactName;
    
    @XmlElement(name = "contact_phone", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String contactPhoneNumber;
    
    @XmlElement(name = "contact_email", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String contactEmail;
    
    @XmlElement(name = "campus_code", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String campusCode;
    
    @XmlElement(name = "bank_code", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String bankCode;
    
    @XmlElement(name = "payment_information", namespace = StringUtils.EMPTY, required = false)
    protected DisbursementVoucherPaymentInformationXml paymentInformation;
    
    @XmlElement(name = "pre_paid_travel", namespace = StringUtils.EMPTY, required = false)
    protected DisbursementVoucherPrePaidTravelOverviewXml prePaidTravelOverview;
    
    public DisbursementVoucherPaymentInformationXml getPaymentInformation() {
        return paymentInformation;
    }

    public void setPaymentInformation(DisbursementVoucherPaymentInformationXml paymentInformation) {
        this.paymentInformation = paymentInformation;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhoneNumber() {
        return contactPhoneNumber;
    }

    public void setContactPhoneNumber(String contactPhoneNumber) {
        this.contactPhoneNumber = contactPhoneNumber;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getCampusCode() {
        return campusCode;
    }

    public void setCampusCode(String campusCode) {
        this.campusCode = campusCode;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public DisbursementVoucherPrePaidTravelOverviewXml getPrePaidTravelOverview() {
        return prePaidTravelOverview;
    }

    public void setPrePaidTravelOverview(DisbursementVoucherPrePaidTravelOverviewXml prePaidTravelOverview) {
        this.prePaidTravelOverview = prePaidTravelOverview;
    }

}
