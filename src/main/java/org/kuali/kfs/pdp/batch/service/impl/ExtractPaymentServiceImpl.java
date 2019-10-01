/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.krad.util.ObjectUtils;
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
import org.kuali.kfs.pdp.businessobject.ProcessSummary;
import org.kuali.kfs.pdp.dataaccess.PaymentGroupHistoryDao;
import org.kuali.kfs.pdp.dataaccess.ProcessDao;
import org.kuali.kfs.pdp.service.PaymentDetailService;
import org.kuali.kfs.pdp.service.PaymentGroupService;
import org.kuali.kfs.pdp.service.PdpEmailService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.InitiateDirectoryBase;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.Country;
import org.kuali.kfs.sys.report.BusinessObjectReportHelper;
import org.kuali.kfs.sys.service.LocationService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.api.util.type.KualiInteger;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Transactional
public class ExtractPaymentServiceImpl extends InitiateDirectoryBase implements ExtractPaymentService {

    private static final Logger LOG = LogManager.getLogger(ExtractPaymentServiceImpl.class);

    protected static String SPACES = "                                                       ";

    protected String directoryName;

    protected DateTimeService dateTimeService;
    protected ParameterService parameterService;
    protected PaymentGroupService paymentGroupService;
    protected PaymentDetailService paymentDetailService;
    protected PaymentGroupHistoryDao paymentGroupHistoryDao;
    protected ProcessDao processDao;
    protected PdpEmailService paymentFileEmailService;
    protected BusinessObjectService businessObjectService;
    protected ConfigurationService kualiConfigurationService;
    protected DataDictionaryService dataDictionaryService;
    protected LocationService locationService;

    // Set this to true to run this process without updating the database. This should stay false for production.
    public static boolean testMode = false;

    /**
     * Generate the output file with prefix and date subfix
     *
     * @param fileprefix
     * @param runDate
     * @return
     */
    protected String getOutputFile(String fileprefix, Date runDate) {
        //add a step to check for directory paths
        prepareDirectories(getRequiredDirectoryNames());

        String filename = directoryName + "/" + fileprefix + "_";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        filename = filename + sdf.format(runDate);
        filename = filename + ".xml";

        return filename;
    }

    @Override
    public void extractCanceledChecks() {
        LOG.debug("extractCancelledChecks() started");

        Date processDate = dateTimeService.getCurrentDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        String checkCancelledFilePrefix = this.kualiConfigurationService.getPropertyValueAsString(
                PdpKeyConstants.ExtractPayment.CHECK_CANCEL_FILENAME);
        checkCancelledFilePrefix = MessageFormat.format(checkCancelledFilePrefix, new Object[]{null});

        String filename = getOutputFile(checkCancelledFilePrefix, processDate);
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractCanceledChecks() filename = " + filename);
        }

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
                } else {
                    writeTag(os, 4, "disbursementNumber", history.getPaymentGroup().getDisbursementNbr().toString());
                }
                if (ObjectUtils.isNotNull(history.getPaymentGroup().getDisbursementType())) {
                    writeTag(os, 4, "disbursementType", history.getPaymentGroup().getDisbursementType().getCode());
                } else {
                    writeTag(os, 4, "disbursementType", history.getDisbursementType().getCode());
                }

                writeCloseTag(os, 2, "check");

                if (!testMode) {
                    history.setLastUpdatedTimestamp(new Timestamp(processDate.getTime()));
                    history.setPmtCancelExtractDate(new Timestamp(processDate.getTime()));
                    history.setPmtCancelExtractStat(Boolean.TRUE);
                    history.setChangeTime(new Timestamp(new Date().getTime()));

                    this.businessObjectService.save(history);
                }
            }

            writeCloseTag(os, 0, "canceledChecks");
            createDoneFile(filename);
        } catch (IOException ie) {
            LOG.error("extractCanceledChecks() Problem reading file:  " + filename, ie);
            throw new IllegalArgumentException("Error writing to output file: " + ie.getMessage());
        } finally {
            // Close file
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ie) {
                    // Not much we can do now
                }
            }
        }
    }

    @Override
    public void extractAchPayments() {
        LOG.debug("extractAchPayments() started");

        Date processDate = dateTimeService.getCurrentDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        PaymentStatus extractedStatus = this.businessObjectService.findBySinglePrimaryKey(PaymentStatus.class,
                PdpConstants.PaymentStatusCodes.EXTRACTED);

        String achFilePrefix = this.kualiConfigurationService.getPropertyValueAsString(
                PdpKeyConstants.ExtractPayment.ACH_FILENAME);
        achFilePrefix = MessageFormat.format(achFilePrefix, new Object[]{null});

        String filename = getOutputFile(achFilePrefix, processDate);
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractAchPayments() filename = " + filename);
        }

        // Open file
        BufferedWriter os = null;

        writeExtractAchFile(extractedStatus, filename, processDate, sdf);
        createDoneFile(filename);
    }

    @Override
    public void extractChecks() {
        LOG.debug("extractChecks() started");

        Date processDate = dateTimeService.getCurrentDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        PaymentStatus extractedStatus = this.businessObjectService.findBySinglePrimaryKey(PaymentStatus.class,
                PdpConstants.PaymentStatusCodes.EXTRACTED);

        String checkFilePrefix = this.kualiConfigurationService.getPropertyValueAsString(
                PdpKeyConstants.ExtractPayment.CHECK_FILENAME);
        checkFilePrefix = MessageFormat.format(checkFilePrefix, new Object[]{null});

        String filename = getOutputFile(checkFilePrefix, processDate);
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractChecks() filename: " + filename);
        }

        List<PaymentProcess> extractsToRun = this.processDao.getAllExtractsToRun();
        for (PaymentProcess extractToRun : extractsToRun) {
            writeExtractCheckFile(extractedStatus, extractToRun, filename, extractToRun.getId().intValue());
            extractToRun.setExtractedInd(true);
            businessObjectService.save(extractToRun);
        }
    }

    protected boolean isResearchParticipantExtractFile(Integer processId) {
        if (parameterService.parameterExists(PaymentDetail.class, PdpConstants.RESEARCH_PARTICIPANT_CUSTOMER_PROFILE)) {
            Map<String, Integer> fieldValues = new HashMap<>();
            fieldValues.put("processId", processId);
            Collection<ProcessSummary> processSummaryList = this.businessObjectService.findMatching(
                    ProcessSummary.class, fieldValues);
            ProcessSummary processSummary = processSummaryList.iterator().next();
            Collection<String> researchParticipantCustomers = parameterService.getParameterValuesAsString(
                    PaymentDetail.class, PdpConstants.RESEARCH_PARTICIPANT_CUSTOMER_PROFILE);
            for (String researchParticipantCustomer : researchParticipantCustomers) {
                String[] customerArray = researchParticipantCustomer.split(KFSConstants.DASH);
                CustomerProfile customer = processSummary.getCustomer();
                if (customer.getChartCode().equals(customerArray[0])
                        && customer.getUnitCode().equals(customerArray[1])
                        && customer.getSubUnitCode().equals(customerArray[2])) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void writeExtractCheckFile(PaymentStatus extractedStatus, PaymentProcess p, String filename,
            Integer processId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date processDate = dateTimeService.getCurrentDate();
        BufferedWriter os = null;

        //Check whether this is for research participant upload. If the customer profile matches research participant's
        //customer profile, then change the filename to append the RP-Upload prefix.
        if (isResearchParticipantExtractFile(processId)) {
            String checkFilePrefix = this.kualiConfigurationService.getPropertyValueAsString(
                    PdpKeyConstants.ExtractPayment.CHECK_FILENAME);
            checkFilePrefix = MessageFormat.format(checkFilePrefix, new Object[]{null});
            checkFilePrefix = PdpConstants.RESEARCH_PARTICIPANT_FILE_PREFIX + KFSConstants.DASH + checkFilePrefix;
            filename = getOutputFile(checkFilePrefix, processDate);
        }

        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8);
            os = new BufferedWriter(writer);
            os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writeOpenTagAttribute(os, 0, "checks", "processId", processId.toString(), "campusCode",
                    p.getCampusCode());

            List<String> bankCodes = paymentGroupService.getDistinctBankCodesForProcessAndType(processId,
                    PdpConstants.DisbursementTypeCodes.CHECK);

            for (String bankCode : bankCodes) {
                List<Integer> disbNbrs = paymentGroupService.getDisbursementNumbersByDisbursementTypeAndBankCode(processId,
                        PdpConstants.DisbursementTypeCodes.CHECK, bankCode);
                for (Integer disbursementNbr : disbNbrs) {
                    boolean first = true;

                    KualiDecimal totalNetAmount = new KualiDecimal(0);

                    // this seems wasteful, but since the total net amount is needed on the first payment detail...it's needed
                    Iterator<PaymentDetail> i2 =
                            paymentDetailService.getByDisbursementNumber(disbursementNbr, processId,
                                    PdpConstants.DisbursementTypeCodes.CHECK, bankCode);
                    while (i2.hasNext()) {
                        PaymentDetail pd = i2.next();
                        totalNetAmount = totalNetAmount.add(pd.getNetPaymentAmount());
                    }

                    List<KualiInteger> paymentGroupIdsSaved = new ArrayList<>();

                    Iterator<PaymentDetail> paymentDetails = paymentDetailService.getByDisbursementNumber(
                            disbursementNbr, processId, PdpConstants.DisbursementTypeCodes.CHECK, bankCode);
                    while (paymentDetails.hasNext()) {
                        PaymentDetail detail = paymentDetails.next();
                        PaymentGroup group = detail.getPaymentGroup();
                        if (!testMode) {
                            if (!paymentGroupIdsSaved.contains(group.getId())) {
                                group.setDisbursementDate(new java.sql.Date(processDate.getTime()));
                                group.setPaymentStatus(extractedStatus);
                                this.businessObjectService.save(group);
                                paymentGroupIdsSaved.add(group.getId());
                            }
                        }

                        if (first) {
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
                            writeTag(os, 4, "paymentDate", sdf.format(group.getPaymentDate()));

                            // Write customer profile information
                            CustomerProfile cp = group.getBatch().getCustomerProfile();
                            writeCustomerProfile(os, 4, cp);

                            writeOpenTag(os, 4, "payments");
                        }

                        writeOpenTag(os, 6, "payment");

                        writeTag(os, 8, "purchaseOrderNbr", detail.getPurchaseOrderNbr());
                        writeTag(os, 8, "invoiceNbr", detail.getInvoiceNbr());
                        writeTag(os, 8, "requisitionNbr", detail.getRequisitionNbr());
                        writeTag(os, 8, "custPaymentDocNbr", detail.getCustPaymentDocNbr());
                        writeTag(os, 8, "customerUnivNbr", detail.getCustomerInstitutionNumber());
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

                        first = false;
                    }
                    writeCloseTag(os, 4, "payments");
                    writeCloseTag(os, 2, "check");
                }
            }
            writeCloseTag(os, 0, "checks");
            createDoneFile(filename);
        } catch (IOException ie) {
            LOG.error("extractChecks() Problem reading file:  " + filename, ie);
            throw new IllegalArgumentException("Error writing to output file: " + ie.getMessage());
        } finally {
            // Close file
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ie) {
                    // Not much we can do now
                }
            }
        }
    }

    protected void writeExtractAchFile(PaymentStatus extractedStatus, String filename, Date processDate,
            SimpleDateFormat sdf) {
        BufferedWriter os = null;
        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8);
            os = new BufferedWriter(writer);
            os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writeOpenTag(os, 0, "achPayments");

            // totals for summary
            Map<String, Integer> unitCounts = new HashMap<>();
            Map<String, KualiDecimal> unitTotals = new HashMap<>();

            Iterator iter = paymentGroupService.getByDisbursementTypeStatusCode(PdpConstants.DisbursementTypeCodes.ACH,
                    PdpConstants.PaymentStatusCodes.PENDING_ACH);
            while (iter.hasNext()) {
                PaymentGroup paymentGroup = (PaymentGroup) iter.next();
                if (!testMode) {
                    paymentGroup.setDisbursementDate(new java.sql.Date(processDate.getTime()));
                    paymentGroup.setPaymentStatus(extractedStatus);
                    businessObjectService.save(paymentGroup);
                }

                writeOpenTagAttribute(os, 2, "ach", "disbursementNbr",
                        paymentGroup.getDisbursementNbr().toString());
                PaymentProcess paymentProcess = paymentGroup.getProcess();
                writeTag(os, 4, "processCampus", paymentProcess.getCampusCode());
                writeTag(os, 4, "processId", paymentProcess.getId().toString());

                writeBank(os, 4, paymentGroup.getBank());

                writeTag(os, 4, "disbursementDate", sdf.format(processDate));
                writeTag(os, 4, "netAmount", paymentGroup.getNetPaymentAmount().toString());

                writePayeeAch(os, 4, paymentGroup);
                writeTag(os, 4, "paymentDate", sdf.format(paymentGroup.getPaymentDate()));

                // Write customer profile information
                CustomerProfile cp = paymentGroup.getBatch().getCustomerProfile();
                writeCustomerProfile(os, 4, cp);

                // Write all payment level information
                writeOpenTag(os, 4, "payments");
                List pdList = paymentGroup.getPaymentDetails();
                for (Object pd : pdList) {
                    PaymentDetail paymentDetail = (PaymentDetail) pd;
                    writeOpenTag(os, 6, "payment");

                    // Write detail info
                    writeTag(os, 6, "purchaseOrderNbr", paymentDetail.getPurchaseOrderNbr());
                    writeTag(os, 6, "invoiceNbr", paymentDetail.getInvoiceNbr());
                    writeTag(os, 6, "requisitionNbr", paymentDetail.getRequisitionNbr());
                    writeTag(os, 6, "custPaymentDocNbr", paymentDetail.getCustPaymentDocNbr());
                    writeTag(os, 6, "customerUnivNbr", paymentDetail.getCustomerInstitutionNumber());
                    writeTag(os, 6, "invoiceDate", sdf.format(paymentDetail.getInvoiceDate()));

                    writeTag(os, 6, "origInvoiceAmount", paymentDetail.getOrigInvoiceAmount().toString());
                    writeTag(os, 6, "netPaymentAmount", paymentDetail.getNetPaymentAmount().toString());
                    writeTag(os, 6, "invTotDiscountAmount", paymentDetail.getInvTotDiscountAmount().toString());
                    writeTag(os, 6, "invTotShipAmount", paymentDetail.getInvTotShipAmount().toString());
                    writeTag(os, 6, "invTotOtherDebitAmount", paymentDetail.getInvTotOtherDebitAmount().toString());
                    writeTag(os, 6, "invTotOtherCreditAmount", paymentDetail.getInvTotOtherCreditAmount().toString());

                    writeOpenTag(os, 6, "notes");
                    for (PaymentNoteText note : paymentDetail.getNotes()) {
                        writeTag(os, 8, "note", escapeString(note.getCustomerNoteText()));
                    }
                    writeCloseTag(os, 6, "notes");

                    writeCloseTag(os, 4, "payment");

                    String unit = paymentGroup.getBatch().getCustomerProfile().getChartCode() + "-" +
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
                }

                writeCloseTag(os, 4, "payments");
                writeCloseTag(os, 2, "ach");
            }
            writeCloseTag(os, 0, "achPayments");

            // send summary email
            paymentFileEmailService.sendAchSummaryEmail(unitCounts, unitTotals, dateTimeService.getCurrentDate());
        } catch (IOException ie) {
            LOG.error("extractAchPayments() Problem reading file:  " + filename, ie);
            throw new IllegalArgumentException("Error writing to output file: " + ie.getMessage());
        } finally {
            // Close file
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ie) {
                    // Not much we can do now
                }
            }
        }
    }

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

    protected void writeOpenTagAttribute(BufferedWriter os, int indent, String tag, String attr, String attrVal)
            throws IOException {
        os.write(SPACES.substring(0, indent));
        os.write("<" + tag + " " + attr + "=\"" + escapeString(attrVal) + "\">\n");
    }

    protected void writeOpenTagAttribute(BufferedWriter os, int indent, String tag, String attr1, String attr1Val,
            String attr2, String attr2Val) throws IOException {
        os.write(SPACES.substring(0, indent));
        os.write("<" + tag + " " + attr1 + "=\"" + escapeString(attr1Val) + "\" " + attr2 + "=\"" +
                escapeString(attr2Val) + "\">\n");
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

    protected void writePayeeInformation(BufferedWriter os, int indent, PaymentGroup pg, boolean includeAch)
            throws IOException {
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
        String countryName = "";
        if (StringUtils.isNotBlank(pg.getCountry())) {
            Country country = locationService.getCountry(pg.getCountry());
            if (country != null) {
                countryName = country.getName();
            }
            if (StringUtils.isBlank(countryName)) {
                countryName = pg.getCountry();
            }
        }
        writeTag(os, indent + 2, "country", countryName);

        if (includeAch) {
            writeTag(os, indent + 2, "achBankRoutingNbr", pg.getAchBankRoutingNbr());
            writeTag(os, indent + 2, "achBankAccountNbr", pg.getAchAccountNumber().getAchBankAccountNbr());
            writeTag(os, indent + 2, "achAccountType", pg.getAchAccountType());
        }
        writeCloseTag(os, indent, "payee");
    }

    /**
     * Creates a '.done' file with the name of the original file.
     */
    protected void createDoneFile(String filename) {
        String doneFileName = StringUtils.substringBeforeLast(filename, ".") + ".done";
        File doneFile = new File(doneFileName);

        if (!doneFile.exists()) {
            boolean doneFileCreated;
            try {
                doneFileCreated = doneFile.createNewFile();
            } catch (IOException e) {
                LOG.error("unable to create done file " + doneFileName, e);
                throw new RuntimeException("Errors encountered while saving the file: Unable to create .done file " +
                        doneFileName, e);
            }

            if (!doneFileCreated) {
                LOG.error("unable to create done file " + doneFileName);
                throw new RuntimeException("Errors encountered while saving the file: Unable to create .done file " +
                        doneFileName);
            }
        }
    }

    protected String escapeString(String input) {
        String output = input.replaceAll("\\&", "&amp;");
        output = output.replaceAll("\"", "&quot;");
        output = output.replaceAll("\\'", "&apos;");
        output = output.replaceAll("\\<", "&lt;");
        output = output.replaceAll("\\>", "&gt;");
        return output;
    }

    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setPaymentGroupService(PaymentGroupService paymentGroupService) {
        this.paymentGroupService = paymentGroupService;
    }

    public void setPaymentDetailService(PaymentDetailService paymentDetailService) {
        this.paymentDetailService = paymentDetailService;
    }

    public void setPaymentGroupHistoryDao(PaymentGroupHistoryDao paymentGroupHistoryDao) {
        this.paymentGroupHistoryDao = paymentGroupHistoryDao;
    }

    public void setProcessDao(ProcessDao processDao) {
        this.processDao = processDao;
    }

    public void setPaymentFileEmailService(PdpEmailService paymentFileEmailService) {
        this.paymentFileEmailService = paymentFileEmailService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setConfigurationService(ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    public List<String> getRequiredDirectoryNames() {
        return new ArrayList<String>() {
            {
                add(directoryName);
            }
        };
    }

    public DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    /**
     * Long check stub note
     */
    @Override
    public List<String> formatCheckNoteLines(String checkNote) {
        List<String> formattedCheckNoteLines = new ArrayList<>();

        if (StringUtils.isBlank(checkNote)) {
            return formattedCheckNoteLines;
        }

        String[] textLines = StringUtils.split(checkNote, BusinessObjectReportHelper.LINE_BREAK);
        int maxLengthOfNoteLine = dataDictionaryService.getAttributeMaxLength(PaymentNoteText.class, "customerNoteText");
        for (String textLine : textLines) {
            String text = WordUtils.wrap(textLine, maxLengthOfNoteLine, BusinessObjectReportHelper.LINE_BREAK, true);
            String[] wrappedTextLines = StringUtils.split(text, BusinessObjectReportHelper.LINE_BREAK);
            Collections.addAll(formattedCheckNoteLines, wrappedTextLines);
        }
        return formattedCheckNoteLines;
    }

}
