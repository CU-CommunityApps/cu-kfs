package edu.cornell.kfs.pdp.service.impl;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.pdp.businessobject.Batch;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.LoadPaymentStatus;
import org.kuali.kfs.pdp.businessobject.PaymentFileLoad;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.service.impl.PaymentFileServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.rice.krad.util.MessageMap;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.pdp.CUPdpConstants;

public class CuPaymentFileServiceImpl extends PaymentFileServiceImpl {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuPaymentFileServiceImpl.class);
    
    public CuPaymentFileServiceImpl() {
        super();
    }
    
    @Override
    public void loadPayments(PaymentFileLoad paymentFile, LoadPaymentStatus status, String incomingFileName) {
        status.setChart(paymentFile.getChart());
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
            businessObjectService.save(paymentGroup);
        }

        // send list of warnings
        paymentFileEmailService.sendLoadEmail(paymentFile, warnings);
        if (paymentFile.isTaxEmailRequired()) {
            paymentFileEmailService.sendTaxEmail(paymentFile);
        }

        removeDoneFile(incomingFileName);

        LOG.debug("loadPayments() was successful");
        status.setLoadStatus(LoadPaymentStatus.LoadStatus.SUCCESS);
    }
    
    @Override
    protected PaymentFileLoad parsePaymentFile(BatchInputFileType paymentInputFileType, String incomingFileName, MessageMap errorMap) {
        FileInputStream fileContents;
        try {
            fileContents = new FileInputStream(incomingFileName);
        }
        catch (FileNotFoundException e1) {
            LOG.error("file to load not found " + incomingFileName, e1);
            throw new RuntimeException("Cannot find the file requested to be loaded " + incomingFileName, e1);
        }

        // do the parse
        PaymentFileLoad paymentFile = null;
        try {
            byte[] fileByteContent = IOUtils.toByteArray(fileContents);
            paymentFile = (PaymentFileLoad) batchInputFileService.parse(paymentInputFileType, fileByteContent);
        }
        catch (IOException e) {
            LOG.error("error while getting file bytes:  " + e.getMessage(), e);
            throw new RuntimeException("Error encountered while attempting to get file bytes: " + e.getMessage(), e);
        }
        catch (ParseException e1) {
            LOG.error("Error parsing xml " + e1.getMessage());
            errorMap.putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_BATCH_UPLOAD_PARSING_XML, new String[] { e1.getMessage() });
            // Get customer object from unparsable file so error email can be sent.
            paymentFile = getCustomerProfileFromUnparsableFile(incomingFileName, paymentFile);
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
            pg.setDisbursementTypeCode(CUPdpConstants.DisbursementTypeCodes.ACH);
        } else {
            pg.setDisbursementTypeCode(CUPdpConstants.DisbursementTypeCodes.CHECK);
        }
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
            InputStreamReader inputReader = new InputStreamReader(exFileContents);
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
                    paymentFile.setChart(chartVal);
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

    
}
