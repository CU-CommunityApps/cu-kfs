/*
 * Copyright 2008 The Kuali Foundation
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
package edu.cornell.kfs.module.bc.document.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import org.kuali.kfs.module.bc.util.ExternalizedMessageWrapper;

import com.lowagie.text.DocumentException;

import edu.cornell.kfs.module.bc.businessobject.SipImportData;

public interface SipImportService {
    
    /**
     * Parses the file and validates
     * 
     * @param fileImportStream
     * @return
     */
	public String importFile(InputStream fileImportStream, List<ExternalizedMessageWrapper> errorReport, String principalId, boolean allowExecutivesToBeImported, List<SipImportData> importData);
    
   
    /**
     * Generates the log file of the lines with the specific errors on the lines indented below and a summary of the errors
     * 
     * @param errorMessages
     * @param baos
     */
    public void generateValidationReportInTextFormat(List<ExternalizedMessageWrapper> logMessages, ByteArrayOutputStream baos);
    
    /**
     * Generates the log file of the lines with the specific errors on the lines indented below and a summary of the errors
     * 
     * @param errorMessages
     * @param baos
     * @throws DocumentException
     */
    public void generateValidationReportInPdfFormat(List<ExternalizedMessageWrapper> logMessages, ByteArrayOutputStream baos)  throws DocumentException;

    
}

