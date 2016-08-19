package edu.cornell.kfs.pdp.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.cornell.kfs.pdp.businessobject.PayeeACHAccountExtractDetail;

public class PayeeACHAccountExtractCsvBuilder {

    protected PayeeACHAccountExtractCsvBuilder() {
        // Do nothing; is just here to prevent public construction of a utility class.
    }

    public static List<PayeeACHAccountExtractDetail> buildPayeeACHAccountExtract(List<Map<String,String>> parseDataList) {
        List<PayeeACHAccountExtractDetail> achDetails = new ArrayList<PayeeACHAccountExtractDetail>();
        
        for (Map<String,String> rowDataMap : parseDataList) {
            achDetails.add(buildACHDetailFromDataMap(rowDataMap));
        }
        
        return achDetails;
    }

    private static PayeeACHAccountExtractDetail buildACHDetailFromDataMap(Map<String,String> rowDataMap) {
        PayeeACHAccountExtractDetail achDetail = new PayeeACHAccountExtractDetail();
        
        // Use toString() instead of name() to get the column values, due to column naming customizations for this case.
        achDetail.setEmployeeID(rowDataMap.get(PayeeACHAccountExtractCsv.EMPL_ID.toString()));
        achDetail.setNetID(rowDataMap.get(PayeeACHAccountExtractCsv.NET_ID.toString()));
        achDetail.setLastName(rowDataMap.get(PayeeACHAccountExtractCsv.LAST_NAME.toString()));
        achDetail.setFirstName(rowDataMap.get(PayeeACHAccountExtractCsv.FIRST_NAME.toString()));
        achDetail.setPaymentType(rowDataMap.get(PayeeACHAccountExtractCsv.PAYMENT_TYPE.toString()));
        achDetail.setBalanceAccount(rowDataMap.get(PayeeACHAccountExtractCsv.BALANCE_ACCT_.toString()));
        achDetail.setCompletedDate(rowDataMap.get(PayeeACHAccountExtractCsv.COMPLETE_DT.toString()));
        achDetail.setBankName(rowDataMap.get(PayeeACHAccountExtractCsv.BANK_NAME.toString()));
        achDetail.setBankRoutingNumber(rowDataMap.get(PayeeACHAccountExtractCsv.ROUTING_NO.toString()));
        achDetail.setBankAccountNumber(rowDataMap.get(PayeeACHAccountExtractCsv.ACCOUNT_NO.toString()));
        achDetail.setBankAccountType(rowDataMap.get(PayeeACHAccountExtractCsv.ACCOUNT_TYPE.toString()));
        
        return achDetail;
    }

}
