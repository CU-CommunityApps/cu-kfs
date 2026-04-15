package edu.cornell.kfs.cemi.pdp.batch.service;

import java.io.IOException;

import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiExcelWriter;

public interface CemiPaymentElectionFileAppender {

    void populatePaymentElectionFileFromIntermediateDataStorage(final CemiExcelWriter fileWriter) throws IOException;

    void cleanUpIntermediateStorage() throws IOException;

}
