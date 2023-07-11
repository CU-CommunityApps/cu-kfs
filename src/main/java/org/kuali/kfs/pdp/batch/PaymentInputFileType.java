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
package org.kuali.kfs.pdp.batch;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.businessobject.LoadPaymentStatus;
import org.kuali.kfs.pdp.businessobject.PayeeId;
import org.kuali.kfs.pdp.businessobject.PaymentAccountDetail;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentFileLoad;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.service.PaymentFileService;
import org.kuali.kfs.pdp.service.PdpEmailService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.batch.XmlBatchInputFileTypeBase;
import org.kuali.kfs.sys.exception.ParseException;

import java.io.File;
import java.sql.Timestamp;

/**
 * Batch input type for the PDP payment file.
 */
public class PaymentInputFileType extends XmlBatchInputFileTypeBase<PaymentFileLoad> {

    private static final Logger LOG = LogManager.getLogger();

    private AccountService accountService;
    protected DateTimeService dateTimeService;
    protected PaymentFileService paymentFileService;
    protected PdpEmailService paymentFileEmailService;

    @Override
    public String getFileName(
            final String principalName, final Object parsedFileContents, final String fileUserIdentifier
    ) {
        final Timestamp currentTimestamp = dateTimeService.getCurrentTimestamp();

        String fileName = PdpConstants.PDP_FILE_UPLOAD_FILE_PREFIX + "_" + principalName;
        if (StringUtils.isNotBlank(fileUserIdentifier)) {
            fileName += "_" + StringUtils.remove(fileUserIdentifier, " ");
        }
        fileName += "_" + dateTimeService.toString(currentTimestamp, "yyyyMMdd_HHmmss");

        // remove spaces in filename
        fileName = StringUtils.remove(fileName, " ");

        return fileName;
    }

    @Override
    public String getAuthorPrincipalName(final File file) {
        final String[] fileNameParts = StringUtils.split(file.getName(), "_");
        if (fileNameParts.length > 3) {
            return fileNameParts[2];
        }
        return null;
    }

    @Override
    public String getFileTypeIdentifier() {
        return PdpConstants.PAYMENT_FILE_TYPE_IDENTIFIER;
    }

    @Override
    public boolean validate(final Object parsedFileContents) {
        final PaymentFileLoad paymentFile = (PaymentFileLoad) parsedFileContents;

        // add validation for chartCode-accountNumber, as chartCode is not required in xsd due to
        // accounts-cant-cross-charts option
        boolean validAccounts = true;
        if (paymentFile.getPaymentGroups() != null) {
            for (final PaymentGroup payGroup : paymentFile.getPaymentGroups()) {
                if (payGroup.getPaymentDetails() == null) {
                    continue;
                }
                for (final PaymentDetail payDetail : payGroup.getPaymentDetails()) {
                    if (payDetail.getAccountDetail() == null) {
                        continue;
                    }
                    for (final PaymentAccountDetail acctDetail : payDetail.getAccountDetail()) {
                        // if chart code is empty while accounts cannot cross charts, then derive chart code from
                        //account number
                        if (StringUtils.isEmpty(acctDetail.getFinChartCode())) {
                            if (accountService.accountsCanCrossCharts()) {
                                GlobalVariables.getMessageMap().putError(
                                        KFSConstants.GLOBAL_ERRORS,
                                        KFSKeyConstants.ERROR_BATCH_UPLOAD_FILE_EMPTY_CHART,
                                        acctDetail.getAccountNbr()
                                );
                                validAccounts = false;
                            } else {
                                // accountNumber shall not be empty, otherwise won't pass schema validation
                                final Account account =
                                        accountService.getUniqueAccountForAccountNumber(acctDetail.getAccountNbr());
                                if (account != null) {
                                    acctDetail.setFinChartCode(account.getChartOfAccountsCode());
                                } else {
                                    GlobalVariables.getMessageMap().putError(
                                            KFSConstants.GLOBAL_ERRORS,
                                            KFSKeyConstants.ERROR_BATCH_UPLOAD_FILE_INVALID_ACCOUNT,
                                            acctDetail.getAccountNbr()
                                    );
                                    validAccounts = false;
                                }
                            }
                        }
                    }
                }
            }
        }

        paymentFileService.doPaymentFileValidation(paymentFile, GlobalVariables.getMessageMap());
        return validAccounts && paymentFile.isPassedValidation();
    }

    @Override
    public String getTitleKey() {
        return PdpKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_PAYMENT;
    }

    @Override
    public void process(final String fileName, final Object parsedFileContents) {
        final PaymentFileLoad paymentFile = (PaymentFileLoad) parsedFileContents;
        if (paymentFile.isPassedValidation()) {
            // collect various information for status of load
            final LoadPaymentStatus status = new LoadPaymentStatus();
            status.setMessageMap(new MessageMap());

            paymentFileService.loadPayments(paymentFile, status, fileName);
            paymentFileService.createOutputFile(status, fileName);
        }
    }

    /**
     * Overridden so we can send the error email here. (keep it consistent between the batch processing and the online
     * file upload web processing)
     */
    @Override
    public PaymentFileLoad parse(final byte[] fileByteContent) throws ParseException {
        PaymentFileLoad paymentFile = null;

        try {
            paymentFile = super.parse(fileByteContent);

            // set the payment group payeeId and payeeIdTypeCd from the payeeIdObj that was set from the XML
            for (final PaymentGroup paymentGroup : paymentFile.getPaymentGroups()) {
                final PayeeId payeeIdObj = paymentGroup.getPayeeIdObj();
                if (payeeIdObj != null) {
                    paymentGroup.setPayeeIdTypeCd(payeeIdObj.getIdType());
                    // CU customization
                    paymentGroup.setPayeeIdAndName(payeeIdObj.getValue());
                    if(PdpConstants.PayeeIdTypeCodes.VENDOR_ID.equalsIgnoreCase(payeeIdObj.getIdType())){
                        paymentGroup.setPayeeOwnerCdFromVendor(payeeIdObj.getValue());
                    }
                }
            }

        } catch (final ParseException e1) {
            LOG.error("Error parsing xml contents: {}", e1::getMessage);
            final MessageMap errorMap = new MessageMap();
            errorMap.putError(
                    KFSConstants.GLOBAL_ERRORS,
                    KFSKeyConstants.ERROR_BATCH_UPLOAD_PARSING,
                    "Error parsing xml contents: " + e1.getMessage()
            );

            // Send error email
            paymentFileEmailService.sendErrorEmail(paymentFile, errorMap);
            throw new ParseException("Error parsing xml contents: " + e1.getMessage(), e1);
        }

        return paymentFile;
    }

    public void setAccountService(final AccountService accountService) {
        this.accountService = accountService;
    }

    public void setPaymentFileService(final PaymentFileService paymentFileService) {
        this.paymentFileService = paymentFileService;
    }

    public void setDateTimeService(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setPaymentFileEmailService(final PdpEmailService paymentFileEmailService) {
        this.paymentFileEmailService = paymentFileEmailService;
    }
}
