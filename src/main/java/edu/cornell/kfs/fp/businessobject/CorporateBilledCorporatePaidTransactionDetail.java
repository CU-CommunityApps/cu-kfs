package edu.cornell.kfs.fp.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.fp.businessobject.ProcurementCardTransactionDetail;
import org.kuali.kfs.fp.businessobject.ProcurementCardVendor;
import org.kuali.kfs.krad.util.ObjectUtils;

public class CorporateBilledCorporatePaidTransactionDetail extends ProcurementCardTransactionDetail {
    private static final long serialVersionUID = -2979017882058066280L;
    
    public CorporateBilledCorporatePaidTransactionDetail() {
        setSourceAccountingLines(new ArrayList<CorporateBilledCorporatePaidSourceAccountingLine>());
        setTargetAccountingLines(new ArrayList<CorporateBilledCorporatePaidTargetAccountingLine>());
    }
    
    public CorporateBilledCorporatePaidTransactionDetail(ProcurementCardTransactionDetail pCardTransactionDetail, String documentNumber) {
        this();
        setDocumentNumber(documentNumber);
        setFinancialDocumentTransactionLineNumber(pCardTransactionDetail.getFinancialDocumentTransactionLineNumber());
        setTransactionDate(pCardTransactionDetail.getTransactionDate());
        setTransactionReferenceNumber(pCardTransactionDetail.getTransactionReferenceNumber());
        setTransactionPostingDate(pCardTransactionDetail.getTransactionPostingDate());
        setTransactionOriginalCurrencyAmount(pCardTransactionDetail.getTransactionOriginalCurrencyAmount());
        setTransactionBillingCurrencyCode(pCardTransactionDetail.getTransactionBillingCurrencyCode());
        setTransactionCurrencyExchangeRate(pCardTransactionDetail.getTransactionCurrencyExchangeRate());
        setTransactionSettlementAmount(pCardTransactionDetail.getTransactionSettlementAmount());
        setTransactionSalesTaxAmount(pCardTransactionDetail.getTransactionSalesTaxAmount());
        setTransactionTaxExemptIndicator(pCardTransactionDetail.getTransactionTaxExemptIndicator());
        setTransactionPurchaseIdentifierIndicator(pCardTransactionDetail.getTransactionPurchaseIdentifierIndicator());
        setTransactionPurchaseIdentifierDescription(pCardTransactionDetail.getTransactionPurchaseIdentifierDescription());
        setTransactionUnitContactName(pCardTransactionDetail.getTransactionUnitContactName());
        setTransactionTravelAuthorizationCode(pCardTransactionDetail.getTransactionTravelAuthorizationCode());
        setTransactionPointOfSaleCode(pCardTransactionDetail.getTransactionPointOfSaleCode());
        setTransactionCycleStartDate(pCardTransactionDetail.getTransactionCycleStartDate());
        setTransactionCycleEndDate(pCardTransactionDetail.getTransactionCycleEndDate());
        setTransactionTotalAmount(pCardTransactionDetail.getTransactionTotalAmount());
        
        ProcurementCardVendor newVendor = (ProcurementCardVendor) ObjectUtils.deepCopy(pCardTransactionDetail.getProcurementCardVendor());
        newVendor.setDocumentNumber(documentNumber);
        setProcurementCardVendor(newVendor);
        
        setExtension(builldExtension(pCardTransactionDetail, documentNumber));
    }

    protected CorporateBilledCorporatePaidTransactionDetailExtendedAttribute builldExtension(ProcurementCardTransactionDetail pCardTransactionDetail, String documentNumber) {
        CorporateBilledCorporatePaidTransactionDetailExtendedAttribute extendedAttribute = new CorporateBilledCorporatePaidTransactionDetailExtendedAttribute();
        extendedAttribute.setDocumentNumber(documentNumber);
        ProcurementCardTransactionDetailExtendedAttribute pCardExtension = (ProcurementCardTransactionDetailExtendedAttribute) pCardTransactionDetail.getExtension();
        extendedAttribute.setFinancialDocumentTransactionLineNumber(pCardExtension.getFinancialDocumentTransactionLineNumber());
        List<PurchasingDataDetail> purchasingDataDetails = new ArrayList<PurchasingDataDetail>();
        
        for (PurchasingDataDetail originalDetail : pCardExtension.getPurchasingDataDetails()) {
            PurchasingDataDetail newDetail = (PurchasingDataDetail) ObjectUtils.deepCopy(originalDetail);
            newDetail.setDocumentNumber(documentNumber);
            purchasingDataDetails.add(newDetail);
        }
        
        extendedAttribute.setPurchasingDataDetails(purchasingDataDetails);
        return extendedAttribute;
    }

}
