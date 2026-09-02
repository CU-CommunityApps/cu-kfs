package edu.cornell.kfs.cemi.module.cg.batch.service;

import java.util.Iterator;

import org.kuali.kfs.module.cg.businessobject.Award;

public interface CemiAwardFileExtractDataBuilder {

//    // Tables holding gathered raw legacy data to be translated to Workday values.
//    void writeAwardFileRawAwardHeaderDataInterimStorage(final Iterator<Award> legacyObjects);
    
    // Tables holding copy of data extract sent to Huron. 
    void writeAwardFileSubmitAwardTabExtractDataToIntermediateStorage(final Iterator<Award> awards);

}
