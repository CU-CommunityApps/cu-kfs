package edu.cornell.kfs.cemi.module.purap.batch.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class CemiPurchaseOrderServiceLineBo extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private String serviceRowId;
    private String serviceCatalogItem;
    private String serviceOrderLineId;
    private String serviceLineNumber;
    private String serviceLineCompany;
    private String serviceDescription;
    private String serviceSupplierContractLine;
    private String serviceAlternateSupplierContract;
    private String serviceCommodityCode;
    private String serviceCommodityCodeType;
    private String servicePaymentStatus;
    private String serviceInvoiceStatus;
    private String serviceReceivingStatus;
    private String serviceResourceCategory;
    private String serviceTaxApplicability;
    private String serviceTaxCode;
    private String serviceTaxRate1;
    private String serviceTaxRecoverability1;
    private String serviceTaxOption1;
    private String serviceTaxRate2;
    private String serviceTaxRecoverability2;
    private String serviceTaxOption2;
    private String serviceTaxRate3;
    private String serviceTaxRecoverability3;
    private String serviceTaxOption3;
    private String serviceTaxRate4;
    private String serviceTaxRecoverability4;
    private String serviceTaxOption4;
    private String serviceTaxRate5;
    private String serviceTaxRecoverability5;
    private String serviceTaxOption5;
    private String serviceTaxRate6;
    private String serviceTaxRecoverability6;
    private String serviceTaxOption6;
    private String serviceExtendedAmount;
    private String serviceDueDate;
    private String serviceStartDate;
    private String serviceEndDate;
    private String servicePrepaid;
    private String serviceDownPayment;
    private String serviceRetention;
    private String serviceBudgetDate;
    private String serviceMemo;
    private String serviceShipToAddress;
    private String serviceShipToContact;
    private String serviceRequester;
    private String serviceDeliverToLocation;
    private String serviceRequisitionLine;
    private String serviceSupplierContract;
    private String serviceSupplierContractExternalSupplierInvoiceSource;
    private String serviceStorageLocation;
    private String serviceCostCenter;
    private String serviceCostCenterExternalSupplierInvoiceSource;
    private String serviceProject;
    private String serviceProjectExternalSupplierInvoiceSource;
    private String serviceGrant;
    private String serviceGrantExternalSupplierInvoiceSource;
    private String serviceGift;
    private String serviceGiftExternalSupplierInvoiceSource;
    private String serviceFund;
    private String serviceFundExternalSupplierInvoiceSource;
    private String serviceCloseStatus;
    private String serviceClosedRowId;
    private String serviceClosedOn;
    private String serviceClosedBy;
    private String serviceClosedReasonCode;

    private List<CemiPurchaseOrderLineSplitBo> lineSplits;

    public String getServiceRowId() {
        return serviceRowId;
    }

    public void setServiceRowId(final String serviceRowId) {
        this.serviceRowId = serviceRowId;
    }

    public String getServiceCatalogItem() {
        return serviceCatalogItem;
    }

    public void setServiceCatalogItem(final String serviceCatalogItem) {
        this.serviceCatalogItem = serviceCatalogItem;
    }

    public String getServiceOrderLineId() {
        return serviceOrderLineId;
    }

    public void setServiceOrderLineId(final String serviceOrderLineId) {
        this.serviceOrderLineId = serviceOrderLineId;
    }

    public String getServiceLineNumber() {
        return serviceLineNumber;
    }

    public void setServiceLineNumber(final String serviceLineNumber) {
        this.serviceLineNumber = serviceLineNumber;
    }

    public String getServiceLineCompany() {
        return serviceLineCompany;
    }

    public void setServiceLineCompany(final String serviceLineCompany) {
        this.serviceLineCompany = serviceLineCompany;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(final String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public String getServiceSupplierContractLine() {
        return serviceSupplierContractLine;
    }

    public void setServiceSupplierContractLine(final String serviceSupplierContractLine) {
        this.serviceSupplierContractLine = serviceSupplierContractLine;
    }

    public String getServiceAlternateSupplierContract() {
        return serviceAlternateSupplierContract;
    }

    public void setServiceAlternateSupplierContract(final String serviceAlternateSupplierContract) {
        this.serviceAlternateSupplierContract = serviceAlternateSupplierContract;
    }

    public String getServiceCommodityCode() {
        return serviceCommodityCode;
    }

    public void setServiceCommodityCode(final String serviceCommodityCode) {
        this.serviceCommodityCode = serviceCommodityCode;
    }

    public String getServiceCommodityCodeType() {
        return serviceCommodityCodeType;
    }

    public void setServiceCommodityCodeType(final String serviceCommodityCodeType) {
        this.serviceCommodityCodeType = serviceCommodityCodeType;
    }

    public String getServicePaymentStatus() {
        return servicePaymentStatus;
    }

    public void setServicePaymentStatus(final String servicePaymentStatus) {
        this.servicePaymentStatus = servicePaymentStatus;
    }

    public String getServiceInvoiceStatus() {
        return serviceInvoiceStatus;
    }

    public void setServiceInvoiceStatus(final String serviceInvoiceStatus) {
        this.serviceInvoiceStatus = serviceInvoiceStatus;
    }

    public String getServiceReceivingStatus() {
        return serviceReceivingStatus;
    }

    public void setServiceReceivingStatus(final String serviceReceivingStatus) {
        this.serviceReceivingStatus = serviceReceivingStatus;
    }

    public String getServiceResourceCategory() {
        return serviceResourceCategory;
    }

    public void setServiceResourceCategory(final String serviceResourceCategory) {
        this.serviceResourceCategory = serviceResourceCategory;
    }

    public String getServiceTaxApplicability() {
        return serviceTaxApplicability;
    }

    public void setServiceTaxApplicability(final String serviceTaxApplicability) {
        this.serviceTaxApplicability = serviceTaxApplicability;
    }

    public String getServiceTaxCode() {
        return serviceTaxCode;
    }

    public void setServiceTaxCode(final String serviceTaxCode) {
        this.serviceTaxCode = serviceTaxCode;
    }

    public String getServiceTaxRate1() {
        return serviceTaxRate1;
    }

    public void setServiceTaxRate1(final String serviceTaxRate1) {
        this.serviceTaxRate1 = serviceTaxRate1;
    }

    public String getServiceTaxRecoverability1() {
        return serviceTaxRecoverability1;
    }

    public void setServiceTaxRecoverability1(final String serviceTaxRecoverability1) {
        this.serviceTaxRecoverability1 = serviceTaxRecoverability1;
    }

    public String getServiceTaxOption1() {
        return serviceTaxOption1;
    }

    public void setServiceTaxOption1(final String serviceTaxOption1) {
        this.serviceTaxOption1 = serviceTaxOption1;
    }

    public String getServiceTaxRate2() {
        return serviceTaxRate2;
    }

    public void setServiceTaxRate2(final String serviceTaxRate2) {
        this.serviceTaxRate2 = serviceTaxRate2;
    }

    public String getServiceTaxRecoverability2() {
        return serviceTaxRecoverability2;
    }

    public void setServiceTaxRecoverability2(final String serviceTaxRecoverability2) {
        this.serviceTaxRecoverability2 = serviceTaxRecoverability2;
    }

    public String getServiceTaxOption2() {
        return serviceTaxOption2;
    }

    public void setServiceTaxOption2(final String serviceTaxOption2) {
        this.serviceTaxOption2 = serviceTaxOption2;
    }

    public String getServiceTaxRate3() {
        return serviceTaxRate3;
    }

    public void setServiceTaxRate3(final String serviceTaxRate3) {
        this.serviceTaxRate3 = serviceTaxRate3;
    }

    public String getServiceTaxRecoverability3() {
        return serviceTaxRecoverability3;
    }

    public void setServiceTaxRecoverability3(final String serviceTaxRecoverability3) {
        this.serviceTaxRecoverability3 = serviceTaxRecoverability3;
    }

    public String getServiceTaxOption3() {
        return serviceTaxOption3;
    }

    public void setServiceTaxOption3(final String serviceTaxOption3) {
        this.serviceTaxOption3 = serviceTaxOption3;
    }

    public String getServiceTaxRate4() {
        return serviceTaxRate4;
    }

    public void setServiceTaxRate4(final String serviceTaxRate4) {
        this.serviceTaxRate4 = serviceTaxRate4;
    }

    public String getServiceTaxRecoverability4() {
        return serviceTaxRecoverability4;
    }

    public void setServiceTaxRecoverability4(final String serviceTaxRecoverability4) {
        this.serviceTaxRecoverability4 = serviceTaxRecoverability4;
    }

    public String getServiceTaxOption4() {
        return serviceTaxOption4;
    }

    public void setServiceTaxOption4(final String serviceTaxOption4) {
        this.serviceTaxOption4 = serviceTaxOption4;
    }

    public String getServiceTaxRate5() {
        return serviceTaxRate5;
    }

    public void setServiceTaxRate5(final String serviceTaxRate5) {
        this.serviceTaxRate5 = serviceTaxRate5;
    }

    public String getServiceTaxRecoverability5() {
        return serviceTaxRecoverability5;
    }

    public void setServiceTaxRecoverability5(final String serviceTaxRecoverability5) {
        this.serviceTaxRecoverability5 = serviceTaxRecoverability5;
    }

    public String getServiceTaxOption5() {
        return serviceTaxOption5;
    }

    public void setServiceTaxOption5(final String serviceTaxOption5) {
        this.serviceTaxOption5 = serviceTaxOption5;
    }

    public String getServiceTaxRate6() {
        return serviceTaxRate6;
    }

    public void setServiceTaxRate6(final String serviceTaxRate6) {
        this.serviceTaxRate6 = serviceTaxRate6;
    }

    public String getServiceTaxRecoverability6() {
        return serviceTaxRecoverability6;
    }

    public void setServiceTaxRecoverability6(final String serviceTaxRecoverability6) {
        this.serviceTaxRecoverability6 = serviceTaxRecoverability6;
    }

    public String getServiceTaxOption6() {
        return serviceTaxOption6;
    }

    public void setServiceTaxOption6(final String serviceTaxOption6) {
        this.serviceTaxOption6 = serviceTaxOption6;
    }

    public String getServiceExtendedAmount() {
        return serviceExtendedAmount;
    }

    public void setServiceExtendedAmount(final String serviceExtendedAmount) {
        this.serviceExtendedAmount = serviceExtendedAmount;
    }

    public String getServiceDueDate() {
        return serviceDueDate;
    }

    public void setServiceDueDate(final String serviceDueDate) {
        this.serviceDueDate = serviceDueDate;
    }

    public String getServiceStartDate() {
        return serviceStartDate;
    }

    public void setServiceStartDate(final String serviceStartDate) {
        this.serviceStartDate = serviceStartDate;
    }

    public String getServiceEndDate() {
        return serviceEndDate;
    }

    public void setServiceEndDate(final String serviceEndDate) {
        this.serviceEndDate = serviceEndDate;
    }

    public String getServicePrepaid() {
        return servicePrepaid;
    }

    public void setServicePrepaid(final String servicePrepaid) {
        this.servicePrepaid = servicePrepaid;
    }

    public String getServiceDownPayment() {
        return serviceDownPayment;
    }

    public void setServiceDownPayment(final String serviceDownPayment) {
        this.serviceDownPayment = serviceDownPayment;
    }

    public String getServiceRetention() {
        return serviceRetention;
    }

    public void setServiceRetention(final String serviceRetention) {
        this.serviceRetention = serviceRetention;
    }

    public String getServiceBudgetDate() {
        return serviceBudgetDate;
    }

    public void setServiceBudgetDate(final String serviceBudgetDate) {
        this.serviceBudgetDate = serviceBudgetDate;
    }

    public String getServiceMemo() {
        return serviceMemo;
    }

    public void setServiceMemo(final String serviceMemo) {
        this.serviceMemo = serviceMemo;
    }

    public String getServiceShipToAddress() {
        return serviceShipToAddress;
    }

    public void setServiceShipToAddress(final String serviceShipToAddress) {
        this.serviceShipToAddress = serviceShipToAddress;
    }

    public String getServiceShipToContact() {
        return serviceShipToContact;
    }

    public void setServiceShipToContact(final String serviceShipToContact) {
        this.serviceShipToContact = serviceShipToContact;
    }

    public String getServiceRequester() {
        return serviceRequester;
    }

    public void setServiceRequester(final String serviceRequester) {
        this.serviceRequester = serviceRequester;
    }

    public String getServiceDeliverToLocation() {
        return serviceDeliverToLocation;
    }

    public void setServiceDeliverToLocation(final String serviceDeliverToLocation) {
        this.serviceDeliverToLocation = serviceDeliverToLocation;
    }

    public String getServiceRequisitionLine() {
        return serviceRequisitionLine;
    }

    public void setServiceRequisitionLine(final String serviceRequisitionLine) {
        this.serviceRequisitionLine = serviceRequisitionLine;
    }

    public String getServiceSupplierContract() {
        return serviceSupplierContract;
    }

    public void setServiceSupplierContract(final String serviceSupplierContract) {
        this.serviceSupplierContract = serviceSupplierContract;
    }

    public String getServiceSupplierContractExternalSupplierInvoiceSource() {
        return serviceSupplierContractExternalSupplierInvoiceSource;
    }

    public void setServiceSupplierContractExternalSupplierInvoiceSource(
            String serviceSupplierContractExternalSupplierInvoiceSource) {
        this.serviceSupplierContractExternalSupplierInvoiceSource = serviceSupplierContractExternalSupplierInvoiceSource;
    }

    public String getServiceStorageLocation() {
        return serviceStorageLocation;
    }

    public void setServiceStorageLocation(final String serviceStorageLocation) {
        this.serviceStorageLocation = serviceStorageLocation;
    }

    public String getServiceCostCenter() {
        return serviceCostCenter;
    }

    public void setServiceCostCenter(final String serviceCostCenter) {
        this.serviceCostCenter = serviceCostCenter;
    }

    public String getServiceCostCenterExternalSupplierInvoiceSource() {
        return serviceCostCenterExternalSupplierInvoiceSource;
    }

    public void setServiceCostCenterExternalSupplierInvoiceSource(final String serviceCostCenterExternalSupplierInvoiceSource) {
        this.serviceCostCenterExternalSupplierInvoiceSource = serviceCostCenterExternalSupplierInvoiceSource;
    }

    public String getServiceProject() {
        return serviceProject;
    }

    public void setServiceProject(final String serviceProject) {
        this.serviceProject = serviceProject;
    }

    public String getServiceProjectExternalSupplierInvoiceSource() {
        return serviceProjectExternalSupplierInvoiceSource;
    }

    public void setServiceProjectExternalSupplierInvoiceSource(final String serviceProjectExternalSupplierInvoiceSource) {
        this.serviceProjectExternalSupplierInvoiceSource = serviceProjectExternalSupplierInvoiceSource;
    }

    public String getServiceGrant() {
        return serviceGrant;
    }

    public void setServiceGrant(final String serviceGrant) {
        this.serviceGrant = serviceGrant;
    }

    public String getServiceGrantExternalSupplierInvoiceSource() {
        return serviceGrantExternalSupplierInvoiceSource;
    }

    public void setServiceGrantExternalSupplierInvoiceSource(final String serviceGrantExternalSupplierInvoiceSource) {
        this.serviceGrantExternalSupplierInvoiceSource = serviceGrantExternalSupplierInvoiceSource;
    }

    public String getServiceGift() {
        return serviceGift;
    }

    public void setServiceGift(final String serviceGift) {
        this.serviceGift = serviceGift;
    }

    public String getServiceGiftExternalSupplierInvoiceSource() {
        return serviceGiftExternalSupplierInvoiceSource;
    }

    public void setServiceGiftExternalSupplierInvoiceSource(final String serviceGiftExternalSupplierInvoiceSource) {
        this.serviceGiftExternalSupplierInvoiceSource = serviceGiftExternalSupplierInvoiceSource;
    }

    public String getServiceFund() {
        return serviceFund;
    }

    public void setServiceFund(final String serviceFund) {
        this.serviceFund = serviceFund;
    }

    public String getServiceFundExternalSupplierInvoiceSource() {
        return serviceFundExternalSupplierInvoiceSource;
    }

    public void setServiceFundExternalSupplierInvoiceSource(final String serviceFundExternalSupplierInvoiceSource) {
        this.serviceFundExternalSupplierInvoiceSource = serviceFundExternalSupplierInvoiceSource;
    }

    public String getServiceCloseStatus() {
        return serviceCloseStatus;
    }

    public void setServiceCloseStatus(final String serviceCloseStatus) {
        this.serviceCloseStatus = serviceCloseStatus;
    }

    public String getServiceClosedRowId() {
        return serviceClosedRowId;
    }

    public void setServiceClosedRowId(final String serviceClosedRowId) {
        this.serviceClosedRowId = serviceClosedRowId;
    }

    public String getServiceClosedOn() {
        return serviceClosedOn;
    }

    public void setServiceClosedOn(final String serviceClosedOn) {
        this.serviceClosedOn = serviceClosedOn;
    }

    public String getServiceClosedBy() {
        return serviceClosedBy;
    }

    public void setServiceClosedBy(final String serviceClosedBy) {
        this.serviceClosedBy = serviceClosedBy;
    }

    public String getServiceClosedReasonCode() {
        return serviceClosedReasonCode;
    }

    public void setServiceClosedReasonCode(final String serviceClosedReasonCode) {
        this.serviceClosedReasonCode = serviceClosedReasonCode;
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
