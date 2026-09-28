package edu.cornell.kfs.cemi.patterntemplate.dataaccess;

import java.util.stream.Stream;

import edu.cornell.kfs.cemi.patterntemplate.batch.businessobject.CemiExampleLEGACYOBJECT;

// Refer to actual implementation in service class CemiEXTRACTNAMEExtractOrmDaoOjbImpl

public interface CemiEXTRACTNAMEExtractOrmDao {

    Stream<CemiExampleLEGACYOBJECT> getLEGACYOBJECTForCemiEXTRACTNAMEExtractAsCloseableStream();

}
