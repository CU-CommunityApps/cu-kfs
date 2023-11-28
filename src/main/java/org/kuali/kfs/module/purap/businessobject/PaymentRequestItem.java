/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PaymentRequestItem extends AccountsPayableItemBase {

    private static final Logger LOG = LogManager.getLogger();

    private BigDecimal purchaseOrderItemUnitPrice;
    
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
    public PaymentRequestItem(final PurchaseOrderItem poi, final PaymentRequestDocument preq) {
        this(poi, preq, new HashMap<>());
    }

    /**
     * Constructs a new payment request item, but also merges expired accounts.
     *
     * @param poi                        purchase order item
     * @param preq                       payment request document
     * @param expiredOrClosedAccountList list of expired or closed accounts to merge
     */
    public PaymentRequestItem(
            final PurchaseOrderItem poi, final PaymentRequestDocument preq,
            final HashMap<String, ExpiredOrClosedAccountEntry> expiredOrClosedAccountList) {
        // copy base attributes w/ extra array of fields not to be copied
        ObjectPopulationUtils.populateFromBaseClass(PurApItemBase.class, poi, this,
                PurapConstants.PREQ_ITEM_UNCOPYABLE_FIELDS);

        setItemDescription(poi.getItemDescription());

        //New Source Line should be set for PaymentRequestItem
        resetAccount();

        // set up accounts
        final List accounts = new ArrayList();
        for (final PurApAccountingLine account : poi.getSourceAccountingLines()) {
            final PurchaseOrderAccount poa = (PurchaseOrderAccount) account;

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

        setSourceAccountingLines(accounts);
        getUseTaxItems().clear();

        // clear amount and desc on below the line - we probably don't need that null
        // itemType check but it's there just in case remove if it causes problems
        // also do this if of type service
        if (ObjectUtils.isNotNull(getItemType()) && getItemType().isAmountBasedGeneralLedgerIndicator()) {
            // setting unit price to be null to be more consistent with other below the line
            setItemUnitPrice(null);
        }

        // copy custom
        purchaseOrderItemUnitPrice = poi.getItemUnitPrice();

        // set doc fields
        setPurapDocumentIdentifier(preq.getPurapDocumentIdentifier());
        setPurapDocument(preq);
    }

    /**
     * @return a purchase order item by inspecting the item type to see if its above the line or below the line and
     *         returns the appropriate type.
     */
    @Override
    public PurchaseOrderItem getPurchaseOrderItem() {
        if (ObjectUtils.isNotNull(getPurapDocumentIdentifier())) {
            if (ObjectUtils.isNull(getPaymentRequest())) {
                refreshReferenceObject(PurapPropertyConstants.PURAP_DOC);
            }
        }
        // ideally we should do this a different way - maybe move it all into the service or save this info somehow
        // (make sure and update though)
        if (getPaymentRequest() != null) {
            final PurchaseOrderDocument po = getPaymentRequest().getPurchaseOrderDocument();
            PurchaseOrderItem poi = null;
            if (getItemType().isLineItemIndicator()) {
                final List items = po.getItems();
                if (items != null) {
                    for (final Object object : items) {
                        final PurchaseOrderItem item = (PurchaseOrderItem) object;
                        if (item != null && getItemLineNumber().equals(item.getItemLineNumber())) {
                            poi = item;
                            break;
                        }
                    }
                }
            } else {
                poi = (PurchaseOrderItem) SpringContext.getBean(PurapService.class).getBelowTheLineByType(po,
                        getItemType());
            }
            if (poi != null) {
                return poi;
            } else {
                LOG.debug(
                        "getPurchaseOrderItem() Returning null because PurchaseOrderItem object for line "
                        + "number{}or itemType {} is null",
                        () -> getItemLineNumber(),
                        () -> getItemTypeCode()
                );
                return null;
            }
        } else {

            LOG.error("getPurchaseOrderItem() Returning null because paymentRequest object is null");
            throw new PurError("Payment Request Object in Purchase Order item line number " + getItemLineNumber() +
                    "or itemType " + getItemTypeCode() + " is null");
        }
    }

    public KualiDecimal getPoOutstandingAmount() {
        final PurchaseOrderItem poi = getPurchaseOrderItem();
        if (ObjectUtils.isNull(getPurchaseOrderItemUnitPrice())
                || KualiDecimal.ZERO.equals(getPurchaseOrderItemUnitPrice())) {
            return null;
        } else {
            return getPoOutstandingAmount(poi);
        }
    }

    private KualiDecimal getPoOutstandingAmount(final PurchaseOrderItem poi) {
        if (poi == null) {
            return KualiDecimal.ZERO;
        } else {
            return poi.getItemOutstandingEncumberedAmount();
        }
    }

    public KualiDecimal getPoOriginalAmount() {
        final PurchaseOrderItem poi = getPurchaseOrderItem();
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
    public void setPoOutstandingAmount(final KualiDecimal amount) {
        // do nothing
    }

    public KualiDecimal getPoOutstandingQuantity() {
        final PurchaseOrderItem poi = getPurchaseOrderItem();
        if (poi == null) {
            return null;
        } else {
            if (PurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE.equals(getItemTypeCode())) {
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
    public void setPoOutstandingQuantity(final KualiDecimal qty) {
        // do nothing
    }

    public BigDecimal getPurchaseOrderItemUnitPrice() {
        return purchaseOrderItemUnitPrice;
    }

    public BigDecimal getOriginalAmountfromPO() {
        return purchaseOrderItemUnitPrice;
    }

    public void setOriginalAmountfromPO(final BigDecimal purchaseOrderItemUnitPrice) {
        // Do nothing
    }

    public void setPurchaseOrderItemUnitPrice(final BigDecimal purchaseOrderItemUnitPrice) {
        this.purchaseOrderItemUnitPrice = purchaseOrderItemUnitPrice;
    }

    public PaymentRequestDocument getPaymentRequest() {
        if (ObjectUtils.isNotNull(getPurapDocumentIdentifier())) {
            if (ObjectUtils.isNull(getPurapDocument())) {
                refreshReferenceObject(PurapPropertyConstants.PURAP_DOC);
            }
        }
        return super.getPurapDocument();
    }

    public void setPaymentRequest(final PaymentRequestDocument paymentRequest) {
        setPurapDocument(paymentRequest);
    }

    public void generateAccountListFromPoItemAccounts(final List<PurApAccountingLine> accounts) {
        for (final PurApAccountingLine line : accounts) {
            final PurchaseOrderAccount poa = (PurchaseOrderAccount) line;
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
        final PurchaseOrderItem poi = getPurchaseOrderItem();
        if (ObjectUtils.isNull(poi)) {
            LOG.debug("poi was null");
            return false;
        }

        // if the po item is not active... skip it
        if (!poi.isItemActiveIndicator()) {
            LOG.debug("poi was not active: {}", poi);
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
        getNewSourceLine().setAmount(null);
        getNewSourceLine().setAccountLinePercent(null);
    }

    /**
     * Added for electronic invoice
     */
    public void addToUnitPrice(final BigDecimal addThisValue) {
        if (getItemUnitPrice() == null) {
            setItemUnitPrice(BigDecimal.ZERO);
        }
        final BigDecimal addedPrice = getItemUnitPrice().add(addThisValue);
        setItemUnitPrice(addedPrice);
    }

    public void addToExtendedPrice(final KualiDecimal addThisValue) {
        if (getExtendedPrice() == null) {
            setExtendedPrice(KualiDecimal.ZERO);
        }
        final KualiDecimal addedPrice = getExtendedPrice().add(addThisValue);
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
			final List<PaymentRequestAccountRevision> preqAccounRevisions) {
		this.preqAccounRevisions = preqAccounRevisions;
	}
    
    
}
