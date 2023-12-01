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
package org.kuali.kfs.sys.batch.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.kim.api.identity.IdentityService;
import org.kuali.kfs.kim.impl.identity.principal.Principal;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.Batch;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.service.CustomerProfileService;
import org.kuali.kfs.pdp.service.PdpEmailService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.service.PaymentSourceExtractionService;
import org.kuali.kfs.sys.batch.service.PaymentSourceToExtractService;
import org.kuali.kfs.sys.document.PaymentSource;
import org.kuali.kfs.sys.document.service.FinancialSystemDocumentService;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Transactional
public class PaymentSourceExtractionServiceImpl implements PaymentSourceExtractionService {

    private static final Logger LOG = LogManager.getLogger();

    protected DateTimeService dateTimeService;
    protected CustomerProfileService customerProfileService;
    protected BusinessObjectService businessObjectService;
    protected PdpEmailService paymentFileEmailService;
    protected PaymentSourceToExtractService<PaymentSource> paymentSourceToExtractService;
    protected DocumentService documentService;
    private FinancialSystemDocumentService financialSystemDocumentService;
    protected Set<String> checkAchFsloDocTypes;

    private IdentityService identityService;

    // This should only be set to true when testing this system. Setting this to true will run the code but
    // won't set the doc status to extracted
    boolean testMode;

    /**
     * This method extracts all payments from a disbursement voucher with a status code of "A" and uploads them as a
     * batch for processing.
     *
     * @return Always returns true if the method completes.
     */
    @Override
    public boolean extractPayments() {
        LOG.debug("extractPayments() started");
        final Date processRunDate = dateTimeService.getCurrentDate();

        final Principal user = identityService.getPrincipalByPrincipalName(KFSConstants.SYSTEM_USER);
        if (user == null) {
            LOG.debug("extractPayments() Unable to find user {}", KFSConstants.SYSTEM_USER);
            throw new IllegalArgumentException("Unable to find user " + KFSConstants.SYSTEM_USER);
        }

        // Get a list of campuses that have documents with an 'A' (approved) status.
        final Map<String, List<PaymentSource>> campusListMap = paymentSourceToExtractService
                .retrievePaymentSourcesByCampus(false);

        if (campusListMap != null && !campusListMap.isEmpty()) {
            // Process each campus one at a time
            for (final String campusCode : campusListMap.keySet()) {
                extractPaymentsForCampus(campusCode, user.getPrincipalId(), processRunDate,
                        campusListMap.get(campusCode));
            }
        }

        return true;
    }

    /**
     * Pulls all disbursement vouchers with status of "A" and marked for immediate payment from the database and
     * builds payment records for them
     */
    @Override
    public void extractImmediatePayments() {
        LOG.debug("extractImmediatePayments() started");
        final Date processRunDate = dateTimeService.getCurrentDate();
        final Principal uuser = identityService
                .getPrincipalByPrincipalName(KFSConstants.SYSTEM_USER);
        if (uuser == null) {
            LOG.debug("extractPayments() Unable to find user {}", KFSConstants.SYSTEM_USER);
            throw new IllegalArgumentException("Unable to find user " + KFSConstants.SYSTEM_USER);
        }

        // Get a list of campuses that have documents with an 'A' (approved) status.
        final Map<String, List<PaymentSource>> documentsByCampus = paymentSourceToExtractService
                .retrievePaymentSourcesByCampus(true);
        // Process each campus one at a time
        for (final String campusCode : documentsByCampus.keySet()) {
            extractImmediatePaymentsForCampus(campusCode, uuser.getPrincipalId(), processRunDate,
                    documentsByCampus.get(campusCode));
        }
    }

    /**
     * This method extracts all outstanding payments from all the disbursement vouchers in approved status for a given
     * campus and adds these payments to a batch file that is uploaded for processing.
     *
     * @param campusCode     The id code of the campus the payments will be retrieved for.
     * @param principalId    The user object used when creating the batch file to upload with outstanding payments.
     * @param processRunDate This is the date that the batch file is created, often this value will be today's date.
     */
    protected void extractPaymentsForCampus(
            final String campusCode, final String principalId, final Date processRunDate,
            final List<? extends PaymentSource> documents) {
        LOG.debug("extractPaymentsForCampus() started for campus: {}", campusCode);

        final Batch batch = createBatch(campusCode, principalId, processRunDate);
        int count = 0;
        KualiDecimal totalAmount = KualiDecimal.ZERO;

        for (final PaymentSource document : documents) {
            if (paymentSourceToExtractService.shouldExtractPayment(document)) {
                addPayment(document, batch, processRunDate, false);
                count++;
                totalAmount = totalAmount.add(paymentSourceToExtractService.getPaymentAmount(document));
            }
        }

        batch.setPaymentCount(new KualiInteger(count));
        batch.setPaymentTotalAmount(totalAmount);

        businessObjectService.save(batch);
        paymentFileEmailService.sendLoadEmail(batch);
    }

    /**
     * Builds payment batch for Disbursement Vouchers marked as immediate
     *
     * @param campusCode     the campus code the disbursement vouchers should be associated with
     * @param principalId    the user responsible building the payment batch (typically the System User, kfs)
     * @param processRunDate the time that the job to build immediate payments is run
     */
    protected void extractImmediatePaymentsForCampus(
            final String campusCode,
            final String principalId,
            final Date processRunDate,
            final List<? extends PaymentSource> documents
    ) {
        LOG.debug("extractImmediatePaymentsForCampus() started for campus: {}", campusCode);

        if (!documents.isEmpty()) {
            final Batch batch = createBatch(campusCode, principalId, processRunDate);
            int count = 0;
            KualiDecimal totalAmount = KualiDecimal.ZERO;

            for (final PaymentSource document : documents) {
                if (paymentSourceToExtractService.shouldExtractPayment(document)) {
                    addPayment(document, batch, processRunDate, false);
                    count++;
                    totalAmount = totalAmount.add(paymentSourceToExtractService.getPaymentAmount(document));
                }
            }

            batch.setPaymentCount(new KualiInteger(count));
            batch.setPaymentTotalAmount(totalAmount);

            businessObjectService.save(batch);
            paymentFileEmailService.sendLoadEmail(batch);
        }
    }

    /**
     * This method creates a payment group from the disbursement voucher and batch provided and persists that group
     * to the database.
     *
     * @param document       The document used to build a payment group detail.
     * @param batch          The batch file used to build a payment group and detail.
     * @param processRunDate The date the batch file is to post.
     */
    protected PaymentGroup addPayment(
            final PaymentSource document,
            final Batch batch,
            final Date processRunDate,
            final boolean immediate) {
        LOG.info("addPayment() started for document number={}", document::getDocumentNumber);

        final java.sql.Date sqlProcessRunDate = new java.sql.Date(processRunDate.getTime());
        final PaymentGroup pg = paymentSourceToExtractService.createPaymentGroup(document, sqlProcessRunDate);
        // the payment source returned null instead of a PaymentGroup?  I guess it didn't want to be paid for some
        // reason (for instance, a 0 amount document or doc which didn't have a travel advance, etc)
        if (pg != null) {
            pg.setBatch(batch);
            // KFSUPGEADE-973 : Cu mods
            // TODO : override this method may not work. because 'testMode' is  package private.  call super.addPayment will not return pg.
            // so, copy over for now.
            if (document instanceof DisbursementVoucherDocument) {
                if(pg.isPayableByACH()) {
                	pg.setDisbursementTypeCode(PdpConstants.DisbursementTypeCodes.ACH);
                }
                else {
                	pg.setDisbursementTypeCode(PdpConstants.DisbursementTypeCodes.CHECK);
                }
            	
            }
            if (immediate) {
                pg.setProcessImmediate(Boolean.TRUE);
            }

            businessObjectService.save(pg);

            if (!testMode) {
                paymentSourceToExtractService.markAsExtracted(document, sqlProcessRunDate, pg.getId());
            }
        }
        return pg;
    }

    /**
     * This method creates a Batch instance and populates it with the information provided.
     *
     * @param campusCode     The campus code used to retrieve a customer profile to be set on the batch.
     * @param principalId    The user who submitted the batch.
     * @param processRunDate The date the batch was submitted and the date the customer profile was generated.
     * @return A fully populated batch instance.
     */
    protected Batch createBatch(final String campusCode, final String principalId, final Date processRunDate) {
        final String unitCode = paymentSourceToExtractService.getPreDisbursementCustomerProfileUnit();
        final String subUnitCode = paymentSourceToExtractService.getPreDisbursementCustomerProfileSubUnit();
        final CustomerProfile customer = customerProfileService.get(campusCode, unitCode, subUnitCode);
        if (customer == null) {
            throw new IllegalArgumentException("Unable to find customer profile for " + campusCode + "/" + unitCode +
                    "/" + subUnitCode);
        }

        // Create the group for this campus
        final Batch batch = new Batch();
        batch.setCustomerProfile(customer);
        batch.setCustomerFileCreateTimestamp(new Timestamp(processRunDate.getTime()));
        batch.setFileProcessTimestamp(new Timestamp(processRunDate.getTime()));
        batch.setPaymentFileName(KFSConstants.DISBURSEMENT_VOUCHER_PDP_EXTRACT_FILE_NAME);
        batch.setSubmiterUserId(principalId);

        // Set these for now, we will update them later
        batch.setPaymentCount(KualiInteger.ZERO);
        batch.setPaymentTotalAmount(KualiDecimal.ZERO);

        businessObjectService.save(batch);

        return batch;
    }

    /**
     * This method retrieves a list of disbursement voucher documents that are in the status provided for the campus
     * code given.
     *
     * @param statusCode     The status of the disbursement vouchers to be retrieved.
     * @param campusCode     The campus code that the disbursement vouchers will be associated with.
     * @param immediatesOnly only retrieve Disbursement Vouchers marked for immediate payment
     * @return A collection of disbursement voucher objects that meet the search criteria given.
     */
    protected Collection<DisbursementVoucherDocument> getListByDocumentStatusCodeCampus(
            final String statusCode,
            final String campusCode,
            final boolean immediatesOnly
    ) {
        LOG.info(
                "getListByDocumentStatusCodeCampus(statusCode={}, campusCode={}, immediatesOnly={}) started",
                statusCode,
                campusCode,
                immediatesOnly
        );

        final Collection<DisbursementVoucherDocument> list = new ArrayList<>();

        final Collection<DisbursementVoucherDocument> docs = financialSystemDocumentService
                .findByDocumentHeaderStatusCode(DisbursementVoucherDocument.class, statusCode);
        for (final DisbursementVoucherDocument element : docs) {
            final String dvdCampusCode = element.getCampusCode();

            if (dvdCampusCode.equals(campusCode)
                    && KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK.equals(
                            element.getDisbVchrPaymentMethodCode())) {
                if (!immediatesOnly || element.isImmediatePaymentIndicator()) {
                    list.add(element);
                }
            }
        }

        return list;
    }

    /**
     * Extracts a single DisbursementVoucherDocument
     */
    @Override
    public void extractSingleImmediatePayment(final PaymentSource paymentSource) {
        LOG.debug("extractImmediatePayment(DisbursementVoucherDocument) started");
        if (paymentSourceToExtractService.shouldExtractPayment(paymentSource)) {
            final Date processRunDate = dateTimeService.getCurrentDate();
            extractSinglePayment(paymentSource, processRunDate, true);
            paymentFileEmailService.sendPaymentSourceImmediateExtractEmail(paymentSource,
                    paymentSourceToExtractService.getImmediateExtractEMailFromAddress(),
                    paymentSourceToExtractService.getImmediateExtractEmailToAddresses());
        }
    }

    @Override
    public void extractSingleExternalPayment(final PaymentSource paymentSource) {
        LOG.debug("extractSingleExternalPayment(PaymentSource) started");
        if (paymentSourceToExtractService.shouldExtractPayment(paymentSource)) {
            final java.sql.Date processRunDate = dateTimeService.getCurrentSqlDate();
            final PaymentGroup paymentGroup = extractSinglePayment(
                    paymentSource,
                    processRunDate,
                    false);
            paymentSourceToExtractService.markAsPaidExternally(
                    paymentSource,
                    paymentGroup,
                    processRunDate);
        }
    }

    private PaymentGroup extractSinglePayment(
            final PaymentSource paymentSource,
            final Date processRunDate,
            final boolean immediate) {
        final Principal principal = identityService.getPrincipalByPrincipalName(KFSConstants.SYSTEM_USER);
        if (principal == null) {
            LOG.debug("extractSinglePayment() Unable to find user {}", KFSConstants.SYSTEM_USER);
            throw new IllegalArgumentException("Unable to find user " + KFSConstants.SYSTEM_USER);
        }

        final Batch batch = createBatch(paymentSource.getCampusCode(), principal.getPrincipalId(), processRunDate);
        KualiDecimal totalAmount = KualiDecimal.ZERO;

        final PaymentGroup paymentGroup = addPayment(paymentSource, batch, processRunDate, immediate);
        totalAmount = totalAmount.add(paymentSourceToExtractService.getPaymentAmount(paymentSource));

        batch.setPaymentCount(new KualiInteger(1L));
        batch.setPaymentTotalAmount(totalAmount);

        businessObjectService.save(batch);
        return paymentGroup;
    }

    public void setDateTimeService(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setCustomerProfileService(final CustomerProfileService customerProfileService) {
        this.customerProfileService = customerProfileService;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setPaymentFileEmailService(final PdpEmailService paymentFileEmailService) {
        this.paymentFileEmailService = paymentFileEmailService;
    }

    public PaymentSourceToExtractService<PaymentSource> getPaymentSourceToExtractService() {
        return paymentSourceToExtractService;
    }

    public void setPaymentSourceToExtractService(
            final PaymentSourceToExtractService<PaymentSource> paymentSourceToExtractService) {
        this.paymentSourceToExtractService = paymentSourceToExtractService;
    }

    public DocumentService getDocumentService() {
        return documentService;
    }

    public void setDocumentService(final DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setFinancialSystemDocumentService(final FinancialSystemDocumentService financialSystemDocumentService) {
        this.financialSystemDocumentService = financialSystemDocumentService;
    }

    public void setIdentityService(final IdentityService identityService) {
        this.identityService = identityService;
    }
}
