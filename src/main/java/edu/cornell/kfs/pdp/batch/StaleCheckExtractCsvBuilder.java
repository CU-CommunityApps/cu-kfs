package edu.cornell.kfs.pdp.batch;

import edu.cornell.kfs.pdp.businessobject.StaleCheckExtractDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StaleCheckExtractCsvBuilder {

    protected StaleCheckExtractCsvBuilder() {}

    public static List<StaleCheckExtractDetail> buildStaleCheckExtract(List<Map<String,String>> parseDataList, String filename) {
        List<StaleCheckExtractDetail> staleCheckDetails = new ArrayList<StaleCheckExtractDetail>();

        int lineNumber = 2;
        for (Map<String,String> rowDataMap : parseDataList) {
            StaleCheckExtractDetail staleCheckExtractDetail = buildACHDetailFromDataMap(rowDataMap);
            staleCheckExtractDetail.setFilename(filename);
            staleCheckExtractDetail.setLineNumber(Integer.toString(lineNumber++));
            staleCheckDetails.add(staleCheckExtractDetail);
        }
        
        return staleCheckDetails;
    }

    private static StaleCheckExtractDetail buildACHDetailFromDataMap(Map<String,String> rowDataMap) {
        StaleCheckExtractDetail staleCheckExtractDetail = new StaleCheckExtractDetail();
        
        // Use toString() instead of name() to get the column values, due to column naming customizations for this case.
        staleCheckExtractDetail.setCheckIssuedDate(rowDataMap.get(StaleCheckExtractCsvFields.CHECK_ISSUED_DATE.toString()));
        staleCheckExtractDetail.setBankCode(rowDataMap.get(StaleCheckExtractCsvFields.ACCOUNT_NUMBER.toString()));
        staleCheckExtractDetail.setCheckStatus(rowDataMap.get(StaleCheckExtractCsvFields.CHECK_STATUS.toString()));
        staleCheckExtractDetail.setCheckNumber(rowDataMap.get(StaleCheckExtractCsvFields.CHECK_NUMBER.toString()));
        staleCheckExtractDetail.setCheckTotalAmount(rowDataMap.get(StaleCheckExtractCsvFields.CHECK_TOTAL_AMOUNT.toString()));

        return staleCheckExtractDetail;
    }

}
