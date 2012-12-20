package edu.cornell.kfs.fp.service;

import javax.jws.WebService;

import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.service.PersonService;
import org.kuali.rice.kns.UserSession;
import org.kuali.rice.kns.document.authorization.DocumentAuthorizer;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.service.DocumentHelperService;
import org.kuali.rice.kns.service.DocumentService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.MessageMap;


/**
 *
 * <p>Title: SubmitTripWebServiceImpl</p>
 * <p>Description: Implements the webservice for travel's trip submission</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Cornell University: Kuali Financial Systems</p>
 * @author Dennis Friends
 * @author Sandy Eccleston
 * @version 1.0
 */
@WebService(endpointInterface = "edu.cornell.kfs.fp.service.SubmitTripWebService")
public class SubmitTripWebServiceImpl implements SubmitTripWebService {

	/**
	 * 
	 */
	public String submitTrip(String dvDescription, String dvExplanation, String tripNumber, String travelerNetId, String initiatorNetId, double totalAmount, String checkStubText) throws Exception {
        UserSession actualUserSession = GlobalVariables.getUserSession();
        MessageMap globalErrorMap = GlobalVariables.getMessageMap();

        try {
        	if(!isValidDVInitiator(initiatorNetId)) {
        		throw new RuntimeException("Initiator identified does not have permission to create a DV.");
        	}
        } catch (Exception ex) {
    		throw new RuntimeException("Initiator identified does not have permission to create a DV.", ex);
        }
        
        try {
	        // create and route doc as system user
	        GlobalVariables.setUserSession(new UserSession(initiatorNetId));
	
	        MessageMap documentErrorMap = new MessageMap();
	        GlobalVariables.setMessageMap(documentErrorMap);
	
	        // Create document with description provided
			DisbursementVoucherDocument dvDoc = null;
			
	        try {
	            dvDoc = (DisbursementVoucherDocument) SpringContext.getBean(DocumentService.class).getNewDocument(DisbursementVoucherDocument.class);
	        }
	        catch (WorkflowException e) {
	            throw new RuntimeException("Error creating new disbursement voucher document: " + e.getMessage(), e);
	        }
		  
	        if(dvDoc != null) {
				dvDoc.getDocumentHeader().setDocumentDescription(dvDescription);
				dvDoc.getDocumentHeader().setExplanation(dvExplanation);
				dvDoc.getDocumentHeader().setOrganizationDocumentNumber(tripNumber);
				
				dvDoc.initiateDocument();

				// Set vendor to traveler using netID provided
				Person traveler = SpringContext.getBean(PersonService.class).getPersonByPrincipalName(travelerNetId);
				dvDoc.templateEmployee(traveler);
				dvDoc.setPayeeAssigned(true);
				
		        dvDoc.getDvPayeeDetail().setDisbVchrPaymentReasonCode("J");
				
				dvDoc.setDisbVchrCheckTotalAmount(new KualiDecimal(totalAmount));
				dvDoc.setDisbVchrPaymentMethodCode("P");

				dvDoc.setDisbVchrCheckStubText(checkStubText);
				
				// Persist document
				SpringContext.getBean(DocumentService.class).saveDocument(dvDoc);
				
				return dvDoc.getDocumentNumber();
	        } else {
	        	return "";
	        }
		} finally {
            GlobalVariables.setUserSession(actualUserSession);
            GlobalVariables.setMessageMap(globalErrorMap);
		}
	}
  

	/**
	 * 
	 */
	public boolean isValidDVInitiator(String initiatorNetId) throws Exception {
		Person initiator = SpringContext.getBean(PersonService.class).getPersonByPrincipalName(initiatorNetId);
        String documentTypeName = SpringContext.getBean(DataDictionaryService.class).getDocumentTypeNameByClass(DisbursementVoucherDocument.class);
        DocumentAuthorizer documentAuthorizer = SpringContext.getBean(DocumentHelperService.class).getDocumentAuthorizer(documentTypeName);

		return documentAuthorizer.canInitiate(documentTypeName, initiator);
	}
}
