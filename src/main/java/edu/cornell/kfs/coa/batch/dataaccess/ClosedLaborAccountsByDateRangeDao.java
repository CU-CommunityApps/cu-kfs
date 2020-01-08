package edu.cornell.kfs.coa.batch.dataaccess;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import edu.cornell.kfs.coa.batch.businessobject.LaborClosedAccount;

public interface ClosedLaborAccountsByDateRangeDao {
    
    List <LaborClosedAccount> obtainLaborClosedAccountsDataFor(Map<String, Date> dateRange);
    
}
