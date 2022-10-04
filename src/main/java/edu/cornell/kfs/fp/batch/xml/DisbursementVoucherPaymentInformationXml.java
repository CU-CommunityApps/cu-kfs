package edu.cornell.kfs.fp.batch.xml;

import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.xmladapters.KualiDecimalXmlAdapter;
import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "payment_information", namespace = StringUtils.EMPTY)
public class DisbursementVoucherPaymentInformationXml {
    
    @XmlElement(name = "payment_reason_code", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String paymentReasonCode;
    
    @XmlElement(name = "payee_id", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String payeeId;
    
    @XmlElement(name = "payee_address_id", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String payeeAddressId;
    
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
    
    @XmlElement(name = "attachment_code", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String attachmentCode;
    
    @XmlElement(name = "special_handling_code", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String specialHandlingCode;
    
    @XmlElement(name = "w9_complete_code", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String w9CompleteCode;
    
    @XmlElement(name = "exceptions_attached_code", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String exceptionAttachedCode;
    
    @XmlElement(name = "special_handling_person_name", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String specialHandlingName;
    
    @XmlElement(name = "special_handling_address_line_1", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String specialHandlingAddress1;
    
    @XmlElement(name = "special_handling_address_line_2", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String specialHandlingAddress2;
    
    @XmlElement(name = "special_handling_city", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String specialHandlingCity;
    
    @XmlElement(name = "special_handling_state", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String specialHandlingState;
    
    @XmlElement(name = "special_handling_zip", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String specialHandlingZip;
    
    @XmlElement(name = "special_handling_country", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String specialHandlingCountry;
    
    @XmlElement(name = "invoice_date", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(StringToJavaDateAdapter.class)
    protected Date invoiceDate;
    
    @XmlElement(name = "invoice_number", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String invoiceNumber;

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

    public String getPayeeAddressId() {
        return payeeAddressId;
    }

    public void setPayeeAddressId(String payeeAddressId) {
        this.payeeAddressId = payeeAddressId;
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

    public String getAttachmentCode() {
        return attachmentCode;
    }

    public void setAttachmentCode(String attachmentCode) {
        this.attachmentCode = attachmentCode;
    }

    public String getSpecialHandlingCode() {
        return specialHandlingCode;
    }

    public void setSpecialHandlingCode(String specialHandlingCode) {
        this.specialHandlingCode = specialHandlingCode;
    }

    public String getW9CompleteCode() {
        return w9CompleteCode;
    }

    public void setW9CompleteCode(String w9CompleteCode) {
        this.w9CompleteCode = w9CompleteCode;
    }

    public String getExceptionAttachedCode() {
        return exceptionAttachedCode;
    }

    public void setExceptionAttachedCode(String exceptionAttachedCode) {
        this.exceptionAttachedCode = exceptionAttachedCode;
    }

    public String getSpecialHandlingName() {
        return specialHandlingName;
    }

    public void setSpecialHandlingName(String specialHandlingName) {
        this.specialHandlingName = specialHandlingName;
    }

    public String getSpecialHandlingAddress1() {
        return specialHandlingAddress1;
    }

    public void setSpecialHandlingAddress1(String specialHandlingAddress1) {
        this.specialHandlingAddress1 = specialHandlingAddress1;
    }

    public String getSpecialHandlingAddress2() {
        return specialHandlingAddress2;
    }

    public void setSpecialHandlingAddress2(String specialHandlingAddress2) {
        this.specialHandlingAddress2 = specialHandlingAddress2;
    }

    public String getSpecialHandlingCity() {
        return specialHandlingCity;
    }

    public void setSpecialHandlingCity(String specialHandlingCity) {
        this.specialHandlingCity = specialHandlingCity;
    }

    public String getSpecialHandlingState() {
        return specialHandlingState;
    }

    public void setSpecialHandlingState(String specialHandlingState) {
        this.specialHandlingState = specialHandlingState;
    }

    public String getSpecialHandlingZip() {
        return specialHandlingZip;
    }

    public void setSpecialHandlingZip(String specialHandlingZip) {
        this.specialHandlingZip = specialHandlingZip;
    }

    public String getSpecialHandlingCountry() {
        return specialHandlingCountry;
    }

    public void setSpecialHandlingCountry(String specialHandlingCountry) {
        this.specialHandlingCountry = specialHandlingCountry;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
    
}
