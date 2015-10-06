package edu.cornell.kfs.fp.dataaccess;

import java.sql.Date;
import java.util.List;

import org.kuali.kfs.fp.document.ProcurementCardDocument;

public interface ProcurementCardDocumentDao {    
    public List<ProcurementCardDocument> getDocumentByCarhdHolderAmountDateVendor(String cardHolder, String amount, Date transactionDate);
    
    /**
     * Gets a list of Procurement Card Document based on the amount, transaction date and net id.
     * If provided transaction date between PCDO transaction date - 3 and PCDO transaction date +21 then auto-match = true
     * 
     * @param amount
     * @param transactionDate
     * @param cardHolderNetID
     * @return a list of PCDO docs that meet the criteria
     */
    public List<ProcurementCardDocument> getDocumentByCarhdHolderNameAmountDateCardHolderNetID(String amount, Date transactionDate, String cardHolderNetID);
    
    /**
     * Gets a list of Procurement Card Document based on the edoc number.
     * 
     * @param edocNumber
     * @return a list of PCDO docs that meet the criteria
     */
    public List<ProcurementCardDocument> getDocumentByEdocNumber(String edocNumber);
}
