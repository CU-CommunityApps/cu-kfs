package edu.cornell.kfs.fp.dataaccess;

import java.sql.Date;
import java.util.Collection;

import org.kuali.kfs.fp.document.DisbursementVoucherDocument;

public interface RecurringDisbursementVoucherSearchDao {

    Collection<String> findSavedDvIdsSpawnedByRecurringDvForCurrentAndPastFiscalPeriods(Date currentFisalPeriodEndDate);

    String findRecurringDvIdForSpawnedDv(String spawnedDvDocumentNumber);

}
