package edu.cornell.kfs.sys.service;

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
import java.util.Set;

import org.kuali.kfs.kim.api.role.RoleService;

/**
 * Extension to ReportWriterService to add email properties and expose some
 * internal values
 *
 * @author Dave Raines
 * @version $Revision$
 */
public interface ReportWriterService extends org.kuali.kfs.sys.service.ReportWriterService {
	public void initialize();

	public File getReportFile();

	public int getLineNumber();

	public int getPageLength();

	public boolean isNewPage();

	public void setNewPage(boolean newPage);

	public String getTitle();

	public String getFullFilePath();

	public String getFromAddress();

	public void setFromAddress(String fromAddress);

	public Set<String> getCcAddresses();

	public void setCcAddresses(Set<String> ccAddressList);

	public String getMessageBody();

	public void setMessageBody(String messageBody);

	public RoleService getRoleService();

	public void setRoleService(RoleService roleService);

	public void setFileNamePrefix(String fileNamePrefix);

	public void setTitle(String title);

	public void destroy();

	public void initalize(String dataFileNamePrefix);
}
