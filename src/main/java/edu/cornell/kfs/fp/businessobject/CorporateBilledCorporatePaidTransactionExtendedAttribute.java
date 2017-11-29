package edu.cornell.kfs.fp.businessobject;

import java.util.ArrayList;
import java.util.List;

public class CorporateBilledCorporatePaidTransactionExtendedAttribute extends ProcurementCardTransactionExtendedAttribute {
    private static final long serialVersionUID = -7363307192713879081L;
    
    List<CorporateBilledCorporatePaidDataRecord> corporateBilledCorporatePaidDataRecords;
    
    public CorporateBilledCorporatePaidTransactionExtendedAttribute() {
        corporateBilledCorporatePaidDataRecords = new ArrayList<CorporateBilledCorporatePaidDataRecord>();
    }

    public List<CorporateBilledCorporatePaidDataRecord> getCorporateBilledCorporatePaidDataRecords() {
        return corporateBilledCorporatePaidDataRecords;
    }

    public void setCorporateBilledCorporatePaidDataRecords(
            List<CorporateBilledCorporatePaidDataRecord> corporateBilledCorporatePaidDataRecords) {
        this.corporateBilledCorporatePaidDataRecords = corporateBilledCorporatePaidDataRecords;
    }
    
    @Override
    protected PurchasingDataRecord buildPurchasingDataRecord() {
        return new CorporateBilledCorporatePaidDataRecord();
    }

}
