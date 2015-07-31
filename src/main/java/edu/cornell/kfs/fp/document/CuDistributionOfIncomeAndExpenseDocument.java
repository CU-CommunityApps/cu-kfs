package edu.cornell.kfs.fp.document;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.document.DistributionOfIncomeAndExpenseDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants.COMPONENT;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants.NAMESPACE;
import org.kuali.rice.kew.api.action.ActionTaken;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kew.framework.postprocessor.DocumentRouteStatusChange;

import edu.cornell.kfs.fp.document.interfaces.CULegacyTravelIntegrationInterface;
import edu.cornell.kfs.fp.document.service.CULegacyTravelService;


@NAMESPACE(namespace = KFSConstants.CoreModuleNamespaces.FINANCIAL)
@COMPONENT(component = "DistributionOfIncomeAndExpense")
public class CuDistributionOfIncomeAndExpenseDocument extends DistributionOfIncomeAndExpenseDocument implements CULegacyTravelIntegrationInterface{

    private static final long serialVersionUID = 1L;

    //TRIP INFORMATION FIELDS
    protected String tripAssociationStatusCode;
    protected String tripId;

    /**
     *  Results that may be returned regarding whether the trip was reopened during the call to the Legacy Travel service.
     */
    protected enum REOPEN_LEGACY_TRIP_RESULT {
      NOT_A_TRAVEL_DI,
      TRAVEL_DI_NOT_REOPENED,
      TRAVEL_DI_REOPENED
    }

    /**
     *  Attempts to reopen a trip
     *  @return -1 = Not a Travel Doc,
     *           0 = Failed to reopen,
     *           1 = Reopened
     *  @see https://jira.cornell.edu/browse/KFSPTS-2715
     */
    public REOPEN_LEGACY_TRIP_RESULT reopenLegacyTrip() {
      REOPEN_LEGACY_TRIP_RESULT tripReOpened = REOPEN_LEGACY_TRIP_RESULT.TRAVEL_DI_NOT_REOPENED;
      boolean isTravelDoc = false;
      List<ActionTaken> actionsTaken = this.getDocumentHeader().getWorkflowDocument().getActionsTaken();
      String reason = "";

      if(actionsTaken.size() > 0) {
        String annotation = actionsTaken.get(actionsTaken.size() - 1).getAnnotation();
        if(StringUtils.isNotEmpty(annotation)) {
          reason = annotation.substring("Disapproval reason - ".length());
        }
      }

      try {
        CULegacyTravelService cuLegacyTravelService = SpringContext.getBean(CULegacyTravelService.class);
        isTravelDoc = cuLegacyTravelService.isCULegacyTravelIntegrationInterfaceAssociatedWithTrip(this);
        if(isTravelDoc) {
          // This means the DV is a Travel DV
          tripReOpened = cuLegacyTravelService.reopenLegacyTrip(this.getDocumentNumber(), reason) ?
              REOPEN_LEGACY_TRIP_RESULT.TRAVEL_DI_REOPENED :
                REOPEN_LEGACY_TRIP_RESULT.TRAVEL_DI_NOT_REOPENED;
          LOG.info("Trip successfully reopened : "+ (tripReOpened == REOPEN_LEGACY_TRIP_RESULT.TRAVEL_DI_REOPENED ? true : false));
        } else {
          LOG.info("DI is not a travel DI");
          tripReOpened = REOPEN_LEGACY_TRIP_RESULT.NOT_A_TRAVEL_DI;
        }
      } catch (Exception ex) {
        LOG.info("Exception occurred while trying to cancel a trip.");
        ex.printStackTrace();
        tripReOpened = REOPEN_LEGACY_TRIP_RESULT.TRAVEL_DI_NOT_REOPENED;
      }

      return tripReOpened;
    }

    /**
     * Overridden to interact with the Legacy Travel service
     * @see https://jira.cornell.edu/browse/KFSPTS-2715
     */
    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
      // If the DI is Canceled or Disapproved, we need to reopen the trip in the Legacy Travel service.
      if (getDocumentHeader().getWorkflowDocument().isCanceled() ||
          getDocumentHeader().getWorkflowDocument().isDisapproved()) {
        this.reopenLegacyTrip(); // Reopen the trip in CU Legacy travel service
      }

      super.doRouteStatusChange(statusChangeEvent);
    }

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
