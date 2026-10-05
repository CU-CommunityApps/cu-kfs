package edu.cornell.kfs.cemi.pdp.dataaccess;

import org.kuali.kfs.core.api.util.type.KualiInteger;

public interface CemiPaymentElectionDao {

    void clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution();

    void queryAndStoreInScopeBusinessObjectKeysForDataExtract();

    void storeSpreadsheetRowItemKeyLegacyObjectKeyExtractRunDateMapping(final String employeeId,
            final KualiInteger achAccountGeneratedIdentifier, final String jobRunDateString);

}
