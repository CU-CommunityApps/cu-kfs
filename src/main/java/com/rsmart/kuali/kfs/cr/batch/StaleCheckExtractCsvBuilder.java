package com.rsmart.kuali.kfs.cr.batch;

import com.rsmart.kuali.kfs.cr.businessobject.StaleCheckBatchRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StaleCheckExtractCsvBuilder {

    public static List<StaleCheckBatchRow> buildStaleCheckExtract(List<Map<String,String>> parseDataList) {
        List<StaleCheckBatchRow> staleCheckDetails = new ArrayList<>();

        for (Map<String,String> rowDataMap : parseDataList) {
            StaleCheckBatchRow staleCheckBatchRow = buildStaleCheckExtractDetailFromDataMap(rowDataMap);
            staleCheckDetails.add(staleCheckBatchRow);
        }
        
        return staleCheckDetails;
    }

    private static StaleCheckBatchRow buildStaleCheckExtractDetailFromDataMap(Map<String,String> rowDataMap) {
        StaleCheckBatchRow staleCheckBatchRow = new StaleCheckBatchRow();

        staleCheckBatchRow.setCheckIssuedDate(rowDataMap.get(StaleCheckExtractCsvFields.CHECK_ISSUED_DATE.toString()));
        staleCheckBatchRow.setBankCode(rowDataMap.get(StaleCheckExtractCsvFields.BANK_CODE.toString()));
        staleCheckBatchRow.setCheckStatus(rowDataMap.get(StaleCheckExtractCsvFields.CHECK_STATUS.toString()));
        staleCheckBatchRow.setCheckNumber(rowDataMap.get(StaleCheckExtractCsvFields.CHECK_NUMBER.toString()));
        staleCheckBatchRow.setCheckTotalAmount(rowDataMap.get(StaleCheckExtractCsvFields.CHECK_TOTAL_AMOUNT.toString()));

        return staleCheckBatchRow;
    }

}
