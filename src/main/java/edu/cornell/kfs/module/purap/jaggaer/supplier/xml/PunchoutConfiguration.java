
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "cxmlHeader", "punchoutContact", "extrinsicInfo", "supplierURL", "formPostURL",
        "buyerCookie", "defaultShipCode", "defaultShipToName", "shipToInfo", "catalog", "logoFileName", "jspTemplate",
        "vertical", "enableEditInspect", "allowEditPunchout", "allowOnePOPerPunchout", "allowMultiplePOWithinPunchout",
        "copyPunchoutValidation", "punchoutUsesSubframes", "forceSSLv3Protocol", "suppressPunchoutLogo",
        "multipleFulfillmentSupplierPunchoutConfig" })
@XmlRootElement(name = "PunchoutConfiguration")
public class PunchoutConfiguration {

    @XmlElement(name = "CXMLHeader")
    protected CXMLHeader cxmlHeader;
    @XmlElement(name = "PunchoutContact")
    protected PunchoutContact punchoutContact;
    @XmlElement(name = "ExtrinsicInfo")
    protected ExtrinsicInfo extrinsicInfo;
    @XmlElement(name = "SupplierURL")
    protected SupplierURL supplierURL;
    @XmlElement(name = "FormPostURL")
    protected FormPostURL formPostURL;
    @XmlElement(name = "BuyerCookie")
    protected BuyerCookie buyerCookie;
    @XmlElement(name = "DefaultShipCode")
    protected DefaultShipCode defaultShipCode;
    @XmlElement(name = "DefaultShipToName")
    protected DefaultShipToName defaultShipToName;
    @XmlElement(name = "ShipToInfo")
    protected ShipToInfo shipToInfo;
    @XmlElement(name = "Catalog")
    protected Catalog catalog;
    @XmlElement(name = "LogoFileName")
    protected LogoFileName logoFileName;
    @XmlElement(name = "JSPTemplate")
    protected JSPTemplate jspTemplate;
    @XmlElement(name = "Vertical")
    protected Vertical vertical;
    @XmlElement(name = "EnableEditInspect")
    protected EnableEditInspect enableEditInspect;
    @XmlElement(name = "AllowEditPunchout")
    protected AllowEditPunchout allowEditPunchout;
    @XmlElement(name = "AllowOnePOPerPunchout")
    protected AllowOnePOPerPunchout allowOnePOPerPunchout;
    @XmlElement(name = "AllowMultiplePOWithinPunchout")
    protected AllowMultiplePOWithinPunchout allowMultiplePOWithinPunchout;
    @XmlElement(name = "CopyPunchoutValidation")
    protected CopyPunchoutValidation copyPunchoutValidation;
    @XmlElement(name = "PunchoutUsesSubframes")
    protected PunchoutUsesSubframes punchoutUsesSubframes;
    @XmlElement(name = "ForceSSLv3Protocol")
    protected ForceSSLv3Protocol forceSSLv3Protocol;
    @XmlElement(name = "SuppressPunchoutLogo")
    protected SuppressPunchoutLogo suppressPunchoutLogo;
    @XmlElement(name = "MultipleFulfillmentSupplierPunchoutConfig")
    protected MultipleFulfillmentSupplierPunchoutConfig multipleFulfillmentSupplierPunchoutConfig;

    public CXMLHeader getCXMLHeader() {
        return cxmlHeader;
    }

    public void setCXMLHeader(CXMLHeader value) {
        this.cxmlHeader = value;
    }

    public PunchoutContact getPunchoutContact() {
        return punchoutContact;
    }

    public void setPunchoutContact(PunchoutContact value) {
        this.punchoutContact = value;
    }

    public ExtrinsicInfo getExtrinsicInfo() {
        return extrinsicInfo;
    }

    public void setExtrinsicInfo(ExtrinsicInfo value) {
        this.extrinsicInfo = value;
    }

    public SupplierURL getSupplierURL() {
        return supplierURL;
    }

    public void setSupplierURL(SupplierURL value) {
        this.supplierURL = value;
    }

    public FormPostURL getFormPostURL() {
        return formPostURL;
    }

    public void setFormPostURL(FormPostURL value) {
        this.formPostURL = value;
    }

    public BuyerCookie getBuyerCookie() {
        return buyerCookie;
    }

    public void setBuyerCookie(BuyerCookie value) {
        this.buyerCookie = value;
    }

    public DefaultShipCode getDefaultShipCode() {
        return defaultShipCode;
    }

    public void setDefaultShipCode(DefaultShipCode value) {
        this.defaultShipCode = value;
    }

    public DefaultShipToName getDefaultShipToName() {
        return defaultShipToName;
    }

    public void setDefaultShipToName(DefaultShipToName value) {
        this.defaultShipToName = value;
    }

    public ShipToInfo getShipToInfo() {
        return shipToInfo;
    }

    public void setShipToInfo(ShipToInfo value) {
        this.shipToInfo = value;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public void setCatalog(Catalog value) {
        this.catalog = value;
    }

    public LogoFileName getLogoFileName() {
        return logoFileName;
    }

    public void setLogoFileName(LogoFileName value) {
        this.logoFileName = value;
    }

    public JSPTemplate getJSPTemplate() {
        return jspTemplate;
    }

    public void setJSPTemplate(JSPTemplate value) {
        this.jspTemplate = value;
    }

    public Vertical getVertical() {
        return vertical;
    }

    public void setVertical(Vertical value) {
        this.vertical = value;
    }

    public EnableEditInspect getEnableEditInspect() {
        return enableEditInspect;
    }

    public void setEnableEditInspect(EnableEditInspect value) {
        this.enableEditInspect = value;
    }

    public AllowEditPunchout getAllowEditPunchout() {
        return allowEditPunchout;
    }

    public void setAllowEditPunchout(AllowEditPunchout value) {
        this.allowEditPunchout = value;
    }

    public AllowOnePOPerPunchout getAllowOnePOPerPunchout() {
        return allowOnePOPerPunchout;
    }

    public void setAllowOnePOPerPunchout(AllowOnePOPerPunchout value) {
        this.allowOnePOPerPunchout = value;
    }

    public AllowMultiplePOWithinPunchout getAllowMultiplePOWithinPunchout() {
        return allowMultiplePOWithinPunchout;
    }

    public void setAllowMultiplePOWithinPunchout(AllowMultiplePOWithinPunchout value) {
        this.allowMultiplePOWithinPunchout = value;
    }

    public CopyPunchoutValidation getCopyPunchoutValidation() {
        return copyPunchoutValidation;
    }

    public void setCopyPunchoutValidation(CopyPunchoutValidation value) {
        this.copyPunchoutValidation = value;
    }

    public PunchoutUsesSubframes getPunchoutUsesSubframes() {
        return punchoutUsesSubframes;
    }

    public void setPunchoutUsesSubframes(PunchoutUsesSubframes value) {
        this.punchoutUsesSubframes = value;
    }

    public ForceSSLv3Protocol getForceSSLv3Protocol() {
        return forceSSLv3Protocol;
    }

    public void setForceSSLv3Protocol(ForceSSLv3Protocol value) {
        this.forceSSLv3Protocol = value;
    }

    public SuppressPunchoutLogo getSuppressPunchoutLogo() {
        return suppressPunchoutLogo;
    }

    public void setSuppressPunchoutLogo(SuppressPunchoutLogo value) {
        this.suppressPunchoutLogo = value;
    }

    public MultipleFulfillmentSupplierPunchoutConfig getMultipleFulfillmentSupplierPunchoutConfig() {
        return multipleFulfillmentSupplierPunchoutConfig;
    }

    public void setMultipleFulfillmentSupplierPunchoutConfig(MultipleFulfillmentSupplierPunchoutConfig value) {
        this.multipleFulfillmentSupplierPunchoutConfig = value;
    }

}
