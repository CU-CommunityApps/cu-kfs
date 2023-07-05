/*
 * Copyright 2008 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.fp.batch;

import java.io.File;
import java.sql.Timestamp;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.util.GlobalVariables;

import com.rsmart.kuali.kfs.fp.FPConstants;
import com.rsmart.kuali.kfs.fp.FPKeyConstants;
import com.rsmart.kuali.kfs.fp.batch.service.DisbursementVoucherDocumentBatchService;
import com.rsmart.kuali.kfs.fp.businessobject.DisbursementVoucherBatchFeed;
import com.rsmart.kuali.kfs.fp.businessobject.DisbursementVoucherBatchStatus;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.batch.service.DigesterXMLBatchInputFileType;

/**
 * Batch input type for the disbursement voucher files
 */
public class DisbursementVoucherInputFileType extends DigesterXMLBatchInputFileType {
    private DateTimeService dateTimeService;
    private DisbursementVoucherDocumentBatchService disbursementVoucherDocumentBatchService;

    /**
     * @see org.kuali.kfs.sys.batch.BatchInputFileType#getFileName(java.lang.String, java.lang.Object, java.lang.String)
     */
    public String getFileName(String principalId, Object parsedFileContents, String fileUserIdentifer) {
        Timestamp currentTimestamp = dateTimeService.getCurrentTimestamp();

        StringBuffer buf = new StringBuffer();
        SimpleDateFormat formatter = new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_yyyyMMdd_HHmmss, Locale.US);
        formatter.setLenient(false);
        formatter.format(currentTimestamp, buf, new FieldPosition(0));

        String fileName = FPConstants.DV_FILE_UPLOAD_FILE_PREFIX + principalId;
        if (StringUtils.isNotBlank(fileUserIdentifer)) {
            fileName += "_" + StringUtils.remove(fileUserIdentifer, " ");
        }
        fileName += "_" + buf.toString();

        return fileName;
    }

    /**
     * @see org.kuali.kfs.sys.batch.BatchInputFileType#getFileTypeIdentifier()
     */
    public String getFileTypeIdentifier() {
        return FPConstants.DV_FILE_TYPE_INDENTIFIER;
    }

    /**
     * @see org.kuali.kfs.sys.batch.BatchInputType#getAuthorPrincipalName(java.io.File)
     */
    public String getAuthorPrincipalName(File file) {
        String[] fileNameParts = StringUtils.split(file.getName(), "_");
        if (fileNameParts.length > 3) {
            return fileNameParts[2];
        }
        return null;
    }

    /**
     * @see org.kuali.kfs.sys.batch.BatchInputFileType#validate(java.lang.Object)
     */
    public boolean validate(Object parsedFileContents) {
        return true;
    }

    /**
     * @see org.kuali.kfs.sys.batch.BatchInputType#getTitleKey()
     */
    public String getTitleKey() {
        return FPKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_DISBURSEMENT_VOUCHER;
    }

    /**
     * @see org.kuali.kfs.sys.batch.BatchInputFileTypeBase#process(java.lang.String, java.lang.Object)
     */
    @Override
    public void process(String fileName, Object parsedFileContents) {
        DisbursementVoucherBatchFeed batchFeed = (DisbursementVoucherBatchFeed) parsedFileContents;

        DisbursementVoucherBatchStatus batchStatus = new DisbursementVoucherBatchStatus();
        disbursementVoucherDocumentBatchService.loadDisbursementVouchers(batchFeed, batchStatus, fileName, GlobalVariables.getMessageMap());
        disbursementVoucherDocumentBatchService.generateAuditReport(batchStatus);
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
     * Gets the dateTimeService attribute.
     * 
     * @return Returns the dateTimeService.
     */
    protected DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    /**
     * Gets the disbursementVoucherDocumentBatchService attribute.
     * 
     * @return Returns the disbursementVoucherDocumentBatchService.
     */
    protected DisbursementVoucherDocumentBatchService getDisbursementVoucherDocumentBatchService() {
        return disbursementVoucherDocumentBatchService;
    }

    /**
     * Sets the disbursementVoucherDocumentBatchService attribute value.
     * 
     * @param disbursementVoucherDocumentBatchService The disbursementVoucherDocumentBatchService to set.
     */
    public void setDisbursementVoucherDocumentBatchService(DisbursementVoucherDocumentBatchService disbursementVoucherDocumentBatchService) {
        this.disbursementVoucherDocumentBatchService = disbursementVoucherDocumentBatchService;
    }

}
