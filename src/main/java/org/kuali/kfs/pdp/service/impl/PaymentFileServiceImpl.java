/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.pdp.service.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.businessobject.Batch;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.LoadPaymentStatus;
import org.kuali.kfs.pdp.businessobject.PaymentFileLoad;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.service.CustomerProfileService;
import org.kuali.kfs.pdp.service.PaymentFileService;
import org.kuali.kfs.pdp.service.PaymentFileValidationService;
import org.kuali.kfs.pdp.service.PdpEmailService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.InitiateDirectoryBase;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.List;

@Transactional
public class PaymentFileServiceImpl extends InitiateDirectoryBase implements PaymentFileService {
    private static final Logger LOG = LogManager.getLogger();

    protected String outgoingDirectoryName;

    protected ParameterService parameterService;
    protected CustomerProfileService customerProfileService;
    protected BatchInputFileService batchInputFileService;
    protected PaymentFileValidationService paymentFileValidationService;
    protected BusinessObjectService businessObjectService;
    protected DateTimeService dateTimeService;
    protected PdpEmailService paymentFileEmailService;
    protected ConfigurationService kualiConfigurationService;

    public PaymentFileServiceImpl() {
        super();
    }

    @Override
    public void processPaymentFiles(final BatchInputFileType paymentInputFileType) {
        final List<String> fileNamesToLoad = batchInputFileService.listInputFileNamesWithDoneFile(paymentInputFileType);

        for (final String incomingFileName : fileNamesToLoad) {
            try {
                LOG.debug("processPaymentFiles() Processing {}", incomingFileName);

                // collect various information for status of load
                final LoadPaymentStatus status = new LoadPaymentStatus();
                status.setMessageMap(new MessageMap());

                // process payment file
                final PaymentFileLoad paymentFile = processPaymentFile(paymentInputFileType, incomingFileName,
                        status.getMessageMap());
                if (paymentFile != null && paymentFile.isPassedValidation()) {
                    // load payment data
                    loadPayments(paymentFile, status, incomingFileName);
                    createOutputFile(status, incomingFileName);
                } else {
                    //if we encounter an error for the payment file, we will remove the .done file so it will not be
                    // parse again
                    LOG.error(
                            "Encounter a problem while processing payment file: {} .  Removing the done file to stop "
                            + "re-process.",
                            incomingFileName
                    );
                    removeDoneFile(incomingFileName);
                }
            } catch (final RuntimeException e) {
                LOG.error("Caught exception trying to load payment file: {}", incomingFileName, e);
                // swallow exception so we can continue processing files, the errors have been reported by email
            }
        }
    }

    /**
     * Attempt to parse the file, run validations, and store batch data
     *
     * @param paymentInputFileType {@link BatchInputFileType} for payment files
     * @param incomingFileName     name of payment file
     * @param errorMap             Map of errors
     * @return {@link LoadPaymentStatus} containing status data for load
     */
    protected PaymentFileLoad processPaymentFile(
            final BatchInputFileType paymentInputFileType, final String incomingFileName,
            final MessageMap errorMap) {
        // parse xml, if errors found return with failure
        final PaymentFileLoad paymentFile = parsePaymentFile(paymentInputFileType, incomingFileName, errorMap);

        // if no parsing error, do further validation
        if (errorMap.hasNoErrors()) {
            doPaymentFileValidation(paymentFile, errorMap);
        }

        //TODO FSKD-5416 KFSCNTRB ???
        // if any error from parsing or post-parsing validation, send error email notice
        if (errorMap.hasErrors()) {
            paymentFileEmailService.sendErrorEmail(paymentFile, errorMap);
        }

        return paymentFile;
    }

    @Override
    public void doPaymentFileValidation(final PaymentFileLoad paymentFile, final MessageMap errorMap) {
        paymentFileValidationService.doHardEdits(paymentFile, errorMap);

        //TODO FSKD-5416 KFSCNTRB ???
        if (errorMap.hasErrors()) {
            // move the sending error email notice logic into the caller processPaymentFile since we need to send such
            // notice on and both parsing error and post-parsing validation error
            // paymentFileEmailService.sendErrorEmail(paymentFile, errorMap);
            // set validation failed
            paymentFile.setPassedValidation(false);
        } else {
            // set validation succeeded
            paymentFile.setPassedValidation(true);
        }
    }

    @Override
    public void loadPayments(final PaymentFileLoad paymentFile, final LoadPaymentStatus status, final String incomingFileName) {
        status.setChart(paymentFile.getCampus());
        status.setUnit(paymentFile.getUnit());
        status.setSubUnit(paymentFile.getSubUnit());
        status.setCreationDate(paymentFile.getCreationDate());
        status.setDetailCount(paymentFile.getActualPaymentCount());
        status.setDetailTotal(paymentFile.getCalculatedPaymentTotalAmount());

        // create batch record for payment load
        final Batch batch = createNewBatch(paymentFile, getBaseFileName(incomingFileName));
        businessObjectService.save(batch);

        paymentFile.setBatchId(batch.getId());
        status.setBatchId(batch.getId());

        // do warnings and set defaults
        final List<String> warnings = paymentFileValidationService.doSoftEdits(paymentFile);
        status.setWarnings(warnings);

        // store groups
        for (final PaymentGroup paymentGroup : paymentFile.getPaymentGroups()) {
            businessObjectService.save(paymentGroup);
        }

        // send list of warnings
        paymentFileEmailService.sendLoadEmail(paymentFile, warnings);

        removeDoneFile(incomingFileName);

        LOG.debug("loadPayments() was successful");
        status.setLoadStatus(LoadPaymentStatus.LoadStatus.SUCCESS);
    }

    /**
     * Calls {@link BatchInputFileService} to validate XML against schema and parse.
     *
     * @param paymentInputFileType {@link BatchInputFileType} for payment files
     * @param incomingFileName     name of the payment file to parse
     * @param errorMap             any errors encountered while parsing are adding to
     * @return {@link PaymentFileLoad} containing the parsed values.
     */
    protected PaymentFileLoad parsePaymentFile(
            final BatchInputFileType paymentInputFileType, final String incomingFileName,
            final MessageMap errorMap) {
        final FileInputStream fileContents;
        try {
            fileContents = new FileInputStream(incomingFileName);
        } catch (final FileNotFoundException e1) {
            LOG.error("file to load not found {}", incomingFileName, e1);
            throw new RuntimeException("Cannot find the file requested to be loaded " + incomingFileName, e1);
        }

        // do the parse
        PaymentFileLoad paymentFile = null;
        try {
            final byte[] fileByteContent = IOUtils.toByteArray(fileContents);
            paymentFile = (PaymentFileLoad) batchInputFileService.parse(paymentInputFileType, fileByteContent);
        } catch (final IOException e) {
            LOG.error("error while getting file bytes:  {}", e::getMessage, () -> e);
            throw new RuntimeException("Error encountered while attempting to get file bytes: " + e.getMessage(), e);
        } catch (final ParseException e1) {
            LOG.error("Error parsing xml {}", e1::getMessage);
            errorMap.putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_BATCH_UPLOAD_PARSING,
                    e1.getMessage());
        }

        return paymentFile;
    }

    @Override
    public boolean createOutputFile(final LoadPaymentStatus status, final String inputFileName) {
        //add a step to check for directory paths
        prepareDirectories(getRequiredDirectoryNames());

        // construct the outgoing file name
        final String filename = outgoingDirectoryName + "/" + getBaseFileName(inputFileName);

        // set code-message indicating overall load status
        final String code;
        String message;
        if (LoadPaymentStatus.LoadStatus.SUCCESS.equals(status.getLoadStatus())) {
            code = "SUCCESS";
            message = "Successful Load";
        } else {
            code = "FAIL";
            message = "Load Failed: ";
            final List<ErrorMessage> errorMessages = status.getMessageMap().getMessages(KFSConstants.GLOBAL_ERRORS);
            for (final ErrorMessage errorMessage : errorMessages) {
                String resourceMessage = kualiConfigurationService.getPropertyValueAsString(errorMessage.getErrorKey());
                resourceMessage = MessageFormat.format(resourceMessage, (Object[]) errorMessage.getMessageParameters());
                message += resourceMessage + ", ";
            }
        }

        try (FileOutputStream out = new FileOutputStream(filename);
             PrintStream p = new PrintStream(out, false, StandardCharsets.UTF_8)) {

            p.println("<pdp_load_status>");
            p.println("  <input_file_name>" + inputFileName + "</input_file_name>");
            p.println("  <code>" + code + "</code>");
            p.println("  <count>" + status.getDetailCount() + "</count>");
            if (status.getDetailTotal() != null) {
                p.println("  <total>" + status.getDetailTotal() + "</total>");
            } else {
                p.println("  <total>0</total>");
            }

            p.println("  <description>" + message + "</description>");
            
            //Cornell Mod
            if(ObjectUtils.isNotNull(status.getWarnings())) { // Warnings list may be null if file failed to load.
                p.println("  <messages>");
                for (final String warning : status.getWarnings()) {
                    p.println("    <message>" + warning + "</message>");
                }
                p.println("  </messages>");
            }
            
            p.println("</pdp_load_status>");

            // creating .done file
            final File doneFile = new File(filename.substring(0, filename.lastIndexOf(".")) + ".done");
            doneFile.createNewFile();
        } catch (final FileNotFoundException e) {
            LOG.error("createOutputFile() Cannot create output file", e);
            return false;
        } catch (final IOException e) {
            LOG.error("createOutputFile() Cannot write to output file", e);
            return false;
        }

        return true;
    }

    /**
     * Create a new {@link Batch} record for the payment file.
     *
     * @param paymentFile parsed payment file object
     * @param fileName    payment file name (without path)
     * @return {@link Batch} object
     */
    protected Batch createNewBatch(final PaymentFileLoad paymentFile, final String fileName) {
        final Timestamp now = dateTimeService.getCurrentTimestamp();

        final Batch batch = new Batch();

        final CustomerProfile customer = customerProfileService.get(paymentFile.getCampus(), paymentFile.getUnit(),
                paymentFile.getSubUnit());
        batch.setCustomerProfile(customer);
        batch.setCustomerFileCreateTimestamp(new Timestamp(paymentFile.getCreationDate().getTime()));
        batch.setFileProcessTimestamp(now);
        batch.setPaymentCount(new KualiInteger(paymentFile.getPaymentCount()));

        if (fileName.length() > 30) {
            batch.setPaymentFileName(fileName.substring(0, 30));
        } else {
            batch.setPaymentFileName(fileName);
        }

        batch.setPaymentTotalAmount(paymentFile.getPaymentTotalAmount());
        batch.setSubmiterUserId(GlobalVariables.getUserSession().getPerson().getPrincipalId());

        return batch;
    }

    /**
     * @return the file name from the file full path.
     */
    protected String getBaseFileName(String filename) {
        // Replace any backslashes with forward slashes. Works on Windows or Unix
        filename = filename.replaceAll("\\\\", "/");

        int startingPointer = filename.length() - 1;
        while (startingPointer > 0 && filename.charAt(startingPointer) != '/') {
            startingPointer--;
        }

        return filename.substring(startingPointer + 1);
    }

    /**
     * Clears out the associated .done file for the processed data file.
     *
     * @param dataFileName the name of date file with done file to remove.
     */
    protected void removeDoneFile(final String dataFileName) {
        final File doneFile = new File(StringUtils.substringBeforeLast(dataFileName, ".") + ".done");
        if (doneFile.exists()) {
            doneFile.delete();
        }
    }

    public void setOutgoingDirectoryName(final String outgoingDirectoryName) {
        this.outgoingDirectoryName = outgoingDirectoryName;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setCustomerProfileService(final CustomerProfileService customerProfileService) {
        this.customerProfileService = customerProfileService;
    }

    public void setBatchInputFileService(final BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public void setPaymentFileValidationService(final PaymentFileValidationService paymentFileValidationService) {
        this.paymentFileValidationService = paymentFileValidationService;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setDateTimeService(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setPaymentFileEmailService(final PdpEmailService paymentFileEmailService) {
        this.paymentFileEmailService = paymentFileEmailService;
    }

    public void setConfigurationService(final ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    @Override
    public List<String> getRequiredDirectoryNames() {
        return List.of(outgoingDirectoryName);
    }
}
