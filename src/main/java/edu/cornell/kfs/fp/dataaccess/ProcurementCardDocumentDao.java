package edu.cornell.kfs.fp.dataaccess;

import java.sql.Date;
import java.util.List;

import org.kuali.kfs.fp.document.ProcurementCardDocument;

public interface ProcurementCardDocumentDao {    
    public List<ProcurementCardDocument> getDocumentByCardHolderAmountDateVendor(String cardHolder, String amount, Date transactionDate);
    
    /**
     * Gets a list of Procurement Card Document based on the amount, transaction date and net id.
     * 
     * @param amount
     * @param transactionDate
     * @param cardHolderNetID
     * @return a list of PCDO docs that meet the criteria
     */
    public List<ProcurementCardDocument> getDocumentByCardHolderNameAmountDateCardHolderNetID(String amount, Date transactionDate, String cardHolderNetID);
    
    /**
     * Gets a list of Procurement Card Document based on the edoc number.
     * 
     * @param edocNumber
     * @return a list of PCDO docs that meet the criteria
     */
    public List<ProcurementCardDocument> getDocumentByEdocNumber(String edocNumber);
}
