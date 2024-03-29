package edu.cornell.kfs.fp.document;

import edu.cornell.kfs.fp.CuFPConstants;
import org.apache.commons.lang3.StringUtils;
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

    @Override
    public void toCopy() {
        super.toCopy();
        setTripAssociationStatusCode(CuFPConstants.IS_NOT_TRIP_DOC);
        setTripId(null);
    }

    public boolean isLegacyTrip() {
        return StringUtils.equals(getTripAssociationStatusCode(), CuFPConstants.IS_TRIP_DOC);
    }

    public String getTripAssociationStatusCode() {
        return this.tripAssociationStatusCode;
    }


    public void setTripAssociationStatusCode(final String tripAssociationStatusCode) {
        this.tripAssociationStatusCode = tripAssociationStatusCode;
    }

    public String getTripId() {
        return this.tripId;
    }


    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}
