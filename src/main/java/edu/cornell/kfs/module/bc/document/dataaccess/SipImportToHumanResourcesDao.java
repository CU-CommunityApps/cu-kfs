package edu.cornell.kfs.module.bc.document.dataaccess;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.cornell.kfs.module.bc.batch.dataaccess.impl.SipImportToHumanResourcesDaoJdbc.SipImportDataForHr;

public interface SipImportToHumanResourcesDao {

    /**
     * use this method to retrieve SIP data stored in the table CU_LD_BCN_SIP_T and format it for Human Resources
     */
    public Collection<SipImportDataForHr>  getSipImportDataForHr();
}
