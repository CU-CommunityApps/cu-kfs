/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.pdp.batch.service.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.batch.service.ExtractPaymentService;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentGroupHistory;
import org.kuali.kfs.pdp.businessobject.PaymentNoteText;
import org.kuali.kfs.pdp.businessobject.PaymentProcess;
import org.kuali.kfs.pdp.businessobject.PaymentStatus;
import org.kuali.kfs.pdp.dataaccess.PaymentGroupHistoryDao;
import org.kuali.kfs.pdp.dataaccess.ProcessDao;
import org.kuali.kfs.pdp.service.PaymentDetailService;
import org.kuali.kfs.pdp.service.PaymentGroupService;
import org.kuali.kfs.pdp.service.PdpEmailService;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.rice.kns.bo.Country;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.CountryService;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.KualiConfigurationService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ExtractPaymentServiceImpl implements ExtractPaymentService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ExtractPaymentServiceImpl.class);

    private String directoryName;

    private DateTimeService dateTimeService;
    private ParameterService parameterService;
    private PaymentGroupService paymentGroupService;
    private PaymentDetailService paymentDetailService;
    private PaymentGroupHistoryDao paymentGroupHistoryDao;
    private ProcessDao processDao;
    private PdpEmailService paymentFileEmailService;
    private BusinessObjectService businessObjectService;
    private KualiConfigurationService kualiConfigurationService;
    private CountryService countryService;

    // Set this to true to run this process without updating the database. This
    // should stay false for production.
    public static boolean testMode = false;

    protected String getOutputFile(String fileprefix, Date runDate) {
        String filename = directoryName + "/" + fileprefix + "_";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        filename = filename + sdf.format(runDate);
        filename = filename + ".xml";

        return filename;
    }

    /**
     * @see org.kuali.kfs.pdp.batch.service.ExtractPaymentService#extractCancelledChecks()
     */
    public void extractCanceledChecks() {
        LOG.debug("extractCancelledChecks() started");

        Date processDate = dateTimeService.getCurrentDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        String checkCancelledFilePrefix = this.kualiConfigurationService.getPropertyString(PdpKeyConstants.ExtractPayment.CHECK_CANCEL_FILENAME);
        checkCancelledFilePrefix = MessageFormat.format(checkCancelledFilePrefix, new Object[] { null });

        String filename = getOutputFile(checkCancelledFilePrefix, processDate);
        LOG.debug("extractCanceledChecks() filename = " + filename);

        // Open file
        BufferedWriter os = null;

        try {
            os = new BufferedWriter(new FileWriter(filename));
            os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writeOpenTag(os, 0, "canceledChecks");

            Iterator paymentIterator = paymentGroupHistoryDao.getCanceledChecks();
            while (paymentIterator.hasNext()) {
                PaymentGroupHistory history = (PaymentGroupHistory) paymentIterator.next();

                writeOpenTag(os, 2, "check");

                writeBank(os, 4, history.getPaymentGroup().getBank());
                writePayee(os, 4, history.getPaymentGroup());

                writeTag(os, 4, "netAmount", history.getPaymentGroup().getNetPaymentAmount().toString());
                if (ObjectUtils.isNotNull(history.getOrigDisburseNbr())) {
                    writeTag(os, 4, "disbursementNumber", history.getOrigDisburseNbr().toString());
                }
                else {
                    writeTag(os, 4, "disbursementNumber", history.getPaymentGroup().getDisbursementNbr().toString());
                }
                if (ObjectUtils.isNotNull(history.getPaymentGroup().getDisbursementType())) {
                    writeTag(os, 4, "disbursementType", history.getPaymentGroup().getDisbursementType().getCode());
                }
                else {
                    writeTag(os, 4, "disbursementType", history.getDisbursementType().getCode());
                }

                writeCloseTag(os, 2, "check");

                if (!testMode) {
                    history.setLastUpdate(new Timestamp(processDate.getTime()));
                    history.setPmtCancelExtractDate(new Timestamp(processDate.getTime()));
                    history.setPmtCancelExtractStat(Boolean.TRUE);
                    history.setChangeTime(new Timestamp(new Date().getTime()));

                    this.businessObjectService.save(history);
                }
            }

            writeCloseTag(os, 0, "canceledChecks");
        }
        catch (IOException ie) {
            LOG.error("extractCanceledChecks() Problem reading file:  " + filename, ie);
            throw new IllegalArgumentException("Error writing to output file: " + ie.getMessage());
        }
        finally {
            // Close file
            if (os != null) {
                try {
                    os.close();
                }
                catch (IOException ie) {
                    // Not much we can do now
                }
            }
        }
    }

    /**
     * @see org.kuali.kfs.pdp.batch.service.ExtractPaymentService#extractAchPayments()
     */
    public void extractAchPayments() {
        LOG.debug("extractAchPayments() started");

        Date processDate = dateTimeService.getCurrentDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        PaymentStatus extractedStatus = (PaymentStatus) this.businessObjectService.findBySinglePrimaryKey(PaymentStatus.class, PdpConstants.PaymentStatusCodes.EXTRACTED);

        String achFilePrefix = this.kualiConfigurationService.getPropertyString(PdpKeyConstants.ExtractPayment.ACH_FILENAME);
        achFilePrefix = MessageFormat.format(achFilePrefix, new Object[] { null });

        String filename = getOutputFile(achFilePrefix, processDate);
        LOG.debug("extractAchPayments() filename = " + filename);

        // Open file
        BufferedWriter os = null;

        writeExtractAchFile(extractedStatus, filename, processDate, sdf);

    }

    /**
     * @see org.kuali.kfs.pdp.batch.service.ExtractPaymentService#extractChecks()
     */
    public void extractChecks() {
        LOG.debug("extractChecks() started");

        Date processDate = dateTimeService.getCurrentDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        PaymentStatus extractedStatus = (PaymentStatus) this.businessObjectService.findBySinglePrimaryKey(PaymentStatus.class, PdpConstants.PaymentStatusCodes.EXTRACTED);

        String checkFilePrefix = this.kualiConfigurationService.getPropertyString(PdpKeyConstants.ExtractPayment.CHECK_FILENAME);
        checkFilePrefix = MessageFormat.format(checkFilePrefix, new Object[] { null });

        String filename = getOutputFile(checkFilePrefix, processDate);
        LOG.debug("extractChecks() filename: " + filename);

        List<PaymentProcess> extractsToRun = this.processDao.getAllExtractsToRun();
        for (PaymentProcess extractToRun : extractsToRun) {
            writeExtractCheckFile(extractedStatus, extractToRun, filename, extractToRun.getId().intValue());
            this.processDao.setExtractProcessAsComplete(extractToRun);
        }
    }

    protected void writeExtractCheckFile(PaymentStatus extractedStatus, PaymentProcess p, String filename, Integer processId) {

    	// Write out the Mellon Fast Track formatted file for checks that are to be printed by Mellon and/or 
        //   generate the issuance file for checks that will be printed locally.  We need to execute this first
    	//   since the writeExtractCheckFile methods sets the extract status so that the writeExtractCheckFileMellonBankFastTrack
    	//   method doesn't find anything to process!
    	writeExtractCheckFileMellonBankFastTrack(extractedStatus, p, filename, processId);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date processDate = dateTimeService.getCurrentDate();
        BufferedWriter os = null;

        try {
            os = new BufferedWriter(new FileWriter(filename));
            os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writeOpenTagAttribute(os, 0, "checks", "processId", processId.toString(), "campusCode", p.getCampusCode());

            List<String> bankCodes = paymentGroupService.getDistinctBankCodesForProcessAndType(processId, PdpConstants.DisbursementTypeCodes.CHECK);

            for (String bankCode : bankCodes) {
                List<Integer> disbNbrs = paymentGroupService.getDisbursementNumbersByDisbursementTypeAndBankCode(processId, PdpConstants.DisbursementTypeCodes.CHECK, bankCode);
                for (Iterator<Integer> iter = disbNbrs.iterator(); iter.hasNext();) {
                    Integer disbursementNbr = iter.next();

                    boolean first = true;

                    KualiDecimal totalNetAmount = new KualiDecimal(0);

                    // this seems wasteful, but since the total net amount is needed on the first payment detail...it's needed
                    Iterator<PaymentDetail> i2 = paymentDetailService.getByDisbursementNumber(disbursementNbr, processId, PdpConstants.DisbursementTypeCodes.CHECK, bankCode);
                    while (i2.hasNext()) {
                        PaymentDetail pd = i2.next();
                        totalNetAmount = totalNetAmount.add(pd.getNetPaymentAmount());
                    }

                    Iterator<PaymentDetail> paymentDetails = paymentDetailService.getByDisbursementNumber(disbursementNbr, processId, PdpConstants.DisbursementTypeCodes.CHECK, bankCode);
                    while (paymentDetails.hasNext()) {
                        PaymentDetail pd = paymentDetails.next();
                        PaymentGroup pg = pd.getPaymentGroup();
                        if (!testMode) {
                            pg.setDisbursementDate(new java.sql.Date(processDate.getTime()));
                            pg.setPaymentStatus(extractedStatus);
                            this.businessObjectService.save(pg);
                        }

                        if (first) {
                            writeOpenTagAttribute(os, 2, "check", "disbursementNbr", pg.getDisbursementNbr().toString());

                            // Write check level information

                            writeBank(os, 4, pg.getBank());

                            writeTag(os, 4, "disbursementDate", sdf.format(processDate));
                            writeTag(os, 4, "netAmount", totalNetAmount.toString());

                            writePayee(os, 4, pg);
                            writeTag(os, 4, "campusAddressIndicator", pg.getCampusAddress().booleanValue() ? "Y" : "N");
                            writeTag(os, 4, "attachmentIndicator", pg.getPymtAttachment().booleanValue() ? "Y" : "N");
                            writeTag(os, 4, "specialHandlingIndicator", pg.getPymtSpecialHandling().booleanValue() ? "Y" : "N");
                            writeTag(os, 4, "immediatePaymentIndicator", pg.getProcessImmediate().booleanValue() ? "Y" : "N");
                            writeTag(os, 4, "customerUnivNbr", pg.getCustomerInstitutionNumber());
                            writeTag(os, 4, "paymentDate", sdf.format(pg.getPaymentDate()));

                            // Write customer profile information
                            CustomerProfile cp = pg.getBatch().getCustomerProfile();
                            writeCustomerProfile(os, 4, cp);

                            writeOpenTag(os, 4, "payments");

                        }

                        writeOpenTag(os, 6, "payment");

                        writeTag(os, 8, "purchaseOrderNbr", pd.getPurchaseOrderNbr());
                        writeTag(os, 8, "invoiceNbr", pd.getInvoiceNbr());
                        writeTag(os, 8, "requisitionNbr", pd.getRequisitionNbr());
                        writeTag(os, 8, "custPaymentDocNbr", pd.getCustPaymentDocNbr());
                        writeTag(os, 8, "invoiceDate", sdf.format(pd.getInvoiceDate()));

                        writeTag(os, 8, "origInvoiceAmount", pd.getOrigInvoiceAmount().toString());
                        writeTag(os, 8, "netPaymentAmount", pd.getNetPaymentAmount().toString());
                        writeTag(os, 8, "invTotDiscountAmount", pd.getInvTotDiscountAmount().toString());
                        writeTag(os, 8, "invTotShipAmount", pd.getInvTotShipAmount().toString());
                        writeTag(os, 8, "invTotOtherDebitAmount", pd.getInvTotOtherDebitAmount().toString());
                        writeTag(os, 8, "invTotOtherCreditAmount", pd.getInvTotOtherCreditAmount().toString());

                        writeOpenTag(os, 8, "notes");
                        for (Iterator ix = pd.getNotes().iterator(); ix.hasNext();) {
                            PaymentNoteText note = (PaymentNoteText) ix.next();
                            writeTag(os, 10, "note", note.getCustomerNoteText());
                        }
                        writeCloseTag(os, 8, "notes");

                        writeCloseTag(os, 6, "payment");

                        first = false;
                    }
                    writeCloseTag(os, 4, "payments");
                    writeCloseTag(os, 2, "check");
                }
            }
            writeCloseTag(os, 0, "checks");
        }
        catch (IOException ie) {
            LOG.error("extractChecks() Problem reading file:  " + filename, ie);
            throw new IllegalArgumentException("Error writing to output file: " + ie.getMessage());
        }
        finally {
            // Close file
            if (os != null) {
                try {
                    os.close();
                }
                catch (IOException ie) {
                    // Not much we can do now
                }
            }
        }
    }

    // This method is called by the method that generates the XML file for checks to be printed by Mellon
    protected void writeExtractCheckFileMellonBankFastTrack(PaymentStatus extractedStatus, PaymentProcess p, String filename, Integer processId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss"); //Used in the Fast Track file
        SimpleDateFormat sdfIDate = new SimpleDateFormat("yyMMdd");    //Used in the issuance file
        SimpleDateFormat sdfITime = new SimpleDateFormat("HHmmss");    //Used in the issuance file
        Date processDate = dateTimeService.getCurrentDate();
        BufferedWriter os = null;
        BufferedWriter osI = null;
        String cDelim = "^";  //column delimiter: Per Mellon FastTrack spec, your choices are: "^" or ",".  If you change this make sure you change the associated name on the next line!
        String cDname = "FFCARET";  // column delimiter name: Per Mellon FastTrack spec, your choices are: FFCARET and FFCOMMA for variable record types
        String hdrRecType = "V";  // record type: Per Mellon's FastTrack spec, can be either V for variable or F for fixed.
        String testIndicator = "P";
        String ourBankAccountNumber = "";
        String ourBankRoutingNumber = "";
        String subUnitCode = "";
        String divisionCode = "";
        boolean specialHandlingCode = false;
        boolean attachmentCode = false;
        boolean immediateCheckCode = false;
        String PreparerInfoText = "";
        String altAddrSendTo = "";
        String altAddrAddr1 = "";
        String altAddrAddr2 = "";
        String altAddrCity = "";
        String altAddrState = "";
        String altAddrZip = "";
        String altAddrCityStateZip = "";  // this is needed because the notes combine these into one field by the time we get it
        int NumOfAltAddressLines = 0;
        int SendToPrefLength = 0;
        String RefDesc1 = "";	// Note lines that are not the alternate address
        String RefDesc2 = "";   // Note lines that are not the alternate address
        String RefDesc3 = "";   // Note lines that are not the alternate address
        String RefDesc4 = "";   // Note lines that are not the alternate address
        String FirstNoteAfterAddressInfo = "";
        String SecondNoteAfterAddressInfo = "";
        String ThirdNoteAfterAddressInfo = "";
        String ftFilename = "";  // Filename for the Fast Track file generated by this method
        String arFilename = "";  // Filename for the issuance (or account reconciliation) file generated by this method for immediate payments
        int arNumOfAddIssues = 0;  //The total number of "add" issues (see record #6)
        KualiDecimal arTotalOfAddIssues = KualiDecimal.ZERO; // The total dollar amount of the add issues for the issuance file.
        boolean wroteMellonIssuanceHeaderRecords = false;
        boolean wroteMellonFastTrackHeaderRecords = false;
                
        // Change the filename so that it ends in .txt instead of .xml
        ftFilename = filename.replace(".xml", ".txt");
        arFilename = ftFilename.replace("check", "check_immediate_only");
        
        int totalRecordCount = 0;
        KualiDecimal totalPaymentAmounts = KualiDecimal.ZERO; 
        
        try {
            //  Obtain the bank account number from the check information provided
            List<String> bankCodes1 = paymentGroupService.getDistinctBankCodesForProcessAndType(processId, PdpConstants.DisbursementTypeCodes.CHECK);
            if (!bankCodes1.isEmpty()) {
	            List<Integer> disbNbrs1 = paymentGroupService.getDisbursementNumbersByDisbursementTypeAndBankCode(processId, PdpConstants.DisbursementTypeCodes.CHECK, bankCodes1.get(0));
	            if (!disbNbrs1.isEmpty()) {
		            Iterator<PaymentDetail> myPds = paymentDetailService.getByDisbursementNumber(disbNbrs1.get(0));
		            if (myPds.hasNext()) {
		            	PaymentDetail myPd = myPds.next();
		                PaymentGroup myPg = myPd.getPaymentGroup();
		                ourBankAccountNumber = myPg.getBank().getBankAccountNumber();
		                ourBankRoutingNumber = myPg.getBank().getBankRoutingNumber();
		            }
	            }
            }
                        
        	// At this point we will start looping through all the checks and write the PAY01000 record (one for each payee), 
        	// two (2) PDT2010 records (one for the Payer & Payee) and as many REM3020 records as needed for each amount being
        	// paid to this payee.
            List<String> bankCodes = paymentGroupService.getDistinctBankCodesForProcessAndType(processId, PdpConstants.DisbursementTypeCodes.CHECK);
            for (String bankCode : bankCodes) {
                List<Integer> disbNbrs = paymentGroupService.getDisbursementNumbersByDisbursementTypeAndBankCode(processId, PdpConstants.DisbursementTypeCodes.CHECK, bankCode);
                for (Iterator<Integer> iter = disbNbrs.iterator(); iter.hasNext();) {
                    Integer disbursementNbr = iter.next();

                    boolean first = true;	//If this payee has multiple checks coming to them, this ensures we only generate 1 PAY01000 record

                    KualiDecimal totalNetAmount = new KualiDecimal(0);

                    // We continue to need this for the FastTrack file as well since the total net amount is needed on the PAY01000 record for each payee
                    Iterator<PaymentDetail> i2 = paymentDetailService.getByDisbursementNumber(disbursementNbr, processId, PdpConstants.DisbursementTypeCodes.CHECK, bankCode);
                    while (i2.hasNext()) {
                        PaymentDetail pd = i2.next();
                        totalNetAmount = totalNetAmount.add(pd.getNetPaymentAmount());
                    }

                    Iterator<PaymentDetail> paymentDetails = paymentDetailService.getByDisbursementNumber(disbursementNbr, processId, PdpConstants.DisbursementTypeCodes.CHECK, bankCode);
                    while (paymentDetails.hasNext()) {
                        PaymentDetail pd = paymentDetails.next();
                        PaymentGroup pg = pd.getPaymentGroup();

                        //Get immediate (a.k.a. local print, a.k.a. manual) check indicator
                        immediateCheckCode = pg.getProcessImmediate();
                        
                        // Parse Notes
                        Iterator<PaymentNoteText> ix = pd.getNotes().iterator();
                        NumOfAltAddressLines = 0;
                        altAddrSendTo = "";
                        altAddrAddr1 = "";
                        altAddrAddr2 = "";
                        altAddrCity = "";
                        altAddrState = "";
                        altAddrZip = "";
                        altAddrCityStateZip = "";
                        SendToPrefLength = 0;
                        
                        while  (ix.hasNext()) {
                        	PaymentNoteText note = (PaymentNoteText) ix.next();
                        	String NoteLine = note.getCustomerNoteText();
                        	if ( NoteLine.contains(DisbursementVoucherConstants.DV_EXTRACT_NOTE_PREFIX_PREPARER) ) {
                        		SendToPrefLength = DisbursementVoucherConstants.DV_EXTRACT_NOTE_PREFIX_SPECIAL_HANDLING_NAME.length();
                        		PreparerInfoText = NoteLine;
                        	}
                        	else if ( NoteLine.contains(DisbursementVoucherConstants.DV_EXTRACT_NOTE_PREFIX_SPECIAL_HANDLING_NAME) ) {
                        		SendToPrefLength = DisbursementVoucherConstants.DV_EXTRACT_NOTE_PREFIX_SPECIAL_HANDLING_NAME.length();
                        		altAddrSendTo = NoteLine.substring(SendToPrefLength);
                        		NumOfAltAddressLines = NumOfAltAddressLines + 1;
                        	}
                        	else if ( NoteLine.contains(DisbursementVoucherConstants.DV_EXTRACT_NOTE_PREFIX_SPECIAL_HANDLING_ADDRESS1) ) {
                        		SendToPrefLength = DisbursementVoucherConstants.DV_EXTRACT_NOTE_PREFIX_SPECIAL_HANDLING_ADDRESS1.length();
                        		altAddrAddr1 = NoteLine.substring(SendToPrefLength);
                        		NumOfAltAddressLines = NumOfAltAddressLines + 1;
                        	}
                        	else if ( NoteLine.contains(DisbursementVoucherConstants.DV_EXTRACT_NOTE_PREFIX_SPECIAL_HANDLING_ADDRESS2) ) {
                        		SendToPrefLength = DisbursementVoucherConstants.DV_EXTRACT_NOTE_PREFIX_SPECIAL_HANDLING_ADDRESS2.length();
                        		altAddrAddr2 = NoteLine.substring(SendToPrefLength);
                        		NumOfAltAddressLines = NumOfAltAddressLines + 1;
                        	}
                        	else if ( NoteLine.contains(DisbursementVoucherConstants.DV_EXTRACT_NOTE_PREFIX_SPECIAL_HANDLING_ADDRESS3) ) {
                        		SendToPrefLength = DisbursementVoucherConstants.DV_EXTRACT_NOTE_PREFIX_SPECIAL_HANDLING_ADDRESS3.length();
                        		altAddrCityStateZip = NoteLine.substring(SendToPrefLength);
                        		String sTemp = altAddrCityStateZip;	//sTemp will be the string we modify as we go along
                        		altAddrZip = sTemp.substring(sTemp.lastIndexOf(" "));  //Use the space between the state and zip to obtain the zip
                        		sTemp = sTemp.replace(altAddrZip, "");  //Remove the space + Zip characters from sTemp
                        		altAddrZip = altAddrZip.trim();  // Trim any leading or trailing blanks
                        		altAddrState = sTemp.substring(sTemp.lastIndexOf(",") + 1); // Use the comma to find the where the state starts
                        		sTemp = sTemp.replace(altAddrState, "");  //Remove what we found from sTemp
                        		altAddrState = altAddrState.trim();  // Trim any leading or trailing blanks
                        		altAddrCity = sTemp.replace(",", "");  // There should only be one comma.  If commas are in the city name, that could be an issue
                        		altAddrCity = altAddrCity.trim();  //Trim any leading or trailing blanks.
                        		NumOfAltAddressLines = NumOfAltAddressLines + 1;
                        	}
                        	
                    		// Retrieve up to 3 non-mailing address note lines and only the first 72 characters as per the Mellon spec.
                    		// Retrieve the first
                        	else {
                        		FirstNoteAfterAddressInfo = (NoteLine.length() < 73) ? NoteLine : NoteLine.substring(0,72);
                        		if (ix.hasNext()) {
                        			//Retrieve the second
                            		note = (PaymentNoteText) ix.next();
                                	NoteLine = note.getCustomerNoteText();
                                	SecondNoteAfterAddressInfo = (NoteLine.length() < 73) ? NoteLine : NoteLine.substring(0,72);

                            		if (ix.hasNext()) {
                            			//Retrieve the second
                                		note = (PaymentNoteText) ix.next();
                                    	NoteLine = note.getCustomerNoteText();
                                    	ThirdNoteAfterAddressInfo = (NoteLine.length() < 73) ? NoteLine : NoteLine.substring(0,72);
                            		}
	                        		else
	                        			break;
                        		}
                        		else
                        			break;
                        	}
                        }                        
                        
                        if (first && !immediateCheckCode) {
                        	if (!wroteMellonFastTrackHeaderRecords) {
                        		//Open the file
                                os = new BufferedWriter(new FileWriter(ftFilename));

	                        	//Write the Fast Track header record (FIL00010)
	                            os.write("FIL00010" + cDelim +                // Record Type
	                            		hdrRecType + cDelim +                 // Variable (V) or Fixed (F) flag
	                            		cDname + cDelim +                     // Delimiter name - Must be either FFCARET or FFCOMMA.  Others are allowed but Mellon will have to be contacted first.
	                            		"CORNELLUNIVKFS" + cDelim +           // Customer Id - a unique identifier for the customer
	                            		testIndicator + cDelim +              // Test Indicator:  T = Test run, P = Production Run
	                            		"820" + cDelim +                      // EDI Document Id (3 Bytes)
	                            		ourBankAccountNumber + cDelim +       // Our bank account number (15 Bytes)
	                            		cDelim +                              // Customer Division Id - 35 bytes - Optional
	                            		sdf.format(processDate) + cDelim +    // File Date and Time - 14 Bytes
	                            		cDelim +                              // Reserved Field - 3 Bytes
	                                    "\n");                                // Filler - 872 bytes
	                        	
	                            //Write the Fast Track email records (FIL00020)
	                        	os.write("FIL00020" + cDelim + 
	                        			"uco_operations-mailbox@cornell.edu" + cDelim +
	                        			cDelim +
	                        			"\n");
	                        	os.write("FIL00020" + cDelim + 
	                        			"uc0-accts-pay-mailbox@cornell.edu" + 
	                        			cDelim + 
	                        			"\n");
	                        	os.write("FIL00020" + cDelim + 
	                        			"lcs38@cornell.edu" + cDelim +
	                        			cDelim + 
	                        			"\n");
	                        	os.write("FIL00020" + cDelim + 
	                        			"cms1@cornell.edu" + cDelim +
	                        			cDelim +
	                        			"\n");
	                        	
	                        	totalRecordCount = 5;  // We've successfully written the header and 4 email records.
	                        	
	                        	wroteMellonFastTrackHeaderRecords = true;
                        	}
                        	
                        	//Set up:
                            // Get country name for code
                            String sCountryName = "";
                            Country country = countryService.getByPrimaryId(pg.getCountry());
                            if (country != null) {
                            	sCountryName = country.getPostalCountryName();
                            }
                            else {
                            	sCountryName = pg.getCountry();
                            }
                            
                            // Get sub unit code
                            CustomerProfile cp = pg.getBatch().getCustomerProfile();
                            subUnitCode = cp.getSubUnitCode();
                            
                            //Get special handling indicator
                            specialHandlingCode = pg.getPymtSpecialHandling();
                             
                            //Get attachment indicator
                            attachmentCode = pg.getPymtAttachment();
                            
/*
                            Special  	 
                            Handing		Attachment		
                            Indicator	Indicator		DV/PURAP	LIBR	CSTR	STAT	CLIF
                            NO				NO			001			002		003		004		005
                            YES				NO			006			007		008		009		010
                            NO				YES			011			012		013		014		015
                            YES				YES			016			017		018		019		020
                            
*/
                            //Determines the division code based on sub unit code, special handling indicator and attachment indicator
                            int dvCodePart1=0;
                            if (subUnitCode.equals("DV")) dvCodePart1 = 1;
                            else if (subUnitCode.equals("PURAP")) dvCodePart1 = 1;
                            else if (subUnitCode.equals("LIBR")) dvCodePart1 = 2;
                            else if (subUnitCode.equals("CSTR")) dvCodePart1 = 3;
                            else if (subUnitCode.equals("STAT")) dvCodePart1 = 4;
                            else if (subUnitCode.equals("CLIF")) dvCodePart1 = 5;
                            // See above table for details
                            int dvCodePart2 = ((specialHandlingCode ? 1 : 0) + (attachmentCode ? 2 : 0)) * 5;
                            // String dvCodePart3 = Integer.toString(dvCodePart1 + dvCodePart2);
                            divisionCode = String.format(String.format("%%0%dd", 3), (dvCodePart1 + dvCodePart2));
                            
                            if (!immediateCheckCode) {
	                        	//Write the Fast Track PAY01000 record (only one per payee)
	                        	os.write("PAY01000" + cDelim +                              // Record Type - 8 bytes
	                        			"7" + cDelim +                                      // 7=Payment and Electronic Advice (Transaction handling code - 2 bytes)
	                        			totalNetAmount.toString() + cDelim +                // Total amount of check (Payment amount - 18 bytes)
	                        			"C" + cDelim +                                      // C=Credit, D=Debit (Credit or debit Flag - 1 Byte)
	                        			"CHK" + cDelim +                                    // CHK=Check (Payment method - 3 Bytes)
	                        			"PBC" + cDelim +                                    // PBC is used for checks (Payment Format - 10 bytes)
	                        			"01" + cDelim +                                     // Originators bank id qualifier - 2 bytes
	                        			ourBankRoutingNumber + cDelim +                     // Originators bank id - 12 bytes
	                        			"DA" + cDelim +                                     // Originating account number Qualifier - 3 bytes
	                        			ourBankAccountNumber + cDelim +                     // Originating account number - 35 bytes
	                        			"2150532082" + cDelim+                              // Originating company identifier - 10 bytes
	                        			cDelim +                                            // Receiving bank id qualifier - 2 bytes
	                        			cDelim +                                            // Receiving bank id - 12 bytes
	                        			cDelim +                                            // Receiving account number qualifier - 3 bytes
	                        			cDelim +                                            // Receiving account number - 35 bytes
	                        			sdf.format(processDate) + cDelim +                  // Effective date - 8 bytes
	                        			cDelim +                                            // Business function code - 3 bytes 
	                        			pg.getDisbursementNbr().toString() + cDelim +       // Trace number (check number) - 50 bytes
	                        			divisionCode + cDelim +                             // Division code - 50 bytes 
	                        			cDelim +                                            // Currency code - 3 bytes
	                        			cDelim +                                            // Note 1 - 80 bytes
	                        			cDelim +                                            // Note 2 - 80 bytes
	                        			cDelim +                                            // Note 3 - 80 bytes
	                        			cDelim +                                            // Note 4 - 80 bytes
	                        			cDelim +                                            // Filler
	                        			"\n");
	
	                        	totalPaymentAmounts = totalPaymentAmounts.add(totalNetAmount);
	                        	
	                            // Write the Fast Track Payer Detail(PDT02010) record (only one per payee)
	                            os.write("PDT02010" + cDelim +                        // Record Type - 8 bytes
	                            		"PR" + cDelim +                               // Name qualifier - 3 bytes (PR = Payer)
	                            		cDelim +                                      // ID code qualifier - 2 bytes
	                            		cDelim +                                      // ID code - 80 bytes
	                            		"Cornell University" + cDelim +               // Name - 60 bytes
	                            		cDelim +                                      // Additional name 1 - 60 bytes
	                            		cDelim +                                      // Additional name 2 - 60 bytes
	                            		"Division of Financial Affairs" + cDelim +    // Address line 1 - 55 bytes
	                            		"341 Pine Tree Road" + cDelim +               // Address line 2 - 55 bytes
	                            		cDelim +                                      // Address line 3 - 55 bytes
	                            		cDelim +                                      // Address line 4 - 55 bytes
	                            		cDelim +                                      // Address line 5 - 55 bytes
	                            		cDelim +                                      // Address line 6 - 55 bytes
	                            		"Ithaca" + cDelim +                           // City - 30 bytes
	                            		"NY" + cDelim +                               // State/Province - 2 bytes
	                            		"148502820" + cDelim +                        // Postal code - 15 bytes
	                            		cDelim +                                      // Country code - 3 bytes
	                            		cDelim +                                      // Country name - 30 bytes
	                            		cDelim +                                      // Ref qualifier 1 - 3 bytes
	                            		cDelim +                                      // Ref ID 1 - 50 bytes
	                            		cDelim +                                      // Ref description 1 - 80 bytes
	                            		cDelim +                                      // Ref qualifier 1 - 3 bytes
	                            		cDelim +                                      // Ref ID 1 - 50 bytes
	                            		cDelim +                                      // Ref description 1 - 80 bytes
	                            		cDelim +                                      // Filler - 46 bytes
	                            		"\n");
	                            
	                            // Write the Fast Track initial payee detail (PDT02010) record
	                            os.write("PDT02010" + cDelim +             // Record Type - 8 bytes
	                            		"PE" + cDelim +                    // Name qualifier - 3 bytes (PE = Payee) This record's data prints on the check
	                            		cDelim +                           // ID code qualifier - 2 bytes
	                            		cDelim +                           // ID code - 80 bytes
	                            		pg.getPayeeName() + cDelim +       // Name - 60 bytes
	                            		cDelim +                           // Additional name 1 - 60 bytes
	                            		cDelim +                           // Additional name 2 - 60 bytes
	                            		pg.getLine1Address() + cDelim +    // Address line 1 - 55 bytes
	                            		pg.getLine2Address() + cDelim +    // Address line 2 - 55 bytes
	                            		pg.getLine3Address() + cDelim +    // Address line 3 - 55 bytes
	                            		pg.getLine4Address() + cDelim +    // Address line 4 - 55 bytes
	                            		cDelim +                           // Address line 5 - 55 bytes
	                            		cDelim +                           // Address line 6 - 55 bytes
	                            		pg.getCity() + cDelim +            // City - 30 bytes
	                            		pg.getState() + cDelim +           // State/Province - 2 bytes
	                            		pg.getZipCd() + cDelim +           // Postal code - 15 bytes
	                            		cDelim +                           // Country code - 3 bytes
	                            		sCountryName + cDelim +            // Country name - 30 bytes
	                               		cDelim +                           // Ref qualifier 1 - 3 bytes
	                            		cDelim +                           // Ref ID 1 - 50 bytes
	                            		cDelim +                           // Ref description 1 - 80 bytes
	                            		cDelim +                           // Ref qualifier 1 - 3 bytes
	                            		cDelim +                           // Ref ID 1 - 50 bytes
	                            		cDelim +                           // Ref description 1 - 80 bytes
	                            		cDelim +                           // Filler - 46 bytes
	                             		"\n");
	                            
	                            // If no alternate address is provided, we must populate with the customer address
	                            if (NumOfAltAddressLines == 0) {
	                            	altAddrSendTo = pg.getPayeeName();
	                            	altAddrAddr1 = pg.getLine1Address();
	                            	altAddrAddr2 = pg.getLine2Address();
	                            	altAddrCityStateZip = pg.getCity() + ", " + pg.getState() + " " + pg.getZipCd();
	                            }
	                            // Write the Fast Track second payee detail (PDT02010) record
	                            os.write("PDT02010" + cDelim +             // Record Type - 8 bytes
	                            		"FE" + cDelim +                    // Name qualifier - 3 bytes (FE = Remit) This record's data prints on the remittance
	                            		cDelim +                           // ID code qualifier - 2 bytes
	                            		cDelim +                           // ID code - 80 bytes
	                            		altAddrSendTo + cDelim +           // Name - 60 bytes
	                            		cDelim +                           // Additional name 1 - 60 bytes
	                            		cDelim +                           // Additional name 2 - 60 bytes
	                            		altAddrAddr1 + cDelim +            // Address line 1 - 55 bytes
	                            		altAddrAddr2 + cDelim +            // Address line 2 - 55 bytes
	                            		cDelim +                           // Address line 3 - 55 bytes
	                            		cDelim +                           // Address line 4 - 55 bytes
	                            		cDelim +                           // Address line 5 - 55 bytes
	                            		cDelim +                           // Address line 6 - 55 bytes
	                            		altAddrCity + cDelim +             // City - 30 bytes
	                            		altAddrState + cDelim +            // State/Province - 2 bytes
	                            		altAddrZip + cDelim +              // Postal code - 15 bytes
	                            		cDelim +                           // Country code - 3 bytes
	                            		cDelim +                           // Country name - 30 bytes
	                               		cDelim +                           // Ref qualifier 1 - 3 bytes
	                            		cDelim +                           // Ref ID 1 - 50 bytes
	                            		cDelim +                           // Ref description 1 - 80 bytes
	                            		cDelim +                           // Ref qualifier 1 - 3 bytes
	                            		cDelim +                           // Ref ID 1 - 50 bytes
	                            		cDelim +                           // Ref description 1 - 80 bytes
	                            		cDelim +                           // Filler - 46 bytes
	                             		"\n");
	                            
	                            totalRecordCount = totalRecordCount + 4;  //One for the PAY01000 record and three for the PDT02010 records
                            }
                            first = false;		// Set this here so it is only executed once per payee
                        }  //If (first)
                        
                        // Write the Fast Track REM03020 records
                        // Set up data based on whether its a DV or a Payment Request
                        String remittanceIdCode = (subUnitCode.equals(DisbursementVoucherConstants.DV_EXTRACT_SUB_UNIT_CODE)) ? 
                        		DisbursementVoucherConstants.DV_EXTRACT_MELLON_FAST_TRACK_INVOICE_NUMBER_CODE : 
                        			DisbursementVoucherConstants.DV_EXTRACT_MELLON_FAST_TRACK_CUSTOMER_PAYMENT_DOC_NBR_CODE;
                        
                        String remittanceIdText = (subUnitCode.equals(DisbursementVoucherConstants.DV_EXTRACT_SUB_UNIT_CODE)) ? 
                        		pd.getCustPaymentDocNbr() : 
                        			pd.getInvoiceNbr();
                        
                        //Lines for the remittance form
                        RefDesc1 = pd.getPurchaseOrderNbr() + ", " + pd.getCustPaymentDocNbr();
                        RefDesc2 = FirstNoteAfterAddressInfo;
                        RefDesc3 = SecondNoteAfterAddressInfo;
                        RefDesc4 = PreparerInfoText;
                        
                        //Only perform the following if this is an immediate check
                        if (immediateCheckCode) {
                        	if (!wroteMellonIssuanceHeaderRecords) {
                        		// Open the File
                                osI = new BufferedWriter(new FileWriter(arFilename));

                                //Write the Mellon issuance header record
                                osI.write("1" +									//
                                	  repeatThis(" ", 2) + 						//
                              		  "MELLONBANK" + 							//
                              		  "CORNELL   " + 							//
                              		  sdfIDate.format(processDate) +			//
                              		  sdfITime.format(processDate) +			//
                              		  repeatThis(" ", 209) + "\n"				//
                              		  );
                                
                                //Write the Mellon issuance service record
                                osI.write("2" +									//
                                	  repeatThis(" ", 30) + 					//
                              		  "100" + 									//
                              		  "242" + 									//
                              		  "0242" +									//
                              		  "1" +										//
                              		  repeatThis(" ", 200) + "\n"				//
                              		  );
                        		
                        		wroteMellonIssuanceHeaderRecords = true;
                        	}
                        	                        	
                            String arPayeeName = pg.getPayeeName().length() <= 10 ? String.format("%10s", pg.getPayeeName()) : pg.getPayeeName().substring(10);
                            //Write the Mellon issuance detail (regular format) record
                            
                            String CheckNumber = repeatThis("0", 10 - pg.getDisbursementNbr().toString().length()) + pg.getDisbursementNbr().toString();
                            String AmountOfCheck = totalNetAmount.toString().replace(".","");
                            AmountOfCheck = repeatThis("0",10 - AmountOfCheck.length()) + AmountOfCheck;
                            String PayeeName = (arPayeeName.length() <= 60) ? arPayeeName + repeatThis(" ", 60 - arPayeeName.length()): arPayeeName.substring(60);
                            String PayeeAddrLine1 = (pg.getLine1Address().length() <= 60) ? pg.getLine1Address() + repeatThis(" ", 60 - pg.getLine1Address().length()): pg.getLine1Address().substring(60);
                            
                            osI.write("6" +																		// Record Type  - 1 Byte
                            	"2" + 																			// Status Code->  2: Add Issue,  6: Void Issue   - 1 Byte
                          		"CORNELL   " + 																	// Origin - Company Name - 10 Bytes
                          		"MELLONWEST" + 																	// Destination - Identification of receiving location:  MELLONWEST, MELLONEAST, MELLONTRUS, BOSTONNOW - 10 bytes
                          		repeatThis("0", 10 - ourBankAccountNumber.length()) + ourBankAccountNumber +  	// Checking account number - 10 Bytes
                          		CheckNumber +																	// Check Serial Number (check number) - 10 Bytes
                          		AmountOfCheck +																	// Check Amount:  Format $$$$$$$$cc - 10 Bytes
                          		sdfIDate.format(processDate) + 													// Issue Date: Format YYMMDD - 6 Bytes
                          		repeatThis(" ", 10) + 															// Additional Data (Optional) - 10 bytes
                          		repeatThis(" ", 5) + 															// Register Information (Optional) - 5 Bytes
                          		repeatThis(" ", 49) +															// Not used - 49 bytes
                          		String.format("%-60.60s", arPayeeName.toUpperCase()) + 							// Payee Line 1 - Payee Name (Required) - 60 Bytes
                          		String.format("%-60.60s", pg.getLine1Address().toUpperCase()) + "\n" );  		// Payee Line 2 - Payee Name or first line of address (Required) - 60 Bytes
                            
                            arNumOfAddIssues = arNumOfAddIssues + 1;						// Totals the number of add issues across ALL checks issued not just for this one payee.
                            arTotalOfAddIssues = arTotalOfAddIssues.add(totalNetAmount);	// Same as for the count but for the total dollar amount.

                        } //if (immediateCheckCode)
                        else {  
                        	// This section is for Fast Track records

                        	//Write the initial Fast Track REM03020 record
	                        os.write("REM03020" + cDelim +                                 					// Record type - 8 bytes
	                        		remittanceIdCode + cDelim +                            					// Remittance qualifier code - 3 bytes
	                        		remittanceIdText + cDelim +                            					// Remittance ID - 50 bytes
	                        		pd.getNetPaymentAmount().toString() + cDelim +         					// Net invoice amount - 18 bytes
	                        		pd.getInvTotShipAmount().toString() + cDelim +         					// Total invoice amount - 18 bytes
	                        		pd.getInvTotDiscountAmount().toString() + cDelim +     					// Discount amount - 18 bytes
	                        		cDelim +                                               					// Note 1 - 80 bytes
	                        		cDelim +                                               					// Note 2 - 80 bytes
	                        		cDelim +                                               					// Ref qualifier 1
	                        		cDelim +                                               					// Ref ID 1
	                        		RefDesc1 + cDelim +                                        				// Ref description 1
	                        		cDelim +                                               					// Ref qualifier 2
	                        		cDelim +                                               					// Ref ID 2
	                        		RefDesc2 + cDelim +                                     				// Ref description 2
	                        		cDelim +                                               					// Ref qualifier 3
	                        		cDelim +                                               					// Ref ID 3
	                        		RefDesc3 + cDelim +                                     				// Ref description 3
	                        		cDelim +                                               					// Ref qualifier 4
	                        		cDelim +                                               					// Ref ID 4
	                        		RefDesc4 + cDelim + 													// Ref description 4
	                        		cDelim +                                               					// Date qualifier 1
	                        		pd.getInvoiceDate().toString().replace("-", "") + cDelim +              // Date 1 
	                        		cDelim +                                               					// Date qualifier 2
	                        		cDelim +                                               					// Date 2
	                        		cDelim +                                               					// Date qualifier 3
	                        		cDelim +                                               					// Date 3
	                        		cDelim +                                               					// Date qualifier 4
	                        		cDelim +                                               					// Date 4
	                                "\n");                                                 					// Filler
	                        
	                        os.write("REM03020" + cDelim +                                 					// Record type - 8 bytes
	                        		remittanceIdCode + cDelim +                            					// Remittance qualifier code - 3 bytes
	                        		remittanceIdText.toUpperCase() + cDelim +                            	// Remittance ID - 50 bytes
	                        		pd.getNetPaymentAmount().toString() + cDelim +         					// Net invoice amount - 18 bytes
	                        		pd.getInvTotShipAmount().toString() + cDelim +         					// Total invoice amount - 18 bytes
	                        		pd.getInvTotDiscountAmount().toString() + cDelim +     					// Discount amount - 18 bytes
	                        		cDelim +                                               					// Note 1 - 80 bytes
	                        		cDelim +                                               					// Note 2 - 80 bytes
	                        		cDelim +                                               					// Ref qualifier 1
	                        		cDelim +                                               					// Ref ID 1
	                        		altAddrSendTo.toUpperCase() + cDelim +                         			// Ref description 1
	                        		cDelim +                                               					// Ref qualifier 2
	                        		cDelim +                                               					// Ref ID 2
	                        		altAddrAddr1.toUpperCase() + cDelim +                                	// Ref description 2
	                        		cDelim +                                               					// Ref qualifier 3
	                        		cDelim +                                               					// Ref ID 3
	                        		altAddrAddr2.toUpperCase() + cDelim +                                	// Ref description 3
	                        		cDelim +                                               					// Ref qualifier 4
	                        		cDelim +                                               					// Ref ID 4
	                        		altAddrCityStateZip.toUpperCase() + cDelim +                         	// Ref description 4
	                        		cDelim +                                               					// Date qualifier 1
	                        		pd.getInvoiceDate().toString().replace("-", "") + cDelim +				// Date 1 
	                        		cDelim +                                               					// Date qualifier 2
	                        		cDelim +                                               					// Date 2
	                        		cDelim +                                               					// Date qualifier 3
	                        		cDelim +                                               					// Date 3
	                        		cDelim +                                               					// Date qualifier 4
	                        		cDelim +                                               					// Date 4
	                                "\n");                                                 					// Filler
	                      
	                        totalRecordCount = totalRecordCount + 2;	//Two for the two REM03020 records
                        }
                    } //while (paymentDetails.hasNext())
                }  //for (Iterator<Integer> iter = disbNbrs.iterator(); iter.hasNext();)
            }  //for (String bankCode : bankCodes)
            
            // Need to update the total record count here to make sure it includes the trailer record
            totalRecordCount = totalRecordCount + 1;  
            
            if (wroteMellonIssuanceHeaderRecords) {
	            //Write the Mellon issuance service total record
            	String NumOfAddIssues = repeatThis("0",10 - Integer.toString(arNumOfAddIssues).length()) + Integer.toString(arNumOfAddIssues);
                String TotalAmountOfAddIssues = arTotalOfAddIssues.toString().replace(".","");
                TotalAmountOfAddIssues = repeatThis("0",12 - TotalAmountOfAddIssues.length()) + TotalAmountOfAddIssues;
	            osI.write("8" +							//  Record Type
	            		NumOfAddIssues + 				//  10 Bytes, numeric only, right justified, prefixed with zeros as needed to make length
	            		TotalAmountOfAddIssues + 		//  12 Bytes, numeric only (no decimals) right justified, prefixed with zeros as needed to make length
	            		repeatThis("0", 10) + 			//  10 Bytes for voided checks which at this point we don't do.
	            		repeatThis("0", 12) +			//  12 Bytes for the total voided amounts
	          		  	repeatThis(" ", 197) + "\n"		//  Required Filler
	          		  );
	            
	            //Write the Mellon issuance trailer record
	            osI.write("9" +							//  Record Type
	            		NumOfAddIssues + 				//  10 bytes, numeric only, right justified, prefixed with zeros
	          		  	repeatThis(" ", 235) + "\n"		//  Required Filler
	          		  );
            }
            if (wroteMellonFastTrackHeaderRecords)
            {
            //Fast Track trailer record
            os.write("TRL09000" + cDelim +             // Record Type
            		totalRecordCount + cDelim +        // Variable (V) or Fixed (F) flag
            		totalPaymentAmounts + cDelim +     // Delimiter name - Must be either FFCARET or FFCOMMA.  Others are allowed but Mellon will have to be contacted first.
            		"\n");                             // EOR
            }
        } //try
        catch (IOException ie) {
            LOG.error("IO Exception with writeExtractCheckFileMellonBankFastTrack() Problem reading file:  " + ftFilename, ie);
            throw new IllegalArgumentException("Error writing to output file: " + ie.getMessage());
        }
        catch (Exception ex) {
            LOG.error("General Exception with writeExtractCheckFileMellonBankFastTrack().  Error is:  " + ex.getMessage(), ex);
        }
        finally {
            // Close file
            if (os != null) {
                try {
                    os.close();
                }
                catch (IOException ie) {
                    // Not much we can do now
                }
            }  //osI.close();
            if (osI != null) {
                try {
                	osI.close();
                }
                catch (IOException ie) {
                    // Not much we can do now
                }
            } 
        }
    }
        
    // This utility function produces a string of (s) characters (n) times.
    protected String repeatThis(String s, int n){
    	return  String.format(String.format("%%0%dd", n), 0).replace("0",s);
    }
        
    protected void writeExtractAchFile(PaymentStatus extractedStatus, String filename, Date processDate, SimpleDateFormat sdf) {
    	    	
        // Writes out the Mellon Fast Track formatted file for ACH payments.  We need to do this first since the status is set in this method which
    	//   causes the writeExtractAchFileMellonBankFastTrack method to not find anything.
    	writeExtractAchFileMellonBankFastTrack(extractedStatus, filename, processDate, sdf);

        BufferedWriter os = null;
        try {
            os = new BufferedWriter(new FileWriter(filename));
            os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writeOpenTag(os, 0, "achPayments");

            // totals for summary
            Map<String, Integer> unitCounts = new HashMap<String, Integer>();
            Map<String, KualiDecimal> unitTotals = new HashMap<String, KualiDecimal>();

            Iterator iter = paymentGroupService.getByDisbursementTypeStatusCode(PdpConstants.DisbursementTypeCodes.ACH, PdpConstants.PaymentStatusCodes.PENDING_ACH);
            while (iter.hasNext()) {
                PaymentGroup paymentGroup = (PaymentGroup) iter.next();
                if (!testMode) {
                    paymentGroup.setDisbursementDate(new java.sql.Date(processDate.getTime()));
                    paymentGroup.setPaymentStatus(extractedStatus);
                    businessObjectService.save(paymentGroup);
                }

                writeOpenTagAttribute(os, 2, "ach", "disbursementNbr", paymentGroup.getDisbursementNbr().toString());
                PaymentProcess paymentProcess = paymentGroup.getProcess();
                writeTag(os, 4, "processCampus", paymentProcess.getCampusCode());
                writeTag(os, 4, "processId", paymentProcess.getId().toString());

                writeBank(os, 4, paymentGroup.getBank());

                writeTag(os, 4, "disbursementDate", sdf.format(processDate));
                writeTag(os, 4, "netAmount", paymentGroup.getNetPaymentAmount().toString());

                writePayeeAch(os, 4, paymentGroup);
                writeTag(os, 4, "customerUnivNbr", paymentGroup.getCustomerInstitutionNumber());
                writeTag(os, 4, "paymentDate", sdf.format(paymentGroup.getPaymentDate()));

                // Write customer profile information
                CustomerProfile cp = paymentGroup.getBatch().getCustomerProfile();
                writeCustomerProfile(os, 4, cp);

                // Write all payment level information
                writeOpenTag(os, 4, "payments");
                List pdList = paymentGroup.getPaymentDetails();
                for (Iterator iterator = pdList.iterator(); iterator.hasNext();) {
                    PaymentDetail paymentDetail = (PaymentDetail) iterator.next();
                    writeOpenTag(os, 6, "payment");

                    // Write detail info
                    writeTag(os, 6, "purchaseOrderNbr", paymentDetail.getPurchaseOrderNbr());
                    writeTag(os, 6, "invoiceNbr", paymentDetail.getInvoiceNbr());
                    writeTag(os, 6, "requisitionNbr", paymentDetail.getRequisitionNbr());
                    writeTag(os, 6, "custPaymentDocNbr", paymentDetail.getCustPaymentDocNbr());
                    writeTag(os, 6, "invoiceDate", sdf.format(paymentDetail.getInvoiceDate()));

                    writeTag(os, 6, "origInvoiceAmount", paymentDetail.getOrigInvoiceAmount().toString());
                    writeTag(os, 6, "netPaymentAmount", paymentDetail.getNetPaymentAmount().toString());
                    writeTag(os, 6, "invTotDiscountAmount", paymentDetail.getInvTotDiscountAmount().toString());
                    writeTag(os, 6, "invTotShipAmount", paymentDetail.getInvTotShipAmount().toString());
                    writeTag(os, 6, "invTotOtherDebitAmount", paymentDetail.getInvTotOtherDebitAmount().toString());
                    writeTag(os, 6, "invTotOtherCreditAmount", paymentDetail.getInvTotOtherCreditAmount().toString());

                    writeOpenTag(os, 6, "notes");
                    for (Iterator i = paymentDetail.getNotes().iterator(); i.hasNext();) {
                        PaymentNoteText note = (PaymentNoteText) i.next();
                        writeTag(os, 8, "note", escapeString(note.getCustomerNoteText()));
                    }
                    writeCloseTag(os, 6, "notes");

                    writeCloseTag(os, 4, "payment");

                    String unit = paymentGroup.getBatch().getCustomerProfile().getChartCode() + "-" + paymentGroup.getBatch().getCustomerProfile().getUnitCode() + "-" + paymentGroup.getBatch().getCustomerProfile().getSubUnitCode();

                    Integer count = 1;
                    if (unitCounts.containsKey(unit)) {
                        count = 1 + unitCounts.get(unit);
                    }
                    unitCounts.put(unit, count);

                    KualiDecimal unitTotal = paymentDetail.getNetPaymentAmount();
                    if (unitTotals.containsKey(unit)) {
                        unitTotal = paymentDetail.getNetPaymentAmount().add(unitTotals.get(unit));
                    }
                    unitTotals.put(unit, unitTotal);
                }

                writeCloseTag(os, 4, "payments");
                writeCloseTag(os, 2, "ach");
            }
            writeCloseTag(os, 0, "achPayments");

            // send summary email
            paymentFileEmailService.sendAchSummaryEmail(unitCounts, unitTotals, dateTimeService.getCurrentDate());
        }
        catch (IOException ie) {
            LOG.error("extractAchPayments() Problem reading file:  " + filename, ie);
            throw new IllegalArgumentException("Error writing to output file: " + ie.getMessage());
        }
        finally {
            // Close file
            if (os != null) {
                try {
                    os.close();
                }
                catch (IOException ie) {
                    // Not much we can do now
                }
            }
        }
    }
    protected void writeExtractAchFileMellonBankFastTrack(PaymentStatus extractedStatus, String filename, Date processDate, SimpleDateFormat sdf) {
        BufferedWriter os = null;
        String cDelim = "^";  //column delimiter: Per Mellon FastTrack spec, your choices are: "^" or ",".  If you change this make sure you change the associated name on the next line!
        String cDname = "FFCARET";  // column delimiter name: Per Mellon FastTrack spec, your choices are: FFCARET and FFCOMMA for variable record types
        String hdrRecType = "V";  // record type: Per Mellon's FastTrack spec, can be either V for variable or F for fixed.
        String testIndicator = "P";
        String ourBankAccountNumber = "";
        String ourBankRoutingNumber = "";
        String subUnitCode = "";
        String divisionCode = "";
        String achCode = "";
        boolean wroteFastTrackHeaderRecords = false;
        // Change the filename so that it ends in .txt instead of .xml
        filename = filename.replace(".xml", ".txt");
        int totalRecordCount = 0;
        KualiDecimal totalPaymentAmounts = KualiDecimal.ZERO; 
    	
    	try {
            Iterator iter = paymentGroupService.getByDisbursementTypeStatusCode(PdpConstants.DisbursementTypeCodes.ACH, PdpConstants.PaymentStatusCodes.PENDING_ACH);
            while (iter.hasNext()) {
                PaymentGroup paymentGroup = (PaymentGroup) iter.next();
                
                // Get our Bank Account Number and our bank routing number
                ourBankAccountNumber = paymentGroup.getBank().getBankAccountNumber();
                ourBankRoutingNumber = paymentGroup.getBank().getBankRoutingNumber();
                
                //We only need to write the Fast Track header records once in this file.
                if (!wroteFastTrackHeaderRecords) {
                	
                	// open the file for writing
                	os = new BufferedWriter(new FileWriter(filename));
                	
	            	//Write the Fast Track header record (FIL00010)
	                os.write("FIL00010" + cDelim +                // Record Type
	                		hdrRecType + cDelim +                 // Variable (V) or Fixed (F) flag
	                		cDname + cDelim +                     // Delimiter name - Must be either FFCARET or FFCOMMA.  Others are allowed but Mellon will have to be contacted first.
	                		"CORNELLUNIVKFS" + cDelim +           // Customer Id - a unique identifier for the customer
	                		testIndicator + cDelim +              // Test Indicator:  T = Test run, P = Production Run
	                		"820" + cDelim +                      // EDI Document Id (3 Bytes)
	                		ourBankAccountNumber + cDelim +       // Our bank account number (15 Bytes)
	                		cDelim +                              // Customer Division Id - 35 bytes - Optional
	                		sdf.format(processDate) + cDelim +    // File Date and Time - 14 Bytes
	                		cDelim +                              // Reserved Field - 3 Bytes
	                        "\n");
	            	
	                //Write the Fast Track email records (FIL00020)
	            	os.write("FIL00020" + cDelim + 
	            			"uco_operations-mailbox@cornell.edu" + cDelim +
	            			cDelim +
	            			"\n");
	            	os.write("FIL00020" + cDelim + 
	            			"uc0-accts-pay-mailbox@cornell.edu" + 
	            			cDelim + 
	            			"\n");
	            	os.write("FIL00020" + cDelim + 
	            			"lcs38@cornell.edu" + cDelim +
	            			cDelim + 
	            			"\n");
	            	os.write("FIL00020" + cDelim + 
	            			"cms1@cornell.edu" + cDelim +
	            			cDelim +
	            			"\n");
	            	
	            	totalRecordCount = 5;  // We've successfully written the header and 4 email records.
	            	wroteFastTrackHeaderRecords = true;
                }
            	// At this point we will start looping through all the checks and write the PAY01000 record (one for each payee), 
            	// two (2) PDT2010 records (one for the Payer & Payee) and as many REM3020 records as needed for each amount being
            	// paid to this payee.
                
                boolean first = true;
                KualiDecimal totalNetAmount = new KualiDecimal(0);

                // We need to obtain the total amount for this specific payee, so loop through all the records here.
                List pdListTot = paymentGroup.getPaymentDetails();
                for (Iterator it2 = pdListTot.iterator(); it2.hasNext();) {
                    PaymentDetail pd = (PaymentDetail) it2.next();
                    totalNetAmount = totalNetAmount.add(pd.getNetPaymentAmount());
                }
                
                List pdList = paymentGroup.getPaymentDetails();
                for (Iterator iterator = pdList.iterator(); iterator.hasNext();) {
                    PaymentDetail paymentDetail = (PaymentDetail) iterator.next();
                    if (first) {
                        // Get country name for code
                        String sCountryName = "";
                        Country country = countryService.getByPrimaryId(paymentGroup.getCountry());
                        if (country != null) {
                        	sCountryName = country.getPostalCountryName();
                        }
                        else {
                        	sCountryName = paymentGroup.getCountry();
                        }
                        
                        int dvCodeInt=0;
                        // Get customer profile information
                        CustomerProfile cp = paymentGroup.getBatch().getCustomerProfile();
                        subUnitCode = cp.getSubUnitCode();

                        if (subUnitCode.equals("DV")) dvCodeInt = 1;
                        else if (subUnitCode.equals("PURAP")) dvCodeInt = 1;
                        else if (subUnitCode.equals("LIBR")) dvCodeInt = 2;
                        else if (subUnitCode.equals("CSTR")) dvCodeInt = 3;
                        else if (subUnitCode.equals("STAT")) dvCodeInt = 4;
                        else if (subUnitCode.equals("CLIF")) dvCodeInt = 5;
                        divisionCode = String.format(String.format("%%0%dd", 3), dvCodeInt);
                        
                        //Determine the ACH Code to send down.  Here are the rules:
                        // 1.  If they are a vendor and they've selected checking account, the ACH code is CTX (vendors can only have ACH to checking)
                        // 2.  If they are a vendor and they've selected savings account, the ACH code is PPD (sometimes sole proprietors are vendors and they use personal not corporate accounts)
                        // 3.  If they are NOT a vendor, then the ACH code is always PPD (allows employees to have their's deposited in checking or savings)
                        
                        //Determine which ACH code to use: CTX for corporate accounts, or PPD for Personal accounts
                        // String payeeId = paymentGroup.getPayeeId();							// Returns the ID of the payee and is the vendor number if the next var indicates that
                        // String payeeIdTypeDesc = paymentGroup.getPayeeIdTypeDesc();			// Returns "Vendor Number" if the payeeID is a vendor number
                        
                        boolean isVendor = (paymentGroup.getPayeeIdTypeCd().equals("V")) ? true : false;	// payeeIdTypeCode returns a "V" if it is a vendor
                        String accountType = paymentGroup.getAchAccountType();								// Returns either a 22 for checking or a 32 for Savings account
                        if (isVendor)
                        	if (accountType.equals("22"))
                        		achCode = "CTX";		// Implementation of rule #1 above
                        	else
                        		achCode = "PPD";		// Implementation of rule #2 above
                        else
                        	achCode = "PPD";			// Implementation of rule #3 above
                        
                    	//Write only 1 PAY01000 record
                    	os.write("PAY01000" + cDelim +                              					// Record Type - 8 bytes
                    			"1" + cDelim +                                      					// 7=Payment and Electronic Advice (Transaction handling code - 2 bytes)
                    			totalNetAmount.toString() + cDelim +                					// Total amount of check (Payment amount - 18 bytes)
                    			"C" + cDelim +                                      					// C=Credit, D=Debit (Credit or debit Flag - 1 Byte)
                    			"ACH" + cDelim +                                    					// ACH= ACH Payment method - 3 Bytes
                    			achCode + cDelim +                                    					// PPD is used for ACH payments to a personal bank account - 10 bytes
                    			"01" + cDelim +                                     					// Originators bank id qualifier - 2 bytes
                    			ourBankRoutingNumber + cDelim +                     					// Originators bank id - 12 bytes
                    			"DA" + cDelim +                                    						// Originating account number Qualifier - 3 bytes
                    			ourBankAccountNumber + cDelim +                     					// Originating account number - 35 bytes
                    			"2150532082" + cDelim+                              					// Originating company identifier - 10 bytes
                    			"01" + cDelim +                                            				// Receiving bank id qualifier - 2 bytes
                    			paymentGroup.getAchBankRoutingNbr() + cDelim +                  		// Receiving bank id - 12 bytes
                    			paymentGroup.getAchAccountType() + cDelim +                             // Receiving account number qualifier - 3 bytes
                    			paymentGroup.getAchAccountNumber().getAchBankAccountNbr() + cDelim + 	// Receiving account number - 35 bytes
                    			sdf.format(processDate) + cDelim +                  					// Effective date - 8 bytes
                    			cDelim +                                            					// Business function code - 3 bytes 
                    			paymentGroup.getDisbursementNbr().toString() + cDelim + 				// Trace number (check number) - 50 bytes
                    			divisionCode + cDelim +                                            		// Division code - 50 bytes 
                    			cDelim +                                            					// Currency code - 3 bytes
                    			cDelim +                                            					// Note 1 - 80 bytes
                    			cDelim +                                            					// Note 2 - 80 bytes
                    			cDelim +                                            					// Note 3 - 80 bytes
                    			cDelim +                                            					// Note 4 - 80 bytes
                    			cDelim +                                            					// 
                    			"\n");

                     	totalPaymentAmounts = totalPaymentAmounts.add(totalNetAmount);
                    	
                        // Write the Payer Detail(PDT02010) record for the payer (us) (only one per payee)
                        os.write("PDT02010" + cDelim +                        	// Record Type - 8 bytes
                        		"PR" + cDelim +                               	// Name qualifier - 3 bytes
                        		cDelim +                                      	// ID code qualifier - 2 bytes
                        		cDelim +                                      	// ID code - 80 bytes
                        		"Cornell University" + cDelim +               	// Name - 60 bytes
                        		cDelim +                                      	// Additional name 1 - 60 bytes
                        		cDelim +                                      	// Additional name 2 - 60 bytes
                        		"Division of Financial Affairs" + cDelim +    	// Address line 1 - 55 bytes
                        		"341 Pine Tree Road" + cDelim +               	// Address line 2 - 55 bytes
                        		cDelim +                                      	// Address line 3 - 55 bytes
                        		cDelim +                                      	// Address line 4 - 55 bytes
                        		cDelim +                                      	// Address line 5 - 55 bytes
                        		cDelim +                                      	// Address line 6 - 55 bytes
                        		"Ithaca" + cDelim +                           	// City - 30 bytes
                        		"NY" + cDelim +                               	// State/Province - 2 bytes
                        		"148502820" + cDelim +                        	// Postal code - 15 bytes
                        		cDelim +                                      	// Country code - 3 bytes
                        		cDelim +                                      	// Country name - 30 bytes
                        		cDelim +                                      	// Ref qualifier 1 - 3 bytes
                        		cDelim +                                     	// Ref ID 1 - 50 bytes
                        		cDelim +                                      	// Ref description 1 - 80 bytes
                        		cDelim +                                      	// Ref qualifier 1 - 3 bytes
                        		cDelim +                                      	// Ref ID 1 - 50 bytes
                        		cDelim +                                      	// Ref description 1 - 80 bytes
                        		cDelim +                                      	// 
                        		"\n");
                        
                        // Write the payee detail (PDT02010) record for the payee (them) (only one per payee)
                        os.write("PDT02010" + cDelim +             											// Record Type - 8 bytes
                        		"PR" + cDelim +                    											// Name qualifier - 3 bytes
                        		cDelim +                           											// ID code qualifier - 2 bytes
                        		cDelim +                           											// ID code - 80 bytes
                        		paymentGroup.getPayeeName() + cDelim +       								// Name - 60 bytes
                        		cDelim +                           											// Additional name 1 - 60 bytes
                        		cDelim +                           											// Additional name 2 - 60 bytes
                        		String.format("%-55.55s", paymentGroup.getLine1Address()) + cDelim +    	// Address line 1 - 55 bytes
                        		String.format("%-55.55s", paymentGroup.getLine2Address()) + cDelim +    	// Address line 2 - 55 bytes
                        		String.format("%-55.55s", paymentGroup.getLine3Address()) + cDelim +    	// Address line 3 - 55 bytes
                        		String.format("%-55.55s", paymentGroup.getLine4Address()) + cDelim +    	// Address line 4 - 55 bytes
                        		cDelim +                          	 										// Address line 5 - 55 bytes
                        		cDelim +                           											// Address line 6 - 55 bytes
                        		String.format("%-30.30s", paymentGroup.getCity()) + cDelim +            	// City - 30 bytes
                        		String.format("%-2.2s", paymentGroup.getState()) + cDelim +           		// State/Province - 2 bytes
                        		String.format("%-15.15s", paymentGroup.getZipCd()) + cDelim +           	// Postal code - 15 bytes
                        		cDelim +                           											// Country code - 3 bytes
                        		sCountryName + cDelim +            											// Country name - 30 bytes
                           		cDelim +                           											// Ref qualifier 1 - 3 bytes
                        		cDelim +                           											// Ref ID 1 - 50 bytes
                        		cDelim +                           											// Ref description 1 - 80 bytes
                        		cDelim +                           											// Ref qualifier 1 - 3 bytes
                        		cDelim +                           											// Ref ID 1 - 50 bytes
                        		cDelim +                           											// Ref description 1 - 80 bytes
                        		cDelim + "\n");
                       
                        totalRecordCount = totalRecordCount + 3;	//One for the PAY01000 record and one for each PDT02010 record
                        first = false;								// Set this here so it is only executed once per payee
                    }  //If (first)

                    // Write the remittance record (REM03020) - occurs once for each of the payee's invoices.
                    // Setup Notes
                    String rec03020Note1= "";
                    String rec03020Note2= "";
                    Iterator<PaymentNoteText> ix = paymentDetail.getNotes().iterator();
                    //Get the first note
                    if  (ix.hasNext()) {
                    	PaymentNoteText note = (PaymentNoteText) ix.next();
                    	rec03020Note1 = note.getCustomerNoteText();
                    }
                    //Get the second note
                    if  (ix.hasNext()) {
                    	PaymentNoteText note = (PaymentNoteText) ix.next();
                    	rec03020Note2 = note.getCustomerNoteText();
                    }
                    
                    // Write the REM03020 record
                    // Set up data based on whether its a DV or a Payment Request
                    String remittanceIdCode = (subUnitCode == DisbursementVoucherConstants.DV_EXTRACT_SUB_UNIT_CODE) ? 
                    		DisbursementVoucherConstants.DV_EXTRACT_MELLON_FAST_TRACK_INVOICE_NUMBER_CODE : 
                    			DisbursementVoucherConstants.DV_EXTRACT_MELLON_FAST_TRACK_CUSTOMER_PAYMENT_DOC_NBR_CODE;
                    String remittanceIdText = (subUnitCode == DisbursementVoucherConstants.DV_EXTRACT_SUB_UNIT_CODE) ? 
                    		paymentDetail.getCustPaymentDocNbr() : 
                    			paymentDetail.getInvoiceNbr();
                    		
                    os.write("REM03020" + cDelim +                                 				// Record type - 8 bytes
                    		remittanceIdCode + cDelim +                            				// Remittance qualifier code - 3 bytes
                    		remittanceIdText + cDelim +                            				// Remittance ID - 50 bytes
                    		paymentDetail.getNetPaymentAmount().toString() + cDelim +         	// Net invoice amount - 18 bytes
                    		paymentDetail.getInvTotShipAmount().toString() + cDelim +         	// Total invoice amount - 18 bytes
                    		paymentDetail.getInvTotDiscountAmount().toString() + cDelim +     	// Discount amount - 18 bytes
                    		String.format("%-80.80s", rec03020Note1) + cDelim +                 // Note 1 - 80 bytes
                    		String.format("%-80.80s", rec03020Note2) + cDelim +                 // Note 2 - 80 bytes
                    		cDelim +                                               				// Ref qualifier 1
                    		cDelim +                                               				// Ref ID 1
                    		cDelim +                                               				// Ref description 1
                    		cDelim +                                               				// Ref qualifier 2
                    		cDelim +                                               				// Ref ID 2
                    		cDelim +                                               				// Ref description 2
                    		cDelim +                                               				// Ref qualifier 3
                    		cDelim +                                               				// Ref ID 3
                    		cDelim +                                               				// Ref description 3
                    		cDelim +                                               				// Ref qualifier 4
                    		cDelim +                                               				// Ref ID 4
                    		cDelim +                                               				// Ref description 4
                    		cDelim +                                               				// Date qualifier 1
                    		cDelim +                                               				// Date 1 
                    		cDelim +                                               				// Date qualifier 2
                    		cDelim +                                               				// Date 2
                    		cDelim +                                               				// Date qualifier 3
                    		cDelim +                                               				// Date 3
                    		cDelim +                                               				// Date qualifier 4
                    		cDelim + "\n");
                    		
                    totalRecordCount = totalRecordCount + 1;	//One for each REM03020 record
                    
                }  //  for (Iterator iterator = pdList.iterator(); iterator.hasNext();)
            }  //  while (iter.hasNext())
            
            if (wroteFastTrackHeaderRecords) {
	            // Need to update the total record count here to make sure it includes the trailer record
	            totalRecordCount = totalRecordCount + 1;   
	            //Now write the trailer record
	            os.write("TRL09000" + cDelim +             // Record Type
	            		totalRecordCount + cDelim +        // Variable (V) or Fixed (F) flag
	            		totalPaymentAmounts + cDelim +     // Delimiter name - Must be either FFCARET or FFCOMMA.  Others are allowed but Mellon will have to be contacted first.
	            		"\n");                             // EOR
            }

        }  // try
        catch (IOException ie) {
            LOG.error("extractAchPayments() Problem reading file:  " + filename, ie);
            throw new IllegalArgumentException("Error writing to output file: " + ie.getMessage());
        }
        finally {
            // Close file
            if (os != null) {
                try {
                    os.close();
                }
                catch (IOException ie) {
                    // Not much we can do now
                	LOG.error("IO Exception in extractAchPayments():  " + filename, ie);
                }
            }
        }
    }
    protected static String SPACES = "                                                       ";

    protected void writeTag(BufferedWriter os, int indent, String tag, String data) throws IOException {
        if (data != null) {
            os.write(SPACES.substring(0, indent));
            os.write("<" + tag + ">" + escapeString(data) + "</" + tag + ">\n");
        }
    }

    protected void writeOpenTag(BufferedWriter os, int indent, String tag) throws IOException {
        os.write(SPACES.substring(0, indent));
        os.write("<" + tag + ">\n");
    }

    protected void writeOpenTagAttribute(BufferedWriter os, int indent, String tag, String attr, String attrVal) throws IOException {
        os.write(SPACES.substring(0, indent));
        os.write("<" + tag + " " + attr + "=\"" + escapeString(attrVal) + "\">\n");
    }

    protected void writeOpenTagAttribute(BufferedWriter os, int indent, String tag, String attr1, String attr1Val, String attr2, String attr2Val) throws IOException {
        os.write(SPACES.substring(0, indent));
        os.write("<" + tag + " " + attr1 + "=\"" + escapeString(attr1Val) + "\" " + attr2 + "=\"" + escapeString(attr2Val) + "\">\n");
    }

    protected void writeCloseTag(BufferedWriter os, int indent, String tag) throws IOException {
        os.write(SPACES.substring(0, indent));
        os.write("</" + tag + ">\n");
    }

    protected void writeBank(BufferedWriter os, int indent, Bank b) throws IOException {
        if (b != null) {
            writeOpenTagAttribute(os, indent, "bank", "code", b.getBankCode());
            writeTag(os, indent + 2, "accountNumber", b.getBankAccountNumber());
            writeTag(os, indent + 2, "routingNumber", b.getBankRoutingNumber());
            writeCloseTag(os, indent, "bank");
        }
    }

    protected void writeCustomerProfile(BufferedWriter os, int indent, CustomerProfile cp) throws IOException {
        writeOpenTag(os, indent, "customerProfile");
        writeTag(os, indent + 2, "chartCode", cp.getChartCode());
        writeTag(os, indent + 2, "orgCode", cp.getUnitCode());
        writeTag(os, indent + 2, "subUnitCode", cp.getSubUnitCode());
        writeOpenTag(os, indent + 2, "checkHeaderNoteLines");
        writeTag(os, indent + 4, "note", cp.getCheckHeaderNoteTextLine1());
        writeTag(os, indent + 4, "note", cp.getCheckHeaderNoteTextLine2());
        writeTag(os, indent + 4, "note", cp.getCheckHeaderNoteTextLine3());
        writeTag(os, indent + 4, "note", cp.getCheckHeaderNoteTextLine4());
        writeCloseTag(os, indent + 2, "checkHeaderNoteLines");
        writeOpenTag(os, indent + 2, "additionalCheckNoteLines");
        writeTag(os, indent + 4, "note", cp.getAdditionalCheckNoteTextLine1());
        writeTag(os, indent + 4, "note", cp.getAdditionalCheckNoteTextLine2());
        writeTag(os, indent + 4, "note", cp.getAdditionalCheckNoteTextLine3());
        writeTag(os, indent + 4, "note", cp.getAdditionalCheckNoteTextLine4());
        writeCloseTag(os, indent + 2, "additionalCheckNoteLines");
        writeCloseTag(os, indent, "customerProfile");
    }

    protected void writePayeeAch(BufferedWriter os, int indent, PaymentGroup pg) throws IOException {
        writePayeeInformation(os, indent, pg, true);
    }

    protected void writePayee(BufferedWriter os, int indent, PaymentGroup pg) throws IOException {
        writePayeeInformation(os, indent, pg, false);
    }

    protected void writePayeeInformation(BufferedWriter os, int indent, PaymentGroup pg, boolean includeAch) throws IOException {
        os.write(SPACES.substring(0, indent));
        os.write("<payee id=\"" + pg.getPayeeId() + "\" type=\"" + pg.getPayeeIdTypeCd() + "\">\n");
        writeTag(os, indent + 2, "payeeName", pg.getPayeeName());
        writeTag(os, indent + 2, "line1Address", pg.getLine1Address());
        writeTag(os, indent + 2, "line2Address", pg.getLine2Address());
        writeTag(os, indent + 2, "line3Address", pg.getLine3Address());
        writeTag(os, indent + 2, "line4Address", pg.getLine4Address());
        writeTag(os, indent + 2, "city", pg.getCity());
        writeTag(os, indent + 2, "state", pg.getState());
        writeTag(os, indent + 2, "zipCd", pg.getZipCd());
        
        // get country name for code
        Country country = countryService.getByPrimaryId(pg.getCountry());
        if (country != null) {
            writeTag(os, indent + 2, "country", country.getPostalCountryName());
        }
        else {
            writeTag(os, indent + 2, "country", pg.getCountry());
        }

        if (includeAch) {
            writeTag(os, indent + 2, "achBankRoutingNbr", pg.getAchBankRoutingNbr());
            writeTag(os, indent + 2, "achBankAccountNbr", pg.getAchAccountNumber().getAchBankAccountNbr());
            writeTag(os, indent + 2, "achAccountType", pg.getAchAccountType());
        }
        writeCloseTag(os, indent, "payee");
    }

    protected String escapeString(String input) {
        String output = input.replaceAll("\\&", "&amp;");
        output = output.replaceAll("\"", "&quot;");
        output = output.replaceAll("\\'", "&apos;");
        output = output.replaceAll("\\<", "&lt;");
        output = output.replaceAll("\\>", "&gt;");
        return output;
    }

    /**
     * Sets the directoryName attribute value.
     * 
     * @param directoryName The directoryName to set.
     */
    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }


    /**
     * Sets the dateTimeService attribute value.
     * 
     * @param dateTimeService The dateTimeService to set.
     */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    /**
     * Sets the parameterService attribute value.
     * 
     * @param parameterService The parameterService to set.
     */
    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    /**
     * Sets the paymentGroupService attribute value.
     * 
     * @param paymentGroupService The paymentGroupService to set.
     */
    public void setPaymentGroupService(PaymentGroupService paymentGroupService) {
        this.paymentGroupService = paymentGroupService;
    }

    /**
     * Sets the paymentDetailService attribute value.
     * 
     * @param paymentDetailService The paymentDetailService to set.
     */
    public void setPaymentDetailService(PaymentDetailService paymentDetailService) {
        this.paymentDetailService = paymentDetailService;
    }

    /**
     * Sets the paymentGroupHistoryDao attribute value.
     * 
     * @param paymentGroupHistoryDao The paymentGroupHistoryDao to set.
     */
    public void setPaymentGroupHistoryDao(PaymentGroupHistoryDao paymentGroupHistoryDao) {
        this.paymentGroupHistoryDao = paymentGroupHistoryDao;
    }

    /**
     * Sets the processDao attribute value.
     * 
     * @param processDao The processDao to set.
     */
    public void setProcessDao(ProcessDao processDao) {
        this.processDao = processDao;
    }

    /**
     * Sets the paymentFileEmailService attribute value.
     * 
     * @param paymentFileEmailService The paymentFileEmailService to set.
     */
    public void setPaymentFileEmailService(PdpEmailService paymentFileEmailService) {
        this.paymentFileEmailService = paymentFileEmailService;
    }

    /**
     * Sets the business object service
     * 
     * @param businessObjectService
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setKualiConfigurationService(KualiConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    /**
     * Gets the countryService attribute.
     * 
     * @return Returns the countryService.
     */
    protected CountryService getCountryService() {
        return countryService;
    }

    /**
     * Sets the countryService attribute value.
     * 
     * @param countryService The countryService to set.
     */
    public void setCountryService(CountryService countryService) {
        this.countryService = countryService;
    }


}
