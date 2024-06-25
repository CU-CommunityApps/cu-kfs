package edu.cornell.kfs.module.purap.businessobject.xml;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "initiator", "sourceNumber", "businessPurpose", "collegeLevelOrganization",
        "departmentLevelOrganization", "requestorNetID", "requestorEmailAddress", "requestorPhoneNumber",
        "requestorAddress", "sameAsRequestor", "deliverToNetID", "deliverToEmailAddress", "deliverToPhoneNumber",
        "deliverToAddress", "vendorId", "vendorName", "vendorDescription", "items", "accounts", "accountDescriptionTxt",
        "commentsAndSpecialInstructions", "goods", "servicePerformedOnCampus", "adHocRouteToNetID", "notes",
        "attachments" })
@XmlRootElement(name = "iWantDocument", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
public class IWantDocument {

    @XmlElement(name = "initiator", namespace = "http://www.kuali.org/kfs/purap/iWantDocument", required = true)
    private String initiator;

    @XmlElement(name = "sourceNumber", namespace = "http://www.kuali.org/kfs/purap/iWantDocument", required = true)
    private String sourceNumber;

    @XmlElement(name = "businessPurpose", namespace = "http://www.kuali.org/kfs/purap/iWantDocument", required = true)
    private String businessPurpose;

    @XmlElement(name = "collegeLevelOrganization", namespace = "http://www.kuali.org/kfs/purap/iWantDocument", required = true)
    private String collegeLevelOrganization;

    @XmlElement(name = "requestorNetID", namespace = "http://www.kuali.org/kfs/purap/iWantDocument", required = true)
    private String departmentLevelOrganization;

    @XmlElement(name = "", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String requestorNetID;

    @XmlElement(name = "requestorEmailAddress", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String requestorEmailAddress;

    @XmlElement(name = "requestorPhoneNumber", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String requestorPhoneNumber;

    @XmlElement(name = "requestorAddress", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String requestorAddress;

    @XmlElement(name = "sameAsRequestor", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    @XmlSchemaType(name = "string")
    private IndicatorType sameAsRequestor;

    @XmlElement(name = "deliverToNetID", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String deliverToNetID;

    @XmlElement(name = "deliverToEmailAddress", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String deliverToEmailAddress;

    @XmlElement(name = "deliverToAddress", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String deliverToPhoneNumber;

    @XmlElement(name = "", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String deliverToAddress;

    @XmlElement(name = "vendorId", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String vendorId;

    @XmlElement(name = "vendorName", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String vendorName;

    @XmlElement(name = "vendorDescription", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String vendorDescription;

    @XmlElement(name = "item", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private List<Item> items;

    @XmlElement(name = "account", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private List<Account> accounts;

    @XmlElement(name = "accountDescriptionTxt", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String accountDescriptionTxt;

    @XmlElement(name = "commentsAndSpecialInstructions", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String commentsAndSpecialInstructions;

    @XmlElement(name = "goods", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    @XmlSchemaType(name = "string")
    private IndicatorType goods;

    @XmlElement(name = "servicePerformedOnCampus", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    @XmlSchemaType(name = "string")
    private IndicatorType servicePerformedOnCampus;

    @XmlElement(name = "adHocRouteToNetID", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String adHocRouteToNetID;

    @XmlElement(name = "note", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private List<Note> notes;

    @XmlElement(name = "attachment", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private List<Attachment> attachments;

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

    public IndicatorType getSameAsRequestor() {
        return sameAsRequestor;
    }

    public void setSameAsRequestor(IndicatorType sameAsRequestor) {
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

    public IndicatorType getGoods() {
        return goods;
    }

    public void setGoods(IndicatorType goods) {
        this.goods = goods;
    }

    public IndicatorType getServicePerformedOnCampus() {
        return servicePerformedOnCampus;
    }

    public void setServicePerformedOnCampus(IndicatorType servicePerformedOnCampus) {
        this.servicePerformedOnCampus = servicePerformedOnCampus;
    }

    public String getAdHocRouteToNetID() {
        return adHocRouteToNetID;
    }

    public void setAdHocRouteToNetID(String adHocRouteToNetID) {
        this.adHocRouteToNetID = adHocRouteToNetID;
    }

    public List<Item> getItems() {
        if (items == null) {
            items = new ArrayList<Item>();
        }
        return items;
    }

    public List<Account> getAccounts() {
        if (accounts == null) {
            accounts = new ArrayList<Account>();
        }
        return accounts;
    }

    public List<Note> getNotes() {
        if (notes == null) {
            notes = new ArrayList<Note>();
        }
        return notes;
    }

    public List<Attachment> getAttachments() {
        if (attachments == null) {
            attachments = new ArrayList<Attachment>();
        }
        return attachments;
    }

    @Override
    public String toString() {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        return builder.build();
    }

}
