package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "itemID"
})
@XmlRootElement(name = "SelectedItem")
public class SelectedItemDTO {

    @XmlElement(name = "ItemID", required = true)
    private ItemIDDTO itemID;

    public ItemIDDTO getItemID() {
        return itemID;
    }

    public void setItemID(ItemIDDTO itemID) {
        this.itemID = itemID;
    }

}
