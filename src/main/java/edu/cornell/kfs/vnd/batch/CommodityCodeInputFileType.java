/**
 * 
 */
package edu.cornell.kfs.vnd.batch;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.gl.batch.CollectorFlatFileInputType;
import org.kuali.kfs.sys.batch.BatchInputFileTypeBase;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.vnd.businessobject.CommodityCode;
import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

/**
 * @author dwf5
 *
 */
public class CommodityCodeInputFileType extends BatchInputFileTypeBase {

	private static final Logger LOG = LogManager.getLogger(CommodityCodeInputFileType.class);
    protected DateTimeService dateTimeService;
    protected static final String FILE_NAME_PREFIX = "pur_unspsc_";

    /**
	 * @see org.kuali.kfs.sys.batch.BatchInputFileType#getFileTypeIdentifier()
	 */
	public String getFileTypeIdentifier() {
		return CUKFSConstants.COMMODITY_CODE_FILE_TYPE_INDENTIFIER;
	}

    /**
     * Builds the file name using the following construction: All commodity code files start with pur_unspsc_
     *  append the username of the user who is uploading the file then the user supplied identifier finally
     * the timestamp
     * 
     * @param user who uploaded the file
     * @param parsedFileContents represents commodity codes
     * @param userIdentifier user identifier for user who uploaded file
     * @return String returns file name using the convention mentioned in the description
     * 
     * @see org.kuali.kfs.sys.batch.BatchInputFileType#getFileName(java.lang.String, java.lang.Object,
     *      java.lang.String)
     */
    public String getFileName(String principalName, Object parsedFileContents, String fileUserIdentifer) {
        String fileName = FILE_NAME_PREFIX;
        fileName += principalName;
        if (org.apache.commons.lang.StringUtils.isNotBlank(fileUserIdentifer)) {
            fileName += "_" + fileUserIdentifer;
        }
        fileName += "_" + dateTimeService.toDateTimeStringForFilename(dateTimeService.getCurrentDate());

        // remove spaces in filename
        fileName = org.apache.commons.lang.StringUtils.remove(fileName, " ");

        return fileName;
    }

    /**
	 * @see org.kuali.kfs.sys.batch.BatchInputFileType#parse(byte[])
	 */
	public Object parse(byte[] fileByteContent) throws ParseException {
        List<CommodityCode> batchList = new ArrayList<CommodityCode>();
        CommodityCode newCode = null;
        BufferedReader bufferedFileReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fileByteContent)));
        String fileLine;
        String commodityCode;
        String commodityCodeDesc;
        int lineNumber = 0;
        
        try {
            while ((fileLine = bufferedFileReader.readLine()) != null) {
                lineNumber++;
                // Parse the tab delimited lines
                StringTokenizer st = new StringTokenizer(fileLine, "\t");
                commodityCode = st.nextToken();
                commodityCodeDesc = st.nextToken();
                if(st.hasMoreTokens()) {
                	// Report too many values on the line
                }
                newCode = new CommodityCode();
                newCode.setPurchasingCommodityCode(commodityCode);
                newCode.setCommodityDescription(commodityCodeDesc);
                batchList.add(newCode);
            }
        }
        catch (IOException e) {
            // probably won't happen since we're reading from a byte array, but just in case
            LOG.error("Error encountered reading from file content", e);
            throw new ParseException("Error encountered reading from file content", e);
        }
        
        return batchList;
	}

	/**
	 * Validation should always return true, since some values to be added may not exist in the DB yet.
	 * 
	 * @see org.kuali.kfs.sys.batch.BatchInputFileType#validate(java.lang.Object)
	 */
	public boolean validate(Object parsedFileContents) {
        return true;
	}

	/**
	 * @see org.kuali.kfs.sys.batch.BatchInputFileType#process(java.lang.String, java.lang.Object)
	 */
	public void process(String fileName, Object parsedFileContents) {
		// do not do anything
	}

	/**
	 * @see org.kuali.kfs.sys.batch.BatchInputType#getAuthorPrincipalName(java.io.File)
	 */
	public String getAuthorPrincipalName(File file) {
        return org.apache.commons.lang.StringUtils.substringBetween(file.getName(), FILE_NAME_PREFIX, "_");
	}

	/**
	 * @see org.kuali.kfs.sys.batch.BatchInputType#getTitleKey()
	 */
	public String getTitleKey() {
        return CUKFSKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_COMMODITY_CODE;
	}

	/**
	 * 
	 * @param dateTimeService
	 */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }
    
}
