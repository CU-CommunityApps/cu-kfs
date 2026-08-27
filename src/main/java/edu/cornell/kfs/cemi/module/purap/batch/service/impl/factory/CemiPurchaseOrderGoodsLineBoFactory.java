package edu.cornell.kfs.cemi.module.purap.batch.service.impl.factory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderAccount;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;

import edu.cornell.kfs.cemi.module.purap.CemiPurchaseOrderConstants;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderGoodsLineBo;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderHeaderBo;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderLineSplitBo;
import edu.cornell.kfs.cemi.module.purap.util.CemiPurchaseOrderUtils;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CemiPurchaseOrderGoodsLineBoFactory {

    private CemiPurchaseOrderHeaderBo headerBo;
    private Optional<PurchaseOrderItem> purchaseOrderItem;
    private List<PurchaseOrderAccount> itemAccountingLines;

    public CemiPurchaseOrderGoodsLineBoFactory(final CemiPurchaseOrderHeaderBo headerBo,
            final Optional<PurchaseOrderItem> purchaseOrderItem) {
        Validate.notNull(headerBo, "headerBo cannot be null");
        Validate.notNull(purchaseOrderItem, "purchaseOrderItem wrapper object cannot be null");
        this.headerBo = headerBo;
        this.purchaseOrderItem = purchaseOrderItem;
        this.itemAccountingLines = purchaseOrderItem.isPresent()
                ? CemiPurchaseOrderUtils.getOutstandingEncumberedAccountingLines(purchaseOrderItem.get())
                : List.of();
        Validate.isTrue(purchaseOrderItem.isEmpty() || itemAccountingLines.size() > 0,
                "If a non-empty purchaseOrderItem wrapper is specified, then the wrapped item must have one or more "
                        + "accounting lines with outstanding encumbrances");
    }

    public static CemiPurchaseOrderGoodsLineBo createGoodsLineBoFrom(CemiPurchaseOrderHeaderBo headerBo,
            final Optional<PurchaseOrderItem> purchaseOrderItem) {
        final CemiPurchaseOrderGoodsLineBoFactory factory = new CemiPurchaseOrderGoodsLineBoFactory(
                headerBo, purchaseOrderItem);
        return factory.createCemiPurchaseOrderGoodsLineBo();
    }

    public CemiPurchaseOrderGoodsLineBo createCemiPurchaseOrderGoodsLineBo() {
        final CemiPurchaseOrderGoodsLineBo goodsLine = new CemiPurchaseOrderGoodsLineBo();

        final String lineNumber = determineLineNumber();
        final String catalogNumber = determineCatalogNumber();

        goodsLine.setGoodsRowId(lineNumber);
        goodsLine.setGoodsCatalogItem(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsPurchaseOrderLineId(determinePurchaseOrderLineId(lineNumber));
        goodsLine.setGoodsLineNumber(lineNumber);
        goodsLine.setGoodsLineCompany(determineCompany());
        goodsLine.setGoodsSupplierItemIdentifier(catalogNumber);
        goodsLine.setGoodsSupplierPartId(catalogNumber);
        goodsLine.setGoodsSupplierPartAuxiliaryId(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsUnspscCode(determineUNSPSCCode());
        goodsLine.setGoodsItemDescription(determineItemDescription());
        goodsLine.setGoodsSupplierContractLine(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsAlternateSupplierContract(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsCommodityCode(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsCommodityCodeType(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsPaymentStatus(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsInvoiceStatus(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsReceivingStatus(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsShippingStatus(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsTrackingStatus(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsResourceCategory(determineResourceCategory());
        goodsLine.setGoodsTaxApplicability(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsTaxCode(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsTaxRate1(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsTaxRecoverability1(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsTaxOption1(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsTaxRate2(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsTaxRecoverability2(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsTaxOption2(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsTaxRate3(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsTaxRecoverability3(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsTaxOption3(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsTaxRate4(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsTaxRecoverability4(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsTaxOption4(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsTaxRate5(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsTaxRecoverability5(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsTaxOption5(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsTaxRate6(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsTaxRecoverability6(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsTaxOption6(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsPackagingString(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsQuantity(determineOutstandingQuantity());
        goodsLine.setGoodsUnitOfMeasure(determineUnitOfMeasure());
        goodsLine.setGoodsUnitCost(determineUnitCost());
        goodsLine.setGoodsRequestedAsNoCharge(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsExtendedAmount(determineOutstandingExtendedAmount());
        goodsLine.setGoodsLotSerialInformation(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsLotNumber(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsSerialNumber(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsDueDate(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsDeliveryType(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsPrepaid(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsDownPayment(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsRetention(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsRequestedDeliveryDate(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsBudgetDate(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsMemo(determineMemo());
        goodsLine.setGoodsShipToAddress(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsShipToGlobalLocationNumber(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsShipToLocationIdentifier(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsShipToContact(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsRequester(determineRequester());
        goodsLine.setGoodsDeliverToLocation(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsDeliverToLocationGln(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsDeliverToLocationIdentifier(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsSupplierContract(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsRequisitionLine(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsStorageLocation(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsCostCenter(determineCostCenter());
        goodsLine.setGoodsCostCenterExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsProject(determineProject());
        goodsLine.setGoodsProjectExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsGrant(determineGrant());
        goodsLine.setGoodsGrantExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsGift(determineGift());
        goodsLine.setGoodsGiftExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsFund(determineFund());
        goodsLine.setGoodsFundExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsAlternateItemIdentifierRowId(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsAlternateItemIdentifierType(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsAlternateItemIdentifierValue(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsAlternateItemIdentifierUnitOfMeasure(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsAlternateItemIdentifierManufacturer(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsItemTag(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsCloseStatus(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsClosedRowId(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsClosedOn(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsClosedBy(CemiBaseConstants.EMPTY_STRING);
        goodsLine.setGoodsClosedReasonCode(CemiBaseConstants.EMPTY_STRING);

        goodsLine.setLineSplits(determineLineSplits());

        return goodsLine;
    }

    private String determineLineNumber() {
        return isEmptyFactory() ? CemiBaseConstants.EMPTY_STRING : purchaseOrderItem.get().getItemLineNumber().toString();
    }

    private String determinePurchaseOrderLineId(final String lineNumber) {
        if (isEmptyFactory()) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        final PurchaseOrderDocument document = (PurchaseOrderDocument) purchaseOrderItem.get().getPurapDocument();
        return StringUtils.joinWith(CUKFSConstants.UNDERSCORE,
                document.getPurapDocumentIdentifier().toString(), lineNumber);
    }

    private String determineCompany() {
        return isEmptyFactory() ? CemiBaseConstants.EMPTY_STRING : headerBo.getCompany();
    }

    @SuppressWarnings("deprecation")
    private String determineCatalogNumber() {
        if (isEmptyFactory()) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        final String catalogNumber = purchaseOrderItem.get().getItemCatalogNumber();
        if (StringUtils.isNotBlank(catalogNumber)
                && !StringUtils.equalsIgnoreCase(catalogNumber, CemiPurchaseOrderConstants.CATALOG_NUMBER_NONE)) {
            return catalogNumber;
        } else {
            return CemiBaseConstants.EMPTY_STRING;
        }
    }

    private String determineUNSPSCCode() {
        return isEmptyFactory() ? CemiBaseConstants.EMPTY_STRING : purchaseOrderItem.get().getPurchasingCommodityCode();
    }

    private String determineItemDescription() {
        return isEmptyFactory() ? CemiBaseConstants.EMPTY_STRING : purchaseOrderItem.get().getItemDescription();
    }

    /*
     * TODO: At a future date, implement logic for deriving the appropriate Workday Spend Category.
     * Setting to blank for now as instructed by Huron; they should populate it with an appropriate default,
     * given that it's a required field.
     */
    private String determineResourceCategory() {
        return CemiBaseConstants.EMPTY_STRING;
    }

    private String determineOutstandingQuantity() {
        return isEmptyFactory()
                ? CemiBaseConstants.EMPTY_STRING
                : CemiPurchaseOrderUtils.formatQuantity(purchaseOrderItem.get().getItemOutstandingEncumberedQuantity());
    }

    private String determineUnitOfMeasure() {
        return isEmptyFactory() ? CemiBaseConstants.EMPTY_STRING : purchaseOrderItem.get().getItemUnitOfMeasureCode();
    }

    private String determineUnitCost() {
        return isEmptyFactory()
                ? CemiBaseConstants.EMPTY_STRING
                : CemiPurchaseOrderUtils.formatAmount(purchaseOrderItem.get().getItemUnitPrice());
    }

    private String determineOutstandingExtendedAmount() {
        return isEmptyFactory()
                ? CemiBaseConstants.EMPTY_STRING
                : CemiPurchaseOrderUtils.formatAmount(purchaseOrderItem.get().getItemOutstandingEncumberedAmount());
    }

    private String determineMemo() {
        if (isEmptyFactory()) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        final String totalLineAmount = CemiPurchaseOrderUtils.formatAmount(purchaseOrderItem.get().getTotalAmount());
        return StringUtils.join(CemiPurchaseOrderConstants.ORIGINAL_PO_LINE_AMOUNT_MEMO_PREFIX, totalLineAmount);
    }

    private String determineRequester() {
        return isEmptyFactory() ? CemiBaseConstants.EMPTY_STRING : headerBo.getWorker();
    }

    // TODO: At a future date, update this method to use a Cost Center mapping table or API.
    private String determineCostCenter() {
        return itemHasExactlyOneOutstandingAccountingLine()
                ? CemiBaseConstants.DEFAULT_ITHACA_COST_CENTER : CemiBaseConstants.EMPTY_STRING;
    }

    // TODO: At a future date, update this method to use a Project mapping table or API, but only in single-acct cases.
    private String determineProject() {
        return CemiBaseConstants.EMPTY_STRING;
    }

    // TODO: At a future date, update this method to use a Grant mapping table or API, but only in single-acct cases.
    private String determineGrant() {
        return CemiBaseConstants.EMPTY_STRING;
    }

    // TODO: At a future date, update this method to use a Gift mapping table or API, but only in single-acct cases.
    private String determineGift() {
        return CemiBaseConstants.EMPTY_STRING;
    }

    // TODO: At a future date, update this method to use a Fund mapping table or API, but only in single-acct cases.
    private String determineFund() {
        return CemiBaseConstants.EMPTY_STRING;
    }

    private List<CemiPurchaseOrderLineSplitBo> determineLineSplits() {
        if (itemAccountingLines.size() > 1) {
            final Stream.Builder<CemiPurchaseOrderLineSplitBo> lineSplits = Stream.builder();
            int accountIndex = 0;
            for (final PurchaseOrderAccount itemAccountingLine : itemAccountingLines) {
                accountIndex++;
                final CemiPurchaseOrderLineSplitBo lineSplit = CemiPurchaseOrderLineSplitBoFactory
                        .createLineSplitBoFrom(purchaseOrderItem, Optional.of(itemAccountingLine),
                                accountIndex);
                lineSplits.add(lineSplit);
            }
            return lineSplits.build().collect(Collectors.toUnmodifiableList());
        } else {
            final CemiPurchaseOrderLineSplitBo emptyLineSplit = CemiPurchaseOrderLineSplitBoFactory
                    .createLineSplitBoFrom(Optional.empty(), Optional.empty(), -1);
            return List.of(emptyLineSplit);
        }
    }

    private boolean itemHasExactlyOneOutstandingAccountingLine() {
        return itemAccountingLines.size() == 1;
    }

    private boolean isEmptyFactory() {
        return purchaseOrderItem.isEmpty();
    }

}
