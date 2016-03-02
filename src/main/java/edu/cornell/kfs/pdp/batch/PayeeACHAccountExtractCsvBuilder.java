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
        achDetail.setEmployeeID(rowDataMap.get(PayeeACHAccountExtractCsv.employeeId.toString()));
        achDetail.setNetID(rowDataMap.get(PayeeACHAccountExtractCsv.netID.toString()));
        achDetail.setLastName(rowDataMap.get(PayeeACHAccountExtractCsv.lastName.toString()));
        achDetail.setFirstName(rowDataMap.get(PayeeACHAccountExtractCsv.firstName.toString()));
        achDetail.setPaymentType(rowDataMap.get(PayeeACHAccountExtractCsv.paymentType.toString()));
        achDetail.setBalanceAccount(rowDataMap.get(PayeeACHAccountExtractCsv.balanceAccount.toString()));
        achDetail.setCompletedDate(rowDataMap.get(PayeeACHAccountExtractCsv.completedDate.toString()));
        achDetail.setBankName(rowDataMap.get(PayeeACHAccountExtractCsv.bankName.toString()));
        achDetail.setBankRoutingNumber(rowDataMap.get(PayeeACHAccountExtractCsv.bankRoutingNumber.toString()));
        achDetail.setBankAccountNumber(rowDataMap.get(PayeeACHAccountExtractCsv.bankAccountNumber.toString()));
        achDetail.setBankAccountType(rowDataMap.get(PayeeACHAccountExtractCsv.bankAccountType.toString()));
        
        return achDetail;
    }

}
