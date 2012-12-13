package edu.cornell.kfs.fp.service;

import javax.jws.WebService;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ProjectCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.ProjectCodeService;
import org.kuali.kfs.coa.service.SubAccountService;
import org.kuali.kfs.coa.service.SubObjectCodeService;
import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.fp.document.CashManagementDocument;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.authorization.DisbursementVoucherDocumentAuthorizer;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.service.PersonService;
import org.kuali.rice.kns.UserSession;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.service.DocumentService;
import org.kuali.rice.kns.util.ErrorMap;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.MessageMap;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;
import org.kuali.rice.kns.workflow.service.WorkflowDocumentService;

import edu.cornell.kfs.coa.service.AccountVerificationWebService;


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
	        // create and route doc as system user
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
		Person initiator = SpringContext.getBean(PersonService.class).getPerson(initiatorNetId);
        String documentTypeName = SpringContext.getBean(DataDictionaryService.class).getDocumentTypeNameByClass(DisbursementVoucherDocument.class);
		return SpringContext.getBean(DisbursementVoucherDocumentAuthorizer.class).canInitiate(documentTypeName, initiator);
	}
}
