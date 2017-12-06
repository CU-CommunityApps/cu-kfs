package edu.cornell.kfs.fp.businessobject;

import java.util.ArrayList;

import org.kuali.kfs.fp.businessobject.ProcurementCardTransactionDetail;

import edu.cornell.kfs.fp.document.CorporateBilledCorporatePaidDocument;

public class CorporateBilledCorporatePaidTransactionDetail extends ProcurementCardTransactionDetail {
    private static final long serialVersionUID = -2979017882058066280L;
    
    private transient CorporateBilledCorporatePaidDocument corporateBilledCorporatePaidDocument;
    
    public CorporateBilledCorporatePaidTransactionDetail() {
        super();
        setSourceAccountingLines(new ArrayList<CorporateBilledCorporatePaidSourceAccountingLine>());
        setTargetAccountingLines(new ArrayList<CorporateBilledCorporatePaidTargetAccountingLine>());
    }

    public CorporateBilledCorporatePaidDocument getCorporateBilledCorporatePaidDocument() {
        return corporateBilledCorporatePaidDocument;
    }

    public void setCorporateBilledCorporatePaidDocument(
            CorporateBilledCorporatePaidDocument corporateBilledCorporatePaidDocument) {
        this.corporateBilledCorporatePaidDocument = corporateBilledCorporatePaidDocument;
    }

}
