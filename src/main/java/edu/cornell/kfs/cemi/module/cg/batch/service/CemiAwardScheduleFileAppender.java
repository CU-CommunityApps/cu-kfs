package edu.cornell.kfs.cemi.module.cg.batch.service;

import java.io.IOException;

import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiExcelWriter;

public interface CemiAwardScheduleFileAppender {

    void populateAwardScheduleFileFromIntermediateDataStorage(final CemiExcelWriter fileWriter) throws IOException;

    void cleanUpIntermediateStorage() throws IOException;

}
