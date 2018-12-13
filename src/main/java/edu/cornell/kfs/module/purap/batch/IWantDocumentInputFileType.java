package edu.cornell.kfs.module.purap.batch;

import java.io.File;
import java.sql.Timestamp;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.batch.XmlBatchInputFileTypeBase;
import org.kuali.rice.core.api.datetime.DateTimeService;

import com.rsmart.kuali.kfs.fp.FPKeyConstants;

import edu.cornell.kfs.module.purap.CUPurapConstants;

public class IWantDocumentInputFileType extends XmlBatchInputFileTypeBase {
    private DateTimeService dateTimeService;

    /**
     * @see org.kuali.kfs.sys.batch.BatchInputFileType#getFileName(java.lang.String, java.lang.Object, java.lang.String)
     */
    public String getFileName(String principalId, Object parsedFileContents, String fileUserIdentifer) {
        Timestamp currentTimestamp = dateTimeService.getCurrentTimestamp();

        StringBuffer buf = new StringBuffer();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        formatter.setLenient(false);
        formatter.format(currentTimestamp, buf, new FieldPosition(0));

        String fileName = CUPurapConstants.I_WANT_DOC_FEED_FILE_PREFIX + principalId;
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
        return CUPurapConstants.I_WANT_DOC_FEED_FILE_TYPE_INDENTIFIER;
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
//        DisbursementVoucherBatchFeed batchFeed = (DisbursementVoucherBatchFeed) parsedFileContents;
//
//        DisbursementVoucherBatchStatus batchStatus = new DisbursementVoucherBatchStatus();
//        disbursementVoucherDocumentBatchService.loadDisbursementVouchers(batchFeed, batchStatus, fileName, GlobalVariables.getMessageMap());
//        disbursementVoucherDocumentBatchService.generateAuditReport(batchStatus);
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

}
