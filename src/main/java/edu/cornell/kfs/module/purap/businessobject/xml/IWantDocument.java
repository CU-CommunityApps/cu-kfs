package edu.cornell.kfs.module.purap.businessobject.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "initiator", "sourceNumber", "businessPurpose", "collegeLevelOrganization",
        "departmentLevelOrganization", "requestorNetID", "requestorEmailAddress", "requestorPhoneNumber",
        "requestorAddress", "sameAsRequestor", "deliverToNetID", "deliverToEmailAddress", "deliverToPhoneNumber",
        "deliverToAddress", "vendorId", "vendorName", "vendorDescription", "item", "account", "accountDescriptionTxt",
        "commentsAndSpecialInstructions", "goods", "servicePerformedOnCampus", "adHocRouteToNetID", "note",
        "attachment" })
@XmlRootElement(name = "iWantDocument")
public class IWantDocument {

    @XmlElement(required = true)
    private String initiator;
    @XmlElement(required = true)
    private String sourceNumber;
    @XmlElement(required = true)
    private String businessPurpose;
    @XmlElement(required = true)
    private String collegeLevelOrganization;
    @XmlElement(required = true)
    private String departmentLevelOrganization;
    private String requestorNetID;
    private String requestorEmailAddress;
    private String requestorPhoneNumber;
    private String requestorAddress;
    @XmlSchemaType(name = "string")
    private String sameAsRequestor;
    private String deliverToNetID;
    private String deliverToEmailAddress;
    private String deliverToPhoneNumber;
    private String deliverToAddress;
    private String vendorId;
    private String vendorName;
    private String vendorDescription;
    private List<Item> item;
    private List<Account> account;
    private String accountDescriptionTxt;
    private String commentsAndSpecialInstructions;
    @XmlSchemaType(name = "string")
    private String goods;
    @XmlSchemaType(name = "string")
    private String servicePerformedOnCampus;
    private String adHocRouteToNetID;
    private List<Note> note;
    private List<Attachment> attachment;

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public String getSourceNumber() {
        return sourceNumber;
    }

    public void setSourceNumber(String sourceNumber) {
        this.sourceNumber = sourceNumber;
    }

    public String getBusinessPurpose() {
        return businessPurpose;
    }

    public void setBusinessPurpose(String businessPurpose) {
        this.businessPurpose = businessPurpose;
    }

    public String getCollegeLevelOrganization() {
        return collegeLevelOrganization;
    }

    public void setCollegeLevelOrganization(String collegeLevelOrganization) {
        this.collegeLevelOrganization = collegeLevelOrganization;
    }

    public String getDepartmentLevelOrganization() {
        return departmentLevelOrganization;
    }

    public void setDepartmentLevelOrganization(String departmentLevelOrganization) {
        this.departmentLevelOrganization = departmentLevelOrganization;
    }

    public String getRequestorNetID() {
        return requestorNetID;
    }

    public void setRequestorNetID(String requestorNetID) {
        this.requestorNetID = requestorNetID;
    }

    public String getRequestorEmailAddress() {
        return requestorEmailAddress;
    }

    public void setRequestorEmailAddress(String requestorEmailAddress) {
        this.requestorEmailAddress = requestorEmailAddress;
    }

    public String getRequestorPhoneNumber() {
        return requestorPhoneNumber;
    }

    public void setRequestorPhoneNumber(String requestorPhoneNumber) {
        this.requestorPhoneNumber = requestorPhoneNumber;
    }

    public String getRequestorAddress() {
        return requestorAddress;
    }

    public void setRequestorAddress(String requestorAddress) {
        this.requestorAddress = requestorAddress;
    }

    public String getSameAsRequestor() {
        return sameAsRequestor;
    }

    public void setSameAsRequestor(String sameAsRequestor) {
        this.sameAsRequestor = sameAsRequestor;
    }

    public String getDeliverToNetID() {
        return deliverToNetID;
    }

    public void setDeliverToNetID(String deliverToNetID) {
        this.deliverToNetID = deliverToNetID;
    }

    public String getDeliverToEmailAddress() {
        return deliverToEmailAddress;
    }

    public void setDeliverToEmailAddress(String deliverToEmailAddress) {
        this.deliverToEmailAddress = deliverToEmailAddress;
    }

    public String getDeliverToPhoneNumber() {
        return deliverToPhoneNumber;
    }

    public void setDeliverToPhoneNumber(String deliverToPhoneNumber) {
        this.deliverToPhoneNumber = deliverToPhoneNumber;
    }

    public String getDeliverToAddress() {
        return deliverToAddress;
    }

    public void setDeliverToAddress(String deliverToAddress) {
        this.deliverToAddress = deliverToAddress;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorDescription() {
        return vendorDescription;
    }

    public void setVendorDescription(String vendorDescription) {
        this.vendorDescription = vendorDescription;
    }

    public List<Item> getItem() {
        if (item == null) {
            item = new ArrayList<Item>();
        }
        return item;
    }

    public List<Account> getAccount() {
        if (account == null) {
            account = new ArrayList<Account>();
        }
        return account;
    }

    public String getAccountDescriptionTxt() {
        return accountDescriptionTxt;
    }

    public void setAccountDescriptionTxt(String accountDescriptionTxt) {
        this.accountDescriptionTxt = accountDescriptionTxt;
    }

    public String getCommentsAndSpecialInstructions() {
        return commentsAndSpecialInstructions;
    }

    public void setCommentsAndSpecialInstructions(String commentsAndSpecialInstructions) {
        this.commentsAndSpecialInstructions = commentsAndSpecialInstructions;
    }

    public String getGoods() {
        return goods;
    }

    public void setGoods(String goods) {
        this.goods = goods;
    }

    public String getServicePerformedOnCampus() {
        return servicePerformedOnCampus;
    }

    public void setServicePerformedOnCampus(String servicePerformedOnCampus) {
        this.servicePerformedOnCampus = servicePerformedOnCampus;
    }

    public String getAdHocRouteToNetID() {
        return adHocRouteToNetID;
    }

    public void setAdHocRouteToNetID(String adHocRouteToNetID) {
        this.adHocRouteToNetID = adHocRouteToNetID;
    }

    public List<Note> getNote() {
        return note;
    }

    public void setNote(List<Note> note) {
        this.note = note;
    }

    public List<Attachment> getAttachment() {
        if (attachment == null) {
            attachment = new ArrayList<Attachment>();
        }
        return attachment;
    }

    @Override
    public String toString() {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        return builder.build();
    }

}
