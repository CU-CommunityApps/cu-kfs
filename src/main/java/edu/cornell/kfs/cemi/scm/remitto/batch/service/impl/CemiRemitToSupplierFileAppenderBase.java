package edu.cornell.kfs.cemi.scm.remitto.batch.service.impl;

import java.io.IOException;

import edu.cornell.kfs.cemi.scm.remitto.batch.service.CemiRemitToSupplierFileAppender;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiExcelWriter;

public class CemiRemitToSupplierFileAppenderBase implements CemiRemitToSupplierFileAppender {

    @Override
    public void populateRemitToSupplierFileFromIntermediateDataStorage(CemiExcelWriter fileWriter) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void cleanUpIntermediateStorage() throws IOException {
        // TODO Auto-generated method stub

    }

}
