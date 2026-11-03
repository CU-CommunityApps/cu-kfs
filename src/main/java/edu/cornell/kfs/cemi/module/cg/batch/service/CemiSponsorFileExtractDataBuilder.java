package edu.cornell.kfs.cemi.module.cg.batch.service;

import java.util.Iterator;

import org.kuali.kfs.module.cg.businessobject.Agency;

public interface CemiSponsorFileExtractDataBuilder {

    void writeSponsorFileSponsorTabExtractDataToIntermediateStorage(final Iterator<Agency> agencies);

}
