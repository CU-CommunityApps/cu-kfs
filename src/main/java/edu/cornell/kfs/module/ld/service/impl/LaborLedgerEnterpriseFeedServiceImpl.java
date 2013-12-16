package edu.cornell.kfs.module.ld.service.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.kns.service.DateTimeService;

import edu.cornell.kfs.module.ld.service.LaborLedgerEnterpriseFeedService;

public class LaborLedgerEnterpriseFeedServiceImpl implements LaborLedgerEnterpriseFeedService {

    private static final String TRANSACTION_SIGN = "+";
    public static final int UNIV_FISCAL_PRD_CD_START_INDEX = 29;
    public static final int UNIV_FISCAL_PRD_CD_END_INDEX = 31;
    public static final int FDOC_TYP_CD_START_INDEX = 31;
    public static final int FDOC_TYP_CD_END_INDEX = 35;
    public static final int FS_ORIGIN_CD_START_INDEX = 35;
    public static final int FS_ORIGIN_CD_END_INDEX = 37;
    public static final int FDOC_NBR_START_INDEX = 37;
    public static final int FDOC_NBR_END_INDEX = 51;
    public static final int TRANSACTION_SIGN_START_INDEX = 114;
    public static final int TRANSACTION_SIGN_END_INDEX = 115;
    public static final int CREDIT_DEBIT_CODE_START_INDEX = 135;
    public static final int CREDIT_DEBIT_CODE_END_INDEX = 136;
    public static final int TRANSACTION_DT_STRART_INDEX = 136;
    public static final int TRANSACTION_DT_END_INDEX = 146;
    public static final String FDOC_REF_TYP_CD = "PAYE";
    public static final int FDOC_REF_TYP_CD_START_INDEX = 164;
    public static final int FDOC_REF_TYP_CD_END_INDEX = 168;
    public static final String FS_REF_ORIGIN_CD = "P4";
    public static final int FS_REF_ORIGIN_CD_START_INDEX = 168;
    public static final int FS_REF_ORIGIN_CD_END_INDEX = 170;
    public static final int FDOC_REF_NBR_START_INDEX = 170;
    public static final int FDOC_REF_NBR_END_INDEX = 184;
    public static final String TRN_ENCUM_UPDT_CD = "R";
    public static final int TRN_ENCUM_UPDT_CD_START_INDEX = 194;
    public static final int TRN_ENCUM_UPDT_CD_END_INDEX = 195;
    public static final int LD_ENTERPRISE_FEED_LINE_LENGTH = 296;

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    protected AccountingPeriodService accountingPeriodService;
    protected DateTimeService dateTimeService;

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(LaborLedgerEnterpriseFeedServiceImpl.class);

    /**
     * @see edu.cornell.kfs.module.ld.service.LaborLedgerEnterpriseFeedService#createDisencumbrance(java.io.InputStream)
     */
    public InputStream createDisencumbrance(InputStream encumbranceFile) {

        InputStream disencumbranceStream = null;
        if (encumbranceFile == null) {

            return null;
        }

        Date currentSqlDate = dateTimeService.getCurrentSqlDate();
        java.util.Date currentDate = dateTimeService.getCurrentDate();

        // get fiscal period code for current date
        AccountingPeriod accountingPeriod = accountingPeriodService.getByDate(currentSqlDate);
        String fiscalPeriodCode = accountingPeriod.getUniversityFiscalPeriodCode();

        ByteArrayOutputStream disencEntriesPrintStream = new ByteArrayOutputStream();
        try {

            InputStream fStream = encumbranceFile;
            BufferedReader in = new BufferedReader(new InputStreamReader(fStream));

            while (in.ready()) {
                String currentLine = in.readLine();

                if (currentLine != null && currentLine.length() == LD_ENTERPRISE_FEED_LINE_LENGTH) {
                    String resultLine = currentLine;

                    // set fiscal period code
                    resultLine = StringUtils.overlay(resultLine, fiscalPeriodCode, UNIV_FISCAL_PRD_CD_START_INDEX, UNIV_FISCAL_PRD_CD_END_INDEX);

                    // set the sign to always pe +
                    resultLine = StringUtils.overlay(resultLine, TRANSACTION_SIGN, TRANSACTION_SIGN_START_INDEX, TRANSACTION_SIGN_END_INDEX);

                    // set the debit/credit code
                    if (KFSConstants.GL_CREDIT_CODE.equalsIgnoreCase(StringUtils.substring(currentLine, CREDIT_DEBIT_CODE_START_INDEX, CREDIT_DEBIT_CODE_END_INDEX))) {
                        resultLine = StringUtils.overlay(resultLine, KFSConstants.GL_DEBIT_CODE, CREDIT_DEBIT_CODE_START_INDEX, CREDIT_DEBIT_CODE_END_INDEX);
                    } else if (KFSConstants.GL_DEBIT_CODE.equalsIgnoreCase(StringUtils.substring(currentLine, CREDIT_DEBIT_CODE_START_INDEX, CREDIT_DEBIT_CODE_END_INDEX))) {
                        resultLine = StringUtils.overlay(resultLine, KFSConstants.GL_CREDIT_CODE, CREDIT_DEBIT_CODE_START_INDEX, CREDIT_DEBIT_CODE_END_INDEX);
                    }

                    // set transaction date
                    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                    String currentDateString = sdf.format(currentDate);
                    resultLine = StringUtils.overlay(resultLine, currentDateString, TRANSACTION_DT_STRART_INDEX, TRANSACTION_DT_END_INDEX);

                    // set the reference transaction document type code to be the same as the input transaction document type
                    // code
                    String inputTransDocumentTypeCode = StringUtils.substring(currentLine, FDOC_TYP_CD_START_INDEX, FDOC_TYP_CD_END_INDEX);
                    // resultLine = StringUtils.overlay(resultLine, FDOC_REF_TYP_CD, FDOC_REF_TYP_CD_START_INDEX,
                    // FDOC_REF_TYP_CD_END_INDEX);
                    resultLine = StringUtils.overlay(resultLine, inputTransDocumentTypeCode, FDOC_REF_TYP_CD_START_INDEX, FDOC_REF_TYP_CD_END_INDEX);

                    // set the reference origination code to be the same as the input transaction origination code
                    String inputTransOriginationCode = StringUtils.substring(currentLine, FS_ORIGIN_CD_START_INDEX, FS_ORIGIN_CD_END_INDEX);
                    // resultLine = StringUtils.overlay(resultLine, FS_REF_ORIGIN_CD, FS_REF_ORIGIN_CD_START_INDEX,
                    // FS_REF_ORIGIN_CD_END_INDEX);
                    resultLine = StringUtils.overlay(resultLine, inputTransOriginationCode, FS_REF_ORIGIN_CD_START_INDEX, FS_REF_ORIGIN_CD_END_INDEX);

                    // set the transaction encumbrance update code to be R
                    resultLine = StringUtils.overlay(resultLine, TRN_ENCUM_UPDT_CD, TRN_ENCUM_UPDT_CD_START_INDEX, TRN_ENCUM_UPDT_CD_END_INDEX);

                    // set the reference document number to be the input transaction document number
                    String fDocNbr = resultLine.substring(FDOC_NBR_START_INDEX, FDOC_NBR_END_INDEX);
                    resultLine = StringUtils.overlay(resultLine, fDocNbr, FDOC_REF_NBR_START_INDEX, FDOC_REF_NBR_END_INDEX);

                    resultLine = resultLine.concat(KFSConstants.NEWLINE);

                    disencEntriesPrintStream.write((resultLine.getBytes()));

                } else {
                    LOG.info("Input file has an invalid format");
                    in.close();
                    disencEntriesPrintStream.close();
                    return null;
                }
            }

            in.close();
            disencEntriesPrintStream.flush();
            disencumbranceStream = new ByteArrayInputStream(disencEntriesPrintStream.toByteArray());
            disencEntriesPrintStream.close();
            return disencumbranceStream;

        } catch (FileNotFoundException e) {
            LOG.info("Error creating PrintStream to write invalid entries");
        } catch (IOException e) {
            LOG.info("Error creating the disencumbrance file");
        }

        return disencumbranceStream;
    }

    /**
     * Gets the accountingPeriodService.
     * 
     * @return accountingPeriodService
     */
    public AccountingPeriodService getAccountingPeriodService() {
        return accountingPeriodService;
    }

    /**
     * Sets the accountingPeriodService.
     * 
     * @param accountingPeriodService
     */
    public void setAccountingPeriodService(AccountingPeriodService accountingPeriodService) {
        this.accountingPeriodService = accountingPeriodService;
    }

    /**
     * Gets the dateTimeService.
     * 
     * @return dateTimeService
     */
    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    /**
     * Sets the dateTimeService.
     * 
     * @param dateTimeService
     */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
