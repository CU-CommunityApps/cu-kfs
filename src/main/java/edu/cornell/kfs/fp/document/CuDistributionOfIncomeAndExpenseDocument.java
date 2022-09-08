package edu.cornell.kfs.fp.document;

import org.kuali.kfs.fp.document.DistributionOfIncomeAndExpenseDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants.COMPONENT;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants.NAMESPACE;

import edu.cornell.kfs.fp.document.interfaces.CULegacyTravelIntegrationInterface;
import edu.cornell.kfs.fp.document.service.impl.CULegacyTravelServiceImpl;

@NAMESPACE(namespace = KFSConstants.CoreModuleNamespaces.FINANCIAL)
@COMPONENT(component = "DistributionOfIncomeAndExpense")
public class CuDistributionOfIncomeAndExpenseDocument extends DistributionOfIncomeAndExpenseDocument implements CULegacyTravelIntegrationInterface {
	
    private static final long serialVersionUID = 1L;

    //TRIP INFORMATION FIELDS
    protected String tripAssociationStatusCode;
    protected String tripId;
    
    public CuDistributionOfIncomeAndExpenseDocument() {
        super();
        tripAssociationStatusCode = CULegacyTravelServiceImpl.TRIP_ASSOCIATIONS.IS_NOT_TRIP_DOC;
    }

    @Override
    public void toCopy() {
        super.toCopy();
        setTripAssociationStatusCode(CULegacyTravelServiceImpl.TRIP_ASSOCIATIONS.IS_NOT_TRIP_DOC);
        setTripId(null);
    }

    public String getTripAssociationStatusCode() {
        return this.tripAssociationStatusCode;
    }


    public void setTripAssociationStatusCode(String tripAssociationStatusCode) {
        this.tripAssociationStatusCode = tripAssociationStatusCode;
    }


    public String getTripId() {
        return this.tripId;
    }


    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}
