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
package edu.cornell.kfs.module.receiptProcessing.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.cornell.kfs.module.receiptProcessing.businessobject.ReceiptProcessing;
import edu.cornell.kfs.module.receiptProcessing.batch.ReceiptProcessingCSV;


/**
 * CSVBuilder convert the parsed data values into ReceiptProcessing BO list,
 * @author cab379
 */
public class ReceiptProcessingCSVBuilder {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ReceiptProcessingCSVBuilder.class);

    /**
     * Convert the parseData object into ReceiptProcessing BO 
     * 
     * @param parseDataList
     * @return
     */
    public static List<ReceiptProcessing> buildReceiptProcessing(List<Map<String, String>> parseDataList) {
        List<ReceiptProcessing> receipts = new ArrayList<ReceiptProcessing>();

        ReceiptProcessing receipt = null, dataReceipt;
        for (Map<String, String> rowDataMap : parseDataList) {

            dataReceipt = buildReceiptsFromDataMap(rowDataMap);
            receipt = dataReceipt;
            receipts.add(receipt);
        }
        return receipts;
    }

     
    /**
     * build the ReceiptProcessing BO from the data row
     * 
     * @param rowDataMap
     * @return
     */
    private static ReceiptProcessing buildReceiptsFromDataMap(Map<String, String> rowDataMap) {

        ReceiptProcessing receipt = new ReceiptProcessing();
        receipt.setCardHolder(rowDataMap.get(ReceiptProcessingCSV.cardHolder.name()));        
        receipt.setAmount(rowDataMap.get(ReceiptProcessingCSV.amount.name()));
        receipt.setPurchasedate(rowDataMap.get(ReceiptProcessingCSV.purchasedate.name()));
        receipt.setSharePointPath(rowDataMap.get(ReceiptProcessingCSV.SharePointPath.name()));
        receipt.setFilename(rowDataMap.get(ReceiptProcessingCSV.filename.name()));
        return receipt;
    }

}
