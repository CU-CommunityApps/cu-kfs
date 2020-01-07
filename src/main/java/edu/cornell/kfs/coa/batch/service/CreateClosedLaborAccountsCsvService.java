package edu.cornell.kfs.coa.batch.service;

import java.io.IOException;

public interface CreateClosedLaborAccountsCsvService {
    
    void createClosedLaborAccountCsvByParameterPastDays() throws IOException;
    
}
