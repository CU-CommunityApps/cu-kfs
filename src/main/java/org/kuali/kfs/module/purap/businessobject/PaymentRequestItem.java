/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.service.AccountsPayableService;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.exception.PurError;
import org.kuali.kfs.module.purap.util.ExpiredOrClosedAccountEntry;
import org.kuali.kfs.module.purap.util.PurApItemUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.util.ObjectPopulationUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PaymentRequestItem extends AccountsPayableItemBase {

    private static final Logger LOG = LogManager.getLogger(PaymentRequestItem.class);

    private BigDecimal purchaseOrderItemUnitPrice;
    private KualiDecimal itemOutstandingInvoiceQuantity;
    private KualiDecimal itemOutstandingInvoiceAmount;
    
    // KFSPTS-1891
    private List<PaymentRequestAccountRevision> preqAccounRevisions;

    public PaymentRequestItem() {

    }

    /**
     * preq item constructor Delegate
     *
     * @param poi  purchase order item
     * @param preq payment request document
     */
    public PaymentRequestItem(PurchaseOrderItem poi, PaymentRequestDocument preq) {
        this(poi, preq, new HashMap<>());
    }

    /**
     * Constructs a new payment request item, but also merges expired accounts.
     *
     * @param poi                        purchase order item
     * @param preq                       payment request document
     * @param expiredOrClosedAccountList list of expired or closed accounts to merge
     */
    public PaymentRequestItem(PurchaseOrderItem poi, PaymentRequestDocument preq,
            HashMap<String, ExpiredOrClosedAccountEntry> expiredOrClosedAccountList) {
        // copy base attributes w/ extra array of fields not to be copied
        ObjectPopulationUtils.populateFromBaseClass(PurApItemBase.class, poi, this,
                PurapConstants.PREQ_ITEM_UNCOPYABLE_FIELDS);

        setItemDescription(poi.getItemDescription());

        //New Source Line should be set for PaymentRequestItem
        resetAccount();

        // set up accounts
        List accounts = new ArrayList();
        for (PurApAccountingLine account : poi.getSourceAccountingLines()) {
            PurchaseOrderAccount poa = (PurchaseOrderAccount) account;

            // check if this account is expired/closed and replace as needed
            SpringContext.getBean(AccountsPayableService.class).processExpiredOrClosedAccount(poa,
                    expiredOrClosedAccountList);

            //KFSMI-4522 copy an accounting line with zero dollar amount if system parameter allows
            if (poa.getAmount().isZero()) {
                if (SpringContext.getBean(AccountsPayableService.class).canCopyAccountingLinesWithZeroAmount()) {
                    accounts.add(new PaymentRequestAccount(this, poa));
                }
            } else {
                accounts.add(new PaymentRequestAccount(this, poa));
            }
        }

        this.setSourceAccountingLines(accounts);
        this.getUseTaxItems().clear();

        // clear amount and desc on below the line - we probably don't need that null
        // itemType check but it's there just in case remove if it causes problems
        // also do this if of type service
        if (ObjectUtils.isNotNull(this.getItemType()) && this.getItemType().isAmountBasedGeneralLedgerIndicator()) {
            // setting unit price to be null to be more consistent with other below the line
            this.setItemUnitPrice(null);
        }

        // copy custom
        this.purchaseOrderItemUnitPrice = poi.getItemUnitPrice();

        // set doc fields
        this.setPurapDocumentIdentifier(preq.getPurapDocumentIdentifier());
        this.setPurapDocument(preq);
    }

    /**
     * @return a purchase order item by inspecting the item type to see if its above the line or below the line and
     *         returns the appropriate type.
     */
    @Override
    public PurchaseOrderItem getPurchaseOrderItem() {
        if (ObjectUtils.isNotNull(this.getPurapDocumentIdentifier())) {
            if (ObjectUtils.isNull(this.getPaymentRequest())) {
                this.refreshReferenceObject(PurapPropertyConstants.PURAP_DOC);
            }
        }
        // ideally we should do this a different way - maybe move it all into the service or save this info somehow
        // (make sure and update though)
        if (getPaymentRequest() != null) {
            PurchaseOrderDocument po = getPaymentRequest().getPurchaseOrderDocument();
            PurchaseOrderItem poi = null;
            if (this.getItemType().isLineItemIndicator()) {
                List items = po.getItems();
                if (items != null) {
                    for (Object object : items) {
                        PurchaseOrderItem item = (PurchaseOrderItem) object;
                        if (item != null && item.getItemLineNumber().equals(this.getItemLineNumber())) {
                            poi = item;
                            break;
                        }
                    }
                }
            } else {
                poi = (PurchaseOrderItem) SpringContext.getBean(PurapService.class).getBelowTheLineByType(po,
                        this.getItemType());
            }
            if (poi != null) {
                return poi;
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getPurchaseOrderItem() Returning null because PurchaseOrderItem object for line " +
                            "number" + getItemLineNumber() + "or itemType " + getItemTypeCode() + " is null");
                }
                return null;
            }
        } else {

            LOG.error("getPurchaseOrderItem() Returning null because paymentRequest object is null");
            throw new PurError("Payment Request Object in Purchase Order item line number " + getItemLineNumber() +
                    "or itemType " + getItemTypeCode() + " is null");
        }
    }

    public KualiDecimal getPoOutstandingAmount() {
        PurchaseOrderItem poi = getPurchaseOrderItem();
        if (ObjectUtils.isNull(this.getPurchaseOrderItemUnitPrice())
                || KualiDecimal.ZERO.equals(this.getPurchaseOrderItemUnitPrice())) {
            return null;
        } else {
            return this.getPoOutstandingAmount(poi);
        }
    }

    private KualiDecimal getPoOutstandingAmount(PurchaseOrderItem poi) {
        if (poi == null) {
            return KualiDecimal.ZERO;
        } else {
            return poi.getItemOutstandingEncumberedAmount();
        }
    }

    public KualiDecimal getPoOriginalAmount() {
        PurchaseOrderItem poi = getPurchaseOrderItem();
        if (poi == null) {
            return null;
        } else {
            return poi.getExtendedPrice();
        }
    }

    /**
     * Exists due to a setter requirement by the htmlControlAttribute
     *
     * @param amount po outstanding amount
     * @deprecated
     */
    @Deprecated
    public void setPoOutstandingAmount(KualiDecimal amount) {
        // do nothing
    }

    public KualiDecimal getPoOutstandingQuantity() {
        PurchaseOrderItem poi = getPurchaseOrderItem();
        if (poi == null) {
            return null;
        } else {
            if (PurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE.equals(this.getItemTypeCode())) {
                return null;
            } else {
                return poi.getOutstandingQuantity();
            }
        }
    }

    /**
     * Exists due to a setter requirement by the htmlControlAttribute
     *
     * @param qty po outstanding quantity
     * @deprecated
     */
    @Deprecated
    public void setPoOutstandingQuantity(KualiDecimal qty) {
        // do nothing
    }

    public BigDecimal getPurchaseOrderItemUnitPrice() {
        return purchaseOrderItemUnitPrice;
    }

    public BigDecimal getOriginalAmountfromPO() {
        return purchaseOrderItemUnitPrice;
    }

    public void setOriginalAmountfromPO(BigDecimal purchaseOrderItemUnitPrice) {
        // Do nothing
    }

    public void setPurchaseOrderItemUnitPrice(BigDecimal purchaseOrderItemUnitPrice) {
        this.purchaseOrderItemUnitPrice = purchaseOrderItemUnitPrice;
    }

    public KualiDecimal getItemOutstandingInvoiceAmount() {
        return itemOutstandingInvoiceAmount;
    }

    public void setItemOutstandingInvoiceAmount(KualiDecimal itemOutstandingInvoiceAmount) {
        this.itemOutstandingInvoiceAmount = itemOutstandingInvoiceAmount;
    }

    public KualiDecimal getItemOutstandingInvoiceQuantity() {
        return itemOutstandingInvoiceQuantity;
    }

    public void setItemOutstandingInvoiceQuantity(KualiDecimal itemOutstandingInvoiceQuantity) {
        this.itemOutstandingInvoiceQuantity = itemOutstandingInvoiceQuantity;
    }

    public PaymentRequestDocument getPaymentRequest() {
        if (ObjectUtils.isNotNull(getPurapDocumentIdentifier())) {
            if (ObjectUtils.isNull(getPurapDocument())) {
                this.refreshReferenceObject(PurapPropertyConstants.PURAP_DOC);
            }
        }
        return super.getPurapDocument();
    }

    public void setPaymentRequest(PaymentRequestDocument paymentRequest) {
        this.setPurapDocument(paymentRequest);
    }

    public void generateAccountListFromPoItemAccounts(List<PurApAccountingLine> accounts) {
        for (PurApAccountingLine line : accounts) {
            PurchaseOrderAccount poa = (PurchaseOrderAccount) line;
            if (!line.isEmpty()) {
                getSourceAccountingLines().add(new PaymentRequestAccount(this, poa));
            }
        }
    }

    @Override
    public Class getAccountingLineClass() {
        return PaymentRequestAccount.class;
    }

    public boolean isDisplayOnPreq() {
        PurchaseOrderItem poi = getPurchaseOrderItem();
        if (ObjectUtils.isNull(poi)) {
            LOG.debug("poi was null");
            return false;
        }

        // if the po item is not active... skip it
        if (!poi.isItemActiveIndicator()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("poi was not active: " + poi.toString());
            }
            return false;
        }

        ItemType poiType = poi.getItemType();

        if (poiType.isQuantityBasedGeneralLedgerIndicator()) {
            if (poi.getItemQuantity().isGreaterThan(poi.getItemInvoicedTotalQuantity())) {
                return true;
            } else {
                return ObjectUtils.isNotNull(this.getItemQuantity())
                        && this.getItemQuantity().isGreaterThan(KualiDecimal.ZERO);
            }
        } else {
            // not quantity based
            if (poi.getItemOutstandingEncumberedAmount().isGreaterThan(KualiDecimal.ZERO)) {
                return true;
            } else {
                return PurApItemUtils.isNonZeroExtended(this);
            }
        }
    }

    /**
     * sets account line percentage to zero.
     */
    @Override
    public void resetAccount() {
        super.resetAccount();
        this.getNewSourceLine().setAmount(null);
        this.getNewSourceLine().setAccountLinePercent(null);
    }

    /**
     * Added for electronic invoice
     */
    public void addToUnitPrice(BigDecimal addThisValue) {
        if (getItemUnitPrice() == null) {
            setItemUnitPrice(BigDecimal.ZERO);
        }
        BigDecimal addedPrice = getItemUnitPrice().add(addThisValue);
        setItemUnitPrice(addedPrice);
    }

    public void addToExtendedPrice(KualiDecimal addThisValue) {
        if (getExtendedPrice() == null) {
            setExtendedPrice(KualiDecimal.ZERO);
        }
        KualiDecimal addedPrice = getExtendedPrice().add(addThisValue);
        setExtendedPrice(addedPrice);
    }

    @Override
    public Class getUseTaxClass() {
        return PaymentRequestItemUseTax.class;
    }
    
	public List<PaymentRequestAccountRevision> getPreqAccounRevisions() {
		return preqAccounRevisions;
	}

	public void setPreqAccounRevisions(
			List<PaymentRequestAccountRevision> preqAccounRevisions) {
		this.preqAccounRevisions = preqAccounRevisions;
	}
    
    
}
