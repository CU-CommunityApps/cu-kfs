package edu.cornell.kfs.coa.batch.dataaccess;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import edu.cornell.kfs.coa.batch.businessobject.ClosedAccount;

public interface ClosedAccountsByDateRangeDao {
    
    List <ClosedAccount> obtainClosedAccountsDataFor(Map<String, Date> dateRange);
    
}
