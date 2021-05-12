package edu.cornell.kfs.module.purap.businessobject;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;

import org.kuali.kfs.module.purap.businessobject.ItemType;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurApItemUseTax;
import org.kuali.kfs.module.purap.businessobject.PurApSummaryItem;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.sys.businessobject.UnitOfMeasure;
import org.kuali.kfs.vnd.businessobject.CommodityCode;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

import edu.cornell.kfs.module.purap.document.IWantDocument;

public class IWantItem extends PersistableBusinessObjectBase implements PurApItem {

    private static final long serialVersionUID = 1L;
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
    

    @Override
    public Integer getItemIdentifier() {
        return itemIdentifier;
    }

    @Override
    public void setItemIdentifier(Integer itemIdentifier) {
        this.itemIdentifier = itemIdentifier;
    }

    @Override
    public Integer getItemLineNumber() {
        return itemLineNumber;
    }

    @Override
    public void setItemLineNumber(Integer itemLineNumber) {
        this.itemLineNumber = itemLineNumber;
    }

    @Override
    public String getItemUnitOfMeasureCode() {
        return itemUnitOfMeasureCode;
    }

    @Override
    public void setItemUnitOfMeasureCode(String itemUnitOfMeasureCode) {
        this.itemUnitOfMeasureCode = itemUnitOfMeasureCode;
    }

    @Override
    public String getItemCatalogNumber() {
        return itemCatalogNumber;
    }

    @Override
    public void setItemCatalogNumber(String itemCatalogNumber) {
        this.itemCatalogNumber = itemCatalogNumber;
    }

    @Override 
    public String getItemDescription() {
        return itemDescription;
    }

    @Override
    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    @Override
    public BigDecimal getItemUnitPrice() {
        return itemUnitPrice;
    }

    @Override
    public void setItemUnitPrice(BigDecimal itemUnitPrice) {
        this.itemUnitPrice = itemUnitPrice;
    }

    public String getPurchasingCommodityCode() {
        return purchasingCommodityCode;
    }

    public void setPurchasingCommodityCode(String purchasingCommodityCode) {
        this.purchasingCommodityCode = purchasingCommodityCode;
    }

    @Override
    public KualiDecimal getItemQuantity() {
        return itemQuantity;
    }

    @Override
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

    @SuppressWarnings("rawtypes")
    protected LinkedHashMap toStringMapper() {
        return null;
    }

    public String getiWantDocumentNumber() {
        return documentNumber;
    }

    public void setiWantDocumentNumber(String iWantDocumentNumber) {
        this.documentNumber = iWantDocumentNumber;
    }

    @Override
    public KualiDecimal getTotalAmount() {
        
        if (this.getItemQuantity() != null && this.getItemUnitPrice() != null) {
            return new KualiDecimal(this.getItemQuantity().bigDecimalValue().multiply(this.getItemUnitPrice()));
            
        } else {
            return KualiDecimal.ZERO;
        }
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    /**
     * Helper method for copying an item.
     * 
     * @param oldItem
     * @return a copy of the item.
     */
    public static IWantItem createCopy(IWantItem oldItem) {
        if (oldItem == null) {
            throw new IllegalArgumentException("source item cannot be null");
        }
        IWantItem copyItem = new IWantItem();

        // NOTE: We do not copy the itemIdentifier (the primary key).
        copyItem.documentNumber = oldItem.documentNumber;
        copyItem.itemLineNumber = oldItem.itemLineNumber;
        copyItem.itemUnitOfMeasureCode = oldItem.itemUnitOfMeasureCode;
        copyItem.itemCatalogNumber = oldItem.itemCatalogNumber;
        copyItem.itemDescription = oldItem.itemDescription;
        copyItem.itemUnitPrice = oldItem.itemUnitPrice;
        copyItem.purchasingCommodityCode = oldItem.purchasingCommodityCode;
        copyItem.itemQuantity = oldItem.itemQuantity;
        copyItem.commodityCode = oldItem.commodityCode;
        copyItem.itemUnitOfMeasure = oldItem.itemUnitOfMeasure;
        copyItem.iWantDocument = oldItem.iWantDocument;

        return copyItem;
    }

    /*
     * ============================================
     * NOTE: We only added the methods below for compliance with the PurApItem interface; most of them just return nulls or are no-ops.
     * ============================================
     */

    @Override
    public boolean isConsideredEntered() {
        return false;
    }

    @Override
    public KualiDecimal calculateExtendedPrice() {
        return null;
    }

    @Override
    public void fixAccountReferences() {
        // Do nothing.
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getAccountingLineClass() {
        return null;
    }

    @Override
    public List<PurApAccountingLine> getBaselineSourceAccountingLines() {
        return null;
    }

    @Override
    public KualiDecimal getExtendedPrice() {
        return null;
    }

    @Override
    public String getExternalOrganizationB2bProductReferenceNumber() {
        return null;
    }

    @Override
    public String getExternalOrganizationB2bProductTypeName() {
        return null;
    }

    @Override
    public boolean getItemAssignedToTradeInIndicator() {
        return false;
    }

    @Override
    public String getItemAuxiliaryPartIdentifier() {
        return null;
    }

    @Override
    public String getItemIdentifierString() {
        return null;
    }

    @Override
    public KualiDecimal getItemTaxAmount() {
        return null;
    }

    @Override
    public ItemType getItemType() {
        return null;
    }

    @Override
    public String getItemTypeCode() {
        return null;
    }

    @Override
    public PurApAccountingLine getNewSourceLine() {
        return null;
    }

    @Override
    public <T extends PurchasingAccountsPayableDocument> T getPurapDocument() {
        return null;
    }

    @Override
    public Integer getPurapDocumentIdentifier() {
        return null;
    }

    @Override
    public List<PurApAccountingLine> getSourceAccountingLines() {
        return null;
    }

    @Override
    public PurApSummaryItem getSummaryItem() {
        return null;
    }

    @Override
    public KualiDecimal getTotalRemitAmount() {
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getUseTaxClass() {
        return null;
    }

    @Override
    public List<PurApItemUseTax> getUseTaxItems() {
        return null;
    }

    @Override
    public void resetAccount() {
        // Do nothing.
    }

    @Override
    public void setExtendedPrice(KualiDecimal arg0) {
        // Do nothing.
    }

    @Override
    public void setExternalOrganizationB2bProductReferenceNumber(String arg0) {
        // Do nothing.
    }

    @Override
    public void setExternalOrganizationB2bProductTypeName(String arg0) {
        // Do nothing.
    }

    @Override
    public void setItemAssignedToTradeInIndicator(boolean arg0) {
        // Do nothing.
    }

    @Override
    public void setItemAuxiliaryPartIdentifier(String arg0) {
        // Do nothing.
    }

    @Override
    public void setItemTaxAmount(KualiDecimal arg0) {
        // Do nothing.
    }

    @Override
    public void setItemType(ItemType arg0) {
        // Do nothing.
    }

    @Override
    public void setItemTypeCode(String arg0) {
        // Do nothing.
    }

    @Override
    public void setNewSourceLine(PurApAccountingLine arg0) {
        // Do nothing.
    }

    @Override
    public void setPurapDocument(PurchasingAccountsPayableDocument arg0) {
        // Do nothing.
    }

    @Override
    public void setPurapDocumentIdentifier(Integer arg0) {
        // Do nothing.
    }

    @Override
    public void setSourceAccountingLines(List<PurApAccountingLine> arg0) {
        // Do nothing.
    }

    @Override
    public void setTotalAmount(KualiDecimal arg0) {
        // Do nothing.
    }

    @Override
    public void setUseTaxItems(List<PurApItemUseTax> arg0) {
        // Do nothing.
    }

}
