/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.pdp.batch.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.dataaccess.DisbursementVoucherDao;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.businessobject.ACHBank;
import org.kuali.kfs.pdp.businessobject.Batch;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentNoteText;
import org.kuali.kfs.pdp.businessobject.PaymentProcess;
import org.kuali.kfs.pdp.businessobject.PaymentStatus;
import org.kuali.kfs.pdp.businessobject.ProcessSummary;
import org.kuali.kfs.pdp.businessobject.options.StandardEntryClassValuesFinder.StandardEntryClass;
import org.kuali.kfs.pdp.dataaccess.ProcessDao;
import org.kuali.kfs.pdp.service.AchBankService;
import org.kuali.kfs.pdp.service.AchService;
import org.kuali.kfs.pdp.service.PaymentDetailService;
import org.kuali.kfs.pdp.service.PaymentGroupService;
import org.kuali.kfs.pdp.service.PdpEmailService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.XmlUtilService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;

import com.prowidesoftware.swift.model.mx.MxPain00100103;
import com.prowidesoftware.swift.model.mx.dic.AccountIdentification4Choice;
import com.prowidesoftware.swift.model.mx.dic.ActiveOrHistoricCurrencyAndAmount;
import com.prowidesoftware.swift.model.mx.dic.AmountType3Choice;
import com.prowidesoftware.swift.model.mx.dic.BranchAndFinancialInstitutionIdentification4;
import com.prowidesoftware.swift.model.mx.dic.CashAccount16;
import com.prowidesoftware.swift.model.mx.dic.CashAccountType2;
import com.prowidesoftware.swift.model.mx.dic.CashAccountType4Code;
import com.prowidesoftware.swift.model.mx.dic.Cheque6;
import com.prowidesoftware.swift.model.mx.dic.ChequeDeliveryMethod1Choice;
import com.prowidesoftware.swift.model.mx.dic.ClearingSystemMemberIdentification2;
import com.prowidesoftware.swift.model.mx.dic.CreditTransferTransactionInformation10;
import com.prowidesoftware.swift.model.mx.dic.CreditorReferenceInformation2;
import com.prowidesoftware.swift.model.mx.dic.CreditorReferenceType1Choice;
import com.prowidesoftware.swift.model.mx.dic.CreditorReferenceType2;
import com.prowidesoftware.swift.model.mx.dic.CustomerCreditTransferInitiationV03;
import com.prowidesoftware.swift.model.mx.dic.DocumentAdjustment1;
import com.prowidesoftware.swift.model.mx.dic.DocumentType5Code;
import com.prowidesoftware.swift.model.mx.dic.FinancialInstitutionIdentification7;
import com.prowidesoftware.swift.model.mx.dic.GenericAccountIdentification1;
import com.prowidesoftware.swift.model.mx.dic.GenericOrganisationIdentification1;
import com.prowidesoftware.swift.model.mx.dic.GroupHeader32;
import com.prowidesoftware.swift.model.mx.dic.LocalInstrument2Choice;
import com.prowidesoftware.swift.model.mx.dic.OrganisationIdentification4;
import com.prowidesoftware.swift.model.mx.dic.OrganisationIdentificationSchemeName1Choice;
import com.prowidesoftware.swift.model.mx.dic.Party6Choice;
import com.prowidesoftware.swift.model.mx.dic.PartyIdentification32;
import com.prowidesoftware.swift.model.mx.dic.PaymentIdentification1;
import com.prowidesoftware.swift.model.mx.dic.PaymentInstructionInformation3;
import com.prowidesoftware.swift.model.mx.dic.PaymentMethod3Code;
import com.prowidesoftware.swift.model.mx.dic.PaymentTypeInformation19;
import com.prowidesoftware.swift.model.mx.dic.PostalAddress6;
import com.prowidesoftware.swift.model.mx.dic.ReferredDocumentInformation3;
import com.prowidesoftware.swift.model.mx.dic.ReferredDocumentType1Choice;
import com.prowidesoftware.swift.model.mx.dic.ReferredDocumentType2;
import com.prowidesoftware.swift.model.mx.dic.RemittanceAmount1;
import com.prowidesoftware.swift.model.mx.dic.RemittanceInformation5;
import com.prowidesoftware.swift.model.mx.dic.ServiceLevel8Choice;
import com.prowidesoftware.swift.model.mx.dic.StructuredRemittanceInformation7;

import edu.cornell.kfs.fp.document.CuDisbursementVoucherConstants;
import edu.cornell.kfs.pdp.CUPdpConstants.Iso20022Constants;
import edu.cornell.kfs.pdp.CUPdpConstants.Iso20022Constants.MessageIdSuffixes;
import edu.cornell.kfs.pdp.CUPdpKeyConstants;
import edu.cornell.kfs.pdp.CUPdpParameterConstants;
import edu.cornell.kfs.pdp.batch.service.impl.PaymentUrgency;
import edu.cornell.kfs.pdp.service.CuCheckStubService;
import edu.cornell.kfs.pdp.service.CuPaymentDetailService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.exception.ManyFIPSCountriesForNameException;
import edu.cornell.kfs.sys.exception.ManyFIPStoISOMappingException;
import edu.cornell.kfs.sys.exception.NoFIPSCountriesForNameException;
import edu.cornell.kfs.sys.exception.NoFIPStoISOMappingException;
import edu.cornell.kfs.sys.service.ISOFIPSConversionService;

/**
 * ====
 * CU Customization: Modified this class extensively to meet Cornell's needs. See the other code comments for details.
 * ====
 * 
 * Extract in ISO 20022 format (i.e. Credit Transfer PAIN.001.001.03 XML file structure).
 *
 * There will be one XML file per "payment process".
 */
public class Iso20022FormatExtractor {

    private static final Logger LOG = LogManager.getLogger();

    /*
     * CU Customization: Removed the private ADDTL_RMT_INF_MAX_LENGTH constant because our customized code
     * no longer uses it.
     */

    private static final String CURRENCY_USD = "USD";
    private static final int REF_MAX_LENGTH = 30;

    /*
     * CU Customization: Removed the private USTRD_MAX_LENGTH constant because our customized code no longer uses it.
     */

    private static final int VENDOR_NUM_MAX_LENGTH = 20;

    private final AchService achService;
    private final AchBankService achBankService;
    private final BusinessObjectService businessObjectService;
    private final ConfigurationService configurationService;
    private final DateTimeService dateTimeService;
    private final DisbursementVoucherDao disbursementVoucherDao;
    private final ParameterService parameterService;
    private final PaymentDetailService paymentDetailService;
    private final PaymentGroupService paymentGroupService;
    private final PdpEmailService pdpEmailService;
    private final ProcessDao processDao;
    private final XmlUtilService xmlUtilService;
    /*
     * CU Customization: Added service for converting country codes/names into ISO country code format.
     *     Also added service to assist with handling Check Stubs (aka Additional Remittance Info).
     */
    private final ISOFIPSConversionService isoFipsConversionService;
    private final CuCheckStubService cuCheckStubService;

    private enum CheckDeliveryPriority {
        // 00000: Mail via Post to creditor's address. (Default if 'prtry' is omitted.)
        NORMAL("00000"),

        // 00PY1: Overnight delivery of check to creditor's address
        OVERNIGHT_TO_CREDITOR("00PY1"),

        // 00HQ1: Overnight delivery of check to designated office address
        OVERNIGHT_TO_DESIGNATED_OFFICE("00HQ1");

        private final String value;

        CheckDeliveryPriority(final String value) {
            this.value = value;
        }

        String getValue() {
            return value;
        }
    }

    /*
     * CU Customization: Added setup for ISO FIPS conversion service.
     * Also added checking of paymentDetailService parameter to require it to implement CuPaymentDetailService.
     * Also added setup for Check Stub helper service.
     */
    public Iso20022FormatExtractor(
            final AchService achService,
            final AchBankService achBankService,
            final BusinessObjectService businessObjectService,
            final ConfigurationService configurationService,
            final DateTimeService dateTimeService,
            final DisbursementVoucherDao disbursementVoucherDao,
            final ParameterService parameterService,
            final PaymentDetailService paymentDetailService,
            final PaymentGroupService paymentGroupService,
            final PdpEmailService pdpEmailService,
            final ProcessDao processDao,
            final XmlUtilService xmlUtilService,
            final ISOFIPSConversionService isoFipsConversionService,
            final CuCheckStubService cuCheckStubService
    ) {
        this.achService = achService;
        this.achBankService = achBankService;
        this.businessObjectService = businessObjectService;
        this.configurationService = configurationService;
        this.dateTimeService = dateTimeService;
        this.disbursementVoucherDao = disbursementVoucherDao;
        this.parameterService = parameterService;
        this.paymentDetailService = paymentDetailService;
        this.paymentGroupService = paymentGroupService;
        this.pdpEmailService = pdpEmailService;
        this.processDao = processDao;
        this.xmlUtilService = xmlUtilService;
        this.isoFipsConversionService = isoFipsConversionService;
        this.cuCheckStubService = cuCheckStubService;
        if (!(paymentDetailService instanceof CuPaymentDetailService)) {
            throw new IllegalArgumentException("paymentDetailService should have implemented CuPaymentDetailService");
        }
    }

    /*
     * CU Customization: Increased this method's visibility to public.
     */
    public void extractAchs(
            final PaymentStatus extractedStatus,
            final String directoryName
    ) {
        LOG.debug("extractAchs(...) - Enter : directoryName={}", directoryName);

        final Date disbursementDate = dateTimeService.getCurrentDate();
        LOG.debug("extractAchs(...) - : disbursementDate={}", disbursementDate);

        final List<PaymentProcess> paymentProcessList = determineUniquePaymentProcesses();
        LOG.info(
                "extractAchs(...) - Extracting PaymentProcesses: numberOfPaymentProcesses={}",
                paymentProcessList::size
        );
        for (final PaymentProcess paymentProcess : paymentProcessList) {
            LOG.debug("extractAchs(...) - : paymentProcess={}", paymentProcess);

            final AchExtractTypeContext extractTypeContext =
                    new AchExtractTypeContext(disbursementDate, extractedStatus, paymentProcess);

            final CustomerCreditTransferInitiationV03 customerCreditTransferInitiation =
                    constructCustomerCreditTransferInitiation(extractTypeContext);

            final MxPain00100103 message = new MxPain00100103();
            message.setCstmrCdtTrfInitn(customerCreditTransferInitiation);

            final String filename = determineFilename(directoryName, extractTypeContext);
            writeMessageToFile(message, filename);

            pdpEmailService.sendAchSummaryEmail(
                    extractTypeContext.getUnitCounts(),
                    extractTypeContext.getUnitTotals(),
                    dateTimeService.getCurrentDate()
            );

            createDoneFile(filename);
        }

        LOG.debug("extractAchs(...) - Exit");
    }

    private List<PaymentProcess> determineUniquePaymentProcesses() {
        final List<Integer> uniquePaymentProcessIds = new LinkedList<>();
        final List<PaymentProcess> uniquePaymentProcessList = new LinkedList<>();

        final Iterator<PaymentGroup> paymentGroupIterator =
                paymentGroupService.getByDisbursementTypeStatusCode(
                        PdpConstants.DisbursementTypeCodes.ACH,
                        PdpConstants.PaymentStatusCodes.PENDING_ACH
                );
        while (paymentGroupIterator.hasNext()) {
            final PaymentGroup paymentGroup = paymentGroupIterator.next();
            final PaymentProcess paymentProcess = paymentGroup.getProcess();
            final int processId = paymentProcess.getId().intValue();
            if (uniquePaymentProcessIds.contains(processId)) {
                continue;
            }
            uniquePaymentProcessIds.add(processId);
            uniquePaymentProcessList.add(paymentProcess);
        }

        LOG.debug(
                "determineUniquePaymentProcesses() - Exit : uniquePaymentProcessIds={}; uniquePaymentProcessList={}",
                uniquePaymentProcessIds,
                uniquePaymentProcessList
        );
        return uniquePaymentProcessList;
    }

    public void extractChecks(
            final PaymentStatus extractedStatus,
            final String directoryName
    ) {
        LOG.debug("extractChecks(...) - Enter : directoryName={}", directoryName);

        final Date disbursementDate = dateTimeService.getCurrentDate();
        LOG.debug("extractChecks(...) - : disbursementDate={}", disbursementDate);

        // Formatted but not Extracted
        final List<PaymentProcess> paymentProcessList = processDao.getAllExtractsToRun();
        LOG.info(
                "extractChecks(...) - Extracting PaymentProcesses: numberOfPaymentProcesses={}",
                paymentProcessList::size
        );
        for (final PaymentProcess paymentProcess : paymentProcessList) {
            LOG.debug("extractChecks(...) - : paymentProcess={}", paymentProcess);

            /*
             * =========
             * CU Customization: Updated the Check extraction and wrapped it in a nested loop so that it runs twice:
             * Once for regular payments and once for immediate payments.
             * =========
             */
            PaymentUrgency[] paymentUrgencies = { PaymentUrgency.REGULAR, PaymentUrgency.IMMEDIATE };
            for (PaymentUrgency urgency : paymentUrgencies) {
                final CheckExtractTypeContext extractTypeContext =
                        new CheckExtractTypeContext(disbursementDate, extractedStatus, paymentProcess, urgency);

                final CustomerCreditTransferInitiationV03 customerCreditTransferInitiation =
                        constructCustomerCreditTransferInitiation(extractTypeContext);

                if (checksWereExtracted(customerCreditTransferInitiation)) {
                    final MxPain00100103 message = new MxPain00100103();
                    message.setCstmrCdtTrfInitn(customerCreditTransferInitiation);

                    final String filename = determineFilename(directoryName, extractTypeContext);
                    writeMessageToFile(message, filename);

                    createDoneFile(filename);
                }
            }
            /*
             * =========
             * End CU Customization Block
             * =========
             */

            paymentProcess.setExtractedInd(true);
            businessObjectService.save(paymentProcess);
        }

        LOG.debug("extractChecks(...) - Exit");
    }

    private CustomerCreditTransferInitiationV03 constructCustomerCreditTransferInitiation(
            final ExtractTypeContext extractTypeContext
    ) {
        LOG.debug(
                "constructCustomerCreditTransferInitiation(...) - Enter : extractTypeContext={}",
                extractTypeContext
        );

        final CustomerCreditTransferInitiationV03 customerCreditTransferInitiation =
                new CustomerCreditTransferInitiationV03();

        // There will be one element per bank account
        final Set<PaymentInstructionInformation3> paymentInstructionInformationSet =
                constructPaymentInstructionInformationSet(extractTypeContext);
        paymentInstructionInformationSet.forEach(customerCreditTransferInitiation::addPmtInf);

        final GroupHeader32 groupHeader = constructGroupHeader(extractTypeContext, paymentInstructionInformationSet);
        customerCreditTransferInitiation.setGrpHdr(groupHeader);

        LOG.debug(
                "constructCustomerCreditTransferInitiation(...) - Exit : customerCreditTransferInitiation={}",
                customerCreditTransferInitiation
        );
        return customerCreditTransferInitiation;
    }

    /*
     * This is a little awkward..but so is the data structure we're working with here. Ordinarily I'm against output
     * parameters; however this method will:
     *  - Increments transactionCounter parameter
     *  - Add to the amounts List
     */
    private Set<PaymentInstructionInformation3> constructPaymentInstructionInformationSet(
            final ExtractTypeContext extractTypeContext
    ) {
        LOG.debug(
                "constructPaymentInJstructionInformationList(...) - Enter : extractTypeContext={}",
                extractTypeContext
        );

        final Set<PaymentInstructionInformation3> paymentInstructionInformationSet = new HashSet<>();

        final String disbursementType = extractTypeContext.isExtractionType(ExtractionType.ACH)
                ? PdpConstants.DisbursementTypeCodes.ACH
                : PdpConstants.DisbursementTypeCodes.CHECK;

        final int processId = extractTypeContext.getPaymentProcess().getId().intValue();
        final List<String> bankCodes =
                paymentGroupService.getDistinctBankCodesForProcessAndType(processId, disbursementType);

        int bankAccountCounter = 0;
        for (final String bankCode : bankCodes) {
            bankAccountCounter++;
            LOG.debug(
                    "constructPaymentInstructionInformationSet(...) - : bankCode={}; bankAccountCounter={}",
                    bankCode,
                    bankAccountCounter
            );

            final List<Integer> disbursementNumbersByBankCode =
                    paymentGroupService.getDisbursementNumbersByDisbursementTypeAndBankCode(
                            processId,
                            disbursementType,
                            bankCode
                    );

            final MultiValuedMap<Integer, PaymentDetail> disbursementPaymentDetails = new ArrayListValuedHashMap<>();
            for (final Integer disbursementNumber : disbursementNumbersByBankCode) {
                LOG.debug(
                        "constructPaymentInstructionInformationSet(...) - : disbursementNumber={}",
                        disbursementNumber
                );

                /*
                 * ==========
                 * CU Customization: Added call to a custom helper method to retrieve and filter Check payments
                 *         based on payment urgency. Also added ability to skip the current inner loop iteration
                 *         if the check/disbursement had no matching payments at the given urgency level.
                 * ==========
                 */
                final Iterator<PaymentDetail> paymentDetailIterator;
                if (extractTypeContext.isExtractionType(ExtractionType.ACH)) {
                    paymentDetailIterator =
                            paymentDetailService.getByDisbursementNumber(
                                    disbursementNumber,
                                    processId,
                                    disbursementType,
                                    bankCode
                            );
                } else if (extractTypeContext.isExtractionType(ExtractionType.CHECK)) {
                    final boolean processImmediate = getProcessImmediateFlagForCheckExtraction(extractTypeContext);
                    paymentDetailIterator =
                            ((CuPaymentDetailService) paymentDetailService).getByDisbursementNumber(
                                    disbursementNumber,
                                    processId,
                                    disbursementType,
                                    bankCode,
                                    processImmediate
                            );
                } else {
                    throw new IllegalStateException("Context of type " + extractTypeContext.getClass().getName()
                            + " was for neither Check nor ACH; this should NEVER happen");
                }
                final Collection<PaymentDetail> paymentDetails = IteratorUtils.toList(paymentDetailIterator);
                if (paymentDetails.isEmpty()) {
                    continue;
                }
                disbursementPaymentDetails.putAll(disbursementNumber, paymentDetails);
                /*
                 * ==========
                 * End CU Customization Block
                 * ==========
                 */

                paymentDetails.forEach(paymentDetail -> {
                    LOG.debug("constructPaymentInstructionInformationSet(...) - : paymentDetail={}", paymentDetail);

                    if (extractTypeContext.isExtractionType(ExtractionType.ACH)) {
                        updateAchCountersForNotificationEmail(extractTypeContext, paymentDetail);
                    }

                    final PaymentGroup paymentGroup = paymentDetail.getPaymentGroup();

                    final Date disbursementDate = extractTypeContext.getDisbursementDate();
                    paymentGroup.setDisbursementDate(new java.sql.Date(disbursementDate.getTime()));

                    paymentGroup.setPaymentStatus(extractTypeContext.getExtractedStatus());

                    businessObjectService.save(paymentGroup);
                });
            }

            if (disbursementPaymentDetails.isEmpty()) {
                // Nothing to do; try the next bankCode
                continue;
            }

            final PaymentInstructionInformation3 paymentInstructionInformation =
                    constructPaymentInstructionInformation(
                            bankAccountCounter,
                            bankCode,
                            disbursementPaymentDetails,
                            extractTypeContext
                    );
            paymentInstructionInformationSet.add(paymentInstructionInformation);
        }

        LOG.debug(
                "constructPaymentInstructionInformationSet(...) - Exit : paymentInstructionInformationSet={}",
                paymentInstructionInformationSet
        );
        return paymentInstructionInformationSet;
    }

    /*
     * CU Customization: Added helper method for determining expected Immediate Payment flag based on payment urgency.
     */
    private boolean getProcessImmediateFlagForCheckExtraction(
            final ExtractTypeContext extractTypeContext
    ) {
        if (extractTypeContext.isLimitedToPaymentsWithUrgency(PaymentUrgency.REGULAR)) {
            return false;
        } else if (extractTypeContext.isLimitedToPaymentsWithUrgency(PaymentUrgency.IMMEDIATE)) {
            return true;
        } else {
            throw new IllegalArgumentException("Context of type " + extractTypeContext.getClass().getName()
                    + " does not filter payments based on urgency");
        }
    }

    private static void updateAchCountersForNotificationEmail(
            final ExtractTypeContext extractTypeContext,
            final PaymentDetail paymentDetail
    ) {
        final CustomerProfile templateCustomerProfile = getCustomerProfile(paymentDetail.getPaymentGroup());
        final String unit = String.format("%s-%s-%s",
                templateCustomerProfile.getCampusCode(),
                templateCustomerProfile.getUnitCode(),
                templateCustomerProfile.getSubUnitCode()
        );
        final AchExtractTypeContext achExtractTypeContext = (AchExtractTypeContext) extractTypeContext;
        achExtractTypeContext.incrementUnitCount(unit);
        achExtractTypeContext.addToUnitTotals(unit, paymentDetail.getNetPaymentAmount());
    }

    private GroupHeader32 constructGroupHeader(
            final ExtractTypeContext extractTypeContext,
            final Set<PaymentInstructionInformation3> paymentInstructionInformationSet
    ) {
        final GroupHeader32 groupHeader = new GroupHeader32();

        /*
         * CU Customization: Append an extra suffix to the Message ID based on extraction type,
         *         to satisfy the bank's uniqueness requirements for Message IDs.
         */
        final int processId = extractTypeContext.getPaymentProcess().getId().intValue();
        final String messageId = Integer.toString(processId) + determineMessageIdSuffix(extractTypeContext);
        groupHeader.setMsgId(messageId);

        final Date disbursementDate = extractTypeContext.getDisbursementDate();
        final XMLGregorianCalendar creditDateTime = constructXmlGregorianCalendarWithDateAndTime(disbursementDate);
        groupHeader.setCreDtTm(creditDateTime);

        final int numberOfTransactions =
                paymentInstructionInformationSet.stream()
                        .map(PaymentInstructionInformation3::getNbOfTxs)
                        .mapToInt(Integer::parseInt)
                        .sum();
        final String numberOfTransactionsString = Integer.toString(numberOfTransactions);
        groupHeader.setNbOfTxs(numberOfTransactionsString);

        final BigDecimal controlSum =
                paymentInstructionInformationSet.stream()
                        .map(PaymentInstructionInformation3::getCtrlSum)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        groupHeader.setCtrlSum(controlSum);

        final String institutionName = retrieveInstitutionName();
        final PartyIdentification32 initiatingPartyIdentification = constructPartyIdentification(institutionName);
        groupHeader.setInitgPty(initiatingPartyIdentification);

        return groupHeader;
    }

    /*
     * CU Customization: Added method for deriving the suffix to append to the file's Message ID.
     */
    private String determineMessageIdSuffix(
            final ExtractTypeContext extractTypeContext
    ) {
        if (extractTypeContext.isExtractionType(ExtractionType.ACH)) {
            return MessageIdSuffixes.ACH;
        } else if (extractTypeContext.isExtractionType(ExtractionType.CHECK)) {
            boolean processImmediate = getProcessImmediateFlagForCheckExtraction(extractTypeContext);
            return processImmediate ? MessageIdSuffixes.IMMEDIATE_CHECK : MessageIdSuffixes.CHECK;
        } else {
            throw new IllegalStateException("Context of type " + extractTypeContext.getClass().getName()
                    + " was for neither Check nor ACH; this should NEVER happen");
        }
    }

    private boolean checksWereExtracted(final CustomerCreditTransferInitiationV03 customerCreditTransferInitiation) {
        return Integer.parseInt(customerCreditTransferInitiation.getGrpHdr().getNbOfTxs()) > 0;
    }

    private static XMLGregorianCalendar constructXmlGregorianCalendarWithDateOnly(
            final Date date
    ) {
        final XMLGregorianCalendar xmlGregorianCalendar = constructXmlGregorianCalendarWithDateAndTime(date);
        if (xmlGregorianCalendar == null) {
            return null;
        }

        // The XML should only include the date, not the time
        xmlGregorianCalendar.setHour(DatatypeConstants.FIELD_UNDEFINED);
        xmlGregorianCalendar.setMinute(DatatypeConstants.FIELD_UNDEFINED);
        xmlGregorianCalendar.setSecond(DatatypeConstants.FIELD_UNDEFINED);
        xmlGregorianCalendar.setTimezone(DatatypeConstants.FIELD_UNDEFINED);

        return xmlGregorianCalendar;
    }

    private static XMLGregorianCalendar constructXmlGregorianCalendarWithDateAndTime(
            final Date date
    ) {
        XMLGregorianCalendar xmlGregorianCalendar = null;
        try {
            final GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTime(date);

            xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
        } catch (final DatatypeConfigurationException e) {
            LOG.error("constructXmlGregorianCalendarWithDateAndTime(...) - Failed to create XMLGregorianCalendar", e);
        }
        return xmlGregorianCalendar;
    }

    private String retrieveInstitutionName() {
        return parameterService.getParameterValueAsString(
                KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class,
                KfsParameterConstants.INSTITUTION_NAME
        );
    }

    private static PartyIdentification32 constructPartyIdentification(final String partyName) {
        final PartyIdentification32 initiatingPartyIdentification = new PartyIdentification32();
        initiatingPartyIdentification.setNm(partyName);
        return initiatingPartyIdentification;
    }

    private static CustomerProfile getCustomerProfile(final PaymentGroup templatePaymentGroup) {
        final Batch batch = templatePaymentGroup.getBatch();
        return batch.getCustomerProfile();
    }

    private PaymentInstructionInformation3 constructPaymentInstructionInformation(
            final int paymentInformationId,
            final String bankCode,
            final MultiValuedMap<Integer, PaymentDetail> disbursementPaymentDetails,
            final ExtractTypeContext extractTypeContext
    ) {
        final PaymentInstructionInformation3 paymentInstructionInformation = new PaymentInstructionInformation3();

        final String paymentInformationIdStr = Integer.toString(paymentInformationId);
        paymentInstructionInformation.setPmtInfId(paymentInformationIdStr);

        final PaymentMethod3Code paymentMethod = determinePaymentMethodCode(extractTypeContext);
        paymentInstructionInformation.setPmtMtd(paymentMethod);

        final PaymentGroup processTemplatePaymentGroup =
                disbursementPaymentDetails.values().iterator().next().getPaymentGroup();

        /*
         * CU Customization: When processing ACH transactions, set the execution/extraction date to be one day later.
         */
        final Date disbursementDate = processTemplatePaymentGroup.getDisbursementDate();
        final Date adjustedExtractionDate;
        if (extractTypeContext.isExtractionType(ExtractionType.ACH)) {
            adjustedExtractionDate = new Date(
                    Instant.ofEpochMilli(disbursementDate.getTime())
                            .plus(1, ChronoUnit.DAYS)
                            .toEpochMilli()
            );
        } else {
            adjustedExtractionDate = disbursementDate;
        }
        final XMLGregorianCalendar extractionDate = constructXmlGregorianCalendarWithDateOnly(adjustedExtractionDate);
        paymentInstructionInformation.setReqdExctnDt(extractionDate);

        final PartyIdentification32 debtorPartyIdentification =
                constructDebtorPartyIdentification(processTemplatePaymentGroup, extractTypeContext);
        paymentInstructionInformation.setDbtr(debtorPartyIdentification);

        final CashAccount16 debtorAccount = constructDebtorAccount(processTemplatePaymentGroup);
        paymentInstructionInformation.setDbtrAcct(debtorAccount);

        final BranchAndFinancialInstitutionIdentification4 debtorAgent = constructDebtorAgent(processTemplatePaymentGroup);
        paymentInstructionInformation.setDbtrAgt(debtorAgent);

        /*
         * CU Customization: Updated to also add payment type info when processing immediate Checks.
         */
        if (extractTypeContext.isExtractionType(ExtractionType.ACH)) {
            final PaymentTypeInformation19 paymentTypeInformation =
                    constructPaymentTypeInformationWithServiceLevel(processTemplatePaymentGroup);
            paymentInstructionInformation.setPmtTpInf(paymentTypeInformation);
        } else if (extractTypeContext.isExtractionType(ExtractionType.CHECK)
                && extractTypeContext.isLimitedToPaymentsWithUrgency(PaymentUrgency.IMMEDIATE)) {
            final PaymentTypeInformation19 paymentTypeInformation =
                    constructPaymentTypeInformationForChecksWithImmediateProcessing();
            paymentInstructionInformation.setPmtTpInf(paymentTypeInformation);
        }

        int numberOfTransactions = 0;
        BigDecimal controlSum = BigDecimal.ZERO;
        for (final Integer disbursementNumber : disbursementPaymentDetails.keySet()) {
            final Collection<PaymentDetail> paymentDetails = disbursementPaymentDetails.get(disbursementNumber);
            final PaymentGroup disbursementTemplatePaymentGroup = paymentDetails.iterator().next().getPaymentGroup();
            final CreditTransferTransactionInformation10 creditTransferTransactionInformation =
                    constructCreditTransferTransactionInformation(
                            bankCode,
                            disbursementNumber,
                            paymentDetails,
                            disbursementTemplatePaymentGroup,
                            extractTypeContext
                    );
            paymentInstructionInformation.addCdtTrfTxInf(creditTransferTransactionInformation);

            numberOfTransactions++;
            controlSum = controlSum.add(creditTransferTransactionInformation.getAmt().getInstdAmt().getValue());
        }
        paymentInstructionInformation.setNbOfTxs(Integer.toString(numberOfTransactions));
        paymentInstructionInformation.setCtrlSum(controlSum);

        return paymentInstructionInformation;
    }

    private PaymentTypeInformation19 constructPaymentTypeInformationWithServiceLevel(
            final PaymentGroup templatePaymentGroup
    ) {
        final PaymentTypeInformation19 paymentTypeInformation = new PaymentTypeInformation19();

        final ServiceLevel8Choice serviceLevel = determineServiceLevelCode(templatePaymentGroup);
        paymentTypeInformation.setSvcLvl(serviceLevel);

        return paymentTypeInformation;
    }

    /*
     * CU Customization: Added method to create payment type info for immediate Checks.
     */
    private PaymentTypeInformation19 constructPaymentTypeInformationForChecksWithImmediateProcessing() {
        final PaymentTypeInformation19 paymentTypeInformation = new PaymentTypeInformation19();

        final LocalInstrument2Choice localInstrument = new LocalInstrument2Choice();
        localInstrument.setPrtry(Iso20022Constants.LOCAL_INSTRUMENT_ISSUE_ONLY);
        paymentTypeInformation.setLclInstrm(localInstrument);

        return paymentTypeInformation;
    }

    /*
     * URGP: Urgent Payment
     * NURG: Non-Urgent
     */
    private static ServiceLevel8Choice determineServiceLevelCode(
            final PaymentGroup templatePaymentGroup
    ) {
        final ServiceLevel8Choice serviceLevel = new ServiceLevel8Choice();

        final boolean processImmediate = templatePaymentGroup.getProcessImmediate();
        final String serviceLevelCode = processImmediate ? "URGP" : "NURG";
        serviceLevel.setCd(serviceLevelCode);

        return serviceLevel;
    }

    private static PaymentMethod3Code determinePaymentMethodCode(
            final ExtractTypeContext extractTypeContext
    ) {
        return extractTypeContext.isExtractionType(ExtractionType.CHECK)
                ? PaymentMethod3Code.CHK
                : PaymentMethod3Code.TRF;
    }

    private PartyIdentification32 constructDebtorPartyIdentification(
            final PaymentGroup templatePaymentGroup,
            final ExtractTypeContext extractTypeContext
    ) {
        final String institutionName = retrieveInstitutionName();
        final PartyIdentification32 debtorPartyIdentification = constructPartyIdentification(institutionName);

        final CustomerProfile templateCustomerProfile = getCustomerProfile(templatePaymentGroup);
        final PostalAddress6 debtorPostalAddress = constructPostalAddress(templateCustomerProfile);
        debtorPartyIdentification.setPstlAdr(debtorPostalAddress);

        if (extractTypeContext.isExtractionType(ExtractionType.ACH)) {
            final Party6Choice debtorId = constructDebtorId(templatePaymentGroup);
            debtorPartyIdentification.setId(debtorId);
        }

        return debtorPartyIdentification;
    }

    private static Party6Choice constructDebtorId(
            final PaymentGroup templatePaymentGroup
    ) {
        final String id = templatePaymentGroup.getBank().getAchInstitutionId();

        final String achCompanyScheme = templatePaymentGroup.getBank().getAchInstitutionSchemeName();
        final OrganisationIdentificationSchemeName1Choice schmeNm = new OrganisationIdentificationSchemeName1Choice();
        schmeNm.setPrtry(achCompanyScheme);

        final GenericOrganisationIdentification1 otherId = new GenericOrganisationIdentification1();
        otherId.setId(id);
        otherId.setSchmeNm(schmeNm);

        final OrganisationIdentification4 orgId = new OrganisationIdentification4();
        orgId.addOthr(otherId);

        final Party6Choice debtorId = new Party6Choice();
        debtorId.setOrgId(orgId);

        return debtorId;
    }

    /*
     * CU Customization: Converted this from a static method to an instance method.
     * Also added conversion of the country code to ISO.
     */
    private PostalAddress6 constructPostalAddress(
            final CustomerProfile templateCustomerProfile
    ) {
        String countryCode = templateCustomerProfile.getCountryCode();
        String isoCountryCode;
        if (StringUtils.isNotBlank(countryCode)) {
            isoCountryCode = convertFIPSCountryCodeToISOCountryCode(countryCode);
        } else {
            LOG.warn("constructPostalAddress, Customer Profile {} has a blank country code; defaulting to '{}'",
                    templateCustomerProfile.getId(), CUKFSConstants.ISO_COUNTRY_CODE_UNKNOWN);
            isoCountryCode = CUKFSConstants.ISO_COUNTRY_CODE_UNKNOWN;
        }
        
        return constructPostalAddress(
                templateCustomerProfile.getAddress1(),
                templateCustomerProfile.getAddress2(),
                templateCustomerProfile.getAddress3(),
                templateCustomerProfile.getAddress4(),
                templateCustomerProfile.getCity(),
                templateCustomerProfile.getStateCode(),
                templateCustomerProfile.getZipCode(),
                isoCountryCode
        );
    }

    private static PostalAddress6 constructPostalAddress(
            final ACHBank achBank
    ) {
        return constructPostalAddress(
                achBank.getBankStreetAddress(),
                null,
                null,
                null,
                achBank.getBankCityName(),
                achBank.getBankStateCode(),
                achBank.getBankZipCode(),
                "US"
        );
    }

    /*
     * CU Customization: Converted this from a static method to an instance method.
     * Also added conversion of the country code to ISO.
     */
    private PostalAddress6 constructPostalAddress(
            final PaymentGroup templatePaymentGroup
    ) {
        String countryValue = templatePaymentGroup.getCountry();
        String isoCountryCode;
        if (StringUtils.isNotBlank(countryValue)) {
            isoCountryCode = wasPaymentGroupCreatedPriorToISOCountryConversion(templatePaymentGroup)
                    ? convertFIPSCountryValueToISOCountryCode(countryValue)
                    : convertISOCountryValueToISOCountryCode(countryValue);
        } else {
            LOG.warn("constructPostalAddress, Payment Group {} has a blank country value; defaulting to '{}'",
                    templatePaymentGroup.getId(), CUKFSConstants.ISO_COUNTRY_CODE_UNKNOWN);
            isoCountryCode = CUKFSConstants.ISO_COUNTRY_CODE_UNKNOWN;
        }
        
        return constructPostalAddress(
                templatePaymentGroup.getLine1Address(),
                templatePaymentGroup.getLine2Address(),
                templatePaymentGroup.getLine3Address(),
                templatePaymentGroup.getLine4Address(),
                templatePaymentGroup.getCity(),
                templatePaymentGroup.getState(),
                templatePaymentGroup.getZipCd(),
                isoCountryCode
        );
    }

    private static PostalAddress6 constructPostalAddress(
            final String addressLine1,
            final String addressLine2,
            final String addressLine3,
            final String addressLine4,
            final String city,
            final String stateCode,
            final String postalCode,
            final String countryCode
    ) {
        final PostalAddress6 postalAddress = new PostalAddress6();

        if (StringUtils.isNotBlank(addressLine1)) {
            postalAddress.addAdrLine(addressLine1);
        }

        if (StringUtils.isNotBlank(addressLine2)) {
            postalAddress.addAdrLine(addressLine2);
        }

        if (StringUtils.isNotBlank(addressLine3)) {
            postalAddress.addAdrLine(addressLine3);
        }

        if (StringUtils.isNotBlank(addressLine4)) {
            postalAddress.addAdrLine(addressLine4);
        }

        if (StringUtils.isNotBlank(city)) {
            postalAddress.setTwnNm(city);
        }

        if (StringUtils.isNotBlank(stateCode)) {
            postalAddress.setCtrySubDvsn(stateCode);
        }

        if (StringUtils.isNotBlank(postalCode)) {
            postalAddress.setPstCd(postalCode);
        }

        if (StringUtils.isNotBlank(countryCode)) {
            postalAddress.setCtry(countryCode);
        }

        return postalAddress;
    }

    /*
     * ==========
     * CU Customization: Added several helper methods for converting country names and FIPS country codes
     * into ISO country codes.
     * ==========
     */

    private boolean wasPaymentGroupCreatedPriorToISOCountryConversion(PaymentGroup templatePaymentGroup) {
        LOG.debug("wasPaymentGroupCreatedPriorToISOCountryConversion, Automatically returning true; "
                + "we will fully implement this method as part of the global FIPS-to-ISO conversion in KFS.");
        return true;
    }

    private String convertISOCountryValueToISOCountryCode(String countryValue) {
        throw new IllegalStateException("The ISO-value-to-ISO-code conversion should not be reachable at this time; "
                + "we will fully implement this feature as part of the global FIPS-to-ISO conversion in KFS.");
    }

    private String convertFIPSCountryValueToISOCountryCode(String countryValue) {
        String trimmedValue = StringUtils.trim(countryValue);
        if (StringUtils.equalsAnyIgnoreCase(trimmedValue,
                KFSConstants.COUNTRY_CODE_UNITED_STATES, CUKFSConstants.COUNTRY_NAME_UNITED_STATES)) {
            LOG.debug("convertFIPSCountryValueToISOCountryCode, Country code/name is for the United States;"
                    + " automatically returning '{}' because FIPS and ISO share the same code for this country",
                    KFSConstants.COUNTRY_CODE_UNITED_STATES);
            return KFSConstants.COUNTRY_CODE_UNITED_STATES;
        } else if (StringUtils.length(trimmedValue) == 2) {
            return convertFIPSCountryCodeToISOCountryCode(trimmedValue);
        } else {
            return convertFIPSCountryNameToISOCountryCode(trimmedValue);
        }
    }

    private String convertFIPSCountryCodeToISOCountryCode(String fipsCountryCode) {
        String isoCountryCode;
        
        try {
            isoCountryCode = isoFipsConversionService.convertFIPSCountryCodeToActiveISOCountryCode(fipsCountryCode);
            LOG.debug("convertFIPSCountryCodeToISOCountryCode, Converted FIPS country '{}' to ISO country '{}'",
                    fipsCountryCode, isoCountryCode);
        } catch (NoFIPStoISOMappingException | ManyFIPStoISOMappingException e) {
            LOG.error("convertFIPSCountryCodeToISOCountryCode, Could not map FIPS country '{}' "
                    + "to a unique active ISO country; will default to ISO country '{}' instead",
                    fipsCountryCode, CUKFSConstants.ISO_COUNTRY_CODE_UNKNOWN, e);
            isoCountryCode = CUKFSConstants.ISO_COUNTRY_CODE_UNKNOWN;
        } catch (RuntimeException e) {
            LOG.error("convertFIPSCountryCodeToISOCountryCode, An unexpected error occurred while attempting to map "
                    + "FIPS country '{}' to an ISO country", fipsCountryCode, e);
            throw e;
        }
        
        return isoCountryCode;
    }

    private String convertFIPSCountryNameToISOCountryCode(String fipsCountryName) {
        String isoCountryCode;
        
        try {
            String fipsCountryCode = isoFipsConversionService
                    .findSingleActiveFIPSCountryCodeByFIPSCountryName(fipsCountryName);
            isoCountryCode = isoFipsConversionService
                    .convertFIPSCountryCodeToActiveISOCountryCode(fipsCountryCode);
            LOG.debug("convertFIPSCountryNameToISOCountryCode, Converted FIPS country '{}' to ISO country code '{}'",
                    fipsCountryName, isoCountryCode);
        } catch (NoFIPSCountriesForNameException | ManyFIPSCountriesForNameException e) {
            LOG.error("convertFIPSCountryNameToISOCountryCode, Could not find a unique FIPS country named '{}'; "
                    + "will default to ISO country '{}' instead",
                    fipsCountryName, CUKFSConstants.ISO_COUNTRY_CODE_UNKNOWN, e);
            isoCountryCode = CUKFSConstants.ISO_COUNTRY_CODE_UNKNOWN;
        } catch (NoFIPStoISOMappingException | ManyFIPStoISOMappingException e) {
            LOG.error("convertFIPSCountryNameToISOCountryCode, Could not find a unique ISO country that maps to "
                    + "FIPS country named '{}'; will default to ISO country '{}' instead",
                    fipsCountryName, CUKFSConstants.ISO_COUNTRY_CODE_UNKNOWN, e);
            isoCountryCode = CUKFSConstants.ISO_COUNTRY_CODE_UNKNOWN;
        } catch (RuntimeException e) {
            LOG.error("convertFIPSCountryNameToISOCountryCode, An unexpected error occurred while attempting to map "
                    + "FIPS country named '{}' to an ISO country", fipsCountryName, e);
            throw e;
        }
        
        return isoCountryCode;
    }

    /*
     * ==========
     * End CU Customization Block
     * ==========
     */

    private static CashAccount16 constructDebtorAccount(
            final PaymentGroup templatePaymentGroup
    ) {
        final CashAccount16 debtorAccount = new CashAccount16();

        final String debtorAccountOtherId = templatePaymentGroup.getBank().getBankAccountNumber();
        final GenericAccountIdentification1 debtorAccountOther = new GenericAccountIdentification1();
        debtorAccountOther.setId(debtorAccountOtherId);
        final AccountIdentification4Choice debtorAccountId = new AccountIdentification4Choice();
        debtorAccountId.setOthr(debtorAccountOther);
        debtorAccount.setId(debtorAccountId);

        debtorAccount.setCcy(CURRENCY_USD);

        return debtorAccount;
    }

    private BranchAndFinancialInstitutionIdentification4 constructDebtorAgent(
            final PaymentGroup templatePaymentGroup
    ) {
        final String bic = templatePaymentGroup.getBank().getBankIdentificationCode();

        final String memberId = templatePaymentGroup.getBank().getBankRoutingNumber();
        final ClearingSystemMemberIdentification2 clearingSystemMemberIdentification =
                new ClearingSystemMemberIdentification2();
        clearingSystemMemberIdentification.setMmbId(memberId);

        final ACHBank achBank = retrieveAchBank(templatePaymentGroup);
        final PostalAddress6 debtorFinancialInstitutionPostalAddress = constructPostalAddress(achBank);

        final FinancialInstitutionIdentification7 debtorFinancialInstitutionIdentification =
                new FinancialInstitutionIdentification7();
        debtorFinancialInstitutionIdentification.setBIC(bic);
        debtorFinancialInstitutionIdentification.setClrSysMmbId(clearingSystemMemberIdentification);
        debtorFinancialInstitutionIdentification.setPstlAdr(debtorFinancialInstitutionPostalAddress);

        final BranchAndFinancialInstitutionIdentification4 debtorAgent =
                new BranchAndFinancialInstitutionIdentification4();
        debtorAgent.setFinInstnId(debtorFinancialInstitutionIdentification);

        return debtorAgent;
    }

    private ACHBank retrieveAchBank(
            final PaymentGroup templatePaymentGroup
    ) {
        final String bankRoutingNumber = templatePaymentGroup.getBank().getBankRoutingNumber();
        final ACHBank achBank = achBankService.getByPrimaryId(bankRoutingNumber);
        LOG.debug("retrieveAchBank(...) - Exit : achBank={}", achBank);
        return achBank;
    }

    private CreditTransferTransactionInformation10 constructCreditTransferTransactionInformation(
            final String bankCode,
            final Integer disbursementNumber,
            final Collection<PaymentDetail> paymentDetails,
            final PaymentGroup templatePaymentGroup,
            final ExtractTypeContext extractTypeContext
    ) {
        final CreditTransferTransactionInformation10 creditTransferTransactionInformation =
                new CreditTransferTransactionInformation10();

        final PaymentIdentification1 paymentIdentification =
                constructPaymentIdentification(bankCode, disbursementNumber);
        creditTransferTransactionInformation.setPmtId(paymentIdentification);

        final AmountType3Choice paymentAmount = constructPaymentAmount(paymentDetails);
        creditTransferTransactionInformation.setAmt(paymentAmount);

        final PartyIdentification32 creditor = constructCreditor(templatePaymentGroup, extractTypeContext);
        creditTransferTransactionInformation.setCdtr(creditor);

        /*
         * CU Customization: Added the ability to suppress remittances from payments that don't need it.
         */
        if (shouldAddRemittanceInformationToPayment(templatePaymentGroup, extractTypeContext)) {
            final RemittanceInformation5 remittanceInformation =
                    constructRemittanceInformation(templatePaymentGroup, paymentDetails, extractTypeContext);
            creditTransferTransactionInformation.setRmtInf(remittanceInformation);
        }

        if (extractTypeContext.isExtractionType(ExtractionType.ACH)) {
            final BranchAndFinancialInstitutionIdentification4 creditorAgent =
                    constructCreditorAgent(templatePaymentGroup);
            creditTransferTransactionInformation.setCdtrAgt(creditorAgent);

            final CashAccount16 creditorAccount = constructCreditorAccount(templatePaymentGroup);
            creditTransferTransactionInformation.setCdtrAcct(creditorAccount);

            final PaymentTypeInformation19 paymentTypeInformation =
                    constructPaymentTypeInformationWithLocalInstrument(templatePaymentGroup);
            creditTransferTransactionInformation.setPmtTpInf(paymentTypeInformation);
        } else if (extractTypeContext.isExtractionType(ExtractionType.CHECK)) {
            /*
             * CU Customization: Adjusted method call to also pass in the extraction context.
             */
            final Cheque6 check = constructCheck(disbursementNumber, templatePaymentGroup, extractTypeContext);
            creditTransferTransactionInformation.setChqInstr(check);
        }

        return creditTransferTransactionInformation;
    }

    /*
     * CU Customization: Added method to check whether or not remittance information should be added to the payment.
     */
    private boolean shouldAddRemittanceInformationToPayment(
            final PaymentGroup templatePaymentGroup,
            final ExtractTypeContext extractTypeContext
    ) {
        if (extractTypeContext.isExtractionType(ExtractionType.CHECK)) {
            return true;
        } else if (extractTypeContext.isExtractionType(ExtractionType.ACH)) {
            PayeeACHAccount payeeAchAccount = retrievePayeeACHAccount(templatePaymentGroup);
            if (ObjectUtils.isNull(payeeAchAccount)) {
                throw new IllegalStateException("Could not find Payee ACH Account for Payment Group '"
                        + templatePaymentGroup.getId()  + "'; this should NEVER happen!");
            }
            return StringUtils.equals(payeeAchAccount.getStandardEntryClass(), StandardEntryClass.CTX.name());
        } else {
            throw new IllegalStateException("Extraction context with Java type "
                    + extractTypeContext.getClass().getName()
                    + " was for neither Check nor ACH; this should NEVER happen!");
        }
    }

    private PaymentTypeInformation19 constructPaymentTypeInformationWithLocalInstrument(
            final PaymentGroup templatePaymentGroup
    ) {
        final PaymentTypeInformation19 paymentTypeInformation = new PaymentTypeInformation19();

        final LocalInstrument2Choice localInstrument = determineLocalInstrument(templatePaymentGroup);
        paymentTypeInformation.setLclInstrm(localInstrument);

        return paymentTypeInformation;
    }

    private LocalInstrument2Choice determineLocalInstrument(
            final PaymentGroup templatePaymentGroup
    ) {
        final LocalInstrument2Choice localInstrument = new LocalInstrument2Choice();

        final PayeeACHAccount payeeAchAccount = retrievePayeeACHAccount(templatePaymentGroup);
        localInstrument.setCd(payeeAchAccount.getStandardEntryClass());

        return localInstrument;
    }

    private PayeeACHAccount retrievePayeeACHAccount(
            final PaymentGroup templatePaymentGroup
    ) {
        final CustomerProfile templateCustomerProfile = getCustomerProfile(templatePaymentGroup);
        return achService.getAchInformation(
                templatePaymentGroup.getPayeeIdTypeCd(),
                templatePaymentGroup.getPayeeId(),
                templateCustomerProfile.getAchTransactionType()
        );
    }

    private BranchAndFinancialInstitutionIdentification4 constructCreditorAgent(
            final PaymentGroup templatePaymentGroup
    ) {
        final FinancialInstitutionIdentification7 financialInstitutionIdentification =
                new FinancialInstitutionIdentification7();

        final String memberId = templatePaymentGroup.getAchBankRoutingNbr();
        final ClearingSystemMemberIdentification2 clearingSystemMemberIdentification =
                new ClearingSystemMemberIdentification2();
        clearingSystemMemberIdentification.setMmbId(memberId);
        financialInstitutionIdentification.setClrSysMmbId(clearingSystemMemberIdentification);

        final ACHBank achBank = retrieveAchBank(templatePaymentGroup);
        final String financialInstitutionName = achBank.getBankName();
        financialInstitutionIdentification.setNm(financialInstitutionName);

        final PostalAddress6 financialInstitutionPostalAddress = constructPostalAddress(achBank);
        financialInstitutionIdentification.setPstlAdr(financialInstitutionPostalAddress);

        final BranchAndFinancialInstitutionIdentification4 agent =
                new BranchAndFinancialInstitutionIdentification4();
        agent.setFinInstnId(financialInstitutionIdentification);

        return agent;
    }

    private static CashAccount16 constructCreditorAccount(
            final PaymentGroup templatePaymentGroup
    ) {
        final CashAccount16 creditorAccount = new CashAccount16();

        final String otherIdStr = templatePaymentGroup.getAchAccountNumber().getAchBankAccountNbr();
        final GenericAccountIdentification1 otherId = new GenericAccountIdentification1();
        otherId.setId(otherIdStr);
        final AccountIdentification4Choice accountId = new AccountIdentification4Choice();
        accountId.setOthr(otherId);
        creditorAccount.setId(accountId);

        final CashAccountType4Code accountTypeCode = CashAccountType4Code.CASH;
        final CashAccountType2 accountType = new CashAccountType2();
        accountType.setCd(accountTypeCode);
        creditorAccount.setTp(accountType);

        return creditorAccount;
    }

    private static PaymentIdentification1 constructPaymentIdentification(
            final String bankCode,
            final int disbursementNumber
    ) {
        final PaymentIdentification1 paymentIdentification = new PaymentIdentification1();

        final String instructionId = String.format("%s / %s", bankCode, disbursementNumber);
        paymentIdentification.setInstrId(instructionId);

        final String endToEndId = Integer.toString(disbursementNumber);
        paymentIdentification.setEndToEndId(endToEndId);

        return paymentIdentification;
    }

    private static AmountType3Choice constructPaymentAmount(
            final Collection<PaymentDetail> paymentDetails
    ) {
        final BigDecimal value =
                paymentDetails
                        .stream()
                        .map(PaymentDetail::getNetPaymentAmount)
                        .reduce(KualiDecimal.ZERO, KualiDecimal::add)
                        .bigDecimalValue();
        final ActiveOrHistoricCurrencyAndAmount amount = constructActiveOrHistoricCurrencyAndAmount(value);

        final AmountType3Choice paymentAmount = new AmountType3Choice();
        paymentAmount.setInstdAmt(amount);

        return paymentAmount;
    }

    private static ActiveOrHistoricCurrencyAndAmount constructActiveOrHistoricCurrencyAndAmount(
            final BigDecimal value
    ) {
        final ActiveOrHistoricCurrencyAndAmount amount = new ActiveOrHistoricCurrencyAndAmount();
        amount.setCcy(CURRENCY_USD);
        amount.setValue(value);
        return amount;
    }

    /*
     * CU Customization: Added extraction context as a method argument, and added the ability
     * to suppress the Delivery Method and Forms Code when building the Immediate Checks file.
     * Also converted this method from a static one to an instance one, so that the referenced
     * determineFormsCode() method can also be converted to an instance one.
     */
    private Cheque6 constructCheck(
            final int disbursementNumber,
            final PaymentGroup templatePaymentGroup,
            final ExtractTypeContext extractTypeContext
    ) {
        final Cheque6 check = new Cheque6();

        final String checkNumber = Integer.toString(disbursementNumber);
        check.setChqNb(checkNumber);

        if (extractTypeContext.isLimitedToPaymentsWithUrgency(PaymentUrgency.REGULAR)) {
            final ChequeDeliveryMethod1Choice checkDeliveryMethod = constructCheckDeliveryMethod(templatePaymentGroup);
            check.setDlvryMtd(checkDeliveryMethod);

            final String formsCode = determineFormsCode();
            check.setFrmsCd(formsCode);
        }

        return check;
    }

    private static ChequeDeliveryMethod1Choice constructCheckDeliveryMethod(
            final PaymentGroup templatePaymentGroup
    ) {
        final ChequeDeliveryMethod1Choice checkDeliveryMethod = new ChequeDeliveryMethod1Choice();

        final CheckDeliveryPriority checkDeliveryPriority = determineCheckDeliveryPriority(templatePaymentGroup);
        checkDeliveryMethod.setPrtry(checkDeliveryPriority.getValue());

        return checkDeliveryMethod;
    }

    private static CheckDeliveryPriority determineCheckDeliveryPriority(
            final PaymentGroup templatePaymentGroup
    ) {
        CheckDeliveryPriority cdp = CheckDeliveryPriority.NORMAL;
        if (templatePaymentGroup.getProcessImmediate()) {
            cdp = CheckDeliveryPriority.OVERNIGHT_TO_CREDITOR;
        } else if (templatePaymentGroup.getPymtSpecialHandling() || templatePaymentGroup.getPymtAttachment()) {
            cdp = CheckDeliveryPriority.OVERNIGHT_TO_DESIGNATED_OFFICE;
        }
        return cdp;
    }

    /*
     * The code identifying which check design should be used.
     * 
     * CU Customization: Converted this method from a static one to an instance one,
     * and updated it to derive the Forms Code from a KFS parameter.
     */
    private String determineFormsCode() {
        String formsCode = parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.PDP,
                KFSConstants.Components.ISO_FORMAT, CUPdpParameterConstants.CU_ISO20022_CHECK_FORMS_CODE);
        if (StringUtils.isBlank(formsCode)) {
            throw new IllegalStateException("Parameter " + CUPdpParameterConstants.CU_ISO20022_CHECK_FORMS_CODE
                    + " is missing or contains a blank Forms Code");
        }
        return formsCode;
    }

    /*
     * CU Customization: Converted this from a static method to an instance method.
     */
    private PartyIdentification32 constructCreditor(
            final PaymentGroup templatePaymentGroup,
            final ExtractTypeContext extractTypeContext
    ) {
        final String creditorName = templatePaymentGroup.getPayeeName();
        final PartyIdentification32 creditor = constructPartyIdentification(creditorName);

        final PostalAddress6 creditorPostalAddress = constructPostalAddress(templatePaymentGroup);
        creditor.setPstlAdr(creditorPostalAddress);

        final Party6Choice creditorId = constructCreditorId(templatePaymentGroup, extractTypeContext);
        creditor.setId(creditorId);

        return creditor;
    }

    private static Party6Choice constructCreditorId(
            final PaymentGroup templatePaymentGroup,
            final ExtractTypeContext extractTypeContext
    ) {
        final String vendorNumber = StringUtils.truncate(templatePaymentGroup.getPayeeId(), VENDOR_NUM_MAX_LENGTH);
        final GenericOrganisationIdentification1 otherId = new GenericOrganisationIdentification1();
        otherId.setId(vendorNumber);
        if (extractTypeContext.isExtractionType(ExtractionType.ACH)) {
            final String schemaNameCode = "CHID";
            final OrganisationIdentificationSchemeName1Choice schemaName =
                    new OrganisationIdentificationSchemeName1Choice();
            schemaName.setCd(schemaNameCode);

            otherId.setSchmeNm(schemaName);
        }

        final OrganisationIdentification4 creditorOrganizationId = new OrganisationIdentification4();
        creditorOrganizationId.addOthr(otherId);

        final Party6Choice creditorId = new Party6Choice();
        creditorId.setOrgId(creditorOrganizationId);
        return creditorId;
    }

    private RemittanceInformation5 constructRemittanceInformation(
            final PaymentGroup templatePaymentGroup,
            final Collection<PaymentDetail> paymentDetails,
            final ExtractTypeContext extractTypeContext
    ) {
        final RemittanceInformation5 remittanceInformation = new RemittanceInformation5();

        /*
         * CU Customization: Removed the setup of unstructured remittance information.
         */

        paymentDetails.forEach(paymentDetail -> {
            final StructuredRemittanceInformation7 structuredRemittanceInformation =
                    constructStructuredRemittanceInformation(paymentDetail, extractTypeContext);
            remittanceInformation.addStrd(structuredRemittanceInformation);
        });

        return remittanceInformation;
    }

    /*
     * CU Customization: Removed the following private methods that are no longer in use,
     *     as a result of our code customizations:
     * 
     * constructUstrd
     * addToJoinerIfNotBlank
     */

    private StructuredRemittanceInformation7 constructStructuredRemittanceInformation(
            final PaymentDetail paymentDetail,
            final ExtractTypeContext extractTypeContext
    ) {
        final StructuredRemittanceInformation7 structuredRemittanceInformation =
                new StructuredRemittanceInformation7();

        /*
         * CU Customization: Also pass the extraction context to the Referred Document Information method.
         */
        final ReferredDocumentInformation3 referredDocumentInformation =
                constructReferredDocumentInformation(paymentDetail, extractTypeContext);
        structuredRemittanceInformation.addRfrdDocInf(referredDocumentInformation);

        final RemittanceAmount1 referredDocumentAmount =
                constructReferredDocumentAmount(paymentDetail, referredDocumentInformation, extractTypeContext);
        structuredRemittanceInformation.setRfrdDocAmt(referredDocumentAmount);

        /*
         * CU Customization: Only add the Creditor Reference Information when the payment specifies a PO number.
         */
        if (StringUtils.isNotBlank(paymentDetail.getPurchaseOrderNbr())) {
            final CreditorReferenceInformation2 creditorReferenceInformation =
                    constructCreditorReferenceInformation(paymentDetail, extractTypeContext);
            structuredRemittanceInformation.setCdtrRefInf(creditorReferenceInformation);
        }

        addCheckStubText(paymentDetail, structuredRemittanceInformation);

        return structuredRemittanceInformation;
    }

    /*
     * CU Customization: Updated this method to allow for deriving the check stub text from PDP notes,
     * if the PaymentDetail does not refer to a Disbursement Voucher.
     * 
     * Also updated the method to derive the max check stub length from a helper service instead of a constant.
     */
    private void addCheckStubText(
            final PaymentDetail paymentDetail,
            final StructuredRemittanceInformation7 structuredRemittanceInformation
    ) {
        final DisbursementVoucherDocument disbursementVoucherDocument =
                disbursementVoucherDao.getDocument(paymentDetail.getCustPaymentDocNbr());
        final String checkStubText;
        if (disbursementVoucherDocument == null) {
            checkStubText = cuCheckStubService.getFullCheckStub(paymentDetail);
        } else {
            checkStubText = disbursementVoucherDocument.getDisbVchrCheckStubText();
        }

        // DVs created in the UI will have checkStubText; DVs imported via Payment File Upload will not
        final String cleanedCheckStubText =
                xmlUtilService.filterOutIllegalXmlCharacters(checkStubText);
        if (StringUtils.isBlank(cleanedCheckStubText)) {
            return;
        }

        final String addtlRmtInf = StringUtils.truncate(cleanedCheckStubText,
                cuCheckStubService.getCheckStubMaxLengthForIso20022());
        structuredRemittanceInformation.addAddtlRmtInf(addtlRmtInf);
    }

    /*
     * CU Customization: Added extraction context both as a method arg and as an extra arg on the Referred
     * Document Type setup.
     */
    private static ReferredDocumentInformation3 constructReferredDocumentInformation(
            final PaymentDetail paymentDetail,
            final ExtractTypeContext extractTypeContext
    ) {
        final ReferredDocumentInformation3 referredDocumentInformation = new ReferredDocumentInformation3();

        final ReferredDocumentType2 referredDocumentType = constructReferredDocumentType(paymentDetail,
                extractTypeContext);
        referredDocumentInformation.setTp(referredDocumentType);

        /*
         * CU Customization: Externalized the Referred Document Number setup to a separate method.
         */
        final String documentNumber = determineReferredDocumentNumber(paymentDetail);
        referredDocumentInformation.setNb(documentNumber);

        final Date invoiceDate = new Date(paymentDetail.getInvoiceDate().getTime());
        final XMLGregorianCalendar rltdDate = constructXmlGregorianCalendarWithDateOnly(invoiceDate);
        referredDocumentInformation.setRltdDt(rltdDate);

        return referredDocumentInformation;
    }

    /*
     * CU Customization: Added extraction context as a method arg and added setup of doc number
     * in the "Issr" property/element.
     */
    private static ReferredDocumentType2 constructReferredDocumentType(
            final PaymentDetail paymentDetail,
            final ExtractTypeContext extractTypeContext
    ) {
        final DocumentType5Code documentTypeCode = determineDocumentTypeCode(paymentDetail);
        final ReferredDocumentType1Choice referredDocumentTypeType = new ReferredDocumentType1Choice();
        referredDocumentTypeType.setCd(documentTypeCode);

        final ReferredDocumentType2 referredDocumentType = new ReferredDocumentType2();
        referredDocumentType.setCdOrPrtry(referredDocumentTypeType);

        if (extractTypeContext.isExtractionType(ExtractionType.CHECK)
                && extractTypeContext.isLimitedToPaymentsWithUrgency(PaymentUrgency.IMMEDIATE)
                && StringUtils.isNotBlank(paymentDetail.getCustPaymentDocNbr())) {
            referredDocumentType.setIssr(paymentDetail.getCustPaymentDocNbr());
        }

        return referredDocumentType;
    }

    private static DocumentType5Code determineDocumentTypeCode(
            final PaymentDetail paymentDetail
    ) {
        DocumentType5Code documentTypeCode = null;
        if (isInvoice(paymentDetail)) {
            documentTypeCode = DocumentType5Code.CINV;
        } else if (isCreditNote(paymentDetail)) {
            documentTypeCode = DocumentType5Code.CREN;
        } else {
            /*
             * CU Customization: Allow payments from PDP feeds to be marked as Invoices or Credit Notes.
             */
            documentTypeCode = paymentDetail.getNetPaymentAmount().isGreaterEqual(KualiDecimal.ZERO)
                    ? DocumentType5Code.CINV
                    : DocumentType5Code.CREN;
        }
        return documentTypeCode;
    }

    private static boolean isInvoice(
            final PaymentDetail paymentDetail
    ) {
        final Set<String> disbursementVoucherTypes = Set.of(
                DisbursementVoucherConstants.DOCUMENT_TYPE_CHECKACH,
                DisbursementVoucherConstants.DOCUMENT_TYPE_CODE
        );

        final String financialDocumentTypeCode = paymentDetail.getFinancialDocumentTypeCode();

        return disbursementVoucherTypes.contains(financialDocumentTypeCode)
               || KFSConstants.FinancialDocumentTypeCodes.PAYMENT_REQUEST.equals(financialDocumentTypeCode);
    }

    private static boolean isCreditNote(
            final PaymentDetail paymentDetail
    ) {
        return KFSConstants.FinancialDocumentTypeCodes.VENDOR_CREDIT_MEMO
                .equals(paymentDetail.getFinancialDocumentTypeCode());
    }

    /*
     * CU Customization: Added new method for deriving the document/invoice number.
     */
    private static String determineReferredDocumentNumber(
            final PaymentDetail paymentDetail
    ) {
        if (StringUtils.equalsAny(paymentDetail.getFinancialDocumentTypeCode(),
                DisbursementVoucherConstants.DOCUMENT_TYPE_CHECKACH,
                DisbursementVoucherConstants.DOCUMENT_TYPE_CODE)) {
            if (StringUtils.isNotBlank(paymentDetail.getInvoiceNbr()) && paymentDetail.getInvoiceDate() != null) {
                return paymentDetail.getInvoiceNbr();
            } else {
                return CuDisbursementVoucherConstants.DV_EXTRACT_EDOC_NUMBER_PREFIX_IDENTIFIER
                        + StringUtils.defaultString(paymentDetail.getCustPaymentDocNbr());
            }
        } else {
            return paymentDetail.getInvoiceNbr();
        }
    }

    private static RemittanceAmount1 constructReferredDocumentAmount(
            final PaymentDetail paymentDetail,
            final ReferredDocumentInformation3 referredDocumentInformation,
            final ExtractTypeContext extractTypeContext
    ) {
        final RemittanceAmount1 referredDocumentAmount = new RemittanceAmount1();

        final DocumentType5Code documentTypeCode = referredDocumentInformation.getTp().getCdOrPrtry().getCd();
        final BigDecimal payableDueAmountValue =
                DocumentType5Code.CREN == documentTypeCode
                        ? paymentDetail.getOrigInvoiceAmount().bigDecimalValue().abs()
                        : paymentDetail.getOrigInvoiceAmount().bigDecimalValue();
        final ActiveOrHistoricCurrencyAndAmount duePayableAmount =
                constructActiveOrHistoricCurrencyAndAmount(payableDueAmountValue);
        referredDocumentAmount.setDuePyblAmt(duePayableAmount);

        final BigDecimal discountAppliedAmountValue = paymentDetail.getInvTotDiscountAmount().bigDecimalValue().abs();
        final ActiveOrHistoricCurrencyAndAmount discountAppliedAmount =
                constructActiveOrHistoricCurrencyAndAmount(discountAppliedAmountValue);
        referredDocumentAmount.setDscntApldAmt(discountAppliedAmount);

        if (DocumentType5Code.CREN == documentTypeCode) {
            final BigDecimal creditNoteAmountValue = paymentDetail.getNetPaymentAmount().bigDecimalValue().abs();
            final ActiveOrHistoricCurrencyAndAmount creditNoteAmount =
                    constructActiveOrHistoricCurrencyAndAmount(creditNoteAmountValue);
            referredDocumentAmount.setCdtNoteAmt(creditNoteAmount);
        } else {
            final BigDecimal remitAmountValue = paymentDetail.getNetPaymentAmount().bigDecimalValue();
            final ActiveOrHistoricCurrencyAndAmount remitAmount =
                    constructActiveOrHistoricCurrencyAndAmount(remitAmountValue);
            referredDocumentAmount.setRmtdAmt(remitAmount);
        }
        if (extractTypeContext.isExtractionType(ExtractionType.ACH)) {
            // KFS does not support this
            final ActiveOrHistoricCurrencyAndAmount taxAmount =
                    constructActiveOrHistoricCurrencyAndAmount(BigDecimal.ZERO);
            referredDocumentAmount.setTaxAmt(taxAmount);
        }

        /*
         * CU Customization: On immediate Check payments, add PDP payment notes as zero-amount adjustments.
         */
        if (extractTypeContext.isExtractionType(ExtractionType.CHECK)
                && extractTypeContext.isLimitedToPaymentsWithUrgency(PaymentUrgency.IMMEDIATE)
                && CollectionUtils.isNotEmpty(paymentDetail.getNotes())) {
            for (final PaymentNoteText paymentNote : paymentDetail.getNotes()) {
                DocumentAdjustment1 adjustmentAmount = constructAdjustmentAmountForStoringPaymentNote(paymentNote);
                referredDocumentAmount.addAdjstmntAmtAndRsn(adjustmentAmount);
            }
        }

        return referredDocumentAmount;
    }

    /*
     * CU Customization: Added method that creates zero-amount adjustments for holding PDP payment notes.
     */
    private static DocumentAdjustment1 constructAdjustmentAmountForStoringPaymentNote(
            final PaymentNoteText paymentNote
    ) {
        DocumentAdjustment1 adjustmentAmount = new DocumentAdjustment1();
        adjustmentAmount.setRsn(Iso20022Constants.ADJUSTMENT_REASON_NOTE_ONLY);
        adjustmentAmount.setAddtlInf(paymentNote.getCustomerNoteText());

        ActiveOrHistoricCurrencyAndAmount zeroDollarAmount =
                constructActiveOrHistoricCurrencyAndAmount(BigDecimal.ZERO);
        adjustmentAmount.setAmt(zeroDollarAmount);

        return adjustmentAmount;
    }

    private static CreditorReferenceInformation2 constructCreditorReferenceInformation(
            final PaymentDetail paymentDetail,
            final ExtractTypeContext extractTypeContext
    ) {
        final CreditorReferenceInformation2 creditorReferenceInformation = new CreditorReferenceInformation2();

        final String purchaseOrderNumber = StringUtils.truncate(paymentDetail.getPurchaseOrderNbr(), REF_MAX_LENGTH);
        creditorReferenceInformation.setRef(purchaseOrderNumber);

        if (extractTypeContext.isExtractionType(ExtractionType.ACH)) {
            final String typeValue = StringUtils.isBlank(purchaseOrderNumber) ? "NA" : "PO";
            final CreditorReferenceType1Choice type = new CreditorReferenceType1Choice();
            type.setPrtry(typeValue);

            final CreditorReferenceType2 referenceType = new CreditorReferenceType2();
            referenceType.setCdOrPrtry(type);

            creditorReferenceInformation.setTp(referenceType);
        }

        return creditorReferenceInformation;
    }

    private String determineFilename(
            final String directoryName,
            final ExtractTypeContext extractTypeContext
    ) {
        LOG.debug(
                "determineFilename(...) Enter - : directoryName={}; extractTypeContext={}",
                directoryName,
                extractTypeContext
        );

        /*
         * CU Customization: Moved the filename property derivation to its own method.
         */
        final String propertyName = determinePropertyContainingExtractFilenamePrefix(extractTypeContext);
        final String rawCheckFilePrefix =
                configurationService.getPropertyValueAsString(propertyName);
        final String formattedCheckFilePrefix = MessageFormat.format(rawCheckFilePrefix, new Object[]{null});

        final String finalFormattedCheckFilePrefix;
        if (extractTypeContext.isExtractionType(ExtractionType.ACH)) {
            finalFormattedCheckFilePrefix = formattedCheckFilePrefix;
        } else {
            // Check whether this is for research participant upload. If the customer profile matches research
            // participant's customer profile, then change the filename to append the RP-Upload prefix.
            finalFormattedCheckFilePrefix =
                    isResearchParticipantExtractFile(extractTypeContext)
                            ? PdpConstants.RESEARCH_PARTICIPANT_FILE_PREFIX + KFSConstants.DASH + formattedCheckFilePrefix
                            : formattedCheckFilePrefix;
        }

        final Date disbursementDate = extractTypeContext.getDisbursementDate();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        final String filename =
                directoryName
                + File.separator
                + finalFormattedCheckFilePrefix
                + "_"
                + sdf.format(disbursementDate)
                + ".xml";

        LOG.debug("determineFilename(...) - : filename={}", filename);
        return filename;
    }

    /*
     * CU Customization: Added method for deriving which property to use for the extract filename.
     */
    private String determinePropertyContainingExtractFilenamePrefix(
            final ExtractTypeContext extractTypeContext
    ) {
        if (extractTypeContext.isExtractionType(ExtractionType.ACH)) {
            return PdpKeyConstants.ExtractPayment.ACH_FILENAME;
        } else if (extractTypeContext.isExtractionType(ExtractionType.CHECK)) {
            final boolean processImmediate = getProcessImmediateFlagForCheckExtraction(extractTypeContext);
            return processImmediate
                    ? CUPdpKeyConstants.ExtractPayment.CHECK_IMMEDIATE_FILENAME
                    : PdpKeyConstants.ExtractPayment.CHECK_FILENAME;
        } else {
            throw new IllegalStateException("Context of type " + extractTypeContext.getClass().getName()
                    + " was for neither Check nor ACH; this should NEVER happen");
        }
    }

    private boolean isResearchParticipantExtractFile(
            final ExtractTypeContext extractTypeContext
    ) {
        final int processId = extractTypeContext.getPaymentProcess().getId().intValue();

        final boolean parameterExists =
                parameterService.parameterExists(
                        PaymentDetail.class,
                        PdpConstants.RESEARCH_PARTICIPANT_CUSTOMER_PROFILE
                );

        if (parameterExists) {
            final Map<String, Integer> fieldValues = Map.of("processId", processId);
            final Collection<ProcessSummary> processSummaryList =
                    businessObjectService.findMatching(ProcessSummary.class, fieldValues);
            final ProcessSummary processSummary = processSummaryList.iterator().next();

            final Collection<String> researchParticipantCustomers =
                    parameterService.getParameterValuesAsString(
                            PaymentDetail.class,
                            PdpConstants.RESEARCH_PARTICIPANT_CUSTOMER_PROFILE
                    );

            for (final String researchParticipantCustomer : researchParticipantCustomers) {
                final String[] customerArray = researchParticipantCustomer.split(KFSConstants.DASH);
                final CustomerProfile customer = processSummary.getCustomer();
                if (customer.getCampusCode().equals(customerArray[0])
                    && customer.getUnitCode().equals(customerArray[1])
                    && customer.getSubUnitCode().equals(customerArray[2])
                ) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void writeMessageToFile(
            final MxPain00100103 message,
            final String filename
    ) {
        try (OutputStream os = new FileOutputStream(filename)) {
            // Ideally, this would be a one-liner -- message.write(os);
            //
            // However, Stevens/JPMC requested the namespace be removed. Unfortunately, the ISO library we are using
            // does not support removing them altogether, which makes me wonder if this really adheres to the standard
            // or if it's something convenient for JPMC. For now, since Stevens/JPMC are the only ones driving this,
            // we'll do the hack below; however, in the future, additional customers/banks could drive this in a more
            // standard direction which might require Stevens to have to post-process our output XML before sending it
            // on to JPMC.
            final String xmlString =
                    message.message()
                            .replaceAll("xmlns:Doc=", "xmlns=")
                            .replaceAll("<Doc:", "<")
                            .replaceAll("</Doc:", "</");
            os.write(xmlString.getBytes("UTF-8"));
        } catch (final IOException e) {
            LOG.error("writeMessageToFile(...) - Problem writing message to file : filename={}", filename, e);
            // I don't like this but it's what the proprietary format is doing so keeping it for consistency.
            throw new IllegalArgumentException("Error writing to output file", e);
        }
    }

    /*
     * Creates a '.done' file with the name of the original file.
     */
    private static void createDoneFile(
            final String filename
    ) {
        final String doneFilename = StringUtils.substringBeforeLast(filename, ".") + ".done";
        final File doneFile = new File(doneFilename);
        if (!doneFile.exists()) {
            boolean doneFileCreated;
            try {
                doneFileCreated = doneFile.createNewFile();
            } catch (final IOException e) {
                LOG.error("createDoneFile(...) - Unable to create done file : doneFilename={}", doneFilename, e);
                throw new RuntimeException(
                        "Errors encountered while saving the file: Unable to create .done file " + doneFilename, e);
            }

            if (!doneFileCreated) {
                LOG.error("createDoneFile(...) - Unable to create done file : doneFilename={}", doneFilename);
                throw new RuntimeException(
                        "Errors encountered while saving the file: Unable to create .done file " + doneFilename);
            }
        }
    }
}
