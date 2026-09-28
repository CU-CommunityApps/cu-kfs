package edu.cornell.kfs.cemi.pdp.dataaccess;

import java.time.LocalDateTime;

import org.kuali.kfs.core.api.util.type.KualiInteger;

public interface CemiPaymentElectionDao {
    
    void clearExistingListOfExtractablePayeeAchAccountGeneratedIds();
   
    void queryAndStorePayeeAchAccountGeneratedIdsForPaymentElectionExtract();
    
    void storeEmployeeIdAchAccountGeneratedIdPaymentElectionExtractRunDate(final String employeeId,
            KualiInteger achAccountGeneratdIdentifier, final LocalDateTime jobRunDate);

}
