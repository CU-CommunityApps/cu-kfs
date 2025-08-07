/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.purap.businessobject;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.document.PurchasingDocument;
import org.kuali.kfs.sys.businessobject.UnitOfMeasure;
import org.kuali.kfs.vnd.businessobject.CommodityCode;

import java.math.BigDecimal;
import java.util.Locale;

public abstract class PurchasingItemBase extends PurApItemBase implements PurchasingItem {

    private String purchasingCommodityCode;

    private CommodityCode commodityCode;

    private UnitOfMeasure itemUnitOfMeasure;
    // KFSPTS-985
    private Integer favoriteAccountLineIdentifier;
    
 // KFSPTS-2257 : eshop flag enhancement
    // (Product Flags - Controlled Substance, Energy Star, Green, Hazardous Material, Rad Minor, Radioactive, Recycled, Select Agent, Toxin)

    private boolean controlled;
    private boolean green;
    private boolean hazardous;
    private boolean radioactive;
    private boolean radioactiveMinor;
    private boolean selectAgent;
    private boolean toxin;
    private boolean recycled; // mapped to greenproduct
    private boolean energyStar;

    @Override
    public boolean isConsideredEntered() {
        if (this instanceof PurchaseOrderItem) {
            // if item is PO item... only validate active items
            final PurchaseOrderItem poi = (PurchaseOrderItem) this;
            if (!poi.isItemActiveIndicator()) {
                return false;
            }
        }
        if (getItemType().isAdditionalChargeIndicator()) {
            return ObjectUtils.isNotNull(getItemUnitPrice()) || StringUtils.isNotBlank(getItemDescription())
                    || !getSourceAccountingLines().isEmpty();
        }
        return true;
    }

    /**
     * Determines if the Purchasing Item is empty.
     *
     * @return boolean true if item is empty, false if conditions show its not empty.
     */
    public boolean isEmpty() {
        return !(StringUtils.isNotEmpty(getItemUnitOfMeasureCode())
                || StringUtils.isNotEmpty(getItemCatalogNumber())
                || StringUtils.isNotEmpty(getItemDescription())
                || StringUtils.isNotEmpty(getItemAuxiliaryPartIdentifier())
                || ObjectUtils.isNotNull(getItemQuantity())
                || ObjectUtils.isNotNull(getItemUnitPrice())
                && getItemUnitPrice().compareTo(BigDecimal.ZERO) != 0
                || !isAccountListEmpty());
    }

    /**
     * Determines if the Purchasing Item Detail is empty.
     *
     * @return boolean true if item is empty, false if conditions show its not empty.
     */
    public boolean isItemDetailEmpty() {
        boolean empty = ObjectUtils.isNull(getItemQuantity()) || StringUtils.isEmpty(getItemQuantity().toString());
        empty &= StringUtils.isEmpty(getItemUnitOfMeasureCode());
        empty &= StringUtils.isEmpty(getItemCatalogNumber());
        empty &= StringUtils.isEmpty(getItemDescription());
        empty &= ObjectUtils.isNull(getItemUnitPrice()) || getItemUnitPrice().compareTo(BigDecimal.ZERO) == 0;
        return empty;
    }

    public CommodityCode getCommodityCode() {
        if (ObjectUtils.isNull(commodityCode)
                || !StringUtils.equalsIgnoreCase(commodityCode.getPurchasingCommodityCode(),
                    getPurchasingCommodityCode())) {
            refreshReferenceObject(PurapPropertyConstants.COMMODITY_CODE);
        }
        return commodityCode;
    }

    public void setCommodityCode(final CommodityCode commodityCode) {
        this.commodityCode = commodityCode;
    }

    public String getPurchasingCommodityCode() {
        return purchasingCommodityCode;
    }

    public void setPurchasingCommodityCode(final String purchasingCommodityCode) {
        this.purchasingCommodityCode = StringUtils.isNotBlank(purchasingCommodityCode) ?
                purchasingCommodityCode.toUpperCase(Locale.US) : purchasingCommodityCode;
    }

    @Override
    public PurchasingCapitalAssetItem getPurchasingCapitalAssetItem() {
        final PurchasingDocument pd = getPurapDocument();
        if (getItemIdentifier() != null) {
            return pd.getPurchasingCapitalAssetItem(getItemIdentifier());
        } else {
            return null;
        }
    }

    public UnitOfMeasure getItemUnitOfMeasure() {
        if (ObjectUtils.isNull(itemUnitOfMeasure)
                || !StringUtils.equalsIgnoreCase(itemUnitOfMeasure.getItemUnitOfMeasureCode(),
                    getItemUnitOfMeasureCode())) {
            refreshReferenceObject(PurapPropertyConstants.ITEM_UNIT_OF_MEASURE);
        }
        return itemUnitOfMeasure;
    }

    public void setItemUnitOfMeasure(final UnitOfMeasure itemUnitOfMeasure) {
        this.itemUnitOfMeasure = itemUnitOfMeasure;
    }

    public boolean isNewItemForAmendment() {
        return false;
    }

	public Integer getFavoriteAccountLineIdentifier() {
		return favoriteAccountLineIdentifier;
	}

	public void setFavoriteAccountLineIdentifier(
			Integer favoriteAccountLineIdentifier) {
		this.favoriteAccountLineIdentifier = favoriteAccountLineIdentifier;
	}
	public boolean isControlled() {
        return controlled;
    }

    public void setControlled(boolean controlled) {
        this.controlled = controlled;
    }

    public boolean isGreen() {
        return green;
    }

    public void setGreen(boolean green) {
        this.green = green;
    }

    public boolean isHazardous() {
        return hazardous;
    }

    public void setHazardous(boolean hazardous) {
        this.hazardous = hazardous;
    }

    public boolean isRadioactive() {
        return radioactive;
    }

    public void setRadioactive(boolean radioactive) {
        this.radioactive = radioactive;
    }

    public boolean isRadioactiveMinor() {
        return radioactiveMinor;
    }

    public void setRadioactiveMinor(boolean radioactiveMinor) {
        this.radioactiveMinor = radioactiveMinor;
    }

    public boolean isSelectAgent() {
        return selectAgent;
    }

    public void setSelectAgent(boolean selectAgent) {
        this.selectAgent = selectAgent;
    }

    public boolean isToxin() {
        return toxin;
    }

    public void setToxin(boolean toxin) {
        this.toxin = toxin;
    }

    public boolean isRecycled() {
        return recycled;
    }

    public void setRecycled(boolean recycled) {
        this.recycled = recycled;
    }

    public boolean isEnergyStar() {
        return energyStar;
    }

    public void setEnergyStar(boolean energyStar) {
        this.energyStar = energyStar;
    }
    
	// KFSPTS-2257
    public String getEshopFlags() {
        StringBuilder sb = new StringBuilder();
        if (isControlled()) {
            sb.append("Controlled,");
        }
        if (isEnergyStar()) {
            sb.append("Energy Star,");
        }
        if (isSelectAgent()) {
            sb.append("Select Agent,");
        }
        if (isRadioactive()) {
            sb.append("Radioactive,");
        }
        if (isRadioactiveMinor()) {
            sb.append("Radioactive Minor,");
        }
        if (isRecycled()) {
            sb.append("Recycled,");
        }
        if (isGreen()) {
            sb.append("Green,");
        }
        if (isToxin()) {
            sb.append("Toxin,");
        }
        if (isHazardous()) {
            sb.append("Hazardous,");
        }
        if (sb.length() == 0) {
            return "None";
        }
        return sb.substring(0, sb.length()-1);
    }
}
