package edu.cornell.kfs.cemi.vnd.batch.service;

import java.io.IOException;

import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiExcelWriter;

public interface CemiSupplierFileAppender {

    void populateSupplierFileFromIntermediateDataStorage(final CemiExcelWriter fileWriter) throws IOException;

    void cleanUpIntermediateStorage() throws IOException;

}
