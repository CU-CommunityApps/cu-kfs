package edu.cornell.kfs.cemi.vnd.batch.service;

import java.time.LocalDateTime;

public interface CemiRemitToSupplierExtractService {

    void resetState();

    void initializeExtractDateSettings();

    void populateListOfInScopeAddresses();

    void generateIntermediateRemitToSupplierExtractData(final LocalDateTime jobRunDate);

    void generateRemitToSupplierExtractFile(final LocalDateTime jobRunDate);

}
