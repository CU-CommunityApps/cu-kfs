package edu.cornell.kfs.module.purap.businessobject.xml;

import java.math.BigDecimal;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "itemUnitOfMeasureCode", "itemCatalogNumber", "itemDescription", "itemUnitPrice",
        "purchasingCommodityCode", "itemQuantity" })
@XmlRootElement(name = "item", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
public class IWantItemXml {

    @XmlElement(name = "itemUnitOfMeasureCode", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String itemUnitOfMeasureCode;

    @XmlElement(name = "itemCatalogNumber", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String itemCatalogNumber;

    @XmlElement(name = "itemDescription", namespace = "http://www.kuali.org/kfs/purap/iWantDocument", required = true)
    private String itemDescription;

    @XmlElement(name = "itemUnitPrice", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private BigDecimal itemUnitPrice;

    @XmlElement(name = "purchasingCommodityCode", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String purchasingCommodityCode;

    @XmlElement(name = "itemQuantity", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private BigDecimal itemQuantity;

    public String getItemUnitOfMeasureCode() {
        return itemUnitOfMeasureCode;
    }

    public void setItemUnitOfMeasureCode(String itemUnitOfMeasureCode) {
        this.itemUnitOfMeasureCode = itemUnitOfMeasureCode;
    }

    public String getItemCatalogNumber() {
        return itemCatalogNumber;
    }

    public void setItemCatalogNumber(String itemCatalogNumber) {
        this.itemCatalogNumber = itemCatalogNumber;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public BigDecimal getItemUnitPrice() {
        return itemUnitPrice;
    }

    public void setItemUnitPrice(BigDecimal itemUnitPrice) {
        this.itemUnitPrice = itemUnitPrice;
    }

    public String getPurchasingCommodityCode() {
        return purchasingCommodityCode;
    }

    public void setPurchasingCommodityCode(String purchasingCommodityCode) {
        this.purchasingCommodityCode = purchasingCommodityCode;
    }

    public BigDecimal getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(BigDecimal itemQuantity) {
        this.itemQuantity = itemQuantity;
    }
    
    @Override
    public String toString() {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        return builder.build();
    }


}
