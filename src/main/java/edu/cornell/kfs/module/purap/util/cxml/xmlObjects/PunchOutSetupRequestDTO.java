package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "buyerCookie",
    "extrinsics",
    "browserFormPost",
    "contacts",
    "supplierSetup",
    "shipTo",
    "selectedItem",
    "itemsOut"
})
@XmlRootElement(name = "PunchOutSetupRequest")
public class PunchOutSetupRequestDTO {

    @XmlAttribute(name = "operation", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String operation;

    @XmlElement(name = "BuyerCookie", required = true)
    private BuyerCookieDTO buyerCookie;

    @XmlElement(name = "Extrinsic")
    private List<ExtrinsicDTO> extrinsics;

    @XmlElement(name = "BrowserFormPost")
    private BrowserFormPostDTO browserFormPost;

    @XmlElement(name = "Contact")
    private List<ContactDTO> contacts;

    @XmlElement(name = "SupplierSetup")
    private SupplierSetupDTO supplierSetup;

    @XmlElement(name = "ShipTo")
    private ShipToDTO shipTo;

    @XmlElement(name = "SelectedItem")
    private SelectedItemDTO selectedItem;

    @XmlElement(name = "ItemOut")
    private List<IgnoredElementDTO> itemsOut;

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public BuyerCookieDTO getBuyerCookie() {
        return buyerCookie;
    }

    public void setBuyerCookie(BuyerCookieDTO buyerCookie) {
        this.buyerCookie = buyerCookie;
    }

    public List<ExtrinsicDTO> getExtrinsics() {
        return extrinsics;
    }

    public void setExtrinsics(List<ExtrinsicDTO> extrinsics) {
        this.extrinsics = extrinsics;
    }

    public BrowserFormPostDTO getBrowserFormPost() {
        return browserFormPost;
    }

    public void setBrowserFormPost(BrowserFormPostDTO browserFormPost) {
        this.browserFormPost = browserFormPost;
    }

    public List<ContactDTO> getContacts() {
        return contacts;
    }

    public void setContacts(List<ContactDTO> contacts) {
        this.contacts = contacts;
    }

    public SupplierSetupDTO getSupplierSetup() {
        return supplierSetup;
    }

    public void setSupplierSetup(SupplierSetupDTO supplierSetup) {
        this.supplierSetup = supplierSetup;
    }

    public ShipToDTO getShipTo() {
        return shipTo;
    }

    public void setShipTo(ShipToDTO shipTo) {
        this.shipTo = shipTo;
    }

    public SelectedItemDTO getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(SelectedItemDTO selectedItem) {
        this.selectedItem = selectedItem;
    }

    public List<IgnoredElementDTO> getItemsOut() {
        return itemsOut;
    }

    public void setItemsOut(List<IgnoredElementDTO> itemsOut) {
        this.itemsOut = itemsOut;
    }

}
