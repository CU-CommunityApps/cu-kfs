/*
 * Copyright 2007-2008 The Kuali Foundation
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
package org.kuali.kfs.module.purap.batch;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoice;
import org.kuali.kfs.sys.batch.XmlBatchInputFileTypeBase;
import org.kuali.rice.core.api.datetime.DateTimeService;

/**
 * Batch input type for the electronic invoice job.
 */
public class ElectronicInvoiceInputFileType extends XmlBatchInputFileTypeBase {
    
    protected DateTimeService dateTimeService;
    protected static final String FILE_NAME_PREFIX = "purap_einvoice_";
	
    /**
     * Returns the identifier of the electronic invoice file type
     * 
     * @return the electronic invoice file type identifier
     */
    public String getFileTypeIdentifer() {
        return PurapConstants.ELECTRONIC_INVOICE_FILE_TYPE_INDENTIFIER;
    }

    public boolean validate(Object parsedFileContents) {
        return true;
    }

    public String getTitleKey() {
        return PurapKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_EINVOICE;
    }

    public String getAuthorPrincipalName(File file) {
        return null;
    }

    public String getFileName(String principalName, Object parsedFileContents, String fileUserIdentifer) {
        
        if (!(parsedFileContents instanceof ElectronicInvoice)){
            throw new RuntimeException("Invalid object type.");
        }
        
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
	 * 
	 * @param dateTimeService
	 */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }
    
    @Override
    public boolean isDoneFileRequired() {
    	return false;
    }
}

