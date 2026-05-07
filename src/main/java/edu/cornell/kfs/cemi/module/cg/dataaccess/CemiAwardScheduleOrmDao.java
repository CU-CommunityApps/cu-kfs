package edu.cornell.kfs.cemi.module.cg.dataaccess;

import java.util.stream.Stream;

import org.kuali.kfs.module.cg.businessobject.Award;

public interface CemiAwardScheduleOrmDao {

    Stream<Award> getAwardsForCemiAwardScheduleExtractAsCloseableStream();

}
