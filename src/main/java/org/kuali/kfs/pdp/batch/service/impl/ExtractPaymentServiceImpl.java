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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.text.html.HTMLDocument.HTMLReader.SpecialAction;

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
import org.kuali.rice.core.config.ConfigContext;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kns.bo.Country;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.CountryService;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.KualiConfigurationService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;
import org.kuali.kfs.sys.KFSParameterKeyConstants;

import com.rsmart.kuali.kfs.pdp.service.AchBundlerHelperService;


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
    protected AchBundlerHelperService achBundlerHelperService;

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

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date processDate = dateTimeService.getCurrentDate();
        BufferedWriter os = null;
        BufferedWriter osI = null;
        boolean wroteImmediateHeaderRecords = false;
        boolean wroteCheckHeaderRecords = false;
        String immediateFilename = filename.replace("check", "check_immediate");
        String checkFilename = filename;
        boolean first = true;
        boolean isImmediate = false;
        
        try {
        	//KFSPTS-1460: parameterized the hard coded email addresses
        	List<String> notificationEmailAddresses = this.getBankPaymentFileNotificationEmailAddresses();  
        	
        	writeExtractCheckFileMellonBankFastTrack(extractedStatus, p, filename, processId, notificationEmailAddresses);
            List<String> bankCodes = paymentGroupService.getDistinctBankCodesForProcessAndType(processId, PdpConstants.DisbursementTypeCodes.CHECK);

            for (String bankCode : bankCodes) {
                List<Integer> disbNbrs = paymentGroupService.getDisbursementNumbersByDisbursementTypeAndBankCode(processId, PdpConstants.DisbursementTypeCodes.CHECK, bankCode);
                for (Iterator<Integer> iter = disbNbrs.iterator(); iter.hasNext();) {
                    Integer disbursementNbr = iter.next();
                    KualiDecimal totalNetAmount = new KualiDecimal(0);
                    
                    first = true;
                    
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
                        
                        isImmediate = pg.getProcessImmediate();
                        // As each record passes through we decide whether we are generating an immediates XML file or a checks XML file or BOTH.
                        if (first && !isImmediate) {
                        	if (!wroteCheckHeaderRecords) { 
                                os = new BufferedWriter(new FileWriter(checkFilename));
                                os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                                writeOpenTagAttribute(os, 0, "checks", "processId", processId.toString(), "campusCode", p.getCampusCode());
                                wroteCheckHeaderRecords = true;
                            }
                        	
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
                            first = false;
                        }
                        
                        if (first && isImmediate) {
                            if (!wroteImmediateHeaderRecords) {
    	                        osI = new BufferedWriter(new FileWriter(immediateFilename));
    	                        osI.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    	                        writeOpenTagAttribute(osI, 0, "checks", "processId", processId.toString(), "campusCode", p.getCampusCode());
    	                        wroteImmediateHeaderRecords = true;
                            }
                            
                            writeOpenTagAttribute(osI, 2, "check", "disbursementNbr", pg.getDisbursementNbr().toString());
                            // Write check level information
                            writeBank(osI, 4, pg.getBank());
                            writeTag(osI, 4, "disbursementDate", sdf.format(processDate));
                            writeTag(osI, 4, "netAmount", totalNetAmount.toString());
                            writePayee(osI, 4, pg);
                            writeTag(osI, 4, "campusAddressIndicator", pg.getCampusAddress().booleanValue() ? "Y" : "N");
                            writeTag(osI, 4, "attachmentIndicator", pg.getPymtAttachment().booleanValue() ? "Y" : "N");
                            writeTag(osI, 4, "specialHandlingIndicator", pg.getPymtSpecialHandling().booleanValue() ? "Y" : "N");
                            writeTag(osI, 4, "immediatePaymentIndicator", pg.getProcessImmediate().booleanValue() ? "Y" : "N");
                            writeTag(osI, 4, "customerUnivNbr", pg.getCustomerInstitutionNumber());
                            writeTag(osI, 4, "paymentDate", sdf.format(pg.getPaymentDate()));

                            // Write customer profile information
                            CustomerProfile cp = pg.getBatch().getCustomerProfile();
                            writeCustomerProfile(osI, 4, cp);
                            writeOpenTag(osI, 4, "payments");
                            first = false;
                        }
                        
                        if (!isImmediate && wroteCheckHeaderRecords) {
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
	                            writeTag(os, 10, "note", updateNoteLine(note.getCustomerNoteText()));
	                        }
	                        writeCloseTag(os, 8, "notes");
	                        writeCloseTag(os, 6, "payment");
                        }
                        
                        if (isImmediate && wroteImmediateHeaderRecords) {
                            writeOpenTag(osI, 6, "payment");
                            writeTag(osI, 8, "purchaseOrderNbr", pd.getPurchaseOrderNbr());
                            writeTag(osI, 8, "invoiceNbr", pd.getInvoiceNbr());
                            writeTag(osI, 8, "requisitionNbr", pd.getRequisitionNbr());
                            writeTag(osI, 8, "custPaymentDocNbr", pd.getCustPaymentDocNbr());
                            writeTag(osI, 8, "invoiceDate", sdf.format(pd.getInvoiceDate()));
                            writeTag(osI, 8, "origInvoiceAmount", pd.getOrigInvoiceAmount().toString());
                            writeTag(osI, 8, "netPaymentAmount", pd.getNetPaymentAmount().toString());
                            writeTag(osI, 8, "invTotDiscountAmount", pd.getInvTotDiscountAmount().toString());
                            writeTag(osI, 8, "invTotShipAmount", pd.getInvTotShipAmount().toString());
                            writeTag(osI, 8, "invTotOtherDebitAmount", pd.getInvTotOtherDebitAmount().toString());
                            writeTag(osI, 8, "invTotOtherCreditAmount", pd.getInvTotOtherCreditAmount().toString());
                            writeOpenTag(osI, 8, "notes");
                            for (Iterator ix = pd.getNotes().iterator(); ix.hasNext();) {
                                PaymentNoteText note = (PaymentNoteText) ix.next();
                                writeTag(osI, 10, "note", updateNoteLine(note.getCustomerNoteText()));
                            }
                            writeCloseTag(osI, 8, "notes");
                            writeCloseTag(osI, 6, "payment");
                        }
                    }
                    if (wroteCheckHeaderRecords && !isImmediate) {
	                    writeCloseTag(os, 4, "payments");
	                    writeCloseTag(os, 2, "check");
                    }
                    
                    if (wroteImmediateHeaderRecords && isImmediate) {
                        writeCloseTag(osI, 4, "payments");
                        writeCloseTag(osI, 2, "check");	
                    }                    
                }
            }
        }
        catch (IOException ie) {
            LOG.error("extractChecks() Problem reading file:  " + (wroteCheckHeaderRecords ? checkFilename : immediateFilename), ie);
            throw new IllegalArgumentException("Error writing to output file: " + (wroteCheckHeaderRecords ? checkFilename : immediateFilename) + "  " + ie.getMessage());
        }
        finally {
            // Close file
            if (os != null) {
                try {
                	writeCloseTag(os, 0, "checks");
                    os.close();
                   	renameFile(checkFilename, checkFilename + ".NOT_USED");  // An XML file containing these records are NEVER sent to anyone at this time.
                }
                catch (IOException ie) {
                    // Not much we can do now
                	LOG.error("IOException encountered in writeExtractCheckFile.  Message is: " + ie.getMessage());
                }
            }
             if (osI != null) {
            	 try {
            		writeCloseTag(osI, 0, "checks");
            		osI.close();
                   	renameFile(immediateFilename, immediateFilename + ".READY");  // An XML file containing these records are ONLY used for local check printing.
            	 }
                 catch (IOException ie) {
                     // Not much we can do now
                 	LOG.error("IOException encountered in writeExtractCheckFile.  Message is: " + ie.getMessage());
                 }
            }
        }
    }

    // This method is called by the method that generates the XML file for checks to be printed by BNY Mellon
    protected void writeExtractCheckFileMellonBankFastTrack(PaymentStatus extractedStatus, PaymentProcess p, String filename, Integer processId, List<String> notificationEmailAddresses) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss"); //Used in the Fast Track file HEADER record
        SimpleDateFormat sdfPAY1000Rec = new SimpleDateFormat("yyyyMMdd"); //Used in the Fast Track file PAY01000 record
        SimpleDateFormat sdfIDate = new SimpleDateFormat("yyMMdd");    //Used in the issuance file
        SimpleDateFormat sdfITime = new SimpleDateFormat("HHmm");      //Used in the issuance file (NO SECONDS)
        Date processDate = dateTimeService.getCurrentDate();
        BufferedWriter os = null;
        BufferedWriter osI = null;
        String cDelim = "^";  //column delimiter: Per BNY Mellon FastTrack spec, your choices are: "^" or ",".  If you change this make sure you change the associated name on the next line!
        String cDname = "FFCARET";  // column delimiter name: Per BNY Mellon FastTrack spec, your choices are: FFCARET and FFCOMMA for variable record types
        String hdrRecType = "V";  	// record type: Per BNY Mellon's FastTrack spec, can be either V for variable or F for fixed.
        String testIndicator;		// For Mellon Fast Track files - indicates whether the generated file is for (T)est or for (P)roduction
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
        String altAddrAddr3 = "";
    	String altAddrAddr4 = "";
        String altAddrCity = "";
        String altAddrState = "";
        String altAddrZip = "";
        String altCountryName = "";
        String altAddrCityStateZip = "";  // this is needed because the notes combine these into one field
        String altRefQualifer = "";
        int NumOfAltAddressLines = 0;
        int SendToPrefLength = 0;
        String RefDesc1 = "";	// Note lines that are not the alternate address
        String RefDesc2 = "";   // Note lines that are not the alternate address
        String RefDesc3 = "";   // Note lines that are not the alternate address
        String RefDesc4 = "";   // Note lines that are not the alternate address
        String FirstNoteAfterAddressInfo = "";
        String SecondNoteAfterAddressInfo = "";
        String ThirdNoteAfterAddressInfo = "";
        String Ref1Qualifier = "";
        String Ref2Qualifier = "";
        String Ref3Qualifier = "";
        String Ref4Qualifier = "";
        String dateQualifier = "";
        String ftFilename = "";  		// Filename for the Fast Track file generated by this method
        String arFilename = "";  		// Filename for the issuance (or account reconciliation) file generated by this method for immediate payments
        int arNumOfAddIssues = 0;  		// The total number of "add" issues (see record #6)
        int numOfIssuanceRecords = 0;  	// The total number of issuance records
        KualiDecimal arTotalOfAddIssues = KualiDecimal.ZERO; // The total dollar amount of the add issues for the issuance file.
        boolean wroteMellonIssuanceHeaderRecords = false;
        boolean wroteMellonFastTrackHeaderRecords = false;
        CustomerProfile cp = null;
        String sCountryName = "";
        String CheckNumber = "";
        boolean MissingCommaFromSpecialHandlingAddress = false;
                
        int totalRecordCount = 0;
        KualiDecimal totalPaymentAmounts = KualiDecimal.ZERO; 
        
        try {
        	// Establish whether we are in a TEST or PRODUCTION environment.  This will change the indicator in the Mellon header record
        	if (isProduction())
        		testIndicator = "P";
        	else
        		testIndicator = "T";
        	
            // Change the filename so that it ends in .txt instead of .xml
            ftFilename = filename.replace(".xml", ".txt");
            arFilename = ftFilename.replace("check", "check_immediate");

            //  Obtain the bank account number from the check information provided
            List<String> bankCodes1 = paymentGroupService.getDistinctBankCodesForProcessAndType(processId, PdpConstants.DisbursementTypeCodes.CHECK);
            if (!bankCodes1.isEmpty()) {
	            List<Integer> disbNbrs1 = paymentGroupService.getDisbursementNumbersByDisbursementTypeAndBankCode(processId, PdpConstants.DisbursementTypeCodes.CHECK, bankCodes1.get(0));
	            if (!disbNbrs1.isEmpty()) {
		            Iterator<PaymentDetail> myPds = paymentDetailService.getByDisbursementNumber(disbNbrs1.get(0));
		            if (myPds.hasNext()) {
		            	PaymentDetail myPd = myPds.next();
		                PaymentGroup myPg = myPd.getPaymentGroup();
		                if (ObjectUtils.isNotNull(myPg)) {
			                ourBankAccountNumber = myPg.getBank().getBankAccountNumber().replace("-", "");
			                ourBankRoutingNumber = myPg.getBank().getBankRoutingNumber();	
		                }
		                else {
		                	LOG.error("No Payment group information exists for requisition number: " + myPd.getRequisitionNbr() + ".  Payee name is: " + myPg.getPayeeName());
		                	throw new Exception("No Payment group information exists for requisition number: " + myPd.getRequisitionNbr() + ".  Payee name is: " + myPg.getPayeeName());
		                }
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
                        
                        // We save these values to the DB AFTER we know that this check has passed all validation and has actually been written
                        pg.setDisbursementDate(new java.sql.Date(processDate.getTime()));
                        pg.setPaymentStatus(extractedStatus);
                        
                        //Get immediate (a.k.a. local print, a.k.a. manual) check indicator
                        immediateCheckCode = pg.getProcessImmediate();
                        
                        // If it exists, get the check (a.k.a. disbursement number)
                        CheckNumber = (ObjectUtils.isNotNull(pg.getDisbursementNbr()) ? pg.getDisbursementNbr().toString() : "" );	
                        
                        // Parse Notes
                        Iterator<PaymentNoteText> ix = pd.getNotes().iterator();
                        NumOfAltAddressLines = 0;
                        altAddrSendTo = "";
                        altAddrAddr1 = "";
                        altAddrAddr2 = "";
                        altAddrAddr3 = "";
                        altAddrAddr4 = "";
                        altAddrCity = "";
                        altAddrState = "";
                        altAddrZip = "";
                        altCountryName = "";
                        altAddrCityStateZip = "";
                        PreparerInfoText = "";
                        SendToPrefLength = 0;
                        FirstNoteAfterAddressInfo="";
                        SecondNoteAfterAddressInfo="";
                        ThirdNoteAfterAddressInfo="";
                        MissingCommaFromSpecialHandlingAddress = false;
                        
                        while (ix.hasNext()) {
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
                        		if (NoteLine.contains(",")) {
	                        		altAddrCityStateZip = NoteLine.substring(SendToPrefLength);
	                        		String sTemp = altAddrCityStateZip;	//sTemp will be the string we modify as we go along
	                        		
	                        		int spaceForZip = 0;
	                        		spaceForZip = sTemp.lastIndexOf(" ");
	                        		if (spaceForZip == -1) {
	                        			altAddrZip = "";		//ZIP is missing so set it to "" per discussion with Marcia
	                        			LOG.warn("WARNING: No zip code was provided.  Changing it to blanks for check number: " + CheckNumber);
	                        		}
	                        		else {
	                        			if (sTemp.substring(spaceForZip).trim().toLowerCase().equals("null")) {
	                        				altAddrZip = sTemp.substring(spaceForZip);  //Use the space between the state and zip to obtain the "null" zip
	                        				sTemp = sTemp.replace(altAddrZip, "");  	//Remove the "null" zip characters from sTemp
		                        			altAddrZip = "";		// Since ZIP is missing set it to "" per discussion with Marcia
		                        			LOG.warn("WARNING: No zip code was provided.  Changing it to blanks for check number: " + CheckNumber);
	                        			}
	                        			else {
		                        			altAddrZip = sTemp.substring(spaceForZip);  //Use the space between the state and zip to obtain the zip
			                        		sTemp = sTemp.replace(altAddrZip, "");  	//Remove the space + Zip characters from sTemp
			                        		altAddrZip = altAddrZip.trim();  			// Trim any leading or trailing blanks
	                        			}
	                        		}
	                        		
	                        		altAddrState = sTemp.substring(sTemp.lastIndexOf(",") + 1); // Use the comma to find the where the state starts
	                        		sTemp = sTemp.replace(altAddrState, "");  	//Remove what we found from sTemp
	                        		altAddrState = altAddrState.trim();  		// Trim any leading or trailing blanks
	                        		altAddrCity = sTemp.replace(",", "");  		// There should only be one comma.  If commas are in the city name, that will be an issue
	                        		altAddrCity = altAddrCity.trim();  			//Trim any leading or trailing blanks.
	                        		NumOfAltAddressLines = NumOfAltAddressLines + 1;
                        		}
                        		else {
                        			// We can't find a comma, so we won't know where the city ends and the state begins so as per discussion,
                        			//    log it as a warning and move on to the next NoteLine because we may have notes that we want to process.
                        			LOG.warn("WARNING: No comma was provided separating the city and state for check number: " + CheckNumber);
                        			MissingCommaFromSpecialHandlingAddress = true;
                        			continue;
                        		}
                        	}  //if ( NoteLine.contains(DisbursementVoucherConstants.DV_EXTRACT_NOTE_PREFIX_SPECIAL_HANDLING_ADDRESS3) )
                       	
                    		// Retrieve up to 3 subsequent note lines and only the first 72 characters as per the BNY Mellon spec.
                        	else {
                        		//  User typed notes will always be contiguous so once we find the first, we just need to keep grabbing 
                        		//    the next line until we either run out of lines or reach the max number to take (which is 3).
                        		if (NoteLine.length() >= 2) {
                            		if (NoteLine.substring(0,2).contains(DisbursementVoucherConstants.DV_EXTRACT_TYPED_NOTE_PREFIX_IDENTIFIER)) {
                            			// Trim the first two characters from the note and assign it as the first user typed note line
                            			NoteLine = NoteLine.substring(2);
    	                        		FirstNoteAfterAddressInfo = (NoteLine.length() <= 72) ? NoteLine : NoteLine.substring(0,72);
    	                        		
		                        		// See if we have a second user typed note line.  If so, then get it.
		                        		if (ix.hasNext()) {
		                            		note = (PaymentNoteText) ix.next();
		                                	NoteLine = note.getCustomerNoteText();
		                                	if (NoteLine.length() >=2) {
		                                		if (NoteLine.substring(0,2).contains(DisbursementVoucherConstants.DV_EXTRACT_TYPED_NOTE_PREFIX_IDENTIFIER)) {
		                                			NoteLine = NoteLine.substring(2);
		                                			SecondNoteAfterAddressInfo = (NoteLine.length() <= 72) ? NoteLine : NoteLine.substring(0,72);
		                                	
				                                	// Try to get the third user typed note line
				                            		if (ix.hasNext()) {
				                                		note = (PaymentNoteText) ix.next();
				                                    	NoteLine = note.getCustomerNoteText();
				                                    	if (NoteLine.length() >=2) {
				                                    		if (NoteLine.substring(0,2).contains(DisbursementVoucherConstants.DV_EXTRACT_TYPED_NOTE_PREFIX_IDENTIFIER)) {
				                                    			NoteLine = NoteLine.substring(2);
				                                    			ThirdNoteAfterAddressInfo = (NoteLine.length() <= 72) ? NoteLine : NoteLine.substring(0,72);
				                                    			break;  // Break here because the Mellon spec only allows us to use the first three user typed note lines
				                                    		}
				                                    		else break;  // Since we're on our potentially last note if this isn't a user types note, then we're done with the while loop
				                                    	}
				                                    	else break;  // Since this is the last potential user note and it doesn't contain :: break out of the while loop
				                            		}
				                            		else break;  // Out of all notes, so break out of the while loop
		                                		}
		                                		else break; // Getting here means that we ran into a note line that doesn't have the :: in front.  Continuing will over write the first notes we did capture, so 
		                                	}
		                                	else break;  // Interspersed system generated notes can't occur (yet) so break out since we've already processed 1 user entered note.
		                        		}
		                        		else break;  // We've processed the first user entered note, but now we find a system generated note, so break out since this would mean that user notes are done as they are contiguous
                            		} 
                            		else continue;  // Getting here means this is not a user note and since we haven't found the first user note, keep looking
                            	}
                        		else continue;  //  Getting here means this is not a user note, so since we still haven't found the first user note keep on looking.
                        	}  //else  (user notes section of this code)
                        } // while (ix.hasNext())                        
                        
                        if (MissingCommaFromSpecialHandlingAddress)
                        	break;
                        
                        // Get customer profile information
                        if (ObjectUtils.isNotNull(pg.getBatch())) {
                            cp = pg.getBatch().getCustomerProfile();
                            if (ObjectUtils.isNotNull(cp))
                            	if (ObjectUtils.isNotNull(cp.getSubUnitCode()))
                            		subUnitCode = cp.getSubUnitCode();
                            	else {
                            		LOG.error("No Sub Unit Code provided for requisition number: " + pd.getRequisitionNbr());
                            		break;
                            	}
                            else {
                            	LOG.error("No customer profile exists for payee name: " + pg.getPayeeName());
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
	                            		cDname + cDelim +                     // Delimiter name - Must be either FFCARET or FFCOMMA.  Others are allowed but BNY Mellon will have to be contacted first.
	                            		"CORNELLUNIVKFS" + cDelim +           // Customer Id - a unique identifier for the customer
	                            		testIndicator + cDelim +              // Test Indicator:  T = Test run, P = Production Run
	                            		"820" + cDelim +                      // EDI Document Id (3 Bytes)
	                            		"043000261" + cDelim +                // Our Mellon bank id (15 Bytes)
	                            		cDelim +                              // Customer Division Id - 35 bytes - Optional
	                            		sdf.format(processDate) + cDelim +    // File Date and Time - 14 Bytes
	                            		cDelim +                              // Reserved Field - 3 Bytes
	                                    "\n");                                // Filler - 872 bytes
	                            
	        	                //KFSPTS-1460: Start of parameterized email notification changes
	        	                totalRecordCount = 1;          	                
	        	                //Write the Fast Track email records (FIL00020) once for each file
	        	                for (Iterator<String> emailIter = notificationEmailAddresses.iterator(); emailIter.hasNext();) {
	        	                    String emailAddress = emailIter.next();
	        	                    
	        	                    //write (FIL00020) record for each email address
	            	            	os.write("FIL00020" + cDelim + 
	            	            			emailAddress + cDelim +
	            	            			cDelim +
	            	            			"\n");
	            	            	
	            	            	totalRecordCount = totalRecordCount + 1;  
	        	                }        	                
	        	                //KFSPTS-1460: End of parameterized email notification changes
	        	                
	        	                /*KFSPTS-1460: Replaced this hardcoded section with parameterized the email addresses
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
	                        	
	                        	totalRecordCount = 5;  // We've successfully written 1 header record and 4 email records.
	                        	KFSPTS-1460*/
	                        	wroteMellonFastTrackHeaderRecords = true;
                        	}
                        	
                            // Get country name
                        	int CountryNameMaxLength = 15;
                            Country country = countryService.getByPrimaryId(pg.getCountry());
                            if (country != null)
                            	sCountryName = country.getPostalCountryName().substring(0,((country.getPostalCountryName().length() >= CountryNameMaxLength)? CountryNameMaxLength: country.getPostalCountryName().length() ));
                            else
                            	if (ObjectUtils.isNotNull(pg.getCountry()))
                            		sCountryName = pg.getCountry().substring(0,((pg.getCountry().length() >= CountryNameMaxLength)? CountryNameMaxLength: pg.getCountry().length() ));
                            	else
                            		sCountryName = "";  // Do this only if both the getPostalCountryName() AND getCountry() are empty
                            
                            // Do final country name processing per requirements from BNY Mellon.
	                    	if (sCountryName.toUpperCase().contains("UNITED STATES"))
	                    		sCountryName = "";
                            
                            //Get special handling indicator
                            specialHandlingCode = pg.getPymtSpecialHandling();
                             
                            //Get attachment indicator
                            attachmentCode = pg.getPymtAttachment();                            
                            
                            //Determines the division code based on special handling indicator and attachment indicator
                            int dvCodeInt = 0;
                            if (specialHandlingCode == false && attachmentCode == false){
                                dvCodeInt = PdpConstants.DivisionCodes.US_MAIL;
                            }
                            if (specialHandlingCode == true && attachmentCode == false){
                                dvCodeInt = PdpConstants.DivisionCodes.US_MAIL;
                            }
                            if (specialHandlingCode == false && attachmentCode == true){
                                dvCodeInt = PdpConstants.DivisionCodes.CU_MAIL_SERVICES;
                            }
                            if (specialHandlingCode == true && attachmentCode == true){
                                dvCodeInt = PdpConstants.DivisionCodes.CU_MAIL_SERVICES;
                            }

                            divisionCode = String.format(String.format("%%0%dd", 3), dvCodeInt);
                            
                            Date DisbursementDate;
                            if (ObjectUtils.isNotNull(pg.getDisbursementDate()))
                            	DisbursementDate = pg.getDisbursementDate();
                            else {
                            	LOG.error("Disbursement Date is NULL for Disbursement Number: " + CheckNumber);
                            	break;
                            }
                            
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
                        			"2150532082" + cDelim+                              // Originating company identifier - 10 bytes  This has to be different for this file than it is for ACH payments per BNY Mellon
                        			cDelim +                                            // Receiving bank id qualifier - 2 bytes
                        			cDelim +                                            // Receiving bank id - 12 bytes
                        			cDelim +                                            // Receiving account number qualifier - 3 bytes
                        			cDelim +                                            // Receiving account number - 35 bytes
                        			sdfPAY1000Rec.format(DisbursementDate) + cDelim +   // Effective date - 8 bytes - YYMMDD format
                        			cDelim +                                            // Business function code - 3 bytes 
                        			CheckNumber + cDelim +       						// Trace number (check number) - 50 bytes
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
                            os.write("PDT02010" + cDelim +                        		// Record Type - 8 bytes
                            		"PR" + cDelim +                               		// Name qualifier - 3 bytes (PR = Payer)
                            		cDelim +                                      		// ID code qualifier - 2 bytes
                            		cDelim +                                      		// ID code - 80 bytes
                            		"Cornell University" + cDelim +               		// Name - 35 bytes
                            		cDelim +                                      		// Additional name 1 - 60 bytes
                            		cDelim +                                      		// Additional name 2 - 60 bytes
                            		"Division of Financial Affairs" + cDelim +   		// Address line 1 - 35 bytes
                            		"341 Pine Tree Road" + cDelim +               		// Address line 2 - 35 bytes
                            		cDelim +                                      		// Address line 3 - 35 bytes
                            		cDelim +                                      		// Address line 4 - 35 bytes
                            		cDelim +                                      		// Address line 5 - 35 bytes
                            		cDelim +                                      		// Address line 6 - 35 bytes
                            		"Ithaca" + cDelim +                           		// City - 30 bytes
                            		"NY" + cDelim +                               		// State/Province - 2 bytes
                            		"148502820" + cDelim +                        		// Postal code - 15 bytes
                            		cDelim +                                      		// Country code - 3 bytes
                            		cDelim +                                      		// Country name - 30 bytes
                            		cDelim +                                      		// Ref qualifier 1 - 3 bytes
                            		cDelim +                                      		// Ref ID 1 - 50 bytes
                            		cDelim +                                      		// Ref description 1 - 80 bytes
                            		cDelim +                                      		// Ref qualifier 1 - 3 bytes
                            		cDelim +                                      		// Ref ID 1 - 50 bytes
                            		cDelim +                                      		// Ref description 1 - 80 bytes
                            		cDelim +  "\n");
                            
                            // Write the Fast Track initial payee detail (PDT02010) record
                            int AddrMaxLength = 35;
                            int CityMaxLength = 30;
                            int StateMaxLength = 2;
                            int ZipMaxLength = 15;
                            int PayeeNameMaxLength = 35;
                            int PayeeIdMaxLength = 18;
                            String PayeeName = "";
                            String PayeeId = "";
                            String PayeeIdQualifier = "";
                            String AddrLine1 = "";
                            String AddrLine2 = "";
                            String AddrLine3 = "";
                            String AddrLine4 = "";
                            String AddrCity = "";
                            String AddrState = "";
                            String AddrZip = "";
                            
                            if (ObjectUtils.isNotNull(pg.getPayeeName()))
                            	PayeeName = pg.getPayeeName().substring(0,((pg.getPayeeName().length() >= PayeeNameMaxLength)? PayeeNameMaxLength: pg.getPayeeName().length() ));
                            if (ObjectUtils.isNotNull(pg.getPayeeId())) {
                            	PayeeId = pg.getPayeeId().substring(0,((pg.getPayeeId().length() >= PayeeIdMaxLength)? PayeeIdMaxLength: pg.getPayeeId().length() ));
                            	PayeeIdQualifier = "ZZ";                            	
                            }
                            if (ObjectUtils.isNotNull(pg.getLine1Address()))
                            	AddrLine1 = pg.getLine1Address().substring(0,((pg.getLine1Address().length() >= AddrMaxLength)? AddrMaxLength: pg.getLine1Address().length() ));
                            if (ObjectUtils.isNotNull(pg.getLine2Address()))
                            	AddrLine2 = pg.getLine2Address().substring(0,((pg.getLine2Address().length() >= AddrMaxLength)? AddrMaxLength: pg.getLine2Address().length() ));
                            if (ObjectUtils.isNotNull(pg.getLine3Address()))
                            	AddrLine3 = pg.getLine3Address().substring(0,((pg.getLine3Address().length() >= AddrMaxLength)? AddrMaxLength: pg.getLine3Address().length() ));
                            if (ObjectUtils.isNotNull(pg.getLine4Address()))
                            	AddrLine4 = pg.getLine4Address().substring(0,((pg.getLine4Address().length() >= AddrMaxLength)? AddrMaxLength: pg.getLine4Address().length() ));
                            if (ObjectUtils.isNotNull(pg.getCity()))
                            	AddrCity = pg.getCity().substring(0,((pg.getCity().length() >= CityMaxLength)? CityMaxLength: pg.getCity().length() ));
                            if (ObjectUtils.isNotNull(pg.getState()))
                            	AddrState = pg.getState().substring(0,((pg.getState().length() >= StateMaxLength)? StateMaxLength: pg.getState().length() ));
                            if (ObjectUtils.isNotNull(pg.getZipCd()))
                            	AddrZip = (pg.getZipCd().substring(0,((pg.getZipCd().length() >= ZipMaxLength)? ZipMaxLength: pg.getZipCd().length() ))).replace("-", "");

                            os.write("PDT02010" + cDelim +				// Record Type - 8 bytes
                            		"PE" + cDelim +						// Name qualifier - 3 bytes (PE = Payee) This record's data prints on the check
                            		PayeeIdQualifier + cDelim +			// ID code qualifier - 2 bytes
                            		PayeeId + cDelim +					// ID code - 80 bytes
                            		PayeeName + cDelim +				// Name - 35 bytes
                            		cDelim +							// Additional name 1 - 60 bytes
                            		cDelim +							// Additional name 2 - 60 bytes
                            		AddrLine1 + cDelim +    			// Address line 1 - 35 bytes
                            		AddrLine2 + cDelim +    			// Address line 2 - 35 bytes
                            		AddrLine3 + cDelim +    			// Address line 3 - 35 bytes
                            		AddrLine4 + cDelim +    			// Address line 4 - 35 bytes
                            		cDelim +							// Address line 5 - 35 bytes
                            		cDelim +							// Address line 6 - 35 bytes
                            		AddrCity + cDelim +					// City - 30 bytes
                            		AddrState + cDelim +				// State/Province - 2 bytes
                            		AddrZip + cDelim +					// Postal code - 15 bytes
                            		cDelim +							// Country code - 3 bytes
                            		sCountryName + cDelim +				// Country name - 15 bytes (do not use if Country Code is used)
                               		cDelim +							// Ref qualifier 1 - 3 bytes (not used)
                            		cDelim +							// Ref ID 1 - 50 bytes (not used)
                            		cDelim +							// Ref description 1 - 80 bytes (not used)
                            		cDelim +							// Ref qualifier 1 - 3 bytes (not used)
                            		cDelim +							// Ref ID 1 - 50 bytes (not used)
                            		cDelim +							// Ref description 1 - 80 bytes (not used)
                            		cDelim + "\n");
                            
                            // If no alternate address is provided, we must populate with the customer address
                            if (NumOfAltAddressLines == 0) {
                            	// If we were not provided an alternate address to mail the check to, then assign the same values 
                            	//   in the third PDT02010 record the same exact values as the second PDT02010 record.
                            	altAddrSendTo = PayeeName; 
                            	altAddrAddr1 = AddrLine1;
                            	altAddrAddr2 = AddrLine2;
                            	altAddrAddr3 = AddrLine3;
                            	altAddrAddr4 = AddrLine4;
                            	altAddrCity = AddrCity;
                            	altAddrState = AddrState;
                            	altAddrZip = AddrZip;
                            	altAddrCityStateZip = altAddrCity + ", " + altAddrState + " " + altAddrZip;
                            	altCountryName = sCountryName;
                           		altRefQualifer = "ZZ";
                           }
                            else {
                            	altAddrZip = altAddrZip.replace("-", "");
                            	altCountryName = "";	// If we have two addresses, an original and an alternate, make sure the alternate does not inherit the original's country name!
                            }
                            // Write the Fast Track second payee detail (PDT02010) record (for alternate addressing (special handling addressing case)
                            os.write("PDT02010" + cDelim +             // Record Type - 8 bytes
                            		"FE" + cDelim +                    // Name qualifier - 3 bytes (FE = Remit) This record's data prints on the remittance
                            		PayeeIdQualifier + cDelim +		   // ID code qualifier - 2 bytes
                            		PayeeId + cDelim +			       // ID code - 80 bytes
                            		altAddrSendTo + cDelim +           // Name - 35 bytes
                            		cDelim +                           // Additional name 1 - 60 bytes
                            		cDelim +                           // Additional name 2 - 60 bytes
                            		altAddrAddr1 + cDelim +            // Address line 1 - 35 bytes
                            		altAddrAddr2 + cDelim +            // Address line 2 - 35 bytes
                            		altAddrAddr3 + cDelim +            // Address line 3 - 35 bytes
                            		altAddrAddr4 + cDelim +            // Address line 4 - 35 bytes
                            		cDelim +                           // Address line 5 - 35 bytes
                            		cDelim +                           // Address line 6 - 35 bytes
                            		altAddrCity + cDelim +             // City - 30 bytes
                            		altAddrState + cDelim +            // State/Province - 2 bytes
                            		altAddrZip + cDelim +              // Postal code - 15 bytes
                            		cDelim +                           // Country code - 3 bytes
                            		altCountryName + cDelim +		   // Country name - 30 15 bytes (do not use if Country Code is used)  Alternate addresses are never outside the USA.
                               		cDelim +                           // Ref qualifier 1 - 3 bytes
                            		cDelim +                           // Ref ID 1 - 50 bytes
                            		cDelim +                           // Ref description 1 - 80 bytes
                            		cDelim +                           // Ref qualifier 1 - 3 bytes
                            		cDelim +                           // Ref ID 1 - 50 bytes
                            		cDelim +                           // Ref description 1 - 80 bytes
                            		cDelim +  "\n");

                            totalRecordCount = totalRecordCount + 4;  // One for the PAY01000 record and three for the PDT02010 records
                            first = false;		// Set this here so it is only executed once per payee
                        }  // if (first && !immediateCheckCode)
                        
                        // Write the Fast Track REM03020 records
                        String remittanceIdCode="";    // Up to 3 characters
                        String remittanceIdText = "";  // Up to 22 characters
                        
                        // Set up data if subUnitCode is DV
                        //   Here we will NOT have an invoice number but we will have an eDoc number and NO PO number
                        if (subUnitCode.equals(DisbursementVoucherConstants.DV_EXTRACT_SUB_UNIT_CODE)) {
                        	remittanceIdCode = "TN";
                        	remittanceIdText = "Doc No:" + pd.getCustPaymentDocNbr();   // Here, we are guaranteed to have a pd.getCustPaymentDocNbr
                            // Assign RefDesc1
                   			RefDesc1 = "";
                        }
                        
                        // Set up data if subUnitCode is PRAP
                        //   Here we will have an invoice number and an eDoc number and a PO number
                        else if (subUnitCode.equals("PRAP")) {
                        	remittanceIdCode = "IV";
                            remittanceIdText = pd.getInvoiceNbr();		  // Here, we are guaranteed to have a pd.getInvoiceNbr
                            // Assign RefDesc1
                            if (ObjectUtils.isNotNull(pd.getPurchaseOrderNbr()))
                            	RefDesc1 = "PO:" + pd.getPurchaseOrderNbr() + ", Doc No:" + pd.getCustPaymentDocNbr();
                            else
                            	RefDesc1 = "Doc No:" + pd.getCustPaymentDocNbr();
                        }
                        
                        // Set up data if subUnitCode is another type of Payment Request
                        //   Here we will have an invoice number and an eDoc number but we will NOT have PO number
                        else if (!subUnitCode.equals(DisbursementVoucherConstants.DV_EXTRACT_SUB_UNIT_CODE) && !subUnitCode.equals("PRAP")) {
                        	remittanceIdCode = "IV";
                            remittanceIdText = pd.getInvoiceNbr();		  // Here, we are guaranteed to have a pd.getInvoiceNbr
                            // Assign RefDesc1
                           	RefDesc1 = "Doc No:" + pd.getCustPaymentDocNbr();
                        }

                        // Assign the RefDesc fields.
                        if (!PreparerInfoText.isEmpty())
	                        if (RefDesc1.isEmpty()) {
		                        if (FirstNoteAfterAddressInfo.isEmpty()) {
		                        	if (SecondNoteAfterAddressInfo.isEmpty()) {
		                        		if (ThirdNoteAfterAddressInfo.isEmpty()) {  
		                        			// no notes
			                        		RefDesc1 = PreparerInfoText;			
			                        		RefDesc2 = "";
			                        		RefDesc3 = "";
			                        		RefDesc4 = "";
		                        		}
		                        		else {  
		                        			// Note3
			                        		RefDesc1 = PreparerInfoText;			
			                        		RefDesc2 = ThirdNoteAfterAddressInfo;
			                        		RefDesc3 = "";
			                        		RefDesc4 = "";
		                        		}
		                        	}
		                        	else {  
		                        		if (ThirdNoteAfterAddressInfo.isEmpty()) {
		                        			// Note2
			                        		RefDesc1 = PreparerInfoText;			
			                        		RefDesc2 = SecondNoteAfterAddressInfo;
			                        		RefDesc3 = "";
			                        		RefDesc4 = "";
		                        		}
		                        		else {
		                        			// Note2, Note3
			                        		RefDesc1 = PreparerInfoText;			
			                        		RefDesc2 = SecondNoteAfterAddressInfo;
			                        		RefDesc3 = ThirdNoteAfterAddressInfo;
			                        		RefDesc4 = "";
		                        		}
		                        	}
		                        }
		                        else {  // Note1
		                        	if (SecondNoteAfterAddressInfo.isEmpty()) {
		                        		if (ThirdNoteAfterAddressInfo.isEmpty()) {  
		                        			// Note1
			                        		RefDesc1 = PreparerInfoText;			
			                        		RefDesc2 = FirstNoteAfterAddressInfo;
			                        		RefDesc3 = "";
			                        		RefDesc4 = "";
		                        		}
		                        		else {  
		                        			// Note1, Note3
			                        		RefDesc1 = PreparerInfoText;			
			                        		RefDesc2 = FirstNoteAfterAddressInfo;
			                        		RefDesc3 = ThirdNoteAfterAddressInfo;
			                        		RefDesc4 = "";
		                        		}
		                        	}
		                        	else { //Note2
		                        		if (ThirdNoteAfterAddressInfo.isEmpty()) {  
		                        			// Note1, Note2
			                        		RefDesc1 = PreparerInfoText;			
			                        		RefDesc2 = FirstNoteAfterAddressInfo;
			                        		RefDesc3 = SecondNoteAfterAddressInfo;
			                        		RefDesc4 = "";
		                        		}
		                        		else {  
		                        			// Note1, Note2, Note3
			                        		RefDesc1 = PreparerInfoText;			
			                        		RefDesc2 = FirstNoteAfterAddressInfo;
			                        		RefDesc3 = SecondNoteAfterAddressInfo;
			                        		RefDesc4 = ThirdNoteAfterAddressInfo;
		                        		}
		                        	}
		                        }
	                        }
	                        else  { // RefDesc1 contains text
		                        if (FirstNoteAfterAddressInfo.isEmpty()) {
		                        	if (SecondNoteAfterAddressInfo.isEmpty()) {
		                        		if (ThirdNoteAfterAddressInfo.isEmpty()) {  
		                        			// no notes
			                        		RefDesc2 = PreparerInfoText;
			                        		RefDesc3 = "";
			                        		RefDesc4 = "";
		                        		}
		                        		else {  
		                        			// Note3
			                        		RefDesc2 = PreparerInfoText;			
			                        		RefDesc3 = ThirdNoteAfterAddressInfo;
			                        		RefDesc4 = "";
		                        		}
		                        	}
		                        	else {  
		                        		if (ThirdNoteAfterAddressInfo.isEmpty()) {
		                        			// Note2
			                        		RefDesc2 = PreparerInfoText;			
			                        		RefDesc3 = SecondNoteAfterAddressInfo;
			                        		RefDesc4 = "";
		                        		}
		                        		else {
		                        			// Note2, Note3
			                        		RefDesc2 = PreparerInfoText;			
			                        		RefDesc3 = SecondNoteAfterAddressInfo;
			                        		RefDesc4 = ThirdNoteAfterAddressInfo;
		                        		}
		                        	}
		                        }
		                        else {  // Note1
		                        	if (SecondNoteAfterAddressInfo.isEmpty()) {
		                        		if (ThirdNoteAfterAddressInfo.isEmpty()) {  
		                        			// Note1
			                        		RefDesc2 = PreparerInfoText;			
			                        		RefDesc3 = FirstNoteAfterAddressInfo;
			                        		RefDesc4 = "";
		                        		}
		                        		else {  
		                        			// Note1, Note3
			                        		RefDesc2 = PreparerInfoText;			
			                        		RefDesc3 = FirstNoteAfterAddressInfo;
			                        		RefDesc4 = ThirdNoteAfterAddressInfo;
		                        		}
		                        	}
		                        	else { //Note2
		                        		if (ThirdNoteAfterAddressInfo.isEmpty()) {  
		                        			// Note1, Note2
			                        		RefDesc2 = PreparerInfoText;			
			                        		RefDesc3 = FirstNoteAfterAddressInfo;
			                        		RefDesc4 = SecondNoteAfterAddressInfo;
		                        		}
		                        		else {  
		                        			// Note1, Note2, Note3
			                        		RefDesc2 = PreparerInfoText;			
			                        		RefDesc3 = FirstNoteAfterAddressInfo;
			                        		RefDesc4 = SecondNoteAfterAddressInfo;
		                        		}
		                        	}
		                        }
	                        }
                        else  // PreparerInfoText is empty
	                        if (RefDesc1.isEmpty())
		                        if (FirstNoteAfterAddressInfo.isEmpty()) {
		                        	if (SecondNoteAfterAddressInfo.isEmpty()) {
		                        		if (ThirdNoteAfterAddressInfo.isEmpty()) {  
		                        			// no notes
			                        		RefDesc1 = "";			
			                        		RefDesc2 = "";
			                        		RefDesc3 = "";
			                        		RefDesc4 = "";
		                        		}
		                        		else {  
		                        			// Note3
			                        		RefDesc1 = ThirdNoteAfterAddressInfo;			
			                        		RefDesc2 = "";
			                        		RefDesc3 = "";
			                        		RefDesc4 = "";
		                        		}
		                        	}
		                        	else {  
		                        		if (ThirdNoteAfterAddressInfo.isEmpty()) {
		                        			// Note2
			                        		RefDesc1 = SecondNoteAfterAddressInfo;			
			                        		RefDesc2 = "";
			                        		RefDesc3 = "";
			                        		RefDesc4 = "";
		                        		}
		                        		else {
		                        			// Note2, Note3
			                        		RefDesc1 = SecondNoteAfterAddressInfo;			
			                        		RefDesc2 = ThirdNoteAfterAddressInfo;
			                        		RefDesc3 = "";
			                        		RefDesc4 = "";
		                        		}
		                        	}
		                        }
		                        else {  // Note1
		                        	if (SecondNoteAfterAddressInfo.isEmpty()) {
		                        		if (ThirdNoteAfterAddressInfo.isEmpty()) {  
		                        			// Note1
			                        		RefDesc1 = FirstNoteAfterAddressInfo;			
			                        		RefDesc2 = "";
			                        		RefDesc3 = "";
			                        		RefDesc4 = "";
		                        		}
		                        		else {  
		                        			// Note1, Note3
			                        		RefDesc1 = FirstNoteAfterAddressInfo;			
			                        		RefDesc2 = ThirdNoteAfterAddressInfo;
			                        		RefDesc3 = "";
			                        		RefDesc4 = "";
		                        		}
		                        	}
		                        	else { //Note2
		                        		if (ThirdNoteAfterAddressInfo.isEmpty()) {  
		                        			// Note1, Note2
			                        		RefDesc1 = FirstNoteAfterAddressInfo;			
			                        		RefDesc2 = SecondNoteAfterAddressInfo;
			                        		RefDesc3 = "";
			                        		RefDesc4 = "";
		                        		}
		                        		else {  
		                        			// Note1, Note2, Note3
			                        		RefDesc1 = FirstNoteAfterAddressInfo;			
			                        		RefDesc2 = SecondNoteAfterAddressInfo;
			                        		RefDesc3 = ThirdNoteAfterAddressInfo;
			                        		RefDesc4 = "";
		                        		}
		                        	}
		                        }	                        
	                        else  // RefDesc1 contains text
		                        if (FirstNoteAfterAddressInfo.isEmpty()) {
		                        	if (SecondNoteAfterAddressInfo.isEmpty()) {
		                        		if (ThirdNoteAfterAddressInfo.isEmpty()) {  
		                        			// no notes
			                        		RefDesc2 = "";
			                        		RefDesc3 = "";
			                        		RefDesc4 = "";
		                        		}
		                        		else {  
		                        			// Note3
			                        		RefDesc2 = ThirdNoteAfterAddressInfo;			
			                        		RefDesc3 = "";
			                        		RefDesc4 = "";
		                        		}
		                        	}
		                        	else {  
		                        		if (ThirdNoteAfterAddressInfo.isEmpty()) {
		                        			// Note2
			                        		RefDesc2 = SecondNoteAfterAddressInfo;			
			                        		RefDesc3 = "";
			                        		RefDesc4 = "";
		                        		}
		                        		else {
		                        			// Note2, Note3
			                        		RefDesc2 = SecondNoteAfterAddressInfo;			
			                        		RefDesc3 = ThirdNoteAfterAddressInfo;
			                        		RefDesc4 = "";
		                        		}
		                        	}
		                        }
		                        else {  // Note1
		                        	if (SecondNoteAfterAddressInfo.isEmpty()) {
		                        		if (ThirdNoteAfterAddressInfo.isEmpty()) {  
		                        			// Note1
			                        		RefDesc2 = FirstNoteAfterAddressInfo;			
			                        		RefDesc3 = "";
			                        		RefDesc4 = "";
		                        		}
		                        		else {  
		                        			// Note1, Note3
			                        		RefDesc2 = FirstNoteAfterAddressInfo;			
			                        		RefDesc3 = ThirdNoteAfterAddressInfo;
			                        		RefDesc4 = "";
		                        		}
		                        	}
		                        	else { //Note2
		                        		if (ThirdNoteAfterAddressInfo.isEmpty()) {  
		                        			// Note1, Note2
			                        		RefDesc2 = FirstNoteAfterAddressInfo;			
			                        		RefDesc3 = SecondNoteAfterAddressInfo;
			                        		RefDesc4 = "";
		                        		}
		                        		else {  
		                        			// Note1, Note2, Note3
			                        		RefDesc2 = FirstNoteAfterAddressInfo;			
			                        		RefDesc3 = SecondNoteAfterAddressInfo;
			                        		RefDesc4 = ThirdNoteAfterAddressInfo;
		                        		}
		                        	}
		                        }
                        
                        //  If we have something in RefDesc1 then RefQualifier must be "ZZ" according to Mellon's spec.
                        if (!RefDesc1.isEmpty()) 
                        	Ref1Qualifier = "ZZ";
                        else
                        	Ref1Qualifier = "";
                        
                        //  If we have something in RefDesc2 then RefQualifier must be "ZZ" according to Mellon's spec.
                        if (!RefDesc2.isEmpty()) 
                        	Ref2Qualifier = "ZZ";
                        else
                        	Ref2Qualifier = "";
                        
                        //  If we have something in RefDesc3 then RefQualifier must be "ZZ" according to Mellon's spec.
                        if (!RefDesc3.isEmpty()) 
                        	Ref3Qualifier = "ZZ";
                        else
                        	Ref3Qualifier = "";
                        
                        //  If we have something in RefDesc4 then RefQualifier must be "ZZ" according to Mellon's spec.
                        if (!RefDesc4.isEmpty()) 
                        	Ref4Qualifier = "ZZ";
                        else
                        	Ref4Qualifier = "";
                        
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
                                
                                //Write the BNY Mellon issuance service record
                                osI.write("2" +									//
                                	  repeatThis(" ", 30) + 					//
                              		  "100" + 									//
                              		  "242" + 									//
                              		  "0242" +									//
                              		  "1" +										//
                              		  repeatThis(" ", 200) + "\n"				//
                              		  );
                        		
                                numOfIssuanceRecords = numOfIssuanceRecords + 2;		// 2 issuance records added here.
                        		wroteMellonIssuanceHeaderRecords = true;
                        	}
                        	                  
                        	String arPayeeName = "";
                        	String arLine1Address = "";
                        	if (ObjectUtils.isNotNull(pg.getPayeeName()))
                        		arPayeeName = pg.getPayeeName();
                            if (ObjectUtils.isNotNull(pg.getLine1Address()))
                            	arLine1Address = pg.getLine1Address();
                        		
                        		//Write the BNY Mellon issuance detail (regular format) record
                            
                            CheckNumber = repeatThis("0", 10 - pg.getDisbursementNbr().toString().length()) + pg.getDisbursementNbr().toString();
                            String AmountOfCheck = totalNetAmount.toString().replace(".","");
                            AmountOfCheck = repeatThis("0",10 - AmountOfCheck.length()) + AmountOfCheck;
                            
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
                          		String.format("%-60.60s", arLine1Address.toUpperCase()) + "\n" );				// Payee Line 2 - Payee Name or first line of address (Required) - 60 Bytes
                            
                            arNumOfAddIssues = arNumOfAddIssues + 1;						// Totals the number of add issues across ALL checks issued not just for this one payee.
                            arTotalOfAddIssues = arTotalOfAddIssues.add(totalNetAmount);	// Same as for the count but for the total dollar amount.
                            numOfIssuanceRecords = numOfIssuanceRecords + 1;				// Add to the total number of issuance records

                        } //if (immediateCheckCode)
                        else {  
                        	//All of these are limited to 18 bytes in Fast Track.
                        	String ftNetPayAmount = "";
                        	String ftTotalAmount = "";
                        	String ftDiscountAmt = "";
                        	if (ObjectUtils.isNotNull(pd.getNetPaymentAmount())) {
                        		ftNetPayAmount = pd.getNetPaymentAmount().toString();
                        		if (ftNetPayAmount.length() > 18) {
                        			LOG.error("Net Payment Amount is more than 18 bytes for check number " + CheckNumber);
                        			break;
                        		}
                        	}
                        	
                        	if (ObjectUtils.isNotNull(pd.getOrigInvoiceAmount())) {
                        		ftTotalAmount = pd.getOrigInvoiceAmount().toString();
                        		if (ftTotalAmount.length() > 18) {
                        			LOG.error("Original Invoice Amount is more than 18 bytes for check number " + CheckNumber);
                        			break;
                        		}
                        	}
                        	
                        	if (ObjectUtils.isNotNull(pd.getInvTotDiscountAmount())) {
                        		ftDiscountAmt = pd.getInvTotDiscountAmount().toString();
                        		if (ftDiscountAmt.length() > 18) {
                        			LOG.error("Discount Amount is more than 18 bytes for check number " + CheckNumber);
                        			break;
                        		}
                        	}
                        	
                        	String InvoiceDate = "";
                        	if (ObjectUtils.isNotNull(pd.getInvoiceDate())) {
                        		InvoiceDate = pd.getInvoiceDate().toString().replace("-", "");
                        		dateQualifier = "003";
                        	}
                        	else {
                        		LOG.error("Invoice date is blank for check number " + CheckNumber);
                    			break;
                        	}                        		
                        	
                        	//Write the Fast Track REM03020 record
	                        os.write("REM03020" + cDelim +				// Record type - 8 bytes
	                        		remittanceIdCode + cDelim +         // Remittance qualifier code - 3 bytes
	                        		remittanceIdText + cDelim +         // Remittance ID - 22 bytes
	                        		ftNetPayAmount + cDelim +         	// Net invoice amount - 18 bytes
	                        		ftTotalAmount + cDelim +         	// Total invoice amount - 18 bytes
	                        		ftDiscountAmt + cDelim +     		// Discount amount - 18 bytes
	                        		cDelim +                            // Note 1 - 80 bytes
	                        		cDelim +                            // Note 2 - 80 bytes
	                        		Ref1Qualifier + cDelim +            // Ref qualifier 1
	                        		cDelim +                            // Ref ID 1
	                        		RefDesc1 + cDelim +                 // Ref description 1 (up to 72 bytes)
	                        		Ref2Qualifier + cDelim +            // Ref qualifier 2
	                        		cDelim +                            // Ref ID 2 (not used)
	                        		RefDesc2 + cDelim +                 // Ref description 2 (up to 72 bytes)
	                        		Ref3Qualifier + cDelim +            // Ref qualifier 3
	                        		cDelim +                            // Ref ID 3 (not used)
	                        		RefDesc3 + cDelim +                 // Ref description 3 (up to 72 bytes)
	                        		Ref4Qualifier + cDelim +            // Ref qualifier 4
	                        		cDelim +                            // Ref ID 4 (not used)
	                        		RefDesc4 + cDelim + 				// Ref description 4 (up to 72 bytes)
	                        		dateQualifier + cDelim +            // Date qualifier 1
	                        		InvoiceDate + cDelim +              // Date 1 
	                        		cDelim +                            // Date qualifier 2 (not used)
	                        		cDelim +                            // Date 2 (not used)
	                        		cDelim +                            // Date qualifier 3 (not used)
	                        		cDelim +                            // Date 3 (not used)
	                        		cDelim +                            // Date qualifier 4 (not used)
	                        		cDelim +                            // Date 4 (not used)
	                                "\n");

	                        totalRecordCount = totalRecordCount + 1;	//One for the REM03020 records
                        }
                        
                        // **** Mark this payment detail as processed here once all validation has been completed and records written successfully. ****
                        // Copied from the above XML generating method since this is the method that needs to record what was processed and what was not.
                        if (!testMode) {
                            pg.setDisbursementDate(new java.sql.Date(processDate.getTime()));
                            pg.setPaymentStatus(extractedStatus);
                            this.businessObjectService.save(pg);
                        }                        
                    } //while (paymentDetails.hasNext())
                }  //for (Iterator<Integer> iter = disbNbrs.iterator(); iter.hasNext();)
            }  //for (String bankCode : bankCodes)
            
            // Need to update the total record count here to make sure it includes the trailer record
            totalRecordCount = totalRecordCount + 1;  
            
            if (wroteMellonIssuanceHeaderRecords) {
	            //Write the BNY Mellon issuance service total record
            	String NumOfAddIssues = repeatThis("0",10 - Integer.toString(arNumOfAddIssues).length()) + Integer.toString(arNumOfAddIssues);
                String TotalAmountOfAddIssues = arTotalOfAddIssues.toString().replace(".","");
                TotalAmountOfAddIssues = repeatThis("0",12 - TotalAmountOfAddIssues.length()) + TotalAmountOfAddIssues;
	            osI.write("8" +							//  Record Type
	            		NumOfAddIssues + 				//  Total Number of Add Issues, 10 Bytes, numeric only, right justified, prefixed with zeros as needed to make length
	            		TotalAmountOfAddIssues + 		//  Total Amount of Add Issues, 12 Bytes, numeric only (no decimals) right justified, prefixed with zeros as needed to make length
	            		repeatThis("0", 10) + 			//  Total number of voided checks, 10 Bytes for voided checks which at this point we don't do.
	            		repeatThis("0", 12) +			//  Total amount of voided checks, 12 Bytes for the total voided amounts
	          		  	repeatThis(" ", 197) + "\n"		//  Required Filler
	          		  );
	            numOfIssuanceRecords = numOfIssuanceRecords + 1;		// This is to account for record #8 above
	            
	            //Write the BNY Mellon issuance trailer record
	            numOfIssuanceRecords = numOfIssuanceRecords + 1;		// Need to add an issuance record here to account for the trailer record
	            String numberOfIssuanceRecords = repeatThis("0", 6 - Integer.toString(numOfIssuanceRecords).length()) + Integer.toString(numOfIssuanceRecords);
	            osI.write("9" +							//  Record Type
	            		numberOfIssuanceRecords + 		//  Total Number of Records, 6 bytes, numeric only, right justified, prefixed with zeros
	          		  	repeatThis(" ", 235) + "\n"		//  Required Filler
	          		  );
            }
            if (wroteMellonFastTrackHeaderRecords)
            {
            //Fast Track trailer record
            os.write("TRL09000" + cDelim +             // Record Type
            		totalRecordCount + cDelim +        // Total # of records in the file including header and trailer. 15 bytes numeric only
            		totalPaymentAmounts + cDelim +     // Total amount of all net payments for the file.  25 bytes numeric only
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
                    renameFile(ftFilename,ftFilename + ".READY");  //  Need to do this at the end to indicate that the file is ready after it is closed.
                }
                catch (IOException ie) {
                    // Not much we can do now
                	LOG.error("IOException encountered in writeExtractCheckFileMellonBankFastTrack.  Message is: " + ie.getMessage());
                }
            }  //osI.close();
            if (osI != null) {
                try {
                	osI.close();
                    // Rename the resulting file to have a .READY at the end
                	renameFile(arFilename,arFilename + ".READY");  //  Need to do this at the end to indicate that the file is ready after it is closed.
                }
                catch (IOException ie) {
                    // Not much we can do now
                	LOG.error("IOException encountered in writeExtractAchFile.  Message is: " + ie.getMessage());
                }
            }
        }
    }
        
	protected boolean renameFile(String fromFile, String toFile) {
    	boolean bResult = false;
		try {
        	File f = new File(fromFile);
        	f.renameTo(new File(toFile));
        }
    	catch (Exception ex){
    		LOG.error("renameFile Exception: " + ex.getMessage());
    		LOG.error("fromFile: " + fromFile + ", toFile: " + toFile);
    	}
    	return bResult;
    }
    // This utility function produces a string of (s) characters (n) times.
    protected String repeatThis(String s, int n){
    	return  String.format(String.format("%%0%dd", n), 0).replace("0",s);
    }
        
    /*
     * KFSPTS-1460: 
     * Re-factored the code so that common pieces could be used by both ExtractPaymentServiceImpl.writeExtractAchFile and
     * AchBundlerExtractPaymentServiceImpl.writeExtractBundledAchFile
     * --Added the call to writePayeeSpecificsToAchFile for re-factored code
     * --Added the call to writePaymentDetailToAchFile for re-factored code
     */
    protected void writeExtractAchFile(PaymentStatus extractedStatus, String filename, Date processDate, SimpleDateFormat sdf) {
    	    	
    	BufferedWriter os = null;
        try {
        	
        	//KFSPTS-1460: parameterized the hard coded email addresses
        	List<String> notificationEmailAddresses = this.getBankPaymentFileNotificationEmailAddresses();  
        	
            // Writes out the BNY Mellon Fast Track formatted file for ACH payments.  We need to do this first since the status is set in this method which
        	//   causes the writeExtractAchFileMellonBankFastTrack method to not find anything.
        	writeExtractAchFileMellonBankFastTrack(extractedStatus, filename, processDate, sdf, notificationEmailAddresses);

            // totals for summary
            Map<String, Integer> unitCounts = new HashMap<String, Integer>();
            Map<String, KualiDecimal> unitTotals = new HashMap<String, KualiDecimal>();

            Iterator iter = paymentGroupService.getByDisbursementTypeStatusCode(PdpConstants.DisbursementTypeCodes.ACH, PdpConstants.PaymentStatusCodes.PENDING_ACH);
            if (iter.hasNext()) {
                os = new BufferedWriter(new FileWriter(filename));
                os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                writeOpenTag(os, 0, "achPayments");
	            while (iter.hasNext()) {
	                PaymentGroup paymentGroup = (PaymentGroup) iter.next();
	   				if (!testMode) {
	                    paymentGroup.setDisbursementDate(new java.sql.Date(processDate.getTime()));
	                    paymentGroup.setPaymentStatus(extractedStatus);
	                    businessObjectService.save(paymentGroup);
	                }
	   				writePayeeSpecificsToAchFile(os, paymentGroup, processDate, sdf);   //KFSPTS-1460: re-factored
	
	                // Write all payment level information
	                writeOpenTag(os, 4, "payments");
	                List pdList = paymentGroup.getPaymentDetails();
	                for (Iterator iterator = pdList.iterator(); iterator.hasNext();) {
	                    PaymentDetail paymentDetail = (PaymentDetail) iterator.next();
	                    writePaymentDetailToAchFile(os, paymentGroup, paymentDetail, unitCounts, unitTotals, sdf);  //KFSPTS-1460: re-factored
	                }
	
	                writeCloseTag(os, 4, "payments");
	                writeCloseTag(os, 2, "ach");  //open for this tag is in method writePayeeSpecificsToAchFile
	            }
	            writeCloseTag(os, 0, "achPayments");
	
	            // send summary email
	            paymentFileEmailService.sendAchSummaryEmail(unitCounts, unitTotals, dateTimeService.getCurrentDate());
            }
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
                    renameFile(filename, filename + ".NOT_USED");  //  Need to do this at the end to indicate that the file is NOT USED after it is closed.
                }
                catch (IOException ie) {
                    // Not much we can do now
                	LOG.error("IOException encountered in writeExtractAchFile.  Message is: " + ie.getMessage());
                }
            }
        }
    }
    
    protected String updateNoteLine(String noteLine) {
        //  Had to add this code to check for and remove the colons (::) that were added in 
        //   DisbursementVoucherExtractServiceImpl.java line 506 v4229 if they exist.  If not
    	//   then just return what was sent.  This was placed in a method as it is used in
    	//   two locations in this class

    	if (noteLine.length() >= 2)
        	if (noteLine.substring(0,2).contains(DisbursementVoucherConstants.DV_EXTRACT_TYPED_NOTE_PREFIX_IDENTIFIER))
        		noteLine = noteLine.substring(2);
        
        return noteLine;
    }
    
    /**
     * KFSPTS-1460:
     * Obtains the notification email addresses to include in the bank payment files from the system parameters.
     * @return
     */
    protected List<String> getBankPaymentFileNotificationEmailAddresses() {
    	
    	String emailAddressesStr = ""; 
    	
    	try {
            emailAddressesStr = parameterService.getParameterValue(KFSParameterKeyConstants.KFS_PDP, KFSParameterKeyConstants.ALL_COMPONENTS, KFSParameterKeyConstants.BANK_PAYMENT_FILE_EMAIL_NOTIFICATION);
        } catch(Exception e) {
            LOG.error("ExtractPaymentServiceImpl.getBankPaymentFileNotificationEmailAddresses: The " + KFSParameterKeyConstants.KFS_PDP + ":" + KFSParameterKeyConstants.ALL_COMPONENTS + ":" + KFSParameterKeyConstants.BANK_PAYMENT_FILE_EMAIL_NOTIFICATION + "system parameter was not found registered in the system.");
        }
    	
    	List<String> emailAddressList = Arrays.asList(emailAddressesStr.split(";"));
    	
    	return emailAddressList;
    }
    
 
    //KFSPTS-1460: 
    // Adjusted the code in this method to deal with the output of the payment details based upon 
    //payee with multiple payments in the same payment group.
    protected void writeExtractAchFileMellonBankFastTrack(PaymentStatus extractedStatus, String filename, Date processDate, SimpleDateFormat sdf, List<String> notificationEmailAddresses) {
        BufferedWriter os = null;
        sdf = new SimpleDateFormat("yyyyMMddHHmmss"); //Used in the Fast Track file HEADER record
        SimpleDateFormat sdfPAY1000Rec = new SimpleDateFormat("yyyyMMdd"); //Used in the Fast Track file PAY01000 record
        String cDelim = "^";  //column delimiter: Per BNY Mellon FastTrack spec, your choices are: "^" or ",".  If you change this make sure you change the associated name on the next line!
        String cDname = "FFCARET";  // column delimiter name: Per BNY Mellon FastTrack spec, your choices are: FFCARET and FFCOMMA for variable record types
        String hdrRecType = "V";    // record type: Per BNY Mellon's FastTrack spec, can be either V for variable or F for fixed.
        String testIndicator;		// For Mellon Fast Track files - indicates whether the generated file is for (T)est or for (P)roduction
        String ourBankAccountNumber = "";
        String ourBankRoutingNumber = "";
        String subUnitCode = "";
        boolean specialHandlingCode = false;
        boolean attachmentCode = false;
        String divisionCode = "";
        String achCode = "";
        String dateQualifer = "";
        boolean wroteFastTrackHeaderRecords = false;
        // Change the filename so that it ends in .txt instead of .xml.
        filename = filename.replace(".xml", ".txt");
        int totalRecordCount = 0;
        KualiDecimal totalPaymentAmounts = KualiDecimal.ZERO;
        CustomerProfile cp = null;
        String sCountryName = "";
    	
    	try {
        	// Establish whether we are in a TEST or PRODUCTION environment.  This will change the indicator in the Mellon header record
        	if (isProduction())
        		testIndicator = "P";
        	else
        		testIndicator = "T";
        	
        	HashSet<String> bankCodes = this.getAchBundlerHelperService().getDistinctBankCodesForPendingAchPayments();
        	
        	for (String bankCode : bankCodes) {
        		HashSet<Integer> disbNbrs = this.getAchBundlerHelperService().getDistinctDisbursementNumbersForPendingAchPaymentsByBankCode(bankCode);
        		
        		for (Iterator<Integer> iter = disbNbrs.iterator(); iter.hasNext();) {
        			Integer disbursementNbr = iter.next();        			
        			boolean first = true;
        			        			
        			//compute total net amount as it is needed on first payment detail
        			KualiDecimal totalNetAmount = new KualiDecimal(0);
        			Iterator<PaymentDetail> payDetailIter = this.getAchBundlerHelperService().getPendingAchPaymentDetailsByDisbursementNumberAndBank(disbursementNbr, bankCode);
        			while (payDetailIter.hasNext()) {
        				PaymentDetail pd = payDetailIter.next();
        				totalNetAmount = totalNetAmount.add(pd.getNetPaymentAmount());
        			}
        			
        			Iterator<PaymentDetail> paymentDetails = this.getAchBundlerHelperService().getPendingAchPaymentDetailsByDisbursementNumberAndBank(disbursementNbr, bankCode);
        			while (paymentDetails.hasNext()) {
        				PaymentDetail pd = paymentDetails.next();
        				PaymentGroup pg = pd.getPaymentGroup();
        				
        				 // Get our Bank Account Number and our bank routing number
                        ourBankAccountNumber = pg.getBank().getBankAccountNumber().replace("-", "");
                        ourBankRoutingNumber = pg.getBank().getBankRoutingNumber();
                        
                        if (!wroteFastTrackHeaderRecords) {                        	
                        	// open the file for writing
                        	os = new BufferedWriter(new FileWriter(filename));
                        	
        	            	//Write the Fast Track header record (FIL00010) once for each file
        	                os.write("FIL00010" + cDelim +                // Record Type
        	                		hdrRecType + cDelim +                 // Variable (V) or Fixed (F) flag
        	                		cDname + cDelim +                     // Delimiter name - Must be either FFCARET or FFCOMMA.  Others are allowed but BNY Mellon will have to be contacted first.
        	                		"CORNELLUNIVKFS" + cDelim +           // Customer Id - a unique identifier for the customer.  This has to be different for ACH than it is for CHECKS per BNY Mellon.
        	                		testIndicator + cDelim +              // Test Indicator:  T = Test run, P = Production Run
        	                		"820" + cDelim +                      // EDI Document Id (3 Bytes)
        	                		"043000261" + cDelim +                // Our Mellon bank id (15 Bytes)
        	                		cDelim +                              // Customer Division Id - 35 bytes - Optional
        	                		sdf.format(processDate) + cDelim +    // File Date and Time - 14 Bytes  YYMMDD format
        	                		cDelim +                              // Reserved Field - 3 Bytes
        	                        "\n");
        	                
        	                //KFSPTS-1460: Start of parameterized email notification changes
        	                totalRecordCount = 1;          	                
        	                //Write the Fast Track email records (FIL00020) once for each file
        	                for (Iterator<String> emailIter = notificationEmailAddresses.iterator(); emailIter.hasNext();) {
        	                    String emailAddress = emailIter.next();
        	                    
        	                    //write (FIL00020) record for each email address
            	            	os.write("FIL00020" + cDelim + 
            	            			emailAddress + cDelim +
            	            			cDelim +
            	            			"\n");
            	            	
            	            	totalRecordCount = totalRecordCount + 1;  
        	                }        	                
        	                //KFSPTS-1460: End of parameterized email notification changes
        	                
        	                /*KFSPTS-1460: Replaced this hardcoded section with parameterized the email addresses.
        	                //Write the Fast Track email records (FIL00020) once for each file
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
                            KFSPTS-1460*/
        	                
        	            	wroteFastTrackHeaderRecords = true;
                        }
                        
                    	// At this point we will start looping through all the checks and write the PAY01000 record (one for each payee), 
                    	// two (2) PDT2010 records (one for the Payer & Payee) and as many REM3020 records as needed for each amount being
                    	// paid to this payee.                        
                        
                        if (first) {
                            // Get country name for code
                            int CountryNameMaxLength = 15;
                            Country country = this.getCountryService().getByPrimaryId(pg.getCountry());
                            if (country != null) {
                            	sCountryName = country.getPostalCountryName().substring(0,((country.getPostalCountryName().length() >= CountryNameMaxLength)? CountryNameMaxLength: country.getPostalCountryName().length() ));
    	                    	if (sCountryName.toUpperCase().contains("UNITED STATES"))
    	                    		sCountryName = "";
                            }
                            else
                            	if (ObjectUtils.isNotNull(pg.getCountry()))
                            		sCountryName = pg.getCountry().substring(0,((pg.getCountry().length() >= CountryNameMaxLength)? CountryNameMaxLength: pg.getCountry().length() ));
    		                    	if (sCountryName.toUpperCase().contains("UNITED STATES"))
    		                    		sCountryName = "";
                            	else
                            		sCountryName = "";
                            
                            int dvCodeInt=0;
                            // Get customer profile information
                            if (ObjectUtils.isNotNull(pg.getBatch())) {
                                cp = pg.getBatch().getCustomerProfile();
                                if (ObjectUtils.isNotNull(cp))
                                	if (ObjectUtils.isNotNull(cp.getSubUnitCode()))
                                		subUnitCode = cp.getSubUnitCode();
                                	else
                                		LOG.error("No Sub Unit Code provided for requisition number: " + pd.getRequisitionNbr());
                                else
                                	LOG.error("No customer profile exists for payee name: " + pg.getPayeeName());
                            }

                            //Get special handling indicator
                            specialHandlingCode = pg.getPymtSpecialHandling();
                             
                            //Get attachment indicator
                            attachmentCode = pg.getPymtAttachment();   
                            
                            if (specialHandlingCode == false && attachmentCode == false){
                                dvCodeInt = PdpConstants.DivisionCodes.US_MAIL;
                            }
                            if (specialHandlingCode == true && attachmentCode == false){
                                dvCodeInt = PdpConstants.DivisionCodes.US_MAIL;
                            }
                            if (specialHandlingCode == false && attachmentCode == true){
                                dvCodeInt = PdpConstants.DivisionCodes.CU_MAIL_SERVICES;
                            }
                            if (specialHandlingCode == true && attachmentCode == true){
                                dvCodeInt = PdpConstants.DivisionCodes.CU_MAIL_SERVICES;
                            }

                            divisionCode = String.format(String.format("%%0%dd", 3), dvCodeInt);
    
                            //Determine the ACH Code to send down.  Here are the rules:
                            // 1.  If they are a vendor and they've selected checking account, the ACH code is CTX (vendors can only have ACH to checking)
                            // 2.  If they are a vendor and they've selected savings account, the ACH code is PPD (sometimes sole proprietors are vendors and they use personal not corporate accounts)
                            // 3.  If they are NOT a vendor, then the ACH code is always PPD (allows employees to have their's deposited in checking or savings)
                            
                            //Determine which ACH code to use: CTX for corporate accounts, or PPD for Personal accounts
                            // String payeeId = paymentGroup.getPayeeId();							// Returns the ID of the payee and is the vendor number if the next var indicates that
                            // String payeeIdTypeDesc = paymentGroup.getPayeeIdTypeDesc();			// Returns "Vendor Number" if the payeeID is a vendor number
                            
                            String AchBankRoutingNbr = "";
                            String AchAccountType = "DA";
                            String AchBankAccountNumber = "";
                            String CheckNumber = "";
                            
                            boolean isVendor = (pg.getPayeeIdTypeCd().equals("V")) ? true : false;	// payeeIdTypeCode returns a "V" if it is a vendor
                            String kfsAccountType = "";
                            
                            if (ObjectUtils.isNotNull(pg.getAchAccountType()))
                            	kfsAccountType = pg.getAchAccountType();							// Returns either a 22 for checking or a 32 for Savings account
                            																		// For Mellon this converts to either DA (checking) or SG (savings)                        
                            if (kfsAccountType.startsWith("22")) {
                                AchAccountType = "DA";
                            }
                            if (kfsAccountType.startsWith("32")) {
                                AchAccountType = "SG";
                            }
                            if (kfsAccountType.contains("PPD")) {
                                achCode = "PPD";
                            }
                            if (kfsAccountType.contains("CTX")) {
                                achCode = "CTX";
                            }
                            
                            if (ObjectUtils.isNotNull(pg.getAchBankRoutingNbr()))
                            	AchBankRoutingNbr = pg.getAchBankRoutingNbr();
                            
                            if (ObjectUtils.isNotNull(pg.getAchAccountNumber().getAchBankAccountNbr()))
                            	AchBankAccountNumber = pg.getAchAccountNumber().getAchBankAccountNbr().toString();
                            
                            if (ObjectUtils.isNotNull(pg.getDisbursementNbr().toString()))
                            	CheckNumber = pg.getDisbursementNbr().toString();
                            
                        	//Write only 1 PAY01000 record for each payee
                        	os.write("PAY01000" + cDelim +                            // Record Type - 8 bytes
                        			"1" + cDelim +                                    // 7=Payment and Electronic Advice (Transaction handling code - 2 bytes)
                        			totalNetAmount.toString() + cDelim +              // Total amount of check (Payment amount - 18 bytes)
                        			"C" + cDelim +                                    // C=Credit, D=Debit (Credit or debit Flag - 1 Byte)
                        			"ACH" + cDelim +                                  // ACH= ACH Payment method - 3 Bytes
                        			achCode + cDelim +                                // PPD is used for ACH payments to a personal bank account - 10 bytes
                        			"01" + cDelim +                                   // Originators bank id qualifier - 2 bytes
                        			ourBankRoutingNumber + cDelim +                   // Originators bank id - 12 bytes
                        			"DA" + cDelim +                                   // Originating account number Qualifier - 3 bytes
                        			ourBankAccountNumber + cDelim +                   // Originating account number - 35 bytes
                        			"1150532082" + cDelim+                            // Originating company identifier - 10 bytes - This has to be different for ACH than it is for CHECKS per BNY Mellon
                        			"01" + cDelim +                                   // Receiving bank id qualifier - 2 bytes
                        			AchBankRoutingNbr + cDelim +					  // Receiving bank id - 12 bytes
                        			AchAccountType + cDelim +                         // Receiving account number qualifier - 3 bytes - must be DA for checking or SG for savings.
                        			AchBankAccountNumber + cDelim + 				  // Receiving account number - 35 bytes
                        			sdfPAY1000Rec.format(processDate) + cDelim +      // Effective date - 8 bytes - YYMMDDHHMMSS
                        			cDelim +                                          // Business function code - 3 bytes 
                        			CheckNumber + cDelim + 							  // Trace number (check number) - 50 bytes
                        			divisionCode + cDelim +                           // Division code - 50 bytes 
                        			cDelim +                                          // Currency code - 3 bytes
                        			cDelim +                                          // Note 1 - 80 bytes
                        			cDelim +                                          // Note 2 - 80 bytes
                        			cDelim +                                          // Note 3 - 80 bytes
                        			cDelim +                                          // Note 4 - 80 bytes
                        			cDelim + "\n");

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
                            		cDelim + "\n");
                            
                            // Write the payee detail (PDT02010) record for the payee (them) (only one per payee)
                            
                            // temp variables to observe length limitations
                            int AddrMaxLength = 35;
                            int CityMaxLength = 30;
                            int StateMaxLength = 2;
                            int ZipMaxLength = 15;
                            int PayeeNameMaxLength = 35;
                            String PayeeName = "";
                            String AddrLine1 = "";
                            String AddrLine2 = "";
                            String AddrLine3 = "";
                            String AddrLine4 = "";
                            String City = "";
                            String State = "";
                            String Zip = "";
                            
                            if (ObjectUtils.isNotNull(pg.getPayeeName()))
                            	PayeeName = pg.getPayeeName().substring(0,((pg.getPayeeName().length() >= PayeeNameMaxLength)? PayeeNameMaxLength: pg.getPayeeName().length() ));
                            if (ObjectUtils.isNotNull(pg.getLine1Address()))
                            	AddrLine1 = pg.getLine1Address().substring(0,((pg.getLine1Address().length() >= AddrMaxLength)? AddrMaxLength: pg.getLine1Address().length() ));
                            if (ObjectUtils.isNotNull(pg.getLine2Address()))
                            	AddrLine2 = pg.getLine2Address().substring(0,((pg.getLine2Address().length() >= AddrMaxLength)? AddrMaxLength: pg.getLine2Address().length() ));
                            if (ObjectUtils.isNotNull(pg.getLine3Address()))
                            	AddrLine3 = pg.getLine3Address().substring(0,((pg.getLine3Address().length() >= AddrMaxLength)? AddrMaxLength: pg.getLine3Address().length() ));
                            if (ObjectUtils.isNotNull(pg.getLine4Address()))
                            	AddrLine4 = pg.getLine4Address().substring(0,((pg.getLine4Address().length() >= AddrMaxLength)? AddrMaxLength: pg.getLine4Address().length() ));
                            if (ObjectUtils.isNotNull(pg.getCity()))
                            	City = pg.getCity().substring(0,((pg.getCity().length() >= CityMaxLength)? CityMaxLength: pg.getCity().length() ));
                            if (ObjectUtils.isNotNull(pg.getState()))
                            	State = pg.getState().substring(0,((pg.getState().length() >= StateMaxLength)? StateMaxLength: pg.getState().length() ));
                            if (ObjectUtils.isNotNull(pg.getZipCd()))
                            	Zip = (pg.getZipCd().substring(0,((pg.getZipCd().length() >= ZipMaxLength)? ZipMaxLength: pg.getZipCd().length() ))).replace("-", "");
                            
                            os.write("PDT02010" + cDelim +      // Record Type - 8 bytes
                            		"PE" + cDelim +             // Name qualifier - 3 bytes
                            		cDelim +                    // ID code qualifier - 2 bytes
                            		cDelim +                    // ID code - 80 bytes
                            		PayeeName + cDelim +       	// Name - 30 bytes
                            		cDelim +                    // Additional name 1 - 60 bytes
                            		cDelim +                    // Additional name 2 - 60 bytes
                            		AddrLine1 + cDelim +    	// Address line 1 - 35 bytes
                            		AddrLine2 + cDelim +    	// Address line 2 - 35 bytes
                            		AddrLine3 + cDelim +    	// Address line 3 - 35 bytes
                            		AddrLine4 + cDelim +    	// Address line 4 - 35 bytes
                            		cDelim +                    // Address line 5 - 35 bytes
                            		cDelim +                    // Address line 6 - 35 bytes
                            		City + cDelim +            	// City - 30 bytes
                            		State + cDelim +           	// State/Province - 2 bytes
                            		Zip + cDelim +           	// Postal code - 15 bytes
                            		cDelim +                    // Country code - 3 bytes
                            		sCountryName + cDelim +     // Country name - 30 bytes  (do not use is Country Code is used)
                               		cDelim +                    // Ref qualifier 1 - 3 bytes
                            		cDelim +                    // Ref ID 1 - 50 bytes
                            		cDelim +                    // Ref description 1 - 80 bytes
                            		cDelim +                    // Ref qualifier 1 - 3 bytes
                            		cDelim +                    // Ref ID 1 - 50 bytes
                            		cDelim +                    // Ref description 1 - 80 bytes
                            		cDelim + "\n");
                           
                            totalRecordCount = totalRecordCount + 3;	//One for the PAY01000 record and one for each PDT02010 record
                            first = false;								// Set this here so it is only executed once per payee
                        }  //If (first)
                        
                        // Write the REM03020 record
                        // Set up remittanceIdCode and remittanceIdText based on whether its a DV or something else.
                        String remittanceIdCode = (subUnitCode.equals(DisbursementVoucherConstants.DV_EXTRACT_SUB_UNIT_CODE)) ? "TN" : "IV" ;
                        String remittanceIdText = (subUnitCode.equals(DisbursementVoucherConstants.DV_EXTRACT_SUB_UNIT_CODE)) ? 
                        		ObjectUtils.isNotNull(pd.getCustPaymentDocNbr()) ? "Doc No:" + pd.getCustPaymentDocNbr() : "" : 
                        			ObjectUtils.isNotNull(pd.getInvoiceNbr()) ? pd.getInvoiceNbr() : "";
                        
                    	//All of these are limited to 18 bytes in Fast Track.
                    	String ftNetPayAmount = "";
                    	String ftTotalAmount = "";
                    	String ftDiscountAmt = "";
                    	if (ObjectUtils.isNotNull(pd.getNetPaymentAmount())) {
                    		ftNetPayAmount = pd.getNetPaymentAmount().toString();
                    		if (ftNetPayAmount.length() > 18) {
                    			LOG.error("Net Payment Amount is more than 18 bytes");
                    			break;
                    		}
                    	}
                    	
                    	if (ObjectUtils.isNotNull(pd.getOrigInvoiceAmount())) {
                    		ftTotalAmount = pd.getOrigInvoiceAmount().toString();
                    		if (ftTotalAmount.length() > 18) {
                    			LOG.error("Original Invoice Amount is more than 18 bytes");
                    			break;
                    		}
                    	}
                    	
                    	if (ObjectUtils.isNotNull(pd.getInvTotDiscountAmount())) {
                    		ftDiscountAmt = pd.getInvTotDiscountAmount().toString();
                    		if (ftDiscountAmt.length() > 18) {
                    			LOG.error("Discount Amount is more than 18 bytes");
                    			break;
                    		}
                    	}
                    	
                    	String InvoiceDate = "";
                    	if (ObjectUtils.isNotNull(pd.getInvoiceDate())) {
                    		InvoiceDate = pd.getInvoiceDate().toString().replace("-", "");
                    		dateQualifer = "003";
                    	}
                    	
                        os.write("REM03020" + cDelim +                                 				// Record type - 8 bytes
                        		remittanceIdCode + cDelim +                            				// Remittance qualifier code - 3 bytes
                        		remittanceIdText + cDelim +                            				// Remittance ID - 50 bytes
                        		ftNetPayAmount + cDelim +         									// Net invoice amount - 18 bytes
                        		ftTotalAmount + cDelim +         									// Total invoice amount - 18 bytes
                        		ftDiscountAmt + cDelim +     										// Discount amount - 18 bytes
                        		cDelim +                 											// Note 1 - 80 bytes - NOT USED PER SPEC
                        		cDelim +                 											// Note 2 - 80 bytes - NOT USED PER SPEC
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
                        		dateQualifer + cDelim +                                				// Date qualifier 1
                        		InvoiceDate + cDelim +                                 				// Date 1 
                        		cDelim +                                               				// Date qualifier 2
                        		cDelim +                                               				// Date 2
                        		cDelim +                                               				// Date qualifier 3
                        		cDelim +                                               				// Date 3
                        		cDelim +                                               				// Date qualifier 4
                        		cDelim + "\n");
                        		
                        totalRecordCount = totalRecordCount + 1;	//One for the REM03020 record                        
        			} //while there are payment details        			
        		} //for each disbNbr        			
        	} // for each bankCode
            
            if (wroteFastTrackHeaderRecords) {
	            // Need to update the total record count here to make sure it includes the trailer record
	            totalRecordCount = totalRecordCount + 1;   
	            //Now write the trailer record
	            os.write("TRL09000" + cDelim +             // Record Type
	            		totalRecordCount + cDelim +        // Total # of records in the file including header and trailer. 15 bytes numeric only
	            		totalPaymentAmounts + cDelim +     // Total amount of all net payments for the file.  25 bytes numeric only
	            		"\n");                             // EOR
            }

        }  // try
        catch (IOException ie) {
            LOG.error("extractAchPayments() Problem reading file:  " + filename, ie);
            throw new IllegalArgumentException("Error writing to output file: " + ie.getMessage());
        }
        catch (Exception ex) {
            LOG.error("General Exception with writeExtractBundledAchFileMellonBankFastTrack().  Error is:  " + ex.getMessage(), ex);
        }
        finally {
            // Close file
            if (os != null) {
                try {
                    os.close();
                    renameFile(filename, filename + ".READY");   //  Need to do this at the end to indicate that the file is ready after it is closed.
                }
                catch (IOException ie) {
                    // Not much we can do now
                	LOG.error("IOException in extractAchPayments():  " + filename, ie);
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

    protected boolean isProduction() {
    	return ConfigContext.getCurrentContextConfig().getProperty(KEWConstants.PROD_DEPLOYMENT_CODE).equalsIgnoreCase(
    			ConfigContext.getCurrentContextConfig().getEnvironment());
        }
    
    
    /*
     * KFSPTS-1460: Added accessor method for extended classes
     */
    protected DateTimeService getDateTimeService() {
        return dateTimeService;
    }
    
    /*
     * KFSPTS-1460: Added accessor method for extended classes
     */
    protected BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }
    
    /*
     * KFSPTS-1460: Added accessor method for extended classes
     */
    protected KualiConfigurationService getKualiConfigurationService() {
        return kualiConfigurationService;
    }
    
    /*
     * KFSPTS-1460: Added accessor method for extended classes
     */
    protected PdpEmailService getPaymentFileEmailService() {
        return paymentFileEmailService;
    }
    
    /*
     * KFSPTS-1460: 
     * New method created due to refactoring the code from ExtractPaymentServiceImpl and AchBundlerExtractPaymnetServiceImpl.
     * Method writes all tags and data for a single payee from open ach to close of customerProfile.
     */
    protected void writePayeeSpecificsToAchFile(BufferedWriter os, PaymentGroup paymentGroup, Date processDate, SimpleDateFormat sdf) throws IOException {
    	
    	try {
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
    	}
    	catch (IOException ioe) {
            LOG.error("writePayeeSpecificsToAchFile(): Problem writing to file - IOException caught and rethrown.");
            throw ioe;
    	}
    	
    }
    
    /*
     * KFSPTS-1460: New method created due to refactoring the code from ExtractPaymentServiceImpl and AchBundlerExtractPaymnetServiceImpl
     */
    protected void writePaymentDetailToAchFile(BufferedWriter os, PaymentGroup paymentGroup, PaymentDetail paymentDetail, Map<String, Integer> unitCounts, Map<String, KualiDecimal> unitTotals, SimpleDateFormat sdf) throws IOException {
    	
    	try {    		
	        writeOpenTag(os, 6, "payment");
	    	
	        // Write detail info
	        writeTag(os, 8, "purchaseOrderNbr", paymentDetail.getPurchaseOrderNbr());
	        writeTag(os, 8, "invoiceNbr", paymentDetail.getInvoiceNbr());
	        writeTag(os, 8, "requisitionNbr", paymentDetail.getRequisitionNbr());
	        writeTag(os, 8, "custPaymentDocNbr", paymentDetail.getCustPaymentDocNbr());
	        writeTag(os, 8, "invoiceDate", sdf.format(paymentDetail.getInvoiceDate()));
	
	        writeTag(os, 8, "origInvoiceAmount", paymentDetail.getOrigInvoiceAmount().toString());
	        writeTag(os, 8, "netPaymentAmount", paymentDetail.getNetPaymentAmount().toString());
	        writeTag(os, 8, "invTotDiscountAmount", paymentDetail.getInvTotDiscountAmount().toString());
	        writeTag(os, 8, "invTotShipAmount", paymentDetail.getInvTotShipAmount().toString());
	        writeTag(os, 8, "invTotOtherDebitAmount", paymentDetail.getInvTotOtherDebitAmount().toString());
	        writeTag(os, 8, "invTotOtherCreditAmount", paymentDetail.getInvTotOtherCreditAmount().toString());
	
	        writeOpenTag(os, 8, "notes");
	        for (Iterator i = paymentDetail.getNotes().iterator(); i.hasNext();) {
	            PaymentNoteText note = (PaymentNoteText) i.next();
	            writeTag(os, 10, "note", updateNoteLine(escapeString(note.getCustomerNoteText())));
	        }
	        writeCloseTag(os, 8, "notes");
	
	        writeCloseTag(os, 6, "payment");
	
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
    	catch (IOException ioe) {
            LOG.error("writePaymentDetailToAchFile(): Problem writing to file - IOException caught and rethrown.");
            throw ioe;
    	}
    }
    
    //KFSPTS-1460
    public AchBundlerHelperService getAchBundlerHelperService() {
        return achBundlerHelperService;
    }

    //KFSPTS-1460
    public void setAchBundlerHelperService(AchBundlerHelperService achBundlerHelperService) {
        this.achBundlerHelperService = achBundlerHelperService;
    }
       
}
