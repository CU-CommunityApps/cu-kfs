package edu.cornell.kfs.fp.document;

import org.kuali.kfs.fp.document.DistributionOfIncomeAndExpenseDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants.COMPONENT;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants.NAMESPACE;
import org.kuali.rice.kew.api.exception.WorkflowException;

import edu.cornell.kfs.fp.document.interfaces.CULegacyTravelIntegrationInterface;


@NAMESPACE(namespace = KFSConstants.CoreModuleNamespaces.FINANCIAL)
@COMPONENT(component = "DistributionOfIncomeAndExpense")
public class CuDistributionOfIncomeAndExpenseDocument extends DistributionOfIncomeAndExpenseDocument implements CULegacyTravelIntegrationInterface{

    //TRIP INFORMATION FILEDS
    protected String tripAssociationStatusCode;
    protected String tripId;

    @Override
    public void toCopy() throws WorkflowException {
        super.toCopy();
        setTripAssociationStatusCode(null);
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
