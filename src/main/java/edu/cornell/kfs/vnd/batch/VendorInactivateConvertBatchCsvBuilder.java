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
package edu.cornell.kfs.vnd.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.cornell.kfs.vnd.businessobject.VendorInactivateConvertBatch;


/**
 * CSVBuilder convert the parsed data values into ReceiptProcessing BO list,
 * @author cab379
 */
public class VendorInactivateConvertBatchCsvBuilder {
	private static final Logger LOG = LogManager.getLogger(VendorInactivateConvertBatchCsvBuilder.class);

    /**
     * Convert the parseData object into ReceiptProcessing BO 
     * 
     * @param parseDataList
     * @return
     */
    public static List<VendorInactivateConvertBatch> buildVendorUpdates(List<Map<String, String>> parseDataList) {
        List<VendorInactivateConvertBatch> vendorUpdates = new ArrayList<VendorInactivateConvertBatch>();

        VendorInactivateConvertBatch vendorUpdate = null, dataVendorUpdate;
        for (Map<String, String> rowDataMap : parseDataList) {

            dataVendorUpdate = buildVendorUpdatesFromDataMap(rowDataMap);
            vendorUpdate = dataVendorUpdate;
            vendorUpdates.add(vendorUpdate);
        }
        return vendorUpdates;
    }

     
    /**
     * build the ReceiptProcessing BO from the data row
     * 
     * @param rowDataMap
     * @return
     */
    private static VendorInactivateConvertBatch buildVendorUpdatesFromDataMap(Map<String, String> rowDataMap) {

    	VendorInactivateConvertBatch vendorUpdate = new VendorInactivateConvertBatch();
    	vendorUpdate.setVendorId(rowDataMap.get(VendorInactivateConvertBatchCsv.vendorID.name()));        
    	vendorUpdate.setAction(rowDataMap.get(VendorInactivateConvertBatchCsv.action.name()));        
    	vendorUpdate.setNote(rowDataMap.get(VendorInactivateConvertBatchCsv.note.name()));
    	vendorUpdate.setReason(rowDataMap.get(VendorInactivateConvertBatchCsv.reason.name()));
    	vendorUpdate.setConvertType(rowDataMap.get(VendorInactivateConvertBatchCsv.convertType.name()));
    	return vendorUpdate;
    }

}
