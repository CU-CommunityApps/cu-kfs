package edu.cornell.kfs.concur.batch.service;

import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractFile;

public interface ConcurRequestExtractFileValidationService {

    boolean fileRowCountMatchesHeaderRowCount(ConcurRequestExtractFile requestExtractFile);

    boolean fileApprovedAmountsMatchHeaderApprovedAmount(ConcurRequestExtractFile requestExtractFile);
}
