package edu.cornell.kfs.cemi.patterntemplate.dataaccess;

import java.util.stream.Stream;

public interface CemiEXTRACTNAMEOrmDao {

    Stream<LEGACYOBJECT> getLEGACYOBJECTForCemiEXTRACTNAMEExtractAsCloseableStream();

    //This class is only present to have the pattern template compile and should be completely removed when implementing.
    public class LEGACYOBJECT {
    }
}
