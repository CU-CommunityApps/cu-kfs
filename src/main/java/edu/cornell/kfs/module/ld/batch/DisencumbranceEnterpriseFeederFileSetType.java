package edu.cornell.kfs.module.ld.batch;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.gl.batch.EnterpriseFeederFileSetType;
import org.kuali.kfs.module.ld.batch.service.EnterpriseFeederService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.kim.impl.identity.Person;

import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class DisencumbranceEnterpriseFeederFileSetType extends EnterpriseFeederFileSetType {
	private static final Logger LOG = LogManager.getLogger(DisencumbranceEnterpriseFeederFileSetType.class);

    private static final String FILE_NAME_PREFIX = "disencFile";
    private static final String FILE_NAME_PART_DELIMITER = "_";
    
    /**
     * Returns directory path for EnterpriseFeederService
     * 
     * @param fileType file type (not used)
     * @see org.kuali.kfs.sys.batch.BatchInputFileSetType#getDirectoryPath(java.lang.String)
     */

    public String getDirectoryPath(String fileType) {
        // all files in the file set go into the same directory
        return SpringContext.getBean(EnterpriseFeederService.class).getDirectoryName();
    }

    
    
    /**
     * @see org.kuali.kfs.sys.batch.BatchInputType#getTitleKey()
     */
    public String getTitleKey() {
        return CUKFSKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_DISENCUMBRANCE_FILE;
    }

    /**
     * @see org.kuali.kfs.sys.batch.BatchInputFileSetType#getDoneFileDirectoryPath()
     */
    public String getDoneFileDirectoryPath() {
        return SpringContext.getBean(EnterpriseFeederService.class).getDirectoryName();
    }
    
    /**
     * Return the file name based on information from user and file user identifier
     * 
     * @param user Person object representing user who uploaded file
     * @param fileUserIdentifer String representing user who uploaded file
     * @return String enterprise feeder formated file name string using information from user and file user identifier
     * @see org.kuali.kfs.sys.batch.BatchInputFileSetType#getFileName(java.lang.String, org.kuali.kfs.kim.bo.Person,
     *      java.lang.String)
     */
    public String getFileName(String fileType, String principalName, String fileUserIdentifer, Date creationDate) {
        DateTimeService dateTimeService = SpringContext.getBean(DateTimeService.class);
        StringBuilder buf = new StringBuilder();
        fileUserIdentifer = StringUtils.deleteWhitespace(fileUserIdentifer);
        fileUserIdentifer = StringUtils.remove(fileUserIdentifer, FILE_NAME_PART_DELIMITER);
        buf.append(FILE_NAME_PREFIX).append(FILE_NAME_PART_DELIMITER).append(principalName)
                .append(FILE_NAME_PART_DELIMITER).append(fileUserIdentifer)
                .append(FILE_NAME_PART_DELIMITER).append(dateTimeService.toDateTimeStringForFilename(creationDate))
                .append(getFileExtension(fileType));
        return buf.toString();
    }
    
    /**
     * Returns done file name for a specific user and file user identifier
     * 
     * @param user the user who uploaded or will upload the file
     * @param fileUserIdentifier the file identifier
     * @return String done file name
     * @see org.kuali.kfs.sys.batch.BatchInputFileSetType#getDoneFileName(org.kuali.kfs.kim.bo.Person, java.lang.String)
     */
    public String getDoneFileName(Person user, String fileUserIdentifer, Date creationDate) {
        DateTimeService dateTimeService = SpringContext.getBean(DateTimeService.class);
        StringBuilder buf = new StringBuilder();
        fileUserIdentifer = StringUtils.deleteWhitespace(fileUserIdentifer);
        fileUserIdentifer = StringUtils.remove(fileUserIdentifer, FILE_NAME_PART_DELIMITER);
        buf.append(FILE_NAME_PREFIX).append(FILE_NAME_PART_DELIMITER).append(user.getPrincipalName())
                .append(FILE_NAME_PART_DELIMITER).append(fileUserIdentifer)
                .append(FILE_NAME_PART_DELIMITER).append(dateTimeService.toDateTimeStringForFilename(creationDate))
                .append(getDoneFileExtension());
        return buf.toString();
    }
    
}