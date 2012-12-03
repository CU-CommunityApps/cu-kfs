package edu.cornell.kfs.module.purap.businessobject;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

import org.kuali.kfs.module.purap.businessobject.PurchaseRequisitionItemUseTax;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.sys.businessobject.UnitOfMeasure;
import org.kuali.kfs.vnd.businessobject.CommodityCode;
import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;
import org.kuali.rice.kns.util.KualiDecimal;

import edu.cornell.kfs.module.purap.document.IWantDocument;

public class IWantItem  extends PersistableBusinessObjectBase{
   private String documentNumber;
    private Integer itemIdentifier;
    private Integer itemLineNumber;
    private String itemUnitOfMeasureCode;
    private String itemCatalogNumber;
    private String itemDescription;
    private BigDecimal itemUnitPrice;
    private String purchasingCommodityCode;
    private KualiDecimal itemQuantity;
    
    private CommodityCode commodityCode;
    private UnitOfMeasure itemUnitOfMeasure;
    private IWantDocument iWantDocument;
    
    public IWantItem() {
        super();
        itemUnitOfMeasureCode = "EA";
    }
    

    public Integer getItemIdentifier() {
        return itemIdentifier;
    }
    public void setItemIdentifier(Integer itemIdentifier) {
        this.itemIdentifier = itemIdentifier;
    }
    public Integer getItemLineNumber() {
        return itemLineNumber;
    }
    public void setItemLineNumber(Integer itemLineNumber) {
        this.itemLineNumber = itemLineNumber;
    }
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
    public KualiDecimal getItemQuantity() {
        return itemQuantity;
    }
    public void setItemQuantity(KualiDecimal itemQuantity) {
        this.itemQuantity = itemQuantity;
    }
    public CommodityCode getCommodityCode() {
        return commodityCode;
    }
    public void setCommodityCode(CommodityCode commodityCode) {
        this.commodityCode = commodityCode;
    }
    public UnitOfMeasure getItemUnitOfMeasure() {
        return itemUnitOfMeasure;
    }
    public void setItemUnitOfMeasure(UnitOfMeasure itemUnitOfMeasure) {
        this.itemUnitOfMeasure = itemUnitOfMeasure;
    }

    public IWantDocument getiWantDocument() {
        return iWantDocument;
    }

    public void setiWantDocument(IWantDocument iWantDocument) {
        this.iWantDocument = iWantDocument;
    }

    @Override
    protected LinkedHashMap toStringMapper() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getiWantDocumentNumber() {
        return documentNumber;
    }

    public void setiWantDocumentNumber(String iWantDocumentNumber) {
        this.documentNumber = iWantDocumentNumber;
    }

    public KualiDecimal getTotalAmount() {
        
        if (this.getItemQuantity() != null && this.getItemUnitPrice() != null) {
            return new KualiDecimal(this.getItemQuantity().bigDecimalValue().multiply(this.getItemUnitPrice()));
            
        } else
            return KualiDecimal.ZERO;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }


    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

}
