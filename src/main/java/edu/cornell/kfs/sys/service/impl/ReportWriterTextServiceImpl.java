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

	private static String REPORT_FILE_NAME_INFIX = "_report_";
	private static int NUM_CHARS_ALLOWED_FOR_FULL_FILE_PATH = 512;

	protected String fullFilePath;
	protected String fromAddress;
	protected String messageBody;
	protected RoleService roleService;
	protected Set<String> ccAddresses;

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
    public void initalize(String dataFileNamePrefix) {
        if (StringUtils.isNotBlank(dataFileNamePrefix)
                && fullFilePathLimitWouldNotBeExceeded(dataFileNamePrefix)) {
            LOG.info("initalize, with XML data file name prefix: {}", dataFileNamePrefix);
            setFileNamePrefix(dataFileNamePrefix + REPORT_FILE_NAME_INFIX);
        }
        initialize();
    }

	private boolean fullFilePathLimitWouldNotBeExceeded(String dataFileNamePrefix) {
        //initialized to force use of default report file prefix when processing fails for any reason
        int numCharsCalculated = NUM_CHARS_ALLOWED_FOR_FULL_FILE_PATH + 1;

        numCharsCalculated = filePath.length() + File.separator.length() 
                + dataFileNamePrefix.length() + REPORT_FILE_NAME_INFIX.length() 
                    + dateTimeService.toDateTimeStringForFilename(dateTimeService.getCurrentDate()).length() 
                        + fileNameSuffix.length();

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
	    super.destroy();
	}
}
