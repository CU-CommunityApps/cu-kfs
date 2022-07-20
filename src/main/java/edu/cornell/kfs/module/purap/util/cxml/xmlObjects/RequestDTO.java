package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "typedRequests"
})
@XmlRootElement(name = "Request")
public class RequestDTO {

    @XmlAttribute(name = "deploymentMode")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String deploymentMode;

    @XmlAttribute(name = "Id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    private String id;

    @XmlElements({
        @XmlElement(name = "ProfileRequest", required = true, type = IgnoredElementDTO.class),
        @XmlElement(name = "OrderRequest", required = true, type = IgnoredElementDTO.class),
        @XmlElement(name = "MasterAgreementRequest", required = true, type = IgnoredElementDTO.class),
        @XmlElement(name = "PunchOutSetupRequest", required = true, type = PunchOutSetupRequestDTO.class),
        @XmlElement(name = "ProviderSetupRequest", required = true, type = IgnoredElementDTO.class),
        @XmlElement(name = "StatusUpdateRequest", required = true, type = IgnoredElementDTO.class),
        @XmlElement(name = "GetPendingRequest", required = true, type = IgnoredElementDTO.class),
        @XmlElement(name = "SubscriptionListRequest", required = true, type = IgnoredElementDTO.class),
        @XmlElement(name = "SubscriptionContentRequest", required = true, type = IgnoredElementDTO.class),
        @XmlElement(name = "SupplierListRequest", required = true, type = IgnoredElementDTO.class),
        @XmlElement(name = "SupplierDataRequest", required = true, type = IgnoredElementDTO.class),
        @XmlElement(name = "CopyRequest", required = true, type = IgnoredElementDTO.class),
        @XmlElement(name = "CatalogUploadRequest", required = true, type = IgnoredElementDTO.class),
        @XmlElement(name = "AuthRequest", required = true, type = IgnoredElementDTO.class),
        @XmlElement(name = "DataRequest", required = true, type = IgnoredElementDTO.class),
        @XmlElement(name = "OrganizationDataRequest", required = true, type = IgnoredElementDTO.class)
    })
    private List<Object> typedRequests;

    public String getDeploymentMode() {
        if (deploymentMode == null) {
            return "production";
        } else {
            return deploymentMode;
        }
    }

    public void setDeploymentMode(String deploymentMode) {
        this.deploymentMode = deploymentMode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Object> getTypedRequests() {
        return typedRequests;
    }

    public void setTypedRequests(List<Object> typedRequests) {
        this.typedRequests = typedRequests;
    }

}
