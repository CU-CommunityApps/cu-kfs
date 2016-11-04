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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Extension to MailMessage to allow attachments
 *
 * @author Dave Raines
 * @version $Revision$
 */
public class MailMessage extends org.kuali.rice.core.api.mail.MailMessage {

	private HashSet<File> attachments;

	public MailMessage() {
		super();
		attachments = new HashSet<File>();
	}

	public void addAttachment(File file) {
		attachments.add(file);
	}

	/**
	 *
	 */
	public Set<File> getAttachments() {
		return attachments;
	}

	/**
	 *
	 */
	public void setAttachments(Collection<File> attachments) {
		this.attachments = new HashSet<File>(attachments);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("to Address: ").append(this.getToAddresses()).append('\n');
		buffer.append("cc Address: ").append(this.getCcAddresses()).append('\n');
		buffer.append("subject: ").append(this.getSubject()).append('\n');
		buffer.append("body: ").append(this.getMessage()).append('\n');
		buffer.append("from Address: ").append(this.getFromAddress()).append('\n');

		return buffer.toString();
	}

}
