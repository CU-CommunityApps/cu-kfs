
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "supplierUsesCXML", "cxmlEnabled", "cxmlFormatName", "cxmlTemplate", "cxmlMaxLines",
        "externalSystem", "internalSystem", "cxmlHeader", "connectionTimeout", "responseTimeout", "maxConnections",
        "maxRetries", "supplierURL", "failureContactEmail", "basicAuth" })
@XmlRootElement(name = "POConfiguration")
public class POConfiguration {

    @XmlElement(name = "SupplierUsesCXML")
    protected SupplierUsesCXML supplierUsesCXML;
    @XmlElement(name = "CXMLEnabled")
    protected CXMLEnabled cxmlEnabled;
    @XmlElement(name = "CXMLFormatName")
    protected CXMLFormatName cxmlFormatName;
    @XmlElement(name = "CXMLTemplate")
    protected CXMLTemplate cxmlTemplate;
    @XmlElement(name = "CXMLMaxLines")
    protected CXMLMaxLines cxmlMaxLines;
    @XmlElement(name = "ExternalSystem")
    protected ExternalSystem externalSystem;
    @XmlElement(name = "InternalSystem")
    protected InternalSystem internalSystem;
    @XmlElement(name = "CXMLHeader")
    protected CXMLHeader cxmlHeader;
    @XmlElement(name = "ConnectionTimeout")
    protected ConnectionTimeout connectionTimeout;
    @XmlElement(name = "ResponseTimeout")
    protected ResponseTimeout responseTimeout;
    @XmlElement(name = "MaxConnections")
    protected MaxConnections maxConnections;
    @XmlElement(name = "MaxRetries")
    protected MaxRetries maxRetries;
    @XmlElement(name = "SupplierURL")
    protected SupplierURL supplierURL;
    @XmlElement(name = "FailureContactEmail")
    protected FailureContactEmail failureContactEmail;
    @XmlElement(name = "BasicAuth")
    protected BasicAuth basicAuth;

    public SupplierUsesCXML getSupplierUsesCXML() {
        return supplierUsesCXML;
    }

    public void setSupplierUsesCXML(SupplierUsesCXML value) {
        this.supplierUsesCXML = value;
    }

    public CXMLEnabled getCXMLEnabled() {
        return cxmlEnabled;
    }

    public void setCXMLEnabled(CXMLEnabled value) {
        this.cxmlEnabled = value;
    }

    public CXMLFormatName getCXMLFormatName() {
        return cxmlFormatName;
    }

    public void setCXMLFormatName(CXMLFormatName value) {
        this.cxmlFormatName = value;
    }

    public CXMLTemplate getCXMLTemplate() {
        return cxmlTemplate;
    }

    public void setCXMLTemplate(CXMLTemplate value) {
        this.cxmlTemplate = value;
    }

    public CXMLMaxLines getCXMLMaxLines() {
        return cxmlMaxLines;
    }

    public void setCXMLMaxLines(CXMLMaxLines value) {
        this.cxmlMaxLines = value;
    }

    public ExternalSystem getExternalSystem() {
        return externalSystem;
    }

    public void setExternalSystem(ExternalSystem value) {
        this.externalSystem = value;
    }

    public InternalSystem getInternalSystem() {
        return internalSystem;
    }

    public void setInternalSystem(InternalSystem value) {
        this.internalSystem = value;
    }

    public CXMLHeader getCXMLHeader() {
        return cxmlHeader;
    }

    public void setCXMLHeader(CXMLHeader value) {
        this.cxmlHeader = value;
    }

    public ConnectionTimeout getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(ConnectionTimeout value) {
        this.connectionTimeout = value;
    }

    public ResponseTimeout getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(ResponseTimeout value) {
        this.responseTimeout = value;
    }

    public MaxConnections getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(MaxConnections value) {
        this.maxConnections = value;
    }

    public MaxRetries getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(MaxRetries value) {
        this.maxRetries = value;
    }

    public SupplierURL getSupplierURL() {
        return supplierURL;
    }

    public void setSupplierURL(SupplierURL value) {
        this.supplierURL = value;
    }

    public FailureContactEmail getFailureContactEmail() {
        return failureContactEmail;
    }

    public void setFailureContactEmail(FailureContactEmail value) {
        this.failureContactEmail = value;
    }

    public BasicAuth getBasicAuth() {
        return basicAuth;
    }

    public void setBasicAuth(BasicAuth value) {
        this.basicAuth = value;
    }

}
