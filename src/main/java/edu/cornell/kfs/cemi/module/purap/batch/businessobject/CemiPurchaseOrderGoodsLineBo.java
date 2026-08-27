package edu.cornell.kfs.cemi.module.purap.batch.businessobject;

import java.util.ArrayList;
import java.util.List;

public class CemiPurchaseOrderGoodsLineBo {

    private String goodsRowId;
    private String goodsCatalogItem;
    private String goodsPurchaseOrderLineId;
    private String goodsLineNumber;
    private String goodsLineCompany;
    private String goodsSupplierItemIdentifier;
    private String goodsSupplierPartId;
    private String goodsSupplierPartAuxiliaryId;
    private String goodsUnspscCode;
    private String goodsItemDescription;
    private String goodsSupplierContractLine;
    private String goodsAlternateSupplierContract;
    private String goodsCommodityCode;
    private String goodsCommodityCodeType;
    private String goodsPaymentStatus;
    private String goodsInvoiceStatus;
    private String goodsReceivingStatus;
    private String goodsShippingStatus;
    private String goodsTrackingStatus;
    private String goodsResourceCategory;
    private String goodsTaxApplicability;
    private String goodsTaxCode;
    private String goodsTaxRate1;
    private String goodsTaxRecoverability1;
    private String goodsTaxOption1;
    private String goodsTaxRate2;
    private String goodsTaxRecoverability2;
    private String goodsTaxOption2;
    private String goodsTaxRate3;
    private String goodsTaxRecoverability3;
    private String goodsTaxOption3;
    private String goodsTaxRate4;
    private String goodsTaxRecoverability4;
    private String goodsTaxOption4;
    private String goodsTaxRate5;
    private String goodsTaxRecoverability5;
    private String goodsTaxOption5;
    private String goodsTaxRate6;
    private String goodsTaxRecoverability6;
    private String goodsTaxOption6;
    private String goodsPackagingString;
    private String goodsQuantity;
    private String goodsUnitOfMeasure;
    private String goodsUnitCost;
    private String goodsRequestedAsNoCharge;
    private String goodsExtendedAmount;
    private String goodsLotSerialInformation;
    private String goodsLotNumber;
    private String goodsSerialNumber;
    private String goodsDueDate;
    private String goodsDeliveryType;
    private String goodsPrepaid;
    private String goodsDownPayment;
    private String goodsRetention;
    private String goodsRequestedDeliveryDate;
    private String goodsBudgetDate;
    private String goodsMemo;
    private String goodsShipToAddress;
    private String goodsShipToGlobalLocationNumber;
    private String goodsShipToLocationIdentifier;
    private String goodsShipToContact;
    private String goodsRequester;
    private String goodsDeliverToLocation;
    private String goodsDeliverToLocationGln;
    private String goodsDeliverToLocationIdentifier;
    private String goodsSupplierContract;
    private String goodsExternalSupplierInvoiceSource;
    private String goodsRequisitionLine;
    private String goodsStorageLocation;
    private String goodsCostCenter;
    private String goodsCostCenterExternalSupplierInvoiceSource;
    private String goodsProject;
    private String goodsProjectExternalSupplierInvoiceSource;
    private String goodsGrant;
    private String goodsGrantExternalSupplierInvoiceSource;
    private String goodsGift;
    private String goodsGiftExternalSupplierInvoiceSource;
    private String goodsFund;
    private String goodsFundExternalSupplierInvoiceSource;
    private String goodsAlternateItemIdentifierRowId;
    private String goodsAlternateItemIdentifierType;
    private String goodsAlternateItemIdentifierValue;
    private String goodsAlternateItemIdentifierUnitOfMeasure;
    private String goodsAlternateItemIdentifierManufacturer;
    private String goodsItemTag;
    private String goodsCloseStatus;
    private String goodsClosedRowId;
    private String goodsClosedOn;
    private String goodsClosedBy;
    private String goodsClosedReasonCode;

    private List<CemiPurchaseOrderLineSplitBo> lineSplits;

    public String getGoodsRowId() {
        return goodsRowId;
    }

    public void setGoodsRowId(final String goodsRowId) {
        this.goodsRowId = goodsRowId;
    }

    public String getGoodsCatalogItem() {
        return goodsCatalogItem;
    }

    public void setGoodsCatalogItem(final String goodsCatalogItem) {
        this.goodsCatalogItem = goodsCatalogItem;
    }

    public String getGoodsPurchaseOrderLineId() {
        return goodsPurchaseOrderLineId;
    }

    public void setGoodsPurchaseOrderLineId(final String goodsPurchaseOrderLineId) {
        this.goodsPurchaseOrderLineId = goodsPurchaseOrderLineId;
    }

    public String getGoodsLineNumber() {
        return goodsLineNumber;
    }

    public void setGoodsLineNumber(final String goodsLineNumber) {
        this.goodsLineNumber = goodsLineNumber;
    }

    public String getGoodsLineCompany() {
        return goodsLineCompany;
    }

    public void setGoodsLineCompany(final String goodsLineCompany) {
        this.goodsLineCompany = goodsLineCompany;
    }

    public String getGoodsSupplierItemIdentifier() {
        return goodsSupplierItemIdentifier;
    }

    public void setGoodsSupplierItemIdentifier(final String goodsSupplierItemIdentifier) {
        this.goodsSupplierItemIdentifier = goodsSupplierItemIdentifier;
    }

    public String getGoodsSupplierPartId() {
        return goodsSupplierPartId;
    }

    public void setGoodsSupplierPartId(final String goodsSupplierPartId) {
        this.goodsSupplierPartId = goodsSupplierPartId;
    }

    public String getGoodsSupplierPartAuxiliaryId() {
        return goodsSupplierPartAuxiliaryId;
    }

    public void setGoodsSupplierPartAuxiliaryId(final String goodsSupplierPartAuxiliaryId) {
        this.goodsSupplierPartAuxiliaryId = goodsSupplierPartAuxiliaryId;
    }

    public String getGoodsUnspscCode() {
        return goodsUnspscCode;
    }

    public void setGoodsUnspscCode(final String goodsUnspscCode) {
        this.goodsUnspscCode = goodsUnspscCode;
    }

    public String getGoodsItemDescription() {
        return goodsItemDescription;
    }

    public void setGoodsItemDescription(final String goodsItemDescription) {
        this.goodsItemDescription = goodsItemDescription;
    }

    public String getGoodsSupplierContractLine() {
        return goodsSupplierContractLine;
    }

    public void setGoodsSupplierContractLine(final String goodsSupplierContractLine) {
        this.goodsSupplierContractLine = goodsSupplierContractLine;
    }

    public String getGoodsAlternateSupplierContract() {
        return goodsAlternateSupplierContract;
    }

    public void setGoodsAlternateSupplierContract(final String goodsAlternateSupplierContract) {
        this.goodsAlternateSupplierContract = goodsAlternateSupplierContract;
    }

    public String getGoodsCommodityCode() {
        return goodsCommodityCode;
    }

    public void setGoodsCommodityCode(final String goodsCommodityCode) {
        this.goodsCommodityCode = goodsCommodityCode;
    }

    public String getGoodsCommodityCodeType() {
        return goodsCommodityCodeType;
    }

    public void setGoodsCommodityCodeType(final String goodsCommodityCodeType) {
        this.goodsCommodityCodeType = goodsCommodityCodeType;
    }

    public String getGoodsPaymentStatus() {
        return goodsPaymentStatus;
    }

    public void setGoodsPaymentStatus(final String goodsPaymentStatus) {
        this.goodsPaymentStatus = goodsPaymentStatus;
    }

    public String getGoodsInvoiceStatus() {
        return goodsInvoiceStatus;
    }

    public void setGoodsInvoiceStatus(final String goodsInvoiceStatus) {
        this.goodsInvoiceStatus = goodsInvoiceStatus;
    }

    public String getGoodsReceivingStatus() {
        return goodsReceivingStatus;
    }

    public void setGoodsReceivingStatus(final String goodsReceivingStatus) {
        this.goodsReceivingStatus = goodsReceivingStatus;
    }

    public String getGoodsShippingStatus() {
        return goodsShippingStatus;
    }

    public void setGoodsShippingStatus(final String goodsShippingStatus) {
        this.goodsShippingStatus = goodsShippingStatus;
    }

    public String getGoodsTrackingStatus() {
        return goodsTrackingStatus;
    }

    public void setGoodsTrackingStatus(final String goodsTrackingStatus) {
        this.goodsTrackingStatus = goodsTrackingStatus;
    }

    public String getGoodsResourceCategory() {
        return goodsResourceCategory;
    }

    public void setGoodsResourceCategory(final String goodsResourceCategory) {
        this.goodsResourceCategory = goodsResourceCategory;
    }

    public String getGoodsTaxApplicability() {
        return goodsTaxApplicability;
    }

    public void setGoodsTaxApplicability(final String goodsTaxApplicability) {
        this.goodsTaxApplicability = goodsTaxApplicability;
    }

    public String getGoodsTaxCode() {
        return goodsTaxCode;
    }

    public void setGoodsTaxCode(final String goodsTaxCode) {
        this.goodsTaxCode = goodsTaxCode;
    }

    public String getGoodsTaxRate1() {
        return goodsTaxRate1;
    }

    public void setGoodsTaxRate1(final String goodsTaxRate1) {
        this.goodsTaxRate1 = goodsTaxRate1;
    }

    public String getGoodsTaxRecoverability1() {
        return goodsTaxRecoverability1;
    }

    public void setGoodsTaxRecoverability1(final String goodsTaxRecoverability1) {
        this.goodsTaxRecoverability1 = goodsTaxRecoverability1;
    }

    public String getGoodsTaxOption1() {
        return goodsTaxOption1;
    }

    public void setGoodsTaxOption1(final String goodsTaxOption1) {
        this.goodsTaxOption1 = goodsTaxOption1;
    }

    public String getGoodsTaxRate2() {
        return goodsTaxRate2;
    }

    public void setGoodsTaxRate2(final String goodsTaxRate2) {
        this.goodsTaxRate2 = goodsTaxRate2;
    }

    public String getGoodsTaxRecoverability2() {
        return goodsTaxRecoverability2;
    }

    public void setGoodsTaxRecoverability2(final String goodsTaxRecoverability2) {
        this.goodsTaxRecoverability2 = goodsTaxRecoverability2;
    }

    public String getGoodsTaxOption2() {
        return goodsTaxOption2;
    }

    public void setGoodsTaxOption2(final String goodsTaxOption2) {
        this.goodsTaxOption2 = goodsTaxOption2;
    }

    public String getGoodsTaxRate3() {
        return goodsTaxRate3;
    }

    public void setGoodsTaxRate3(final String goodsTaxRate3) {
        this.goodsTaxRate3 = goodsTaxRate3;
    }

    public String getGoodsTaxRecoverability3() {
        return goodsTaxRecoverability3;
    }

    public void setGoodsTaxRecoverability3(final String goodsTaxRecoverability3) {
        this.goodsTaxRecoverability3 = goodsTaxRecoverability3;
    }

    public String getGoodsTaxOption3() {
        return goodsTaxOption3;
    }

    public void setGoodsTaxOption3(final String goodsTaxOption3) {
        this.goodsTaxOption3 = goodsTaxOption3;
    }

    public String getGoodsTaxRate4() {
        return goodsTaxRate4;
    }

    public void setGoodsTaxRate4(final String goodsTaxRate4) {
        this.goodsTaxRate4 = goodsTaxRate4;
    }

    public String getGoodsTaxRecoverability4() {
        return goodsTaxRecoverability4;
    }

    public void setGoodsTaxRecoverability4(final String goodsTaxRecoverability4) {
        this.goodsTaxRecoverability4 = goodsTaxRecoverability4;
    }

    public String getGoodsTaxOption4() {
        return goodsTaxOption4;
    }

    public void setGoodsTaxOption4(final String goodsTaxOption4) {
        this.goodsTaxOption4 = goodsTaxOption4;
    }

    public String getGoodsTaxRate5() {
        return goodsTaxRate5;
    }

    public void setGoodsTaxRate5(final String goodsTaxRate5) {
        this.goodsTaxRate5 = goodsTaxRate5;
    }

    public String getGoodsTaxRecoverability5() {
        return goodsTaxRecoverability5;
    }

    public void setGoodsTaxRecoverability5(final String goodsTaxRecoverability5) {
        this.goodsTaxRecoverability5 = goodsTaxRecoverability5;
    }

    public String getGoodsTaxOption5() {
        return goodsTaxOption5;
    }

    public void setGoodsTaxOption5(final String goodsTaxOption5) {
        this.goodsTaxOption5 = goodsTaxOption5;
    }

    public String getGoodsTaxRate6() {
        return goodsTaxRate6;
    }

    public void setGoodsTaxRate6(final String goodsTaxRate6) {
        this.goodsTaxRate6 = goodsTaxRate6;
    }

    public String getGoodsTaxRecoverability6() {
        return goodsTaxRecoverability6;
    }

    public void setGoodsTaxRecoverability6(final String goodsTaxRecoverability6) {
        this.goodsTaxRecoverability6 = goodsTaxRecoverability6;
    }

    public String getGoodsTaxOption6() {
        return goodsTaxOption6;
    }

    public void setGoodsTaxOption6(final String goodsTaxOption6) {
        this.goodsTaxOption6 = goodsTaxOption6;
    }

    public String getGoodsPackagingString() {
        return goodsPackagingString;
    }

    public void setGoodsPackagingString(final String goodsPackagingString) {
        this.goodsPackagingString = goodsPackagingString;
    }

    public String getGoodsQuantity() {
        return goodsQuantity;
    }

    public void setGoodsQuantity(final String goodsQuantity) {
        this.goodsQuantity = goodsQuantity;
    }

    public String getGoodsUnitOfMeasure() {
        return goodsUnitOfMeasure;
    }

    public void setGoodsUnitOfMeasure(final String goodsUnitOfMeasure) {
        this.goodsUnitOfMeasure = goodsUnitOfMeasure;
    }

    public String getGoodsUnitCost() {
        return goodsUnitCost;
    }

    public void setGoodsUnitCost(final String goodsUnitCost) {
        this.goodsUnitCost = goodsUnitCost;
    }

    public String getGoodsRequestedAsNoCharge() {
        return goodsRequestedAsNoCharge;
    }

    public void setGoodsRequestedAsNoCharge(final String goodsRequestedAsNoCharge) {
        this.goodsRequestedAsNoCharge = goodsRequestedAsNoCharge;
    }

    public String getGoodsExtendedAmount() {
        return goodsExtendedAmount;
    }

    public void setGoodsExtendedAmount(final String goodsExtendedAmount) {
        this.goodsExtendedAmount = goodsExtendedAmount;
    }

    public String getGoodsLotSerialInformation() {
        return goodsLotSerialInformation;
    }

    public void setGoodsLotSerialInformation(final String goodsLotSerialInformation) {
        this.goodsLotSerialInformation = goodsLotSerialInformation;
    }

    public String getGoodsLotNumber() {
        return goodsLotNumber;
    }

    public void setGoodsLotNumber(final String goodsLotNumber) {
        this.goodsLotNumber = goodsLotNumber;
    }

    public String getGoodsSerialNumber() {
        return goodsSerialNumber;
    }

    public void setGoodsSerialNumber(final String goodsSerialNumber) {
        this.goodsSerialNumber = goodsSerialNumber;
    }

    public String getGoodsDueDate() {
        return goodsDueDate;
    }

    public void setGoodsDueDate(final String goodsDueDate) {
        this.goodsDueDate = goodsDueDate;
    }

    public String getGoodsDeliveryType() {
        return goodsDeliveryType;
    }

    public void setGoodsDeliveryType(final String goodsDeliveryType) {
        this.goodsDeliveryType = goodsDeliveryType;
    }

    public String getGoodsPrepaid() {
        return goodsPrepaid;
    }

    public void setGoodsPrepaid(final String goodsPrepaid) {
        this.goodsPrepaid = goodsPrepaid;
    }

    public String getGoodsDownPayment() {
        return goodsDownPayment;
    }

    public void setGoodsDownPayment(final String goodsDownPayment) {
        this.goodsDownPayment = goodsDownPayment;
    }

    public String getGoodsRetention() {
        return goodsRetention;
    }

    public void setGoodsRetention(final String goodsRetention) {
        this.goodsRetention = goodsRetention;
    }

    public String getGoodsRequestedDeliveryDate() {
        return goodsRequestedDeliveryDate;
    }

    public void setGoodsRequestedDeliveryDate(final String goodsRequestedDeliveryDate) {
        this.goodsRequestedDeliveryDate = goodsRequestedDeliveryDate;
    }

    public String getGoodsBudgetDate() {
        return goodsBudgetDate;
    }

    public void setGoodsBudgetDate(final String goodsBudgetDate) {
        this.goodsBudgetDate = goodsBudgetDate;
    }

    public String getGoodsMemo() {
        return goodsMemo;
    }

    public void setGoodsMemo(final String goodsMemo) {
        this.goodsMemo = goodsMemo;
    }

    public String getGoodsShipToAddress() {
        return goodsShipToAddress;
    }

    public void setGoodsShipToAddress(final String goodsShipToAddress) {
        this.goodsShipToAddress = goodsShipToAddress;
    }

    public String getGoodsShipToGlobalLocationNumber() {
        return goodsShipToGlobalLocationNumber;
    }

    public void setGoodsShipToGlobalLocationNumber(final String goodsShipToGlobalLocationNumber) {
        this.goodsShipToGlobalLocationNumber = goodsShipToGlobalLocationNumber;
    }

    public String getGoodsShipToLocationIdentifier() {
        return goodsShipToLocationIdentifier;
    }

    public void setGoodsShipToLocationIdentifier(final String goodsShipToLocationIdentifier) {
        this.goodsShipToLocationIdentifier = goodsShipToLocationIdentifier;
    }

    public String getGoodsShipToContact() {
        return goodsShipToContact;
    }

    public void setGoodsShipToContact(final String goodsShipToContact) {
        this.goodsShipToContact = goodsShipToContact;
    }

    public String getGoodsRequester() {
        return goodsRequester;
    }

    public void setGoodsRequester(final String goodsRequester) {
        this.goodsRequester = goodsRequester;
    }

    public String getGoodsDeliverToLocation() {
        return goodsDeliverToLocation;
    }

    public void setGoodsDeliverToLocation(final String goodsDeliverToLocation) {
        this.goodsDeliverToLocation = goodsDeliverToLocation;
    }

    public String getGoodsDeliverToLocationGln() {
        return goodsDeliverToLocationGln;
    }

    public void setGoodsDeliverToLocationGln(final String goodsDeliverToLocationGln) {
        this.goodsDeliverToLocationGln = goodsDeliverToLocationGln;
    }

    public String getGoodsDeliverToLocationIdentifier() {
        return goodsDeliverToLocationIdentifier;
    }

    public void setGoodsDeliverToLocationIdentifier(final String goodsDeliverToLocationIdentifier) {
        this.goodsDeliverToLocationIdentifier = goodsDeliverToLocationIdentifier;
    }

    public String getGoodsSupplierContract() {
        return goodsSupplierContract;
    }

    public void setGoodsSupplierContract(final String goodsSupplierContract) {
        this.goodsSupplierContract = goodsSupplierContract;
    }

    public String getGoodsExternalSupplierInvoiceSource() {
        return goodsExternalSupplierInvoiceSource;
    }

    public void setGoodsExternalSupplierInvoiceSource(final String goodsExternalSupplierInvoiceSource) {
        this.goodsExternalSupplierInvoiceSource = goodsExternalSupplierInvoiceSource;
    }

    public String getGoodsRequisitionLine() {
        return goodsRequisitionLine;
    }

    public void setGoodsRequisitionLine(final String goodsRequisitionLine) {
        this.goodsRequisitionLine = goodsRequisitionLine;
    }

    public String getGoodsStorageLocation() {
        return goodsStorageLocation;
    }

    public void setGoodsStorageLocation(final String goodsStorageLocation) {
        this.goodsStorageLocation = goodsStorageLocation;
    }

    public String getGoodsCostCenter() {
        return goodsCostCenter;
    }

    public void setGoodsCostCenter(final String goodsCostCenter) {
        this.goodsCostCenter = goodsCostCenter;
    }

    public String getGoodsCostCenterExternalSupplierInvoiceSource() {
        return goodsCostCenterExternalSupplierInvoiceSource;
    }

    public void setGoodsCostCenterExternalSupplierInvoiceSource(final String goodsCostCenterExternalSupplierInvoiceSource) {
        this.goodsCostCenterExternalSupplierInvoiceSource = goodsCostCenterExternalSupplierInvoiceSource;
    }

    public String getGoodsProject() {
        return goodsProject;
    }

    public void setGoodsProject(final String goodsProject) {
        this.goodsProject = goodsProject;
    }

    public String getGoodsProjectExternalSupplierInvoiceSource() {
        return goodsProjectExternalSupplierInvoiceSource;
    }

    public void setGoodsProjectExternalSupplierInvoiceSource(final String goodsProjectExternalSupplierInvoiceSource) {
        this.goodsProjectExternalSupplierInvoiceSource = goodsProjectExternalSupplierInvoiceSource;
    }

    public String getGoodsGrant() {
        return goodsGrant;
    }

    public void setGoodsGrant(final String goodsGrant) {
        this.goodsGrant = goodsGrant;
    }

    public String getGoodsGrantExternalSupplierInvoiceSource() {
        return goodsGrantExternalSupplierInvoiceSource;
    }

    public void setGoodsGrantExternalSupplierInvoiceSource(final String goodsGrantExternalSupplierInvoiceSource) {
        this.goodsGrantExternalSupplierInvoiceSource = goodsGrantExternalSupplierInvoiceSource;
    }

    public String getGoodsGift() {
        return goodsGift;
    }

    public void setGoodsGift(final String goodsGift) {
        this.goodsGift = goodsGift;
    }

    public String getGoodsGiftExternalSupplierInvoiceSource() {
        return goodsGiftExternalSupplierInvoiceSource;
    }

    public void setGoodsGiftExternalSupplierInvoiceSource(final String goodsGiftExternalSupplierInvoiceSource) {
        this.goodsGiftExternalSupplierInvoiceSource = goodsGiftExternalSupplierInvoiceSource;
    }

    public String getGoodsFund() {
        return goodsFund;
    }

    public void setGoodsFund(final String goodsFund) {
        this.goodsFund = goodsFund;
    }

    public String getGoodsFundExternalSupplierInvoiceSource() {
        return goodsFundExternalSupplierInvoiceSource;
    }

    public void setGoodsFundExternalSupplierInvoiceSource(final String goodsFundExternalSupplierInvoiceSource) {
        this.goodsFundExternalSupplierInvoiceSource = goodsFundExternalSupplierInvoiceSource;
    }

    public String getGoodsAlternateItemIdentifierRowId() {
        return goodsAlternateItemIdentifierRowId;
    }

    public void setGoodsAlternateItemIdentifierRowId(final String goodsAlternateItemIdentifierRowId) {
        this.goodsAlternateItemIdentifierRowId = goodsAlternateItemIdentifierRowId;
    }

    public String getGoodsAlternateItemIdentifierType() {
        return goodsAlternateItemIdentifierType;
    }

    public void setGoodsAlternateItemIdentifierType(final String goodsAlternateItemIdentifierType) {
        this.goodsAlternateItemIdentifierType = goodsAlternateItemIdentifierType;
    }

    public String getGoodsAlternateItemIdentifierValue() {
        return goodsAlternateItemIdentifierValue;
    }

    public void setGoodsAlternateItemIdentifierValue(final String goodsAlternateItemIdentifierValue) {
        this.goodsAlternateItemIdentifierValue = goodsAlternateItemIdentifierValue;
    }

    public String getGoodsAlternateItemIdentifierUnitOfMeasure() {
        return goodsAlternateItemIdentifierUnitOfMeasure;
    }

    public void setGoodsAlternateItemIdentifierUnitOfMeasure(final String goodsAlternateItemIdentifierUnitOfMeasure) {
        this.goodsAlternateItemIdentifierUnitOfMeasure = goodsAlternateItemIdentifierUnitOfMeasure;
    }

    public String getGoodsAlternateItemIdentifierManufacturer() {
        return goodsAlternateItemIdentifierManufacturer;
    }

    public void setGoodsAlternateItemIdentifierManufacturer(final String goodsAlternateItemIdentifierManufacturer) {
        this.goodsAlternateItemIdentifierManufacturer = goodsAlternateItemIdentifierManufacturer;
    }

    public String getGoodsItemTag() {
        return goodsItemTag;
    }

    public void setGoodsItemTag(final String goodsItemTag) {
        this.goodsItemTag = goodsItemTag;
    }

    public String getGoodsCloseStatus() {
        return goodsCloseStatus;
    }

    public void setGoodsCloseStatus(final String goodsCloseStatus) {
        this.goodsCloseStatus = goodsCloseStatus;
    }

    public String getGoodsClosedRowId() {
        return goodsClosedRowId;
    }

    public void setGoodsClosedRowId(final String goodsClosedRowId) {
        this.goodsClosedRowId = goodsClosedRowId;
    }

    public String getGoodsClosedOn() {
        return goodsClosedOn;
    }

    public void setGoodsClosedOn(final String goodsClosedOn) {
        this.goodsClosedOn = goodsClosedOn;
    }

    public String getGoodsClosedBy() {
        return goodsClosedBy;
    }

    public void setGoodsClosedBy(final String goodsClosedBy) {
        this.goodsClosedBy = goodsClosedBy;
    }

    public String getGoodsClosedReasonCode() {
        return goodsClosedReasonCode;
    }

    public void setGoodsClosedReasonCode(final String goodsClosedReasonCode) {
        this.goodsClosedReasonCode = goodsClosedReasonCode;
    }

    public List<CemiPurchaseOrderLineSplitBo> getLineSplits() {
        if (lineSplits == null) {
            lineSplits = new ArrayList<>();
        }
        return lineSplits;
    }

    public void setLineSplits(final List<CemiPurchaseOrderLineSplitBo> lineSplits) {
        this.lineSplits = lineSplits;
    }

}
