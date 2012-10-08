/*
 * Copyright 2009 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.fp;

public class FPConstants {
    public static final String DV_FILE_UPLOAD_FILE_PREFIX = "dv_file_";
    public static final String DV_FILE_TYPE_INDENTIFIER = "disbursementVoucherInputFileType";
    public static final String DV_BATCH_ID_SEQUENCE_NAME = "DV_BATCH_ID_SEQ";
    
    // constants used as the key of the statistics entries for the batch audit report
    public class BatchReportStatisticKeys {
        public static final String NUM_DV_RECORDS_READ = "numDVRecordsRead";
        public static final String NUM_ACCOUNTING_RECORDS_READ = "numAccountingRecordsRead";
        public static final String NUM_DV_RECORDS_WRITTEN = "numDVRecordsWritten";
        public static final String NUM_ACCOUNTING_RECORDS_WRITTEN = "numAccountingRecordsWritten";
        public static final String NUM_GLPE_RECORDS_WRITTEN = "numGlpeRecordsWritten";
    }
}
