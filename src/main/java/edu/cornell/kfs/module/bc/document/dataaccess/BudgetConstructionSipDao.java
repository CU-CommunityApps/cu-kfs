package edu.cornell.kfs.module.bc.document.dataaccess;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.cornell.kfs.module.bc.document.dataaccess.impl.BudgetConstructionSipDaoJdbc.SIPExportData;

public interface BudgetConstructionSipDao {

    /**
     * use this method to retrieve SIP data
     * @param univId
     */
    public Collection<SIPExportData>  getSIPExtractByPersonUnivId(String univId);
}
