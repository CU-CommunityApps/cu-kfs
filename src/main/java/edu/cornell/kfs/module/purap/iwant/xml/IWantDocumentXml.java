package edu.cornell.kfs.module.purap.iwant.xml;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.kuali.kfs.kew.xml.BooleanJaxbAdapter;

import edu.cornell.kfs.module.purap.document.BatchIWantDocument;
import edu.cornell.kfs.module.purap.iwant.xml.IWantXmlConstants.IWantIndicatorTypeXml;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "initiator", "sourceNumber", "businessPurpose", "collegeLevelOrganization",
        "departmentLevelOrganization", "requestorNetID", "requestorEmailAddress", "requestorPhoneNumber",
        "requestorAddress", "sameAsRequestor", "deliverToNetID", "deliverToEmailAddress", "deliverToPhoneNumber",
        "deliverToAddress", "vendorId", "vendorName", "vendorDescription", "items", "transactionLines",
        "accountDescriptionTxt", "commentsAndSpecialInstructions", "goods", "servicePerformedOnCampus",
        "adHocRouteToNetID", "notes" })
@XmlRootElement(name = "iWantDocument", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
public class IWantDocumentXml {

    @XmlElement(name = "initiator", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE, required = true)
    private String initiator;

    @XmlElement(name = "sourceNumber", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE, required = true)
    private String sourceNumber;

    @XmlElement(name = "businessPurpose", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE, required = true)
    private String businessPurpose;

    @XmlElement(name = "collegeLevelOrganization", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE, required = true)
    private String collegeLevelOrganization;

    @XmlElement(name = "departmentLevelOrganization", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE, required = true)
    private String departmentLevelOrganization;

    @XmlElement(name = "requestorNetID", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String requestorNetID;

    @XmlElement(name = "requestorEmailAddress", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String requestorEmailAddress;

    @XmlElement(name = "requestorPhoneNumber", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String requestorPhoneNumber;

    @XmlElement(name = "requestorAddress", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String requestorAddress;

    @XmlElement(name = "sameAsRequestor", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    @XmlJavaTypeAdapter(BooleanJaxbAdapter.class)
    private Boolean sameAsRequestor;

    @XmlElement(name = "deliverToNetID", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String deliverToNetID;

    @XmlElement(name = "deliverToEmailAddress", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String deliverToEmailAddress;

    @XmlElement(name = "deliverToPhoneNumber", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String deliverToPhoneNumber;

    @XmlElement(name = "deliverToAddress", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String deliverToAddress;

    @XmlElement(name = "vendorId", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String vendorId;

    @XmlElement(name = "vendorName", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String vendorName;

    @XmlElement(name = "vendorDescription", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String vendorDescription;

    @XmlElement(name = "item", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private List<IWantItemXml> items;

    @XmlElement(name = "account", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private List<IWantTransactionLineXml> transactionLines;

    @XmlElement(name = "accountDescriptionTxt", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String accountDescriptionTxt;

    @XmlElement(name = "commentsAndSpecialInstructions", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String commentsAndSpecialInstructions;

    @XmlElement(name = "goods", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    @XmlJavaTypeAdapter(BooleanJaxbAdapter.class)
    private Boolean goods;

    @XmlElement(name = "servicePerformedOnCampus", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    @XmlJavaTypeAdapter(BooleanJaxbAdapter.class)
    private Boolean servicePerformedOnCampus;

    @XmlElement(name = "adHocRouteToNetID", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String adHocRouteToNetID;

    @XmlElement(name = "note", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private List<IWantNoteXml> notes;

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

    public Boolean getSameAsRequestor() {
        return sameAsRequestor;
    }

    public void setSameAsRequestor(Boolean sameAsRequestor) {
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

    public Boolean getGoods() {
        return goods;
    }

    public void setGoods(Boolean goods) {
        this.goods = goods;
    }

    public Boolean getServicePerformedOnCampus() {
        return servicePerformedOnCampus;
    }

    public void setServicePerformedOnCampus(Boolean servicePerformedOnCampus) {
        this.servicePerformedOnCampus = servicePerformedOnCampus;
    }

    public String getAdHocRouteToNetID() {
        return adHocRouteToNetID;
    }

    public void setAdHocRouteToNetID(String adHocRouteToNetID) {
        this.adHocRouteToNetID = adHocRouteToNetID;
    }

    public List<IWantItemXml> getItems() {
        if (items == null) {
            items = new ArrayList<IWantItemXml>();
        }
        return items;
    }

    public List<IWantTransactionLineXml> getTransactionLines() {
        if (transactionLines == null) {
            transactionLines = new ArrayList<IWantTransactionLineXml>();
        }
        return transactionLines;
    }

    public List<IWantNoteXml> getNotes() {
        if (notes == null) {
            notes = new ArrayList<IWantNoteXml>();
        }
        return notes;
    }
    
    public BatchIWantDocument toBatchIWantDocument() {
        BatchIWantDocument doc = new BatchIWantDocument();
        
        doc.setInitiator(initiator);
        doc.setSourceNumber(sourceNumber);
        doc.setBusinessPurpose(businessPurpose);
        doc.setCollegeLevelOrganization(collegeLevelOrganization);
        doc.setDepartmentLevelOrganization(departmentLevelOrganization);
        doc.setInitiatorNetID(requestorNetID);
        doc.setInitiatorEmailAddress(requestorEmailAddress);
        doc.setInitiatorPhoneNumber(requestorPhoneNumber);
        doc.setInitiatorAddress(requestorAddress);
        doc.setSameAsInitiator(sameAsRequestor);
        doc.setDeliverToNetID(deliverToNetID);
        doc.setDeliverToEmailAddress(deliverToEmailAddress);
        doc.setDeliverToPhoneNumber(deliverToPhoneNumber);
        doc.setDeliverToAddress(deliverToAddress);
        doc.setVendorNumber(vendorId);
        doc.setVendorName(vendorName);
        doc.setVendorDescription(vendorDescription);
        doc.setAccountDescriptionTxt(accountDescriptionTxt);
        doc.setCommentsAndSpecialInstructions(commentsAndSpecialInstructions);
        doc.setGoods(goods);
        doc.setServicePerformedOnCampus(IWantIndicatorTypeXml.fromBoolean(servicePerformedOnCampus));
        doc.setCurrentRouteToNetId(adHocRouteToNetID);
        
        for (IWantItemXml item : getItems()) {
            doc.getItems().add(item.toBatchIWantItem());
        }
        
        for (IWantTransactionLineXml transaction : getTransactionLines()) {
            doc.getAccounts().add(transaction.toBatchIWantAccount());
        }
        
        for (IWantNoteXml note : getNotes()) {
            doc.getNotes().add(note.toNote());
        }
        
        return doc;
    }

    @Override
    public String toString() {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        return builder.build();
    }
    
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
