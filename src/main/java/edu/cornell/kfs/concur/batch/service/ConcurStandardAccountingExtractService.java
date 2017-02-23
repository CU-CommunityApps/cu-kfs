package edu.cornell.kfs.concur.batch.service;

import java.io.File;
import java.util.List;

import org.kuali.kfs.krad.exception.ValidationException;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;

public interface ConcurStandardAccountingExtractService {

    ConcurStandardAccountingExtractFile parseStandardAccoutingExtractFileToStandardAccountingExtractFile(File standardAccountingExtractFile) throws ValidationException;

    boolean extractPdpFeedFromStandardAccounitngExtract(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile);

}
