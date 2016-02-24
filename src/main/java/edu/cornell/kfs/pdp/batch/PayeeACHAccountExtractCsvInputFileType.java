package edu.cornell.kfs.pdp.batch;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.CsvBatchInputFileTypeBase;
import org.kuali.kfs.sys.exception.ParseException;

import edu.cornell.kfs.pdp.CUPdpConstants;

/**
 * File type denoting a Payee ACH Account Extract .csv file for adding or updating Payee ACH Accounts.
 */
public class PayeeACHAccountExtractCsvInputFileType extends CsvBatchInputFileTypeBase<PayeeACHAccountExtractCsv> {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PayeeACHAccountExtractCsvInputFileType.class);

    // This field is duplicated from the superclass because access to this class is needed here, but the superclass makes it private without any getters.
    private Class<?> csvEnumClass;

    /**
     * This implementation just returns an empty string, since the returned value is not needed in this case.
     * 
     * @see org.kuali.kfs.sys.batch.BatchInputFileType#getFileName(java.lang.String, java.lang.Object, java.lang.String)
     */
    @Override
    public String getFileName(String principalName, Object parsedFileContents, String fileUserIdentifier) {
        return KFSConstants.EMPTY_STRING;
    }

    @Override
    public String getFileTypeIdentifer() {
        return CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_FILE_TYPE_ID;
    }

    @Override
    public boolean validate(Object parsedFileContents) {
        return true;
    }

    @Override
    public String getAuthorPrincipalName(File file) {
        return null;
    }

    @Override
    public String getTitleKey() {
        return "Workday ACH Batch Upload";
    }

    /**
     * Overridden to call "convertParsedObjectToVO" after performing the regular parsing.
     * 
     * @see org.kuali.kfs.sys.batch.CsvBatchInputFileTypeBase#parse(byte[])
     */
    @Override
    public Object parse(byte[] fileByteContent) throws ParseException {
        Object parsedContents = super.parse(fileByteContent);
        return convertParsedObjectToVO(parsedContents);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object convertParsedObjectToVO(Object parsedContent) {
        // Convert from List<Map<String,String>> to a list of DTOs, and catch errors appropriately as in similar parsing methods.
        try {
            return PayeeACHAccountExtractCsvBuilder.buildPayeeACHAccountExtract((List<Map<String,String>>) parsedContent);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Overridden to build a list of the enum constants' toString() values instead,
     * since the expected enum class is using custom toString() header names in this case.
     * PayeeACHAccountExtractCsv is the expected enum class.
     * 
     * Subclasses that use a different enum class will need to override this accordingly.
     * 
     * @see org.kuali.kfs.sys.batch.CsvBatchInputFileTypeBase#getCsvHeaderList()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected List<String> getCsvHeaderList() {
        List<String> headers = new ArrayList<String>();
        for (PayeeACHAccountExtractCsv enumConst : EnumSet.allOf((Class<PayeeACHAccountExtractCsv>) getCsvEnumClass())) {
            headers.add(enumConst.toString());
        }
        return headers;
    }

    protected Class<?> getCsvEnumClass() {
        return csvEnumClass;
    }

    /**
     * Overridden to also make the enum class available to this implementation.
     * 
     * @see org.kuali.kfs.sys.batch.CsvBatchInputFileTypeBase#setCsvEnumClass(java.lang.Class)
     */
    @Override
    public void setCsvEnumClass(Class<?> csvEnumClass) {
        super.setCsvEnumClass(csvEnumClass);
        this.csvEnumClass = csvEnumClass;
    }

}
