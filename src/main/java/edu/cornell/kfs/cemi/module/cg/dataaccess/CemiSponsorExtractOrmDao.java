package edu.cornell.kfs.cemi.module.cg.dataaccess;

import java.util.stream.Stream;

import org.kuali.kfs.module.cg.businessobject.Agency;

public interface CemiSponsorExtractOrmDao {

    Stream<Agency> getAgenciesForCemiSponsorExtractAsCloseableStream();

}
