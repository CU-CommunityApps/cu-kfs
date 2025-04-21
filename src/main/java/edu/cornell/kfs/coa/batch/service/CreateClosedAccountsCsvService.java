package edu.cornell.kfs.coa.batch.service;

import java.io.IOException;

public interface CreateClosedAccountsCsvService {
    
    void createClosedAccountsCsvByParameterPastDays() throws IOException;
    
}
