package edu.cornell.kfs.cemi.module.cg.batch.service;

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;

import org.kuali.kfs.module.cg.businessobject.Award;

public interface CemiAwardScheduleDataBuilder extends Closeable {

    void writeAwardScheduleDataToIntermediateStorage(final Iterator<Award> awards,
            final LocalDateTime jobRunDate) throws IOException;

}
