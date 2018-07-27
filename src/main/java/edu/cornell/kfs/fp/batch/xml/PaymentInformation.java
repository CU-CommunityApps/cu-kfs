package edu.cornell.kfs.fp.batch.xml;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.xmladapters.KualiDecimalXmlAdapter;
import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "PaymentInformation", namespace = StringUtils.EMPTY)
public class PaymentInformation {
    
    @XmlElement(name = "payment_reason_code", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String paymentReasonCode;
    
    @XmlElement(name = "payee_id", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String payeeId;
    
    @XmlElement(name = "payee_type_code", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String payeeTypeCode;
    
    @XmlElement(name = "payee_name", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String payeeName;
    
    @XmlElement(name = "address_line_1", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String addressLine1;
    
    @XmlElement(name = "address_line_2", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String addressLine2;
    
    @XmlElement(name = "city", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String city;
    
    @XmlElement(name = "state", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String state;
    
    @XmlElement(name = "country", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String country;
    
    @XmlElement(name = "postal_code", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String postalCode;
    
    @XmlElement(name = "check_amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal checkAmount;
    
    @XmlElement(name = "due_date", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(StringToJavaDateAdapter.class)
    protected Date dueDate;
    
    @XmlElement(name = "payment_method", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String paymentMethod;
    
    @XmlElement(name = "documentation_location_code", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String documentationLocationCode;
    
    @XmlElement(name = "check_stub_text", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String checkStubText;

    public String getPaymentReasonCode() {
        return paymentReasonCode;
    }

    public void setPaymentReasonCode(String paymentReasonCode) {
        this.paymentReasonCode = paymentReasonCode;
    }

    public String getPayeeId() {
        return payeeId;
    }

    public void setPayeeId(String payeeId) {
        this.payeeId = payeeId;
    }

    public String getPayeeTypeCode() {
        return payeeTypeCode;
    }

    public void setPayeeTypeCode(String payeeTypeCode) {
        this.payeeTypeCode = payeeTypeCode;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public KualiDecimal getCheckAmount() {
        return checkAmount;
    }

    public void setCheckAmount(KualiDecimal checkAmount) {
        this.checkAmount = checkAmount;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDocumentationLocationCode() {
        return documentationLocationCode;
    }

    public void setDocumentationLocationCode(String documentationLocationCode) {
        this.documentationLocationCode = documentationLocationCode;
    }

    public String getCheckStubText() {
        return checkStubText;
    }

    public void setCheckStubText(String checkStubText) {
        this.checkStubText = checkStubText;
    }
    
}
