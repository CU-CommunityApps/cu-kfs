package edu.cornell.kfs.pdp.batch.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.core.impl.config.property.Config;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.batch.service.impl.ExtractPaymentServiceImpl;
import org.kuali.kfs.pdp.batch.service.impl.Iso20022FormatExtractor;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentNoteText;
import org.kuali.kfs.pdp.businessobject.PaymentProcess;
import org.kuali.kfs.pdp.businessobject.PaymentStatus;
import org.kuali.kfs.sys.KFSConstants;

import com.rsmart.kuali.kfs.pdp.service.AchBundlerHelperService;

import edu.cornell.kfs.fp.document.CuDisbursementVoucherConstants;
import edu.cornell.kfs.pdp.CUPdpParameterConstants;
import edu.cornell.kfs.pdp.batch.service.CuPayeeAddressService;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class CuExtractPaymentServiceImpl extends ExtractPaymentServiceImpl {
    private static final Logger LOG = LogManager.getLogger();
    
    protected AchBundlerHelperService achBundlerHelperService;
    protected CuPayeeAddressService cuPayeeAddressService;
    protected Iso20022FormatExtractor iso20022FormatExtractor;

    public CuExtractPaymentServiceImpl() {
    	super(null);
    }

    public CuExtractPaymentServiceImpl(
            final Iso20022FormatExtractor iso20022FormatExtractor
    ) {
    	super(iso20022FormatExtractor);
    	this.iso20022FormatExtractor = iso20022FormatExtractor;
    }

    /** MOD: Overridden to make filename unique by adding milliseconds to filename **/
    @Override
    protected String getOutputFile(final String fileprefix, final Date runDate) {
        //add a step to check for directory paths
        prepareDirectories(getRequiredDirectoryNames());

        String filename = directoryName + "/" + fileprefix + "_";
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US);
        filename = filename + sdf.format(runDate);
        filename = filename + ".xml";

        return filename;
    }
    
    /**
    * MOD: Overridden to detect if the Bundle ACH Payments system parameter is on and if so, to 
    * call the new extraction bundler method
    */
    @Override
    public void extractAchPayments() {
        LOG.debug("MOD - extractAchPayments() - Enter");

        PaymentStatus extractedStatus = businessObjectService.findBySinglePrimaryKey(PaymentStatus.class,
                PdpConstants.PaymentStatusCodes.EXTRACTED);

        if (shouldUseIso20022Format()) {
            iso20022FormatExtractor.extractAchs(extractedStatus, directoryName);
            return;
        }

        Date processDate = dateTimeService.getCurrentDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    
        String achFilePrefix = kualiConfigurationService.getPropertyValueAsString(
                PdpKeyConstants.ExtractPayment.ACH_FILENAME);
        achFilePrefix = MessageFormat.format(achFilePrefix, new Object[] { null });
    
        String filename = getOutputFile(achFilePrefix, processDate);

        /** 
        * MOD: This is the only section in the method that is changed.  This mod calls a new method that bundles 
        * ACHs into single disbursements if the flag to do so is turned on.
        */
        LOG.info("extractAchPayments writing file " + filename);
        if (getAchBundlerHelperService().shouldBundleAchPayments()) {
            writeExtractBundledAchFile(extractedStatus, filename, processDate, sdf);
        } else {
            writeExtractAchFile(extractedStatus, filename, processDate, sdf);
        }
    }

    /**
     * Overridden to call a CU-specific version of the parameter-checking method to determine
     * whether or not to use the ISO 20022 format, as well as to allow for generating
     * the proprietary XML when the ISO 20022 format is enabled.
     */
    @Override
    public void extractChecks() {
        LOG.debug("extractChecks() - Enter");

        final PaymentStatus extractedStatus =
                businessObjectService.findBySinglePrimaryKey(
                        PaymentStatus.class,
                        PdpConstants.PaymentStatusCodes.EXTRACTED
                );

        if (shouldCreateLegacyCheckFiles()) {
            extractChecksToProprietaryFormat(extractedStatus);
        }

        if (shouldUseIso20022Format()) {
            iso20022FormatExtractor.extractChecks(extractedStatus, directoryName);
        }

        LOG.debug("extractChecks() - Exit");
    }

    /*
     * The KualiCo superclass declares this method as private, so we have to copy it
     * to allow this subclass to invoke it.
     */
    private void extractChecksToProprietaryFormat(
            final PaymentStatus extractedStatus
    ) {
        LOG.debug("extractChecksToProprietaryFormat(...) - Enter : extractedStatus={}", extractedStatus);

        final Date processDate = dateTimeService.getCurrentDate();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        String checkFilePrefix = this.kualiConfigurationService.getPropertyValueAsString(
                PdpKeyConstants.ExtractPayment.CHECK_FILENAME);
        checkFilePrefix = MessageFormat.format(checkFilePrefix, new Object[]{null});

        final String filename = getOutputFile(checkFilePrefix, processDate);
        LOG.debug("extractChecksToProprietaryFormat(...) - : filename={}", filename);

        final List<PaymentProcess> extractsToRun = this.processDao.getAllExtractsToRun();
        for (final PaymentProcess extractToRun : extractsToRun) {
            writeExtractCheckFile(extractedStatus, extractToRun, filename, extractToRun.getId().intValue());
            if (shouldPerformDataUpdatesWhenCreatingLegacyCheckFiles()) {
                extractToRun.setExtractedInd(true);
                businessObjectService.save(extractToRun);
            }
        }

        LOG.debug("extractChecksToProprietaryFormat(...) - Exit");
    }

    /*
     * The KualiCo superclass declares this method as private, so we have to define our own instead of overriding.
     */
    private boolean shouldUseIso20022Format() {
        if (isIso20022FormatParameterEnabled(CUPdpParameterConstants.CU_USE_ISO20022_FORMAT_IND)) {
            if (isIso20022FormatParameterEnabled(PdpConstants.ISO20022_FORMAT_IND)) {
                return true;
            } else {
                throw new IllegalStateException("ISO20022_FORMAT_IND cannot be disabled "
                        + "if CU_USE_ISO20022_FORMAT_IND is enabled");
            }
        } else {
            return false;
        }
    }

    private boolean isIso20022FormatParameterEnabled(final String parameterName) {
        return parameterService.getParameterValueAsBoolean(KFSConstants.CoreModuleNamespaces.PDP,
                KFSConstants.Components.ISO_FORMAT, parameterName, Boolean.FALSE);
    }

    private boolean shouldCreateLegacyCheckFiles() {
        return !shouldUseIso20022Format() || isIso20022FormatParameterEnabled(
                CUPdpParameterConstants.CU_ISO20022_FORCE_CREATE_LEGACY_CHECK_FILES);
    }

    private boolean shouldPerformDataUpdatesWhenCreatingLegacyCheckFiles() {
        return !shouldUseIso20022Format();
    }

   /**
    * A custom method that goes through and extracts all pending ACH payments and bundles them by payee/disbursement nbr.
    * Changes made to this method due to re-factoring the code so that common pieces could be used 
    * by both ExtractPaymentServiceImpl.writeExtractAchFile and AchBundlerExtractPaymentServiceImpl.writeExtractBundledAchFile
    * as well as incorporating the Mellon file creation.
    * --Added the call to writePayeeSpecificsToAchFile for re-factored code
    * --Added the call to writePaymentDetailToAchFile for re-factored code
    * --Made the "finally" clause match the ExtractPaymentServiceImpl.writeExtractAchFile finally so that the XML files are named the same regardless of which routine is invoked.
    * --Added call to get the parameterized bank notification email addresses
    * --Removed FastTrack customization
    * 
    * @param extractedStatus
    * @param filename
    * @param processDate
    * @param sdf
    */
   protected void writeExtractBundledAchFile(
           final PaymentStatus extractedStatus, final String filename, 
           final Date processDate, final SimpleDateFormat sdf) {
       LOG.info("writeExtractBundledAchFile started.");
       BufferedWriter os = null;

       try {
           final List<String> notificationEmailAddresses = getBankPaymentFileNotificationEmailAddresses();  
           
           // totals for summary
           final Map<String, Integer> unitCounts = new HashMap<String, Integer>();
           final Map<String, KualiDecimal> unitTotals = new HashMap<String, KualiDecimal>();
           
           os = new BufferedWriter(new FileWriter(filename, StandardCharsets.UTF_8));
           os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
           writeOpenTag(os, 0, "achPayments");            

           final HashSet<String> bankCodes = getAchBundlerHelperService().getDistinctBankCodesForPendingAchPayments();

           for (final String bankCode : bankCodes) {
               final HashSet<Integer> disbNbrs = getAchBundlerHelperService().getDistinctDisbursementNumbersForPendingAchPaymentsByBankCode(bankCode);
               for (Iterator<Integer> iter = disbNbrs.iterator(); iter.hasNext();) {
                   final Integer disbursementNbr = iter.next();

                   boolean first = true;

                   KualiDecimal totalNetAmount = new KualiDecimal(0);

                   // this seems wasteful, but since the total net amount is needed on the first payment detail...it's needed
                   final Iterator<PaymentDetail> i2 = getAchBundlerHelperService().getPendingAchPaymentDetailsByDisbursementNumberAndBank(disbursementNbr, bankCode);
                   while (i2.hasNext()) {
                       final PaymentDetail pd = i2.next();
                       totalNetAmount = totalNetAmount.add(pd.getNetPaymentAmount());
                   }

                   final Iterator<PaymentDetail> paymentDetails = getAchBundlerHelperService().getPendingAchPaymentDetailsByDisbursementNumberAndBank(disbursementNbr, bankCode);
                   while (paymentDetails.hasNext()) {
                       final PaymentDetail paymentDetail = paymentDetails.next();
                       final PaymentGroup paymentGroup = paymentDetail.getPaymentGroup();
                       if (!testMode) {
                           paymentGroup.setDisbursementDate(new java.sql.Date(processDate.getTime()));
                           paymentGroup.setPaymentStatus(extractedStatus);
                           businessObjectService.save(paymentGroup);
                       }

                       if (first) {
                           writePayeeSpecificsToAchFile(os, paymentGroup, processDate, sdf);
                           writeOpenTag(os, 4, "payments");
                       }
                                              
                       writePaymentDetailToAchFile(os, paymentGroup, paymentDetail, unitCounts, unitTotals, sdf);
                       first = false;
                   }
                   writeCloseTag(os, 4, "payments");
                   writeCloseTag(os, 2, "ach");   //open for this tag is in method writePayeeSpecificsToAchFile
               }
           }
           writeCloseTag(os, 0, "achPayments");
           
           paymentFileEmailService.sendAchSummaryEmail(unitCounts, unitTotals, dateTimeService.getCurrentDate());
       }
       catch (final IOException ie) {
           LOG.error("MOD: extractAchFile() Problem reading file:  " + filename, ie);
           throw new IllegalArgumentException("Error writing to output file: " + ie.getMessage());
       }
       finally {
           // Close file
           if (os != null) {
               try {
                   os.close();
                   renameFile(filename, filename + ".NOT_USED");  //  Need to do this at the end to indicate that the file is NOT USED after it is closed.
               }
               catch (final IOException ie) {
                   // Not much we can do now
                   LOG.error("IOException encountered in writeExtractBundledAchFile.  Message is: " + ie.getMessage());
               }
           }
       }
   }    
    @Override
    protected void writeExtractCheckFile(
            final PaymentStatus extractedStatus, final PaymentProcess p, String filename,
            final Integer processId) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        final Date processDate = dateTimeService.getCurrentDate();
        BufferedWriter os = null;
        BufferedWriter osI = null;
        boolean wroteImmediateHeaderRecords = false;
        boolean wroteCheckHeaderRecords = false;
        String immediateFilename = filename.replace("check", "check_immediate");
        String checkFilename = filename;
        boolean first = true;
        boolean isImmediate = false;
        
        //Check whether this is for research participant upload. If the customer profile matches research participant's
        //customer profile, then change the filename to append the RP-Upload prefix.
        if (isResearchParticipantExtractFile(processId)) {
            String checkFilePrefix = this.kualiConfigurationService.getPropertyValueAsString(
                    PdpKeyConstants.ExtractPayment.CHECK_FILENAME);
            checkFilePrefix = MessageFormat.format(checkFilePrefix, new Object[] { null });
            checkFilePrefix = PdpConstants.RESEARCH_PARTICIPANT_FILE_PREFIX + KFSConstants.DASH + checkFilePrefix;
            filename = getOutputFile(checkFilePrefix, processDate);
        }
        
        final List<String> bankCodes = paymentGroupService.getDistinctBankCodesForProcessAndType(processId,
                PdpConstants.DisbursementTypeCodes.CHECK);
        if (bankCodes.isEmpty()) {
            return;
        }

        try {
            final List<String> notificationEmailAddresses = this.getBankPaymentFileNotificationEmailAddresses();  

            for (final String bankCode : bankCodes) {
                final List<Integer> disbNbrs = paymentGroupService.getDisbursementNumbersByDisbursementTypeAndBankCode(processId, PdpConstants.DisbursementTypeCodes.CHECK, bankCode);
                for (final Integer disbursementNbr : disbNbrs) {
                    first = true;

                    KualiDecimal totalNetAmount = new KualiDecimal(0);

                    // this seems wasteful, but since the total net amount is needed on the first payment detail...it's needed
                    final Iterator<PaymentDetail> i2 =
                            paymentDetailService.getByDisbursementNumber(disbursementNbr, processId,
                                    PdpConstants.DisbursementTypeCodes.CHECK, bankCode);
                    while (i2.hasNext()) {
                        final PaymentDetail pd = i2.next();
                        totalNetAmount = totalNetAmount.add(pd.getNetPaymentAmount());
                    }

                    final List<KualiInteger> paymentGroupIdsSaved = new ArrayList<>();

                    final Iterator<PaymentDetail> paymentDetails = paymentDetailService.getByDisbursementNumber(
                            disbursementNbr, processId, PdpConstants.DisbursementTypeCodes.CHECK, bankCode);
                    while (paymentDetails.hasNext()) {
                        final PaymentDetail detail = paymentDetails.next();
                        final PaymentGroup group = detail.getPaymentGroup();
                        if (!testMode && shouldPerformDataUpdatesWhenCreatingLegacyCheckFiles()) {
                            if (!paymentGroupIdsSaved.contains(group.getId())) {
                                group.setDisbursementDate(new java.sql.Date(processDate.getTime()));
                                group.setPaymentStatus(extractedStatus);
                                this.businessObjectService.save(group);
                                paymentGroupIdsSaved.add(group.getId());
                            }
                        }

                        isImmediate = group.getProcessImmediate();
                        if (first && !isImmediate) {
                            if (!wroteCheckHeaderRecords) { 
                                os = new BufferedWriter(new FileWriter(checkFilename, StandardCharsets.UTF_8));
                                os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                                writeOpenTagAttribute(os, 0, "checks", "processId", processId.toString(), "campusCode", p.getCampusCode());
                                wroteCheckHeaderRecords = true;
                            }
                            
                            writeOpenTagAttribute(os, 2, "check", "disbursementNbr",
                                    group.getDisbursementNbr().toString());

                            // Write check level information

                            writeBank(os, 4, group.getBank());

                            writeTag(os, 4, "disbursementDate", sdf.format(processDate));
                            writeTag(os, 4, "netAmount", totalNetAmount.toString());

                            writePayee(os, 4, group);
                            writeTag(os, 4, "campusAddressIndicator", group.getCampusAddress() ? "Y" : "N");
                            writeTag(os, 4, "attachmentIndicator", group.getPymtAttachment() ? "Y" : "N");
                            writeTag(os, 4, "specialHandlingIndicator",
                                    group.getPymtSpecialHandling() ? "Y" : "N");
                            writeTag(os, 4, "immediatePaymentIndicator",
                                    group.getProcessImmediate() ? "Y" : "N");
                            writeTag(os, 4, "customerUnivNbr", group.getCustomerInstitutionNumber());
                            writeTag(os, 4, "paymentDate", sdf.format(group.getPaymentDate()));

                            // Write customer profile information
                            CustomerProfile cp = group.getBatch().getCustomerProfile();
                            writeCustomerProfile(os, 4, cp);

                            writeOpenTag(os, 4, "payments");
                            first = false;
                        }
                        
                        if (first && isImmediate) {
                            if (!wroteImmediateHeaderRecords) {
                                osI = new BufferedWriter(new FileWriter(immediateFilename, StandardCharsets.UTF_8));
                                osI.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                                writeOpenTagAttribute(osI, 0, "checks", "processId", processId.toString(), "campusCode", p.getCampusCode());
                                wroteImmediateHeaderRecords = true;
                            }
                            
                            writeOpenTagAttribute(osI, 2, "check", "disbursementNbr",
                                    group.getDisbursementNbr().toString());
                            // Write check level information
                            writeBank(osI, 4, group.getBank());
                            writeTag(osI, 4, "disbursementDate", sdf.format(processDate));
                            writeTag(osI, 4, "netAmount", totalNetAmount.toString());
                            writePayee(osI, 4, group);
                            writeTag(osI, 4, "campusAddressIndicator", group.getCampusAddress() ? "Y" : "N");
                            writeTag(osI, 4, "attachmentIndicator", group.getPymtAttachment() ? "Y" : "N");
                            writeTag(osI, 4, "specialHandlingIndicator",
                                    group.getPymtSpecialHandling() ? "Y" : "N");
                            writeTag(osI, 4, "immediatePaymentIndicator",
                                    group.getProcessImmediate() ? "Y" : "N");
                            writeTag(osI, 4, "customerUnivNbr", group.getCustomerInstitutionNumber());
                            writeTag(osI, 4, "paymentDate", sdf.format(group.getPaymentDate()));

                            // Write customer profile information
                            CustomerProfile cp = group.getBatch().getCustomerProfile();
                            writeCustomerProfile(osI, 4, cp);
                            writeOpenTag(osI, 4, "payments");
                            first = false;
                        }
                        
                        if (!isImmediate && wroteCheckHeaderRecords) {
                            writeOpenTag(os, 6, "payment");
    
                            writeTag(os, 8, "purchaseOrderNbr", detail.getPurchaseOrderNbr());
                            writeTag(os, 8, "invoiceNbr", detail.getInvoiceNbr());
                            writeTag(os, 8, "requisitionNbr", detail.getRequisitionNbr());
                            writeTag(os, 8, "custPaymentDocNbr", detail.getCustPaymentDocNbr());
                            writeTag(os, 8, "invoiceDate", sdf.format(detail.getInvoiceDate()));
    
                            writeTag(os, 8, "origInvoiceAmount", detail.getOrigInvoiceAmount().toString());
                            writeTag(os, 8, "netPaymentAmount", detail.getNetPaymentAmount().toString());
                            writeTag(os, 8, "invTotDiscountAmount", detail.getInvTotDiscountAmount().toString());
                            writeTag(os, 8, "invTotShipAmount", detail.getInvTotShipAmount().toString());
                            writeTag(os, 8, "invTotOtherDebitAmount", detail.getInvTotOtherDebitAmount().toString());
                            writeTag(os, 8, "invTotOtherCreditAmount", detail.getInvTotOtherCreditAmount().toString());
    
                            writeOpenTag(os, 8, "notes");
                            for (PaymentNoteText note : detail.getNotes()) {
                                writeTag(os, 10, "note", note.getCustomerNoteText());
                            }
                            writeCloseTag(os, 8, "notes");
    
                            writeCloseTag(os, 6, "payment");
                       }
                        
                       if (isImmediate && wroteImmediateHeaderRecords) {
                            writeOpenTag(osI, 6, "payment");
    
                            writeTag(osI, 8, "purchaseOrderNbr", detail.getPurchaseOrderNbr());
                            writeTag(osI, 8, "invoiceNbr", detail.getInvoiceNbr());
                            writeTag(osI, 8, "requisitionNbr", detail.getRequisitionNbr());
                            writeTag(osI, 8, "custPaymentDocNbr", detail.getCustPaymentDocNbr());
                            writeTag(osI, 8, "invoiceDate", sdf.format(detail.getInvoiceDate()));
    
                            writeTag(osI, 8, "origInvoiceAmount", detail.getOrigInvoiceAmount().toString());
                            writeTag(osI, 8, "netPaymentAmount", detail.getNetPaymentAmount().toString());
                            writeTag(osI, 8, "invTotDiscountAmount", detail.getInvTotDiscountAmount().toString());
                            writeTag(osI, 8, "invTotShipAmount", detail.getInvTotShipAmount().toString());
                            writeTag(osI, 8, "invTotOtherDebitAmount", detail.getInvTotOtherDebitAmount().toString());
                            writeTag(osI, 8, "invTotOtherCreditAmount", detail.getInvTotOtherCreditAmount().toString());
    
                            writeOpenTag(osI, 8, "notes");
                            for (PaymentNoteText note : detail.getNotes()) {
                                writeTag(osI, 10, "note", note.getCustomerNoteText());
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
            
            
        } catch (final IOException ie) {
            LOG.error("extractChecks() Problem reading file:  {}", filename, ie);
            throw new IllegalArgumentException("Error writing to output file: " + ie.getMessage());
        } finally {
            // Close file
            if (os != null) {
                try {
                    writeCloseTag(os, 0, "checks");
                    os.close();
                    // An XML file containing these records are NEVER sent to anyone at this time.
                    renameFile(checkFilename, checkFilename + ".NOT_USED");  
                    createDoneFile( checkFilename + ".NOT_USED");
                } catch (final IOException ie) {
                    // Not much we can do now
                }
            }
            if (osI != null) {
                try {
                    writeCloseTag(osI, 0, "checks");
                    osI.close();
                    // An XML file containing these records are ONLY used for local check printing.
                    renameFile(immediateFilename, immediateFilename + ".READY");  
                    createDoneFile( immediateFilename + ".READY");
                } catch (final IOException ie) {
                    // Not much we can do now
                   LOG.error("IOException encountered in writeExtractCheckFile.  Message is: " + ie.getMessage());
                }
            }
        }
    }

    
    public String stripLeadingSpace(final String stringToCheck) {
        return (StringUtils.isNotEmpty(stringToCheck)) ? StringUtils.removeStart(stringToCheck, KFSConstants.BLANK_SPACE) : stringToCheck;
    }
    
    public String stripTrailingSpace(final String stringToCheck) {
        return (StringUtils.isNotEmpty(stringToCheck)) ? StringUtils.removeEnd(stringToCheck, KFSConstants.BLANK_SPACE) : stringToCheck;
    }
    
    public int calculateMaxNumCharsFromNewNoteLine(final String noteLine, final String currentCheckStubDataLine) {
        String proposedCheckStubLine;
        
        if (StringUtils.isBlank(currentCheckStubDataLine) && StringUtils.isBlank(noteLine)) {
            proposedCheckStubLine = KFSConstants.EMPTY_STRING;
            
        } else if (StringUtils.isBlank(currentCheckStubDataLine) && StringUtils.isNotBlank(noteLine)) {
            proposedCheckStubLine = noteLine;
            
        } else if (StringUtils.isNotBlank(currentCheckStubDataLine) && StringUtils.isBlank(noteLine)) {
            proposedCheckStubLine = currentCheckStubDataLine;
            
        } else {
            proposedCheckStubLine = (currentCheckStubDataLine + KFSConstants.BLANK_SPACE + noteLine);
        }
         
        int totalNumChars = proposedCheckStubLine.length();
        
        if (totalNumChars == 0 || totalNumChars <= CuDisbursementVoucherConstants.DV_EXTRACT_MAX_NOTE_LINE_SIZE) {
            return noteLine.length();
        } else {
            final String wrappedText = WordUtils.wrap(proposedCheckStubLine, CuDisbursementVoucherConstants.DV_EXTRACT_MAX_NOTE_LINE_SIZE, "\n", false);
            final String[] constructedCheckStubLines = wrappedText.split("\n");
            return noteLine.length() - constructedCheckStubLines[1].length();
        }
    }
    
    protected boolean isProduction() {
        return ConfigContext.getCurrentContextConfig().getProperty(Config.PROD_ENVIRONMENT_CODE).equalsIgnoreCase(
                ConfigContext.getCurrentContextConfig().getEnvironment());
    }
    
    @Override
    protected void writeExtractAchFile(
            final PaymentStatus extractedStatus, final String filename, final Date processDate,
            final SimpleDateFormat sdf) {
        BufferedWriter os = null;
        try {
            List<String> notificationEmailAddresses = this.getBankPaymentFileNotificationEmailAddresses();  
            
            // totals for summary
            final Map<String, Integer> unitCounts = new HashMap<String, Integer>();
            final Map<String, KualiDecimal> unitTotals = new HashMap<String, KualiDecimal>();

            Iterator iter = paymentGroupService.getByDisbursementTypeStatusCode(PdpConstants.DisbursementTypeCodes.ACH, 
                    PdpConstants.PaymentStatusCodes.PENDING_ACH);
            if (iter.hasNext()) {
                final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8);
                os = new BufferedWriter(writer);
                os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                writeOpenTag(os, 0, "achPayments");

                while (iter.hasNext()) {
                    final PaymentGroup paymentGroup = (PaymentGroup) iter.next();
                    if (!testMode) {
                        paymentGroup.setDisbursementDate(new java.sql.Date(processDate.getTime()));
                        paymentGroup.setPaymentStatus(extractedStatus);
                        businessObjectService.save(paymentGroup);
                    }

                    writePayeeSpecificsToAchFile(os, paymentGroup, processDate, sdf);

                    // Write all payment level information
                    writeOpenTag(os, 4, "payments");
                    final List<PaymentDetail> pdList = paymentGroup.getPaymentDetails();
                    for ( final PaymentDetail paymentDetail: pdList) {
                        writePaymentDetailToAchFile(os, paymentGroup, paymentDetail, unitCounts, unitTotals, sdf);
                    }

                    writeCloseTag(os, 4, "payments");
                    writeCloseTag(os, 2, "ach");
                }
                writeCloseTag(os, 0, "achPayments");

                paymentFileEmailService.sendAchSummaryEmail(unitCounts, unitTotals, dateTimeService.getCurrentDate());
            }
        } catch (final IOException ie) {
            LOG.error("extractAchPayments() Problem reading file:  {}", filename, ie);
            throw new IllegalArgumentException("Error writing to output file: " + ie.getMessage());
        } finally {
            // Close file
            if (os != null) {
                try {
                    os.close();
                } catch (final IOException ie) {
                    // Not much we can do now
                }
            }
        }
    }
    /*
     * New method created due to refactoring the code from ExtractPaymentServiceImpl and AchBundlerExtractPaymnetServiceImpl.
     * Method writes all tags and data for a single payee from open ach to close of customerProfile.
     */
    protected void writePayeeSpecificsToAchFile(
            final BufferedWriter os, final PaymentGroup paymentGroup, final Date processDate, 
            final SimpleDateFormat sdf) throws IOException {
        
        try {
            writeOpenTagAttribute(os, 2, "ach", "disbursementNbr",
                    paymentGroup.getDisbursementNbr().toString());
            final PaymentProcess paymentProcess = paymentGroup.getProcess();
            writeTag(os, 4, "processCampus", paymentProcess.getCampusCode());
            writeTag(os, 4, "processId", paymentProcess.getId().toString());
        
            writeBank(os, 4, paymentGroup.getBank());
        
            writeTag(os, 4, "disbursementDate", sdf.format(processDate));
            writeTag(os, 4, "netAmount", paymentGroup.getNetPaymentAmount().toString());
        
            writePayeeAch(os, 4, paymentGroup);
            writeTag(os, 4, "paymentDate", sdf.format(paymentGroup.getPaymentDate()));
        
            CustomerProfile cp = paymentGroup.getBatch().getCustomerProfile();
            writeCustomerProfile(os, 4, cp);
        
            writeOpenTag(os, 4, "payments");
        }
        catch (final IOException ioe) {
            LOG.error("writePayeeSpecificsToAchFile(): Problem writing to file - IOException caught and rethrown.");
            throw ioe;
        }
        
    }
    
    /*
     * New method created due to refactoring the code from ExtractPaymentServiceImpl and AchBundlerExtractPaymnetServiceImpl
     */
    protected void writePaymentDetailToAchFile(
            final BufferedWriter os, final PaymentGroup paymentGroup, final PaymentDetail paymentDetail, 
            final Map<String, Integer> unitCounts, final Map<String, KualiDecimal> unitTotals, final SimpleDateFormat sdf) throws IOException {
        
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
            for (final PaymentNoteText note : paymentDetail.getNotes()) {
                writeTag(os, 10, "note", updateNoteLine(escapeString(note.getCustomerNoteText())));
            }
            writeCloseTag(os, 8, "notes");
    
            writeCloseTag(os, 6, "payment");
    
            final String unit = paymentGroup.getBatch().getCustomerProfile().getCampusCode() + "-" +
                    paymentGroup.getBatch().getCustomerProfile().getUnitCode() + "-" +
                    paymentGroup.getBatch().getCustomerProfile().getSubUnitCode();
    
            int count = 1;
            if (unitCounts.containsKey(unit)) {
                count = 1 + unitCounts.get(unit);
            }
            unitCounts.put(unit, count);
    
            KualiDecimal unitTotal = paymentDetail.getNetPaymentAmount();
            if (unitTotals.containsKey(unit)) {
                unitTotal = paymentDetail.getNetPaymentAmount().add(unitTotals.get(unit));
            }
            unitTotals.put(unit, unitTotal);
        } catch (final IOException ioe) {
            LOG.error("writePaymentDetailToAchFile(): Problem writing to file - IOException caught and rethrown.");
            throw ioe;
        }
    }
    
    /**
     * Obtains the notification email addresses to include in the bank payment files from the system parameters.
     * @return
     */
    protected List<String> getBankPaymentFileNotificationEmailAddresses() {
        
        String emailAddressesStr = ""; 
        
        try {
            emailAddressesStr = parameterService.getParameterValueAsString(CUKFSParameterKeyConstants.KFS_PDP, CUKFSParameterKeyConstants.ALL_COMPONENTS, 
                    CUKFSParameterKeyConstants.BANK_PAYMENT_FILE_EMAIL_NOTIFICATION);
        } catch (final Exception e) {
            LOG.error("ExtractPaymentServiceImpl.getBankPaymentFileNotificationEmailAddresses: The " + CUKFSParameterKeyConstants.KFS_PDP + ":" 
                    + CUKFSParameterKeyConstants.ALL_COMPONENTS + ":" + CUKFSParameterKeyConstants.BANK_PAYMENT_FILE_EMAIL_NOTIFICATION 
                    + "system parameter was not found registered in the system.");
        }
        
        final List<String> emailAddressList = Arrays.asList(emailAddressesStr.split(";"));
        
        return emailAddressList;
    }
    protected String updateNoteLine(String noteLine) {
        // Had to add this code to check for and remove the colons (::) that were added in
        // DisbursementVoucherExtractServiceImpl.java line 506 v4229 if they exist.  If not
        // then just return what was sent.  This was placed in a method as it is used in
        // two locations in this class

        if (noteLine.length() >= 2 && noteLine.substring(0,2).contains(CuDisbursementVoucherConstants.DV_EXTRACT_TYPED_NOTE_PREFIX_IDENTIFIER)) {
            noteLine = noteLine.substring(2);     
        }

        return noteLine;
    }
    
    protected boolean renameFile(final String fromFile, final String toFile) {
        boolean bResult = false;
        try {
            final File f = new File(fromFile);
            f.renameTo(new File(toFile));
        } catch (final Exception ex) {
            LOG.error("renameFile Exception: " + ex.getMessage());
            LOG.error("fromFile: " + fromFile + ", toFile: " + toFile);
        }
        return bResult;
    }
    // This utility function produces a string of (s) characters (n) times.
    protected String repeatThis(final String s, final int n) {
        return  String.format(String.format("%%0%dd", n), 0).replace("0",s);
    }
    
    public AchBundlerHelperService getAchBundlerHelperService() {
        return achBundlerHelperService;
    }

    public void setAchBundlerHelperService(final AchBundlerHelperService achBundlerHelperService) {
        this.achBundlerHelperService = achBundlerHelperService;
    }

    public void setCuPayeeAddressService(final CuPayeeAddressService cuPayeeAddressService) {
        this.cuPayeeAddressService = cuPayeeAddressService;
    }
}
