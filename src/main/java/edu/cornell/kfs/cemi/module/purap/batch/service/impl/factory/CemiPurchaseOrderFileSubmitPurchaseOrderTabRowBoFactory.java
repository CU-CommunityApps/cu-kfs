package edu.cornell.kfs.cemi.module.purap.batch.service.impl.factory;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderFileSubmitPurchaseOrderTabRowBo;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderGoodsLineBo;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderHeaderBo;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderLineSplitBo;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderServiceLineBo;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;

public class CemiPurchaseOrderFileSubmitPurchaseOrderTabRowBoFactory {

    private CemiPurchaseOrderHeaderBo headerBo;
    private Pair<CemiPurchaseOrderGoodsLineBo, CemiPurchaseOrderLineSplitBo> goodsLineWithSplit;
    private Pair<CemiPurchaseOrderServiceLineBo, CemiPurchaseOrderLineSplitBo> serviceLineWithSplit;

    public CemiPurchaseOrderFileSubmitPurchaseOrderTabRowBoFactory (final CemiPurchaseOrderHeaderBo headerBo,
            final Pair<CemiPurchaseOrderGoodsLineBo, CemiPurchaseOrderLineSplitBo> goodsLineWithSplit,
            final Pair<CemiPurchaseOrderServiceLineBo, CemiPurchaseOrderLineSplitBo> serviceLineWithSplit) {
        Validate.notNull(headerBo, "headerBo cannot be null");
        Validate.notNull(goodsLineWithSplit, "goodsLineWithSplit wrapper object cannot be null");
        Validate.notNull(serviceLineWithSplit, "serviceLineWithSplit wrapper object cannot be null");
        Validate.notNull(goodsLineWithSplit.getLeft(), "Goods Line BO cannot be null; an empty placeholder BO "
                + "must be provided if a given row does not have a Goods Line");
        Validate.notNull(serviceLineWithSplit.getLeft(), "Service Line BO cannot be null; an empty placeholder BO "
                + "must be provided if a given row does not have a Service Line");
        Validate.notNull(goodsLineWithSplit.getRight(), "Line Split BO for Goods Line cannot be null; an empty "
                + "placeholder BO must be provided if a given row does not have a split for its Goods Line");
        Validate.notNull(serviceLineWithSplit.getLeft(), "Line Split BO for Service Line cannot be null; an empty "
                + "placeholder BO must be provided if a given row does not have a split for its Service Line");
        this.headerBo = headerBo;
        this.goodsLineWithSplit = goodsLineWithSplit;
        this.serviceLineWithSplit = serviceLineWithSplit;
    }

    public static CemiPurchaseOrderFileSubmitPurchaseOrderTabRowBo createTabRowBoFrom(
            final CemiPurchaseOrderHeaderBo headerBo,
            final Pair<CemiPurchaseOrderGoodsLineBo, CemiPurchaseOrderLineSplitBo> goodsLineWithSplit,
            final Pair<CemiPurchaseOrderServiceLineBo, CemiPurchaseOrderLineSplitBo> serviceLineWithSplit) {
        final CemiPurchaseOrderFileSubmitPurchaseOrderTabRowBoFactory factory
                 = new CemiPurchaseOrderFileSubmitPurchaseOrderTabRowBoFactory(
                        headerBo, goodsLineWithSplit, serviceLineWithSplit);
        return factory.createCemiPurchaseOrderFileSubmitPurchaseOrderTabRowBo();
    }

    public CemiPurchaseOrderFileSubmitPurchaseOrderTabRowBo createCemiPurchaseOrderFileSubmitPurchaseOrderTabRowBo() {
        final CemiPurchaseOrderFileSubmitPurchaseOrderTabRowBo poRowBo
                = new CemiPurchaseOrderFileSubmitPurchaseOrderTabRowBo();

        final CemiPurchaseOrderGoodsLineBo goodsLine = goodsLineWithSplit.getLeft();
        final CemiPurchaseOrderServiceLineBo serviceLine = serviceLineWithSplit.getLeft();
        final CemiPurchaseOrderLineSplitBo goodsLineSplit = goodsLineWithSplit.getRight();
        final CemiPurchaseOrderLineSplitBo serviceLineSplit = serviceLineWithSplit.getRight();

        poRowBo.setKfsDocumentNumber(headerBo.getKfsDocumentNumber());
        poRowBo.setKfsPurchaseOrderId(headerBo.getKfsPurchaseOrderId());

        poRowBo.setSpreadsheetKey(headerBo.getSpreadsheetKey());
        poRowBo.setAddOnly(headerBo.getAddOnly());
        poRowBo.setExistingPurchaseOrderDocumentNumber(headerBo.getExistingPurchaseOrderDocumentNumber());
        poRowBo.setOrderTypeReference(headerBo.getOrderTypeReference());
        poRowBo.setAutoComplete(headerBo.getAutoComplete());
        poRowBo.setComment(headerBo.getComment());
        poRowBo.setWorker(headerBo.getWorker());
        poRowBo.setPurchaseOrderId(headerBo.getPurchaseOrderId());
        poRowBo.setSubmit(headerBo.getSubmit());
        poRowBo.setLockedInWorkday(headerBo.getLockedInWorkday());
        poRowBo.setDocumentNumber(headerBo.getDocumentNumber());
        poRowBo.setInvoiceStatus(headerBo.getInvoiceStatus());
        poRowBo.setPaymentStatus(headerBo.getPaymentStatus());
        poRowBo.setReceivingStatus(headerBo.getReceivingStatus());
        poRowBo.setShippingStatus(headerBo.getShippingStatus());
        poRowBo.setTrackingStatus(headerBo.getTrackingStatus());
        poRowBo.setCompany(headerBo.getCompany());
        poRowBo.setSupplier(headerBo.getSupplier());
        poRowBo.setPurchaseOrderType(headerBo.getPurchaseOrderType());
        poRowBo.setExternalPoNumber(headerBo.getExternalPoNumber());
        poRowBo.setOrderFromSupplierConnection(headerBo.getOrderFromSupplierConnection());
        poRowBo.setDocumentDate(headerBo.getDocumentDate());
        poRowBo.setTaxAmount(headerBo.getTaxAmount());
        poRowBo.setFreightAmount(headerBo.getFreightAmount());
        poRowBo.setOtherCharges(headerBo.getOtherCharges());
        poRowBo.setPaymentTerms(headerBo.getPaymentTerms());
        poRowBo.setOverridePaymentType(headerBo.getOverridePaymentType());
        poRowBo.setProcurementCreditCard(headerBo.getProcurementCreditCard());
        poRowBo.setShippingTerms(headerBo.getShippingTerms());
        poRowBo.setShippingMethod(headerBo.getShippingMethod());
        poRowBo.setShippingInstruction(headerBo.getShippingInstruction());
        poRowBo.setDueDate(headerBo.getDueDate());
        poRowBo.setSupplierContract(headerBo.getSupplierContract());
        poRowBo.setExternalSupplierInvoiceSource(headerBo.getExternalSupplierInvoiceSource());
        poRowBo.setCurrency(headerBo.getCurrency());
        poRowBo.setAcknowledgementExpected(headerBo.getAcknowledgementExpected());
        poRowBo.setDefaultTaxOption(headerBo.getDefaultTaxOption());
        poRowBo.setDefaultTaxCode(headerBo.getDefaultTaxCode());
        poRowBo.setIssueOption(headerBo.getIssueOption());
        poRowBo.setEmailRowId(headerBo.getEmailRowId());
        poRowBo.setEmailId(headerBo.getEmailId());
        poRowBo.setEmailAddress(headerBo.getEmailAddress());
        poRowBo.setBuyer(headerBo.getBuyer());
        poRowBo.setBillToContact(headerBo.getBillToContact());
        poRowBo.setBillToContactDetail(headerBo.getBillToContactDetail());
        poRowBo.setExistingBillToAddressId(headerBo.getExistingBillToAddressId());
        poRowBo.setNewBillToAddressId(headerBo.getNewBillToAddressId());
        poRowBo.setShipToContact(headerBo.getShipToContact());
        poRowBo.setShipToContactDetail(headerBo.getShipToContactDetail());
        poRowBo.setExistingShipToAddressId(headerBo.getExistingShipToAddressId());
        poRowBo.setNewShipToAddressId(headerBo.getNewShipToAddressId());
        poRowBo.setDocumentLink(headerBo.getDocumentLink());
        poRowBo.setMemoForSupplier(headerBo.getMemoForSupplier());
        poRowBo.setInternalMemo(headerBo.getInternalMemo());
        poRowBo.setPrepaid(headerBo.getPrepaid());
        poRowBo.setPrepaymentReleaseType(headerBo.getPrepaymentReleaseType());
        poRowBo.setExpectedReleaseDate(headerBo.getExpectedReleaseDate());
        poRowBo.setFrequency(headerBo.getFrequency());
        poRowBo.setNumberOfPrepaymentInstallments(headerBo.getNumberOfPrepaymentInstallments());
        poRowBo.setUseInvoiceDate(headerBo.getUseInvoiceDate());
        poRowBo.setSpecifiedDate(headerBo.getSpecifiedDate());
        poRowBo.setUsePrepaidPostingRulesForReceiptAccruals(headerBo.getUsePrepaidPostingRulesForReceiptAccruals());
        poRowBo.setPercentToRetain(headerBo.getPercentToRetain());
        poRowBo.setEstimatedRetentionReleaseDate(headerBo.getEstimatedRetentionReleaseDate());
        poRowBo.setXmlname3rdPartyRetention(headerBo.getXmlname3rdPartyRetention());
        poRowBo.setRetentionMemo(headerBo.getRetentionMemo());
        poRowBo.setDownPaymentAmount(headerBo.getDownPaymentAmount());
        poRowBo.setDownPaymentPercentage(headerBo.getDownPaymentPercentage());
        poRowBo.setDownPaymentMemo(headerBo.getDownPaymentMemo());
        poRowBo.setProcedureDate(headerBo.getProcedureDate());
        poRowBo.setProcedure(headerBo.getProcedure());
        poRowBo.setProcedureNumber(headerBo.getProcedureNumber());
        poRowBo.setPatientId(headerBo.getPatientId());
        poRowBo.setMedicalRecordNumber(headerBo.getMedicalRecordNumber());
        poRowBo.setPhysicianId(headerBo.getPhysicianId());
        poRowBo.setVerifiedBy(headerBo.getVerifiedBy());
        poRowBo.setSupplierRepresentative(headerBo.getSupplierRepresentative());
        poRowBo.setSupplierSalesOrderNumber(headerBo.getSupplierSalesOrderNumber());
        poRowBo.setAdditionalProcedureDetails(headerBo.getAdditionalProcedureDetails());
        poRowBo.setGoodsRowId(goodsLine.getGoodsRowId());
        poRowBo.setGoodsCatalogItem(goodsLine.getGoodsCatalogItem());
        poRowBo.setGoodsPurchaseOrderLineId(goodsLine.getGoodsPurchaseOrderLineId());
        poRowBo.setGoodsLineNumber(goodsLine.getGoodsLineNumber());
        poRowBo.setGoodsLineCompany(goodsLine.getGoodsLineCompany());
        poRowBo.setGoodsSupplierItemIdentifier(goodsLine.getGoodsSupplierItemIdentifier());
        poRowBo.setGoodsSupplierPartId(goodsLine.getGoodsSupplierPartId());
        poRowBo.setGoodsSupplierPartAuxiliaryId(goodsLine.getGoodsSupplierPartAuxiliaryId());
        poRowBo.setGoodsUnspscCode(goodsLine.getGoodsUnspscCode());
        poRowBo.setGoodsItemDescription(goodsLine.getGoodsItemDescription());
        poRowBo.setGoodsSupplierContractLine(goodsLine.getGoodsSupplierContractLine());
        poRowBo.setGoodsAlternateSupplierContract(goodsLine.getGoodsAlternateSupplierContract());
        poRowBo.setGoodsCommodityCode(goodsLine.getGoodsCommodityCode());
        poRowBo.setGoodsCommodityCodeType(goodsLine.getGoodsCommodityCodeType());
        poRowBo.setGoodsPaymentStatus(goodsLine.getGoodsPaymentStatus());
        poRowBo.setGoodsInvoiceStatus(goodsLine.getGoodsInvoiceStatus());
        poRowBo.setGoodsReceivingStatus(goodsLine.getGoodsReceivingStatus());
        poRowBo.setGoodsShippingStatus(goodsLine.getGoodsShippingStatus());
        poRowBo.setGoodsTrackingStatus(goodsLine.getGoodsTrackingStatus());
        poRowBo.setGoodsResourceCategory(goodsLine.getGoodsResourceCategory());
        poRowBo.setGoodsTaxApplicability(goodsLine.getGoodsTaxApplicability());
        poRowBo.setGoodsTaxCode(goodsLine.getGoodsTaxCode());
        poRowBo.setGoodsTaxRate1(goodsLine.getGoodsTaxRate1());
        poRowBo.setGoodsTaxRecoverability1(goodsLine.getGoodsTaxRecoverability1());
        poRowBo.setGoodsTaxOption1(goodsLine.getGoodsTaxOption1());
        poRowBo.setGoodsTaxRate2(goodsLine.getGoodsTaxRate2());
        poRowBo.setGoodsTaxRecoverability2(goodsLine.getGoodsTaxRecoverability2());
        poRowBo.setGoodsTaxOption2(goodsLine.getGoodsTaxOption2());
        poRowBo.setGoodsTaxRate3(goodsLine.getGoodsTaxRate3());
        poRowBo.setGoodsTaxRecoverability3(goodsLine.getGoodsTaxRecoverability3());
        poRowBo.setGoodsTaxOption3(goodsLine.getGoodsTaxOption3());
        poRowBo.setGoodsTaxRate4(goodsLine.getGoodsTaxRate4());
        poRowBo.setGoodsTaxRecoverability4(goodsLine.getGoodsTaxRecoverability4());
        poRowBo.setGoodsTaxOption4(goodsLine.getGoodsTaxOption4());
        poRowBo.setGoodsTaxRate5(goodsLine.getGoodsTaxRate5());
        poRowBo.setGoodsTaxRecoverability5(goodsLine.getGoodsTaxRecoverability5());
        poRowBo.setGoodsTaxOption5(goodsLine.getGoodsTaxOption5());
        poRowBo.setGoodsTaxRate6(goodsLine.getGoodsTaxRate6());
        poRowBo.setGoodsTaxRecoverability6(goodsLine.getGoodsTaxRecoverability6());
        poRowBo.setGoodsTaxOption6(goodsLine.getGoodsTaxOption6());
        poRowBo.setGoodsPackagingString(goodsLine.getGoodsPackagingString());
        poRowBo.setGoodsQuantity(goodsLine.getGoodsQuantity());
        poRowBo.setGoodsUnitOfMeasure(goodsLine.getGoodsUnitOfMeasure());
        poRowBo.setGoodsUnitCost(goodsLine.getGoodsUnitCost());
        poRowBo.setGoodsRequestedAsNoCharge(goodsLine.getGoodsRequestedAsNoCharge());
        poRowBo.setGoodsExtendedAmount(goodsLine.getGoodsExtendedAmount());
        poRowBo.setGoodsLotSerialInformation(goodsLine.getGoodsLotSerialInformation());
        poRowBo.setGoodsLotNumber(goodsLine.getGoodsLotNumber());
        poRowBo.setGoodsSerialNumber(goodsLine.getGoodsSerialNumber());
        poRowBo.setGoodsDueDate(goodsLine.getGoodsDueDate());
        poRowBo.setGoodsDeliveryType(goodsLine.getGoodsDeliveryType());
        poRowBo.setGoodsPrepaid(goodsLine.getGoodsPrepaid());
        poRowBo.setGoodsDownPayment(goodsLine.getGoodsDownPayment());
        poRowBo.setGoodsRetention(goodsLine.getGoodsRetention());
        poRowBo.setGoodsRequestedDeliveryDate(goodsLine.getGoodsRequestedDeliveryDate());
        poRowBo.setGoodsBudgetDate(goodsLine.getGoodsBudgetDate());
        poRowBo.setGoodsMemo(goodsLine.getGoodsMemo());
        poRowBo.setGoodsShipToAddress(goodsLine.getGoodsShipToAddress());
        poRowBo.setGoodsShipToGlobalLocationNumber(goodsLine.getGoodsShipToGlobalLocationNumber());
        poRowBo.setGoodsShipToLocationIdentifier(goodsLine.getGoodsShipToLocationIdentifier());
        poRowBo.setGoodsShipToContact(goodsLine.getGoodsShipToContact());
        poRowBo.setGoodsRequester(goodsLine.getGoodsRequester());
        poRowBo.setGoodsDeliverToLocation(goodsLine.getGoodsDeliverToLocation());
        poRowBo.setGoodsDeliverToLocationGln(goodsLine.getGoodsDeliverToLocationGln());
        poRowBo.setGoodsDeliverToLocationIdentifier(goodsLine.getGoodsDeliverToLocationIdentifier());
        poRowBo.setGoodsSupplierContract(goodsLine.getGoodsSupplierContract());
        poRowBo.setGoodsExternalSupplierInvoiceSource(goodsLine.getGoodsExternalSupplierInvoiceSource());
        poRowBo.setGoodsRequisitionLine(goodsLine.getGoodsRequisitionLine());
        poRowBo.setGoodsStorageLocation(goodsLine.getGoodsStorageLocation());
        poRowBo.setGoodsCostCenter(goodsLine.getGoodsCostCenter());
        poRowBo.setGoodsCostCenterExternalSupplierInvoiceSource(
                goodsLine.getGoodsCostCenterExternalSupplierInvoiceSource());
        poRowBo.setGoodsProject(goodsLine.getGoodsProject());
        poRowBo.setGoodsProjectExternalSupplierInvoiceSource(goodsLine.getGoodsProjectExternalSupplierInvoiceSource());
        poRowBo.setGoodsGrant(goodsLine.getGoodsGrant());
        poRowBo.setGoodsGrantExternalSupplierInvoiceSource(goodsLine.getGoodsGrantExternalSupplierInvoiceSource());
        poRowBo.setGoodsGift(goodsLine.getGoodsGift());
        poRowBo.setGoodsGiftExternalSupplierInvoiceSource(goodsLine.getGoodsGiftExternalSupplierInvoiceSource());
        poRowBo.setGoodsFund(goodsLine.getGoodsFund());
        poRowBo.setGoodsFundExternalSupplierInvoiceSource(goodsLine.getGoodsFundExternalSupplierInvoiceSource());
        poRowBo.setGoodsLineSplitRowId(goodsLineSplit.getLineSplitRowId());
        poRowBo.setGoodsExistingBusinessDocumentLineSplitId(goodsLineSplit.getExistingBusinessDocumentLineSplitId());
        poRowBo.setGoodsNewBusinessDocumentLineSplitId(goodsLineSplit.getNewBusinessDocumentLineSplitId());
        poRowBo.setGoodsLineSplitQuantity(goodsLineSplit.getLineSplitQuantity());
        poRowBo.setGoodsLineSplitExtendedAmount(goodsLineSplit.getLineSplitExtendedAmount());
        poRowBo.setGoodsLineSplitBudgetDate(goodsLineSplit.getLineSplitBudgetDate());
        poRowBo.setGoodsLineSplitMemo(goodsLineSplit.getLineSplitMemo());
        poRowBo.setGoodsLineSplitAllocation(goodsLineSplit.getLineSplitAllocation());
        poRowBo.setGoodsLineSplitCostCenter(goodsLineSplit.getLineSplitCostCenter());
        poRowBo.setGoodsLineSplitCostCenterExternalSupplierInvoiceSource(
                goodsLineSplit.getLineSplitCostCenterExternalSupplierInvoiceSource());
        poRowBo.setGoodsLineSplitProject(goodsLineSplit.getLineSplitProject());
        poRowBo.setGoodsLineSplitProjectExternalSupplierInvoiceSource(
                goodsLineSplit.getLineSplitProjectExternalSupplierInvoiceSource());
        poRowBo.setGoodsLineSplitGrant(goodsLineSplit.getLineSplitGrant());
        poRowBo.setGoodsLineSplitGrantExternalSupplierInvoiceSource(
                goodsLineSplit.getLineSplitGrantExternalSupplierInvoiceSource());
        poRowBo.setGoodsLineSplitGift(goodsLineSplit.getLineSplitGift());
        poRowBo.setGoodsLineSplitGiftExternalSupplierInvoiceSource(
                goodsLineSplit.getLineSplitGiftExternalSupplierInvoiceSource());
        poRowBo.setGoodsLineSplitFund(goodsLineSplit.getLineSplitFund());
        poRowBo.setGoodsLineSplitFundExternalSupplierInvoiceSource(
                goodsLineSplit.getLineSplitFundExternalSupplierInvoiceSource());
        poRowBo.setGoodsAlternateItemIdentifierRowId(goodsLine.getGoodsAlternateItemIdentifierRowId());
        poRowBo.setGoodsAlternateItemIdentifierType(goodsLine.getGoodsAlternateItemIdentifierType());
        poRowBo.setGoodsAlternateItemIdentifierValue(goodsLine.getGoodsAlternateItemIdentifierValue());
        poRowBo.setGoodsAlternateItemIdentifierUnitOfMeasure(goodsLine.getGoodsAlternateItemIdentifierUnitOfMeasure());
        poRowBo.setGoodsAlternateItemIdentifierManufacturer(goodsLine.getGoodsAlternateItemIdentifierManufacturer());
        poRowBo.setGoodsItemTag(goodsLine.getGoodsItemTag());
        poRowBo.setGoodsCloseStatus(goodsLine.getGoodsCloseStatus());
        poRowBo.setGoodsClosedRowId(goodsLine.getGoodsClosedRowId());
        poRowBo.setGoodsClosedOn(goodsLine.getGoodsClosedOn());
        poRowBo.setGoodsClosedBy(goodsLine.getGoodsClosedBy());
        poRowBo.setGoodsClosedReasonCode(goodsLine.getGoodsClosedReasonCode());
                poRowBo.setServiceRowId(serviceLine.getServiceRowId());
        poRowBo.setServiceCatalogItem(serviceLine.getServiceCatalogItem());
        poRowBo.setServiceOrderLineId(serviceLine.getServiceOrderLineId());
        poRowBo.setServiceLineNumber(serviceLine.getServiceLineNumber());
        poRowBo.setServiceLineCompany(serviceLine.getServiceLineCompany());
        poRowBo.setServiceDescription(serviceLine.getServiceDescription());
        poRowBo.setServiceSupplierContractLine(serviceLine.getServiceSupplierContractLine());
        poRowBo.setServiceAlternateSupplierContract(serviceLine.getServiceAlternateSupplierContract());
        poRowBo.setServiceCommodityCode(serviceLine.getServiceCommodityCode());
        poRowBo.setServiceCommodityCodeType(serviceLine.getServiceCommodityCodeType());
        poRowBo.setServicePaymentStatus(serviceLine.getServicePaymentStatus());
        poRowBo.setServiceInvoiceStatus(serviceLine.getServiceInvoiceStatus());
        poRowBo.setServiceReceivingStatus(serviceLine.getServiceReceivingStatus());
        poRowBo.setServiceResourceCategory(serviceLine.getServiceResourceCategory());
        poRowBo.setServiceTaxApplicability(serviceLine.getServiceTaxApplicability());
        poRowBo.setServiceTaxCode(serviceLine.getServiceTaxCode());
        poRowBo.setServiceTaxRate1(serviceLine.getServiceTaxRate1());
        poRowBo.setServiceTaxRecoverability1(serviceLine.getServiceTaxRecoverability1());
        poRowBo.setServiceTaxOption1(serviceLine.getServiceTaxOption1());
        poRowBo.setServiceTaxRate2(serviceLine.getServiceTaxRate2());
        poRowBo.setServiceTaxRecoverability2(serviceLine.getServiceTaxRecoverability2());
        poRowBo.setServiceTaxOption2(serviceLine.getServiceTaxOption2());
        poRowBo.setServiceTaxRate3(serviceLine.getServiceTaxRate3());
        poRowBo.setServiceTaxRecoverability3(serviceLine.getServiceTaxRecoverability3());
        poRowBo.setServiceTaxOption3(serviceLine.getServiceTaxOption3());
        poRowBo.setServiceTaxRate4(serviceLine.getServiceTaxRate4());
        poRowBo.setServiceTaxRecoverability4(serviceLine.getServiceTaxRecoverability4());
        poRowBo.setServiceTaxOption4(serviceLine.getServiceTaxOption4());
        poRowBo.setServiceTaxRate5(serviceLine.getServiceTaxRate5());
        poRowBo.setServiceTaxRecoverability5(serviceLine.getServiceTaxRecoverability5());
        poRowBo.setServiceTaxOption5(serviceLine.getServiceTaxOption5());
        poRowBo.setServiceTaxRate6(serviceLine.getServiceTaxRate6());
        poRowBo.setServiceTaxRecoverability6(serviceLine.getServiceTaxRecoverability6());
        poRowBo.setServiceTaxOption6(serviceLine.getServiceTaxOption6());
        poRowBo.setServiceExtendedAmount(serviceLine.getServiceExtendedAmount());
        poRowBo.setServiceDueDate(serviceLine.getServiceDueDate());
        poRowBo.setServiceStartDate(serviceLine.getServiceStartDate());
        poRowBo.setServiceEndDate(serviceLine.getServiceEndDate());
        poRowBo.setServicePrepaid(serviceLine.getServicePrepaid());
        poRowBo.setServiceDownPayment(serviceLine.getServiceDownPayment());
        poRowBo.setServiceRetention(serviceLine.getServiceRetention());
        poRowBo.setServiceBudgetDate(serviceLine.getServiceBudgetDate());
        poRowBo.setServiceMemo(serviceLine.getServiceMemo());
        poRowBo.setServiceShipToAddress(serviceLine.getServiceShipToAddress());
        poRowBo.setServiceShipToContact(serviceLine.getServiceShipToContact());
        poRowBo.setServiceRequester(serviceLine.getServiceRequester());
        poRowBo.setServiceDeliverToLocation(serviceLine.getServiceDeliverToLocation());
        poRowBo.setServiceRequisitionLine(serviceLine.getServiceRequisitionLine());
        poRowBo.setServiceSupplierContract(serviceLine.getServiceSupplierContract());
        poRowBo.setServiceSupplierContractExternalSupplierInvoiceSource(
                serviceLine.getServiceSupplierContractExternalSupplierInvoiceSource());
        poRowBo.setServiceStorageLocation(serviceLine.getServiceStorageLocation());
        poRowBo.setServiceCostCenter(serviceLine.getServiceCostCenter());
        poRowBo.setServiceCostCenterExternalSupplierInvoiceSource(
                serviceLine.getServiceCostCenterExternalSupplierInvoiceSource());
        poRowBo.setServiceProject(serviceLine.getServiceProject());
        poRowBo.setServiceProjectExternalSupplierInvoiceSource(
                serviceLine.getServiceProjectExternalSupplierInvoiceSource());
        poRowBo.setServiceGrant(serviceLine.getServiceGrant());
        poRowBo.setServiceGrantExternalSupplierInvoiceSource(serviceLine.getServiceGrantExternalSupplierInvoiceSource());
        poRowBo.setServiceGift(serviceLine.getServiceGift());
        poRowBo.setServiceGiftExternalSupplierInvoiceSource(serviceLine.getServiceGiftExternalSupplierInvoiceSource());
        poRowBo.setServiceFund(serviceLine.getServiceFund());
        poRowBo.setServiceFundExternalSupplierInvoiceSource(serviceLine.getServiceFundExternalSupplierInvoiceSource());
                poRowBo.setServiceLineSplitRowId(serviceLineSplit.getLineSplitRowId());
        poRowBo.setServiceExistingBusinessDocumentLineSplitId(serviceLineSplit.getExistingBusinessDocumentLineSplitId());
        poRowBo.setServiceNewBusinessDocumentLineSplitId(serviceLineSplit.getNewBusinessDocumentLineSplitId());
        poRowBo.setServiceLineSplitQuantity(serviceLineSplit.getLineSplitQuantity());
        poRowBo.setServiceLineSplitExtendedAmount(serviceLineSplit.getLineSplitExtendedAmount());
        poRowBo.setServiceLineSplitBudgetDate(serviceLineSplit.getLineSplitBudgetDate());
        poRowBo.setServiceLineSplitMemo(serviceLineSplit.getLineSplitMemo());
        poRowBo.setServiceLineSplitAllocation(serviceLineSplit.getLineSplitAllocation());
        poRowBo.setServiceLineSplitCostCenter(serviceLineSplit.getLineSplitCostCenter());
        poRowBo.setServiceLineSplitCostCenterExternalSupplierInvoiceSource(
                serviceLineSplit.getLineSplitCostCenterExternalSupplierInvoiceSource());
        poRowBo.setServiceLineSplitProject(serviceLineSplit.getLineSplitProject());
        poRowBo.setServiceLineSplitProjectExternalSupplierInvoiceSource(
                serviceLineSplit.getLineSplitProjectExternalSupplierInvoiceSource());
        poRowBo.setServiceLineSplitGrant(serviceLineSplit.getLineSplitGrant());
        poRowBo.setServiceLineSplitGrantExternalSupplierInvoiceSource(
                serviceLineSplit.getLineSplitGrantExternalSupplierInvoiceSource());
        poRowBo.setServiceLineSplitGift(serviceLineSplit.getLineSplitGift());
        poRowBo.setServiceLineSplitGiftExternalSupplierInvoiceSource(
                serviceLineSplit.getLineSplitGiftExternalSupplierInvoiceSource());
        poRowBo.setServiceLineSplitFund(serviceLineSplit.getLineSplitFund());
        poRowBo.setServiceLineSplitFundExternalSupplierInvoiceSource(
                serviceLineSplit.getLineSplitFundExternalSupplierInvoiceSource());
        poRowBo.setServiceCloseStatus(serviceLine.getServiceCloseStatus());
        poRowBo.setServiceClosedRowId(serviceLine.getServiceClosedRowId());
        poRowBo.setServiceClosedOn(serviceLine.getServiceClosedOn());
        poRowBo.setServiceClosedBy(serviceLine.getServiceClosedBy());
        poRowBo.setServiceClosedReasonCode(serviceLine.getServiceClosedReasonCode());
                poRowBo.setDeliverablesRowId(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesOrderLineId(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesLineNumber(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesLineCompany(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesProject(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesSupplierContractLine(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesAlternateSupplierContract(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesReceivingStatus(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesPaymentStatus(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesInvoiceStatus(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesPrepaid(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesDownPayment(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesRetention(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesBudgetDate(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesMemo(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesRequisitionLine(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesResourceCategory(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesProjectPlanPhaseRowId(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesProjectPlanPhase(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesProjectPlanTaskRowId(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesProjectPlanTask(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesProjectSubtaskRowId(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesProjectSubtaskDescription(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesProjectSubtaskAmount(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesExtendedAmount(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesWorktags(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesWorktagsExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesSupplierContract(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesSupplierContractExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesLineSplitRowId(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesExistingBusinessDocumentLineSplitId(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesNewBusinessDocumentLineSplitId(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesLineSplitQuantity(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesLineSplitExtendedAmount(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesLineSplitBudgetDate(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesLineSplitMemo(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesLineSplitAllocation(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesLineSplitWorktag(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesLineSplitWorktagExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesCloseStatus(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesClosedRowId(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesClosedOn(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesClosedBy(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setDeliverablesClosedReasonCode(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setTaxCodeRowId(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setTaxCodeApplicability(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setTaxCode(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setTaxCodeAmount(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setTaxRateRowId(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setTaxRate(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setTaxRateAmount(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setTaxRateRecoverability(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setTaxRateType(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setTaxRatePointDateType(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setTaxRatePointDate(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setAttachmentRowId(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setAttachmentContentType(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setAttachmentFilename(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setAttachmentEncoding(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setAttachmentCompressed(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setAttachmentFileContent(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setAttachmentComment(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setAttachmentExternal(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setAttachmentCategory(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setAmortizationRowId(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setAmortizationFrequency(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setAmortizationNumberOfPrepaymentInstallments(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setAmortizationUseInvoiceDate(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setAmortizationSpecifiedDate(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setAmortizationIncludeAllAvailablePrepaidLines(CemiBaseConstants.EMPTY_STRING);
        poRowBo.setAmortizationPurchaseOrderLineNumber(CemiBaseConstants.EMPTY_STRING);

        return poRowBo;
    }

}
