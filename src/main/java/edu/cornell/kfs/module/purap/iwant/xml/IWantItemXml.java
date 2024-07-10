package edu.cornell.kfs.module.purap.iwant.xml;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.purap.businessobject.BatchIWantItem;
import edu.cornell.kfs.sys.xmladapters.KualiDecimalNullPossibleXmlAdapter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "itemUnitOfMeasureCode", "itemCatalogNumber", "itemDescription", "itemUnitPrice",
        "purchasingCommodityCode", "itemQuantity" })
@XmlRootElement(name = "item", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
public class IWantItemXml {

    @XmlElement(name = "itemUnitOfMeasureCode", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String itemUnitOfMeasureCode;

    @XmlElement(name = "itemCatalogNumber", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String itemCatalogNumber;

    @XmlElement(name = "itemDescription", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE, required = true)
    private String itemDescription;

    @XmlElement(name = "itemUnitPrice", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    @XmlJavaTypeAdapter(KualiDecimalNullPossibleXmlAdapter.class)
    private KualiDecimal itemUnitPrice;

    @XmlElement(name = "purchasingCommodityCode", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String purchasingCommodityCode;

    @XmlElement(name = "itemQuantity", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    @XmlJavaTypeAdapter(KualiDecimalNullPossibleXmlAdapter.class)
    private KualiDecimal itemQuantity;

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

    public KualiDecimal getItemUnitPrice() {
        return itemUnitPrice;
    }

    public void setItemUnitPrice(KualiDecimal itemUnitPrice) {
        this.itemUnitPrice = itemUnitPrice;
    }

    public String getPurchasingCommodityCode() {
        return purchasingCommodityCode;
    }

    public void setPurchasingCommodityCode(String purchasingCommodityCode) {
        this.purchasingCommodityCode = purchasingCommodityCode;
    }

    public KualiDecimal getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(KualiDecimal itemQuantity) {
        this.itemQuantity = itemQuantity;
    }
    
    public BatchIWantItem toBatchIWantItem() {
        BatchIWantItem item = new BatchIWantItem();
        item.setItemUnitOfMeasureCode(itemUnitOfMeasureCode);
        item.setItemCatalogNumber(itemCatalogNumber);
        item.setItemDescription(itemDescription);
        item.setItemUnitPrice(itemUnitPrice != null ? itemUnitPrice.bigDecimalValue() : null);
        item.setPurchasingCommodityCode(purchasingCommodityCode);
        item.setItemQuantity(String.valueOf(itemQuantity));
        return item;
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
