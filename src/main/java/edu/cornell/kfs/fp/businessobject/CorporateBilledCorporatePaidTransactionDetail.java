package edu.cornell.kfs.fp.businessobject;

import java.util.ArrayList;

import org.kuali.kfs.fp.businessobject.ProcurementCardTransactionDetail;

public class CorporateBilledCorporatePaidTransactionDetail extends ProcurementCardTransactionDetail {
    private static final long serialVersionUID = -2979017882058066280L;
    
    public CorporateBilledCorporatePaidTransactionDetail() {
        super();
        setSourceAccountingLines(new ArrayList<CorporateBilledCorporatePaidSourceAccountingLine>());
        setTargetAccountingLines(new ArrayList<CorporateBilledCorporatePaidTargetAccountingLine>());
    }

}
