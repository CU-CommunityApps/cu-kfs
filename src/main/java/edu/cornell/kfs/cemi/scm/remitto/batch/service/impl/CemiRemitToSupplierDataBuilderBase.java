package edu.cornell.kfs.cemi.scm.remitto.batch.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;

import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.cemi.scm.remitto.batch.service.CemiRemitToSupplierDataBuilder;

public class CemiRemitToSupplierDataBuilderBase implements CemiRemitToSupplierDataBuilder {

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeRemitToSupplierDataToIntermediateStorage(Iterator<VendorHeader> suppliers,
            LocalDateTime jobRunDate) throws IOException {
        // TODO Auto-generated method stub

    }

}
