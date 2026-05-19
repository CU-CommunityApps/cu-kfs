package edu.cornell.kfs.cemi.scm.remitto.batch.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;

import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.cemi.module.cg.batch.service.impl.CemiAwardScheduleDataBuilderBase;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardScheduleDao;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;

public class CemiAwardScheduleDataBuilderCsvImpl extends CemiAwardScheduleDataBuilderBase {

    protected CemiAwardScheduleDataBuilderCsvImpl(CemiOutputDefinition outputDefinition,
            CemiAwardScheduleDao cemiAwardScheduleDao, DateTimeService dateTimeService,
            BusinessObjectService businessObjectService, LocalDateTime jobRunDate, boolean maskSensitiveData) {
        super(outputDefinition, cemiAwardScheduleDao, dateTimeService, businessObjectService, jobRunDate, maskSensitiveData);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void writeDataToIntermediateStorage(String sheetName, Object rowObject) throws IOException {
        // TODO Auto-generated method stub

    }

}
