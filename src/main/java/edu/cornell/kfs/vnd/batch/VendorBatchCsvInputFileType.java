/*
 * Copyright 2011 The Kuali Foundation
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
package edu.cornell.kfs.vnd.batch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.batch.CsvBatchInputFileTypeBase;
import org.kuali.kfs.sys.exception.ParseException;

import edu.cornell.kfs.vnd.businessobject.VendorBatchDetail;

public class VendorBatchCsvInputFileType  extends CsvBatchInputFileTypeBase<VendorBatchCsv> {

	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(VendorBatchCsvInputFileType.class);

    private static final String FILE_NAME_DELIM = "_";
           
    
    /**
     * 
     * @see This method is required by extending CsvBatchInputFileTypeBase which implements an interface that requires this method,
     *  but we do not need this method for anything in this class
     */
    public String getFileName(String principalName, Object parsedFileContents, String fileUserIdentifer) {
        return "";
    }

    /**
     * 
     * @see org.kuali.kfs.sys.batch.BatchInputFileType#getFileTypeIdentifer()
     */
    public String getFileTypeIdentifer() {
        return "vendorBatch_kfs";
    }

    
    public boolean validate(Object parsedFileContents) {
        return true;
    }
    
    /**
     * override super class implementation to specify/convert to the expected data structure
     * 
     * For customer load, it will be CustomerDigesterVO
     * 
     * @see org.kuali.kfs.sys.batch.CsvBatchInputFileTypeBase#parse(byte[])
     */
    @Override
    public Object parse(byte[] fileByteContent) throws ParseException {
        
        //super class should have already defined a way to parse the content
        Object parsedContents = super.parse(fileByteContent);        
        List<VendorBatchDetail> vendors = (List<VendorBatchDetail>)convertParsedObjectToVO(parsedContents);
        return vendors;    
    }
    
  
    public String getAuthorPrincipalName(File file) {
        String[] fileNameParts = StringUtils.split(file.getName(), FILE_NAME_DELIM);
        if (fileNameParts.length > 3) {
            return fileNameParts[2];
        }
        return null;
    }

    
    /**
     * Convert the parsedFileContents object into Receipt Processing for validation
     * 
     * @see org.kuali.kfs.sys.batch.CsvBatchInputFileTypeBase#convertParsedObjectToVO(java.lang.Object)
     */
    @Override
    protected Object convertParsedObjectToVO(Object parsedContent) {
        List<VendorBatchDetail> vendors = new ArrayList<VendorBatchDetail>();
        try {
            //  attempt to cast the parsedFileContents into the expected type
            List<Map<String, String>> parseDataList = (List<Map<String, String>>) parsedContent; 
            vendors = VendorBatchCsvBuilder.buildVendorBatch(parseDataList);
        }
        catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
        
        return vendors;
    }

    public String getTitleKey() {
        
        return "Vendor (CSV format) Batch Attachement Upload";
    }
    

}
