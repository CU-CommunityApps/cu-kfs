
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
package com.rsmart.kuali.kfs.pdp.batch.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.batch.service.impl.ExtractPaymentServiceImpl;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentNoteText;
import org.kuali.kfs.pdp.businessobject.PaymentProcess;
import org.kuali.kfs.pdp.businessobject.PaymentStatus;
import org.kuali.kfs.pdp.service.PdpEmailService;
import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.Config;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.ObjectUtils;
import org.kuali.rice.location.api.country.Country;
import org.springframework.transaction.annotation.Transactional;

import com.rsmart.kuali.kfs.pdp.service.AchBundlerHelperService;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

/**
 * MOD: this class is responsible for detecting if bundled ACH payments (by payee/disb nbr) are desired via the setting of the 
 * system parameter to Y, and if so, bundles the extracted payments.
 */
@Transactional
public class AchBundlerExtractPaymentServiceImpl extends ExtractPaymentServiceImpl {
  
    public static String DV_EXTRACT_SUB_UNIT_CODE = "DV";
    public static String DV_EXTRACT_TYPED_NOTE_PREFIX_IDENTIFIER = "::";
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AchBundlerExtractPaymentServiceImpl.class);
    
    /**
     * KFSPTS-1460:
     * Differences between this file and the file of the same name received from UConn are:
     *   --implements ExtractPaymentService removed.
     *   --method shouldBundleAchPayments was duplicated in this class and in class AchBundlerFormatServiceImpl.
     *     -created a single method in class AchBundlerHelperService and to be called from both the format and extract payment classes.
     */
    
    public AchBundlerExtractPaymentServiceImpl() {
        super();
    }

    /**
     * MOD: Overridden to detect if the Bundle ACH Payments system parameter is on and if so, to 
     * call the new extraction bundler method
     * @see org.kuali.kfs.pdp.batch.service.ExtractPaymentService#extractAchPayments()
     */
    @Override
    public void extractAchPayments() {
        LOG.debug("AchBundlerExtractPaymentServiceImpl MOD - extractAchPayments() started");

        Date processDate = SpringContext.getBean(DateTimeService.class).getCurrentDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        PaymentStatus extractedStatus = (PaymentStatus) SpringContext.getBean(BusinessObjectService.class).findBySinglePrimaryKey(PaymentStatus.class, PdpConstants.PaymentStatusCodes.EXTRACTED);

        String achFilePrefix = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(PdpKeyConstants.ExtractPayment.ACH_FILENAME);
        achFilePrefix = MessageFormat.format(achFilePrefix, new Object[] { null });

        String filename = getOutputFile(achFilePrefix, processDate);
        LOG.debug("AchBundlerExtractPayment MOD: extractAchPayments() filename = " + filename);

        /** 
         * MOD: This is the only section in the method that is changed.  This mod calls a new method that bundles 
         * ACHs into single disbursements if the flag to do so is turned on.
         */
        if (SpringContext.getBean(AchBundlerHelperService.class).shouldBundleAchPayments()) {
            writeExtractBundledAchFile(extractedStatus, filename, processDate, sdf);
        } else {
            writeExtractAchFile(extractedStatus, filename, processDate, sdf);
        }
    }
    
    
    /**
     * KFSPTS-1460: 
     * Re-factored 
     * Changes made to this method due to re-factoring the code so that common pieces could be used 
     * by both ExtractPaymentServiceImpl.writeExtractAchFile and AchBundlerExtractPaymentServiceImpl.writeExtractBundledAchFile
     * as well as incorporating the Mellon file creation.
     * --Added the call to method writeExtractAchFileMellonBankFastTrack
     * --Added the call to writePayeeSpecificsToAchFile for re-factored code
     * --Added the call to writePaymentDetailToAchFile for re-factored code
     * --Made the "finally" clause match the ExtractPaymentServiceImpl.writeExtractAchFile finally so that the XML files are named the same regardless of which routine is invoked.
     * --Added call to get the parameterized bank notification email addresses
     */
    /**
     * A custom method that goes through and extracts all pending ACH payments and bundles them by payee/disbursement nbr.
     * 
     * @param extractedStatus
     * @param filename
     * @param processDate
     * @param sdf
     */
    protected void writeExtractBundledAchFile(PaymentStatus extractedStatus, String filename, Date processDate, SimpleDateFormat sdf) {
    	LOG.info("AchBundledExtractPaymentServiceImpl.writeExtractBundledAchFile started.");
        BufferedWriter os = null;

        try {
        	
        	//KFSPTS-1460: parameterized the hard coded email addresses
        	List<String> notificationEmailAddresses = getBankPaymentFileNotificationEmailAddresses();  
        	
            // Writes out the BNY Mellon Fast Track formatted file for ACH payments.  We need to do this first since the status is set in this method which
        	//   causes the writeExtractAchFileMellonBankFastTrack method to not find anything.
        	writeExtractAchFileMellonBankFastTrack(extractedStatus, filename, processDate, sdf, notificationEmailAddresses);
        	
            // totals for summary
            Map<String, Integer> unitCounts = new HashMap<String, Integer>();
            Map<String, KualiDecimal> unitTotals = new HashMap<String, KualiDecimal>();
        	
            os = new BufferedWriter(new FileWriter(filename));
            os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writeOpenTag(os, 0, "achPayments");            

            HashSet<String> bankCodes = SpringContext.getBean(AchBundlerHelperService.class).getDistinctBankCodesForPendingAchPayments();

            for (String bankCode : bankCodes) {
                HashSet<Integer> disbNbrs = SpringContext.getBean(AchBundlerHelperService.class).getDistinctDisbursementNumbersForPendingAchPaymentsByBankCode(bankCode);
                for (Iterator<Integer> iter = disbNbrs.iterator(); iter.hasNext();) {
                    Integer disbursementNbr = iter.next();

                    boolean first = true;

                    KualiDecimal totalNetAmount = new KualiDecimal(0);

                    // this seems wasteful, but since the total net amount is needed on the first payment detail...it's needed
                    Iterator<PaymentDetail> i2 = SpringContext.getBean(AchBundlerHelperService.class).getPendingAchPaymentDetailsByDisbursementNumberAndBank(disbursementNbr, bankCode);
                    while (i2.hasNext()) {
                        PaymentDetail pd = i2.next();
                        totalNetAmount = totalNetAmount.add(pd.getNetPaymentAmount());
                    }

                    Iterator<PaymentDetail> paymentDetails = SpringContext.getBean(AchBundlerHelperService.class).getPendingAchPaymentDetailsByDisbursementNumberAndBank(disbursementNbr, bankCode);
                    while (paymentDetails.hasNext()) {
                        PaymentDetail paymentDetail = paymentDetails.next();
                        PaymentGroup paymentGroup = paymentDetail.getPaymentGroup();
                        if (!testMode) {
                        	paymentGroup.setDisbursementDate(new java.sql.Date(processDate.getTime()));
                        	paymentGroup.setPaymentStatus(extractedStatus);
                        	SpringContext.getBean(BusinessObjectService.class).save(paymentGroup);
                        }

                        if (first) {
                        	writePayeeSpecificsToAchFile(os, paymentGroup, processDate, sdf);  //KFSPTS-1460: re--factored

                            writeOpenTag(os, 4, "payments");
                        }
                                               
                        writePaymentDetailToAchFile(os, paymentGroup, paymentDetail, unitCounts, unitTotals, sdf);   //KFSPTS-1460: re-factored
                        
                        first = false;
                    }
                    writeCloseTag(os, 4, "payments");
                    writeCloseTag(os, 2, "ach");   //open for this tag is in method writePayeeSpecificsToAchFile
                }
            }
            writeCloseTag(os, 0, "achPayments");
            
            // send summary email
            getPaymentFileEmailService().sendAchSummaryEmail(unitCounts, unitTotals, SpringContext.getBean(DateTimeService.class).getCurrentDate());
        }
        catch (IOException ie) {
            LOG.error("AchBunderlExtract MOD: extractAchFile() Problem reading file:  " + filename, ie);
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
                	LOG.error("IOException encountered in writeExtractBundledAchFile.  Message is: " + ie.getMessage());
                }
            }
        }
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
        String testIndicator;   // For Mellon Fast Track files - indicates whether the generated file is for (T)est or for (P)roduction
        String ourBankAccountNumber = "";
        String ourBankRoutingNumber = "";
        String subUnitCode = "";
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
          
          HashSet<String> bankCodes = SpringContext.getBean(AchBundlerHelperService.class).getDistinctBankCodesForPendingAchPayments();
          
          for (String bankCode : bankCodes) {
            HashSet<Integer> disbNbrs = SpringContext.getBean(AchBundlerHelperService.class).getDistinctDisbursementNumbersForPendingAchPaymentsByBankCode(bankCode);
            
            for (Iterator<Integer> iter = disbNbrs.iterator(); iter.hasNext();) {
              Integer disbursementNbr = iter.next();              
              boolean first = true;
                            
              //compute total net amount as it is needed on first payment detail
              KualiDecimal totalNetAmount = new KualiDecimal(0);
              Iterator<PaymentDetail> payDetailIter = SpringContext.getBean(AchBundlerHelperService.class).getPendingAchPaymentDetailsByDisbursementNumberAndBank(disbursementNbr, bankCode);
              while (payDetailIter.hasNext()) {
                PaymentDetail pd = payDetailIter.next();
                totalNetAmount = totalNetAmount.add(pd.getNetPaymentAmount());
              }
              
              Iterator<PaymentDetail> paymentDetails = SpringContext.getBean(AchBundlerHelperService.class).getPendingAchPaymentDetailsByDisbursementNumberAndBank(disbursementNbr, bankCode);
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
                            Country country = this.getCountryService().getCountry(pg.getCountry());
                            if (country != null) {
                              sCountryName = country.getName().substring(0,((country.getName().length() >= CountryNameMaxLength)? CountryNameMaxLength: country.getName().length() ));
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

                            if (subUnitCode.equals("DV")) dvCodeInt = 1;
                            else if (subUnitCode.equals("PRAP")) dvCodeInt = 1;
                            else if (subUnitCode.equals("LIBR")) dvCodeInt = 2;
                            else if (subUnitCode.equals("CSTR")) dvCodeInt = 3;
                            else if (subUnitCode.equals("STAT")) dvCodeInt = 4;
                            else if (subUnitCode.equals("CLIF")) dvCodeInt = 5;
                            else dvCodeInt = 0;
                            if (dvCodeInt != 0)
                              divisionCode = String.format(String.format("%%0%dd", 3), dvCodeInt);
                            else
                            {
                              LOG.error("writeExtractBundledAchFileMellonBankFastTrack EXCEPTION: DIVSION CODE ISSUE=>  SUB UNIT IS " + subUnitCode + " BUT CAN ONLY BE 'DV', 'PRAP', 'LIBR', 'CSTR', 'STAT' OR 'CLIF'");
                              break;
                            }
                            
                            //Determine the ACH Code to send down.  Here are the rules:
                            // 1.  If they are a vendor and they've selected checking account, the ACH code is CTX (vendors can only have ACH to checking)
                            // 2.  If they are a vendor and they've selected savings account, the ACH code is PPD (sometimes sole proprietors are vendors and they use personal not corporate accounts)
                            // 3.  If they are NOT a vendor, then the ACH code is always PPD (allows employees to have their's deposited in checking or savings)
                            
                            //Determine which ACH code to use: CTX for corporate accounts, or PPD for Personal accounts
                            // String payeeId = paymentGroup.getPayeeId();              // Returns the ID of the payee and is the vendor number if the next var indicates that
                            // String payeeIdTypeDesc = paymentGroup.getPayeeIdTypeDesc();      // Returns "Vendor Number" if the payeeID is a vendor number
                            
                            String AchBankRoutingNbr = "";
                            String AchAccountType = "DA";
                            String AchBankAccountNumber = "";
                            String CheckNumber = "";
                            
                            boolean isVendor = (pg.getPayeeIdTypeCd().equals("V")) ? true : false;  // payeeIdTypeCode returns a "V" if it is a vendor
                            String kfsAccountType = "";
                            
                            if (ObjectUtils.isNotNull(pg.getAchAccountType()))
                              kfsAccountType = pg.getAchAccountType();              // Returns either a 22 for checking or a 32 for Savings account
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
                              AchBankRoutingNbr + cDelim +            // Receiving bank id - 12 bytes
                              AchAccountType + cDelim +                         // Receiving account number qualifier - 3 bytes - must be DA for checking or SG for savings.
                              AchBankAccountNumber + cDelim +           // Receiving account number - 35 bytes
                              sdfPAY1000Rec.format(processDate) + cDelim +      // Effective date - 8 bytes - YYMMDDHHMMSS
                              cDelim +                                          // Business function code - 3 bytes 
                              CheckNumber + cDelim +                // Trace number (check number) - 50 bytes
                              divisionCode + cDelim +                           // Division code - 50 bytes 
                              cDelim +                                          // Currency code - 3 bytes
                              cDelim +                                          // Note 1 - 80 bytes
                              cDelim +                                          // Note 2 - 80 bytes
                              cDelim +                                          // Note 3 - 80 bytes
                              cDelim +                                          // Note 4 - 80 bytes
                              cDelim + "\n");

                          totalPaymentAmounts = totalPaymentAmounts.add(totalNetAmount);
                          
                            // Write the Payer Detail(PDT02010) record for the payer (us) (only one per payee)
                            os.write("PDT02010" + cDelim +                          // Record Type - 8 bytes
                                "PR" + cDelim +                                 // Name qualifier - 3 bytes
                                cDelim +                                        // ID code qualifier - 2 bytes
                                cDelim +                                        // ID code - 80 bytes
                                "Cornell University" + cDelim +                 // Name - 60 bytes
                                cDelim +                                        // Additional name 1 - 60 bytes
                                cDelim +                                        // Additional name 2 - 60 bytes
                                "Division of Financial Affairs" + cDelim +      // Address line 1 - 55 bytes
                                "341 Pine Tree Road" + cDelim +                 // Address line 2 - 55 bytes
                                cDelim +                                        // Address line 3 - 55 bytes
                                cDelim +                                        // Address line 4 - 55 bytes
                                cDelim +                                        // Address line 5 - 55 bytes
                                cDelim +                                        // Address line 6 - 55 bytes
                                "Ithaca" + cDelim +                             // City - 30 bytes
                                "NY" + cDelim +                                 // State/Province - 2 bytes
                                "148502820" + cDelim +                          // Postal code - 15 bytes
                                cDelim +                                        // Country code - 3 bytes
                                cDelim +                                        // Country name - 30 bytes
                                cDelim +                                        // Ref qualifier 1 - 3 bytes
                                cDelim +                                      // Ref ID 1 - 50 bytes
                                cDelim +                                        // Ref description 1 - 80 bytes
                                cDelim +                                        // Ref qualifier 1 - 3 bytes
                                cDelim +                                        // Ref ID 1 - 50 bytes
                                cDelim +                                        // Ref description 1 - 80 bytes
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
                                "PR" + cDelim +             // Name qualifier - 3 bytes
                                cDelim +                    // ID code qualifier - 2 bytes
                                cDelim +                    // ID code - 80 bytes
                                PayeeName + cDelim +        // Name - 30 bytes
                                cDelim +                    // Additional name 1 - 60 bytes
                                cDelim +                    // Additional name 2 - 60 bytes
                                AddrLine1 + cDelim +      // Address line 1 - 35 bytes
                                AddrLine2 + cDelim +      // Address line 2 - 35 bytes
                                AddrLine3 + cDelim +      // Address line 3 - 35 bytes
                                AddrLine4 + cDelim +      // Address line 4 - 35 bytes
                                cDelim +                    // Address line 5 - 35 bytes
                                cDelim +                    // Address line 6 - 35 bytes
                                City + cDelim +             // City - 30 bytes
                                State + cDelim +            // State/Province - 2 bytes
                                Zip + cDelim +            // Postal code - 15 bytes
                                cDelim +                    // Country code - 3 bytes
                                sCountryName + cDelim +     // Country name - 30 bytes  (do not use is Country Code is used)
                                  cDelim +                    // Ref qualifier 1 - 3 bytes
                                cDelim +                    // Ref ID 1 - 50 bytes
                                cDelim +                    // Ref description 1 - 80 bytes
                                cDelim +                    // Ref qualifier 1 - 3 bytes
                                cDelim +                    // Ref ID 1 - 50 bytes
                                cDelim +                    // Ref description 1 - 80 bytes
                                cDelim + "\n");
                           
                            totalRecordCount = totalRecordCount + 3;  //One for the PAY01000 record and one for each PDT02010 record
                            first = false;                // Set this here so it is only executed once per payee
                        }  //If (first)
                        
                        // Write the REM03020 record
                        // Set up remittanceIdCode and remittanceIdText based on whether its a DV or something else.
                        String remittanceIdCode = (subUnitCode.equals(DisbursementVoucherConstants.DOCUMENT_TYPE_CODE)) ? "TN" : "IV" ;
                        String remittanceIdText = (subUnitCode.equals(DisbursementVoucherConstants.DOCUMENT_TYPE_CODE)) ? 
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
                      
                        os.write("REM03020" + cDelim +                                        // Record type - 8 bytes
                            remittanceIdCode + cDelim +                                   // Remittance qualifier code - 3 bytes
                            remittanceIdText + cDelim +                                   // Remittance ID - 50 bytes
                            ftNetPayAmount + cDelim +                           // Net invoice amount - 18 bytes
                            ftTotalAmount + cDelim +                          // Total invoice amount - 18 bytes
                            ftDiscountAmt + cDelim +                        // Discount amount - 18 bytes
                            cDelim +                                      // Note 1 - 80 bytes - NOT USED PER SPEC
                            cDelim +                                      // Note 2 - 80 bytes - NOT USED PER SPEC
                            cDelim +                                                      // Ref qualifier 1
                            cDelim +                                                      // Ref ID 1
                            cDelim +                                                      // Ref description 1
                            cDelim +                                                      // Ref qualifier 2
                            cDelim +                                                      // Ref ID 2
                            cDelim +                                                      // Ref description 2
                            cDelim +                                                      // Ref qualifier 3
                            cDelim +                                                      // Ref ID 3
                            cDelim +                                                      // Ref description 3
                            cDelim +                                                      // Ref qualifier 4
                            cDelim +                                                      // Ref ID 4
                            cDelim +                                                      // Ref description 4
                            dateQualifer + cDelim +                                       // Date qualifier 1
                            InvoiceDate + cDelim +                                        // Date 1 
                            cDelim +                                                      // Date qualifier 2
                            cDelim +                                                      // Date 2
                            cDelim +                                                      // Date qualifier 3
                            cDelim +                                                      // Date 3
                            cDelim +                                                      // Date qualifier 4
                            cDelim + "\n");
                            
                        totalRecordCount = totalRecordCount + 1;  //One for the REM03020 record                        
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
    
        //TODO UPGRADE-911
        //Is this the right call?
        writePayeeAch(os, 4, paymentGroup);
        writeTag(os, 4, "customerUnivNbr", paymentGroup.getPayeeId());// .getCustomerInstitutionNumber());
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
    
    protected String updateNoteLine(String noteLine) {
      //  Had to add this code to check for and remove the colons (::) that were added in 
      //   DisbursementVoucherExtractServiceImpl.java line 506 v4229 if they exist.  If not
    //   then just return what was sent.  This was placed in a method as it is used in
    //   two locations in this class

    if (noteLine.length() >= 2)
        if (noteLine.substring(0,2).contains(DV_EXTRACT_TYPED_NOTE_PREFIX_IDENTIFIER))
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
            emailAddressesStr = parameterService.getParameterValueAsString(CUKFSParameterKeyConstants.KFS_PDP, CUKFSParameterKeyConstants.ALL_COMPONENTS, CUKFSParameterKeyConstants.BANK_PAYMENT_FILE_EMAIL_NOTIFICATION);
        } catch(Exception e) {
            LOG.error("ExtractPaymentServiceImpl.getBankPaymentFileNotificationEmailAddresses: The " + CUKFSParameterKeyConstants.KFS_PDP + ":" + CUKFSParameterKeyConstants.ALL_COMPONENTS + ":" + CUKFSParameterKeyConstants.BANK_PAYMENT_FILE_EMAIL_NOTIFICATION + "system parameter was not found registered in the system.");
        }
      
      List<String> emailAddressList = Arrays.asList(emailAddressesStr.split(";"));
      
      return emailAddressList;
    }
    
    protected boolean isProduction() {
      return ConfigContext.getCurrentContextConfig().getProperty(Config.PROD_ENVIRONMENT_CODE).equalsIgnoreCase(
          ConfigContext.getCurrentContextConfig().getEnvironment());
   }
}