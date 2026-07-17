package edu.cornell.kfs.cemi.patterntemplate.batch.service;

import java.util.Iterator;

// Refer to actual implementation in service class CemiEXTRACTNAMEleFileExtractDataBuilderDefaultImpl

import edu.cornell.kfs.cemi.patterntemplate.batch.businessobject.CemiExampleLEGACYOBJECT;

public interface CemiEXTRACTNAMEFileExtractDataBuilder {

    void writeEXTRACTNAMEFileTABNAMETabExtractDataToIntermediateStorage(final Iterator<CemiExampleLEGACYOBJECT> legacyObjects);

}
