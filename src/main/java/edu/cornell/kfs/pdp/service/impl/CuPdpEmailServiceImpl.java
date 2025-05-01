package edu.cornell.kfs.pdp.service.impl;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.web.format.CurrencyFormatter;
import org.kuali.kfs.core.web.format.DateFormatter;
import org.kuali.kfs.core.web.format.Formatter;
import org.kuali.kfs.core.web.format.IntegerFormatter;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.businessobject.ACHBank;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentNoteText;
import org.kuali.kfs.pdp.service.impl.PdpEmailServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.mail.BodyMailMessage;

import com.rsmart.kuali.kfs.pdp.service.AchBundlerHelperService;

import edu.cornell.kfs.pdp.CUPdpKeyConstants;
import edu.cornell.kfs.pdp.service.CuPdpEmailService;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class CuPdpEmailServiceImpl extends PdpEmailServiceImpl implements CuPdpEmailService{
    private static final Logger LOG = LogManager.getLogger();
    
    private AchBundlerHelperService achBundlerHelperService;
    private final Environment environment;

    public CuPdpEmailServiceImpl(final Environment environment) {
        Validate.isTrue(environment != null, "environment must be supplied");
        this.environment = environment;
    }

	/**
	 * Sends advice notification email to the payee receiving an ACH payment
	 * 
	 * KFSPTS-1460: 
	 * Deprecated this method signature due to need for refactoring to deal with both the unbundled and bundled cases. 
	 * The major change is that the paymentDetail input parameter is no longer a singleton and is a list of payment details instead.
	 * The caller will no longer loop through the payment detail records calling sendAchAdviceEmail but instead will pass the 
	 * entire list of payment detail records and sendAchAdviceEmail will loop through them taking into account cases for
	 * multiples and singletons when creating and sending the advice emails.
	 * 
	 * @param paymentGroup ACH payment group to send notification for
	 * @param paymentDetail Payment Detail containing payment amounts
	 * @param customer Pdp Customer profile for payment
	 */
	@Deprecated
	public void sendAchAdviceEmail(final PaymentGroup paymentGroup, final PaymentDetail paymentDetail, final CustomerProfile customer) {
		LOG.info("DEPRECATED method sendAchAdviceEmail() with payment details as a singleton was called.  NO ACH advices were sent.");
		//throwing run time exception so caller does not update the database that the ACH advice was sent.
		throw new RuntimeException("DEPRECATED method sendAchAdviceEmail() with payment details as a singleton was called.  NO ACH advices were sent.");
    }

	/**
	 * Send advice notification email to the payee receiving an ACH payment for both bundled and unbundled ACH payments.
	 * 
	 * KFSPTS-1460: 
	 * New method signature due to need for refactoring to deal with both the unbundled and bundled cases. 
	 * The major change is that the paymentDetail input parameter is now a list of payment details instead of being a singleton.
	 * The caller will pass the entire list of payment detail records and sendAchAdviceEmail will loop through them taking into 
	 * account cases for multiples and singletons when creating and sending the advice emails.
	 *  
	 * @param paymentGroup Payment group corresponding to the payment detail records
	 * @param paymentDetails List of all payment details to process for the single advice email being sent
	 * @param customer Pdp customer profile for payment
	 */
	public void sendAchAdviceEmail(final PaymentGroup paymentGroup, final List<PaymentDetail> paymentDetails, final CustomerProfile customer) {		
        LOG.debug("sendAchAdviceEmail() with payment details list starting");	     
        Integer numPayments = 0;
        final boolean shouldBundleAchPayments = getAchBundlerHelperService().shouldBundleAchPayments();
        if (shouldBundleAchPayments) {
        	//Send out one email to the payee listing all the payment details for the specified payment group
        	
            final BodyMailMessage bundledMessage = createAdviceMessageAndPopulateHeader(paymentGroup, customer);
        	
        	//create the formatted body
   			// this seems wasteful, but since the total net amount is needed in the message body before the payment details...it's needed
        	KualiDecimal totalNetAmount = new KualiDecimal(0);
        	final Iterator<PaymentDetail> pdToNetAmountIter = paymentDetails.iterator();
        	while (pdToNetAmountIter.hasNext()){
        		numPayments = numPayments + 1;
        		final PaymentDetail pd = pdToNetAmountIter.next();
        		totalNetAmount = totalNetAmount.add(pd.getNetPaymentAmount());
        	}     
            final int maxNumDetailsForBody = 10; //max # of payment detail records to include in email body as well as in the attachment, only used for non-DV advices
        	final StringBuffer bundledBody = createAdviceMessageBody(paymentGroup, customer, totalNetAmount, numPayments);
        	
        	//format payment details based on the whether it is a DV or a PREQ
        	boolean adviceIsForDV = false;    //formatting of payment details for DV is different than for PREQ
        	boolean firstPass = true;         //first time through loop      
        	StringBuffer bundledAtachmentData = new StringBuffer(); 
        	        	    
        	for (Iterator <PaymentDetail> payDetailsIter = paymentDetails.iterator(); payDetailsIter.hasNext();) {
        		final PaymentDetail paymentDetail = payDetailsIter.next();
        		
        		//initialize data the first time through the loop 
        		if (firstPass){
        			adviceIsForDV = (paymentDetail.getFinancialDocumentTypeCode().equalsIgnoreCase(DisbursementVoucherConstants.DOCUMENT_TYPE_CHECKACH))?true:false;        			
        			if (adviceIsForDV){
        				//we will NOT be sending an attachment, all payment detail will be in the body of the message
        				bundledBody.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_PAYMENT_HEADER_LINE_ONE));
        				bundledBody.append(customer.getAdviceHeaderText());
        				bundledBody.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_PAYMENT_SEPARATOR));
        			}
        			else {
        				//we will be sending an attachment
        				bundledAtachmentData = new StringBuffer();
        	            //creating the payment detail table header
        	        	bundledAtachmentData.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_ATTACHMENT_HEADING_SUMMARY_LINE_ONE));
        	        	bundledAtachmentData.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_ATTACHMENT_HEADING_SUMMARY_LINE_TWO));
        	        	bundledAtachmentData.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_ATTACHMENT_HEADING_SUMMARY_LINE_THREE));
        	        	
        	            //verbiage describing the payment details and attachment
        	        	bundledBody.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_DETAIL_INFO_MSG, numPayments));
        	        	if (numPayments <= maxNumDetailsForBody) {
        	        		//email body will have details and an attachment will be sent 
        	        		bundledBody.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_DETAIL_UNDER_LIMIT_MSG));
        	        		//individual customer message for the payment
            	        	bundledBody.append(customer.getAdviceHeaderText());
            	        	bundledBody.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_PAYMENT_SEPARATOR));
        	        	}//implied else that numPayments is over the max so only send an attachment
        			}               			
        			firstPass = false;  //ensure headers only included once
        		}//first-pass
        		
        		if (adviceIsForDV){
        			//format payment detail information and include it in the message body, do not send an attachment
        			bundledBody.append(createAdviceMessagePaymentDetail(paymentGroup, paymentDetail, adviceIsForDV, shouldBundleAchPayments));        			
        		}
        		else{ 
        			//put payment detail information in the attachment
        			bundledAtachmentData.append(createAdviceMessagePaymentDetail(paymentGroup, paymentDetail, adviceIsForDV, shouldBundleAchPayments));
        			
        			//also put payment detail in the body when the number of payments is fewer than the max
        			if (numPayments <= maxNumDetailsForBody) {
        				//explicitly using false instead of shouldBundleAchPayments so that we get the correct format for the email body
        				bundledBody.append(createAdviceMessagePaymentDetail(paymentGroup, paymentDetail, adviceIsForDV, false)); 
        			}
        		}        		
   
        	}//for-loop
        	
        	bundledMessage.setMessage(bundledBody.toString());
        	if (!adviceIsForDV) {
        		//only create the attachment file when the payments are NOT for DV's
        		Formatter integerFormatter = new IntegerFormatter();
            	String attachmentFileName = new String("paymentDetailsForDisbursement_" + (String)integerFormatter.formatForPresentation(paymentGroup.getDisbursementNbr()) + ".csv");
            	bundledMessage.setAttachmentFileName(attachmentFileName);
            	bundledMessage.setAttachmentContent(bundledAtachmentData.toString().getBytes());
            	bundledMessage.setAttachmentContentType(new String("text/csv"));
        	}        	
        	sendFormattedAchAdviceEmail(bundledMessage, customer, paymentGroup);        	
        }
        else {
        	//Maintain original spec of sending the payee an email for each payment detail in the payment group
        	//PdpMailMessage extends the MailMessage class.  Type casting is used in sendFormattedAchAdviceEmail
        	//so that the appropriate mail "send" is invoked based on the message type that we created and passed.
        	for (final PaymentDetail paymentDetail : paymentGroup.getPaymentDetails()) {
        		numPayments = paymentGroup.getPaymentDetails().size();
        		final BodyMailMessage nonBundledMessage = createAdviceMessageAndPopulateHeader(paymentGroup, customer);
        		final StringBuffer nonBundledBody =  createAdviceMessageBody(paymentGroup, customer, paymentDetail.getNetPaymentAmount(), numPayments);
        		nonBundledBody.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_PAYMENT_HEADER_LINE_ONE));
        		nonBundledBody.append(customer.getAdviceHeaderText());
        		nonBundledBody.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_PAYMENT_SEPARATOR));
        		final boolean adviceIsForDV = (paymentDetail.getFinancialDocumentTypeCode().equalsIgnoreCase(DisbursementVoucherConstants.DOCUMENT_TYPE_CHECKACH))?true:false;
        		nonBundledBody.append(createAdviceMessagePaymentDetail(paymentGroup, paymentDetail, adviceIsForDV, shouldBundleAchPayments));
        		nonBundledMessage.setMessage(nonBundledBody.toString());
         		sendFormattedAchAdviceEmail(nonBundledMessage, customer, paymentGroup);
        	}
        }
        
    }   
   
   /**
    * KFSPTS-1460: New method. created from code in sendAchAdviceEmail.
    * @return
    */
    private BodyMailMessage createAdviceMessageAndPopulateHeader(final PaymentGroup paymentGroup, final CustomerProfile customer) {
    	LOG.debug("createAdviceMessageAndPopulateHeader() starting");
    	final BodyMailMessage message = new BodyMailMessage();
        	
    	String fromAddress = customer.getAdviceReturnEmailAddr();	
		if ((fromAddress == null) || (fromAddress.isEmpty())) {
			//get the default from address
			fromAddress = parameterService.getParameterValueAsString(CUKFSParameterKeyConstants.KFS_PDP, CUKFSParameterKeyConstants.ALL_COMPONENTS, CUKFSParameterKeyConstants.PDP_CUSTOMER_MISSING_ADVICE_RETURN_EMAIL);
		}	
    	
        if (environment.isProductionEnvironment()) {
            message.addToAddress(paymentGroup.getAdviceEmailAddress());
            message.addCcAddress(paymentGroup.getAdviceEmailAddress());
            message.addBccAddress(paymentGroup.getAdviceEmailAddress());
            message.setFromAddress(fromAddress);
            message.setSubject(customer.getAdviceSubjectLine());
        }
        else {
            message.addToAddress(emailService.getDefaultToAddress());
            message.addCcAddress(emailService.getDefaultToAddress());
            message.addBccAddress(emailService.getDefaultToAddress());
            message.setFromAddress(fromAddress);
            message.setSubject(environment.getName() + ": " + customer.getAdviceSubjectLine() + ":" + paymentGroup.getAdviceEmailAddress());
        }        
        
        LOG.debug("sending email to " + paymentGroup.getAdviceEmailAddress() + " for disb # " + paymentGroup.getDisbursementNbr());        
    	return message;
    }
    
    
    /**
     * KFSPTS-1460: New method. Created from code in sendAchAdviceEmail and new code.
     * All content in the body of the email message is created in this method regardless 
     * of the number of payment details for the payment group.
     */
    private StringBuffer createAdviceMessageBody(final PaymentGroup paymentGroup, final CustomerProfile customer, final KualiDecimal netPaymentAmount, final Integer numPayments) {
    	LOG.debug("createAdviceMessageBody() starting");  	
    	
        // formatter for payment amounts
        final Formatter moneyFormatter = new CurrencyFormatter();
        final Formatter integerFormatter = new IntegerFormatter();
    	
        String payeeName = "";
        if (paymentGroup.getPayeeName() != null) {
        	payeeName = paymentGroup.getPayeeName();
        }
        
        String paymentDescription = "";
        if (customer.getAchPaymentDescription() != null) {
        	paymentDescription = customer.getAchPaymentDescription();
        }
        
        final StringBuffer body = new StringBuffer();
        body.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_PAYMENT_TO, payeeName));
        body.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_PAYMENT_FROM, paymentDescription));

        // get bank name to which the payment is being transferred
        String bankName = "";
        final ACHBank achBank = achBankService.getByPrimaryId(paymentGroup.getAchBankRoutingNbr());
        if (achBank == null) {
            LOG.error("Bank could not be found for routing number " + paymentGroup.getAchBankRoutingNbr());
        } else {
            bankName = achBank.getBankName();
        }
		String disbNbr = "";
        if (paymentGroup.getDisbursementNbr() != null) {
        	disbNbr = (String)integerFormatter.formatForPresentation(paymentGroup.getDisbursementNbr());
        }
        
        //verbiage stating bank, net amount, and disb num that was sent
        body.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_BANK_AMOUNT, bankName,
                moneyFormatter.formatForPresentation(netPaymentAmount), disbNbr)); 
        
        //verbiage stating when the deposit should be expected
        body.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_DEPOSIT_DAYS));
        		
        //verbiage stating the number of payments the net deposit was for
        body.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_DEPOSIT_NUM_PAYMENTS,
                integerFormatter.formatForPresentation(numPayments.toString())));
    	
    	return body;
    }
    
    
    /**
     * KFSPTS-1460: New method. Create a formatted payment detail line for the ACH advice.
     */
    private String createAdviceMessagePaymentDetail(final PaymentGroup paymentGroup, final PaymentDetail paymentDetail, final boolean adviceIsForDV, final boolean shouldBundleAchPayments) {
    	LOG.debug("createAdviceMessagePaymentDetail() starting");  
    
        final Formatter moneyFormatter = new CurrencyFormatter();
        final Formatter dateFormatter = new DateFormatter();
        final Formatter integerFormatter = new IntegerFormatter();
        
		String invoiceNbr =  "";
        if (StringUtils.isNotBlank(paymentDetail.getInvoiceNbr())) {
            invoiceNbr = paymentDetail.getInvoiceNbr();
        }
        
		String poNbr = "";
        if (StringUtils.isNotBlank(paymentDetail.getPurchaseOrderNbr())) {
        	poNbr = paymentDetail.getPurchaseOrderNbr();
        }
        
		String invoiceDate = "";
        if (paymentDetail.getInvoiceDate() != null) {
        	invoiceDate = (String)dateFormatter.formatForPresentation(paymentDetail.getInvoiceDate());
        }
        
		String sourceDocNbr = "";
        if (StringUtils.isNotBlank(paymentDetail.getCustPaymentDocNbr())) {
        	sourceDocNbr = paymentDetail.getCustPaymentDocNbr();
        }
        
		String payDate = "";
        if (paymentGroup.getPaymentDate() != null) {
        	payDate = (String)dateFormatter.formatForPresentation(paymentGroup.getPaymentDate());
        }        
        
		String disbNbr = "";
        if (paymentGroup.getDisbursementNbr() != null) {
        	disbNbr = (String)integerFormatter.formatForPresentation(paymentGroup.getDisbursementNbr());
        }
		
		String disbDate = "";
        if (paymentGroup.getDisbursementDate() != null) {
        	disbDate = (String)dateFormatter.formatForPresentation(paymentGroup.getDisbursementDate());
        } 
        
		String originalInvoiceAmount = "";
        if (paymentDetail.getOrigInvoiceAmount() != null) {
        	final String amount = (String)moneyFormatter.formatForPresentation(paymentDetail.getOrigInvoiceAmount());
        	originalInvoiceAmount = StringUtils.remove(amount, KFSConstants.COMMA);
        }
        
		String invoiceTotalDiscount = "";
        if (paymentDetail.getInvTotDiscountAmount() != null) {
        	final String amount = (String)moneyFormatter.formatForPresentation(paymentDetail.getInvTotDiscountAmount());
        	invoiceTotalDiscount = StringUtils.remove(amount, KFSConstants.COMMA);
        }
        
		String netPayAmount = "";
        if (paymentDetail.getNetPaymentAmount() != null) {
        	 final String amount = (String)moneyFormatter.formatForPresentation(paymentDetail.getNetPaymentAmount());
        	 netPayAmount = StringUtils.remove(amount, KFSConstants.COMMA);
        }          
        
        //there are three types of formats that need to be created: DV (same format for both bundled and non), PREQ-bundled, PREQ-non-bundled
        StringBuffer formattedPaymentDetail = new StringBuffer();
        
        if (adviceIsForDV){
        //DV payment detail gets put in message body, format for that
        	formattedPaymentDetail.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_SOURCE_DOCUMENT_NUMBER, sourceDocNbr));
            if (StringUtils.isNotBlank(invoiceNbr) && StringUtils.isNotBlank(invoiceDate)) {
                formattedPaymentDetail.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_INVOICE_NUMBER, invoiceNbr));
                formattedPaymentDetail.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_INVOICE_DATE, invoiceDate));
            }
        	formattedPaymentDetail.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_NET_PAYMENT_AMOUNT, netPayAmount));
        	formattedPaymentDetail.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_ORIGINAL_INVOICE_AMOUNT, originalInvoiceAmount));
        	
            // print payment notes
        	formattedPaymentDetail.append(KFSConstants.NEWLINE);
            for (final PaymentNoteText paymentNoteText : paymentDetail.getNotes()) {
            	formattedPaymentDetail.append(paymentNoteText.getCustomerNoteText() + KFSConstants.NEWLINE);
            }

            if (paymentDetail.getNotes().isEmpty()) {
            	formattedPaymentDetail.append(getMessage(PdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_NO_NOTES));
            }
        	
            formattedPaymentDetail.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_PAYMENT_SEPARATOR));        	
        }        
        else if (shouldBundleAchPayments){
        //PREQ payment detail gets put in attachment	
        	formattedPaymentDetail.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_ATTACHMENT_PAYMENT_TABLE_ITEM_LINE, invoiceNbr, poNbr, invoiceDate, sourceDocNbr, payDate, disbNbr, disbDate, originalInvoiceAmount, invoiceTotalDiscount, netPayAmount));
        }        
        else{
        //PREQ payment detail gets put in message body	(used for BOTH non-bundled adviced and the first N payment details of bundled advices
        	formattedPaymentDetail.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_INVOICE_NUMBER, invoiceNbr));
        	formattedPaymentDetail.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_PURCHASE_ORDER_NUMBER, poNbr));        	
        	formattedPaymentDetail.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_SOURCE_DOCUMENT_NUMBER, sourceDocNbr));
        	
        	formattedPaymentDetail.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_NET_PAYMENT_AMOUNT, netPayAmount));
        	formattedPaymentDetail.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_ORIGINAL_INVOICE_AMOUNT, originalInvoiceAmount));
        	formattedPaymentDetail.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_TOTAL_DISCOUNT_AMOUNT, invoiceTotalDiscount));
        	
            // print payment notes
        	formattedPaymentDetail.append(KFSConstants.NEWLINE);
            for (final PaymentNoteText paymentNoteText : paymentDetail.getNotes()) {
            	formattedPaymentDetail.append(paymentNoteText.getCustomerNoteText() + KFSConstants.NEWLINE);
            }

            if (paymentDetail.getNotes().isEmpty()) {
            	formattedPaymentDetail.append(getMessage(PdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_NO_NOTES));
            }
        	
            formattedPaymentDetail.append(getMessage(CUPdpKeyConstants.MESSAGE_PDP_ACH_ADVICE_EMAIL_BODY_PAYMENT_SEPARATOR));        	
        }
		        		
		return formattedPaymentDetail.toString();
    }
       
    
    /**
     * KFSPTS-1460: broke this logic out of sendAchAdviceEmail
     * 
     */
    private void sendFormattedAchAdviceEmail(final BodyMailMessage message, final CustomerProfile customer, final PaymentGroup paymentGroup) {
    	LOG.debug("sendFormattedAchAdviceEmail() starting");
    	emailService.sendMessage(message, false);
    }
    
    /**
     * KFSPTS-1460: New method
     */
    public AchBundlerHelperService getAchBundlerHelperService() {
        return achBundlerHelperService;
    }

    /**
     * KFSPTS-1460: New method
     */
    public void setAchBundlerHelperService(final AchBundlerHelperService achBundlerHelperService) {
        this.achBundlerHelperService = achBundlerHelperService;
    }  

}
