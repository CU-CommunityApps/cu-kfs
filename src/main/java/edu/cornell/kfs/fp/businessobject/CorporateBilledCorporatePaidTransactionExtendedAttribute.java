package edu.cornell.kfs.fp.businessobject;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class CorporateBilledCorporatePaidTransactionExtendedAttribute extends ProcurementCardTransactionExtendedAttribute {
    private static final long serialVersionUID = -7363307192713879081L;
    
    List<CorporateBilledCorporatePaidDataRecord> corporateBilledCorporatePaidDataRecords;
    
    public CorporateBilledCorporatePaidTransactionExtendedAttribute() {
        corporateBilledCorporatePaidDataRecords = new ArrayList<CorporateBilledCorporatePaidDataRecord>();
    }

    public List<CorporateBilledCorporatePaidDataRecord> getCorporateBilledCorporatePaidDataRecords() {
        return corporateBilledCorporatePaidDataRecords;
    }

    public void setCorporateBilledCorporatePaidDataRecords(
            List<CorporateBilledCorporatePaidDataRecord> corporateBilledCorporatePaidDataRecords) {
        this.corporateBilledCorporatePaidDataRecords = corporateBilledCorporatePaidDataRecords;
    }
    
    @Override
    protected CorporateBilledCorporatePaidDataRecord buildPurchasingDataRecordObject() {
        return new CorporateBilledCorporatePaidDataRecord();
    }
    
    @Override
    public int addAddendumLines(BufferedReader bufferedFileReader, int lineCount) throws IOException, ParseException {
        for (PurchasingDataRecord purchasingDataRecord : parseUSBankType50Lines(bufferedFileReader, lineCount)) {
            CorporateBilledCorporatePaidDataRecord cbcpDataRecord = (CorporateBilledCorporatePaidDataRecord) purchasingDataRecord;
            getCorporateBilledCorporatePaidDataRecords().add(cbcpDataRecord);
        }
        return lineCount + this.getCorporateBilledCorporatePaidDataRecords().size();
      }

}
