package edu.cornell.kfs.pmw.batch.report;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportSummaryItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportVendorItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksEmailableReportData;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;

public class PaymentWorksNewVendorPayeeAchBatchReportData extends PaymentWorksEmailableReportData {
    
    private PaymentWorksBatchReportSummaryItem disapprovedVendorsSummary;
    private PaymentWorksBatchReportSummaryItem noAchDataProvidedVendorsSummary;
    private PaymentWorksBatchReportSummaryItem recordsWithForeignAchSummary;
    private PaymentWorksBatchReportSummaryItem recordsWithPaymentMethodWireDomesticSummary;
    private PaymentWorksBatchReportSummaryItem recordsWithPaymentMethodWireForeignSummary;
    
    private List<PaymentWorksBatchReportVendorItem> disapprovedVendors;
    private List<PaymentWorksBatchReportVendorItem> noAchDataProvidedVendors;
    private List<PaymentWorksBatchReportVendorItem> pmwVendorAchsThatCouldNotBeProcessed;
    private List<PaymentWorksBatchReportVendorItem> recordsGeneratingException;
    private List<PaymentWorksBatchReportVendorItem> foreignAchItems;
    private List<PaymentWorksBatchReportVendorItem> paymentMethodWireDomsticItems;
    private List<PaymentWorksBatchReportVendorItem> paymentMethodWireForeignItems;
    
    public PaymentWorksNewVendorPayeeAchBatchReportData() {
        super();
        this.disapprovedVendorsSummary = new PaymentWorksBatchReportSummaryItem();
        this.noAchDataProvidedVendorsSummary = new PaymentWorksBatchReportSummaryItem();
        this.recordsWithForeignAchSummary = new PaymentWorksBatchReportSummaryItem();
        this.recordsWithPaymentMethodWireDomesticSummary  = new PaymentWorksBatchReportSummaryItem();
        this.recordsWithPaymentMethodWireForeignSummary  = new PaymentWorksBatchReportSummaryItem();
        
        this.noAchDataProvidedVendors = new ArrayList<PaymentWorksBatchReportVendorItem>();
        this.pmwVendorAchsThatCouldNotBeProcessed = new ArrayList<PaymentWorksBatchReportVendorItem>();
        this.disapprovedVendors = new ArrayList<PaymentWorksBatchReportVendorItem>();
        this.recordsGeneratingException = new ArrayList<PaymentWorksBatchReportVendorItem>();
        this.foreignAchItems = new ArrayList<PaymentWorksBatchReportVendorItem>();
        this.paymentMethodWireDomsticItems = new ArrayList<PaymentWorksBatchReportVendorItem>();
        this.paymentMethodWireForeignItems = new ArrayList<PaymentWorksBatchReportVendorItem>();
    }

    public PaymentWorksBatchReportSummaryItem getDisapprovedVendorsSummary() {
        return disapprovedVendorsSummary;
    }

    public void setDisapprovedVendorsSummary(PaymentWorksBatchReportSummaryItem disapprovedVendorsSummary) {
        this.disapprovedVendorsSummary = disapprovedVendorsSummary;
    }

    public PaymentWorksBatchReportSummaryItem getNoAchDataProvidedVendorsSummary() {
        return noAchDataProvidedVendorsSummary;
    }

    public void setNoAchDataProvidedVendorsSummary(PaymentWorksBatchReportSummaryItem noAchDataProvidedVendorsSummary) {
        this.noAchDataProvidedVendorsSummary = noAchDataProvidedVendorsSummary;
    }

    public List<PaymentWorksBatchReportVendorItem> getPmwVendorAchsThatCouldNotBeProcessed() {
        return pmwVendorAchsThatCouldNotBeProcessed;
    }

    public void setPmwVendorAchsThatCouldNotBeProcessed(List<PaymentWorksBatchReportVendorItem> pmwVendorAchsThatCouldNotBeProcessed) {
        this.pmwVendorAchsThatCouldNotBeProcessed = pmwVendorAchsThatCouldNotBeProcessed;
    }
    
    public void addPmwVendorAchThatCouldNotBeProcessed(PaymentWorksBatchReportVendorItem pmwVendorAchThatCouldNotBeProcessed) {
        if (this.pmwVendorAchsThatCouldNotBeProcessed == null) {
            this.pmwVendorAchsThatCouldNotBeProcessed = new ArrayList<PaymentWorksBatchReportVendorItem>();
        }
        this.pmwVendorAchsThatCouldNotBeProcessed.add(pmwVendorAchThatCouldNotBeProcessed);
    }

    public List<PaymentWorksBatchReportVendorItem> getNoAchDataProvidedVendors() {
        return noAchDataProvidedVendors;
    }

    public void setNoAchDataProvidedVendors(List<PaymentWorksBatchReportVendorItem> noAchDataProvidedVendors) {
        this.noAchDataProvidedVendors = noAchDataProvidedVendors;
    }
    
    public void addNoAchDataProvidedVendor(PaymentWorksBatchReportVendorItem noAchDataProvidedVendor) {
        if (this.noAchDataProvidedVendors == null) {
            this.noAchDataProvidedVendors = new ArrayList<PaymentWorksBatchReportVendorItem>();
        }
        this.noAchDataProvidedVendors.add(noAchDataProvidedVendor);
    }

    public List<PaymentWorksBatchReportVendorItem> getDisapprovedVendors() {
        return disapprovedVendors;
    }

    public void setDisapprovedVendors(List<PaymentWorksBatchReportVendorItem> disapprovedVendors) {
        this.disapprovedVendors = disapprovedVendors;
    }
    
    public void addDisapprovedVendor(PaymentWorksBatchReportVendorItem disapprovedVendor) {
        if (this.disapprovedVendors == null) {
            this.disapprovedVendors = new ArrayList<PaymentWorksBatchReportVendorItem>();
        }
        this.disapprovedVendors.add(disapprovedVendor);
    }

    public List<PaymentWorksBatchReportVendorItem> getRecordsGeneratingException() {
        return recordsGeneratingException;
    }

    public void setRecordsGeneratingException(List<PaymentWorksBatchReportVendorItem> recordsGeneratingException) {
        this.recordsGeneratingException = recordsGeneratingException;
    }
    
    public void addRecordGeneratingException(PaymentWorksBatchReportVendorItem recordGeneratingException) {
        if (this.recordsGeneratingException == null) {
            this.recordsGeneratingException = new ArrayList<PaymentWorksBatchReportVendorItem>();
        }
        this.recordsGeneratingException.add(recordGeneratingException);
    }
    
    @Override
    public String retrieveReportName() {
        return PaymentWorksConstants.PaymentWorksBatchReportNames.NEW_VENDOR_REQUESTS_PAYEE_ACH_REPORT_NAME;
    }
    
    @Override
    public void populateOutstandingSummaryItemsForReport() {
        super.populateSummaryItemsForReport(getPmwVendorAchsThatCouldNotBeProcessed().size(), getRecordsGeneratingException().size());
        getRecordsThatCouldNotBeProcessedSummary().setRecordCount(getPmwVendorAchsThatCouldNotBeProcessed().size());
        getDisapprovedVendorsSummary().setRecordCount(getDisapprovedVendors().size());
        getNoAchDataProvidedVendorsSummary().setRecordCount(getNoAchDataProvidedVendors().size());
    }
    
    public List<PaymentWorksBatchReportVendorItem> retrievePaymentWorksVendorsWithPayeeAchsProcessed() {
        return super.getRecordsProcessed();
    }
    
    public List<PaymentWorksBatchReportVendorItem> retrievePaymentWorksVendorsWithPayeeAchProcessingErrors() {
        return super.getRecordsWithProcessingErrors();
    }
    
    public List<PaymentWorksBatchReportVendorItem> retrievePaymentWorksVendorsWithUnprocessablePayeeAchs() {
        return this.getPmwVendorAchsThatCouldNotBeProcessed();
    }
    
    public PaymentWorksBatchReportSummaryItem getRecordsWithForeignAchSummary() {
        return recordsWithForeignAchSummary;
    }

    public void setRecordsWithForeignAchSummary(PaymentWorksBatchReportSummaryItem recordsWithForeignAchSummary) {
        this.recordsWithForeignAchSummary = recordsWithForeignAchSummary;
    }

    public List<PaymentWorksBatchReportVendorItem> getForeignAchItems() {
        return foreignAchItems;
    }

    public void setForeignAchItems(List<PaymentWorksBatchReportVendorItem> foreignAchItems) {
        this.foreignAchItems = foreignAchItems;
    }
    
    public void addForeignAchItem(PaymentWorksBatchReportVendorItem foreignAchItem) {
        if (this.foreignAchItems == null) {
            this.foreignAchItems = new ArrayList<PaymentWorksBatchReportVendorItem>();
        }
        this.foreignAchItems.add(foreignAchItem);
    }

    public PaymentWorksBatchReportSummaryItem getRecordsWithPaymentMethodWireDomesticSummary() {
        return recordsWithPaymentMethodWireDomesticSummary;
    }

    public void setRecordsWithPaymentMethodWireDomesticSummary(PaymentWorksBatchReportSummaryItem recordsWithPaymentMethodWireDomesticSummary) {
        this.recordsWithPaymentMethodWireDomesticSummary = recordsWithPaymentMethodWireDomesticSummary;
    }

    public PaymentWorksBatchReportSummaryItem getRecordsWithPaymentMethodWireForeignSummary() {
        return recordsWithPaymentMethodWireForeignSummary;
    }

    public void setRecordsWithPaymentMethodWireForeignSummary(PaymentWorksBatchReportSummaryItem recordsWithPaymentMethodWireForeignSummary) {
        this.recordsWithPaymentMethodWireForeignSummary = recordsWithPaymentMethodWireForeignSummary;
    }
    
    public List<PaymentWorksBatchReportVendorItem> getPaymentMethodWireDomsticItems() {
        return paymentMethodWireDomsticItems;
    }

    public void setPaymentMethodWireDomsticItems(List<PaymentWorksBatchReportVendorItem> paymentMethodWireDomsticItems) {
        this.paymentMethodWireDomsticItems = paymentMethodWireDomsticItems;
    }
    
    public void addPaymentWireDomesticItem(PaymentWorksBatchReportVendorItem paymentMethodWireDomsticItem) {
        if (this.paymentMethodWireDomsticItems == null) {
            this.paymentMethodWireDomsticItems = new ArrayList<PaymentWorksBatchReportVendorItem>();
        }
        this.paymentMethodWireDomsticItems.add(paymentMethodWireDomsticItem);
    }

    public List<PaymentWorksBatchReportVendorItem> getPaymentMethodWireForeignItems() {
        return paymentMethodWireForeignItems;
    }

    public void setPaymentMethodWireForeignItems(List<PaymentWorksBatchReportVendorItem> paymentMethodWireForeignItems) {
        this.paymentMethodWireForeignItems = paymentMethodWireForeignItems;
    }
    
    public void addPaymentWireForeignItem(PaymentWorksBatchReportVendorItem paymentMethodWireForeignItem) {
        if (this.paymentMethodWireForeignItems == null) {
            this.paymentMethodWireForeignItems = new ArrayList<PaymentWorksBatchReportVendorItem>();
        }
        this.paymentMethodWireForeignItems.add(paymentMethodWireForeignItem);
    }
    
}
