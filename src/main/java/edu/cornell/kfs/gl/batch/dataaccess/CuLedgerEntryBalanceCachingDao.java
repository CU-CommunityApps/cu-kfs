package edu.cornell.kfs.gl.batch.dataaccess;

import java.util.List;

public interface CuLedgerEntryBalanceCachingDao {
	
    public List compareEntryHistory(String entryTable, String historyTable, int fiscalYear);
    
    public List compareBalanceHistory(String entryTable, String historyTable, int fiscalYear);
    
    public List accountBalanceCompareHistory(String accountBalanceTable, String historyTable, int fiscalYear);
    
    public List encumbranceCompareHistory(String encumbranceTable, String historyTable, int fiscalYear);


}
