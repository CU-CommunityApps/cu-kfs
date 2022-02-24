package edu.cornell.kfs.fp.document;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.document.DistributionOfIncomeAndExpenseDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants.COMPONENT;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants.NAMESPACE;
import org.kuali.kfs.kew.actiontaken.ActionTaken;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteStatusChange;

import edu.cornell.kfs.fp.document.interfaces.CULegacyTravelIntegrationInterface;
import edu.cornell.kfs.fp.document.service.CULegacyTravelService;
import edu.cornell.kfs.fp.document.service.impl.CULegacyTravelServiceImpl;


@NAMESPACE(namespace = KFSConstants.CoreModuleNamespaces.FINANCIAL)
@COMPONENT(component = "DistributionOfIncomeAndExpense")
public class CuDistributionOfIncomeAndExpenseDocument extends DistributionOfIncomeAndExpenseDocument implements CULegacyTravelIntegrationInterface{
	private static final Logger LOG = LogManager.getLogger(CuDistributionOfIncomeAndExpenseDocument.class);
	
    private static final long serialVersionUID = 1L;

    //TRIP INFORMATION FIELDS
    protected String tripAssociationStatusCode;
    protected String tripId;
    
    public CuDistributionOfIncomeAndExpenseDocument() {
        super();
        tripAssociationStatusCode = CULegacyTravelServiceImpl.TRIP_ASSOCIATIONS.IS_NOT_TRIP_DOC;
    }

    /**
     * Overridden to interact with the Legacy Travel service
     * @see https://jira.cornell.edu/browse/KFSPTS-2715
     */
    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
      // If the DI is Cancelled or Disapproved, we need to reopen the trip in the Legacy Travel service.
      if (getDocumentHeader().getWorkflowDocument().isCanceled() ||
          getDocumentHeader().getWorkflowDocument().isDisapproved()) {        
        boolean tripReOpened = false;
        boolean isTravelDoc = false;
        List<ActionTaken> actionsTaken = this.getDocumentHeader().getWorkflowDocument().getActionsTaken();
        String disapprovalReason = "";

        if(actionsTaken.size() > 0) {
          String annotation = actionsTaken.get(actionsTaken.size() - 1).getAnnotation();
          if(StringUtils.isNotEmpty(annotation)) {
            disapprovalReason = annotation.substring("Disapproval reason - ".length());
          }
        }

        try {
          CULegacyTravelService cuLegacyTravelService = SpringContext.getBean(CULegacyTravelService.class);
          isTravelDoc = cuLegacyTravelService.isCULegacyTravelIntegrationInterfaceAssociatedWithTrip(this);
          if(isTravelDoc) {
            // This means the DI is a Travel DI
            tripReOpened = cuLegacyTravelService.reopenLegacyTrip(this.getDocumentNumber(), disapprovalReason);
            LOG.info("Trip successfully reopened : " + tripReOpened);
          } else {
            LOG.info("DI is not a travel DI");
          }
        } catch (Exception ex) {
          LOG.error("Exception occurred while trying to cancel a trip.", ex);
        }
        
      }

      super.doRouteStatusChange(statusChangeEvent);
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
