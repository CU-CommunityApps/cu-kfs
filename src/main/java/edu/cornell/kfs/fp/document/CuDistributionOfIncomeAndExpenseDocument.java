package edu.cornell.kfs.fp.document;

import edu.cornell.kfs.fp.CuFPConstants;
import org.kuali.kfs.fp.document.DistributionOfIncomeAndExpenseDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants.COMPONENT;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants.NAMESPACE;

@NAMESPACE(namespace = KFSConstants.CoreModuleNamespaces.FINANCIAL)
@COMPONENT(component = "DistributionOfIncomeAndExpense")
public class CuDistributionOfIncomeAndExpenseDocument extends DistributionOfIncomeAndExpenseDocument {
	
    private static final long serialVersionUID = 1L;

    //TRIP INFORMATION FIELDS
    protected String tripAssociationStatusCode;
    protected String tripId;
    
    public CuDistributionOfIncomeAndExpenseDocument() {
        super();
        tripAssociationStatusCode = CuFPConstants.IS_NOT_TRIP_DOC;
    }

    public String getTripId() {
        return this.tripId;
    }


    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}
