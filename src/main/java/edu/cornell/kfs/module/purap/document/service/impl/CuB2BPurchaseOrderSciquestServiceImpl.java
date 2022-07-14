package edu.cornell.kfs.module.purap.document.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.service.AttachmentService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.module.purap.document.service.impl.B2BPurchaseOrderSciquestServiceImpl;
import org.kuali.kfs.module.purap.exception.B2BConnectionException;
import org.kuali.kfs.module.purap.exception.CxmlParseError;
import org.kuali.kfs.module.purap.util.PurApDateFormatUtils;
import org.kuali.kfs.module.purap.util.cxml.B2BParserHelper;
import org.kuali.kfs.module.purap.util.cxml.PurchaseOrderResponse;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.ContractManager;
import org.kuali.kfs.vnd.businessobject.PaymentTermType;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.api.WorkflowDocument;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.document.service.CuB2BPurchaseOrderService;

public class CuB2BPurchaseOrderSciquestServiceImpl extends B2BPurchaseOrderSciquestServiceImpl implements CuB2BPurchaseOrderService{
    private static final Logger LOG = LogManager.getLogger();
    
    private static final String NEWLINE = "\r\n";
    private static final String PREFIX = "--";
    
    // distribution methods
    private static final int FAX = 1;
    private static final int EMAIL = 2;
    private static final int MANUAL = 3;
    private static final int CONVERSION = 4;
    
    //KFSUPGRADE-406
    // Non-quantity Unit of Measure - this UOM should be used as the default UOM for all Non-Quantity orders to ensure they are properly handled by SciQuest (KFSPTS-792)
    private static final String NON_QUANTITY_UOM = "LOT"; 
    //end KFSUPGRADE-406
    
    // KFSUPGRADE-583
    // Text to pass to SciQuest in the case where a federally funded account is used on the PO
    private static final String FEDERAL_FUNDING_TEXT = "Federal funding has been designated for this purchase order.";
    // end KFSUPGRADE-583
    
    private AttachmentService attachmentService;
    private BusinessObjectService businessObjectService;
    private PurchaseOrderService purchaseOrderService;
    private VendorService vendorService;
    
    @Override
    public String sendPurchaseOrder(PurchaseOrderDocument purchaseOrder) {
        /*
         * IMPORTANT DESIGN NOTE: We need the contract manager's name, phone number, and e-mail address. B2B orders that don't
         * qualify to become APO's will have contract managers on the PO, and the ones that DO become APO's will not. We decided to
         * always get the contract manager from the B2B contract associated with the order, and for B2B orders to ignore the
         * contract manager field on the PO. We pull the name and phone number from the contract manager table and get the e-mail
         * address from the user data.
         */

    	// CU change for contract manager
        // non-catalog POs might not have a vendor contract, so we need to get the contract manager from the PO which will always be there
        ContractManager contractManager = null;
        
        if (!PurapConstants.RequisitionSources.B2B.equals(purchaseOrder.getRequisitionSourceCode())) {
        	contractManager = purchaseOrder.getContractManager();
        } else {
        	contractManager = purchaseOrder.getVendorContract().getContractManager();
        }
        String contractManagerEmail = getContractManagerEmail(contractManager);

        String vendorDuns = purchaseOrder.getVendorDetail().getVendorDunsNumber();

        RequisitionDocument r = requisitionService.getRequisitionById(purchaseOrder.getRequisitionIdentifier());
        WorkflowDocument reqWorkflowDoc = r.getDocumentHeader().getWorkflowDocument();
        String requisitionInitiatorPrincipalId = getRequisitionInitiatorPrincipal(reqWorkflowDoc.getInitiatorPrincipalId());

        if (LOG.isDebugEnabled()) {
            LOG.debug("sendPurchaseOrder(): b2bPurchaseOrderURL is " + getB2bPurchaseOrderURL());
        }
        if (!PurapConstants.RequisitionSources.B2B.equals(purchaseOrder.getRequisitionSourceCode())) {
            prepareNonB2BPurchaseOrderForTransmission(purchaseOrder);
        }

        String validateErrors = verifyCxmlPOData(purchaseOrder, requisitionInitiatorPrincipalId, getB2bPurchaseOrderPassword(), contractManager, contractManagerEmail, vendorDuns);
        if (!StringUtils.isEmpty(validateErrors)) {
            return validateErrors;
        }

        StringBuffer transmitErrors = new StringBuffer();

        try {
            LOG.debug("sendPurchaseOrder() Generating cxml");
            String cxml = getCxml(purchaseOrder, requisitionInitiatorPrincipalId, getB2bPurchaseOrderPassword(), contractManager, contractManagerEmail, vendorDuns, true);

            LOG.debug("sendPurchaseOrder() Sending cxml\n" + cxml);
            LOG.debug("sendPurchaseOrder() Sending cxml\n" + cxml);
            String responseCxml = b2bDao.sendPunchOutRequest(cxml, getB2bPurchaseOrderURL());
            LOG.info("b2bPurchaseOrderURL " + getB2bPurchaseOrderURL());

            LOG.info("sendPurchaseOrder(): Response cXML for po #" + purchaseOrder.getPurapDocumentIdentifier() + ":\n" + responseCxml);
            
            // allow PO to use old form, then POA use new form for testing
            if (!responseCxml.contains("Success") && responseCxml.contains("No custom field found") && responseCxml.contains("document configuration (DeliveryEmail)")) {               
                String cxml1 = cxml.substring(0, cxml.indexOf("<CustomFieldValueSet label=\"Delivery Phone")) +
                                              cxml.substring(cxml.indexOf("</POHeader>"));
                LOG.info("sendPurchaseOrder() re-Sending cxml\n" + cxml1);
                responseCxml = b2bDao.sendPunchOutRequest(cxml1, getB2bPurchaseOrderURL());
                cxml = getCxml(purchaseOrder, requisitionInitiatorPrincipalId, getB2bPurchaseOrderPassword(), contractManager, contractManagerEmail, vendorDuns, false);
                LOG.debug("sendPurchaseOrder() re-Sending cxml\n" + cxml);
                responseCxml = b2bDao.sendPunchOutRequest(cxml, getB2bPurchaseOrderURL());
                LOG.info("re-sendPurchaseOrder(): Response cXML for po number " + purchaseOrder.getPurapDocumentIdentifier() + ":" + responseCxml);
            }

            PurchaseOrderResponse poResponse = B2BParserHelper.getInstance().parsePurchaseOrderResponse(responseCxml);
            String statusText = poResponse.getStatusText();
            if (LOG.isDebugEnabled()) {
                LOG.debug("sendPurchaseOrder(): statusText is " + statusText);
            }
            if (ObjectUtils.isNull(statusText) || (!"success".equalsIgnoreCase(statusText.trim()))) {
                LOG.error("sendPurchaseOrder(): PO cXML for po number " + purchaseOrder.getPurapDocumentIdentifier() + " failed sending to SciQuest:\n" + statusText);
                transmitErrors.append("Unable to send Purchase Order: " + statusText);

                // find any additional error messages that might have been sent
                List errorMessages = poResponse.getPOResponseErrorMessages();
                if (ObjectUtils.isNotNull(errorMessages) && !errorMessages.isEmpty()) {
                    for (Iterator iter = errorMessages.iterator(); iter.hasNext();) {
                        String errorMessage = (String) iter.next();
                        if (ObjectUtils.isNotNull(errorMessage)) {
                            LOG.error("sendPurchaseOrder(): SciQuest error message for po number " + purchaseOrder.getPurapDocumentIdentifier() + ": " + errorMessage);
                            transmitErrors.append("Error sending Purchase Order: " + errorMessage);
                        }
                    }
                }
            }
        }
        catch (B2BConnectionException e) {
            LOG.error("sendPurchaseOrder() Error connecting to b2b", e);
            transmitErrors.append("Connection to vendor failed.");
        }
        catch (CxmlParseError e) {
            LOG.error("sendPurchaseOrder() Error Parsing", e);
            transmitErrors.append("Unable to read cxml returned from vendor.");
        }
        catch (Throwable e) {
            LOG.error("sendPurchaseOrder() Unknown Error", e);
            transmitErrors.append("Unexpected error occurred while attempting to transmit Purchase Order.");
        }

        return transmitErrors.toString();
    }

    @Override
    public String getCxml(PurchaseOrderDocument purchaseOrder, String requisitionInitiatorPrincipalId, String password, ContractManager contractManager, String contractManagerEmail, String vendorDuns, boolean includeNewFields) {
    	WorkflowDocument workFlowDocument = purchaseOrder.getDocumentHeader().getWorkflowDocument();
        String documentType = workFlowDocument.getDocumentTypeName();

        // KFSUPGRADE-1458 complete the potransmission enhancement
        int disbMethod = 0;
        String poTransmissionCode = purchaseOrder.getPurchaseOrderTransmissionMethodCode();

        if(PurapConstants.POTransmissionMethods.FAX.equalsIgnoreCase(poTransmissionCode)) {
            // fax
        	disbMethod = FAX;
        } else if (CUPurapConstants.POTransmissionMethods.EMAIL.equalsIgnoreCase(poTransmissionCode)) {
        	// email
        	disbMethod = EMAIL;
        } else if(CUPurapConstants.POTransmissionMethods.CONVERSION.equalsIgnoreCase(poTransmissionCode)) {
            // conversion
        	disbMethod = CONVERSION;
        } else {
            // manual
        	disbMethod = MANUAL;
        }
        StringBuffer cxml = new StringBuffer();
        
        List<Note> notesToSendToVendor = getNotesToSendToVendor(purchaseOrder);
        // comment out for investigation
        if (CollectionUtils.isNotEmpty(notesToSendToVendor)) {
            cxml.append("--" + CUPurapConstants.MIME_BOUNDARY_FOR_ATTACHMENTS + "\r\n");
            cxml.append("Content-Type: application/xop+xml;\r\n");
            cxml.append("        charset=\"UTF-8\";\r\n");
            cxml.append("        type=\"text/xml\"\r\n");
            cxml.append("Content-Transfer-Encoding: 8bit\r\n");
            cxml.append("Content-ID: <1222711868656.6893160141700477326@sciquest.com>\r\n");
            cxml.append("\r\n"); // blank line before part
        }

        cxml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        cxml.append("<!DOCTYPE PurchaseOrderMessage SYSTEM \"PO.dtd\">\n");
        cxml.append("<PurchaseOrderMessage version=\"2.0\">\n");
        cxml.append("  <Header>\n");

        // MessageId - can be whatever you would like it to be. Just make it unique.
        cxml.append("    <MessageId>KFS_cXML_PO</MessageId>\n");

        // Timestamp - it doesn't matter what's in the timezone, just that it's there (need "T" space between date/time)
        Date currentDate = getDateTimeService().getCurrentDate();
        SimpleDateFormat dateFormat = PurApDateFormatUtils.getSimpleDateFormat(
                PurapConstants.NamedDateFormats.CXML_SIMPLE_DATE_FORMAT);
        SimpleDateFormat timeFormat = PurApDateFormatUtils.getSimpleDateFormat(
                PurapConstants.NamedDateFormats.CXML_SIMPLE_TIME_FORMAT);
        cxml.append("    <Timestamp>").append(dateFormat.format(currentDate)).append("T")
                .append(timeFormat.format(currentDate)).append("+05:30")
                .append("</Timestamp>\n");

        cxml.append("    <Authentication>\n");
        // Cu is using useragent
        //cxml.append("      <Identity>").append(getB2bPurchaseOrderIdentity()).append("</Identity>\n");
        cxml.append("      <Identity>").append(getB2bUserAgent()).append("</Identity>\n");
        cxml.append("      <SharedSecret>").append(password).append("</SharedSecret>\n");
        cxml.append("    </Authentication>\n");
        cxml.append("  </Header>\n");
        cxml.append("  <PurchaseOrder>\n");
        //KFSPTS-1458  -- Added two if-checks for MOPOT in following if-elsif for POV and POA documents to set <DistributeRevision> tag to "true" based on MOPOT value. 
        //KFSPTS-1458  -- These same checks/changes needed to be added to the <DistributionMethod> tag section further on in the file because of how the cxml needs to be created.
        //KFSPTS-1458  -- Based true/false value for both <DistributeRevision> tag and <DistributionMethod> tag on MOPOT value being "US Mail" (MANUAL=MANL) or "Do Not Send" (CONVERSION=CNVS)       
        // void = VOPE      ammend = CGIN ?   ammend should =
        if (StringUtils.equals(workFlowDocument.getApplicationDocumentStatus(), PurchaseOrderStatuses.APPDOC_PENDING_VOID) || documentType.equalsIgnoreCase(PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_VOID_DOCUMENT)) {
            cxml.append("    <POHeader type=\"cancel\">\n");            
            //KFSPTS-1458 -- When MOPOT is "Do Not Send" = code of conversion,  distribute revision tag needs to be false;
            //KFSPTS-1458 -- otherwise distribute revision tag needs to be true (for "Fax", "Email", and "US Mail"=manual mopot).
            if (disbMethod == CONVERSION) {
            	cxml.append("    <DistributeRevision>false</DistributeRevision>\n");
            }
            else { //FAX, EMAIL, MANUAL=US Mail
            	cxml.append("    <DistributeRevision>true</DistributeRevision>\n");
            }
        } else if (documentType.equals(PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_AMENDMENT_DOCUMENT)) {
            cxml.append("    <POHeader type=\"update\">\n");
            //KFSPTS-1458 -- When MOPOT is "Do Not Send" = code of conversion,  distribute revision tag needs to be false;
            //KFSPTS-1458 -- otherwise distribute revision tag needs to be true (for "Fax", "Email", and "US Mail"=manual mopot).
            if (disbMethod == CONVERSION) {
            	cxml.append("    <DistributeRevision>false</DistributeRevision>\n");
            }
            else { //FAX, EMAIL, MANUAL=US Mail
            	cxml.append("    <DistributeRevision>true</DistributeRevision>\n");
            }
        } else  {
          cxml.append("    <POHeader>\n");
        }
        cxml.append("      <PONumber>").append(purchaseOrder.getPurapDocumentIdentifier()).append("</PONumber>\n");
        cxml.append("      <Requestor>\n");
        cxml.append("        <UserProfile username=\"").append(requisitionInitiatorPrincipalId.toUpperCase(Locale.US)).append("\">\n");
        cxml.append("        </UserProfile>\n");
        cxml.append("      </Requestor>\n");
        cxml.append("      <Priority>High</Priority>\n");
        cxml.append("      <AccountingDate>").append(purchaseOrder.getPurchaseOrderCreateTimestamp()).append("</AccountingDate>\n");

        // CU modified the supplier section.
        // Provide DUNS number and SupplierNumber if vendor is B2B
        if(purchaseOrder.getVendorContract() != null && purchaseOrder.getVendorContract().getVendorB2bIndicator() && disbMethod!=CONVERSION) {
	        /** *** SUPPLIER SECTION **** */
	        cxml.append("      <Supplier>\n");
	        cxml.append("        <DUNS>").append(vendorDuns).append("</DUNS>\n");
	        cxml.append("        <SupplierNumber>").append(purchaseOrder.getVendorNumber()).append("</SupplierNumber>\n");
	
	        // Type attribute is required. Valid values: main and technical. Only main will be considered for POImport.
	        cxml.append("        <ContactInfo type=\"main\">\n");
	        // TelephoneNumber is required. With all fields, only numeric digits will be stored. Non-numeric characters are allowed, but
	        // will be stripped before storing.
	        cxml.append("          <Phone>\n");
	        cxml.append("            <TelephoneNumber>\n");
	        cxml.append("              <CountryCode>1</CountryCode>\n");
	        if (contractManager.getContractManagerPhoneNumber().length() > 4) {
	            cxml.append("              <AreaCode>").append(contractManager.getContractManagerPhoneNumber().substring(0, 3)).append("</AreaCode>\n");
	            cxml.append("              <Number>").append(contractManager.getContractManagerPhoneNumber().substring(3)).append("</Number>\n");
	        }
	        else {
	            LOG.error("getCxml() The phone number is invalid for this contract manager: " + contractManager.getContractManagerUserIdentifier() + " " + contractManager.getContractManagerName());
	            cxml.append("              <AreaCode>555</AreaCode>\n");
	            cxml.append("              <Number>").append(contractManager.getContractManagerPhoneNumber()).append("</Number>\n");
	        }
	        cxml.append("            </TelephoneNumber>\n");
	        cxml.append("          </Phone>\n");
	        cxml.append("        </ContactInfo>\n");
	        cxml.append("      </Supplier>\n");

        // Only pass address and distribution method if vendor is non-B2B
        } else {

            /** *** SUPPLIER SECTION **** */
            cxml.append("      <Supplier>\n");
            cxml.append("        <Name><![CDATA[").append(purchaseOrder.getVendorName()).append("]]></Name>\n");

            // Type attribute is required. Valid values: main and technical. Only main will be considered for POImport.
            cxml.append("        <ContactInfo type=\"main\">\n");
            // TelephoneNumber is required. With all fields, only numeric digits will be stored. Non-numeric characters are allowed, but
            // will be stripped before storing.
            cxml.append("          <Phone>\n");
            cxml.append("            <TelephoneNumber>\n");
            cxml.append("              <CountryCode>1</CountryCode>\n");
            if (StringUtils.isNotEmpty(purchaseOrder.getVendorPhoneNumber()) && purchaseOrder.getVendorPhoneNumber().length() > 4) {
                cxml.append("              <AreaCode>").append(purchaseOrder.getVendorPhoneNumber().substring(0, 3)).append("</AreaCode>\n");
                cxml.append("              <Number>").append(purchaseOrder.getVendorPhoneNumber().substring(3)).append("</Number>\n");
            } else {

                cxml.append("              <AreaCode>000</AreaCode>\n");
                cxml.append("              <Number>-000-0000</Number>\n");
            }

            cxml.append("            </TelephoneNumber>\n");
            cxml.append("          </Phone>\n");

            cxml.append("          <ContactAddress>\n");
            if (StringUtils.isNotEmpty(purchaseOrder.getVendorLine1Address())) {
                cxml.append("          <AddressLine label=\"Street1\" linenumber=\"1\"><![CDATA[").append(purchaseOrder.getVendorLine1Address()).append("]]></AddressLine>\n");
            }
            if (StringUtils.isNotEmpty(purchaseOrder.getVendorLine2Address())) {
                cxml.append("          <AddressLine label=\"Street2\" linenumber=\"2\"><![CDATA[").append(purchaseOrder.getVendorLine2Address()).append("]]></AddressLine>\n");
            }
            if (StringUtils.isNotEmpty(purchaseOrder.getVendorStateCode())) {
                cxml.append("          <State>").append(purchaseOrder.getBillingStateCode()).append("</State>\n");
            }
            if (StringUtils.isNotEmpty(purchaseOrder.getVendorPostalCode())) {
                cxml.append("          <PostalCode>").append(purchaseOrder.getBillingPostalCode()).append("</PostalCode>\n");
            }
            if (StringUtils.isNotEmpty(purchaseOrder.getVendorCountryCode())) {
                cxml.append("          <Country isocountrycode=\"").append(purchaseOrder.getBillingCountryCode()).append("\">").append(purchaseOrder.getBillingCountryCode()).append("</Country>\n");
            }
            cxml.append("          </ContactAddress>\n");

            cxml.append("        </ContactInfo>\n");
            cxml.append("      </Supplier>\n");

            /** *** DISTRIBUTION SECTION *** */
            VendorAddress vendorAddress = vendorService.getVendorDefaultAddress(purchaseOrder.getVendorDetail().getVendorAddresses(), VendorConstants.AddressTypes.PURCHASE_ORDER, purchaseOrder.getDeliveryCampusCode());
            cxml.append("      <OrderDistribution>\n");

            // first take fax from PO, if empty then get fax number for PO default vendor address
            String vendorFaxNumber = purchaseOrder.getVendorFaxNumber();
            if (StringUtils.isBlank(vendorFaxNumber) && vendorAddress != null) {
                vendorFaxNumber = vendorAddress.getVendorFaxNumber();
            }

            //KFSPTS-1458 -- code existing prior to this change is commented out below.
            //KFSPTS-1458 -- new code should model how fax number is determined:
            //KFSPTS-1458 -- first take vendor email address from PO, if empty then get vendor email address from PO default vendor address
            //String emailAddress = "";
            //if (vendorAddress != null) {
            //    emailAddress = vendorAddress.getVendorAddressEmailAddress();
            //}            
            String emailAddress = purchaseOrder.getVendorEmailAddress();
            if (StringUtils.isBlank(emailAddress) && vendorAddress != null) {
            	emailAddress = vendorAddress.getVendorAddressEmailAddress();
            }

            // Distribution Method
            switch(disbMethod) {
	            case FAX:
	                // fax
	                cxml.append("        <DistributionMethod type=\"fax\">\n");
	                cxml.append("          <Fax>\n");
	                cxml.append("            <TelephoneNumber>\n");
	                cxml.append("              <CountryCode>1</CountryCode>\n");
	                cxml.append("              <AreaCode>").append(vendorFaxNumber.substring(0, 3)).append("</AreaCode>\n");
	                cxml.append("              <Number>").append(vendorFaxNumber.substring(3)).append("</Number>\n");
	                cxml.append("            </TelephoneNumber>\n");
	                cxml.append("          </Fax>\n");
	                break;
	            case EMAIL:
	            	// email
	                cxml.append("        <DistributionMethod type=\"html_email_attachments\">\n");
	                cxml.append("          <Email><![CDATA[").append(emailAddress).append("]]></Email>\n");
	                break;
	            case CONVERSION:
	                // conversion
	            	//KFSPTS-1458 : removed logic that was temp mopot for conversion. 	            	
	            	///**
	            	// * Temporary method of distribution for PO conversion effort.  Will be short-lived, so we 
	            	// * didn't bother setting up constants and parameters to store the email, but rather hard-
	            	// * coded the email address.
	            	// */
	                //cxml.append("        <DistributionMethod type=\"html_email_attachments\">\n");
	                //cxml.append("          <Email><![CDATA[").append("db18@cornell.edu").append("]]></Email>\n");
	            	//
	            	//KFSPTS-1458: replaced temp logic with distribution method type of manual
	            	cxml.append("        <DistributionMethod type=\"manual\">\n");
	                break;
	            default:
	                // manual
	                //KFSPTS-1458 removed: cxml.append("        <DistributionMethod type=\"manual\">\n");
	            	//
	            	//KFSPTS-1458: US Mail = manual should be sent to EGA email address
	            	//KFSPTS-1458: Replaced hard coded email address with parameterized email address. 
	                cxml.append("        <DistributionMethod type=\"html_email_attachments\">\n");
	                cxml.append("          <Email><![CDATA[").append(this.getMethodOfPOTransmissionConversionEmail()).append("]]></Email>\n");
	                break;
            } 
            cxml.append("        </DistributionMethod>\n");
            cxml.append("      </OrderDistribution>\n");
        }

        /** *** BILL TO SECTION **** */
        cxml.append("      <BillTo>\n");
        cxml.append("        <Address>\n");
        cxml.append("          <TemplateName>Bill To</TemplateName>\n");
        cxml.append("          <AddressCode>").append(purchaseOrder.getDeliveryCampusCode()).append("</AddressCode>\n");
        // Contact - There can be 0-5 Contact elements. The label attribute is optional.
        // TODO : this may cause sequence error from SQ if email is empty, then it will send sequence of 1,2,3,5.
        // in CU 3.0.1, it only sends billingphone#
//        cxml.append("          <Contact label=\"FirstName\" linenumber=\"1\"><![CDATA[Accounts]]></Contact>\n");
//        cxml.append("          <Contact label=\"LastName\" linenumber=\"2\"><![CDATA[Payable]]></Contact>\n");
//        cxml.append("          <Contact label=\"Company\" linenumber=\"3\"><![CDATA[").append(purchaseOrder.getBillingName().trim()).append("]]></Contact>\n");
//        // since email address is not required, we need to check whether its empty; if yes, don't add it
//        if (!StringUtils.isEmpty(purchaseOrder.getBillingEmailAddress())) {
//            cxml.append("          <Contact label=\"Email\" linenumber=\"4\"><![CDATA[").append(purchaseOrder.getBillingEmailAddress().trim()).append("]]></Contact>\n");
//        }
        // since phone number is not required, we need to check whether its empty; if yes, don't add it
//        if (!StringUtils.isEmpty(purchaseOrder.getBillingPhoneNumber())) {
//            cxml.append("          <Contact label=\"Phone\" linenumber=\"5\"><![CDATA[").append(purchaseOrder.getBillingPhoneNumber().trim()).append("]]></Contact>\n");
            cxml.append("          <Contact label=\"Phone\" linenumber=\"1\"><![CDATA[").append(purchaseOrder.getBillingPhoneNumber().trim()).append("]]></Contact>\n");
//        }
        // There must be 1-5 AddressLine elements. The label attribute is optional.
        cxml.append("          <AddressLine label=\"Street1\" linenumber=\"1\"><![CDATA[").append(purchaseOrder.getBillingLine1Address()).append("]]></AddressLine>\n");
        cxml.append("          <AddressLine label=\"Street2\" linenumber=\"2\"><![CDATA[").append(purchaseOrder.getBillingLine2Address()).append("]]></AddressLine>\n");
        cxml.append("          <City><![CDATA[").append(purchaseOrder.getBillingCityName()).append("]]></City>\n"); // Required.
        cxml.append("          <State>").append(purchaseOrder.getBillingStateCode()).append("</State>\n");
        cxml.append("          <PostalCode>").append(purchaseOrder.getBillingPostalCode()).append("</PostalCode>\n"); // Required.
        cxml.append("          <Country isocountrycode=\"").append(purchaseOrder.getBillingCountryCode()).append("\">").append(purchaseOrder.getBillingCountryCode()).append("</Country>\n");
        cxml.append("        </Address>\n");
        cxml.append("      </BillTo>\n");

        /** *** SHIP TO SECTION **** */
        cxml.append("      <ShipTo>\n");
        cxml.append("        <Address>\n");
        cxml.append("          <TemplateName>Ship To</TemplateName>\n");
        // AddressCode. A code to identify the address, that is sent to the supplier.
        cxml.append("          <AddressCode>").append(purchaseOrder.getDeliveryBuildingCode()).append("</AddressCode>\n");
        cxml.append("          <Contact label=\"Name\" linenumber=\"1\"><![CDATA[").append(purchaseOrder.getDeliveryToName().trim()).append("]]></Contact>\n");
        cxml.append("          <Contact label=\"PurchasingEmail\" linenumber=\"2\"><![CDATA[").append(contractManagerEmail).append("]]></Contact>\n");
        if (ObjectUtils.isNotNull(purchaseOrder.getInstitutionContactEmailAddress())) {
            cxml.append("          <Contact label=\"ContactEmail\" linenumber=\"3\"><![CDATA[").append(purchaseOrder.getInstitutionContactEmailAddress()).append("]]></Contact>\n");
        }
        else {
            cxml.append("          <Contact label=\"ContactEmail\" linenumber=\"3\"><![CDATA[").append(purchaseOrder.getRequestorPersonEmailAddress()).append("]]></Contact>\n");
        }
        if (ObjectUtils.isNotNull(purchaseOrder.getInstitutionContactPhoneNumber())) {
            cxml.append("          <Contact label=\"Phone\" linenumber=\"4\"><![CDATA[").append(purchaseOrder.getInstitutionContactPhoneNumber().trim()).append("]]></Contact>\n");
        }
        else {
            cxml.append("          <Contact label=\"Phone\" linenumber=\"4\"><![CDATA[").append(purchaseOrder.getRequestorPersonPhoneNumber()).append("]]></Contact>\n");
        }

        //check indicator to decide if receiving or delivery address should be sent to the vendor
        if (purchaseOrder.getAddressToVendorIndicator()) { //use receiving address
            cxml.append("          <AddressLine label=\"Street1\" linenumber=\"1\"><![CDATA[").append(purchaseOrder.getReceivingName().trim()).append("]]></AddressLine>\n");
            cxml.append("          <AddressLine label=\"Street2\" linenumber=\"2\"><![CDATA[").append(purchaseOrder.getReceivingLine1Address().trim()).append("]]></AddressLine>\n");
            if (ObjectUtils.isNull(purchaseOrder.getReceivingLine2Address())) {
                cxml.append("          <AddressLine label=\"Street3\" linenumber=\"3\"><![CDATA[").append(" ").append("]]></AddressLine>\n");
            }
            else {
                cxml.append("          <AddressLine label=\"Street3\" linenumber=\"3\"><![CDATA[").append(purchaseOrder.getReceivingLine2Address()).append("]]></AddressLine>\n");
            }
            cxml.append("          <City><![CDATA[").append(purchaseOrder.getReceivingCityName().trim()).append("]]></City>\n");
            cxml.append("          <State>").append(purchaseOrder.getReceivingStateCode()).append("</State>\n");
            cxml.append("          <PostalCode>").append(purchaseOrder.getReceivingPostalCode()).append("</PostalCode>\n");
            cxml.append("          <Country isocountrycode=\"").append(purchaseOrder.getReceivingCountryCode()).append("\">").append(purchaseOrder.getReceivingCountryCode()).append("</Country>\n");
        }
        else { //use final delivery address
            cxml.append(getBuildingLine(purchaseOrder));
            cxml.append("          <AddressLine label=\"Street1\" linenumber=\"1\"><![CDATA[").append(purchaseOrder.getDeliveryBuildingLine1Address().trim()).append("]]></AddressLine>\n");
            cxml.append("          <AddressLine label=\"Street2\" linenumber=\"2\"><![CDATA[Room #").append(purchaseOrder.getDeliveryBuildingRoomNumber().trim()).append("]]></AddressLine>\n");
            cxml.append("          <AddressLine label=\"Company\" linenumber=\"4\"><![CDATA[").append(purchaseOrder.getBillingName().trim()).append("]]></AddressLine>\n");
            if (ObjectUtils.isNull(purchaseOrder.getDeliveryBuildingLine2Address())) {
                cxml.append("          <AddressLine label=\"Street3\" linenumber=\"3\"><![CDATA[").append(" ").append("]]></AddressLine>\n");
            }
            else {
                cxml.append("          <AddressLine label=\"Street3\" linenumber=\"3\"><![CDATA[").append(purchaseOrder.getDeliveryBuildingLine2Address()).append("]]></AddressLine>\n");
            }
            cxml.append("          <City><![CDATA[").append(purchaseOrder.getDeliveryCityName().trim()).append("]]></City>\n");
            cxml.append("          <State>").append(purchaseOrder.getDeliveryStateCode()).append("</State>\n");
            cxml.append("          <PostalCode>").append(purchaseOrder.getDeliveryPostalCode()).append("</PostalCode>\n");
            cxml.append("          <Country isocountrycode=\"").append(purchaseOrder.getDeliveryCountryCode()).append("\">").append(purchaseOrder.getDeliveryCountryCode()).append("</Country>\n");
        }

        cxml.append("        </Address>\n");
        cxml.append("      </ShipTo>\n");

        // KFSUPGRADE-408
        // Get payment terms for the PO
        String payTermCode = purchaseOrder.getVendorPaymentTermsCode();
        if(StringUtils.isBlank(payTermCode)) {
        	// Retrieve the vendor and pull the payment terms from the vendor record if the PO terms are null
        	VendorDetail poVendor = vendorService.getByVendorNumber(purchaseOrder.getVendorNumber());
        	payTermCode = poVendor.getVendorPaymentTermsCode();
        }
        
        PaymentTermType payTerm = businessObjectService.findBySinglePrimaryKey(PaymentTermType.class, payTermCode);
        
        cxml.append("      <PaymentInfo>\n");
        cxml.append("        <Terms>\n");
        if(payTerm.getVendorPaymentTermsPercent().doubleValue() > 0.0) {
        	cxml.append("          <Discount>").append(payTerm.getVendorPaymentTermsPercent()).append("</Discount>\n");
        }
        if(payTerm.getVendorDiscountDueNumber().doubleValue() > 0.0) {
        	cxml.append("          <Days>").append(payTerm.getVendorDiscountDueNumber()).append("</Days>\n");
        }
        cxml.append("          <Net>").append(payTerm.getVendorNetDueNumber()).append("</Net>\n");
        cxml.append("        </Terms>\n");
        cxml.append("      </PaymentInfo>\n");
        // end KFSUPGRADE-408
        /** *** EXTERNAL INFO SECTION **** */
//KAFSUPGRADE-407 : cu enhancement. This will be refactored by kfspts-794         
        /** *** EXTERNAL INFO SECTION **** */
        //KFSPTS-794: Original code prior to change going in.         
        //        String vendorNoteText = purchaseOrder.getVendorNoteText();
        //        if (ObjectUtils.isNotNull(vendorNoteText)) {
        //	        cxml.append("      <ExternalInfo>\n");
        //	        cxml.append("        <Note><![CDATA[").append(vendorNoteText).append("]]></Note>\n");
        //	        cxml.append("      </ExternalInfo>\n");
        //        }
        /*KFSPTS-794: Start new code: Define the attachments
         * * This code change was taken from an enhancement provided to Cornell from CSU.  
         * The specific implementation pieces required for this enhancement needed to be taken from the files
         * * provided and incorporated into our code base.
         * */
        //Attachments must be defined in the xml part and must match info in MIME binary part
        //        List<Note> notesToSendToVendor = getNotesToSendToVendor(purchaseOrder);
        if (!notesToSendToVendor.isEmpty()) {
            String allNotes = "";
            String allNotesNoAttach = "";
            String vendorNoteText = purchaseOrder.getVendorNoteText();
            if (ObjectUtils.isNotNull(vendorNoteText)) {
            	allNotesNoAttach = vendorNoteText;
            }
            cxml.append("      <ExternalInfo>\n");
            for (int i = 0; i < notesToSendToVendor.size(); i++) {
                Note note = notesToSendToVendor.get(i);
                Attachment attachment = attachmentService.getAttachmentByNoteId(note.getNoteIdentifier());
                if (ObjectUtils.isNotNull(attachment)) {
                    allNotes = allNotes + "\n(" + (i + 1) + ") " + note.getNoteText() + "  ";
                } else {
                    allNotesNoAttach = allNotesNoAttach + "          " + note.getNoteText() + "          ";
                }
            }
            cxml.append("          <Note><![CDATA[" + allNotesNoAttach + "          " + allNotes + "]]></Note>\n");
            cxml.append("        <Attachments xmlns:xop = \"http://www.w3.org/2004/08/xop/include/\" >\n");

            for (int i = 0; i < notesToSendToVendor.size(); i++) {
                Note note = notesToSendToVendor.get(i);
                Attachment attachment = attachmentService.getAttachmentByNoteId(note.getNoteIdentifier());
                if (ObjectUtils.isNotNull(attachment)) {
                    cxml.append("          <Attachment id=\"" + attachment.getAttachmentIdentifier() + "\" type=\"file\">\n");
                    cxml.append("            <AttachmentName><![CDATA[" + attachment.getAttachmentFileName() + "]]></AttachmentName>\n");
                    cxml.append("            <AttachmentURL>http://usertest.sciquest.com/apps/Router/ReqAttachmentDownload?AttachmentId=" + attachment.getAttachmentIdentifier() +
                            "&amp;DocId=" + purchaseOrder.getPurapDocumentIdentifier() +
                            "&amp;OrgName=SQSupportTest&amp;AuthMethod=Local</AttachmentURL>\n");
                    cxml.append("            <AttachmentSize>" + attachment.getAttachmentFileSize() / 1024 + "</AttachmentSize>\n");
                    cxml.append("            <xop:Include href=\"cid:" + attachment.getAttachmentIdentifier() + "@sciquest.com\" />\n");
                    cxml.append("          </Attachment>\n");
                }
            }
            cxml.append("        </Attachments>\n");
            cxml.append("      </ExternalInfo>\n");
        } else {
            String vendorNoteText = purchaseOrder.getVendorNoteText();
            if (ObjectUtils.isNotNull(vendorNoteText)) {
  	            cxml.append("      <ExternalInfo>\n");
  	            cxml.append("        <Note><![CDATA[").append(vendorNoteText).append("]]></Note>\n");
  	            cxml.append("      </ExternalInfo>\n");
             }

        }
/*KFSPTS-794: End new code: Define the attachments */
        
        cxml.append("      <CustomFieldValueSet label=\"Contact Name\" name=\"InitiatorName\">\n");
        cxml.append("        <CustomFieldValue>\n");
        if (ObjectUtils.isNotNull(purchaseOrder.getInstitutionContactName())) {
        	cxml.append("          <Value><![CDATA[").append(purchaseOrder.getInstitutionContactName()).append("]]></Value>\n");
        } else {
            cxml.append("          <Value><![CDATA[").append(purchaseOrder.getRequestorPersonName()).append("]]></Value>\n");
        }
        cxml.append("         </CustomFieldValue>\n");
        cxml.append("      </CustomFieldValueSet>\n");
        
        cxml.append("      <CustomFieldValueSet label=\"Supplier Address\" name=\"SupplierAddress1\">\n");
        cxml.append("        <CustomFieldValue>\n");
        cxml.append("          <Value><![CDATA[").append(purchaseOrder.getVendorLine1Address()).append("]]></Value>\n");
        cxml.append("         </CustomFieldValue>\n");
        cxml.append("      </CustomFieldValueSet>\n");
        cxml.append("      <CustomFieldValueSet label=\"Supplier City State Zip\" name=\"SupplierCityStateZip\">\n");
        cxml.append("        <CustomFieldValue>\n");
        cxml.append("          <Value><![CDATA[").append(purchaseOrder.getVendorCityName()).append(", ").append(purchaseOrder.getVendorStateCode()).append(" ").append(purchaseOrder.getVendorPostalCode()).append("]]></Value>\n");
        cxml.append("         </CustomFieldValue>\n");
        cxml.append("      </CustomFieldValueSet>\n");
        
        String deliveryInstructionText = purchaseOrder.getDeliveryInstructionText();
        
        if (ObjectUtils.isNotNull(deliveryInstructionText)) {
	        cxml.append("      <CustomFieldValueSet label=\"Delivery Instructions\" name=\"Delivery Instructions\">\n");
	        cxml.append("        <CustomFieldValue>\n");
	        cxml.append("          <Value><![CDATA[").append(deliveryInstructionText).append("]]></Value>\n");
	        cxml.append("         </CustomFieldValue>\n");
	        cxml.append("      </CustomFieldValueSet>\n");
        }
 // end KFSUPGRADE-407    
        
        // KFSUPGRADE-583
        if (purchaseOrder.getDocumentFundingSourceCode().equalsIgnoreCase(CUPurapConstants.PurapFundingSources.FEDERAL_FUNDING_SOURCE)) {
	        cxml.append("      <CustomFieldValueSet label=\"Funding\" name=\"Funding\">\n");
	        cxml.append("        <CustomFieldValue>\n");
	        cxml.append("          <Value><![CDATA[" + FEDERAL_FUNDING_TEXT + "]]></Value>\n");
	        cxml.append("         </CustomFieldValue>\n");
	        cxml.append("      </CustomFieldValueSet>\n");
        }
        // END KFSUPGRADE-583
        
        // KFSUPGRADE-400 KFSPTS-427 : additional fields
        // do we have to check if field is empty or null ?
		if (includeNewFields) {
			cxml.append(addCustomFieldValueSet("DeliveryPhone", "Delivery Phone", purchaseOrder.getDeliveryToPhoneNumber()));
			cxml.append(addCustomFieldValueSet("DeliveryEmail", "Delivery Email", purchaseOrder.getDeliveryToEmailAddress()));
			cxml.append(addCustomFieldValueSet("ShipTitle", "Ship Title", getVendorShipTitle(purchaseOrder)));
			cxml.append(addCustomFieldValueSet("ShipPayTerms", "Ship Pay Termse", getVendorShipPayTerms(purchaseOrder)));
			cxml.append(addCustomFieldValueSet("SupplierAddress2", "Supplier Address 2", purchaseOrder.getVendorLine2Address()));
			cxml.append(addCustomFieldValueSet("SupplierCountry", "Supplier Country", getVendorCountry(purchaseOrder)));
		}
  
  
        // end KFSUPGRADE-400 KFSPTS-427 fields
        cxml.append("    </POHeader>\n");

        /** *** Items Section **** */
        List detailList = purchaseOrder.getItems();
        for (Iterator iter = detailList.iterator(); iter.hasNext();) {
            PurchaseOrderItem poi = (PurchaseOrderItem) iter.next();
            if ((ObjectUtils.isNotNull(poi.getItemType())) && poi.getItemType().isLineItemIndicator()) {
            	//KFSUPGRADE-406
                String uom = poi.getItemUnitOfMeasureCode();
                KualiDecimal quantity = poi.getItemQuantity();
                // Pass in value of 1 for quantity if no quantity provided. This helps handle non-quantity orders in SciQuest. KFSPTS-792
                if (quantity == null || quantity.isZero()) {
                        quantity = new KualiDecimal(1);
                        uom = NON_QUANTITY_UOM;
                }
                //end KFSUPGRADE-406
                // ==== CU Customization: Use quantity of zero for inactive PO items, allowing SciQuest to cancel them properly. ====
                if (!poi.isItemActiveIndicator()) {
                    quantity = new KualiDecimal(0);
                }
                // ==== End CU Customization ====
                
                cxml.append("    <POLine linenumber=\"").append(poi.getItemLineNumber()).append("\">\n");
                cxml.append("      <Item>\n");
                // CatalogNumber - This is a string that the supplier uses to identify the item (i.e., SKU). Optional.
                cxml.append("        <CatalogNumber><![CDATA[").append(poi.getItemCatalogNumber()).append("]]></CatalogNumber>\n");
                if (ObjectUtils.isNotNull(poi.getItemAuxiliaryPartIdentifier())) {
                    cxml.append("        <AuxiliaryCatalogNumber><![CDATA[").append(poi.getItemAuxiliaryPartIdentifier()).append("]]></AuxiliaryCatalogNumber>\n");
                }
                cxml.append("        <Description><![CDATA[").append(poi.getItemDescription()).append("]]></Description>\n"); // Required.
                cxml.append("        <ProductUnitOfMeasure type=\"supplier\"><Measurement><MeasurementValue><![CDATA[").append(uom).append("]]></MeasurementValue></Measurement></ProductUnitOfMeasure>\n");
                cxml.append("        <ProductUnitOfMeasure type=\"system\"><Measurement><MeasurementValue><![CDATA[").append(uom).append("]]></MeasurementValue></Measurement></ProductUnitOfMeasure>\n");
                // ProductReferenceNumber - Unique id for hosted products in SelectSite
                if (poi.getExternalOrganizationB2bProductTypeName().equals("Punchout")) {
                    cxml.append("        <ProductReferenceNumber>null</ProductReferenceNumber>\n");
                }
                else {
                    cxml.append("        <ProductReferenceNumber>").append(poi.getExternalOrganizationB2bProductReferenceNumber()).append("</ProductReferenceNumber>\n");
                }
                // ProductType - Describes the type of the product or service. Valid values: Catalog, Form, Punchout. Mandatory.
                if (PurapConstants.RequisitionSources.B2B.equals(purchaseOrder.getRequisitionSourceCode())) {
                	cxml.append("        <ProductType>").append(poi.getExternalOrganizationB2bProductTypeName()).append("</ProductType>\n");
                } else {
                	cxml.append("        <ProductType>").append("Form").append("</ProductType>\n");
                }
                cxml.append("      </Item>\n");
                cxml.append("      <Quantity>").append(quantity).append("</Quantity>\n");
                // LineCharges - All the monetary charges for this line, including the price, tax, shipping, and handling.
                // Required.
                cxml.append("      <LineCharges>\n");
                cxml.append("        <UnitPrice>\n");
                cxml.append("          <Money currency=\"USD\">").append(poi.getItemUnitPrice()).append("</Money>\n");
                cxml.append("        </UnitPrice>\n");
                cxml.append("      </LineCharges>\n");
                cxml.append("    </POLine>\n");
            }
        }

        cxml.append("  </PurchaseOrder>\n");
        cxml.append("</PurchaseOrderMessage>\r\n");
        
        LOG.debug("getCxml(): cXML for po number " + purchaseOrder.getPurapDocumentIdentifier() + ":\n" + cxml.toString());
        
        /*KFSPTS-794: Start new code: Add each attachment as raw binary data. */
        //*****************************************************************************************************************
        //* This is where the attachment gets put into the xml as raw binary data                                         *
        //*****************************************************************************************************************
        if (!notesToSendToVendor.isEmpty()) {
        	//           cxml.append("\r\n");
        	for (int i = 0; i < notesToSendToVendor.size(); i++) {
        		Note note = notesToSendToVendor.get(i);
        		try {
        			Attachment poAttachment = attachmentService.getAttachmentByNoteId(note.getNoteIdentifier());
        			if (ObjectUtils.isNotNull(poAttachment)) {
        				//        cxml.append("\r\n"); // blank line.  This extra blank line cause first word doc has format issue
        				cxml.append("--" + CUPurapConstants.MIME_BOUNDARY_FOR_ATTACHMENTS + "\r\n");
        				cxml.append("Content-Type: application/octet-stream\r\n");
        				//   cxml.append("Content-Type: application/pdf\r\n");
        				cxml.append("Content-Transfer-Encoding: binary\r\n");
        				cxml.append("Content-ID: <" + poAttachment.getAttachmentIdentifier() + "@sciquest.com>\r\n");
        				cxml.append("Content-Disposition: attachment; filename=\"" + poAttachment.getAttachmentFileName() + "\"" + "\r\n\r\n");
        				
        				InputStream attInputStream = poAttachment.getAttachmentContents();
        				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        				int c;
        				while ((c = attInputStream.read()) != -1) buffer.write(c);
        				
        				String binaryStream = new String(buffer.toByteArray(),"ISO-8859-1");
        				
        				cxml.append(binaryStream + "\r\n");
        				}
        			} catch (IOException e) {
        				e.printStackTrace();
        				}
        		}
        	cxml.append("--" + CUPurapConstants.MIME_BOUNDARY_FOR_ATTACHMENTS + "--\r\n");  // signals this is the last MIME boundary
        	} else {
        		//            cxml.append("\n\n--" + CUPurapConstants.MIME_BOUNDARY_FOR_ATTACHMENTS + "--\n");
        		}       
        /*KFSPTS-794: Stop new code: Add each attachment as raw binary data. */       

        return cxml.toString();
    }

    @Override
    public String verifyCxmlPOData(PurchaseOrderDocument purchaseOrder, String requisitionInitiatorPrincipalId, String password, ContractManager contractManager, String contractManagerEmail, String vendorDuns) {
        StringBuffer errors = new StringBuffer();

        if (ObjectUtils.isNull(purchaseOrder)) {
            LOG.error("verifyCxmlPOData()  The Purchase Order is null.");
            errors.append("Error occurred retrieving Purchase Order\n");
            return errors.toString();
        }
        if (ObjectUtils.isNull(contractManager)) {
            LOG.error("verifyCxmlPOData()  The contractManager is null.");
            errors.append("Error occurred retrieving Contract Manager\n");
            return errors.toString();
        }
        if (StringUtils.isEmpty(password)) {
            LOG.error("verifyCxmlPOData()  The B2B PO password is required for the cXML PO but is missing.");
            errors.append("Missing Data: B2B PO password\n");
        }
        if (ObjectUtils.isNull(purchaseOrder.getPurapDocumentIdentifier())) {
            LOG.error("verifyCxmlPOData()  The purchase order Id is required for the cXML PO but is missing.");
            errors.append("Missing Data: Purchase Order ID\n");
        }
        if (StringUtils.isEmpty(requisitionInitiatorPrincipalId)) {
            LOG.error("verifyCxmlPOData()  The requisition initiator principal name is required for the cXML PO but is missing.");
            errors.append("Missing Data: Requisition Initiator Principal Name\n");
        }
        if (ObjectUtils.isNull(purchaseOrder.getPurchaseOrderCreateTimestamp())) {
            LOG.error("verifyCxmlPOData()  The PO create date is required for the cXML PO but is null.");
            errors.append("Create Date\n");
        }
        if (StringUtils.isEmpty(contractManager.getContractManagerPhoneNumber())) {
            LOG.error("verifyCxmlPOData()  The contract manager phone number is required for the cXML PO but is missing.");
            errors.append("Missing Data: Contract Manager Phone Number\n");
        }
        if (StringUtils.isEmpty(contractManager.getContractManagerName())) {
            LOG.error("verifyCxmlPOData()  The contract manager name is required for the cXML PO but is missing.");
            errors.append("Missing Data: Contract Manager Name\n");
        }
        if (StringUtils.isEmpty(purchaseOrder.getDeliveryCampusCode())) {
            LOG.error("verifyCxmlPOData()  The Delivery Campus Code is required for the cXML PO but is missing.");
            errors.append("Missing Data: Delivery Campus Code\n");
        }
        if (StringUtils.isEmpty(purchaseOrder.getBillingName())) {
            LOG.error("verifyCxmlPOData()  The Delivery Billing Name is required for the cXML PO but is missing.");
            errors.append("Missing Data: Delivery Billing Name\n");
        }
        if (StringUtils.isEmpty(purchaseOrder.getBillingLine1Address())) {
            LOG.error("verifyCxmlPOData()  The Billing Line 1 Address is required for the cXML PO but is missing.");
            errors.append("Missing Data: Billing Line 1 Address\n");
        }
        if (StringUtils.isEmpty(purchaseOrder.getBillingLine2Address())) {
            LOG.error("verifyCxmlPOData()  The Billing Line 2 Address is required for the cXML PO but is missing.");
            errors.append("Missing Data: Billing Line 2 Address\n");
        }
        if (StringUtils.isEmpty(purchaseOrder.getBillingCityName())) {
            LOG.error("verifyCxmlPOData()  The Billing Address City Name is required for the cXML PO but is missing.");
            errors.append("Missing Data: Billing Address City Name\n");
        }
        if (StringUtils.isEmpty(purchaseOrder.getBillingStateCode())) {
            LOG.error("verifyCxmlPOData()  The Billing Address State Code is required for the cXML PO but is missing.");
            errors.append("Missing Data: Billing Address State Code\n");
        }
        if (StringUtils.isEmpty(purchaseOrder.getBillingPostalCode())) {
            LOG.error("verifyCxmlPOData()  The Billing Address Postal Code is required for the cXML PO but is missing.");
            errors.append("Missing Data: Billing Address Postal Code\n");
        }
        if (StringUtils.isEmpty(purchaseOrder.getDeliveryToName())) {
            LOG.error("verifyCxmlPOData()  The Delivery To Name is required for the cXML PO but is missing.");
            errors.append("Missing Data: Delivery To Name\n");
        }
        if (StringUtils.isEmpty(contractManagerEmail)) {
            LOG.error("verifyCxmlPOData()  The Contract Manager Email is required for the cXML PO but is missing.");
            errors.append("Missing Data: Contract Manager Email\n");
        }
        if (StringUtils.isEmpty(purchaseOrder.getDeliveryToEmailAddress())) {
            LOG.error("verifyCxmlPOData()  The Requesting Person Email Address is required for the cXML PO but is missing.");
            errors.append("Missing Data: Requesting Person Email Address\n");
        }
        if (StringUtils.isEmpty(purchaseOrder.getDeliveryToPhoneNumber())) {
            LOG.error("verifyCxmlPOData()  The Requesting Person Phone Number is required for the cXML PO but is missing.");
            errors.append("Missing Data: Requesting Person Phone Number\n");
        }
        if (StringUtils.isEmpty(purchaseOrder.getDeliveryBuildingLine1Address())) {
            LOG.error("verifyCxmlPOData()  The Delivery Line 1 Address is required for the cXML PO but is missing.");
            errors.append("Missing Data: Delivery Line 1 Address\n");
        }
        if (StringUtils.isEmpty(purchaseOrder.getDeliveryToName())) {
            LOG.error("verifyCxmlPOData()  The Delivery To Name is required for the cXML PO but is missing.");
            errors.append("Missing Data: Delivery To Name\n");
        }
        if (StringUtils.isEmpty(purchaseOrder.getDeliveryCityName())) {
            LOG.error("verifyCxmlPOData()  The Delivery City Name is required for the cXML PO but is missing.");
            errors.append("Missing Data: Delivery City Name\n");
        }
        
		// KFSUPGRADE-405 KFSPTS-1796
		if (StringUtils.equals(KFSConstants.COUNTRY_CODE_UNITED_STATES, purchaseOrder.getDeliveryCountryCode())) {
			if (StringUtils.isEmpty(purchaseOrder.getDeliveryStateCode())) {
				LOG.error("verifyCxmlPOData()  The Delivery State is required for the cXML PO but is missing.");
				errors.append("Missing Data: Delivery State\n");
			}

			if (StringUtils.isEmpty(purchaseOrder.getDeliveryPostalCode())) {
				LOG.error("verifyCxmlPOData()  The Delivery Postal Code is required for the cXML PO but is missing.");
				errors.append("Missing Data: Delivery Postal Code\n");
			}
		}

        // verify item data
        List detailList = purchaseOrder.getItems();
        for (Iterator iter = detailList.iterator(); iter.hasNext();) {
            PurchaseOrderItem poi = (PurchaseOrderItem) iter.next();
            // CU enhancement for non-qty to exclude from qty/uom/catalog# check
        	boolean nonQuantityOrder = PurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE.equals(poi.getItemTypeCode());
            if (ObjectUtils.isNotNull(poi.getItemType()) && poi.getItemType().isLineItemIndicator()) {
                if (ObjectUtils.isNull(poi.getItemLineNumber())) {
                    LOG.error("verifyCxmlPOData()  The Item Line Number is required for the cXML PO but is missing.");
                    errors.append("Missing Data: Item Line Number\n");
                }
                // CU : added B2B & nonqty Check
                if (!nonQuantityOrder && StringUtils.isEmpty(poi.getItemCatalogNumber())  && PurapConstants.RequisitionSources.B2B.equals(purchaseOrder.getRequisitionSourceCode())) {
                    LOG.error("verifyCxmlPOData()  The Catalog Number for item number " + poi.getItemLineNumber() + " is required for the cXML PO but is missing.");
                    errors.append("Missing Data: Item#" + poi.getItemLineNumber() + " - Catalog Number\n");
                }
                if (StringUtils.isEmpty(poi.getItemDescription())) {
                    LOG.error("verifyCxmlPOData()  The Description for item number " + poi.getItemLineNumber() + " is required for the cXML PO but is missing.");
                    errors.append("Missing Data: Item#" + poi.getItemLineNumber() + " - Description\n");
                }
                // CU : added nonqtyorder check
                if (!nonQuantityOrder && StringUtils.isEmpty(poi.getItemUnitOfMeasureCode())) {
                    LOG.error("verifyCxmlPOData()  The Unit Of Measure Code for item number " + poi.getItemLineNumber() + " is required for the cXML PO but is missing.");
                    errors.append("Missing Data: Item#" + poi.getItemLineNumber() + " - Unit Of Measure\n");
                }
                // CU : added B2B Check
                if (StringUtils.isEmpty(poi.getExternalOrganizationB2bProductTypeName()) && PurapConstants.RequisitionSources.B2B.equals(purchaseOrder.getRequisitionSourceCode())) {
                    LOG.error("verifyCxmlPOData()  The External Org B2B Product Type Name for item number " + poi.getItemLineNumber() + " is required for the cXML PO but is missing.");
                    errors.append("Missing Data: Item#" + poi.getItemLineNumber() + " - External Org B2B Product Type Name\n");
                }
                // CU : added nonqtyorder check
                if (!nonQuantityOrder && poi.getItemQuantity() == null) {
                    LOG.error("verifyCxmlPOData()  The Order Quantity for item number " + poi.getItemLineNumber() + " is required for the cXML PO but is missing.");
                    errors.append("Missing Data: Item#" + poi.getItemLineNumber() + " - Order Quantity\n");
                }
                if (poi.getItemUnitPrice() == null) {
                    LOG.error("verifyCxmlPOData()  The Unit Price for item number " + poi.getItemLineNumber() + " is required for the cXML PO but is missing.");
                    errors.append("Missing Data: Item#" + poi.getItemLineNumber() + " - Unit Price\n");
                }
            }
        } // end item looping

        return errors.toString();
    }

    /**
     * Sets fields on the purchase order document that the are expected for the xml but might not be populated
     * on non B2B documents (to prevent NPEs)
     * CU change
     * @param purchaseOrder document instance to prepare
     */
    protected void prepareNonB2BPurchaseOrderForTransmission(PurchaseOrderDocument purchaseOrder) {
        List detailList = purchaseOrder.getItems();
        for (Iterator iter = detailList.iterator(); iter.hasNext();) {
            PurchaseOrderItem poi = (PurchaseOrderItem) iter.next();
            
            if (poi.getItemCatalogNumber() == null) {
            	poi.setItemCatalogNumber("");
            }
            if (poi.getExternalOrganizationB2bProductTypeName() == null) {
            	poi.setExternalOrganizationB2bProductTypeName("");
            }
            if (poi.getExternalOrganizationB2bProductReferenceNumber() == null) {
            	poi.setExternalOrganizationB2bProductReferenceNumber("");
            }
            if (poi.getExternalOrganizationB2bProductTypeName() == null) {
            	poi.setExternalOrganizationB2bProductTypeName("");
            }
        }    
    }

    protected String getContractManagerEmail(ContractManager cm) {
    	// KFSUPGRADE-509 : CU apo contract manager for non-b2b order
    	// FIXME : if discontinue this fix, then contractmanagermail will be empty, and this will cause verifypocxml to have error,
    	// hence cxml will not be sent to SQ.  this is mostly for non-b2b order.
    	if(cm.getContractManagerCode().equals(PurapConstants.APO_CONTRACT_MANAGER)) {
    		return parameterService.getParameterValueAsString(PurchaseOrderDocument.class, CUPurapParameterConstants.APO_CONTRACT_MANAGER_EMAIL);
    	} 

        return super.getContractManagerEmail(cm);
    }

    /**
     * KFSPTS-1458
     * Retrieve the email address to use for the method of PO transmission MANL = US Mail.
     * A hard coded email address had been where this method is called in this class.
     */
    protected String getMethodOfPOTransmissionConversionEmail() {
    	String emailAddressToUse = null;
    	
    	try
    	{	emailAddressToUse = parameterService.getParameterValueAsString(PurchaseOrderDocument.class, CUPurapParameterConstants.MANUAL_DISTRIBUTION_EMAIL);
    		if ( (emailAddressToUse == null) || emailAddressToUse.length() == 0) {
    			//parameter value is not defined, return email address defined as a fail-safe constant when this condition occurs
    			emailAddressToUse = CUPurapConstants.MANUAL_DISRIBUTION_FAILSAFE_EMAIL_ADDRESS;
    		}            
    	}
    	catch (IllegalArgumentException e) {
    		//parameter is not defined, return email address defined as a failsafe constant when this condition occurs
    		emailAddressToUse = CUPurapConstants.MANUAL_DISRIBUTION_FAILSAFE_EMAIL_ADDRESS;
    	}
    	
    	return emailAddressToUse;
    }
    
    /**
     * KFSPTS-794: This code change was taken from an enhancement provided to Cornell from CSU.
     * 
     * Returns list of Note(s) that should be sent to the vendor
     */
    private List<Note> getNotesToSendToVendor(PurchaseOrderDocument purchaseOrder) {
        List<Note> notesToSend = new ArrayList<Note>(); // this may not work for POA because PO note is linked to oldest PO
        List<Note> boNotes = purchaseOrder.getNotes(); 
        if (CollectionUtils.isEmpty(boNotes)) {
        	boNotes = purchaseOrderService.getPurchaseOrderNotes(purchaseOrder.getPurapDocumentIdentifier());        	
        }

        for (Note note : boNotes) {
            if (StringUtils.equalsIgnoreCase(note.getNoteTopicText(), CUPurapConstants.AttachemntToVendorIndicators.SEND_TO_VENDOR)) {
                notesToSend.add(note);
            }
        }
        return notesToSend;
    }
    
    /*
     * helper method for repeated code
     */
    
    private String addCustomFieldValueSet(String name, String label, String value) {
       
       StringBuffer customField = new StringBuffer();
 
       customField.append("      <CustomFieldValueSet label=\"").append(label).append("\" name=\"").append(name).append("\">\n");
       customField.append("        <CustomFieldValue>\n");
       customField.append("          <Value><![CDATA[").append(value).append("]]></Value>\n");
//      customField.append("          <Description />\n");  // is this really needed
       customField.append("         </CustomFieldValue>\n");
       customField.append("      </CustomFieldValueSet>\n");
       return customField.toString();
 
   }
    
   private String getVendorShipTitle(PurchaseOrderDocument purchaseOrder) {
       if (purchaseOrder.getVendorShippingTitle() == null) {
           purchaseOrder.refreshReferenceObject("vendorShippingTitle");
       }
       if (purchaseOrder.getVendorShippingTitle() == null) {
           return KFSConstants.EMPTY_STRING;
       } else {
           return purchaseOrder.getVendorShippingTitle().getVendorShippingTitleDescription();
       }
   }
   
    /*
     * Do null check just incase.
     */
    private String getVendorShipPayTerms(PurchaseOrderDocument purchaseOrder) {
    	if (purchaseOrder.getVendorShippingPaymentTerms() == null) {
    		purchaseOrder.refreshReferenceObject("vendorShippingPaymentTerms");
    	}
    	if (purchaseOrder.getVendorShippingPaymentTerms() == null) {
    		return KFSConstants.EMPTY_STRING;
    	} else {
    		return purchaseOrder.getVendorShippingPaymentTerms().getVendorShippingPaymentTermsDescription();
    	}
    	
    }
   
    private String getVendorCountry(PurchaseOrderDocument purchaseOrder) {
    	// Don't need refresh vendorcountry because it is getting by service
    	if (purchaseOrder.getVendorShippingPaymentTerms() == null) {
    		purchaseOrder.refreshReferenceObject("vendorShippingPaymentTerms");
    	}
    	if (purchaseOrder.getVendorCountry() == null) {
    		return KFSConstants.EMPTY_STRING;
    	} else {
    		return purchaseOrder.getVendorCountry().getName();
    	}

    }


    public void setPurchaseOrderService(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }


    public void setVendorService(VendorService vendorService) {
        this.vendorService = vendorService;
    }


    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }


    public void setAttachmentService(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

}