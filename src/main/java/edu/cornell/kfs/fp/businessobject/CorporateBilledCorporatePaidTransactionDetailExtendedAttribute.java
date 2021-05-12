package edu.cornell.kfs.fp.businessobject;

import java.util.ArrayList;
import java.util.List;

public class CorporateBilledCorporatePaidTransactionDetailExtendedAttribute extends ProcurementCardTransactionDetailExtendedAttribute {
    private static final long serialVersionUID = 8666291254266800374L;
    
    private List<CorporateBilledCorporatePaidDataDetail> corporateBilledCorporatePaidDataDetails;
    
    public CorporateBilledCorporatePaidTransactionDetailExtendedAttribute() {
        super();
        corporateBilledCorporatePaidDataDetails = new ArrayList<CorporateBilledCorporatePaidDataDetail>();
    }

    public List<CorporateBilledCorporatePaidDataDetail> getCorporateBilledCorporatePaidDataDetails() {
        return corporateBilledCorporatePaidDataDetails;
    }

    public void setCorporateBilledCorporatePaidDataDetails(
            List<CorporateBilledCorporatePaidDataDetail> corporateBilledCorporatePaidDataDetails) {
        this.corporateBilledCorporatePaidDataDetails = corporateBilledCorporatePaidDataDetails;
    }
}
