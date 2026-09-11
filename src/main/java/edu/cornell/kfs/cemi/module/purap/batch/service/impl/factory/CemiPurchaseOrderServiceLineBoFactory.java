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
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderHeaderBo;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderLineSplitBo;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderServiceLineBo;
import edu.cornell.kfs.cemi.module.purap.util.CemiPurchaseOrderUtils;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CemiPurchaseOrderServiceLineBoFactory {

    private CemiPurchaseOrderHeaderBo headerBo;
    private Optional<PurchaseOrderItem> purchaseOrderItem;
    private List<PurchaseOrderAccount> itemAccountingLines;

    public CemiPurchaseOrderServiceLineBoFactory(final CemiPurchaseOrderHeaderBo headerBo,
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

    public static CemiPurchaseOrderServiceLineBo createServiceLineBoFrom(CemiPurchaseOrderHeaderBo headerBo,
            final Optional<PurchaseOrderItem> purchaseOrderItem) {
        final CemiPurchaseOrderServiceLineBoFactory factory = new CemiPurchaseOrderServiceLineBoFactory(
                headerBo, purchaseOrderItem);
        return factory.createCemiPurchaseOrderServiceLineBo();
    }

    public CemiPurchaseOrderServiceLineBo createCemiPurchaseOrderServiceLineBo() {
        final CemiPurchaseOrderServiceLineBo serviceLine = new CemiPurchaseOrderServiceLineBo();

        final String lineNumber = determineLineNumber();

        serviceLine.setServiceRowId(lineNumber);
        serviceLine.setServiceCatalogItem(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceOrderLineId(determineServiceOrderLineId(lineNumber));
        serviceLine.setServiceLineNumber(lineNumber);
        serviceLine.setServiceLineCompany(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceDescription(determineItemDescription());
        serviceLine.setServiceSupplierContractLine(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceAlternateSupplierContract(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceCommodityCode(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceCommodityCodeType(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServicePaymentStatus(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceInvoiceStatus(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceReceivingStatus(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceResourceCategory(determineResourceCategory());
        serviceLine.setServiceTaxApplicability(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceTaxCode(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceTaxRate1(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceTaxRecoverability1(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceTaxOption1(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceTaxRate2(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceTaxRecoverability2(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceTaxOption2(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceTaxRate3(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceTaxRecoverability3(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceTaxOption3(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceTaxRate4(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceTaxRecoverability4(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceTaxOption4(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceTaxRate5(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceTaxRecoverability5(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceTaxOption5(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceTaxRate6(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceTaxRecoverability6(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceTaxOption6(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceExtendedAmount(determineOutstandingExtendedAmount());
        serviceLine.setServiceDueDate(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceStartDate(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceEndDate(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServicePrepaid(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceDownPayment(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceRetention(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceBudgetDate(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceMemo(determineMemo());
        serviceLine.setServiceShipToAddress(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceShipToContact(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceRequester(determineRequester());
        serviceLine.setServiceDeliverToLocation(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceRequisitionLine(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceSupplierContract(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceSupplierContractExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceStorageLocation(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceCostCenter(determineCostCenter());
        serviceLine.setServiceCostCenterExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceProject(determineProject());
        serviceLine.setServiceProjectExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceGrant(determineGrant());
        serviceLine.setServiceGrantExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceGift(determineGift());
        serviceLine.setServiceGiftExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceFund(determineFund());
        serviceLine.setServiceFundExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceCloseStatus(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceClosedRowId(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceClosedOn(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceClosedBy(CemiBaseConstants.EMPTY_STRING);
        serviceLine.setServiceClosedReasonCode(CemiBaseConstants.EMPTY_STRING);

        serviceLine.setLineSplits(determineLineSplits());

        return serviceLine;
    }

    private String determineLineNumber() {
        return isEmptyFactory() ? CemiBaseConstants.EMPTY_STRING : purchaseOrderItem.get().getItemLineNumber().toString();
    }

    private String determineServiceOrderLineId(final String lineNumber) {
        if (isEmptyFactory()) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        final PurchaseOrderDocument document = (PurchaseOrderDocument) purchaseOrderItem.get().getPurapDocument();
        return StringUtils.joinWith(CUKFSConstants.UNDERSCORE,
                document.getPurapDocumentIdentifier().toString(), lineNumber);
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
