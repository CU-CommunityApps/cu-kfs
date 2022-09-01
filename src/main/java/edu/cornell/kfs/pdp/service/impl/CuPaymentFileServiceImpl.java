package edu.cornell.kfs.pdp.service.impl;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.Batch;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.LoadPaymentStatus;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentFileLoad;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.service.impl.PaymentFileServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;

import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.pdp.CUPdpParameterConstants;
import edu.cornell.kfs.pdp.service.CuPdpEmployeeService;

public class CuPaymentFileServiceImpl extends PaymentFileServiceImpl {
    private static final Logger LOG = LogManager.getLogger();
    
    protected VendorService vendorService;
    protected EmailService emailService;
    protected PersonService personService;
    protected CuPdpEmployeeService cuPdpEmployeeService;
    
    public CuPaymentFileServiceImpl() {
        super();
    }
    
    @Override
    public void loadPayments(PaymentFileLoad paymentFile, LoadPaymentStatus status, String incomingFileName) {
        status.setChart(paymentFile.getCampus());
        status.setUnit(paymentFile.getUnit());
        status.setSubUnit(paymentFile.getSubUnit());
        status.setCreationDate(paymentFile.getCreationDate());
        status.setDetailCount(paymentFile.getActualPaymentCount());
        status.setDetailTotal(paymentFile.getCalculatedPaymentTotalAmount());

        // create batch record for payment load
        Batch batch = createNewBatch(paymentFile, getBaseFileName(incomingFileName));
        businessObjectService.save(batch);

        paymentFile.setBatchId(batch.getId());
        status.setBatchId(batch.getId());

        // do warnings and set defaults
        List<String> warnings = paymentFileValidationService.doSoftEdits(paymentFile);
        status.setWarnings(warnings);

        // store groups
        for (PaymentGroup paymentGroup : paymentFile.getPaymentGroups()) {
            assignDisbursementTypeCode(paymentGroup);
            updatePaymentFieldsForEmployeePayee(paymentFile, paymentGroup);
            businessObjectService.save(paymentGroup);
        }

        // CU Customization: Check for and warn about inactive vendors.
        checkForInactiveVendors(paymentFile.getPaymentGroups(), batch.getCustomerProfile());

        // send list of warnings
        paymentFileEmailService.sendLoadEmail(paymentFile, warnings);
        if (paymentFile.isTaxEmailRequired()) {
            paymentFileEmailService.sendTaxEmail(paymentFile);
        }

        removeDoneFile(incomingFileName);

        LOG.debug("loadPayments() was successful");
        status.setLoadStatus(LoadPaymentStatus.LoadStatus.SUCCESS);
    }
    
    private void updatePaymentFieldsForEmployeePayee(PaymentFileLoad paymentFile, PaymentGroup paymentGroup) {
        LOG.debug("updatePaymentFieldsForEmployeePayee, entering");
        if (cuPdpEmployeeService.shouldPayeeBeProcessedAsEmployeeForThisCustomer(paymentFile)) {
            Person employee = personService.getPersonByEmployeeId(paymentGroup.getPayeeId());
            LOG.debug("updatePaymentFieldsForEmployeePayee, processing payee as emoployee: " + employee.getName());
            updatePayeeAddressFieldsFromPerson(paymentGroup, employee);
            paymentGroup.setEmployeeIndicator(true);
        }
    }
    
    private void updatePayeeAddressFieldsFromPerson(PaymentGroup paymentGroup, Person person) {
        paymentGroup.setLine1Address(person.getAddressLine1Unmasked());
        paymentGroup.setLine2Address(person.getAddressLine2Unmasked());
        paymentGroup.setLine3Address(person.getAddressLine3Unmasked());
        paymentGroup.setCity(person.getAddressCityUnmasked());
        paymentGroup.setState(person.getAddressStateProvinceCodeUnmasked());
        paymentGroup.setZipCd(person.getAddressPostalCodeUnmasked());
        paymentGroup.setCountry(person.getAddressCountryCodeUnmasked());
    }
    
    @Override
    protected PaymentFileLoad parsePaymentFile(BatchInputFileType paymentInputFileType, String incomingFileName, MessageMap errorMap) {
        FileInputStream fileContents;
        try {
            fileContents = new FileInputStream(incomingFileName);
        } catch (FileNotFoundException e1) {
            LOG.error("parsePaymentFile: file to load not found " + incomingFileName, e1);
            throw new RuntimeException("Cannot find the file requested to be loaded " + incomingFileName, e1);
        }

        // do the parse
        PaymentFileLoad paymentFile = null;
        try {
            byte[] fileByteContent = IOUtils.toByteArray(fileContents);
            paymentFile = (PaymentFileLoad) batchInputFileService.parse(paymentInputFileType, fileByteContent);
        } catch (IOException e) {
            LOG.error("parsePaymentFile: error while getting file bytes:  " + e.getMessage(), e);
            sendErrorEmailForGenericException(e, incomingFileName, paymentFile);
            throw new RuntimeException("Error encountered while attempting to get file bytes: " + e.getMessage(), e);
        } catch (ParseException e1) {
            LOG.error("parsePaymentFile: Error parsing xml " + e1.getMessage());
            errorMap.putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_BATCH_UPLOAD_PARSING,
                    e1.getMessage());
            // Get customer object from unparsable file so error email can be sent.
            paymentFile = getCustomerProfileFromUnparsableFile(incomingFileName, paymentFile);
        } catch (RuntimeException e2) {
            LOG.error("parsePaymentFile: Error reading XML: " + e2.getMessage());
            sendErrorEmailForGenericException(e2, incomingFileName, paymentFile);
            throw e2;
        }

        return paymentFile;
    }
    
    /**
     * This method exists here because PaymentGroups are defined in the PaymentFileLoad objects based on the XML that is read in from the PDP Customers.  
     * The assignment of this attribute prior to submission of the PaymentGroup for Formatting is necessary to support the Format process allowing ACH only file generation.
     * See KFSPTS-918 for complete details.
     * 
     * This method takes a given PaymentGroup and sets the disbursement type code on that group.
     * 
     * @param pg
     */
    private void assignDisbursementTypeCode(PaymentGroup pg) {
        if (pg.isPayableByACH()) {
            pg.setDisbursementTypeCode(PdpConstants.DisbursementTypeCodes.ACH);
        } else {
            pg.setDisbursementTypeCode(PdpConstants.DisbursementTypeCodes.CHECK);
        }
    }
    
    private void sendErrorEmailForGenericException(
            Exception genericException, String incomingFileName, PaymentFileLoad paymentFile) {
        PaymentFileLoad paymentFileForEmail;
        try {
            paymentFileForEmail = getCustomerProfileFromUnparsableFile(incomingFileName, paymentFile);
        } catch (RuntimeException e) {
            LOG.error("sendErrorEmailForGenericException: Could not read file contents as plain text", e);
            paymentFileForEmail = paymentFile;
        }
        
        MessageMap errorMap = new MessageMap();
        errorMap.putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_BATCH_UPLOAD_PARSING,
                "Error reading XML file: " + genericException.getMessage());
        paymentFileEmailService.sendErrorEmail(paymentFileForEmail, errorMap);
    }
    
    /**
     * @param incomingFileName
     * @param paymentFile
     * @return
     */
    private PaymentFileLoad getCustomerProfileFromUnparsableFile(String incomingFileName, PaymentFileLoad paymentFile) {
        FileInputStream exFileContents;

        try {
            exFileContents = new FileInputStream(incomingFileName); 
        } catch (FileNotFoundException e1) {
            LOG.error("file to load not found " + incomingFileName, e1);
            throw new RuntimeException("Cannot find the file requested to be loaded " + incomingFileName, e1);
        }

        try {   
            InputStreamReader inputReader = new InputStreamReader(exFileContents, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputReader);
            String line = "";
            boolean found = false;
            String chartVal = "";
            String unitVal = "";
            String subUnitVal = "";

            while(!found && (line=bufferedReader.readLine())!=null) {
                // Use multiple ifs instead of else/ifs because all values could occur on the same line.
                if(StringUtils.contains(line, CUPdpConstants.CustomerProfilePrimaryKeyTags.CHART_OPEN)) {
                    chartVal = StringUtils.substringBetween(line, CUPdpConstants.CustomerProfilePrimaryKeyTags.CHART_OPEN, CUPdpConstants.CustomerProfilePrimaryKeyTags.CHART_CLOSE);
                }
                if(StringUtils.contains(line, CUPdpConstants.CustomerProfilePrimaryKeyTags.UNIT_OPEN)) {
                    unitVal = StringUtils.substringBetween(line, CUPdpConstants.CustomerProfilePrimaryKeyTags.UNIT_OPEN, CUPdpConstants.CustomerProfilePrimaryKeyTags.UNIT_CLOSE);
                }
                if(StringUtils.contains(line, CUPdpConstants.CustomerProfilePrimaryKeyTags.SUBUNIT_OPEN)) {
                    subUnitVal = StringUtils.substringBetween(line, CUPdpConstants.CustomerProfilePrimaryKeyTags.SUBUNIT_OPEN, CUPdpConstants.CustomerProfilePrimaryKeyTags.SUBUNIT_CLOSE);
                    found = true;
                }
            }

            if(found) {
                // Note: the pdpEmailServiceImpl doesn't actually use the customer object from the paymentFile, but rather retrieves an instance using
                // the values provided for chart, unit and sub_unit.  However, it doesn't make sense to even populate the paymentFile object if 
                // the values retrieved don't map to a valid customer object, so we will retrieve the object here to validate the values.
                CustomerProfile customer = customerProfileService.get(chartVal, unitVal, subUnitVal);
                if(ObjectUtils.isNotNull(customer)) {
                    if(ObjectUtils.isNull(paymentFile)) {
                        paymentFile = new PaymentFileLoad();
                    }
                    paymentFile.setCampus(chartVal);
                    paymentFile.setUnit(unitVal);
                    paymentFile.setSubUnit(subUnitVal);
                    paymentFile.setCustomer(customer);
                }
            }
            
        } catch(Exception ex) {
            LOG.error("Attempts to retrieve the customer profile from the unparsable XML file failed with the following error.", ex);
        } finally {
            try {
                exFileContents.close();
            } catch(IOException io) {
                LOG.error("File stream object could not be closed.", io);                   
            }
        }
        return paymentFile;
    }

    /**
     * Checks whether any of the batch load's payment groups reference inactive vendors,
     * and sends warning emails appropriately if so.
     * 
     * @param paymentGroups The payment groups that were loaded.
     * @param customer The customer's profile.
     */
    private void checkForInactiveVendors(List<PaymentGroup> paymentGroups, CustomerProfile customer) {
        final int MESSAGE_START_SIZE = 300;
        StringBuilder inactiveVendorsMessage = new StringBuilder(MESSAGE_START_SIZE);
        
        for (PaymentGroup paymentGroup : paymentGroups) {
            // Determine whether the payment group's vendor is inactive.
            VendorDetail vendor = vendorService.getVendorDetail(paymentGroup.getPayeeId());
            
            if (vendor != null && !vendor.isActiveIndicator()) {
                // If vendor is inactive, then append warning text to final email message.
                LOG.warn("Found payment group with inactive vendor payee. Payment Group ID: " + paymentGroup.getId()
                        + ", Vendor ID: " + paymentGroup.getPayeeId());
                String warnMessageStart = getStartOfVendorInactiveMessage(vendor);
                if (inactiveVendorsMessage.length() == 0) {
                    // Add header if necessary.
                    inactiveVendorsMessage.append("The PDP feed submitted by your unit includes payments to inactive vendors.  ")
                            .append("Action is needed on your part to rectify the situation for future payments.  ")
                            .append("Review the inactive reason to determine action needed.  Details follow:\n\n");
                }
                
                // Append payment detail information to the message. (As per the payment file XSD, there should be at least one detail.)
                for (PaymentDetail paymentDetail : paymentGroup.getPaymentDetails()) {
                    inactiveVendorsMessage.append(warnMessageStart)
                            .append("Customer Payment Doc Nbr: ").append(paymentDetail.getCustPaymentDocNbr()).append('\n')
                            .append("Payment Group ID: ").append(paymentDetail.getPaymentGroupId()).append('\n')
                            .append("Payment Date: ").append(paymentGroup.getPaymentDate()).append('\n')
                            .append("Amount: ").append(paymentDetail.getNetPaymentAmount()).append("\n\n");
                }
            }
        }
        
        // If one or more inactive vendors were found, then send notification emails to warn about their presence.
        if (inactiveVendorsMessage.length() > 0) {
            sendInactiveVendorsMessage(customer, inactiveVendorsMessage.toString());
        }
    }

    /**
     * Builds the common start of all vendor-inactive-warning messages for a specific payment group's vendor.
     * 
     * @param vendor The inactive vendor.
     */
    private String getStartOfVendorInactiveMessage(VendorDetail vendor) {
        final int BUILDER_SIZE = 100;
        // Have the inactivation reason printed as "reasonCode -- reasonDescription", or as "None" if no reason is given.
        String reasonCode = vendor.getVendorInactiveReasonCode();
        String reasonDesc = " -- " + (ObjectUtils.isNotNull(vendor.getVendorInactiveReason())
                ? vendor.getVendorInactiveReason().getVendorInactiveReasonDescription() : "No Reason Description Given");
        if (StringUtils.isBlank(reasonCode)) {
            reasonCode = "None";
            reasonDesc = KFSConstants.EMPTY_STRING;
        }
        
        // Build and return the message.
        return new StringBuilder(BUILDER_SIZE)
                .append("Vendor Number: ").append(vendor.getVendorNumber()).append('\n')
                .append("Vendor Name: ").append(vendor.getVendorName()).append('\n')
                .append("Vendor Inactivation Reason: ").append(reasonCode).append(reasonDesc).append('\n').toString();
    }

    /**
     * Sends notification emails when a PDP XML file has one or more inactive vendors.
     * 
     * @param customer The customer's profile.
     * @param messageText The text to use as the body of the email message.
     */
    private void sendInactiveVendorsMessage(CustomerProfile customer, String messageText) {
        BodyMailMessage message = new BodyMailMessage();
        message.setFromAddress(parameterService.getParameterValueAsString(
                KFSConstants.CoreModuleNamespaces.PDP, KfsParameterConstants.BATCH_COMPONENT, KFSConstants.FROM_EMAIL_ADDRESS_PARAM_NM));
        message.setSubject("Inactive vendors detected in PDP feed for customer: " + customer.getCustomerDescription());
        message.setMessage(messageText);
        // Send to pre-determined recipient list, plus the customer's process email address.
        message.setToAddresses(new HashSet<String>(parameterService.getParameterValuesAsString(KFSConstants.CoreModuleNamespaces.PDP,
                KfsParameterConstants.BATCH_COMPONENT, CUPdpParameterConstants.WARNING_INACTIVE_VENDOR_TO_EMAIL_ADDRESSES)));
        message.addToAddress(customer.getProcessingEmailAddr());
        
        emailService.sendMessage(message, false);
    }

    public void setVendorService(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setCuPdpEmployeeService(CuPdpEmployeeService cuPdpEmployeeService) {
        this.cuPdpEmployeeService = cuPdpEmployeeService;
    }

}
