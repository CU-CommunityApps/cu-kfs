package edu.cornell.kfs.module.purap.businessobject.xml.fixture;

import edu.cornell.kfs.module.purap.businessobject.xml.IWantDocumentXml;
import edu.cornell.kfs.module.purap.businessobject.xml.IWantXmlConstants.IWantIndicatorTypeXml;

public enum IWantDocumentFixture {
    FULL_EXAMPLE("ccs1", "source number", "business purpose", 
                "college org", "department org", "jdh34", 
                "jdh34@cornell.edu", "6072559900", "req address",
                IWantIndicatorTypeXml.N, "ccs1", "ccs1@cornell.edu",
                "607-255-9900", "deliver address", "vendor id", "vendor name",
                "vendor description", "account description", "special instructions",
                IWantIndicatorTypeXml.Y, IWantIndicatorTypeXml.Y, "se12");
    
    public final String initiator;
    public final String sourceNumber;
    public final String businessPurpose;
    public final String collegeLevelOrganization;
    public final String departmentLevelOrganization;
    public final String requestorNetID;
    public final String requestorEmailAddress;
    public final String requestorPhoneNumber;
    public final String requestorAddress;
    public final IWantIndicatorTypeXml sameAsRequestor;
    public final String deliverToNetID;
    public final String deliverToEmailAddress;
    public final String deliverToPhoneNumber;
    public final String deliverToAddress;
    public final String vendorId;
    public final String vendorName;
    public final String vendorDescription;
    public final String accountDescriptionTxt;
    public final String commentsAndSpecialInstructions;
    public final IWantIndicatorTypeXml goods;
    public final IWantIndicatorTypeXml servicePerformedOnCampus;
    public final String adHocRouteToNetID;
    
    private IWantDocumentFixture(String initiator, String sourceNumber, String businessPurpose,
            String collegeLevelOrganization, String departmentLevelOrganization, String requestorNetID,
            String requestorEmailAddress, String requestorPhoneNumber, String requestorAddress,
            IWantIndicatorTypeXml sameAsRequestor, String deliverToNetID, String deliverToEmailAddress,
            String deliverToPhoneNumber, String deliverToAddress, String vendorId, String vendorName,
            String vendorDescription, String accountDescriptionTxt, String commentsAndSpecialInstructions, 
            IWantIndicatorTypeXml goods, IWantIndicatorTypeXml servicePerformedOnCampus, String adHocRouteToNetID) {
        this.initiator = initiator;
        this.sourceNumber = sourceNumber;
        this.businessPurpose = businessPurpose;
        this.collegeLevelOrganization = collegeLevelOrganization;
        this.departmentLevelOrganization = departmentLevelOrganization;
        this.requestorNetID = requestorNetID;
        this.requestorEmailAddress = requestorEmailAddress;
        this.requestorPhoneNumber = requestorPhoneNumber;
        this.requestorAddress = requestorAddress;
        this.sameAsRequestor = sameAsRequestor;
        this.deliverToNetID = deliverToNetID;
        this.deliverToEmailAddress = deliverToEmailAddress;
        this.deliverToPhoneNumber = deliverToPhoneNumber;
        this.deliverToAddress = deliverToAddress;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.vendorDescription = vendorDescription;
        this.accountDescriptionTxt = accountDescriptionTxt;
        this.commentsAndSpecialInstructions = commentsAndSpecialInstructions;
        this.goods = goods;
        this.servicePerformedOnCampus = servicePerformedOnCampus;
        this.adHocRouteToNetID = adHocRouteToNetID;
    }
    
    public IWantDocumentXml toIWantDocumentXml() {
        IWantDocumentXml doc = new IWantDocumentXml();
        doc.setInitiator(initiator);
        doc.setSourceNumber(sourceNumber);
        doc.setBusinessPurpose(businessPurpose);
        doc.setCollegeLevelOrganization(collegeLevelOrganization);
        doc.setDepartmentLevelOrganization(departmentLevelOrganization);
        doc.setRequestorNetID(requestorNetID);
        doc.setRequestorEmailAddress(requestorEmailAddress);
        doc.setRequestorPhoneNumber(requestorPhoneNumber);
        doc.setRequestorAddress(requestorAddress);
        doc.setSameAsRequestor(sameAsRequestor);
        doc.setDeliverToNetID(deliverToNetID);
        doc.setDeliverToAddress(deliverToAddress);
        doc.setDeliverToEmailAddress(deliverToEmailAddress);
        doc.setDeliverToPhoneNumber(deliverToPhoneNumber);
        doc.setVendorId(vendorId);
        doc.setVendorName(vendorName);
        doc.setVendorDescription(vendorDescription);
        doc.setAccountDescriptionTxt(accountDescriptionTxt);
        doc.setCommentsAndSpecialInstructions(commentsAndSpecialInstructions);
        doc.setGoods(goods);
        doc.setServicePerformedOnCampus(servicePerformedOnCampus);
        doc.setAdHocRouteToNetID(adHocRouteToNetID);
        return doc;
    }

}
