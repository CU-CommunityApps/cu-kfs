package edu.cornell.kfs.module.ld.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.module.ld.service.LaborLedgerEnterpriseFeedService;

public class LaborLedgerEnterpriseFeedServiceImpl implements LaborLedgerEnterpriseFeedService {

    private static final String TRANSACTION_SIGN = "+";
    private static final int TRANSACTION_SIGN_START_INDEX = 114;
    private static final int TRANSACTION_SIGN_END_INDEX = 115;
    private static final int CREDIT_DEBIT_CODE_START_INDEX = 135;
    private static final int CREDIT_DEBIT_CODE_END_INDEX = 136;
    public static final String FDOC_REF_TYP_CD = "PAYE";
    public static final int FDOC_REF_TYP_CD_START_INDEX = 165;
    public static final int FDOC_REF_TYP_CD_END_INDEX = 168;
    public static final String FS_REF_ORIGIN_CD = "P4";
    public static final int FS_REF_ORIGIN_CD_START_INDEX = 168;
    public static final int FS_REF_ORIGIN_CD_END_INDEX = 170;
    public static final String TRN_ENCUM_UPDT_CD = "R";
    public static final int TRN_ENCUM_UPDT_CD_START_INDEX = 195;
    public static final int TRN_ENCUM_UPDT_CD_END_INDEX = 196;
    public static final int LD_ENTERPRISE_FEED_LINE_LENGTH = 296;

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(LaborLedgerEnterpriseFeedServiceImpl.class);

    /**
     * @see edu.cornell.kfs.module.ld.service.LaborLedgerEnterpriseFeedService#createDisencumbrance(java.io.File)
     */
    public boolean createDisencumbrance(File encumbranceFile) {

        boolean fileCreated = false;

        if (encumbranceFile == null) {
            fileCreated = false;
            return fileCreated;
        }

        File disencumbranceFile = createDisencumbranceFile(encumbranceFile.getAbsolutePath());

        PrintStream disencEntriesPrintStream;
        try {
            disencEntriesPrintStream = new PrintStream(disencumbranceFile);

            FileInputStream fStream = new FileInputStream(encumbranceFile.getAbsolutePath());
            BufferedReader in = new BufferedReader(new InputStreamReader(fStream));

            while (in.ready()) {
                String currentLine = in.readLine();

                if (currentLine != null && currentLine.length() == LD_ENTERPRISE_FEED_LINE_LENGTH) {
                    String resultLine = currentLine;

                    resultLine = StringUtils.overlay(resultLine, TRANSACTION_SIGN, TRANSACTION_SIGN_START_INDEX, TRANSACTION_SIGN_END_INDEX);

                    if (KFSConstants.GL_CREDIT_CODE.equalsIgnoreCase(StringUtils.substring(currentLine, CREDIT_DEBIT_CODE_START_INDEX, CREDIT_DEBIT_CODE_END_INDEX))) {
                        resultLine = StringUtils.overlay(resultLine, KFSConstants.GL_DEBIT_CODE, CREDIT_DEBIT_CODE_START_INDEX, CREDIT_DEBIT_CODE_END_INDEX);
                    } else if (KFSConstants.GL_DEBIT_CODE.equalsIgnoreCase(StringUtils.substring(currentLine, CREDIT_DEBIT_CODE_START_INDEX, CREDIT_DEBIT_CODE_END_INDEX))) {
                        resultLine = StringUtils.overlay(resultLine, KFSConstants.GL_CREDIT_CODE, CREDIT_DEBIT_CODE_START_INDEX, CREDIT_DEBIT_CODE_END_INDEX);
                    }

                    resultLine = StringUtils.overlay(resultLine, FDOC_REF_TYP_CD, FDOC_REF_TYP_CD_START_INDEX, FDOC_REF_TYP_CD_END_INDEX);
                    resultLine = StringUtils.overlay(resultLine, FS_REF_ORIGIN_CD, FS_REF_ORIGIN_CD_START_INDEX, FS_REF_ORIGIN_CD_END_INDEX);
                    resultLine = StringUtils.overlay(resultLine, TRN_ENCUM_UPDT_CD, TRN_ENCUM_UPDT_CD_START_INDEX, TRN_ENCUM_UPDT_CD_END_INDEX);

                    disencEntriesPrintStream.println(resultLine);

                } else {
                    LOG.info("Input file has an invalid format");
                    fileCreated = false;
                    disencumbranceFile.delete();
                    return fileCreated;
                }
            }
            in.close();
            disencEntriesPrintStream.close();
            fileCreated = true;
        } catch (FileNotFoundException e) {
            fileCreated = false;
            LOG.info("Error creating PrintStream to write invalid entries");
        } catch (IOException e) {
            fileCreated = false;
            disencumbranceFile.delete();
            LOG.info("Error creating the disencumbrance file");
        }

        return fileCreated;
    }

    /**
     * @param encumbranceFile
     * @return
     */
    protected File createDisencumbranceFile(String encumbranceFile) {

        File disencumbranceFile = new File(StringUtils.substringBeforeLast(encumbranceFile, ".") + "_DISENCUM.data");
        try {
            if (!disencumbranceFile.exists()) {
                if (!disencumbranceFile.createNewFile()) {
                    LOG.info("Error creating the disencumbrance file");
                }
            } else {
                LOG.info("The disencumbrance file already exists for input: " + encumbranceFile);
            }
        } catch (IOException e) {
            LOG.error("Exception while creating the disencumbrance file " + e.getMessage());
        }

        return disencumbranceFile;

    }

}
