package edu.cornell.kfs.sys.service.impl;

/*
 * Copyright 2012 The Kuali Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kim.api.role.RoleService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.sys.service.ReportWriterService;

/**
 * UConn implementation of the Kuali ReportWriterTextService. This
 * implementation exposes some reportWriterService properties like reportFile
 * and lineNumber. It also adds mail properties for the purpose of mailing the
 * report.
 *
 * @author Dave Raines
 */
public class ReportWriterTextServiceImpl extends org.kuali.kfs.sys.service.impl.ReportWriterTextServiceImpl
		implements ReportWriterService {
	private static final Logger LOG = LogManager.getLogger();

	private static final String REPORT_FILE_NAME_INFIX = "_report_";
	private static final int NUM_CHARS_ALLOWED_FOR_FULL_FILE_PATH = 512;

	protected String fullFilePath;
	protected String fromAddress;
	protected String messageBody;
	protected RoleService roleService;
	protected Set<String> ccAddresses;
	
	protected String springFileNamePrefixBackupCopy = KFSConstants.EMPTY_STRING;
	protected boolean resetFileNamePrefixToSpringValue = false;

	public ReportWriterTextServiceImpl() {
		super();
		ccAddresses = new HashSet<String>();
	}

	@Override
	public void initialize() {
		try {
			fullFilePath = generateFullFilePath();
			LOG.info("initialize, report fullFilePath: {}", fullFilePath);
			printStream = new PrintStream(fullFilePath);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

		page = initialPageNumber;
		initializeBusinessObjectReportHelpers();
		this.writeHeader(title);
	}

	@Override
	public void initialize(String fullyQualifiedDataFileName) {
	    String dataFileNamePrefix = obtainDataFileName(fullyQualifiedDataFileName);
	    if (StringUtils.isNotBlank(fullyQualifiedDataFileName)
	            && StringUtils.isNotBlank(dataFileNamePrefix)
	            && fullFilePathLimitWouldNotBeExceeded(dataFileNamePrefix)) {
	        //use data file name as part of this report file name
	        configureFileNamePrefixForThisReport(dataFileNamePrefix);
	        LOG.info("initialize, data file name configured into report prefix name: {}", fileNamePrefix);
	    } //else - use spring configured default report file name
	    initialize();
	}
	
	/**
	 * This method generates a file name prefix from the fully qualified file name attribute
	 *     (i.e.   directoryPath/fileName.fileExtension)
	 * This functionality was created for the CreateAccountingDocumentReportItem xmlFileName attribute but could
	 * be utilized by any other batch job that desired the report output file contain the input data file name
	 * as the report file prefix. The input parameter is assumed to contain the fully qualified directory path
	 * as well as a file name and extension. This method strips off both the directory path and file extension
	 * returning just the file name portion of the string.
	 * When both a directory path and file extension are not detected, a zero length string is returned.
	 *
	 * Example:
	 *  Input parameter : /infra/work/staging/fp/accountingXmlDocument/fp_ib_netsuite_20240229_050035.xml
	 *  Return value    : fp_ib_netsuite_20240229_050035
	 *  
	 *  @param fullyQualifiedDataFileName
	 */
	private String obtainDataFileName(String fullyQualifiedDataFileName) {
	    String onlyDataFileName = KFSConstants.EMPTY_STRING;
	    
	    if (StringUtils.contains(fullyQualifiedDataFileName, KFSConstants.DELIMITER)
	            && (StringUtils.contains(fullyQualifiedDataFileName, File.separator))) {
	        onlyDataFileName = removeFileExtension(fullyQualifiedDataFileName);
	        
	        if (StringUtils.isNotBlank(onlyDataFileName)) {
	            onlyDataFileName = StringUtils.substringAfterLast(onlyDataFileName, File.separator);
	        }
	    }
	    return onlyDataFileName;
	}
	
	private String removeFileExtension(String fullyQualifiedDataFileName) {
	    String filePathAndNameWithExtensionRemoved = KFSConstants.EMPTY_STRING;
	    String [] subStringArray = StringUtils.split(fullyQualifiedDataFileName, KFSConstants.DELIMITER);
	    if (ObjectUtils.isNotNull(subStringArray) 
	            && subStringArray.length == 2) {
	        filePathAndNameWithExtensionRemoved =  subStringArray[0];
	    }
	    return filePathAndNameWithExtensionRemoved;
	}
	
	private boolean fullFilePathLimitWouldNotBeExceeded(String dataFileNamePrefix) {
	    int numCharsCalculated = StringUtils.length(filePath) + File.separator.length() 
	            + StringUtils.length(dataFileNamePrefix) + StringUtils.length(REPORT_FILE_NAME_INFIX) 
	                + StringUtils.length(dateTimeService.toDateTimeStringForFilename(dateTimeService.getCurrentDate())) 
	                    + StringUtils.length(fileNameSuffix);

	    if (numCharsCalculated < NUM_CHARS_ALLOWED_FOR_FULL_FILE_PATH) {
	        LOG.debug("fullFilePathLimitWouldNotBeExceeded: report file name prefix {} ok for path limit.", dataFileNamePrefix);
	        return true;
	    } else {
	        LOG.warn("fullFilePathLimitWouldNotBeExceeded: Using data file XML name as prefix for report file prefix "
	                    + "would make full report file path, name, extension too long. Using default report file name "
	                    + "prefix {} instead.", fileNamePrefix);
	        return false;
	    }
	}
	
	private void configureFileNamePrefixForThisReport(String dataFileNamePrefix) {
	    resetFileNamePrefixToSpringValue = true;
	    springFileNamePrefixBackupCopy = fileNamePrefix;
	    setFileNamePrefix(dataFileNamePrefix + REPORT_FILE_NAME_INFIX);
	}
	
	private void restoreFileNamePrefixToSpringConfiguredPrefix() {
	    if (resetFileNamePrefixToSpringValue) {
	        resetFileNamePrefixToSpringValue = false;
	        setFileNamePrefix(springFileNamePrefixBackupCopy);
	        springFileNamePrefixBackupCopy = KFSConstants.EMPTY_STRING;
	        LOG.info("restoreFileNamePrefixToSpringConfiguredPrefix, data file name prefix reset to Spring default value: {}", fileNamePrefix);
	    }
	}

	@Override
	public File getReportFile() {
		File report = new File(this.fullFilePath);
		return report;
	}

	@Override
	public int getLineNumber() {
		return line;
	}

	@Override
	public int getPageLength() {
		return pageLength;
	}

	@Override
	public boolean isNewPage() {
		return newPage;
	}

	@Override
	public void setNewPage(boolean newPage) {
		this.newPage = newPage;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getFullFilePath() {
		return fullFilePath;
	}

	@Override
	public String getFromAddress() {
		return fromAddress;
	}

	@Override
	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	@Override
	public Set<String> getCcAddresses() {
		return ccAddresses;
	}

	@Override
	public void setCcAddresses(Set<String> ccAddressList) {
		this.ccAddresses = ccAddressList;
	}

	@Override
	public String getMessageBody() {
		return messageBody;
	}

	@Override
	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	@Override
	public RoleService getRoleService() {
		return roleService;
	}

	@Override
	public void setRoleService(RoleService roleService) {
		this.roleService = roleService;
	}
	
	@Override
	public void writeFormattedMessageLine(String format, Object... args) {
		super.writeFormattedMessageLine(format, args);
		debugWriteFormattedMessageLine(format, args);
	}

	protected void debugWriteFormattedMessageLine(String format, Object... args) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("writeFormattedMessageLine, the format: " + format);
			int i = 0;
			for (Object arg : args) {
				LOG.debug("writeFormattedMessageLine, the " + i + " arg is " + arg.toString());
				i++;
			}
		}
	}

	@Override
    public void setFileNamePrefix(String fileNamePrefix) {
        super.setFileNamePrefix(fileNamePrefix);
    }

	@Override
    public void setTitle(String title) {
        super.setTitle(title);
    }

	@Override
	public void destroy() {
	    restoreFileNamePrefixToSpringConfiguredPrefix();
	    super.destroy();
	}
}
