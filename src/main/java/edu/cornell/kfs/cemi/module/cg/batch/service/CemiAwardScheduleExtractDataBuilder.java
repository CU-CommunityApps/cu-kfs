package edu.cornell.kfs.cemi.module.cg.batch.service;

import java.util.Iterator;

import org.kuali.kfs.module.cg.businessobject.Award;

public interface CemiAwardScheduleExtractDataBuilder {

    void writeAwardScheduleExtractDataToIntermediateStorage(final Iterator<Award> awards, String jobRunDateString);

}
